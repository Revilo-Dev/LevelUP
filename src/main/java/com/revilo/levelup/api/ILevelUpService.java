package com.revilo.levelup.api;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public interface ILevelUpService {
    int getLevel(Player player);

    long getXp(Player player);

    long getXpNeededForNextLevel(Player player);

    float getProgressToNextLevel(Player player);

    boolean meetsLevelRequirement(Player player, int requiredLevel);

    long addXp(ServerPlayer player, long amount, ResourceLocation source);

    void setXp(ServerPlayer player, long xp);

    void setLevel(ServerPlayer player, int level);

    void sync(ServerPlayer player);

    void spawnXpOrb(ServerLevel level, Vec3 position, int amount);

    void spawnXpOrb(ServerLevel level, double x, double y, double z, int amount);
}
