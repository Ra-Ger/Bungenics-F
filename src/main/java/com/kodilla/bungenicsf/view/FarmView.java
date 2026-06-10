package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.PlayerDto;
import com.kodilla.bungenicsf.dto.RabbitFarmDto;
import com.kodilla.bungenicsf.dto.StructureDto;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.service.WeatherClientService;
import com.kodilla.bungenicsf.session.PlayerSession;
import com.kodilla.bungenicsf.view.component.BuildStructureDialog;
import com.kodilla.bungenicsf.view.component.FarmGridComponent;
import com.kodilla.bungenicsf.view.component.RoomManagementDialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Route(value = "farm", layout = MainLayout.class)
public class FarmView extends VerticalLayout {

    private final BackendClientService backend;
    private final WeatherClientService weatherService;
    private final PlayerSession playerSession;

    private final FarmGridComponent farmGrid = new FarmGridComponent();
    private final Span farmBalanceLabel = new Span("Loading gold...");
    private final Span foodInventoryLabel = new Span("Loading warehouse...");
    private final Span weatherLabel = new Span("Loading weather...");
    private Long playerFarmId = null;

    public FarmView(BackendClientService backendClientService, WeatherClientService weatherService, PlayerSession playerSession) {
        this.backend = backendClientService;
        this.weatherService = weatherService;
        this.playerSession = playerSession;

        setAlignItems(Alignment.CENTER);

        add(new H2("Your Farm"));

        HorizontalLayout infoBar = new HorizontalLayout(farmBalanceLabel, foodInventoryLabel, weatherLabel);
        infoBar.setSpacing(true);
        infoBar.setAlignItems(Alignment.CENTER);
        infoBar.setWidthFull();
        infoBar.getStyle()
                .set("background", "#ffffff")
                .set("padding", "12px 20px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.1)");
        add(infoBar);

        farmGrid.setOnBuildClickListener(this::openBuildDialog);
        farmGrid.setOnManageClickListener(this::openManageDialog);
        add(farmGrid);

        refreshFarm();
    }

    private void refreshFarm() {
        try {
            Long currentSessionPlayerId = playerSession.getPlayerId();
            if (currentSessionPlayerId == null) return;

            PlayerDto player = backend.getPlayer(currentSessionPlayerId);
            BigDecimal money = player.money() != null
                    ? player.money().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            farmBalanceLabel.setText(String.format(Locale.US, "💰 %.2f Gold", money));

            String location = player.location() != null ? player.location() : "Pęcice, Poland";
            weatherLabel.setText(weatherService.getCurrentWeather(location));
            weatherLabel.getStyle().set("color", "#1565c0").set("font-weight", "500").set("margin-left", "auto");

            List<RabbitFarmDto> farms = backend.getAllFarms();
            RabbitFarmDto farm = farms.stream()
                    .filter(f -> currentSessionPlayerId.equals(f.playerId()))
                    .findFirst()
                    .orElse(null);

            if (farm == null) {
                farm = backend.createFarm(currentSessionPlayerId);
            }

            playerFarmId = farm.id();

            foodInventoryLabel.setText(String.format("🌾 Hay: %.1f kg | 🥕 Carrots: %.1f kg | 🥬 Lettuce: %.1f kg | 🍃 Spinach: %.1f kg",
                    farm.hayAmount(), farm.carrotAmount(), farm.lettuceAmount(), farm.spinachAmount()));

            List<StructureDto> structures = backend.getAllStructures().stream()
                    .filter(s -> playerFarmId.equals(s.rabbitFarmId()))
                    .toList();

            Map<Integer, StructureDto> structureMap = new HashMap<>();
            for (StructureDto s : structures) {
                if (s.gridIndex() != null) {
                    structureMap.put(s.gridIndex(), s);
                }
            }

            farmGrid.render(structureMap);
        } catch (Exception e) {
            Notification.show("Error refreshing farm board: " + e.getMessage());
        }
    }

    private void openBuildDialog(int gridIndex) {
        new BuildStructureDialog(playerFarmId, gridIndex, backend, this::refreshFarm).open();
    }

    private void openManageDialog(Long structureId) {
        new RoomManagementDialog(structureId, backend, playerSession, this::refreshFarm).open();
    }
}