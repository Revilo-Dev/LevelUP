package com.revilo.levelup.client;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.client.entity.LevelUpXpOrbRenderer;
import com.revilo.levelup.client.gui.InventoryLevelBarRenderer;
import com.revilo.levelup.registry.LevelUpEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

public final class LevelUpClientEvents {
    private LevelUpClientEvents() {}

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(LevelUpEntities.LEVEL_ORB.get(), LevelUpXpOrbRenderer::new);
    }

    @EventBusSubscriber(modid = LevelUpMod.MOD_ID, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {}

        @SubscribeEvent
        public static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
            InventoryLevelBarRenderer.render(event);
        }
    }
}
