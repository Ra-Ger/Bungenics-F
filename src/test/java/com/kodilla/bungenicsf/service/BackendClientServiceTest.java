package com.kodilla.bungenicsf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodilla.bungenicsf.dto.PlayerDto;
import com.kodilla.bungenicsf.exception.BackendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
class BackendClientServiceTest {

    private static final String BASE_URL = "http://localhost:8080";

    @Mock
    private WeatherClientService weatherClientService;

    private BackendClientService backendClientService;
    private MockRestServiceServer mockServer;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestClient.Builder restClientBuilder = RestClient.builder().baseUrl(BASE_URL);
        mockServer = MockRestServiceServer.bindTo(restClientBuilder).build();
        RestClient restClient = restClientBuilder.build();

        backendClientService = new BackendClientService(restClient, weatherClientService);
    }

    @Test
    @DisplayName("Should send POST request and return created PlayerDto when createPlayer is called")
    void createPlayer_ShouldReturnCreatedPlayer() throws Exception {
        // Given
        String name = "Jan";
        String location = "Warsaw";
        PlayerDto expectedResponse = new PlayerDto(1L, 100L, name, location, new BigDecimal("500.00"));

        doNothing().when(weatherClientService).validateLocation(location);

        mockServer.expect(requestTo(BASE_URL + "/players"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedResponse), MediaType.APPLICATION_JSON));

        // When
        PlayerDto actualResult = backendClientService.createPlayer(name, location);

        // Then
        mockServer.verify();
        assertNotNull(actualResult);
        assertEquals(1L, actualResult.id());
        assertEquals("Jan", actualResult.name());
        assertEquals("Warsaw", actualResult.location());
    }

    @Test
    @DisplayName("Should send GET request to /players/{id} and return PlayerDto")
    void getPlayer_ShouldReturnPlayerDto() throws Exception {
        // Given
        Long playerId = 1L;
        PlayerDto expectedPlayer = new PlayerDto(playerId, 100L, "Jan", "Warsaw", new BigDecimal("500.00"));

        mockServer.expect(requestTo(BASE_URL + "/players/" + playerId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(objectMapper.writeValueAsString(expectedPlayer), MediaType.APPLICATION_JSON));

        // When
        PlayerDto actualPlayer = backendClientService.getPlayer(playerId);

        // Then
        mockServer.verify();
        assertNotNull(actualPlayer);
        assertEquals(playerId, actualPlayer.id());
        assertEquals("Jan", actualPlayer.name());
    }

    @Test
    @DisplayName("Should parse backend error body and throw BackendException on HTTP error status")
    void getPlayer_ShouldThrowBackendException_WhenBackendReturnsError() {
        // Given
        Long playerId = 999L;
        String errorJsonBody = "{\"message\":\"Player with id 999 not found\"}";

        mockServer.expect(requestTo(BASE_URL + "/players/" + playerId))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withBadRequest().body(errorJsonBody).contentType(MediaType.APPLICATION_JSON));

        // When & Then
        BackendException exception = assertThrows(
                BackendException.class,
                () -> backendClientService.getPlayer(playerId)
        );

        mockServer.verify();
        assertEquals("Player with id 999 not found", exception.getMessage());
    }


}