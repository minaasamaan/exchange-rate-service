package com.mycompany.exchangerate.resource;

import com.mycompany.core.resource.ApiResource;
import com.mycompany.exchangerate.dto.BitCoinRateDto;
import com.mycompany.exchangerate.manager.BitCoinRatesManagerImpl;
import com.mycompany.exchangerate.transformer.BitCoinRateTransformer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneOffset;

import static com.mycompany.core.resource.ApiResource.API_V1;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(API_V1)
public class BitCoinRatesResource implements ApiResource {

    @Autowired
    private BitCoinRatesManagerImpl bitCoinRatesManager;
    @Autowired
    private BitCoinRateTransformer  bitCoinRateTransformer;

    @GetMapping(value = "rate",
                produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getRate() {
        return ResponseEntity.ok(BitCoinRateDto.builder().rate(bitCoinRatesManager.getLatestRate()).date(LocalDate.now(
                ZoneOffset.UTC)).build());
    }

    @GetMapping(value = "history",
                produces = APPLICATION_JSON_VALUE)
    public ResponseEntity getHistory(@RequestParam(name = "start",
                                                   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                             LocalDate startDate,
                                     @RequestParam(name = "end",
                                                   required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                                             LocalDate endDate,
                                     Pageable pageable,
                                     PagedResourcesAssembler<BitCoinRateDto> assembler) {
        Page<BitCoinRateDto> dtoPage = bitCoinRateTransformer.transform(bitCoinRatesManager.getHistory(startDate,
                                                                                                       endDate,
                                                                                                       pageable));
        return ResponseEntity.ok(assembler.toResource(dtoPage));
    }
}
