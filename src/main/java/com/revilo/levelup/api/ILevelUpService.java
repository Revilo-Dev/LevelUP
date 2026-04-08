package com.revilo.levelup.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ILevelUpService {
    int getMaxLevel();

    double getLevelMultiplier();

    int getLevel(Player player);

    long getXp(Player player);

    int getXpMultiplier(Player player);

    long getXpIntoCurrentLevel(Player player);

    long getXpNeededForNextLevel(Player player);

    float getProgressToNextLevel(Player player);

    long getXpForNextLevel(int currentLevel);

    long getTotalXpForLevel(int level);

    int levelForTotalXp(long totalXp);

    boolean meetsLevelRequirement(Player player, int requiredLevel);

    long addXp(ServerPlayer player, long amount, ResourceLocation source);

    long addLevels(ServerPlayer player, int levels, ResourceLocation source);

    void setXp(ServerPlayer player, long xp);

    void setLevel(ServerPlayer player, int level);

    void setXpMultiplier(ServerPlayer player, int multiplier);

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
