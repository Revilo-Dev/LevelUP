package com.revilo.levelup.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.OptionalInt;

public final class LevelUpApi {
    public static final String API_VERSION = "2";
    // api delegate
    private static final ILevelUpService SERVICE = LevelUpService.INSTANCE;

    private LevelUpApi() {}

    public static String getApiVersion() {
        return SERVICE.getApiVersion();
    }

    public static LevelUpCapabilities getCapabilities() {
        return SERVICE.getCapabilities();
    }

    public static int getMaxLevel() {
        return SERVICE.getMaxLevel();
    }

    public static OptionalInt getMaximumLevel() {
        return SERVICE.getMaximumLevel();
    }

    public static double getLevelMultiplier() {
        return SERVICE.getLevelMultiplier();
    }

    public static Optional<LevelUpPlayerView> getPlayerData(ServerPlayer player) {
        return SERVICE.getPlayerData(player);
    }

    public static LevelCurve getActiveLevelCurve() {
        return SERVICE.getActiveLevelCurve();
    }

    public static int getLevel(Player player) {
        return SERVICE.getLevel(player);
    }

    public static int getHighestLevelReached(Player player) {
        return SERVICE.getHighestLevelReached(player);
    }

    public static long getXp(Player player) {
        return SERVICE.getXp(player);
    }

    public static int getXpMultiplier(Player player) {
        return SERVICE.getXpMultiplier(player);
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

    public static boolean isAtMaximumLevel(ServerPlayer player) {
        return SERVICE.isAtMaximumLevel(player);
    }

    public static void registerMilestone(ResourceLocation id, int interval) {
        SERVICE.registerMilestone(id, interval);
    }

    public static LevelChangeResult addExperience(ServerPlayer player, long amount, ResourceLocation source) {
        return SERVICE.addExperience(player, amount, source);
    }

    // legacy helper
    public static long addXp(ServerPlayer player, long amount, ResourceLocation source) {
        return SERVICE.addXp(player, amount, source);
    }

    // legacy helper
    public static void awardXp(ServerPlayer player, long amount, ResourceLocation source) {
        SERVICE.addXp(player, amount, source);
    }

    public static LevelChangeResult setExperience(ServerPlayer player, long amount, ResourceLocation source) {
        return SERVICE.setExperience(player, amount, source);
    }

    public static LevelChangeResult addLevels(ServerPlayer player, int levels, ResourceLocation source) {
        return SERVICE.addLevels(player, levels, source);
    }

    public static LevelChangeResult setLevel(ServerPlayer player, int level, ResourceLocation source) {
        return SERVICE.setLevel(player, level, source);
    }

    public static LevelChangeResult reset(ServerPlayer player, ResourceLocation source) {
        return SERVICE.reset(player, source);
    }

    public static void setXp(ServerPlayer player, long xp) {
        SERVICE.setXp(player, xp);
    }

    public static void setLevel(ServerPlayer player, int level) {
        SERVICE.setLevel(player, level);
    }

    public static void setXpMultiplier(ServerPlayer player, int multiplier) {
        SERVICE.setXpMultiplier(player, multiplier);
    }

    public static boolean isLevelLocked(Player player) {
        return SERVICE.isLevelLocked(player);
    }

    public static int getLockedLevel(Player player) {
        return SERVICE.getLockedLevel(player);
    }

    public static void lockLevel(ServerPlayer player, int level) {
        SERVICE.lockLevel(player, level);
    }

    public static void unlockLevel(ServerPlayer player) {
        SERVICE.unlockLevel(player);
    }

    public static boolean isPaused() {
        return SERVICE.isPaused();
    }

    public static void setPaused(boolean paused) {
        SERVICE.setPaused(paused);
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

    // coordinate overload
    public static void spawnLevelUpXpOrb(ServerLevel level, double x, double y, double z, int amount) {
        SERVICE.spawnXpOrb(level, x, y, z, amount);
    }

    public static void spawnLevelUpXpOrbForLevel(ServerLevel serverLevel, Vec3 position, int targetLevel) {
        SERVICE.spawnXpOrbForLevel(serverLevel, position, targetLevel);
    }

    // coordinate overload
    public static void spawnLevelUpXpOrbForLevel(ServerLevel serverLevel, double x, double y, double z, int targetLevel) {
        SERVICE.spawnXpOrbForLevel(serverLevel, x, y, z, targetLevel);
    }
}
