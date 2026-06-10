package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RoomDto(
        Long id,
        Integer slots,
        List<RabbitDto> rabbits
) {}