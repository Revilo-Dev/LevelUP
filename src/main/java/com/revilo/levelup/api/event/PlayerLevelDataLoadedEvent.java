package com.revilo.levelup.api.event;

import com.revilo.levelup.api.LevelUpPlayerView;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.Event;

public class PlayerLevelDataLoadedEvent extends Event {
    private final ServerPlayer player;
    private final LevelUpPlayerView data;

    public PlayerLevelDataLoadedEvent(ServerPlayer player, LevelUpPlayerView data) {
        this.player = player;
        this.data = data;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public LevelUpPlayerView getData() {
        return data;
    }
}
