package com.kodilla.bungenicsf.service;

import com.kodilla.bungenicsf.dto.WeatherDto;
import com.kodilla.bungenicsf.utils.WeatherUtils;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Service
public class WeatherClientService {

    private final RestClient restClient;

    public WeatherClientService(RestClient restClient) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = restClient.mutate()
                .requestFactory(requestFactory)
                .build();
    }

    public void validateLocation(String locationName) {
        if (locationName == null || locationName.trim().isEmpty()) {
            throw new IllegalArgumentException("Location cannot be empty");
        }

        restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/weather")
                        .queryParam("city", locationName)
                        .build())
                .retrieve()
                .toBodilessEntity();
    }

    public String getCurrentWeather(String locationName) {
        try {
            WeatherDto dto = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/weather")
                            .queryParam("city", locationName)
                            .build())
                    .retrieve()
                    .body(WeatherDto.class);

            if (dto != null) {
                String icon = WeatherUtils.getWeatherIcon(dto.weatherCode());
                return String.format("📍 %s | %s %s, %.1f°C, Wind: %.1f km/h",
                        locationName, icon, dto.weatherDescription(), dto.temperature(), dto.windSpeed());
            }
        } catch (Exception e) {
            return "📍 " + locationName + " (Weather service down)";
        }
        return "📍 " + locationName;
    }
}