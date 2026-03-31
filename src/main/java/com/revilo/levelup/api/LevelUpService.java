package com.revilo.levelup.api;

import com.revilo.levelup.data.LevelFormula;
import com.revilo.levelup.data.PlayerProgressionData;
import com.revilo.levelup.entity.LevelUpXpOrbEntity;
import com.revilo.levelup.event.LevelUpLevelChangedEvent;
import com.revilo.levelup.event.LevelUpOutputEvent;
import com.revilo.levelup.event.LevelUpXpGainedEvent;
import com.revilo.levelup.network.LevelUpNetwork;
import com.revilo.levelup.registry.LevelUpAttachments;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;

public final class LevelUpService implements ILevelUpService {
    public static final LevelUpService INSTANCE = new LevelUpService();

    private LevelUpService() {}

    @Override
    public int getMaxLevel() {
        return LevelUpProgressionOverrides.getMaxLevel();
    }

    @Override
    public double getLevelMultiplier() {
        return LevelUpProgressionOverrides.getLevelMultiplier();
    }

    @Override
    public int getLevel(Player player) {
        return player.getData(LevelUpAttachments.PLAYER_PROGRESSION).getLevel();
    }

    @Override
    public long getXp(Player player) {
        return player.getData(LevelUpAttachments.PLAYER_PROGRESSION).getXp();
    }

    @Override
    public long getXpNeededForNextLevel(Player player) {
        int level = getLevel(player);
        if (level >= getMaxLevel()) {
            return 0L;
        }
        long total = getXp(player);
        long progressFloor = LevelFormula.totalXpForLevel(level);
        long progress = Math.max(0L, total - progressFloor);
        long cost = LevelFormula.xpForNextLevel(level);
        return Math.max(0L, cost - progress);
    }

    @Override
    public float getProgressToNextLevel(Player player) {
        int level = getLevel(player);
        if (level >= getMaxLevel()) {
            return 1.0F;
        }
        long total = getXp(player);
        long floor = LevelFormula.totalXpForLevel(level);
        long intoLevel = Math.max(0L, total - floor);
        long cost = Math.max(1L, LevelFormula.xpForNextLevel(level));
        return Math.min(1.0F, (float) intoLevel / (float) cost);
    }

    @Override
    public boolean meetsLevelRequirement(Player player, int requiredLevel) {
        return getLevel(player) >= Math.max(0, requiredLevel);
    }

    @Override
    public long addXp(ServerPlayer player, long amount, ResourceLocation source) {
        long safeAmount = Math.max(0L, amount);
        if (safeAmount == 0L) {
            return 0L;
        }

        LevelUpXpGainedEvent event = new LevelUpXpGainedEvent(player, source, safeAmount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            return 0L;
        }
        long gain = Math.max(0L, event.getAmount());
        if (gain == 0L) {
            return 0L;
        }

        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        int oldLevel = data.getLevel();
        long oldXp = data.getXp();
        long newXp = LevelFormula.saturatingAdd(oldXp, gain);
        data.setXp(newXp);

        int newLevel = LevelFormula.levelForXp(newXp, getMaxLevel());
        data.setLevel(newLevel);

        if (newLevel != oldLevel) {
            NeoForge.EVENT_BUS.post(new LevelUpLevelChangedEvent(player, oldLevel, newLevel));
            if (newLevel > oldLevel) {
                NeoForge.EVENT_BUS.post(new LevelUpOutputEvent(player, oldLevel, newLevel, oldXp, newXp, source));
            }
            if (newLevel > oldLevel) {
                for (int level = oldLevel + 1; level <= newLevel; level++) {
                    NeoForge.EVENT_BUS.post(new LevelUpLevelChangedEvent.LevelUp(player, level - 1, level));
                }
            }
        }

        sync(player);
        return gain;
    }

    @Override
    public void setXp(ServerPlayer player, long xp) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        int oldLevel = data.getLevel();
        long oldXp = data.getXp();
        data.setXp(Math.max(0L, xp));
        int newLevel = LevelFormula.levelForXp(data.getXp(), getMaxLevel());
        data.setLevel(newLevel);
        if (newLevel != oldLevel) {
            NeoForge.EVENT_BUS.post(new LevelUpLevelChangedEvent(player, oldLevel, newLevel));
            if (newLevel > oldLevel) {
                NeoForge.EVENT_BUS.post(new LevelUpOutputEvent(player, oldLevel, newLevel, oldXp, data.getXp(), LevelUpSources.UNKNOWN));
            }
        }
        sync(player);
    }

    @Override
    public void setLevel(ServerPlayer player, int level) {
        int clampedLevel = Math.max(0, Math.min(level, getMaxLevel()));
        long xpAtLevel = LevelFormula.totalXpForLevel(clampedLevel);
        setXp(player, xpAtLevel);
    }

    @Override
    public void setMaxLevelOverride(int maxLevel) {
        LevelUpProgressionOverrides.setMaxLevelOverride(maxLevel);
    }

    @Override
    public void clearMaxLevelOverride() {
        LevelUpProgressionOverrides.clearMaxLevelOverride();
    }

    @Override
    public void setLevelMultiplierOverride(double levelMultiplier) {
        LevelUpProgressionOverrides.setLevelMultiplierOverride(levelMultiplier);
    }

    @Override
    public void clearLevelMultiplierOverride() {
        LevelUpProgressionOverrides.clearLevelMultiplierOverride();
    }

    @Override
    public void sync(ServerPlayer player) {
        LevelUpNetwork.syncPlayer(player);
    }

    @Override
    public void spawnXpOrb(ServerLevel level, Vec3 position, int amount) {
        LevelUpXpOrbEntity.award(level, position, amount);
    }

    @Override
    public void spawnXpOrb(ServerLevel level, double x, double y, double z, int amount) {
        spawnXpOrb(level, new Vec3(x, y, z), amount);
    }
}
