package com.revilo.levelup.api.event;

import com.revilo.levelup.api.LevelChangeReason;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class PlayerLevelMilestoneEvent extends Event {
    private final ServerPlayer player;
    private final ResourceLocation milestoneId;
    private final int milestone;
    private final int previousLevel;
    private final int newLevel;
    private final LevelChangeReason reason;
    private final ResourceLocation source;

    public PlayerLevelMilestoneEvent(ServerPlayer player, int milestone, int previousLevel, int newLevel, LevelChangeReason reason, ResourceLocation source) {
        this(player, source, milestone, previousLevel, newLevel, reason, source);
    }

    public PlayerLevelMilestoneEvent(ServerPlayer player, ResourceLocation milestoneId, int milestone, int previousLevel, int newLevel, LevelChangeReason reason, ResourceLocation source) {
        this.player = player;
        this.milestoneId = milestoneId;
        this.milestone = milestone;
        this.previousLevel = previousLevel;
        this.newLevel = newLevel;
        this.reason = reason;
        this.source = source;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getMilestoneId() {
        return milestoneId;
    }

    public int getMilestone() {
        return milestone;
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
