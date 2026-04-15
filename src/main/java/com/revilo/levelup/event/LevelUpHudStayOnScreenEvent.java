package com.revilo.levelup.event;

import net.neoforged.bus.api.Event;

public class LevelUpHudStayOnScreenEvent extends Event {
    private final boolean stayOnScreen;

    public LevelUpHudStayOnScreenEvent(boolean stayOnScreen) {
        this.stayOnScreen = stayOnScreen;
    }

    public boolean shouldStayOnScreen() {
        return stayOnScreen;
    }
}
