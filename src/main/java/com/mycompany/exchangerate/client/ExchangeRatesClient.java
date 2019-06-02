package com.mycompany.exchangerate.client;

import com.mycompany.exchangerate.exception.ExchangeRateException;
import com.mycompany.exchangerate.transformer.BitCoinRateTransformer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Instant;

import static com.mycompany.exchangerate.exception.ProcessingError.INVALID_DOWNSTREAM_RESPONSE;

/**
 * A client class that encapsulates calls to downstream dependency used for getting either latest bitcoin rate or historical rates in case of backfilling
 */
@Component
public class ExchangeRatesClient {
    private static final int    BTC           = 100000000;

    private static final String FROM_BTC_PATH = "/frombtc";
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BitCoinRateTransformer bitCoinRateTransformer;

    @Value("${api.exchange_rates.url}")
    private String url;

    /**
     * Gets latest Bitcoin rate given {@code currency}
     * @param currency
     * @return the latest rate in {@code currency} of one Bitcoin
     */
    public Double getRate(String currency) {
        return getRate(null, currency);
    }

    /**
     * Gets historical Bitcoin rate given {@code currency} and a specific point in time donated by {@code time}
     * @param time The {@link Instant} point of time the rate is needed for
     * @param currency
     * @return the rate at {@code time} in {@code currency} of one Bitcoin
     */
    public Double getRate(Instant time, String currency) {

        UriComponentsBuilder queryBuilder = UriComponentsBuilder.fromHttpUrl(url.concat(FROM_BTC_PATH))
                                                                .queryParam("value", BTC)
                                                                .queryParam("currency", currency);
        if (null != time) {
            queryBuilder.queryParam("time", time.toEpochMilli());
        }

        String rateString = restTemplate.getForObject(queryBuilder.build().toUriString(), String.class);

        double rate;
        try {
            rate = DecimalFormat.getNumberInstance().parse(rateString).doubleValue();
        }
        catch (ParseException e) {
            logger.error("Invalid value: " + rateString, e);
            throw new ExchangeRateException(INVALID_DOWNSTREAM_RESPONSE);
        }
        return rate;
    }
}
