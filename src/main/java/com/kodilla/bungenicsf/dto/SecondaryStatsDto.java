package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kodilla.bungenicsf.domain.rabbit.AttackType;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SecondaryStatsDto (
        Long id,
        Float weight,
        Float nutritionLevel,
        Float life,
        Float stress,
        Float age,
        Float strength,
        Float agility,
        Float intelligence,
        Float basicStrength,
        Float basicAgility,
        Float basicIntelligence,
        String preferredAttack
)
{}
