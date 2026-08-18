package com.revilo.levelup.network;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.client.ClientPlayerLevels;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.UUID;

/** Synchronizes a player's level to clients that render player labels. */
public record S2CPlayerLevelDisplaySyncPacket(UUID playerId, int level) implements CustomPacketPayload {
    public static final Type<S2CPlayerLevelDisplaySyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LevelUpMod.MOD_ID, "sync_player_level_display"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPlayerLevelDisplaySyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(S2CPlayerLevelDisplaySyncPacket::write, S2CPlayerLevelDisplaySyncPacket::new);

    private S2CPlayerLevelDisplaySyncPacket(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUUID(), buffer.readVarInt());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(playerId);
        buffer.writeVarInt(level);
    }

    @Override
    public Type<S2CPlayerLevelDisplaySyncPacket> type() {
        return TYPE;
    }

    public static void handle(S2CPlayerLevelDisplaySyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> ClientPlayerLevels.update(payload.playerId(), payload.level()));
    }
}
