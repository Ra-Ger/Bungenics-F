package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record StructureDto(
        Long id,
        Long rabbitFarmId,
        Integer slots,
        String structureType,
        Integer gridIndex,
        List<RoomDto> rooms
) {}