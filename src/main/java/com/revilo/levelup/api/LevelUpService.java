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
    private volatile boolean paused;

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
    public int getXpMultiplier(Player player) {
        return player.getData(LevelUpAttachments.PLAYER_PROGRESSION).getMultiplier();
    }

    @Override
    public long getXpIntoCurrentLevel(Player player) {
        int level = getLevel(player);
        long total = getXp(player);
        long floor = LevelFormula.totalXpForLevel(level);
        return Math.max(0L, total - floor);
    }

    @Override
    public long getXpNeededForNextLevel(Player player) {
        int level = getLevel(player);
        if (level >= getMaxLevel()) {
            return 0L;
        }
        long progress = getXpIntoCurrentLevel(player);
        long cost = getXpForNextLevel(level);
        return Math.max(0L, cost - progress);
    }

    @Override
    public float getProgressToNextLevel(Player player) {
        int level = getLevel(player);
        if (level >= getMaxLevel()) {
            return 1.0F;
        }
        long intoLevel = getXpIntoCurrentLevel(player);
        long cost = Math.max(1L, getXpForNextLevel(level));
        return Math.min(1.0F, (float) intoLevel / (float) cost);
    }

    @Override
    public long getXpForNextLevel(int currentLevel) {
        return LevelFormula.xpForNextLevel(currentLevel);
    }

    @Override
    public long getTotalXpForLevel(int level) {
        return LevelFormula.totalXpForLevel(level);
    }

    @Override
    public int levelForTotalXp(long totalXp) {
        return LevelFormula.levelForXp(totalXp, getMaxLevel());
    }

    @Override
    public boolean meetsLevelRequirement(Player player, int requiredLevel) {
        return getLevel(player) >= Math.max(0, requiredLevel);
    }

    @Override
    public long addXp(ServerPlayer player, long amount, ResourceLocation source) {
        if (paused) {
            return 0L;
        }

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
        gain = saturatingMultiply(gain, getXpMultiplier(player));
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

        LevelUpNetwork.syncPlayer(player, true);
        return gain;
    }

    @Override
    public long addLevels(ServerPlayer player, int levels, ResourceLocation source) {
        int safeLevels = Math.max(0, levels);
        if (safeLevels == 0) {
            return 0L;
        }

        int currentLevel = getLevel(player);
        int targetLevel = Math.min(getMaxLevel(), currentLevel + safeLevels);
        long targetXp = getTotalXpForLevel(targetLevel);
        long currentXp = getXp(player);
        long xpToAdd = Math.max(0L, targetXp - currentXp);
        return addXp(player, xpToAdd, source);
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
    public void setXpMultiplier(ServerPlayer player, int multiplier) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        data.setMultiplier(multiplier);
        sync(player);
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
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

    @Override
    public void spawnXpOrbForLevel(ServerLevel serverLevel, Vec3 position, int targetLevel) {
        long totalXp = getTotalXpForLevel(targetLevel);
        int orbValue = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, totalXp));
        if (orbValue > 0) {
            spawnXpOrb(serverLevel, position, orbValue);
        }
    }

    @Override
    public void spawnXpOrbForLevel(ServerLevel serverLevel, double x, double y, double z, int targetLevel) {
        spawnXpOrbForLevel(serverLevel, new Vec3(x, y, z), targetLevel);
    }

    private static long saturatingMultiply(long value, int multiplier) {
        if (value <= 0L || multiplier <= 0) {
            return 0L;
        }
        if (value > Long.MAX_VALUE / multiplier) {
            return Long.MAX_VALUE;
        }
        return value * multiplier;
    }
}
