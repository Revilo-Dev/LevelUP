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

        private Client(ModConfigSpec.Builder builder) {
            builder.push("hud");
            showTopCenterLevelOverlay = builder.comment("Show an animated LevelUP progress bar at the top center of the HUD when LevelUP XP increases.")
                    .define("showTopCenterLevelOverlay", true);
            builder.pop();
        }
    }
}
