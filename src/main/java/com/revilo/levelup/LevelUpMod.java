package com.revilo.levelup;

import com.revilo.levelup.config.LevelUpConfig;
import com.revilo.levelup.client.LevelUpClientEvents;
import com.revilo.levelup.command.LevelUpCommands;
import com.revilo.levelup.network.LevelUpNetwork;
import com.revilo.levelup.registry.LevelUpAttachments;
import com.revilo.levelup.registry.LevelUpEntities;
import com.revilo.levelup.registry.LevelUpItems;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;

@Mod(LevelUpMod.MOD_ID)
public final class LevelUpMod {
    public static final String MOD_ID = "levelup";

    public LevelUpMod(IEventBus modBus, ModContainer modContainer) {
        LevelUpAttachments.REGISTER.register(modBus);
        LevelUpEntities.REGISTER.register(modBus);
        LevelUpItems.REGISTER.register(modBus);
        modBus.addListener(LevelUpNetwork::onRegisterPayloadHandlers);
        NeoForge.EVENT_BUS.addListener(LevelUpCommands::onRegisterCommands);
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modBus.addListener(LevelUpClientEvents::onRegisterRenderers);
        }
        modContainer.registerConfig(ModConfig.Type.COMMON, LevelUpConfig.SPEC);
    }
}
