package com.mycompany.exchangerate.resource;

import com.mycompany.Application;
import com.mycompany.exchangerate.model.BitCoinRate;
import com.mycompany.exchangerate.repository.BitCoinRateRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
                classes = Application.class)
@AutoConfigureMockMvc
public class BitCoinRatesResourceIntegrationTest {

    private static final String GET_LATEST_RATE = "/v1/rate";

    private static final String GET_HISTORY = "/v1/history?start=%s&end=%s&page=%s&size=%s";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BitCoinRateRepository rateRepository;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${api.exchange_rates.url}")
    private String url;

    private MockRestServiceServer mockServer;

    @Before
    public void init() {
        rateRepository.deleteAll();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldReturnLatestRate() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                          requestTo(new URI(url + "/frombtc?value=100000000&currency=USD")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withStatus(HttpStatus.OK)
                                      .body("1,234.56")
                  );

        mockMvc.perform(get(GET_LATEST_RATE))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$.rate").value(1234.56))
               .andExpect(jsonPath("$.date").value(LocalDate.now(ZoneOffset.UTC).toString()));

        mockServer.verify();
    }

    @Test
    public void shouldFallbackToLatestHistoryIfDownstreamFailed() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate yesterday = today.minusDays(1);

        rateRepository.save(BitCoinRate.builder().rate(12.12).date(twoDaysAgo).lastModified(Instant.now()).build());
        rateRepository.save(BitCoinRate.builder().rate(11.11).date(yesterday).lastModified(Instant.now()).build());
        rateRepository.save(BitCoinRate.builder().rate(10.1).date(today).lastModified(Instant.now()).build());

        mockServer.expect(ExpectedCount.once(),
                          requestTo(new URI(url + "/frombtc?value=100000000&currency=USD")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                  );

        mockMvc.perform(get(GET_LATEST_RATE))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$.rate").value(10.1))
               .andExpect(jsonPath("$.date").value(today.toString()));

        mockServer.verify();
    }

    @Test
    public void shouldValidateStartEndDates() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate yesterday = today.minusDays(1);

        mockMvc.perform(get(String.format(GET_HISTORY, today, yesterday, 0, 2)))
               .andExpect(status().isBadRequest())
               .andDo(print());
    }

    @Test
    public void shouldHandleDownstreamFailedAndNoHistory() throws Exception {
        mockServer.expect(ExpectedCount.once(),
                          requestTo(new URI(url + "/frombtc?value=100000000&currency=USD")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withStatus(HttpStatus.SERVICE_UNAVAILABLE)
                  );

        mockMvc.perform(get(GET_LATEST_RATE))
               .andExpect(status().isServiceUnavailable())
               .andDo(print());

        mockServer.verify();
    }

    @Test
    public void shouldReturnHistoricalRatesWithPagination() throws Exception {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate yesterday = today.minusDays(1);

        rateRepository.save(BitCoinRate.builder().rate(12.12).date(twoDaysAgo).lastModified(Instant.now()).build());
        rateRepository.save(BitCoinRate.builder().rate(11.11).date(yesterday).lastModified(Instant.now()).build());
        rateRepository.save(BitCoinRate.builder().rate(10.1).date(today).lastModified(Instant.now()).build());

        mockMvc.perform(get(String.format(GET_HISTORY, twoDaysAgo, today, 0, 2)))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$.content[0].rate").value(12.12))
               .andExpect(jsonPath("$.content[0].date").value(twoDaysAgo.toString()))
               .andExpect(jsonPath("$.content[1].rate").value(11.11))
               .andExpect(jsonPath("$.content[1].date").value(yesterday.toString()))
               .andExpect(jsonPath("$.content[2].rate").doesNotExist())
               .andExpect(jsonPath("$.content[2].date").doesNotExist());

        mockMvc.perform(get(String.format(GET_HISTORY, twoDaysAgo, today, 1, 2)))
               .andExpect(status().isOk())
               .andDo(print())
               .andExpect(jsonPath("$.content[0].rate").value(10.1))
               .andExpect(jsonPath("$.content[0].date").value(today.toString()))
               .andExpect(jsonPath("$.content[1].rate").doesNotExist())
               .andExpect(jsonPath("$.content[1].date").doesNotExist());
    }
}
