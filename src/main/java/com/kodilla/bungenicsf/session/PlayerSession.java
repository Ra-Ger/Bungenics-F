package com.kodilla.bungenicsf.session;

import com.vaadin.flow.server.VaadinSession;
import com.vaadin.flow.spring.annotation.VaadinSessionScope;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@VaadinSessionScope
public class PlayerSession implements Serializable {

    private static final String PLAYER_ID_KEY = "playerId";

    public Long getPlayerId() {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            Object id = session.getAttribute(PLAYER_ID_KEY);
            if (id instanceof Long longId) {
                return longId;
            }
        }
        return null;
    }

    public void setPlayerId(Long playerId) {
        VaadinSession session = VaadinSession.getCurrent();
        if (session != null) {
            session.setAttribute(PLAYER_ID_KEY, playerId);
        }
    }

    public void set(Long playerId) {
        setPlayerId(playerId);
    }

    public boolean isActive() {
        return getPlayerId() != null;
    }
}