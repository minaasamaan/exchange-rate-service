package com.mycompany.exchangerate.manager;

import com.mycompany.exchangerate.model.BitCoinRate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * A manager to handle requests for latest rates and rates history, through delegating to either downstream dependency for latest rates, or
 */
public interface BitCoinRatesManager {

    /**
     * Gets latest BitCoin rate with respect to hardcoded default currency. This should be achieved by delegating to a downstream dependency.
     * This method should have a proper fallback to latest rate from local history in case of failover on consuming downstream.
     * @return
     */
    Double getLatestRate();

    /**
     * Gets paginated {@link BitCoinRate} history between {@code startDate} and {@code endDate} inclusive.
     * @param startDate defaults to start of history.
     * @param endDate defaults to current date.
     * @param pageable {@link Pageable}
     * @return {@link Page<BitCoinRate>}
     */
    Page<BitCoinRate> getHistory(LocalDate startDate,
                                 LocalDate endDate,
                                 Pageable pageable);
}
