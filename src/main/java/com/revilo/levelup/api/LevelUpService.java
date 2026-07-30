package com.revilo.levelup.api;

import com.revilo.levelup.data.LevelFormula;
import com.revilo.levelup.data.PlayerProgressionData;
import com.revilo.levelup.entity.LevelUpXpOrbEntity;
import com.revilo.levelup.api.event.PlayerExperienceChangeEvent;
import com.revilo.levelup.api.event.PlayerLevelChangeEvent;
import com.revilo.levelup.api.event.PlayerLevelMilestoneEvent;
import com.revilo.levelup.event.LevelUpLevelChangedEvent;
import com.revilo.levelup.event.LevelUpLevelLockChangedEvent;
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

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.concurrent.ConcurrentHashMap;

public final class LevelUpService implements ILevelUpService {
    private static final int DEFAULT_MILESTONE_INTERVAL = 5;
    private static final ResourceLocation DEFAULT_MILESTONE_ID = LevelUpSources.id("default_5_level_milestone");
    public static final LevelUpService INSTANCE = new LevelUpService();
    private static final LevelUpCapabilities CAPABILITIES = new LevelUpCapabilities(
            true,
            true,
            true,
            true,
            true,
            true,
            true,
            true
    );
    private static final LevelCurve ACTIVE_LEVEL_CURVE = new LevelCurve() {
        @Override
        public long experienceRequiredForLevel(int level) {
            return LevelFormula.xpForNextLevel(level);
        }

        @Override
        public long cumulativeExperienceForLevel(int level) {
            return LevelFormula.totalXpForLevel(level);
        }

        @Override
        public int levelFromTotalExperience(long experience) {
            return LevelFormula.levelForXp(experience, LevelUpProgressionOverrides.getMaxLevel());
        }
    };
    private final Map<ResourceLocation, Integer> milestones = new ConcurrentHashMap<>();
    private volatile boolean paused;

    private LevelUpService() {
        milestones.put(DEFAULT_MILESTONE_ID, DEFAULT_MILESTONE_INTERVAL);
    }

    @Override
    public String getApiVersion() {
        return LevelUpApi.API_VERSION;
    }

    @Override
    public LevelUpCapabilities getCapabilities() {
        return CAPABILITIES;
    }

    @Override
    public int getMaxLevel() {
        return LevelUpProgressionOverrides.getMaxLevel();
    }

    @Override
    public OptionalInt getMaximumLevel() {
        return OptionalInt.of(getMaxLevel());
    }

    @Override
    public double getLevelMultiplier() {
        return LevelUpProgressionOverrides.getLevelMultiplier();
    }

    @Override
    public Optional<LevelUpPlayerView> getPlayerData(ServerPlayer player) {
        return Optional.of(new PlayerView(player));
    }

    @Override
    public LevelCurve getActiveLevelCurve() {
        return ACTIVE_LEVEL_CURVE;
    }

    @Override
    public int getLevel(Player player) {
        return player.getData(LevelUpAttachments.PLAYER_PROGRESSION).getLevel();
    }

    @Override
    public int getHighestLevelReached(Player player) {
        return player.getData(LevelUpAttachments.PLAYER_PROGRESSION).getHighestLevelReached();
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
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        int level = data.getLevel();
        int maxLevel = getEffectiveMaxLevel(data);
        if (level >= maxLevel) {
            return 0L;
        }
        long progress = Math.max(0L, data.getXp() - LevelFormula.totalXpForLevel(level));
        long cost = LevelFormula.xpForNextLevel(level);
        return Math.max(0L, cost - progress);
    }

    @Override
    public float getProgressToNextLevel(Player player) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        int level = data.getLevel();
        int maxLevel = getEffectiveMaxLevel(data);
        if (level >= maxLevel) {
            return 1.0F;
        }
        long intoLevel = Math.max(0L, data.getXp() - LevelFormula.totalXpForLevel(level));
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
    public boolean isAtMaximumLevel(ServerPlayer player) {
        return getLevel(player) >= getEffectiveMaxLevel(player);
    }

    @Override
    public void registerMilestone(ResourceLocation id, int interval) {
        if (id == null || interval <= 0) {
            throw new IllegalArgumentException("Milestone id must be non-null and interval must be positive.");
        }
        milestones.put(id, interval);
    }

    @Override
    public LevelChangeResult addExperience(ServerPlayer player, long amount, ResourceLocation source) {
        if (paused) {
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            return LevelChangeResult.unchanged(data.getLevel(), data.getXp());
        }

        long safeAmount = Math.max(0L, amount);
        if (safeAmount == 0L) {
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            return LevelChangeResult.unchanged(data.getLevel(), data.getXp());
        }

        LevelUpXpGainedEvent event = new LevelUpXpGainedEvent(player, source, safeAmount);
        if (NeoForge.EVENT_BUS.post(event).isCanceled()) {
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            return LevelChangeResult.unchanged(data.getLevel(), data.getXp());
        }
        long gain = Math.max(0L, event.getAmount());
        if (gain == 0L) {
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            return LevelChangeResult.unchanged(data.getLevel(), data.getXp());
        }
        gain = saturatingMultiply(gain, getXpMultiplier(player));
        if (gain == 0L) {
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            return LevelChangeResult.unchanged(data.getLevel(), data.getXp());
        }

        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        long newXp = LevelFormula.saturatingAdd(data.getXp(), gain);
        return applyExperience(player, newXp, source, reasonForExperienceGain(source), true);
    }

    @Override
    public long addXp(ServerPlayer player, long amount, ResourceLocation source) {
        LevelChangeResult result = addExperience(player, amount, source);
        return Math.max(0L, result.newExperience() - result.previousExperience());
    }

    @Override
    public LevelChangeResult addLevels(ServerPlayer player, int levels, ResourceLocation source) {
        int safeLevels = Math.max(0, levels);
        if (safeLevels == 0) {
            PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
            return LevelChangeResult.unchanged(data.getLevel(), data.getXp());
        }

        int currentLevel = getLevel(player);
        int targetLevel = Math.min(getEffectiveMaxLevel(player), currentLevel + safeLevels);
        return setLevel(player, targetLevel, source);
    }

    @Override
    public LevelChangeResult setExperience(ServerPlayer player, long xp, ResourceLocation source) {
        return applyExperience(player, xp, source, reasonForSource(source), false);
    }

    @Override
    public LevelChangeResult setLevel(ServerPlayer player, int level, ResourceLocation source) {
        int clampedLevel = Math.max(0, Math.min(level, getEffectiveMaxLevel(player)));
        long xpAtLevel = LevelFormula.totalXpForLevel(clampedLevel);
        return applyExperience(player, xpAtLevel, source, reasonForSource(source), false);
    }

    @Override
    public LevelChangeResult reset(ServerPlayer player, ResourceLocation source) {
        setXpMultiplier(player, 1);
        LevelChangeResult result = applyExperience(player, 0L, source, LevelChangeReason.RESET, false);
        player.getData(LevelUpAttachments.PLAYER_PROGRESSION).resetHighestLevelReached();
        sync(player);
        return result;
    }

    @Override
    public void setXp(ServerPlayer player, long xp) {
        setExperience(player, xp, LevelUpSources.UNKNOWN);
    }

    @Override
    public void setLevel(ServerPlayer player, int level) {
        setLevel(player, level, LevelUpSources.UNKNOWN);
    }

    private LevelChangeResult applyExperience(ServerPlayer player, long xp, ResourceLocation source, LevelChangeReason reason, boolean showOverlay) {
        ResourceLocation safeSource = source == null ? LevelUpSources.UNKNOWN : source;
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        int oldLevel = data.getLevel();
        long oldXp = data.getXp();
        int maxLevel = getEffectiveMaxLevel(data);
        long clampedXp = Math.min(Math.max(0L, xp), LevelFormula.totalXpForLevel(maxLevel));
        data.setXp(clampedXp);
        int newLevel = LevelFormula.levelForXp(data.getXp(), maxLevel);
        data.setLevel(newLevel);
        LevelChangeResult result = new LevelChangeResult(oldLevel, newLevel, oldXp, data.getXp(), crossedLevels(oldLevel, newLevel));
        if (oldXp != data.getXp()) {
            NeoForge.EVENT_BUS.post(new PlayerExperienceChangeEvent(player, oldXp, data.getXp(), reason, safeSource));
        }
        if (newLevel != oldLevel) {
            NeoForge.EVENT_BUS.post(new LevelUpLevelChangedEvent(player, oldLevel, newLevel));
            NeoForge.EVENT_BUS.post(new PlayerLevelChangeEvent(player, oldLevel, newLevel, reason, safeSource));
            if (newLevel > oldLevel) {
                NeoForge.EVENT_BUS.post(new LevelUpOutputEvent(player, oldLevel, newLevel, oldXp, data.getXp(), safeSource));
                for (int level : result.crossedLevels()) {
                    NeoForge.EVENT_BUS.post(new LevelUpLevelChangedEvent.LevelUp(player, level - 1, level));
                }
                for (Map.Entry<ResourceLocation, Integer> entry : milestones.entrySet()) {
                    for (int milestone : result.crossedMilestones(entry.getValue())) {
                        NeoForge.EVENT_BUS.post(new PlayerLevelMilestoneEvent(player, entry.getKey(), milestone, oldLevel, newLevel, reason, safeSource));
                    }
                }
            }
        }
        LevelUpNetwork.syncPlayer(player, showOverlay);
        return result;
    }

    @Override
    public void setXpMultiplier(ServerPlayer player, int multiplier) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        data.setMultiplier(multiplier);
        sync(player);
    }

    @Override
    public boolean isLevelLocked(Player player) {
        return getLockedLevel(player) >= 0;
    }

    @Override
    public int getLockedLevel(Player player) {
        return player.getData(LevelUpAttachments.PLAYER_PROGRESSION).getLockedLevel();
    }

    @Override
    public void lockLevel(ServerPlayer player, int level) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        int oldLockedLevel = data.getLockedLevel();
        int newLockedLevel = Math.max(0, Math.min(level, getMaxLevel()));
        if (oldLockedLevel == newLockedLevel) {
            return;
        }

        data.setLockedLevel(newLockedLevel);
        NeoForge.EVENT_BUS.post(new LevelUpLevelLockChangedEvent(player, oldLockedLevel, newLockedLevel));

        int maxLevel = getEffectiveMaxLevel(data);
        long maxXp = LevelFormula.totalXpForLevel(maxLevel);
        if (data.getXp() > maxXp || data.getLevel() > maxLevel) {
            applyExperience(player, maxXp, LevelUpSources.UNKNOWN, LevelChangeReason.API, false);
            return;
        }

        sync(player);
    }

    @Override
    public void unlockLevel(ServerPlayer player) {
        PlayerProgressionData data = player.getData(LevelUpAttachments.PLAYER_PROGRESSION);
        int oldLockedLevel = data.getLockedLevel();
        if (oldLockedLevel < 0) {
            return;
        }

        data.setLockedLevel(-1);
        NeoForge.EVENT_BUS.post(new LevelUpLevelLockChangedEvent(player, oldLockedLevel, -1));
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

    private int getEffectiveMaxLevel(Player player) {
        return getEffectiveMaxLevel(player.getData(LevelUpAttachments.PLAYER_PROGRESSION));
    }

    private int getEffectiveMaxLevel(PlayerProgressionData data) {
        int maxLevel = getMaxLevel();
        int lockedLevel = data.getLockedLevel();
        if (lockedLevel >= 0) {
            maxLevel = Math.min(maxLevel, lockedLevel);
        }
        return Math.max(0, maxLevel);
    }

    private static ArrayList<Integer> crossedLevels(int oldLevel, int newLevel) {
        if (newLevel <= oldLevel) {
            return new ArrayList<>();
        }
        ArrayList<Integer> levels = new ArrayList<>(newLevel - oldLevel);
        for (int level = oldLevel + 1; level <= newLevel; level++) {
            levels.add(level);
        }
        return levels;
    }

    private static LevelChangeReason reasonForSource(ResourceLocation source) {
        if (LevelUpSources.COMMAND.equals(source)) {
            return LevelChangeReason.COMMAND;
        }
        return LevelChangeReason.API;
    }

    private static LevelChangeReason reasonForExperienceGain(ResourceLocation source) {
        if (LevelUpSources.COMMAND.equals(source)) {
            return LevelChangeReason.COMMAND;
        }
        return LevelChangeReason.EXPERIENCE_GAIN;
    }

    private final class PlayerView implements LevelUpPlayerView {
        private final ServerPlayer player;

        private PlayerView(ServerPlayer player) {
            this.player = player;
        }

        @Override
        public int getLevel() {
            return LevelUpService.this.getLevel(player);
        }

        @Override
        public int getHighestLevelReached() {
            return LevelUpService.this.getHighestLevelReached(player);
        }

        @Override
        public long getCurrentExperience() {
            return LevelUpService.this.getXpIntoCurrentLevel(player);
        }

        @Override
        public long getExperienceForCurrentLevel() {
            return LevelUpService.this.getTotalXpForLevel(getLevel());
        }

        @Override
        public long getExperienceForNextLevel() {
            return LevelUpService.this.getXpNeededForNextLevel(player);
        }

        @Override
        public long getTotalExperience() {
            return LevelUpService.this.getXp(player);
        }

        @Override
        public float getLevelProgress() {
            return LevelUpService.this.getProgressToNextLevel(player);
        }
    }
}
