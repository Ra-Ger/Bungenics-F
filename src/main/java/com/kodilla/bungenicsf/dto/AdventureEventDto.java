package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdventureEventDto(
        Long id,
        String name,
        String result,
        BigDecimal goldReward,
        Float carrotReward,
        Float lettuceReward,
        Float spinachReward
) {}