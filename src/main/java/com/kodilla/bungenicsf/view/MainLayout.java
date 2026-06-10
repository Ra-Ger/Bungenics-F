package com.kodilla.bungenicsf.view;

import com.kodilla.bungenicsf.session.PlayerSession;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.applayout.DrawerToggle;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.RouterLink;

public class MainLayout extends AppLayout implements BeforeEnterObserver {

    private final PlayerSession playerSession;

    public MainLayout(PlayerSession playerSession) {
        this.playerSession = playerSession;
        createHeader();
        createDrawer();
    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        if (!playerSession.isActive()) {
            event.forwardTo(StartMenuView.class);
        }
    }

    private void createHeader() {
        H1 logo = new H1("Bunny farm - Idle Game");
        logo.getStyle().set("font-size", "var(--lumo-font-size-l)").set("margin", "0");
        HorizontalLayout header = new HorizontalLayout(new DrawerToggle(), logo);
        header.setDefaultVerticalComponentAlignment(FlexComponent.Alignment.CENTER);
        header.setWidthFull();
        header.addClassNames("py-0", "px-m");
        addToNavbar(header);
    }

    private void createDrawer() {
        RouterLink infoLink = new RouterLink("Info", InfoView.class);
        RouterLink farmLink = new RouterLink("Farm Board", FarmView.class);
        RouterLink rabbitsLink = new RouterLink("Rabbits", RabbitsView.class);
        RouterLink adventuresLink = new RouterLink("Adventures", AdventuresView.class);
        RouterLink vetLink = new RouterLink("Vet Clinic 🏥", VetView.class);
        RouterLink shopLink = new RouterLink("Shop", ShopView.class);

        Button exitButton = new Button("Exit to Menu", VaadinIcon.SIGN_OUT.create(), e -> {
            playerSession.setPlayerId(null);
            getUI().ifPresent(ui -> ui.navigate(StartMenuView.class));
        });
        exitButton.addThemeVariants(ButtonVariant.LUMO_ERROR, ButtonVariant.LUMO_PRIMARY);
        exitButton.setWidthFull();
        exitButton.getStyle().set("margin-top", "auto").set("border-radius", "8px");

        VerticalLayout drawerLayout = new VerticalLayout(infoLink, farmLink, rabbitsLink, adventuresLink, vetLink, shopLink, exitButton);
        drawerLayout.setSizeFull();
        drawerLayout.setAlignItems(FlexComponent.Alignment.STRETCH);
        addToDrawer(drawerLayout);
    }
}