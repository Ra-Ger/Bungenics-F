package com.kodilla.bungenicsf.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WeatherDto(
        double temperature,
        double humidity,
        double windSpeed,
        int weatherCode,
        String weatherDescription,
        String location
) {}