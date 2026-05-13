package com.revilo.levelup.event;

import net.neoforged.bus.api.Event;

public class LevelUpHudEnabledEvent extends Event {
    private final boolean enabled;

    public LevelUpHudEnabledEvent(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
