package com.revilo.levelup.api;

import com.revilo.levelup.event.LevelUpLevelChangedEvent;
import com.revilo.levelup.event.LevelUpOutputEvent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

/**
 * Example extension points for other mods.
 * These methods are not auto-registered; copy patterns into external mods.
 */
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

    @SubscribeEvent
    public static void onLevelUpGrantSkillPoint(LevelUpLevelChangedEvent.LevelUp event) {
        ServerPlayer player = event.getPlayer();
        int newLevel = event.getNewLevel();
        // Example:
        // CodexSkillsApi.addSkillPoints(player, 1);
    }

    @SubscribeEvent
    public static void onLevelUpOutput(LevelUpOutputEvent event) {
        // Example output payload for external mods:
        // event.getPlayer(), event.getOldLevel(), event.getNewLevel(),
        // event.getOldXp(), event.getNewXp(), event.getSource(), event.getLevelsGained()
    }
}
