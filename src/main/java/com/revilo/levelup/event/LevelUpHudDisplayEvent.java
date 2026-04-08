package com.revilo.levelup.event;

import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.Event;

public class LevelUpHudDisplayEvent extends Event {
    private final Component label;
    private final float progress;
    private final long durationMillis;

    public LevelUpHudDisplayEvent(Component label, float progress, long durationMillis) {
        this.label = label;
        this.progress = Math.max(0.0F, Math.min(1.0F, progress));
        this.durationMillis = Math.max(0L, durationMillis);
    }

    public Component getLabel() {
        return label;
    }

    public float getProgress() {
        return progress;
    }

    public long getDurationMillis() {
        return durationMillis;
    }
}
