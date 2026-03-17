package com.revilo.levelup.event;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.api.LevelUpSources;
import com.revilo.levelup.config.LevelUpConfig;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.monster.Zombie;
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
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getEntity() instanceof Zombie zombie)) {
            return;
        }
        // Temporary testing behavior: zombies only, fixed 50 LevelUP XP dropped as blue orbs.
        LevelUpApi.spawnLevelUpXpOrb(player.serverLevel(), zombie.position(), 50);
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
