package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RabbitFarmDto(
        Long id,
        Long playerId,
        Float hayAmount,
        Float spinachAmount,
        Float carrotAmount,
        Float lettuceAmount,
        List<StructureDto> structures
) {}