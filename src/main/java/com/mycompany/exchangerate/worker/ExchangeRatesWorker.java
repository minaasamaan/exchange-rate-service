package com.mycompany.exchangerate.worker;

import com.mycompany.exchangerate.client.ExchangeRatesClient;
import com.mycompany.exchangerate.model.BitCoinRate;
import com.mycompany.exchangerate.repository.BitCoinRateRepository;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;

/**
 * A background self-healing worker, that fetches latest Bitcoin rates and update database accordingly.
 */
@Component
public class ExchangeRatesWorker {

    private final Log logger = LogFactory.getLog(getClass());

    private final static String DEFAULT_CURRENCY = "USD";

    @Autowired
    private ExchangeRatesClient exchangeRatesClient;

    @Autowired
    private BitCoinRateRepository bitCoinRateRepository;

    @Value("${scheduler.exchange_rates.enabled}")
    private Boolean isSchedulingEnabled;

    @Scheduled(fixedDelayString = "${scheduler.exchange_rates.interval_in_millis}")
    public void execute() {
        if (isSchedulingEnabled) {
            updateRates();
        }
    }

    void updateRates() {
        if (logger.isTraceEnabled()) {
            logger.trace(String.format("Starting updateRates at %s", LocalDateTime.now(ZoneOffset.UTC)));
        }

        LocalDate lastProcessedDate = bitCoinRateRepository.getMaxBitCoinRateDate();
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        //self-healing mechanism to back-fill missing dates since last successfully inserted date, also to update yesterday's rate.
        while (lastProcessedDate != null && lastProcessedDate.isBefore(currentDate)) {
            logger.info(String.format("Self-healing for date: %s", lastProcessedDate));

            LocalDateTime endOfDay = lastProcessedDate.atTime(LocalTime.MAX);
            Instant endOfDayInstant = endOfDay.toInstant(ZoneOffset.UTC);
            Double rate = exchangeRatesClient.getRate(endOfDayInstant, DEFAULT_CURRENCY);

            upsert(rate, lastProcessedDate, endOfDayInstant);

            lastProcessedDate = lastProcessedDate.plusDays(1);
        }

        //update current rate
        Double rate = exchangeRatesClient.getRate(DEFAULT_CURRENCY);
        upsert(rate, currentDate, Instant.now());

        if (logger.isTraceEnabled()) {
            logger.trace(String.format("Finalizing updateRates at %s", LocalDateTime.now(ZoneOffset.UTC)));
        }
    }

    private void upsert(double rate, LocalDate localDate, Instant lastModified) {

        BitCoinRate currentRate = bitCoinRateRepository.getBitCoinRateByDate(localDate);

        Long id = null;

        if (currentRate != null) {
            id = currentRate.getId();
        }
        bitCoinRateRepository.save(BitCoinRate.builder()
                                              .id(id)
                                              .rate(rate)
                                              .date(localDate)
                                              .lastModified(lastModified)
                                              .build());
    }
}
