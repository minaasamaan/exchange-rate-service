package com.mycompany.exchangerate.repository;

import com.mycompany.exchangerate.model.BitCoinRate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.time.LocalDate;
import java.util.List;

public interface BitCoinRateRepository extends JpaRepository<BitCoinRate,Long> {

    Page<BitCoinRate> getBitCoinRateByDateBetween(LocalDate from,
                                                  LocalDate to,
                                                  Pageable pageable);

    Page<BitCoinRate> getBitCoinRateByDateLessThanEqual(LocalDate to, Pageable pageable);

    BitCoinRate getBitCoinRateByDate(LocalDate date);

    @Query("select max(date) from com.mycompany.exchangerate.model.BitCoinRate bcr")
    LocalDate getMaxBitCoinRateDate();
}
