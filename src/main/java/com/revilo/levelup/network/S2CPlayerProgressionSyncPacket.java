package com.revilo.levelup.network;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.data.PlayerProgressionData;
import com.revilo.levelup.registry.LevelUpAttachments;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public record S2CPlayerProgressionSyncPacket(int level, long xp) implements CustomPacketPayload {
    public static final Type<S2CPlayerProgressionSyncPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(LevelUpMod.MOD_ID, "sync_player_progression"));
    public static final StreamCodec<RegistryFriendlyByteBuf, S2CPlayerProgressionSyncPacket> STREAM_CODEC =
            CustomPacketPayload.codec(S2CPlayerProgressionSyncPacket::write, S2CPlayerProgressionSyncPacket::new);

    private S2CPlayerProgressionSyncPacket(RegistryFriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readVarLong());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(level);
        buffer.writeVarLong(xp);
    }

    @Override
    public Type<S2CPlayerProgressionSyncPacket> type() {
        return TYPE;
    }

    public static void handle(S2CPlayerProgressionSyncPacket payload, IPayloadContext context) {
        context.enqueueWork(() -> {
            Player player = context.player();
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            data.setLevel(payload.level());
            data.setXp(payload.xp());
        });
    }
}
