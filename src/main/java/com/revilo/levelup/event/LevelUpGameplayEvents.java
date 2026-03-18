package com.revilo.levelup.event;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.api.LevelUpSources;
import com.revilo.levelup.config.LevelUpConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerXpEvent;

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
        if (!LevelUpConfig.COMMON.allMobsDropLevelXp.get()) {
            return;
        }
        if (!(event.getEntity() instanceof LivingEntity deadEntity)) {
            return;
        }
        if (deadEntity instanceof ServerPlayer) {
            return;
        }
        if (!(deadEntity.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(deadEntity.getKillCredit() instanceof ServerPlayer killer)) {
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

    @SubscribeEvent
    public static void onVanillaXpGain(PlayerXpEvent.XpChange event) {
        if (!LevelUpConfig.COMMON.convertVanillaXp.get()) {
            return;
        }
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (event.getAmount() <= 0) {
            return;
        }
        long converted = Math.round(event.getAmount() * LevelUpConfig.COMMON.vanillaXpMultiplier.get());
        if (converted > 0L) {
            LevelUpApi.awardXp(player, converted, LevelUpSources.VANILLA_XP);
        }
    }
}
