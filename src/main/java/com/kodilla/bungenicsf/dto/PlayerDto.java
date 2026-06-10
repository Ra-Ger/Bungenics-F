package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record PlayerDto(
        Long id,
        Long version,
        String name,
        String location,
        BigDecimal money
) {}