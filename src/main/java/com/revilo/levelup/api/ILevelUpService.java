package com.revilo.levelup.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.OptionalInt;

public interface ILevelUpService {
    String getApiVersion();

    LevelUpCapabilities getCapabilities();

    int getMaxLevel();

    OptionalInt getMaximumLevel();

    double getLevelMultiplier();

    Optional<LevelUpPlayerView> getPlayerData(ServerPlayer player);

    LevelCurve getActiveLevelCurve();

    int getLevel(Player player);

    int getHighestLevelReached(Player player);

    long getXp(Player player);

    int getXpMultiplier(Player player);

    long getXpIntoCurrentLevel(Player player);

    long getXpNeededForNextLevel(Player player);

    float getProgressToNextLevel(Player player);

    long getXpForNextLevel(int currentLevel);

    long getTotalXpForLevel(int level);

    int levelForTotalXp(long totalXp);

    boolean meetsLevelRequirement(Player player, int requiredLevel);

    boolean isAtMaximumLevel(ServerPlayer player);

    void registerMilestone(ResourceLocation id, int interval);

    LevelChangeResult addExperience(ServerPlayer player, long amount, ResourceLocation source);

    long addXp(ServerPlayer player, long amount, ResourceLocation source);

    LevelChangeResult setExperience(ServerPlayer player, long xp, ResourceLocation source);

    LevelChangeResult addLevels(ServerPlayer player, int levels, ResourceLocation source);

    LevelChangeResult setLevel(ServerPlayer player, int level, ResourceLocation source);

    LevelChangeResult reset(ServerPlayer player, ResourceLocation source);

    void setXp(ServerPlayer player, long xp);

    void setLevel(ServerPlayer player, int level);

    void setXpMultiplier(ServerPlayer player, int multiplier);

    boolean isLevelLocked(Player player);

    int getLockedLevel(Player player);

    void lockLevel(ServerPlayer player, int level);

    void unlockLevel(ServerPlayer player);

    boolean isPaused();

    void setPaused(boolean paused);

    void setMaxLevelOverride(int maxLevel);

    void clearMaxLevelOverride();

    void setLevelMultiplierOverride(double levelMultiplier);

    void clearLevelMultiplierOverride();

    void sync(ServerPlayer player);

    void spawnXpOrb(ServerLevel level, Vec3 position, int amount);

    void spawnXpOrb(ServerLevel level, double x, double y, double z, int amount);

    void spawnXpOrbForLevel(ServerLevel serverLevel, Vec3 position, int targetLevel);

    void spawnXpOrbForLevel(ServerLevel serverLevel, double x, double y, double z, int targetLevel);
}
