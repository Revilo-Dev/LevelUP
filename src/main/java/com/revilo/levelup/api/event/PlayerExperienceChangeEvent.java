package com.revilo.levelup.api.event;

import com.revilo.levelup.api.LevelChangeReason;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class PlayerExperienceChangeEvent extends Event {
    private final ServerPlayer player;
    private final long oldExperience;
    private final long newExperience;
    private final LevelChangeReason reason;
    private final ResourceLocation source;

    public PlayerExperienceChangeEvent(ServerPlayer player, long oldExperience, long newExperience, LevelChangeReason reason, ResourceLocation source) {
        this.player = player;
        this.oldExperience = oldExperience;
        this.newExperience = newExperience;
        this.reason = reason;
        this.source = source;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public long getOldExperience() {
        return oldExperience;
    }

    public long getNewExperience() {
        return newExperience;
    }

    public LevelChangeReason getReason() {
        return reason;
    }

    public ResourceLocation getSource() {
        return source;
    }
}
