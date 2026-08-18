package com.revilo.levelup.client;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Client-side level snapshots for rendering other players' level labels. */
public final class ClientPlayerLevels {
    private static final Map<UUID, Integer> LEVELS = new ConcurrentHashMap<>();

    private ClientPlayerLevels() {}

    public static void update(UUID playerId, int level) {
        LEVELS.put(playerId, level);
    }

    public static int get(UUID playerId) {
        return LEVELS.getOrDefault(playerId, 0);
    }
}
