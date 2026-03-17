package com.revilo.levelup.registry;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.entity.LevelUpXpOrbEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LevelUpEntities {
    public static final DeferredRegister<EntityType<?>> REGISTER =
            DeferredRegister.create(Registries.ENTITY_TYPE, LevelUpMod.MOD_ID);

    public static final Supplier<EntityType<LevelUpXpOrbEntity>> LEVEL_ORB = REGISTER.register(
            "level_orb",
            () -> EntityType.Builder.<LevelUpXpOrbEntity>of(LevelUpXpOrbEntity::new, MobCategory.MISC)
                    .sized(0.5F, 0.5F)
                    .clientTrackingRange(6)
                    .updateInterval(2)
                    .build("level_orb")
    );

    private LevelUpEntities() {}
}
