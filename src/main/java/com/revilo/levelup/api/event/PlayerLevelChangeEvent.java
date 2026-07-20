package com.revilo.levelup.api.event;

import com.revilo.levelup.api.LevelChangeReason;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class PlayerLevelChangeEvent extends Event {
    private final ServerPlayer player;
    private final int previousLevel;
    private final int newLevel;
    private final LevelChangeReason reason;
    private final ResourceLocation source;

    public PlayerLevelChangeEvent(ServerPlayer player, int previousLevel, int newLevel, LevelChangeReason reason, ResourceLocation source) {
        this.player = player;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.reason = reason;
        this.source = source;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public int getPreviousLevel() {
        return previousLevel;
    }

    public int getNewLevel() {
        return newLevel;
    }

    public LevelChangeReason getReason() {
        return reason;
    }

    public ResourceLocation getSource() {
        return source;
    }
}
