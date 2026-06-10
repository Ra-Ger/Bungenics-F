package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record AdventureDto(
        Long id,
        String name,
        Long playerId,
        Long rabbitId,
        String type,
        @JsonFormat(shape = JsonFormat.Shape.ANY)
        LocalDateTime endTime,
        String status,
        List<AdventureEventDto> adventureEvents
) {}