package com.revilo.levelup.config;

import com.revilo.levelup.registry.LevelUpTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.common.ModConfigSpec;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class LevelUpConfig {
    public static final ModConfigSpec SPEC;
    public static final Common COMMON;
    private static final Pattern SOURCES_SECTION_PATTERN = Pattern.compile("(?ms)(\\[sources]\\s*\\R)(.*?)(?=^\\[|\\z)");

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new Common(builder);
        SPEC = builder.build();
    }

    private LevelUpConfig() {}

    public static void migrateCommonConfig(ModConfig modConfig) {
        if (modConfig.getSpec() != SPEC) {
            return;
        }
        Path configPath = FMLPaths.CONFIGDIR.get().resolve(modConfig.getFileName());
        if (!Files.isRegularFile(configPath)) {
            return;
        }
        try {
            String configText = Files.readString(configPath);
            boolean hasNewKeys = containsSourceKey(configText, "mobs_drop_levels")
                    || containsSourceKey(configText, "only_tagged_mobs_drop_levels")
                    || containsSourceKey(configText, "mob_level_drop_base_value");
            boolean hasLegacyKeys = containsSourceKey(configText, "enable_mob_kill_xp")
                    || containsSourceKey(configText, "drop_levels_only_from_mobs_with_tag")
                    || containsSourceKey(configText, "mobKillXp");
            COMMON.setUseLegacyMobDropConfig(!hasNewKeys && hasLegacyKeys);
            if (hasNewKeys || !hasLegacyKeys) {
                return;
            }

            String migrated = configText;
            migrated = appendSourceKeyFromLegacy(migrated, "mobs_drop_levels", "enable_mob_kill_xp");
            migrated = appendSourceKeyFromLegacy(migrated, "only_tagged_mobs_drop_levels", "drop_levels_only_from_mobs_with_tag");
            migrated = appendSourceKeyFromLegacy(migrated, "mob_level_drop_base_value", "mobKillXp");
            migrated = removeSourceKey(migrated, "enable_mob_kill_xp");
            migrated = removeSourceKey(migrated, "drop_levels_only_from_mobs_with_tag");
            migrated = removeSourceKey(migrated, "mobKillXp");
            if (!migrated.equals(configText)) {
                Files.writeString(configPath, migrated);
            }
        } catch (IOException ignored) {
            COMMON.setUseLegacyMobDropConfig(false);
        }
    }

    private static boolean containsSourceKey(String configText, String key) {
        return Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=").matcher(getSourcesBody(configText)).find();
    }

    private static String appendSourceKeyFromLegacy(String configText, String newKey, String legacyKey) {
        if (containsSourceKey(configText, newKey)) {
            return configText;
        }
        String legacyValue = extractSourceValue(configText, legacyKey);
        if (legacyValue == null) {
            return configText;
        }
        Matcher matcher = SOURCES_SECTION_PATTERN.matcher(configText);
        if (!matcher.find()) {
            return configText;
        }
        String body = matcher.group(2);
        String lineBreak = body.contains("\r\n") ? "\r\n" : "\n";
        StringBuilder updatedBody = new StringBuilder(body);
        if (!body.isEmpty() && !body.endsWith("\n") && !body.endsWith("\r")) {
            updatedBody.append(lineBreak);
        }
        updatedBody.append(newKey).append(" = ").append(legacyValue).append(lineBreak);
        return configText.substring(0, matcher.start(2)) + updatedBody + configText.substring(matcher.end(2));
    }

    private static String removeSourceKey(String configText, String key) {
        Matcher matcher = SOURCES_SECTION_PATTERN.matcher(configText);
        if (!matcher.find()) {
            return configText;
        }
        String body = matcher.group(2);
        String updatedBody = body.replaceAll("(?m)^\\s*" + Pattern.quote(key) + "\\s*=.*(?:\\R)?", "");
        return configText.substring(0, matcher.start(2)) + updatedBody + configText.substring(matcher.end(2));
    }

    private static String extractSourceValue(String configText, String key) {
        Matcher matcher = Pattern.compile("(?m)^\\s*" + Pattern.quote(key) + "\\s*=\\s*(.+?)\\s*$").matcher(getSourcesBody(configText));
        return matcher.find() ? matcher.group(1) : null;
    }

    private static String getSourcesBody(String configText) {
        Matcher matcher = SOURCES_SECTION_PATTERN.matcher(configText);
        return matcher.find() ? matcher.group(2) : "";
    }

    public static final class Common {
        public final ModConfigSpec.IntValue baseXpPerLevel;
        public final ModConfigSpec.IntValue linearXpPerLevel;
        public final ModConfigSpec.DoubleValue exponent;
        public final ModConfigSpec.DoubleValue levelMultiplier;
        public final ModConfigSpec.IntValue maxLevel;
        public final ModConfigSpec.BooleanValue mobsDropLevels;
        public final ModConfigSpec.IntValue mobLevelDropBaseValue;
        public final ModConfigSpec.BooleanValue onlyTaggedMobsDropLevels;
        private final ModConfigSpec.BooleanValue legacyEnableMobKillXp;
        private final ModConfigSpec.IntValue legacyMobKillXp;
        private final ModConfigSpec.BooleanValue legacyDropLevelsOnlyFromMobsWithTag;
        private boolean useLegacyMobDropConfig;

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
            mobsDropLevels = builder.comment(
                            "When true, hostile enemy mobs can drop LevelUP level orbs on player kills.",
                            "When false, generic mob level drops are disabled."
                    )
                    .define("mobs_drop_levels", true);
            mobLevelDropBaseValue = builder.comment(
                            "Base LevelUP orb value used for eligible mob drops.",
                            "Actual drop amount is `base_value * max_health / 20`, rounded down with a minimum of 1."
                    )
                    .defineInRange("mob_level_drop_base_value", 8, 0, Integer.MAX_VALUE);
            onlyTaggedMobsDropLevels = builder.comment(
                            "When true, only entity types in the `levelup:drops_levels` entity type tag (legacy `levelup:drop_levels` also accepted) drop LevelUP level orbs.",
                            "This overrides the generic hostile mob mode.",
                            "If both `mobs_drop_levels` and `only_tagged_mobs_drop_levels` are false, mobs never drop levels."
                    )
                    .define("only_tagged_mobs_drop_levels", false);
            legacyEnableMobKillXp = builder.comment("Deprecated legacy key. Migrated to `mobs_drop_levels`.")
                    .define("enable_mob_kill_xp", true);
            legacyMobKillXp = builder.comment("Deprecated legacy key. Migrated to `mob_level_drop_base_value`.")
                    .defineInRange("mobKillXp", 8, 0, Integer.MAX_VALUE);
            legacyDropLevelsOnlyFromMobsWithTag = builder.comment("Deprecated legacy key. Migrated to `only_tagged_mobs_drop_levels`.")
                    .define("drop_levels_only_from_mobs_with_tag", false);
            builder.pop();
        }

        public boolean shouldDropLevels(LivingEntity entity) {
            if (isOnlyTaggedMobsDropLevelsEnabled()) {
                return entity.getType().is(LevelUpTags.DROPS_LEVELS)
                        || entity.getType().is(LevelUpTags.LEGACY_DROP_LEVELS);
            }
            if (!areMobLevelDropsEnabled()) {
                return false;
            }
            return entity instanceof Enemy;
        }

        public int getMobLevelDropAmount(LivingEntity entity) {
            int baseValue = getMobLevelDropBaseValue();
            if (baseValue <= 0) {
                return 0;
            }
            double scaledValue = baseValue * Math.max(1.0D, entity.getMaxHealth() / 20.0D);
            return Math.max(1, Mth.floor(scaledValue));
        }

        private boolean areMobLevelDropsEnabled() {
            return useLegacyMobDropConfig ? legacyEnableMobKillXp.get() : mobsDropLevels.get();
        }

        private boolean isOnlyTaggedMobsDropLevelsEnabled() {
            return useLegacyMobDropConfig ? legacyDropLevelsOnlyFromMobsWithTag.get() : onlyTaggedMobsDropLevels.get();
        }

        private int getMobLevelDropBaseValue() {
            return useLegacyMobDropConfig ? legacyMobKillXp.get() : mobLevelDropBaseValue.get();
        }

        private void setUseLegacyMobDropConfig(boolean useLegacyMobDropConfig) {
            this.useLegacyMobDropConfig = useLegacyMobDropConfig;
        }
    }
}
