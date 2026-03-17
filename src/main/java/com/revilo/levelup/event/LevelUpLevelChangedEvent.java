package com.revilo.levelup.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class LevelUpLevelChangedEvent extends Event {
    private final ServerPlayer player;
    private final int oldLevel;
    private final int newLevel;

    public LevelUpLevelChangedEvent(ServerPlayer player, int oldLevel, int newLevel) {
        this.player = player;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
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

    public static final class LevelUp extends LevelUpLevelChangedEvent {
        public LevelUp(ServerPlayer player, int oldLevel, int newLevel) {
            super(player, oldLevel, newLevel);
        }
    }
}
