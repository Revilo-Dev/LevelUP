package com.revilo.levelup.event;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

// level lock event
// minus one means unlocked
public class LevelUpLevelLockChangedEvent extends Event {
    private final ServerPlayer player;
    private final int oldLockedLevel;
    private final int newLockedLevel;

    public LevelUpLevelLockChangedEvent(ServerPlayer player, int oldLockedLevel, int newLockedLevel) {
        this.player = player;
        this.oldLockedLevel = oldLockedLevel;
        this.newLockedLevel = newLockedLevel;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getOldLockedLevel() {
        return oldLockedLevel;
    }

    public int getNewLockedLevel() {
        return newLockedLevel;
    }

    public boolean isLocked() {
        return newLockedLevel >= 0;
    }
}
