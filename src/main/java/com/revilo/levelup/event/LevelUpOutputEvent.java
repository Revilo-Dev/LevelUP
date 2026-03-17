package com.revilo.levelup.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

/**
 * High-signal output event for cross-mod integrations when a player levels up.
 */
public class LevelUpOutputEvent extends Event {
    private final ServerPlayer player;
    private final int oldLevel;
    private final int newLevel;
    private final long oldXp;
    private final long newXp;
    private final ResourceLocation source;

    public LevelUpOutputEvent(ServerPlayer player, int oldLevel, int newLevel, long oldXp, long newXp, ResourceLocation source) {
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
        this.oldXp = oldXp;
        this.newXp = newXp;
        this.source = source;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getOldLevel() {
        return oldLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public long getOldXp() {
        return oldXp;
    }

    public long getNewXp() {
        return newXp;
    }

    public ResourceLocation getSource() {
        return source;
    }

    public int getLevelsGained() {
        return Math.max(0, newLevel - oldLevel);
    }
}
