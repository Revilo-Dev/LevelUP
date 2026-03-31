package com.revilo.levelup.api;

import com.revilo.levelup.LevelUpMod;
import net.minecraft.resources.ResourceLocation;

public final class LevelUpSources {
    public static final ResourceLocation UNKNOWN = id("unknown");
    public static final ResourceLocation MOB_KILL = id("mob_kill");
    public static final ResourceLocation GATEWAY_COMPLETE = id("gateway_complete");
    public static final ResourceLocation QUEST_COMPLETE = id("quest_complete");
    public static final ResourceLocation OBJECTIVE_COMPLETE = id("objective_complete");
    public static final ResourceLocation ORB_PICKUP = id("orb_pickup");
    public static final ResourceLocation ITEM_USE = id("item_use");
    public static final ResourceLocation COMMAND = id("command");

    private LevelUpSources() {}

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(LevelUpMod.MOD_ID, path);
    }
}
