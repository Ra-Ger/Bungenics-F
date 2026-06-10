package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.dto.AdventureDto;
import com.kodilla.bungenicsf.dto.AdventureEventDto;
import com.kodilla.bungenicsf.service.BackendClientService;
import com.kodilla.bungenicsf.session.PlayerSession;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

import java.util.List;

@Route(value = "info", layout = MainLayout.class)
public class InfoView extends VerticalLayout {

    private final BackendClientService backendClientService;
    private final PlayerSession playerSession;
    private final VerticalLayout logsContainer = new VerticalLayout();

    public InfoView(BackendClientService backendClientService, PlayerSession playerSession) {
        this.backendClientService = backendClientService;
        this.playerSession = playerSession;

        setSpacing(true);
        setPadding(true);

        add(new H2("📜 Game Info & Expedition Reports"));
        add(new Paragraph("Welcome to Bunny Farm Idle Clicker. Inspect your adventure logs, event reports, and rewards below!"));

        Button refreshBtn = new Button("🔄 Refresh Logs", e -> refreshLogs());
        refreshBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);

        logsContainer.setWidthFull();
        add(logsContainer);

        refreshLogs();
    }

    private void refreshLogs() {
        logsContainer.removeAll();
        try {
            Long currentSessionPlayerId = playerSession.getPlayerId();
            if (currentSessionPlayerId == null) {
                logsContainer.add(new Span("No player session found."));
                return;
            }

            List<AdventureDto> adventures = backendClientService.getCompletedAdventures(currentSessionPlayerId);

            if (adventures == null || adventures.isEmpty()) {
                logsContainer.add(new Span("No completed expeditions recorded yet. Send rabbits on adventures to see logs here!"));
                return;
            }

            for (AdventureDto adv : adventures) {
                HorizontalLayout card = new HorizontalLayout();
                card.setWidthFull();
                card.getStyle()
                        .set("background", "#ffffff")
                        .set("border-left", "6px solid #2e7d32")
                        .set("border-radius", "10px")
                        .set("padding", "16px")
                        .set("box-shadow", "0 2px 8px rgba(0,0,0,0.08)")
                        .set("align-items", "center")
                        .set("cursor", "pointer");

                Image icon = new Image("images/farm/Playhouse.png", "Expedition");
                icon.setWidth("48px");
                icon.setHeight("48px");
                icon.getStyle().set("border-radius", "50%").set("margin-right", "15px");

                VerticalLayout info = new VerticalLayout();
                info.setPadding(false);
                info.setSpacing(false);

                H3 title = new H3("Log #" + adv.id() + " - " + adv.name());
                title.getStyle().set("margin", "0").set("font-size", "1.1rem");

                int eventCount = adv.adventureEvents() != null ? adv.adventureEvents().size() : 0;
                Span subtitle = new Span("Type: " + adv.type() + " | Status: " + adv.status() + " | Steps: " + eventCount);
                subtitle.getStyle().set("color", "#666").set("font-size", "0.9rem");

                info.add(title, subtitle);

                Button detailsBtn = new Button("View Details", e -> openExpeditionDetailsDialog(adv));
                detailsBtn.addThemeVariants(ButtonVariant.LUMO_SMALL, ButtonVariant.LUMO_SUCCESS);
                detailsBtn.getStyle().set("margin-left", "auto");

                card.add(icon, info, detailsBtn);
                logsContainer.add(card);
            }
        } catch (Exception e) {
            logsContainer.add(new Span("Error loading expedition logs: " + e.getMessage()));
        }
    }

    private void openExpeditionDetailsDialog(AdventureDto adv) {
        Dialog dialog = new Dialog();
        dialog.setHeaderTitle("Expedition Log Details #" + adv.id());
        dialog.setWidth("600px");

        VerticalLayout layout = new VerticalLayout();
        layout.setSpacing(true);

        layout.add(new Span("Expedition: " + adv.name()));
        layout.add(new Span("Location Type: " + adv.type()));
        layout.add(new Span("Assigned Rabbit ID: #" + adv.rabbitId()));
        layout.add(new Span("Status: " + adv.status()));

        layout.add(new H3("Chronological Journey Events:"));

        if (adv.adventureEvents() == null || adv.adventureEvents().isEmpty()) {
            layout.add(new Paragraph("No detailed event messages were generated for this expedition."));
        } else {
            int stepCounter = 1;
            for (AdventureEventDto event : adv.adventureEvents()) {
                VerticalLayout eventBox = new VerticalLayout();
                eventBox.setPadding(false);
                eventBox.getStyle()
                        .set("background", "#f8f9fa")
                        .set("border-left", "4px solid #0284c7")
                        .set("border-radius", "6px")
                        .set("padding", "10px 14px")
                        .set("margin-bottom", "6px");

                Span header = new Span("Step " + stepCounter + ": " + (event.name() != null ? event.name() : "Event"));
                header.getStyle().set("font-weight", "bold").set("color", "#1e293b").set("font-size", "0.95rem");

                Span body = new Span(event.result() != null ? event.result() : "");
                body.getStyle().set("color", "#334155").set("font-size", "0.9rem").set("margin-top", "2px");

                eventBox.add(header, body);

                if (event.goldReward() != null && event.goldReward().doubleValue() > 0) {
                    Span reward = new Span("💰 Reward: +" + event.goldReward() + " Gold");
                    reward.getStyle().set("color", "#15803d").set("font-weight", "bold").set("font-size", "0.85rem").set("margin-top", "4px");
                    eventBox.add(reward);
                }
                if (event.carrotReward() != null && event.carrotReward() > 0f) {
                    Span reward = new Span("🥕 Reward: +" + event.carrotReward() + " Carrots");
                    reward.getStyle().set("color", "#ea580c").set("font-weight", "bold").set("font-size", "0.85rem").set("margin-top", "2px");
                    eventBox.add(reward);
                }
                if (event.lettuceReward() != null && event.lettuceReward() > 0f) {
                    Span reward = new Span("🥬 Reward: +" + event.lettuceReward() + " Lettuce");
                    reward.getStyle().set("color", "#16a34a").set("font-weight", "bold").set("font-size", "0.85rem").set("margin-top", "2px");
                    eventBox.add(reward);
                }
                if (event.spinachReward() != null && event.spinachReward() > 0f) {
                    Span reward = new Span("🍃 Reward: +" + event.spinachReward() + " Spinach");
                    reward.getStyle().set("color", "#059669").set("font-weight", "bold").set("font-size", "0.85rem").set("margin-top", "2px");
                    eventBox.add(reward);
                }

                layout.add(eventBox);
                stepCounter++;
            }
        }

        Button closeBtn = new Button("Close", e -> dialog.close());
        dialog.getFooter().add(closeBtn);

        dialog.add(layout);
        dialog.open();
    }
}