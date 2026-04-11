package com.revilo.levelup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LevelUpClientConfig {
    public static final ModConfigSpec SPEC;
    public static final Client CLIENT;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        CLIENT = new Client(builder);
        SPEC = builder.build();
    }

    private LevelUpClientConfig() {}

    public static final class Client {
        public final ModConfigSpec.BooleanValue showTopCenterLevelOverlay;
        public final ModConfigSpec.BooleanValue showTemporaryLevelOverlay;
        public final ModConfigSpec.BooleanValue showInventoryLevelBar;
        public final ModConfigSpec.ConfigValue<String> levelHudPosition;
        public final ModConfigSpec.BooleanValue levelHudStayOnScreen;
        public final ModConfigSpec.ConfigValue<String> levelHudColor;

        private Client(ModConfigSpec.Builder builder) {
            builder.push("hud");
            showTopCenterLevelOverlay = builder.comment("Show an animated LevelUP progress bar at the top center of the HUD when LevelUP XP increases.")
                    .define("showTopCenterLevelOverlay", true);
            showTemporaryLevelOverlay = builder.comment("Show temporary LevelUP HUD popups from XP updates/custom HUD events.")
                    .define("showTemporaryLevelOverlay", true);
            levelHudPosition = builder.comment("Where to draw the LevelUP HUD: top or bottom.")
                    .define("levelHudPosition", "top", value -> value instanceof String string && isValidPosition(string));
            levelHudStayOnScreen = builder.comment("Keep the LevelUP HUD visible even when no recent XP gain is animating.")
                    .define("levelHudStayOnScreen", false);
            levelHudColor = builder.comment("Hex color used for the LevelUP progress fill and level text.")
                    .define("levelHudColor", "#53a4bc", value -> value instanceof String string && isValidHexColor(string));
            showInventoryLevelBar = builder.comment("Show the LevelUP progress bar inside the inventory screen.")
                    .define("showInventoryLevelBar", true);
            builder.pop();
        }

        private static boolean isValidPosition(String value) {
            return "top".equalsIgnoreCase(value) || "bottom".equalsIgnoreCase(value);
        }

        private static boolean isValidHexColor(String value) {
            String normalized = value.startsWith("#") ? value.substring(1) : value;
            return normalized.matches("[0-9a-fA-F]{6}");
        }
    }
}
