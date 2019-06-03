package com.mycompany.exchangerate.worker;

import com.mycompany.Application;
import com.mycompany.exchangerate.model.BitCoinRate;
import com.mycompany.exchangerate.repository.BitCoinRateRepository;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.client.ExpectedCount;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK,
                classes = Application.class)
public class ExchangeRatesWorkerIntegrationTest {

    @Autowired
    private ExchangeRatesWorker testee;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private BitCoinRateRepository rateRepository;

    @Value("${api.exchange_rates.url}")
    private String url;

    private MockRestServiceServer mockServer;

    @Before
    public void init() {
        rateRepository.deleteAll();
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void shouldBackFillMissingDates() throws URISyntaxException {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        LocalDate twoDaysAgo = today.minusDays(2);
        LocalDate yesterday = today.minusDays(1);

        rateRepository.save(BitCoinRate.builder().rate(12.12).date(twoDaysAgo).lastModified(Instant.now()).build());

        mockServer.expect(ExpectedCount.once(),
                          requestTo(new URI(url + "/frombtc?value=100000000&currency=USD&time=" + twoDaysAgo.atTime(
                                  LocalTime.MAX).toInstant(ZoneOffset.UTC).toEpochMilli())))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withStatus(HttpStatus.OK)
                                      .body("1,234.56")
                  );

        mockServer.expect(ExpectedCount.once(),
                          requestTo(new URI(url + "/frombtc?value=100000000&currency=USD&time=" + yesterday.atTime(
                                  LocalTime.MAX).toInstant(ZoneOffset.UTC).toEpochMilli())))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withStatus(HttpStatus.OK)
                                      .body("2,234.56")
                  );

        mockServer.expect(ExpectedCount.once(),
                          requestTo(new URI(url + "/frombtc?value=100000000&currency=USD")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withStatus(HttpStatus.OK)
                                      .body("3,234.56")
                  );

        testee.updateRates();

        verifyRate(1234.56, twoDaysAgo);
        verifyRate(2234.56, yesterday);
        verifyRate(3234.56, today);

        mockServer.verify();

        assertEquals(3, rateRepository.count());
    }

    @Test
    public void shouldInsertCurrentDateIfNotExists() throws URISyntaxException {
        LocalDate today = LocalDate.now(ZoneOffset.UTC);

        mockServer.expect(ExpectedCount.once(),
                          requestTo(new URI(url + "/frombtc?value=100000000&currency=USD")))
                  .andExpect(method(HttpMethod.GET))
                  .andRespond(withStatus(HttpStatus.OK)
                                      .body("1,234.56")
                  );

        assertNull(rateRepository.getBitCoinRateByDate(today));

        testee.updateRates();

        verifyRate(1234.56, today);

        mockServer.verify();

        assertEquals(1, rateRepository.count());
    }

    private void verifyRate(double rate, LocalDate date) {

        BitCoinRate actual = rateRepository.getBitCoinRateByDate(date);

        assertNotNull(actual);

        assertEquals(rate, actual.getRate(), 0);
    }
}
