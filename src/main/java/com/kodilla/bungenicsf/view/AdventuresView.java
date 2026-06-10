package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.PlayerDto;
import com.kodilla.bungenicsf.dto.RabbitDto;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.utils.ImageUrls;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.util.List;

@Route(value = "adventures", layout = MainLayout.class)
public class AdventuresView extends VerticalLayout {

    private final BackendClientService backendClientService;
    private final ComboBox<RabbitDto> rabbitCombo = new ComboBox<>("Select Your Rabbit");

    public AdventuresView(BackendClientService backendClientService) {
        this.backendClientService = backendClientService;

        add(new H2("🧭 Send Rabbit on Adventure"));
        add(new Paragraph("Adventures take 1 minute to complete. You can view adventure portraits and loot reports in the Info tab!"));

        rabbitCombo.setItemLabelGenerator(r -> r.name() + " (" + r.breed() + " - Lvl: " + Math.round(r.nutritionLevel()) + "%)");
        refreshRabbitCombo();

        ComboBox<String> typeCombo = new ComboBox<>("Select Destination");
        typeCombo.setItems("FOREST", "MOUNTAIN", "MEADOW");

        Button sendButton = new Button("Send on Expedition!", e -> {
            try {
                Long currentPlayerId = (Long) VaadinSession.getCurrent().getAttribute("playerId");
                if (currentPlayerId == null) {
                    Notification.show("No active player session found!");
                    return;
                }

                RabbitDto rabbit = rabbitCombo.getValue();
                String type = typeCombo.getValue();

                if (rabbit != null && type != null) {
                    backendClientService.sendRabbitOnAdventure(currentPlayerId, rabbit.id(), type);
                    Notification.show("🐰 " + rabbit.name() + " departed safely on expedition to " + type + "!");
                    refreshRabbitCombo();
                } else {
                    Notification.show("Please select both a rabbit and a destination.");
                }
            } catch (Exception ex) {
                Notification.show("Cannot send rabbit: " + ex.getMessage());
            }
        });
        sendButton.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(rabbitCombo, typeCombo, sendButton);
    }

    private void refreshRabbitCombo() {
        try {
            Long currentPlayerId = (Long) VaadinSession.getCurrent().getAttribute("playerId");
            if (currentPlayerId == null) return;

            List<RabbitDto> availableRabbits = backendClientService.getAllRabbits().stream()
                    .filter(r -> currentPlayerId.equals(r.playerId()))
                    .filter(r -> "IDLE".equals(r.status()))
                    .toList();

            rabbitCombo.setItems(availableRabbits);
        } catch (Exception e) {
            Notification.show("Failed to load rabbits for adventure: " + e.getMessage());
        }
    }
}