package com.revilo.levelup.event;

import net.neoforged.bus.api.Event;

public class LevelUpHudPositionEvent extends Event {
    private final String position;

    public LevelUpHudPositionEvent(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }
}
