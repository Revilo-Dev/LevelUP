package com.revilo.levelup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LevelUpConfig {
    public static final ModConfigSpec SPEC;
    public static final Common COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        SPEC = builder.build();
    }

    private LevelUpConfig() {}

    public static final class Common {
        public final ModConfigSpec.IntValue baseXpPerLevel;
        public final ModConfigSpec.IntValue linearXpPerLevel;
        public final ModConfigSpec.DoubleValue exponent;
        public final ModConfigSpec.DoubleValue levelMultiplier;
        public final ModConfigSpec.IntValue maxLevel;
        public final ModConfigSpec.BooleanValue enableMobKillXp;
        public final ModConfigSpec.IntValue mobKillXp;
        public final ModConfigSpec.BooleanValue dropLevelsOnlyFromMobsWithTag;

        private Common(ModConfigSpec.Builder builder) {
            builder.push("progression");
            baseXpPerLevel = builder.comment("Base XP needed at level 1.")
                    .defineInRange("baseXpPerLevel", 100, 1, Integer.MAX_VALUE);
            linearXpPerLevel = builder.comment("Linear XP scaling term applied each level.")
                    .defineInRange("linearXpPerLevel", 20, 0, Integer.MAX_VALUE);
            exponent = builder.comment("Exponent applied to level growth (>= 1.0 recommended).")
                    .defineInRange("exponent", 1.35D, 1.0D, 5.0D);
            levelMultiplier = builder.comment("Global multiplier applied to level XP costs. Default 0.75 lowers scaling by 25%.")
                    .defineInRange("levelMultiplier", 0.75D, 0.01D, 100.0D);
            maxLevel = builder.comment("Hard level cap for this framework.")
                    .defineInRange("maxLevel", 500, 1, Integer.MAX_VALUE);
            builder.pop();

            builder.push("sources");
            enableMobKillXp = builder.comment("When false, mob kills will never drop LevelUP XP.")
                    .define("enable_mob_kill_xp", true);
            mobKillXp = builder.comment("LevelUP XP dropped when an eligible mob is killed by a player. Set to 0 to disable all mob LevelUP XP drops.")
                    .defineInRange("mobKillXp", 8, 0, Integer.MAX_VALUE);
            dropLevelsOnlyFromMobsWithTag = builder.comment(
                            "When true, only entity types in the `levelup:drops_levels` entity type tag (legacy `levelup:drop_levels` also accepted) drop LevelUP XP.",
                            "When false, all hostile mobs drop LevelUP XP."
                    )
                    .define("drop_levels_only_from_mobs_with_tag", false);
            builder.pop();
        }
    }
}
