package com.revilo.levelup.api;

import com.revilo.levelup.event.LevelUpLevelChangedEvent;
import com.revilo.levelup.event.LevelUpLevelLockChangedEvent;
import com.revilo.levelup.event.LevelUpOutputEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

// example integration hooks
// reference patterns
@EventBusSubscriber
public final class LevelUpIntegrationExamples {
    private LevelUpIntegrationExamples() {}

    public static void onGatewayCompleted(ServerPlayer player, Vec3 rewardPos, int xpReward) {
        LevelUpApi.awardXp(player, xpReward, LevelUpSources.GATEWAY_COMPLETE);
        LevelUpApi.spawnLevelUpXpOrb(player.serverLevel(), rewardPos, xpReward);
    }

    public static void onShrineCompleted(ServerPlayer player, Vec3 rewardPos, int rewardLevel) {
        LevelUpApi.spawnLevelUpXpOrbForLevel(player.serverLevel(), rewardPos, rewardLevel);
    }

    public static double scaleGatewayModifier(ServerPlayer player, double baseValue) {
        int level = LevelUpApi.getLevel(player);
        return baseValue * (1.0D + (level * 0.01D));
    }

    public static boolean canUseFeature(ServerPlayer player, ResourceLocation featureId, int requiredLevel) {
        return LevelUpApi.meetsLevelRequirement(player, requiredLevel);
    }

    public static void onProgressionPackLoaded() {
        LevelUpApi.setMaxLevelOverride(1000);
        LevelUpApi.setLevelMultiplierOverride(0.60D);
    }

    public static void onPlayerStartedStoryArc(ServerPlayer player) {
        // story gate lock
        LevelUpApi.lockLevel(player, 10);
    }

    public static void onBossObjectiveCompleted(ServerPlayer player) {
        // story gate unlock
        LevelUpApi.unlockLevel(player);
    }

    @SubscribeEvent
    public static void onLevelUpGrantSkillPoint(LevelUpLevelChangedEvent.LevelUp event) {
        ServerPlayer player = event.getPlayer();
        int newLevel = event.getNewLevel();
        // skill point bridge
        // codex skills api add skill points
    }

    @SubscribeEvent
    public static void onLevelUpOutput(LevelUpOutputEvent event) {
        // output payload values
        // player old level new level old xp new xp source levels gained
    }

    @SubscribeEvent
    public static void onLevelLockChanged(LevelUpLevelLockChangedEvent event) {
        // lock payload values
        // player old lock new lock locked state
    }
}
