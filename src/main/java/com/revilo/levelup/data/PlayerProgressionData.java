package com.revilo.levelup.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public final class PlayerProgressionData {
    public static final Codec<PlayerProgressionData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("level").forGetter(PlayerProgressionData::getLevel),
            Codec.LONG.fieldOf("xp").forGetter(PlayerProgressionData::getXp),
            Codec.INT.optionalFieldOf("multiplier", 1).forGetter(PlayerProgressionData::getMultiplier),
            Codec.INT.optionalFieldOf("locked_level", -1).forGetter(PlayerProgressionData::getLockedLevel)
    ).apply(instance, PlayerProgressionData::new));

    private int level;
    private long xp;
    private int multiplier;
    private int lockedLevel;

    public PlayerProgressionData() {
        this(0, 0L, 1, -1);
    }

    public PlayerProgressionData(int level, long xp, int multiplier, int lockedLevel) {
        this.level = Math.max(0, level);
        this.xp = Math.max(0L, xp);
        this.multiplier = Math.max(0, multiplier);
        this.lockedLevel = Math.max(-1, lockedLevel);
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = Math.max(0, level);
    }

    public long getXp() {
        return xp;
    }

    public void setXp(long xp) {
        this.xp = Math.max(0L, xp);
    }

    public int getMultiplier() {
        return multiplier;
    }

    public void setMultiplier(int multiplier) {
        this.multiplier = Math.max(0, multiplier);
    }

    public int getLockedLevel() {
        return lockedLevel;
    }

    public void setLockedLevel(int lockedLevel) {
        this.lockedLevel = Math.max(-1, lockedLevel);
    }
}
