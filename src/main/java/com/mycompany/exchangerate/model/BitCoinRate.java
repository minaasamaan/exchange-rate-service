package com.mycompany.exchangerate.model;

import java.time.Instant;
import java.time.LocalDate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static javax.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity(name= "bit_coin_rates")
public class BitCoinRate {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private double rate;
    @NotNull
    private LocalDate date;
    @NotNull
    @Column(name="last_modified")
    private Instant lastModified;
}
