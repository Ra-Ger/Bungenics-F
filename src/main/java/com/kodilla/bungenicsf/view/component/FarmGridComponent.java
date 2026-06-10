package com.kodilla.bungenicsf.view.component;

import com.kodilla.bungenicsf.dto.RoomDto;
import com.kodilla.bungenicsf.dto.StructureDto;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.dom.Element;
import lombok.Setter;

import java.util.Map;
import java.util.function.Consumer;

public class FarmGridComponent extends Div {

    private final Div gridContainer = new Div();

    @Setter
    private Consumer<Integer> onBuildClickListener;

    @Setter
    private Consumer<Long> onManageClickListener;

    public FarmGridComponent() {
        addClassName("farm-board-wrapper");

        // Inject component-specific CSS to ensure the full farm background and 3x3 grid render accurately
        injectComponentStyles();

        gridContainer.addClassName("farm-grid");
        add(gridContainer);
    }

    private void injectComponentStyles() {
        Element style = new Element("style");
        style.setText("""
            .farm-board-wrapper {
                background-image: url('images/farm/FarmField.jpg') !important;
                background-size: 100% 100% !important;
                background-repeat: no-repeat !important;
                background-position: center !important;
                border-radius: 24px !important;
                /* Generous padding ensures the outer fence, trees, and paths of FarmField.jpg frame the grid */
                padding: 75px 65px 65px 65px !important;
                box-shadow: 0 12px 36px rgba(0,0,0,0.3) !important;
                display: inline-block !important;
                box-sizing: border-box !important;
            }
            .farm-grid {
                display: grid !important;
                grid-template-columns: repeat(3, 170px) !important;
                grid-template-rows: repeat(3, 170px) !important;
                gap: 16px !important;
                justify-content: center !important;
                align-items: center !important;
            }
            .farm-cell {
                position: relative !important;
                width: 170px !important;
                height: 170px !important;
                border-radius: 16px !important;
                overflow: hidden !important;
                background: transparent !important;
                cursor: pointer !important;
                box-sizing: border-box !important;
                transition: transform 0.25s ease, box-shadow 0.25s ease !important;
            }
            .farm-cell:hover {
                transform: translateY(-4px) scale(1.03) !important;
                z-index: 10 !important;
            }
            .empty-plot-grass {
                border: 2px dashed rgba(255, 255, 255, 0.6) !important;
                background: rgba(0, 0, 0, 0.08) !important;
                border-radius: 16px !important;
            }
            .empty-plot-grass:hover {
                border-color: #ffd700 !important;
                background: rgba(0, 0, 0, 0.22) !important;
            }
            .cell-bg-illustration {
                width: 100% !important;
                height: 100% !important;
                object-fit: contain !important;
                position: absolute !important;
                top: 0 !important;
                left: 0 !important;
                z-index: 1 !important;
                transition: filter 0.25s ease !important;
                pointer-events: none !important;
            }
            .farm-cell:hover .cell-bg-illustration {
                filter: brightness(85%) blur(2px) !important;
            }
            .cell-ui-overlay {
                position: absolute !important;
                top: 0 !important;
                left: 0 !important;
                right: 0 !important;
                bottom: 0 !important;
                z-index: 2 !important;
                display: flex !important;
                flex-direction: column !important;
                justify-content: space-between !important;
                align-items: center !important;
                padding: 12px !important;
                box-sizing: border-box !important;
                background: linear-gradient(to bottom, rgba(0,0,0,0.1) 0%, rgba(0,0,0,0.30) 100%) !important;
                color: white !important;
                opacity: 0 !important;
                transition: opacity 0.25s ease !important;
                pointer-events: none !important;
                border-radius: 14px !important;
            }
            .farm-cell:hover .cell-ui-overlay {
                opacity: 1 !important;
                pointer-events: auto !important;
            }
            .cell-title {
                font-weight: bold !important;
                font-size: 0.95rem !important;
                text-shadow: 1px 1px 3px rgba(0,0,0,0.9) !important;
                text-align: center !important;
            }
            .cell-stats {
                font-size: 0.82rem !important;
                text-shadow: 1px 1px 2px rgba(0,0,0,0.9) !important;
                text-align: center !important;
            }
            .hover-action-trigger {
                width: 100% !important;
                display: flex !important;
                justify-content: center !important;
            }
        """);
        getElement().appendChild(style);
    }

    public void render(Map<Integer, StructureDto> structureMap) {
        gridContainer.removeAll();

        for (int i = 0; i < 9; i++) {
            StructureDto structure = structureMap != null ? structureMap.get(i) : null;
            if (structure != null) {
                renderStructure(i, structure);
            } else {
                renderEmptyPlot(i);
            }
        }
    }

    private void renderStructure(int gridIndex, StructureDto structure) {
        Div cell = new Div();
        cell.addClassName("farm-cell");

        String imagePath = getBuildingImagePath(structure.structureType());
        Image img = new Image(imagePath, structure.structureType());
        img.addClassName("cell-bg-illustration");

        Div overlay = new Div();
        overlay.addClassName("cell-ui-overlay");

        String icon = switch (structure.structureType().toUpperCase()) {
            case "WARREN" -> "🛖 ";
            case "PLAYHOUSE" -> "🎡 ";
            case "TRYSTHOUSE" -> "🏩 ";
            case "TRAINING_GROUND", "TRAINING" -> "⚔️ ";
            default -> "🏠 ";
        };

        Span title = new Span(icon + structure.structureType().toUpperCase());
        title.addClassName("cell-title");

        int totalSlots = 0;
        int occupiedSlots = 0;
        if (structure.rooms() != null) {
            for (RoomDto r : structure.rooms()) {
                totalSlots += r.slots() != null ? r.slots() : 0;
                occupiedSlots += r.rabbits() != null ? r.rabbits().size() : 0;
            }
        }

        Span stats = new Span(String.format("Rooms: %d\nBunnies: %d/%d",
                structure.rooms() != null ? structure.rooms().size() : 0,
                occupiedSlots,
                totalSlots));
        stats.addClassName("cell-stats");

        Button manageBtn = new Button("Manage", e -> {
            if (onManageClickListener != null) {
                onManageClickListener.accept(structure.id());
            }
        });
        manageBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        Div actionContainer = new Div(manageBtn);
        actionContainer.addClassName("hover-action-trigger");

        overlay.add(title, stats, actionContainer);
        cell.add(img, overlay);
        gridContainer.add(cell);
    }

    private void renderEmptyPlot(int gridIndex) {
        Div cell = new Div();
        cell.addClassName("farm-cell");
        cell.addClassName("empty-plot-grass");

        Div overlay = new Div();
        overlay.addClassName("cell-ui-overlay");

        Span title = new Span("Empty Plot");
        title.addClassName("cell-title");

        Button buildBtn = new Button("Build (-100g)", e -> {
            if (onBuildClickListener != null) {
                onBuildClickListener.accept(gridIndex);
            }
        });
        buildBtn.addThemeVariants(ButtonVariant.LUMO_SUCCESS, ButtonVariant.LUMO_SMALL);

        Div actionContainer = new Div(buildBtn);
        actionContainer.addClassName("hover-action-trigger");

        overlay.add(title, actionContainer);
        cell.add(overlay);
        gridContainer.add(cell);
    }

    private String getBuildingImagePath(String type) {
        if (type == null) return "images/farm/Warren.png";
        return switch (type.toUpperCase()) {
            case "WARREN" -> "images/farm/Warren.png";
            case "PLAYHOUSE" -> "images/farm/Playhouse.png";
            case "TRYSTHOUSE" -> "images/farm/Trysthouse.png";
            case "TRAINING_GROUND", "TRAINING" -> "images/farm/TrainingGround.png";
            default -> "images/farm/Warren.png";
        };
    }
}