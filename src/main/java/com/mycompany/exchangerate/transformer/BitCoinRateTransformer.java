package com.mycompany.exchangerate.transformer;

import com.mycompany.exchangerate.dto.BitCoinRateDto;
import com.mycompany.exchangerate.model.BitCoinRate;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
public class BitCoinRateTransformer {

    public Page<BitCoinRateDto> transform(Page<BitCoinRate> bitCoinRates) {
        return bitCoinRates.map(bitCoinRate -> BitCoinRateDto.builder()
                                                             .rate(bitCoinRate.getRate())
                                                             .date(bitCoinRate.getDate())
                                                             .build());
    }
}
