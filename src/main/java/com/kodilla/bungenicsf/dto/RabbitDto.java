package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RabbitDto(
        Long id,
        Long playerId,
        String name,
        String breed,
        String sex,
        Float weight,
        Float adultWeight,
        Float nutritionLevel,
        Float life,
        Float stress,
        Float age,
        Float maxLifetime,
        Long motherId,
        Long fatherId,
        String status,
        LocalDateTime restEndTime,
        LocalDateTime breedingEndTime,
        LocalDateTime adventureEndTime,
        LocalDateTime vetEndTime,
        LocalDateTime trainingEndTime,
        SecondaryStatsDto secondaryStats,
        Set<String> traits
) {}