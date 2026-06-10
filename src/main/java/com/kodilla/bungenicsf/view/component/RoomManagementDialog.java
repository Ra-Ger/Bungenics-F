package com.kodilla.bungenicsf.view.component;

import com.kodilla.bungenicsf.dto.RabbitDto;
import com.kodilla.bungenicsf.dto.RoomDto;
import com.kodilla.bungenicsf.dto.StructureDto;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.session.PlayerSession;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
public class RoomManagementDialog extends Dialog {

    private final BackendClientService backend;
    private final PlayerSession playerSession;
    private final Runnable onRefreshNeeded;

    public RoomManagementDialog(Long structureId, BackendClientService backend, PlayerSession playerSession, Runnable onRefreshNeeded) {
        this.backend = backend;
        this.playerSession = playerSession;
        this.onRefreshNeeded = onRefreshNeeded;

        StructureDto s = backend.getStructureById(structureId);
        if (s == null) {
            if (onRefreshNeeded != null) onRefreshNeeded.run();
            return;
        }

        boolean isTrysthouse = "TRYSTHOUSE".equalsIgnoreCase(s.structureType());
        boolean isPlayhouse = "PLAYHOUSE".equalsIgnoreCase(s.structureType());
        boolean isTrainingGround = "TRAINING_GROUND".equalsIgnoreCase(s.structureType());

        setHeaderTitle("Manage Structure: " + s.structureType() + " #" + s.id());

        VerticalLayout layout = new VerticalLayout();
        layout.setWidth("580px");

        Button addRoomBtn = new Button("➕ Add Room (-50g)", e -> {
            e.getSource().setEnabled(false);
            try {
                backend.addRoomToStructure(structureId);
                Notification.show("New room added!");
                refreshAndReopen(structureId);
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
                    refreshAndReopen(structureId);
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
                rRow.setAlignItems(FlexComponent.Alignment.CENTER);

                Button removeBtn = new Button("Kick", rx -> {
                    try {
                        backend.removeRabbitFromRoom(room.id(), r.id());
                        Notification.show(r.name() + " #" + r.id() + " removed. Relocated to Warren if space was available.");
                        refreshAndReopen(structureId);
                    } catch (Exception err) {
                        Notification.show("Error: " + err.getMessage());
                    }
                });
                removeBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_SMALL);
                removeBtn.setEnabled(!"BREEDING".equalsIgnoreCase(r.status()) && !"TRAINING".equalsIgnoreCase(r.status()));

                rRow.add(removeBtn);
                roomLayout.add(rRow);

                if (isTrainingGround) {
                    renderTrainingSection(roomLayout, room, r, structureId);
                }
            }

            if (isTrysthouse) {
                renderBreedingSection(roomLayout, room, isBreedingActive, hasFemale, hasMale, structureId);
            }

            if (room.rabbits().size() < room.slots() && !isBreedingActive) {
                renderAssignmentSection(roomLayout, room, isPlayhouse, structureId);
            }

            layout.add(roomLayout);
        }

        add(layout);
    }

    private void refreshAndReopen(Long structureId) {
        close();
        if (onRefreshNeeded != null) {
            onRefreshNeeded.run();
        }
        new RoomManagementDialog(structureId, backend, playerSession, onRefreshNeeded).open();
    }

    private void renderTrainingSection(VerticalLayout roomLayout, RoomDto room, RabbitDto r, Long structureId) {
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
                Button finBtn = new Button("Finish Training", e -> refreshAndReopen(structureId));
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
            roomLayout.add(new TrainingControl(room.id(), r, backend, () -> refreshAndReopen(structureId)));
        }
    }

    private void renderBreedingSection(VerticalLayout roomLayout, RoomDto room, boolean isBreedingActive, boolean hasFemale, boolean hasMale, Long structureId) {
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
                Button refreshBtn = new Button("Finish & Relocate Parents", e -> refreshAndReopen(structureId));
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
                    refreshAndReopen(structureId);
                } catch (Exception ex) {
                    Notification.show("Breeding failed: " + ex.getMessage());
                }
            });
            breedBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);
            breedBtn.getStyle().set("background-color", "#e91e63").set("color", "white");
            roomLayout.add(breedBtn);
        }
    }

    private void renderAssignmentSection(VerticalLayout roomLayout, RoomDto room, boolean isPlayhouse, Long structureId) {
        ComboBox<RabbitDto> assignCombo = new ComboBox<>(isPlayhouse ? "Assign stressed rabbit" : "Assign rabbit (IDLE / KIT / RESTING)");
        try {
            Long currentSessionPlayerId = playerSession.getPlayerId();

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
                        refreshAndReopen(structureId);
                    } catch (Exception err) {
                        Notification.show("Error assigning: " + err.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            log.error("Failed to populate assignment combo for room {}", room.id(), e);
        }
        roomLayout.add(assignCombo);
    }
}