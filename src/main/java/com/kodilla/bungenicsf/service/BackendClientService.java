package com.kodilla.bungenicsf.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kodilla.bungenicsf.dto.*;
import com.kodilla.bungenicsf.exception.BackendException;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
public class BackendClientService {

    private final RestClient restClient;
    private final WeatherClientService weatherClientService;

    public BackendClientService(RestClient restClient, WeatherClientService weatherClientService) {
        this.weatherClientService = weatherClientService;

        this.restClient = restClient.mutate()
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    String body = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);
                    throw new BackendException(parseErrorMessage(body));
                })
                .build();
    }

    private String parseErrorMessage(String body) {
        if (body == null || body.isBlank()) {
            return "Unknown backend error occurred";
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(body);
            if (node.isArray() && !node.isEmpty()) {
                JsonNode first = node.get(0);
                if (first.has("message")) {
                    return first.get("message").asText();
                }
            } else if (node.isObject()) {
                if (node.has("message")) {
                    return node.get("message").asText();
                } else if (node.has("error")) {
                    return node.get("error").asText();
                }
            }
        } catch (Exception ignored) {
        }
        return body;
    }

    public void validateLocation(String location) {
        weatherClientService.validateLocation(location);
    }

    public PlayerDto createPlayer(String name, String location) {
        validateLocation(location);

        PlayerDto newPlayer = new PlayerDto(null, null, name, location, null);
        return restClient.post()
                .uri("/players")
                .contentType(MediaType.APPLICATION_JSON)
                .body(newPlayer)
                .retrieve()
                .body(PlayerDto.class);
    }

    public RabbitFarmDto createFarm(Long playerId) {
        RabbitFarmDto newFarm = new RabbitFarmDto(null, playerId, null, null, null, null, List.of());
        return restClient.post()
                .uri("/farms")
                .contentType(MediaType.APPLICATION_JSON)
                .body(newFarm)
                .retrieve()
                .body(RabbitFarmDto.class);
    }

    public PlayerDto getPlayer(Long id) {
        return restClient.get()
                .uri("/players/" + id)
                .retrieve()
                .body(PlayerDto.class);
    }

    public List<RabbitDto> getAllRabbits() {
        return restClient.get().uri("/rabbits").retrieve()
                .body(new ParameterizedTypeReference<List<RabbitDto>>() {});
    }

    public List<PlayerDto> getAllPlayers() {
        return restClient.get().uri("/players").retrieve()
                .body(new ParameterizedTypeReference<List<PlayerDto>>() {});
    }

    public List<RabbitFarmDto> getAllFarms() {
        return restClient.get().uri("/farms").retrieve()
                .body(new ParameterizedTypeReference<List<RabbitFarmDto>>() {});
    }

    public void deletePlayer(Long id) {
        restClient.delete()
                .uri("/players/" + id)
                .retrieve()
                .toBodilessEntity();
    }

    public BigDecimal getFoodPrice(String foodType, Float amount) {
        return restClient.get()
                .uri(uriBuilder -> uriBuilder.path("/shop/food-price")
                        .queryParam("foodType", foodType)
                        .queryParam("amount", amount).build())
                .retrieve().body(BigDecimal.class);
    }

    public BigDecimal getRabbitSellValue(Long rabbitId) {
        return restClient.get()
                .uri("/shop/rabbit-value/" + rabbitId)
                .retrieve().body(BigDecimal.class);
    }

    public List<RabbitDto> getMarketRabbits() {
        return restClient.get().uri("/shop/market").retrieve()
                .body(new ParameterizedTypeReference<List<RabbitDto>>() {});
    }

    public void buyRabbit(Long playerId, Long rabbitId) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/shop/buy-rabbit")
                        .queryParam("playerId", playerId)
                        .queryParam("rabbitId", rabbitId).build())
                .retrieve().toBodilessEntity();
    }

    public void sellRabbit(Long playerId, Long rabbitId) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/shop/sell")
                        .queryParam("playerId", playerId)
                        .queryParam("rabbitId", rabbitId).build())
                .retrieve().toBodilessEntity();
    }

    public void buyFood(Long playerId, String foodType, Float amount) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/shop/buy")
                        .queryParam("playerId", playerId)
                        .queryParam("foodType", foodType)
                        .queryParam("amount", amount).build())
                .retrieve().toBodilessEntity();
    }

    public void sendRabbitOnAdventure(Long playerId, Long rabbitId, String type) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/adventures/send")
                        .queryParam("playerId", playerId)
                        .queryParam("rabbitId", rabbitId)
                        .queryParam("type", type).build())
                .retrieve().toBodilessEntity();
    }

    public List<AdventureDto> getCompletedAdventures(Long playerId) {
        return restClient.get()
                .uri("/adventures/completed/" + playerId)
                .retrieve()
                .body(new ParameterizedTypeReference<List<AdventureDto>>() {});
    }

    public List<StructureDto> getAllStructures() {
        return restClient.get().uri("/structures").retrieve()
                .body(new ParameterizedTypeReference<List<StructureDto>>() {});
    }

    public void buildStructure(Long farmId, String type, Integer gridIndex) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/structures/build")
                        .queryParam("farmId", farmId)
                        .queryParam("type", type)
                        .queryParam("gridIndex", gridIndex).build())
                .retrieve().toBodilessEntity();
    }

    public void addRoom(Long structureId) {
        addRoomToStructure(structureId);
    }

    public StructureDto addRoomToStructure(Long structureId) {
        return restClient.post()
                .uri("/structures/" + structureId + "/add-room")
                .retrieve()
                .body(StructureDto.class);
    }

    public void expandRoom(Long roomId) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/structures/rooms/" + roomId + "/expand").build())
                .retrieve().toBodilessEntity();
    }

    public void assignRabbitToRoom(Long roomId, Long rabbitId) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/structures/rooms/" + roomId + "/assign")
                        .queryParam("rabbitId", rabbitId).build())
                .retrieve().toBodilessEntity();
    }

    public void removeRabbitFromRoom(Long roomId, Long rabbitId) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/structures/rooms/" + roomId + "/remove")
                        .queryParam("rabbitId", rabbitId).build())
                .retrieve().toBodilessEntity();
    }

    public void startBreeding(Long roomId) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/structures/rooms/" + roomId + "/breed").build())
                .retrieve().toBodilessEntity();
    }

    public void startTraining(Long roomId, Long rabbitId, String enhancedFoodType) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/structures/rooms/" + roomId + "/train")
                        .queryParam("rabbitId", rabbitId)
                        .queryParam("foodType", enhancedFoodType != null ? enhancedFoodType : "")
                        .build())
                .retrieve().toBodilessEntity();
    }

    public void updateRabbitStatus(Long rabbitId, String status) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/rabbits/" + rabbitId + "/status")
                        .queryParam("status", status).build())
                .retrieve().toBodilessEntity();
    }

    public void admitToVet(Long rabbitId) {
        restClient.post()
                .uri("/vet/" + rabbitId + "/admit")
                .retrieve().toBodilessEntity();
    }

    public void renameRabbit(Long rabbitId, String newName) {
        restClient.post()
                .uri(uriBuilder -> uriBuilder.path("/rabbits/" + rabbitId + "/rename")
                        .queryParam("newName", newName).build())
                .retrieve().toBodilessEntity();
    }

    public StructureDto getStructureById(Long id) {
        return restClient.get()
                .uri("/structures/" + id)
                .retrieve()
                .body(StructureDto.class);
    }


}