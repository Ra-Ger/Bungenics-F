package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.*;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.service.WeatherClientService;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Route(value = "farm", layout = MainLayout.class)
public class FarmView extends VerticalLayout {

    private final BackendClientService backend;
    private final WeatherClientService weatherService;
    private final Div gridContainer = new Div();
    private final Span farmBalanceLabel = new Span("Loading gold...");
    private final Span foodInventoryLabel = new Span("Loading warehouse...");
    private final Span weatherLabel = new Span("Loading weather...");
    private Long playerFarmId = null;
    private Dialog activeManageDialog = null;

    public FarmView(BackendClientService backendClientService, WeatherClientService weatherService) {
        this.backend = backendClientService;
        this.weatherService = weatherService;
        setAlignItems(Alignment.CENTER);

        Html customStyles = new Html("<style>" +
                "  .farm-board-wrapper {" +
                "    background: url('images/farm/FarmField.jpg') no-repeat center;" +
                "    background-size: cover;" +
                "    border-radius: 20px;" +
                "    padding: 30px;" +
                "    box-shadow: inset 0 0 100px rgba(0,0,0,0.4), 0 10px 30px rgba(0,0,0,0.25);" +
                "    display: inline-block;" +
                "  }" +
                "  .farm-grid {" +
                "    display: grid;" +
                "    grid-template-columns: repeat(3, 190px);" +
                "    grid-template-rows: repeat(3, 190px);" +
                "    gap: 15px;" +
                "  }" +
                "  .farm-cell {" +
                "    position: relative;" +
                "    border-radius: 16px;" +
                "    overflow: hidden;" +
                "    border: 3px solid rgba(255,255,255,0.7);" +
                "    box-shadow: 0 4px 8px rgba(0,0,0,0.15);" +
                "    transition: all 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);" +
                "    background: transparent;" +
                "    cursor: pointer;" +
                "  }" +
                "  .farm-cell:hover {" +
                "    transform: translateY(-6px) scale(1.02);" +
                "    box-shadow: 0 12px 24px rgba(0,0,0,0.3);" +
                "    border-color: #ffd700;" +
                "  }" +
                "  .cell-bg-illustration {" +
                "    width: 100%;" +
                "    height: 100%;" +
                "    object-fit: cover;" +
                "    position: absolute;" +
                "    top: 0; left: 0;" +
                "    z-index: 1;" +
                "    transition: filter 0.3s ease;" +
                "  }" +
                "  .farm-cell:hover .cell-bg-illustration {" +
                "    filter: brightness(50%) blur(2px);" +
                "  }" +
                "  .cell-ui-overlay {" +
                "    position: absolute;" +
                "    top: 0; left: 0; right: 0; bottom: 0;" +
                "    z-index: 2;" +
                "    display: flex;" +
                "    flex-direction: column;" +
                "    justify-content: space-between;" +
                "    padding: 12px;" +
                "    box-sizing: border-box;" +
                "    background: linear-gradient(to bottom, rgba(0,0,0,0.2) 30%, rgba(0,0,0,0.7) 100%);" +
                "    color: white;" +
                "    opacity: 0;" +
                "    transform: translateY(8px);" +
                "    transition: opacity 0.3s ease, transform 0.3s cubic-bezier(0.25, 0.8, 0.25, 1);" +
                "    pointer-events: none;" +
                "  }" +
                "  .farm-cell:hover .cell-ui-overlay {" +
                "    opacity: 1;" +
                "    transform: translateY(0);" +
                "    pointer-events: auto;" +
                "  }" +
                "  .cell-title {" +
                "    font-weight: bold;" +
                "    text-shadow: 1px 1px 3px rgba(0,0,0,0.8);" +
                "    font-size: 1.1rem;" +
                "  }" +
                "  .cell-stats {" +
                "    font-size: 0.85rem;" +
                "    text-shadow: 1px 1px 2px rgba(0,0,0,0.9);" +
                "  }" +
                "  .hover-action-trigger {" +
                "    width: 100%;" +
                "    display: flex;" +
                "    justify-content: center;" +
                "  }" +
                "  .empty-plot-grass {" +
                "    background: rgba(0, 0, 0, 0.2);" +
                "    border: 3px dashed rgba(255,255,255,0.6);" +
                "  }" +
                "</style>");
        add(customStyles);

        add(new H2("Your Farm"));

        HorizontalLayout infoBar = new HorizontalLayout(farmBalanceLabel, foodInventoryLabel, weatherLabel);
        infoBar.setSpacing(true);
        infoBar.setAlignItems(Alignment.CENTER);
        infoBar.setWidthFull();
        infoBar.getStyle().set("background", "#ffffff").set("padding", "12px 20px").set("border-radius", "12px").set("box-shadow", "0 4px 10px rgba(0,0,0,0.1)");
        add(infoBar);

        Div boardWrapper = new Div();
        boardWrapper.addClassName("farm-board-wrapper");

        gridContainer.addClassName("farm-grid");
        boardWrapper.add(gridContainer);
        add(boardWrapper);

        refreshFarm();
    }

    private void refreshFarm() {
        gridContainer.removeAll();
        try {
            Long currentSessionPlayerId = (Long) VaadinSession.getCurrent().getAttribute("playerId");
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

            for (int i = 0; i < 9; i++) {
                final int index = i;
                Div cell = new Div();
                cell.addClassName("farm-cell");

                if (structureMap.containsKey(index)) {
                    StructureDto structure = structureMap.get(index);
                    renderOccupiedPlot(cell, structure);
                } else {
                    renderEmptyPlot(cell, index);
                }
                gridContainer.add(cell);
            }
        } catch (Exception e) {
            Notification.show("Error refreshing farm board: " + e.getMessage());
        }
    }

    private void renderEmptyPlot(Div cell, int gridIndex) {
        cell.addClassName("empty-plot-grass");

        Div overlay = new Div();
        overlay.addClassName("cell-ui-overlay");

        Span title = new Span("Empty Plot");
        title.addClassName("cell-title");
        overlay.add(title);

        Div hoverAction = new Div();
        hoverAction.addClassName("hover-action-trigger");

        Button buildBtn = new Button("Build (-100g)");
        buildBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
        buildBtn.addClickListener(e -> openBuildDialog(gridIndex));
        hoverAction.add(buildBtn);

        overlay.add(hoverAction);
        cell.add(overlay);
    }

    private void renderOccupiedPlot(Div cell, StructureDto s) {
        String buildingUrl = switch (s.structureType()) {
            case "WARREN" -> "images/farm/Warren.png";
            case "PLAYHOUSE" -> "images/farm/Playhouse.png";
            case "TRYSTHOUSE" -> "images/farm/Trysthouse.png";
            case "TRAINING_GROUND" -> "images/farm/TrainingGround.png";
            default -> "images/farm/Playhouse.png";
        };

        String emoji = switch (s.structureType()) {
            case "WARREN" -> "🏠 WARREN";
            case "PLAYHOUSE" -> "⚽ PLAYHOUSE";
            case "TRYSTHOUSE" -> "💕 TRYSTHOUSE";
            case "TRAINING_GROUND" -> "⚔️ TRAINING";
            default -> "🏰 " + s.structureType();
        };

        Image buildingImg = new Image(buildingUrl, s.structureType());
        buildingImg.addClassName("cell-bg-illustration");
        cell.add(buildingImg);

        Div overlay = new Div();
        overlay.addClassName("cell-ui-overlay");

        Span title = new Span(emoji);
        title.addClassName("cell-title");
        overlay.add(title);

        VerticalLayout statsLayout = new VerticalLayout();
        statsLayout.setSpacing(false);
        statsLayout.setPadding(false);
        statsLayout.addClassName("cell-stats");

        int totalBunnies = s.rooms().stream().mapToInt(r -> r.rabbits().size()).sum();
        int totalCapacity = s.rooms().stream().mapToInt(RoomDto::slots).sum();

        statsLayout.add(new Span("Rooms: " + s.rooms().size()));
        statsLayout.add(new Span("Bunnies: " + totalBunnies + "/" + totalCapacity));
        overlay.add(statsLayout);

        Div hoverAction = new Div();
        hoverAction.addClassName("hover-action-trigger");

        Button manageBtn = new Button("Manage");
        manageBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
        manageBtn.addClickListener(e -> openManageDialog(s.id()));
        hoverAction.add(manageBtn);

        overlay.add(hoverAction);
        cell.add(overlay);
    }

    private void openBuildDialog(int gridIndex) {
        Dialog d = new Dialog();
        d.setHeaderTitle("Build New Structure");

        VerticalLayout layout = new VerticalLayout();
        ComboBox<String> typeCombo = new ComboBox<>("Select Type");
        typeCombo.setItems("WARREN", "PLAYHOUSE", "TRYSTHOUSE", "TRAINING_GROUND");

        Button confirmBtn = new Button("Build (-100g)", e -> {
            if (typeCombo.getValue() != null && playerFarmId != null) {
                try {
                    backend.buildStructure(playerFarmId, typeCombo.getValue(), gridIndex);
                    Notification.show("Structure built successfully!");
                    d.close();
                    refreshFarm();
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(typeCombo, confirmBtn);
        d.add(layout);
        d.open();
    }

    private void openManageDialog(Long structureId) {
        if (activeManageDialog != null && activeManageDialog.isOpened()) {
            activeManageDialog.close();
        }

        Dialog d = new Dialog();
        activeManageDialog = d;

        StructureDto s = backend.getStructureById(structureId);
        if (s == null) {
            refreshFarm();
            return;
        }

        boolean isTrysthouse = "TRYSTHOUSE".equalsIgnoreCase(s.structureType());
        boolean isPlayhouse = "PLAYHOUSE".equalsIgnoreCase(s.structureType());
        boolean isTrainingGround = "TRAINING_GROUND".equalsIgnoreCase(s.structureType());

        d.setHeaderTitle("Manage Structure: " + s.structureType() + " #" + s.id());

        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("580px"); // 480

        Button addRoomBtn = new Button("➕ Add Room (-50g)", e -> {
            e.getSource().setEnabled(false);
            try {
                backend.addRoomToStructure(structureId);
                Notification.show("New room added!");
                refreshFarm();
                d.close();
                openManageDialog(structureId);
            } catch (Exception err) {
                Notification.show("Error adding room: " + err.getMessage());
                e.getSource().setEnabled(true);
            }
        });
        addRoomBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        layout.add(addRoomBtn);

        layout.add(new H3("Rooms & Assigned Bunnies:"));

        for (int i = 0; i < s.rooms().size(); i++) {
            RoomDto room = s.rooms().get(i);
            VerticalLayout roomLayout = new VerticalLayout();
            roomLayout.getStyle().set("border", "1px solid #ccc").set("padding", "10px").set("border-radius", "8px");

            HorizontalLayout header = new HorizontalLayout(
                    new Span("Room " + (i + 1) + " (Cap: " + room.rabbits().size() + "/" + room.slots() + ")")
            );

            Button expandBtn = new Button("+2 Slots (-40g)", ex -> {
                try {
                    backend.expandRoom(room.id());
                    Notification.show("Expanded room slots!");
                    refreshFarm();
                    d.close();
                    openManageDialog(structureId);
                } catch (Exception err) {
                    Notification.show("Error expanding slots: " + err.getMessage());
                }
            });
            expandBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_PRIMARY);
            expandBtn.setEnabled(room.slots() < 8 && !isTrysthouse);

            header.add(expandBtn);
            roomLayout.add(header);

            boolean hasFemale = room.rabbits().stream().anyMatch(r -> "FEMALE".equalsIgnoreCase(r.sex()) && ("IDLE".equalsIgnoreCase(r.status()) || "RESTING".equalsIgnoreCase(r.status())));
            boolean hasMale = room.rabbits().stream().anyMatch(r -> "MALE".equalsIgnoreCase(r.sex()) && ("IDLE".equalsIgnoreCase(r.status()) || "RESTING".equalsIgnoreCase(r.status())));
            boolean isBreedingActive = room.rabbits().stream().anyMatch(r -> "BREEDING".equalsIgnoreCase(r.status()));

            for (RabbitDto r : room.rabbits()) {
                String label = "🐰 " + r.name() + " #" + r.id() + " (" + ("FEMALE".equalsIgnoreCase(r.sex()) ? "♀️" : "♂️") + " - " + r.status() + ")";
                HorizontalLayout rRow = new HorizontalLayout(new Span(label));
                rRow.setAlignItems(Alignment.CENTER);

                Button removeBtn = new Button("Kick", rx -> {
                    try {
                        backend.removeRabbitFromRoom(room.id(), r.id());
                        Notification.show(r.name() + " #" + r.id() + " removed. Relocated to Warren if space was available.");
                        refreshFarm();
                        d.close();
                        openManageDialog(structureId);
                    } catch (Exception err) {
                        Notification.show("Error: " + err.getMessage());
                    }
                });
                removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                removeBtn.setEnabled(!"BREEDING".equalsIgnoreCase(r.status()) && !"TRAINING".equalsIgnoreCase(r.status()));

                rRow.add(removeBtn);
                roomLayout.add(rRow);

                if (isTrainingGround) {
                    if ("TRAINING".equalsIgnoreCase(r.status())) {
                        long remainingSeconds = 0;
                        if (r.trainingEndTime() != null) {
                            remainingSeconds = Math.max(0, Duration.between(LocalDateTime.now(), r.trainingEndTime()).getSeconds());
                        }
                        long mins = remainingSeconds / 60;
                        long secs = remainingSeconds % 60;

                        if (remainingSeconds <= 0) {
                            Span finLabel = new Span("🏋️ Training completed! Click to collect stat gains...");
                            finLabel.getStyle().set("color", "#2e7d32").set("font-weight", "bold");
                            Button finBtn = new Button("Finish Training", e -> {
                                refreshFarm();
                                d.close();
                                openManageDialog(structureId);
                            });
                            finBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                            roomLayout.add(new HorizontalLayout(finLabel, finBtn));
                        } else {
                            Span timerLabel = new Span(String.format("🏋️ Training in progress! Finishes in: %02dm %02ds", mins, secs));
                            timerLabel.getStyle().set("color", "#1976d2").set("font-weight", "bold").set("font-size", "0.9rem");
                            roomLayout.add(timerLabel);
                        }
                    } else if ("RESTING".equalsIgnoreCase(r.status())) {
                        Span restingInfo = new Span("😴 Bunny is resting... Cannot start training until rested.");
                        restingInfo.getStyle().set("color", "#757575").set("font-style", "italic").set("font-size", "0.85rem");
                        roomLayout.add(restingInfo);
                    } else if ("IDLE".equalsIgnoreCase(r.status())) {
                        SecondaryStatsDto stats = r.secondaryStats();
                        float str = (stats != null && stats.strength() != null) ? stats.strength() : 10.0f;
                        float agi = (stats != null && stats.agility() != null) ? stats.agility() : 10.0f;
                        float intel = (stats != null && stats.intelligence() != null) ? stats.intelligence() : 10.0f;
                        float totalStats = str + agi + intel;
                        double trainingCost = totalStats * 1.5;

                        VerticalLayout trainBox = new VerticalLayout();
                        trainBox.getStyle().set("background", "#f5f5f5").set("padding", "8px").set("border-radius", "6px").set("margin-top", "5px");

                        Span costText = new Span(String.format(Locale.US, "Start Training (Cost: %.2f Gold | Enhanced requires food):", trainingCost));
                        costText.getStyle().set("font-weight", "bold").set("font-size", "0.85rem");
                        trainBox.add(costText);

                        HorizontalLayout trainButtons = new HorizontalLayout();
                        Button stdTrainBtn = new Button(String.format(Locale.US, "Standard (%.2fg)", trainingCost), e -> {
                            try {
                                backend.startTraining(room.id(), r.id(), "NONE");
                                Notification.show(String.format(Locale.US, "Started standard training for %s (-%.2f Gold)!", r.name(), trainingCost));
                                refreshFarm();
                                d.close();
                                openManageDialog(structureId);
                            } catch (Exception ex) {
                                Notification.show("Training error: " + ex.getMessage());
                            }
                        });
                        stdTrainBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

                        ComboBox<String> foodCombo = new ComboBox<>();
                        foodCombo.setPlaceholder("Enhanced Food (+2 Stat)");
                        foodCombo.setItems("Spinach (+2 STR)", "Carrot (+2 AGI)", "Lettuce (+2 INT)");
                        foodCombo.getStyle().set("width", "180px");

                        Button enhTrainBtn = new Button(String.format(Locale.US, "Enhanced (%.2fg + Food)", trainingCost), e -> {
                            String selected = foodCombo.getValue();
                            if (selected == null) {
                                Notification.show("Select an enhanced food type first!");
                                return;
                            }
                            String foodType = selected.contains("Spinach") ? "SPINACH" : (selected.contains("Carrot") ? "CARROT" : "LETTUCE");
                            try {
                                backend.startTraining(room.id(), r.id(), foodType);
                                Notification.show(String.format(Locale.US, "Started enhanced training (%s) for %s (-%.2f Gold + Food)!", foodType, r.name(), trainingCost));
                                refreshFarm();
                                d.close();
                                openManageDialog(structureId);
                            } catch (Exception ex) {
                                Notification.show("Training error: " + ex.getMessage());
                            }
                        });
                        enhTrainBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

                        trainButtons.add(stdTrainBtn, foodCombo, enhTrainBtn);
                        trainButtons.setAlignItems(Alignment.CENTER);
                        trainBox.add(trainButtons);
                        roomLayout.add(trainBox);
                    }
                }
            }

            if (isTrysthouse) {
                if (isBreedingActive) {
                    RabbitDto breedingRabbit = room.rabbits().stream()
                            .filter(r -> "BREEDING".equalsIgnoreCase(r.status()) && r.breedingEndTime() != null)
                            .findFirst().orElse(null);

                    long remainingSeconds = 0;
                    if (breedingRabbit != null && breedingRabbit.breedingEndTime() != null) {
                        remainingSeconds = Math.max(0, Duration.between(LocalDateTime.now(), breedingRabbit.breedingEndTime()).getSeconds());
                    }

                    long mins = remainingSeconds / 60;
                    long secs = remainingSeconds % 60;

                    if (remainingSeconds <= 0) {
                        Span finishedLabel = new Span("💕 Breeding finished! Click to collect & return parents to Warren...");
                        finishedLabel.getStyle().set("color", "#2e7d32").set("font-weight", "bold");
                        Button refreshBtn = new Button("Finish & Relocate Parents", e -> {
                            refreshFarm();
                            d.close();
                            openManageDialog(structureId);
                        });
                        refreshBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                        roomLayout.add(new HorizontalLayout(finishedLabel, refreshBtn));
                    } else {
                        Span timerLabel = new Span(String.format("💕 Breeding in progress! Kit arrives in: %02dm %02ds", mins, secs));
                        timerLabel.getStyle().set("color", "#d81b60").set("font-weight", "bold").set("font-size", "0.9rem");
                        roomLayout.add(timerLabel);
                    }
                } else if (hasFemale && hasMale) {
                    Button breedBtn = new Button("💕 Breed Bunnies (5 min)", e -> {
                        try {
                            backend.startBreeding(room.id());
                            Notification.show("Breeding started!");
                            refreshFarm();
                            d.close();
                            openManageDialog(structureId);
                        } catch (Exception ex) {
                            Notification.show("Breeding failed: " + ex.getMessage());
                        }
                    });
                    breedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
                    breedBtn.getStyle().set("background-color", "#e91e63").set("color", "white");
                    roomLayout.add(breedBtn);
                }
            }

            if (room.rabbits().size() < room.slots() && !isBreedingActive) {
                ComboBox<RabbitDto> assignCombo = new ComboBox<>(isPlayhouse ? "Assign stressed rabbit" : "Assign rabbit (IDLE / KIT / RESTING)");
                try {
                    Long currentSessionPlayerId = (Long) VaadinSession.getCurrent().getAttribute("playerId");

                    Set<Long> currentRoomRabbitIds = room.rabbits().stream()
                            .map(RabbitDto::id)
                            .collect(Collectors.toSet());

                    List<RabbitDto> available = backend.getAllRabbits().stream()
                            .filter(r -> currentSessionPlayerId != null && currentSessionPlayerId.equals(r.playerId()))
                            .filter(r -> "IDLE".equalsIgnoreCase(r.status()) || "KIT".equalsIgnoreCase(r.status()) || "RESTING".equalsIgnoreCase(r.status()))
                            .filter(r -> !currentRoomRabbitIds.contains(r.id()))
                            .filter(r -> !isPlayhouse || (r.stress() != null && r.stress() > 0.0f))
                            .toList();

                    assignCombo.setItems(available);
                    assignCombo.setItemLabelGenerator(r -> r.name() + " #" + r.id() +
                            " (Stress: " + String.format(Locale.US, "%.1f", r.stress() != null ? r.stress() : 0.0f) + "%" +
                            ", Status: " + r.status() + ")");

                    assignCombo.addValueChangeListener(ev -> {
                        if (ev.getValue() != null) {
                            try {
                                backend.assignRabbitToRoom(room.id(), ev.getValue().id());
                                Notification.show("Assigned " + ev.getValue().name() + " #" + ev.getValue().id() + "!");
                                refreshFarm();
                                d.close();
                                openManageDialog(structureId);
                            } catch (Exception err) {
                                Notification.show("Error assigning: " + err.getMessage());
                            }
                        }
                    });
                } catch (Exception ignored) {}
                roomLayout.add(assignCombo);
            }

            layout.add(roomLayout);
        }

        d.add(layout);
        d.open();
    }
}