package com.revilo.levelup.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class LevelUpClientConfig {
    public static final String CLIENT_FILE_NAME = "levelup-client.toml";
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
        public final ModConfigSpec.BooleanValue showPlayerLevelBelowNametag;
        public final ModConfigSpec.IntValue hudLevelBarOffsetX;
        public final ModConfigSpec.IntValue hudLevelBarOffsetY;
        public final ModConfigSpec.IntValue inventoryLevelBarOffsetX;
        public final ModConfigSpec.IntValue inventoryLevelBarOffsetY;
        public final ModConfigSpec.BooleanValue openHudLevelBarRepositionGui;
        public final ModConfigSpec.BooleanValue openInventoryLevelBarRepositionGui;
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
            showPlayerLevelBelowNametag = builder.comment("Show a player's LevelUP level below their nametag.")
                    .define("showPlayerLevelBelowNametag", true);
            hudLevelBarOffsetX = builder.comment("Horizontal offset for the HUD LevelUP bar in pixels.")
                    .defineInRange("hudLevelBarOffsetX", 0, -2000, 2000);
            hudLevelBarOffsetY = builder.comment("Vertical offset for the HUD LevelUP bar in pixels.")
                    .defineInRange("hudLevelBarOffsetY", 0, -2000, 2000);
            inventoryLevelBarOffsetX = builder.comment("Horizontal offset for the inventory LevelUP bar in pixels.")
                    .defineInRange("inventoryLevelBarOffsetX", 0, -2000, 2000);
            inventoryLevelBarOffsetY = builder.comment("Vertical offset for the inventory LevelUP bar in pixels.")
                    .defineInRange("inventoryLevelBarOffsetY", 0, -2000, 2000);
            openHudLevelBarRepositionGui = builder.comment("Set true to open a GUI and reposition the HUD LevelUP bar. Resets to false after opening.")
                    .define("openHudLevelBarRepositionGui", false);
            openInventoryLevelBarRepositionGui = builder.comment("Set true to open a GUI and reposition the inventory LevelUP bar. Resets to false after opening.")
                    .define("openInventoryLevelBarRepositionGui", false);
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
