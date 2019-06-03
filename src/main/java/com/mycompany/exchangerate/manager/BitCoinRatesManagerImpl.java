package com.mycompany.exchangerate.manager;

import com.mycompany.exchangerate.client.ExchangeRatesClient;
import com.mycompany.exchangerate.exception.ExchangeRateException;
import com.mycompany.exchangerate.model.BitCoinRate;
import com.mycompany.exchangerate.repository.BitCoinRateRepository;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.netflix.hystrix.contrib.javanica.annotation.HystrixProperty;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static com.mycompany.exchangerate.exception.ProcessingError.EMPTY_HISTORY;
import static com.mycompany.exchangerate.exception.ProcessingError.START_AFTER_END;

/**
 * @see com.mycompany.exchangerate.manager.BitCoinRatesManager
 */
@Service
public class BitCoinRatesManagerImpl implements BitCoinRatesManager{

    private final Log logger = LogFactory.getLog(getClass());

    private final static String DEFAULT_CURRENCY = "USD";

    @Autowired
    private ExchangeRatesClient exchangeRatesClient;

    @Autowired
    private BitCoinRateRepository bitCoinRateRepository;

    /**
     * {@inheritDoc}
     */
    @HystrixCommand(fallbackMethod = "getTodayRateFromHistory",
                    commandProperties = {
                            @HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds",
                                             value = "1000")})
    @Override
    public Double getLatestRate() {
        return exchangeRatesClient.getRate(DEFAULT_CURRENCY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<BitCoinRate> getHistory(LocalDate startDate,
                                        LocalDate endDate,
                                        Pageable pageable) {
        if (endDate == null) {
            endDate = LocalDate.now(ZoneOffset.UTC);
        }

        if (startDate == null) {
            return bitCoinRateRepository.getBitCoinRateByDateLessThanEqual(endDate, pageable);
        }

        if(startDate.isAfter(endDate)){
            throw new ExchangeRateException(START_AFTER_END);
        }
        return bitCoinRateRepository.getBitCoinRateByDateBetween(startDate, endDate, pageable);
    }

    /**
     * A Fallback for {@link BitCoinRatesManagerImpl#getLatestRate()} )}
     */
    private Double getTodayRateFromHistory() {
        logger.warn("Using fallback getTodayRateFromHistory after failure of getLatestRate!");
        BitCoinRate latestFromHistory = bitCoinRateRepository.getBitCoinRateByDate(LocalDate.now(ZoneOffset.UTC));

        if (latestFromHistory != null){
            return latestFromHistory.getRate();
        }

        logger.warn("Fallback getTodayRateFromHistory failed, no history data exists!");

        throw new ExchangeRateException(EMPTY_HISTORY);
    }
}
