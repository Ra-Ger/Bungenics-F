package com.kodilla.bungenicsf.view.component;

import com.kodilla.bungenicsf.dto.RabbitDto;
import com.kodilla.bungenicsf.dto.SecondaryStatsDto;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

import java.util.Locale;

public class TrainingControl extends VerticalLayout {

    public TrainingControl(Long roomId, RabbitDto rabbit, BackendClientService backend, Runnable onActionComplete) {
        getStyle().set("background", "#f5f5f5")
                .set("padding", "8px")
                .set("border-radius", "6px")
                .set("margin-top", "5px");

        SecondaryStatsDto stats = rabbit.secondaryStats();
        float str = (stats != null && stats.strength() != null) ? stats.strength() : 10.0f;
        float agi = (stats != null && stats.agility() != null) ? stats.agility() : 10.0f;
        float intel = (stats != null && stats.intelligence() != null) ? stats.intelligence() : 10.0f;
        float totalStats = str + agi + intel;
        double trainingCost = totalStats * 1.5;

        Span costText = new Span(String.format(Locale.US, "Start Training (Cost: %.2f Gold | Enhanced requires food):", trainingCost));
        costText.getStyle().set("font-weight", "bold").set("font-size", "0.85rem");
        add(costText);

        HorizontalLayout trainButtons = new HorizontalLayout();
        trainButtons.setAlignItems(FlexComponent.Alignment.CENTER);

        Button stdTrainBtn = new Button(String.format(Locale.US, "Standard (%.2fg)", trainingCost), e -> {
            try {
                backend.startTraining(roomId, rabbit.id(), "NONE");
                Notification.show(String.format(Locale.US, "Started standard training for %s (-%.2f Gold)!", rabbit.name(), trainingCost));
                if (onActionComplete != null) {
                    onActionComplete.run();
                }
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
                backend.startTraining(roomId, rabbit.id(), foodType);
                Notification.show(String.format(Locale.US, "Started enhanced training (%s) for %s (-%.2f Gold + Food)!", foodType, rabbit.name(), trainingCost));
                if (onActionComplete != null) {
                    onActionComplete.run();
                }
            } catch (Exception ex) {
                Notification.show("Training error: " + ex.getMessage());
            }
        });
        enhTrainBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        trainButtons.add(stdTrainBtn, foodCombo, enhTrainBtn);
        add(trainButtons);
    }
}