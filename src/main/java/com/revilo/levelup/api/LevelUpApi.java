package com.revilo.levelup.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public final class LevelUpApi {
    private static final ILevelUpService SERVICE = LevelUpService.INSTANCE;

    private LevelUpApi() {}

    public static int getMaxLevel() {
        return SERVICE.getMaxLevel();
    }

    public static double getLevelMultiplier() {
        return SERVICE.getLevelMultiplier();
    }

    public static int getLevel(Player player) {
        return SERVICE.getLevel(player);
    }

    public static long getXp(Player player) {
        return SERVICE.getXp(player);
    }

    public static long getXpIntoCurrentLevel(Player player) {
        return SERVICE.getXpIntoCurrentLevel(player);
    }

    public static long getXpNeededForNextLevel(Player player) {
        return SERVICE.getXpNeededForNextLevel(player);
    }

    public static float getProgressToNextLevel(Player player) {
        return SERVICE.getProgressToNextLevel(player);
    }

    public static long getXpForNextLevel(int currentLevel) {
        return SERVICE.getXpForNextLevel(currentLevel);
    }

    public static long getTotalXpForLevel(int level) {
        return SERVICE.getTotalXpForLevel(level);
    }

    public static int levelForTotalXp(long totalXp) {
        return SERVICE.levelForTotalXp(totalXp);
    }

    public static boolean meetsLevelRequirement(Player player, int requiredLevel) {
        return SERVICE.meetsLevelRequirement(player, requiredLevel);
    }

    public static long addXp(ServerPlayer player, long amount, ResourceLocation source) {
        return SERVICE.addXp(player, amount, source);
    }

    public static void awardXp(ServerPlayer player, long amount, ResourceLocation source) {
        SERVICE.addXp(player, amount, source);
    }

    public static long addLevels(ServerPlayer player, int levels, ResourceLocation source) {
        return SERVICE.addLevels(player, levels, source);
    }

    public static void setXp(ServerPlayer player, long xp) {
        SERVICE.setXp(player, xp);
    }

    public static void setLevel(ServerPlayer player, int level) {
        SERVICE.setLevel(player, level);
    }

    public static void setMaxLevelOverride(int maxLevel) {
        SERVICE.setMaxLevelOverride(maxLevel);
    }

    public static void clearMaxLevelOverride() {
        SERVICE.clearMaxLevelOverride();
    }

    public static void setLevelMultiplierOverride(double levelMultiplier) {
        SERVICE.setLevelMultiplierOverride(levelMultiplier);
    }

    public static void clearLevelMultiplierOverride() {
        SERVICE.clearLevelMultiplierOverride();
    }

    public static void sync(ServerPlayer player) {
        SERVICE.sync(player);
    }

    public static void spawnLevelUpXpOrb(ServerLevel level, Vec3 position, int amount) {
        SERVICE.spawnXpOrb(level, position, amount);
    }

    public static void spawnLevelUpXpOrb(ServerLevel level, double x, double y, double z, int amount) {
        SERVICE.spawnXpOrb(level, x, y, z, amount);
    }

    public static void spawnLevelUpXpOrbForLevel(ServerLevel serverLevel, Vec3 position, int targetLevel) {
        SERVICE.spawnXpOrbForLevel(serverLevel, position, targetLevel);
    }

    public static void spawnLevelUpXpOrbForLevel(ServerLevel serverLevel, double x, double y, double z, int targetLevel) {
        SERVICE.spawnXpOrbForLevel(serverLevel, x, y, z, targetLevel);
    }
}
