package com.revilo.levelup.event;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.api.event.PlayerLevelDataLoadedEvent;
import com.revilo.levelup.config.LevelUpConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.NeoForge;

@EventBusSubscriber(modid = LevelUpMod.MOD_ID)
public final class LevelUpGameplayEvents {
    private LevelUpGameplayEvents() {}

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            LevelUpApi.getPlayerData(player).ifPresent(data ->
                    NeoForge.EVENT_BUS.post(new PlayerLevelDataLoadedEvent(player, data))
            );
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
        ServerPlayer killer = getValidPlayerMeleeKiller(event, deadEntity);
        if (killer == null) {
            return;
        }
        if (!LevelUpConfig.COMMON.shouldDropLevels(deadEntity)) {
            return;
        }
        int dropAmount = LevelUpConfig.COMMON.getMobLevelDropAmount(deadEntity);
        if (dropAmount <= 0) {
            return;
        }
        LevelUpApi.spawnLevelUpXpOrb(serverLevel, deadEntity.position(), dropAmount);
    }

    private static ServerPlayer getValidPlayerMeleeKiller(LivingDeathEvent event, LivingEntity deadEntity) {
        if (!event.getSource().is(DamageTypes.PLAYER_ATTACK)) {
            return null;
        }
        Entity attacker = event.getSource().getEntity();
        if (!(attacker instanceof ServerPlayer killer)) {
            return null;
        }
        if (attacker != event.getSource().getDirectEntity()) {
            return null;
        }
        if (killer == deadEntity) {
            return null;
        }
        if (deadEntity.getKillCredit() != killer) {
            return null;
        }
        return killer;
    }
}
