package com.mycompany.exchangerate.client;

import com.mycompany.exchangerate.transformer.BitCoinRateTransformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.time.Instant;

@Component
public class ExchangeRatesClient {

    private static final String FROM_BTC_PATH = "/frombtc";
    private static final int    BTC              = 100000000;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BitCoinRateTransformer bitCoinRateTransformer;

    @Value("${api.exchange_rates.url}")
    private String url;

    public Double getRate(String currency) {
        return getRate(null, currency);
    }

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
            throw new RuntimeException("Invalid value: " + rateString);
        }
        return rate;
    }
}
