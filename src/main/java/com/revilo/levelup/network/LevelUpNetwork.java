package com.revilo.levelup.network;

import com.revilo.levelup.data.PlayerProgressionData;
import com.revilo.levelup.registry.LevelUpAttachments;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class LevelUpNetwork {
    private static final String PROTOCOL_VERSION = "1";

    private LevelUpNetwork() {}

    public static void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        // client sync packet
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                S2CPlayerProgressionSyncPacket.TYPE,
                S2CPlayerProgressionSyncPacket.STREAM_CODEC,
                S2CPlayerProgressionSyncPacket::handle
        );
        registrar.playToClient(
                S2CPlayerLevelDisplaySyncPacket.TYPE,
                S2CPlayerLevelDisplaySyncPacket.STREAM_CODEC,
                S2CPlayerLevelDisplaySyncPacket::handle
        );
    }

    public static void syncPlayer(ServerPlayer player) {
        syncPlayer(player, false);
    }

    public static void syncPlayer(ServerPlayer player, boolean showOverlay) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        // attachment snapshot
        PacketDistributor.sendToPlayer(player, new S2CPlayerProgressionSyncPacket(
                data.getLevel(),
                data.getXp(),
                data.getMultiplier(),
                data.getHighestLevelReached(),
                showOverlay
        ));
        player.refreshTabListName();
        PacketDistributor.sendToAllPlayers(new S2CPlayerLevelDisplaySyncPacket(player.getUUID(), data.getLevel()));
    }

    public static void syncAllDisplayLevels(ServerPlayer recipient) {
        for (ServerPlayer player : recipient.server.getPlayerList().getPlayers()) {
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            PacketDistributor.sendToPlayer(recipient, new S2CPlayerLevelDisplaySyncPacket(player.getUUID(), data.getLevel()));
        }
    }
}
