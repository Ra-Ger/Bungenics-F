package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.PlayerDto;
import com.kodilla.bungenicsf.dto.RabbitDto;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.utils.ImageUrls;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinSession;

import java.math.BigDecimal;
import java.util.List;

@Route(value = "shop", layout = MainLayout.class)
public class ShopView extends VerticalLayout {

    private final BackendClientService backendClientService;
    private Long currentPlayerId = null;
    private final Span moneyLabel = new Span("Money: Connecting...");

    private final Span foodPricePreview = new Span("Estimated Cost: 0.00 gold");
    private final Span rabbitSellPreview = new Span("Estimated Sell Value: 0.00 gold");

    private final Grid<RabbitDto> marketGrid = new Grid<>(RabbitDto.class, false);
    private final ComboBox<RabbitDto> rabbitSellCombo = new ComboBox<>("Chosen rabbit to sell");

    public ShopView(BackendClientService backendClientService) {
        this.backendClientService = backendClientService;

        add(new H2("🛒 Shop & Market"));
        add(moneyLabel);

        currentPlayerId = (Long) VaadinSession.getCurrent().getAttribute("playerId");
        refreshPlayerData();

        add(new H3("Buy Food"));
        ComboBox<String> foodTypeCombo = new ComboBox<>("Choose food type");
        foodTypeCombo.setItems("CARROT", "LETTUCE", "SPINACH", "HAY");

        NumberField amountField = new NumberField("Amount (kg)");
        amountField.setMin(1);
        amountField.setValue(1.0);

        foodTypeCombo.addValueChangeListener(e -> updateFoodPricePreview(foodTypeCombo.getValue(), amountField.getValue()));
        amountField.addValueChangeListener(e -> updateFoodPricePreview(foodTypeCombo.getValue(), amountField.getValue()));

        Button buyFoodBtn = new Button("Buy Food", e -> {
            if (currentPlayerId != null && foodTypeCombo.getValue() != null) {
                try {
                    backendClientService.buyFood(currentPlayerId, foodTypeCombo.getValue(), amountField.getValue().floatValue());
                    Notification.show("Food purchased successfully!");
                    refreshPlayerData();
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            }
        });
        buyFoodBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        add(new HorizontalLayout(foodTypeCombo, amountField, buyFoodBtn), foodPricePreview);

        add(new H3("Sell Rabbit"));
        rabbitSellCombo.setItemLabelGenerator(r -> r.name() + " (#" + r.id() + " - " + r.breed() + " - " + ("FEMALE".equalsIgnoreCase(r.sex()) ? "♀️" : "♂️") + ")");
        refreshSellRabbitCombo();

        rabbitSellCombo.addValueChangeListener(e -> {
            if (e.getValue() != null) {
                try {
                    BigDecimal val = backendClientService.getRabbitSellValue(e.getValue().id());
                    rabbitSellPreview.setText("Estimated Sell Value: " + val + " gold");
                } catch (Exception ignored) {}
            } else {
                rabbitSellPreview.setText("Estimated Sell Value: 0.00 gold");
            }
        });

        Button sellBtn = new Button("Sell Selected Rabbit", e -> {
            if (currentPlayerId != null && rabbitSellCombo.getValue() != null) {
                try {
                    backendClientService.sellRabbit(currentPlayerId, rabbitSellCombo.getValue().id());
                    Notification.show("Rabbit sold successfully!");
                    refreshSellRabbitCombo();
                    refreshPlayerData();
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            }
        });
        sellBtn.addThemeVariants(ButtonVariant.LUMO_ERROR);

        add(new HorizontalLayout(rabbitSellCombo, sellBtn), rabbitSellPreview);

        add(new H3("Rabbits Available on Market"));
        configureMarketGrid();
        add(marketGrid);
        refreshMarketGrid();
    }

    private void updateFoodPricePreview(String type, Double amount) {
        if (type != null && amount != null && amount > 0) {
            try {
                BigDecimal cost = backendClientService.getFoodPrice(type, amount.floatValue());
                foodPricePreview.setText("Estimated Cost: " + cost + " gold");
            } catch (Exception ignored) {}
        } else {
            foodPricePreview.setText("Estimated Cost: 0.00 gold");
        }
    }

    private void configureMarketGrid() {
        marketGrid.addComponentColumn(rabbit -> {
            Image img = new Image(ImageUrls.getRabbitImageUrl(rabbit.breed()), "Rabbit");
            img.setWidth("45px");
            img.setHeight("45px");
            img.getStyle().set("border-radius", "50%").set("object-fit", "cover");
            Span idLabel = new Span("#" + rabbit.id());
            HorizontalLayout l = new HorizontalLayout(img, idLabel);
            l.setAlignItems(Alignment.CENTER);
            return l;
        }).setHeader("ID").setAutoWidth(true);

        marketGrid.addColumn(RabbitDto::name).setHeader("Name");
        marketGrid.addColumn(RabbitDto::breed).setHeader("Breed");

        marketGrid.addColumn(r -> "FEMALE".equalsIgnoreCase(r.sex()) ? "♀️ Female" : "♂️ Male")
                .setHeader("Sex");

        marketGrid.addColumn(r -> {
            if (r.secondaryStats() != null) {
                float avg = (r.secondaryStats().strength() + r.secondaryStats().agility() + r.secondaryStats().intelligence()) / 3f;
                return Math.max(1, Math.round(avg));
            }
            return 1;
        }).setHeader("LVL").setSortable(true);

        marketGrid.addColumn(r -> {
            try {
                return backendClientService.getRabbitSellValue(r.id()).multiply(BigDecimal.valueOf(2.0)) + " gold";
            } catch (Exception e) {
                return "N/A";
            }
        }).setHeader("Price");

        marketGrid.addComponentColumn(rabbit -> {
            Button buyBtn = new Button("Buy");
            buyBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);
            buyBtn.addClickListener(e -> {
                try {
                    backendClientService.buyRabbit(currentPlayerId, rabbit.id());
                    Notification.show("Successfully bought " + rabbit.name() + "!");
                    refreshMarketGrid();
                    refreshSellRabbitCombo();
                    refreshPlayerData();
                } catch (Exception ex) {
                    Notification.show("Error: " + ex.getMessage());
                }
            });

            return new HorizontalLayout(buyBtn);
        }).setHeader("Actions");
    }

    private void refreshPlayerData() {
        try {
            if (currentPlayerId != null) {
                PlayerDto p = backendClientService.getPlayer(currentPlayerId);
                moneyLabel.setText("💰 Balance for " + p.name() + ": " + p.money() + " Gold");
            }
        } catch (Exception ignored) {}
    }

    private void refreshSellRabbitCombo() {
        try {
            if (currentPlayerId == null) return;
            List<RabbitDto> playerRabbits = backendClientService.getAllRabbits().stream()
                    .filter(r -> currentPlayerId.equals(r.playerId()))
                    .filter(r -> !"MARKET".equalsIgnoreCase(r.status()))
                    .toList();
            rabbitSellCombo.setItems(playerRabbits);
        } catch (Exception ignored) {}
    }

    private void refreshMarketGrid() {
        try {
            marketGrid.setItems(backendClientService.getMarketRabbits());
        } catch (Exception ignored) {}
    }
}