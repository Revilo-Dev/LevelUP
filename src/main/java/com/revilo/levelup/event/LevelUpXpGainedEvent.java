package com.revilo.levelup.event;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class LevelUpXpGainedEvent extends Event implements ICancellableEvent {
    private final ServerPlayer player;
    private final ResourceLocation source;
    private long amount;

    public LevelUpXpGainedEvent(ServerPlayer player, ResourceLocation source, long amount) {
        this.player = player;
        this.source = source;
        this.amount = Math.max(0L, amount);
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public ResourceLocation getSource() {
        return source;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = Math.max(0L, amount);
    }
}
