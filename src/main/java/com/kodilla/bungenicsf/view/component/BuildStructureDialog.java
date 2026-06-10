package com.kodilla.bungenicsf.view.component;

import com.kodilla.bungenicsf.service.BackendClientService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;

public class BuildStructureDialog extends Dialog {

    public BuildStructureDialog(Long playerFarmId, int gridIndex, BackendClientService backend, Runnable onStructureBuilt) {
        setHeaderTitle("Build New Structure");

        VerticalLayout layout = new VerticalLayout();
        ComboBox<String> typeCombo = new ComboBox<>("Select Type");
        typeCombo.setItems("WARREN", "PLAYHOUSE", "TRYSTHOUSE", "TRAINING_GROUND");

        Button confirmBtn = new Button("Build (-100g)", e -> {
            if (typeCombo.getValue() != null && playerFarmId != null) {
                try {
                    backend.buildStructure(playerFarmId, typeCombo.getValue(), gridIndex);
                    Notification.show("Structure built successfully!");
                    close();
                    if (onStructureBuilt != null) {
                        onStructureBuilt.run();
                    }
                } catch (Exception ex) {
                    Notification.show(ex.getMessage());
                }
            }
        });
        confirmBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        layout.add(typeCombo, confirmBtn);
        add(layout);
    }
}