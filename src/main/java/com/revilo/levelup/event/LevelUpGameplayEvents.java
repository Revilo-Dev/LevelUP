package com.revilo.levelup.event;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.config.LevelUpConfig;
import com.revilo.levelup.registry.LevelUpTags;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = LevelUpMod.MOD_ID)
public final class LevelUpGameplayEvents {
    private LevelUpGameplayEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LevelUpApi.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LevelUpApi.sync(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LevelUpApi.sync(player);
        }
    }

    @SubscribeEvent
    public static void onMobKilled(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof Mob deadEntity)) {
            return;
        }
        if (!(deadEntity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(deadEntity.getKillCredit() instanceof ServerPlayer killer)) {
            return;
        }
        if (!shouldDropLevelXp(deadEntity)) {
            return;
        }

        int baseDrop = LevelUpConfig.COMMON.mobKillXp.get();
        int vanillaXp = Math.max(0, deadEntity.getExperienceReward(serverLevel, killer));
        int totalDrop = baseDrop + vanillaXp;
        if (totalDrop <= 0) {
            return;
        }

        LevelUpApi.spawnLevelUpXpOrb(serverLevel, deadEntity.position(), totalDrop);
    }

    private static boolean shouldDropLevelXp(LivingEntity entity) {
        if (!LevelUpConfig.COMMON.dropLevelsOnlyFromMobsWithTag.get()) {
            return entity instanceof Enemy;
        }

        return entity.getType().is(LevelUpTags.DROPS_LEVELS)
                || entity.getTags().contains(LevelUpTags.DROPS_LEVELS_ENTITY_TAG);
    }
}
