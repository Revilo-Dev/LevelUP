package com.revilo.levelup.registry;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.data.PlayerProgressionData;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public final class LevelUpAttachments {
    public static final DeferredRegister<AttachmentType<?>> REGISTER =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, LevelUpMod.MOD_ID);

    public static final Supplier<AttachmentType<PlayerProgressionData>> PLAYER_PROGRESSION = REGISTER.register(
            "player_progression",
            () -> AttachmentType.builder(PlayerProgressionData::new)
                    .serialize(PlayerProgressionData.CODEC)
                    .copyOnDeath()
                    .build()
    );

    private LevelUpAttachments() {}
}
