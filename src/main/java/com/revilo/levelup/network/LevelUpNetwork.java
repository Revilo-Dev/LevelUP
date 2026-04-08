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
        PayloadRegistrar registrar = event.registrar(PROTOCOL_VERSION);
        registrar.playToClient(
                S2CPlayerProgressionSyncPacket.TYPE,
                S2CPlayerProgressionSyncPacket.STREAM_CODEC,
                S2CPlayerProgressionSyncPacket::handle
        );
    }

    public static void syncPlayer(ServerPlayer player) {
        syncPlayer(player, false);
    }

    public static void syncPlayer(ServerPlayer player, boolean showOverlay) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        PacketDistributor.sendToPlayer(player, new S2CPlayerProgressionSyncPacket(data.getLevel(), data.getXp(), data.getMultiplier(), showOverlay));
    }
}
