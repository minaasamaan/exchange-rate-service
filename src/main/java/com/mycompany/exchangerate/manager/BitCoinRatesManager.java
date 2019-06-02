package com.mycompany.exchangerate.manager;

import com.mycompany.exchangerate.client.ExchangeRatesClient;
import com.mycompany.exchangerate.model.BitCoinRate;
import com.mycompany.exchangerate.repository.BitCoinRateRepository;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
public class BitCoinRatesManager {

    private final static String DEFAULT_CURRENCY = "USD";

    @Autowired
    private ExchangeRatesClient exchangeRatesClient;

    @Autowired
    private BitCoinRateRepository bitCoinRateRepository;

    @HystrixCommand(fallbackMethod = "getTodayRateFromHistory",
                    commandProperties = {
                            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",
                                             value = "1000")})
    public Double getLatestRate() {
        return exchangeRatesClient.getRate(DEFAULT_CURRENCY);
    }

    public Page<BitCoinRate> getHistory(LocalDate startDate,
                                        LocalDate endDate,
                                        Pageable pageable) {
        if (endDate == null) {
            endDate = LocalDate.now(ZoneOffset.UTC);
        }

        if (startDate == null) {
            return bitCoinRateRepository.getBitCoinRateByDateLessThanEqual(endDate, pageable);
        }
        return bitCoinRateRepository.getBitCoinRateByDateBetween(startDate, endDate, pageable);
    }

    //fallback for getLatestRate
    private Double getTodayRateFromHistory() {
        BitCoinRate latestFromHistory= bitCoinRateRepository.getBitCoinRateByDate(LocalDate.now(ZoneOffset.UTC));
        return latestFromHistory==null? null: latestFromHistory.getRate();
    }
}
