package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.*;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.utils.ImageUrls;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
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
import java.util.List;
import java.util.Locale;

@Route(value = "vet", layout = MainLayout.class)
public class VetView extends VerticalLayout {

    private final BackendClientService backend;
    private final Grid<RabbitDto> generalRabbitsGrid = new Grid<>(RabbitDto.class, false);
    private final Grid<RabbitDto> vetClinicGrid = new Grid<>(RabbitDto.class, false);
    private final Span goldBalanceLabel = new Span("💰 Loading Gold...");

    public VetView(BackendClientService backendClientService) {
        this.backend = backendClientService;
        setSpacing(true);

        add(new H2("🏥 Vet Clinic"));

        HorizontalLayout infoBar = new HorizontalLayout();
        infoBar.setSpacing(true);
        infoBar.setAlignItems(Alignment.CENTER);
        infoBar.getStyle()
                .set("background", "#ffffff")
                .set("padding", "10px 20px")
                .set("border-radius", "12px")
                .set("box-shadow", "0 4px 10px rgba(0,0,0,0.08)");

        goldBalanceLabel.getStyle()
                .set("font-weight", "bold")
                .set("font-size", "1.1rem")
                .set("color", "#d97706");
        infoBar.add(goldBalanceLabel);
        add(infoBar);

        add(new Paragraph("Admit injured or stressed rabbits here. They regenerate Health and reduce Stress over time."));

        add(new H3("Bunnies at Your Farm"));
        configureGeneralGrid();
        add(generalRabbitsGrid);

        add(new H3("Admitted in Ward (Under Treatment)"));
        configureVetGrid();
        add(vetClinicGrid);

        refreshGrids();
    }

    private void configureGeneralGrid() {
        generalRabbitsGrid.addComponentColumn(rabbit -> {
            Image img = new Image(ImageUrls.getRabbitImageUrl(rabbit.breed()), "Rabbit");
            img.setWidth("40px");
            img.setHeight("40px");
            img.getStyle().set("border-radius", "50%").set("object-fit", "cover");
            Span idLabel = new Span("#" + rabbit.id());
            HorizontalLayout l = new HorizontalLayout(img, idLabel);
            l.setAlignItems(Alignment.CENTER);
            return l;
        }).setHeader("ID").setAutoWidth(true);

        generalRabbitsGrid.addColumn(RabbitDto::name).setHeader("Name").setAutoWidth(true);
        generalRabbitsGrid.addColumn(r -> "FEMALE".equalsIgnoreCase(r.sex()) ? "♀️ Female" : "♂️ Male").setHeader("Sex").setAutoWidth(true);

        generalRabbitsGrid.addColumn(r -> {
            float maxHp = (r.secondaryStats() != null && r.secondaryStats().life() != null) ? r.secondaryStats().life() : 100.0f;
            float current = r.life() != null ? r.life() : maxHp;
            return String.format(Locale.US, "%.0f/%.0f ❤️", current, maxHp);
        }).setHeader("Health").setAutoWidth(true);

        generalRabbitsGrid.addColumn(r -> (r.stress() != null ? r.stress() : 0.0f) + "/100 😰").setHeader("Stress").setAutoWidth(true);
        generalRabbitsGrid.addColumn(RabbitDto::status).setHeader("Status").setAutoWidth(true);

        // Display Vet Admission Fee Column (Base 50.0 + 0.5 per missing HP)
        generalRabbitsGrid.addColumn(rabbit -> {
            float maxHp = (rabbit.secondaryStats() != null && rabbit.secondaryStats().life() != null) ? rabbit.secondaryStats().life() : 100.0f;
            float currentHp = rabbit.life() != null ? rabbit.life() : maxHp;
            double cost = 50.0 + (maxHp - currentHp) / 2.0;
            return String.format(Locale.US, "💰 %.2f Gold", cost);
        }).setHeader("Vet Fee").setAutoWidth(true);

        generalRabbitsGrid.addComponentColumn(rabbit -> {
            float maxHp = (rabbit.secondaryStats() != null && rabbit.secondaryStats().life() != null) ? rabbit.secondaryStats().life() : 100.0f;
            float currentHp = rabbit.life() != null ? rabbit.life() : maxHp;
            float currentStress = rabbit.stress() != null ? rabbit.stress() : 0.0f;
            double cost = 50.0 + (maxHp - currentHp) / 2.0;

            Button admitBtn = new Button(String.format(Locale.US, "Admit (-%.1fg)", cost));
            admitBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

            // Only IDLE rabbits that are injured or stressed can be admitted
            admitBtn.setEnabled("IDLE".equalsIgnoreCase(rabbit.status()) && (currentHp < maxHp || currentStress > 0.0f));

            admitBtn.addClickListener(e -> {
                try {
                    backend.admitToVet(rabbit.id());
                    Notification.show("Rabbit #" + rabbit.id() + " (" + rabbit.name() + ") admitted to vet ward!");
                    refreshGrids();
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            });
            return admitBtn;
        }).setHeader("Action");
    }

    private void configureVetGrid() {
        vetClinicGrid.addComponentColumn(rabbit -> {
            Image img = new Image(ImageUrls.getRabbitImageUrl(rabbit.breed()), "Rabbit");
            img.setWidth("40px");
            img.setHeight("40px");
            img.getStyle().set("border-radius", "50%").set("object-fit", "cover");
            Span idLabel = new Span("#" + rabbit.id());
            HorizontalLayout l = new HorizontalLayout(img, idLabel);
            l.setAlignItems(Alignment.CENTER);
            return l;
        }).setHeader("ID").setAutoWidth(true);

        vetClinicGrid.addColumn(RabbitDto::name).setHeader("Name").setAutoWidth(true);
        vetClinicGrid.addColumn(r -> "FEMALE".equalsIgnoreCase(r.sex()) ? "♀️ Female" : "♂️ Male").setHeader("Sex").setAutoWidth(true);

        vetClinicGrid.addColumn(r -> {
            float maxHp = (r.secondaryStats() != null && r.secondaryStats().life() != null) ? r.secondaryStats().life() : 100.0f;
            float current = r.life() != null ? r.life() : maxHp;
            return String.format(Locale.US, "%.0f/%.0f ❤️", current, maxHp);
        }).setHeader("Health").setAutoWidth(true);

        vetClinicGrid.addColumn(r -> (r.stress() != null ? r.stress() : 0.0f) + "/100 😰").setHeader("Stress").setAutoWidth(true);

        vetClinicGrid.addComponentColumn(rabbit -> {
            LocalDateTime now = LocalDateTime.now();
            String statusText = "Healing in progress...";
            if (rabbit.vetEndTime() != null) {
                long secs = Math.max(0, Duration.between(now, rabbit.vetEndTime()).getSeconds());
                statusText = String.format(Locale.US, "⏰ Healing (%02dm %02ds remaining)", secs / 60, secs % 60);
            }
            Span statusSpan = new Span(statusText);
            statusSpan.getStyle().set("color", "#0284c7").set("font-weight", "600");
            return statusSpan;
        }).setHeader("Status").setAutoWidth(true);
    }


    private void refreshGrids() {
        try {
            Long currentSessionPlayerId = (Long) VaadinSession.getCurrent().getAttribute("playerId");
            if (currentSessionPlayerId == null) return;

            // Update player balance
            PlayerDto player = backend.getPlayer(currentSessionPlayerId);
            BigDecimal money = player.money() != null
                    ? player.money().setScale(2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;
            goldBalanceLabel.setText(String.format(Locale.US, "💰 Gold: %.2f", money));

            List<RabbitDto> allServerRabbits = backend.getAllRabbits();

            List<RabbitDto> myRabbits = allServerRabbits.stream()
                    .filter(r -> currentSessionPlayerId.equals(r.playerId()))
                    .toList();

            List<RabbitDto> general = myRabbits.stream()
                    .filter(r -> !"ON_VET".equals(r.status()) && !"MARKET".equals(r.status()))
                    .toList();

            List<RabbitDto> admitted = myRabbits.stream()
                    .filter(r -> "ON_VET".equals(r.status()))
                    .toList();

            generalRabbitsGrid.setItems(general);
            vetClinicGrid.setItems(admitted);
        } catch (Exception e) {
            Notification.show("Unable to load rabbits for Vet Clinic: " + e.getMessage());
        }
    }
}