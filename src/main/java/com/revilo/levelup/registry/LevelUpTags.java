package com.revilo.levelup.registry;

import com.revilo.levelup.LevelUpMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public final class LevelUpTags {
    public static final String DROPS_LEVELS_ENTITY_TAG = "drops_levels";
    public static final TagKey<EntityType<?>> DROPS_LEVELS = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(LevelUpMod.MOD_ID, "drops_levels")
    );

    private LevelUpTags() {}
}
