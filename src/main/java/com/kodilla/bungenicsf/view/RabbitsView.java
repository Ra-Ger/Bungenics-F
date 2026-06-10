package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.*;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.session.PlayerSession;
import com.kodilla.bungenicsf.utils.BasicConstants;
import com.kodilla.bungenicsf.utils.ImageUrls;
import com.vaadin.flow.component.Html;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Route(value = "rabbits", layout = MainLayout.class)
public class RabbitsView extends VerticalLayout {

    private final BackendClientService backendClientService;
    private final PlayerSession playerSession;
    private final Grid<RabbitDto> grid = new Grid<>(RabbitDto.class, false);

    private static class TraitInfo {
        final String name;
        final String displayName;
        final String type;
        final String description;

        TraitInfo(String name, String displayName, String type, String description) {
            this.name = name;
            this.displayName = displayName;
            this.type = type;
            this.description = description;
        }
    }

    public RabbitsView(BackendClientService backendClientService, PlayerSession playerSession) {
        this.backendClientService = backendClientService;
        this.playerSession = playerSession;

        Html customStyles = new Html("<style>" +
                "  .trait-badge-container {" +
                "    position: relative;" +
                "    display: inline-block;" +
                "  }" +
                "  .trait-popup-card {" +
                "    position: absolute;" +
                "    bottom: 125%;" +
                "    left: 50%;" +
                "    transform: translateX(-50%);" +
                "    background: #ffffff;" +
                "    border: 1px solid #ddd;" +
                "    border-radius: 8px;" +
                "    padding: 8px 12px;" +
                "    box-shadow: 0 4px 16px rgba(0,0,0,0.25);" +
                "    width: 220px;" +
                "    z-index: 10000;" +
                "    pointer-events: none;" +
                "    opacity: 0;" +
                "    visibility: hidden;" +
                "    transition: opacity 0.2s ease-in-out, visibility 0.2s ease-in-out;" +
                "  }" +
                "  .trait-popup-card::after {" +
                "    content: '';" +
                "    position: absolute;" +
                "    top: 100%;" +
                "    left: 50%;" +
                "    margin-left: -6px;" +
                "    border-width: 6px;" +
                "    border-style: solid;" +
                "    border-color: #ffffff transparent transparent transparent;" +
                "  }" +
                "  .trait-badge-container:hover .trait-popup-card {" +
                "    opacity: 1;" +
                "    visibility: visible;" +
                "  }" +
                "</style>");
        add(customStyles);

        add(new H2("🐇 Your Rabbits List"));

        configureGrid();
        add(grid);

        updateList();
    }

    private float calculateMaxStress(RabbitDto rabbit) {
        if (rabbit != null && rabbit.secondaryStats() != null && rabbit.secondaryStats().stress() != null && rabbit.secondaryStats().stress() > 0) {
            return rabbit.secondaryStats().stress();
        }
        return 100.0f;
    }

    private float calculateMaxLife(RabbitDto rabbit) {
        if (rabbit != null && rabbit.secondaryStats() != null && rabbit.secondaryStats().life() != null && rabbit.secondaryStats().life() > 0) {
            return rabbit.secondaryStats().life();
        }
        return 100.0f;
    }

    private void configureGrid() {
        grid.addComponentColumn(rabbit -> {
                    String imageUrl = ImageUrls.getRabbitImageUrl(rabbit.breed());
                    Image rabbitImage = new Image(imageUrl, "Rabbit image");

                    rabbitImage.setWidth(BasicConstants.RABBIT_PORTRAIT_SIZE + "px");
                    rabbitImage.setHeight(BasicConstants.RABBIT_PORTRAIT_SIZE + "px");
                    rabbitImage.getStyle().set("border-radius", "50%");
                    rabbitImage.getStyle().set("margin-right", "10px");
                    rabbitImage.getStyle().set("object-fit", "cover");

                    Span idLabel = new Span("#" + rabbit.id());

                    HorizontalLayout layout = new HorizontalLayout(rabbitImage, idLabel);
                    layout.setAlignItems(Alignment.CENTER);
                    return layout;
                }).setHeader("ID")
                .setSortable(true)
                .setComparator(RabbitDto::id);

        grid.addColumn(RabbitDto::name).setHeader("Name").setSortable(true);
        grid.addColumn(RabbitDto::breed).setHeader("Breed").setSortable(true);

        grid.addColumn(r -> "FEMALE".equalsIgnoreCase(r.sex()) ? "♀️ Female" : "♂️ Male")
                .setHeader("Sex")
                .setSortable(true);

        grid.addColumn(RabbitDto::weight).setHeader("Weight (kg)").setSortable(true);

        grid.addComponentColumn(r -> {
                    float currentLife = r.life() != null ? r.life() : 100.0f;
                    float maxLife = calculateMaxLife(r);
                    float healthPct = maxLife > 0 ? (currentLife / maxLife) * 100.0f : 100.0f;

                    Span healthSpan = new Span(String.format(java.util.Locale.US, "%.0f / %.0f HP", currentLife, maxLife));
                    if (healthPct <= 30.0f) {
                        healthSpan.getStyle().set("color", "#d32f2f").set("font-weight", "bold");
                    } else if (healthPct <= 70.0f) {
                        healthSpan.getStyle().set("color", "#f57c00");
                    } else {
                        healthSpan.getStyle().set("color", "#388e3c");
                    }
                    return healthSpan;
                }).setHeader("Health").setSortable(true)
                .setComparator((r1, r2) -> Float.compare(r1.life() != null ? r1.life() : 0.0f, r2.life() != null ? r2.life() : 0.0f));

        grid.addColumn(r -> String.format(java.util.Locale.US, "%.1f %%", r.nutritionLevel() != null ? r.nutritionLevel() : 100.0f))
                .setHeader("Hunger lvl (%)").setSortable(true);

        grid.addComponentColumn(r -> {
                    float stress = r.stress() != null ? r.stress() : 0.0f;
                    float maxStress = calculateMaxStress(r);
                    float stressPct = maxStress > 0 ? (stress / maxStress) * 100.0f : 0.0f;

                    Span stressSpan = new Span(String.format(java.util.Locale.US, "%.1f %%", stressPct));
                    if (stressPct > 60.0f) {
                        stressSpan.getStyle().set("color", "#d32f2f").set("font-weight", "bold");
                    } else if (stressPct > 30.0f) {
                        stressSpan.getStyle().set("color", "#f57c00");
                    } else {
                        stressSpan.getStyle().set("color", "#388e3c");
                    }
                    return stressSpan;
                }).setHeader("Stress (%)").setSortable(true)
                .setComparator((r1, r2) -> {
                    float s1 = r1.stress() != null ? r1.stress() : 0.0f;
                    float m1 = calculateMaxStress(r1);
                    float s2 = r2.stress() != null ? r2.stress() : 0.0f;
                    float m2 = calculateMaxStress(r2);
                    return Float.compare(m1 > 0 ? s1 / m1 : 0.0f, m2 > 0 ? s2 / m2 : 0.0f);
                });

        grid.addComponentColumn(rabbit -> {
            String statusStr = rabbit.status() != null ? rabbit.status().toUpperCase() : "UNKNOWN";
            LocalDateTime now = LocalDateTime.now();
            String statusDetail = statusStr;

            if ("KIT".equals(statusStr)) {
                float adultW = rabbit.adultWeight() != null ? rabbit.adultWeight() : 3.0f;
                int reqDays = adultW < 2.5f ? 3 : (adultW < 5.0f ? 5 : 7);
                if (rabbit.traits() != null && rabbit.traits().contains("QUICK_GROWER")) {
                    reqDays = Math.max(1, Math.round(reqDays * 0.5f));
                }
                float currentAge = rabbit.age() != null ? rabbit.age() : 0.0f;
                int daysLeft = Math.max(0, Math.round(reqDays - currentAge));
                statusDetail = String.format("KIT (%dd to adult)", daysLeft);
            } else if ("RESTING".equals(statusStr) && rabbit.restEndTime() != null) {
                long secs = Math.max(0, Duration.between(now, rabbit.restEndTime()).getSeconds());
                statusDetail = String.format("RESTING (%02dm %02ds left)", secs / 60, secs % 60);
            } else if ("BREEDING".equals(statusStr) && rabbit.breedingEndTime() != null) {
                long secs = Math.max(0, Duration.between(now, rabbit.breedingEndTime()).getSeconds());
                statusDetail = String.format("BREEDING (%02dm %02ds left)", secs / 60, secs % 60);
            } else if (("ADVENTURE".equals(statusStr) || "ON_ADVENTURE".equals(statusStr)) && rabbit.adventureEndTime() != null) {
                long secs = Math.max(0, Duration.between(now, rabbit.adventureEndTime()).getSeconds());
                statusDetail = String.format("ADVENTURE (%02dm %02ds left)", secs / 60, secs % 60);
            } else if ("ON_VET".equals(statusStr) && rabbit.vetEndTime() != null) {
                long secs = Math.max(0, Duration.between(now, rabbit.vetEndTime()).getSeconds());
                statusDetail = String.format("ON VET (%02dm %02ds left)", secs / 60, secs % 60);
            } else if ("TRAINING".equals(statusStr) && rabbit.trainingEndTime() != null) {
                long secs = Math.max(0, Duration.between(now, rabbit.trainingEndTime()).getSeconds());
                statusDetail = String.format("TRAINING (%02dm %02ds left)", secs / 60, secs % 60);
            }

            Span statusBadge = new Span(statusDetail);
            statusBadge.getStyle().set("font-weight", "600");

            if ("BREEDING".equals(statusStr)) {
                statusBadge.getStyle().set("color", "#d81b60");
            } else if ("RESTING".equals(statusStr)) {
                statusBadge.getStyle().set("color", "#7b1fa2");
            } else if ("ADVENTURE".equals(statusStr) || "ON_ADVENTURE".equals(statusStr)) {
                statusBadge.getStyle().set("color", "#1976d2");
            } else if ("TRAINING".equals(statusStr)) {
                statusBadge.getStyle().set("color", "#e65100");
            } else if ("KIT".equals(statusStr)) {
                statusBadge.getStyle().set("color", "#f57c00");
            } else if ("IDLE".equals(statusStr)) {
                statusBadge.getStyle().set("color", "#388e3c");
            }

            return statusBadge;
        }).setHeader("Status").setSortable(true);

        grid.addComponentColumn(rabbit -> {
            Button infoBtn = new Button("Info");
            infoBtn.addThemeVariants(ButtonVariant.LUMO_SMALL);
            infoBtn.addClickListener(e -> openDetailedInfoDialog(rabbit));
            return infoBtn;
        }).setHeader("Profile Details");

        grid.getColumns().forEach(col -> col.setAutoWidth(true));
    }

    private void openDetailedInfoDialog(RabbitDto rabbit) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle(rabbit.name() + " #" + rabbit.id() + " Profile");

        HorizontalLayout mainLayout = new HorizontalLayout();
        mainLayout.setAlignItems(Alignment.CENTER);
        mainLayout.setSpacing(true);
        mainLayout.getStyle().set("padding", "10px");

        String imageUrl = ImageUrls.getRabbitImageUrl(rabbit.breed());
        Image portrait = new Image(imageUrl, "Rabbit Portrait");
        portrait.setWidth(BasicConstants.RABBIT_DETAILED_INFO_PORTRAIT_SIZE + "px");
        portrait.setHeight(BasicConstants.RABBIT_DETAILED_INFO_PORTRAIT_SIZE + "px");
        portrait.getStyle().set("border-radius", "12px").set("object-fit", "cover").set("border", "2px solid #ccc");

        VerticalLayout infoLayout = new VerticalLayout();
        infoLayout.setPadding(false);
        infoLayout.setSpacing(false);

        com.vaadin.flow.component.textfield.TextField renameField = new com.vaadin.flow.component.textfield.TextField();
        renameField.setValue(rabbit.name() != null ? rabbit.name() : "");
        renameField.setPlaceholder("New rabbit name");
        renameField.setWidth("180px");

        Button renameBtn = new Button("Rename", e -> {
            String newName = renameField.getValue();
            if (newName != null && !newName.isBlank()) {
                try {
                    backendClientService.renameRabbit(rabbit.id(), newName.trim());
                    com.vaadin.flow.component.notification.Notification.show("Rabbit renamed to " + newName.trim());
                    dialog.close();
                    updateList();
                } catch (Exception ex) {
                    com.vaadin.flow.component.notification.Notification.show("Error renaming: " + ex.getMessage());
                }
            }
        });
        renameBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY, ButtonVariant.LUMO_SMALL);

        HorizontalLayout renameLayout = new HorizontalLayout(renameField, renameBtn);
        renameLayout.setAlignItems(Alignment.BASELINE);
        infoLayout.add(renameLayout);

        infoLayout.add(new Span("Breed: " + rabbit.breed()));
        infoLayout.add(new Span("Sex: " + ("FEMALE".equalsIgnoreCase(rabbit.sex()) ? "♀️ Female" : "♂️ Male")));

        float currentAge = rabbit.age() != null ? rabbit.age() : 0.0f;
        float maxLifetime = rabbit.maxLifetime() != null ? rabbit.maxLifetime()
                : (rabbit.secondaryStats() != null && rabbit.secondaryStats().age() != null ? rabbit.secondaryStats().age() : 16.0f);
        infoLayout.add(new Span(String.format(java.util.Locale.US, "Age: %.1f years (Max Lifespan: %.1f years)", currentAge, maxLifetime)));

        float adultWeight = rabbit.adultWeight() != null ? rabbit.adultWeight() : (rabbit.weight() != null ? rabbit.weight() : 1.0f);
        infoLayout.add(new Span(String.format(java.util.Locale.US, "Weight: %.2f kg (Target Adult: %.2f kg)", rabbit.weight() != null ? rabbit.weight() : 0.0f, adultWeight)));

        float currentHealth = rabbit.life() != null ? rabbit.life() : 100.0f;
        float maxHealth = (rabbit.secondaryStats() != null && rabbit.secondaryStats().life() != null) ? rabbit.secondaryStats().life() : 100.0f;
        infoLayout.add(new Span(String.format(java.util.Locale.US, "Current Health: %.0f / %.0f HP", currentHealth, maxHealth)));

        float currentStress = rabbit.stress() != null ? rabbit.stress() : 0.0f;
        float maxStress = (rabbit.secondaryStats() != null && rabbit.secondaryStats().stress() != null) ? rabbit.secondaryStats().stress() : 100.0f;
        infoLayout.add(new Span(String.format(java.util.Locale.US, "Stress Level: %.1f / %.0f", currentStress, maxStress)));

        infoLayout.add(new Span("Hunger Lvl: " + rabbit.nutritionLevel() + " %"));
        infoLayout.add(new Span("Status: " + rabbit.status()));

        if (rabbit.secondaryStats() != null) {
            SecondaryStatsDto stats = rabbit.secondaryStats();
            infoLayout.add(new H3("RPG Stats (Current / Base Genetics)"));

            float str = stats.strength() != null ? stats.strength() : 0f;
            float baseStr = stats.basicStrength() != null ? stats.basicStrength() : str;
            infoLayout.add(new Span(String.format(java.util.Locale.US, "Strength: %.0f (Base: %.0f)", str, baseStr)));

            float agi = stats.agility() != null ? stats.agility() : 0f;
            float baseAgi = stats.basicAgility() != null ? stats.basicAgility() : agi;
            infoLayout.add(new Span(String.format(java.util.Locale.US, "Agility: %.0f (Base: %.0f)", agi, baseAgi)));

            float intel = stats.intelligence() != null ? stats.intelligence() : 0f;
            float baseIntel = stats.basicIntelligence() != null ? stats.basicIntelligence() : intel;
            infoLayout.add(new Span(String.format(java.util.Locale.US, "Intelligence: %.0f (Base: %.0f)", intel, baseIntel)));
        }

        infoLayout.add(new H3("Inherited & Active Traits"));
        if (rabbit.traits() != null && !rabbit.traits().isEmpty()) {
            HorizontalLayout traitsLayout = new HorizontalLayout();
            traitsLayout.getStyle().set("flex-wrap", "wrap").set("gap", "8px").set("margin-top", "6px");

            for (String traitStr : rabbit.traits()) {
                TraitInfo traitInfo = findTraitByName(traitStr);
                boolean isNegative = traitInfo != null && "NEGATIVE".equalsIgnoreCase(traitInfo.type);

                Div badgeContainer = new Div();
                badgeContainer.addClassName("trait-badge-container");

                Span traitBadge = new Span((isNegative ? "⚠️ " : "✨ ") + (traitInfo != null ? traitInfo.displayName : traitStr));
                traitBadge.getStyle()
                        .set("background-color", isNegative ? "#ffebee" : "#e8f5e9")
                        .set("color", isNegative ? "#c62828" : "#2e7d32")
                        .set("border", "1px solid " + (isNegative ? "#ef9a9a" : "#a5d6a7"))
                        .set("padding", "4px 10px")
                        .set("border-radius", "12px")
                        .set("font-size", "0.85rem")
                        .set("font-weight", "600")
                        .set("display", "inline-block")
                        .set("cursor", "pointer");

                if (traitInfo != null) {
                    Div popupCard = new Div();
                    popupCard.addClassName("trait-popup-card");

                    Span title = new Span(traitInfo.displayName);
                    title.getStyle().set("font-weight", "bold").set("font-size", "0.9rem").set("color", isNegative ? "#c62828" : "#2e7d32");

                    Span badgeType = new Span(isNegative ? " [NEGATIVE]" : " [POSITIVE]");
                    badgeType.getStyle().set("font-size", "0.75rem").set("font-weight", "bold").set("color", isNegative ? "#d32f2f" : "#388e3c").set("margin-left", "4px");

                    Div titleRow = new Div(title, badgeType);

                    Div desc = new Div();
                    desc.setText(traitInfo.description);
                    desc.getStyle().set("font-size", "0.8rem").set("color", "#424242").set("margin-top", "4px").set("line-height", "1.3");

                    popupCard.add(titleRow, desc);
                    badgeContainer.add(traitBadge, popupCard);
                } else {
                    badgeContainer.add(traitBadge);
                }

                traitsLayout.add(badgeContainer);
            }
            infoLayout.add(traitsLayout);
        } else {
            infoLayout.add(new Span("No special traits inherited."));
        }

        infoLayout.add(new H3("Lineage"));
        infoLayout.add(new Span("Mother ID: " + (rabbit.motherId() != null ? "#" + rabbit.motherId() : "Wild / Purchased")));
        infoLayout.add(new Span("Father ID: " + (rabbit.fatherId() != null ? "#" + rabbit.fatherId() : "Wild / Purchased")));

        mainLayout.add(portrait, infoLayout);
        dialog.add(mainLayout);

        Button close = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(close);
        dialog.open();
    }

    private TraitInfo findTraitByName(String traitStr) {
        if (traitStr == null || traitStr.isBlank()) return null;
        String clean = traitStr.trim().toUpperCase().replace(" ", "_");

        return switch (clean) {
            case "HARDY" -> new TraitInfo("HARDY", "Hardy", "POSITIVE", "Loses 20% less life in combat.");
            case "QUICK_GROWER", "QUICK GROWER" -> new TraitInfo("QUICK_GROWER", "Quick Grower", "POSITIVE", "Matures 50% faster.");
            case "LUCKY" -> new TraitInfo("LUCKY", "Lucky", "POSITIVE", "Finds 10% of extra loot on adventures.");
            case "CALM" -> new TraitInfo("CALM", "Calm", "POSITIVE", "Stress increase reduced by 20%.");
            case "FERTILE" -> new TraitInfo("FERTILE", "Fertile", "POSITIVE", "25% chance of twin pregnancy.");
            case "GLUTTON" -> new TraitInfo("GLUTTON", "Glutton", "NEGATIVE", "Consumes 20% more food.");
            case "FRAGILE" -> new TraitInfo("FRAGILE", "Fragile", "NEGATIVE", "Loses 20% more life in combat.");
            case "SKITTISH" -> new TraitInfo("SKITTISH", "Skittish", "NEGATIVE", "Stress increase increased by 20%.");
            case "LAZY" -> new TraitInfo("LAZY", "Lazy", "NEGATIVE", "Completes tasks 20% slower.");
            case "WEAK_GENES", "WEAK GENES" -> new TraitInfo("WEAK_GENES", "Weak Genes", "NEGATIVE", "20% chance for offspring to receive an additional random negative trait.");
            default -> new TraitInfo(clean, traitStr, "POSITIVE", "Special rabbit trait.");
        };
    }

    private void updateList() {
        try {
            Long currentSessionPlayerId = playerSession.getPlayerId();
            if (currentSessionPlayerId == null) return;

            List<RabbitDto> rabbits = backendClientService.getAllRabbits().stream()
                    .filter(r -> currentSessionPlayerId.equals(r.playerId()))
                    .filter(r -> !"DEAD".equalsIgnoreCase(r.status()))
                    .toList();

            grid.setItems(rabbits);
        } catch (Exception e) {
            com.vaadin.flow.component.notification.Notification.show("Error loading rabbits: " + e.getMessage());
        }
    }
}