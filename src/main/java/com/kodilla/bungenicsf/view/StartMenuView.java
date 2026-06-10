package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.PlayerDto;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.service.WeatherClientService;
import com.kodilla.bungenicsf.session.PlayerSession;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import java.util.ArrayList;
import java.util.List;

@Route("")
public class StartMenuView extends VerticalLayout {

    private final BackendClientService backendService;
    private final WeatherClientService weatherService;
    private final PlayerSession playerSession;

    public StartMenuView(BackendClientService backendService, WeatherClientService weatherService, PlayerSession playerSession) {
        this.backendService = backendService;
        this.weatherService = weatherService;
        this.playerSession = playerSession;

        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);
        getStyle().set("background", "radial-gradient(circle, #eef9f2 0%, #b2dfdb 100%)");

        H1 title = new H1("🐇 Bungenics - Idle Clicker");
        title.getStyle().set("font-size", "3rem").set("color", "#2e7d32").set("margin-bottom", "40px");

        Button newGameBtn = new Button("New Game", e -> openNewGameDialog());
        newGameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_LARGE);
        newGameBtn.setWidth("250px");

        Button loadGameBtn = new Button("Load Game", e -> openLoadGameDialog());
        loadGameBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_LARGE);
        loadGameBtn.setWidth("250px");

        Button optionsBtn = new Button("Options", e -> Notification.show("🔧 Options coming soon..."));
        optionsBtn.addThemeVariants(ButtonVariant.LUMO_CONTRAST, ButtonVariant.LUMO_LARGE);
        optionsBtn.setWidth("250px");

        if (playerSession.isActive()) {
            Button resumeBtn = new Button("Resume Game", e -> UI.getCurrent().navigate(InfoView.class));
            resumeBtn.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
            add(title, resumeBtn, newGameBtn, loadGameBtn, optionsBtn);
        } else {
            add(title, newGameBtn, loadGameBtn, optionsBtn);
        }
    }

    private void openNewGameDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Create New Player");

        TextField nameField = new TextField("Player Name");

        ComboBox<String> locationCombo = new ComboBox<>("Select Farm Location");
        List<String> locations = new ArrayList<>();
        for (com.kodilla.bungenicsf.dto.SuggestedLocation sl : com.kodilla.bungenicsf.dto.SuggestedLocation.values()) {
            locations.add(sl.getDisplayName());
        }
        locations.add("Custom Location...");
        locationCombo.setItems(locations);

        TextField customLocationField = new TextField("Enter Custom Location");
        customLocationField.setVisible(false);

        Paragraph descPara = new Paragraph();
        descPara.getStyle().set("font-size", "0.9em").set("color", "gray").set("max-width", "300px");

        locationCombo.addValueChangeListener(e -> {
            if ("Custom Location...".equals(e.getValue())) {
                customLocationField.setVisible(true);
                descPara.setText("Enter any real-world city to pull actual weather data.");
            } else {
                customLocationField.setVisible(false);
                com.kodilla.bungenicsf.dto.SuggestedLocation sl = com.kodilla.bungenicsf.dto.SuggestedLocation.fromDisplayName(e.getValue());
                if (sl != null) {
                    descPara.setText("Climate: " + sl.getDescription());
                } else {
                    descPara.setText("");
                }
            }
        });

        Button createBtn = new Button("Create & Start", e -> {
            String name = nameField.getValue().trim();
            String location = "Custom Location...".equals(locationCombo.getValue())
                    ? customLocationField.getValue().trim()
                    : locationCombo.getValue();

            if (name.isEmpty()) {
                Notification notif = Notification.show("Player name cannot be empty.", 3000, Notification.Position.MIDDLE);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            if (location == null || location.isEmpty()) {
                Notification notif = Notification.show("Please select or enter a farm location.", 3000, Notification.Position.MIDDLE);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
                return;
            }

            try {
                PlayerDto newPlayer = backendService.createPlayer(name, location);
                backendService.createFarm(newPlayer.id());

                playerSession.setPlayerId(newPlayer.id());
                UI.getCurrent().navigate(FarmView.class);
                dialog.close();
            } catch (Exception ex) {
                String errorMsg = ex.getMessage();
                if (errorMsg == null || errorMsg.isBlank()) {
                    errorMsg = "Location not found or backend server error!";
                }
                Notification notif = Notification.show("❌ Error: " + errorMsg, 5000, Notification.Position.MIDDLE);
                notif.addThemeVariants(NotificationVariant.LUMO_ERROR);
            }
        });
        createBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        VerticalLayout layout = new VerticalLayout(nameField, locationCombo, customLocationField, descPara, createBtn);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }

    private void openLoadGameDialog() {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Load or Delete Player");

        ComboBox<PlayerDto> playerCombo = new ComboBox<>("Select Player Profile");

        Runnable populateDropdown = () -> {
            try {
                List<PlayerDto> players = backendService.getAllPlayers();
                playerCombo.setItems(players);
                playerCombo.setItemLabelGenerator(p -> p.name() + " (Gold: " + p.money() + "g)");
            } catch (Exception e) {
                Notification.show("Failed to load players. Is backend server reachable?");
            }
        };

        populateDropdown.run();

        Button loadBtn = new Button("Load & Play", VaadinIcon.PLAY.create(), e -> {
            if (playerCombo.getValue() != null) {
                playerSession.setPlayerId(playerCombo.getValue().id());
                UI.getCurrent().navigate(FarmView.class);
                dialog.close();
            } else {
                Notification.show("Please select a player first.");
            }
        });
        loadBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_PRIMARY);

        Button deleteBtn = new Button("Delete Profile", VaadinIcon.TRASH.create(), e -> {
            PlayerDto selected = playerCombo.getValue();
            if (selected != null) {
                try {
                    backendService.deletePlayer(selected.id());
                    Notification.show("Player profile and all associated rabbits/farms permanently deleted.");
                    populateDropdown.run();
                    playerCombo.setValue(null);
                } catch (Exception ex) {
                    Notification.show("Deletion failed: " + ex.getMessage());
                }
            } else {
                Notification.show("Please select a player profile to delete.");
            }
        });
        deleteBtn.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_TERTIARY);

        HorizontalLayout actionRow = new HorizontalLayout(loadBtn, deleteBtn);
        actionRow.setSpacing(true);

        VerticalLayout layout = new VerticalLayout(playerCombo, actionRow);
        layout.setAlignItems(Alignment.CENTER);
        dialog.add(layout);
        dialog.open();
    }
}