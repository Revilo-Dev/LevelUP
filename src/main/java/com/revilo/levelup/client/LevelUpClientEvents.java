package com.revilo.levelup.client;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.client.entity.LevelUpXpOrbRenderer;
import com.revilo.levelup.client.gui.InventoryLevelBarRenderer;
import com.revilo.levelup.client.gui.TopCenterLevelOverlay;
import com.revilo.levelup.event.LevelUpHudDisplayEvent;
import com.revilo.levelup.event.LevelUpHudEnabledEvent;
import com.revilo.levelup.event.LevelUpHudPositionEvent;
import com.revilo.levelup.event.LevelUpHudStayOnScreenEvent;
import com.revilo.levelup.registry.LevelUpEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class LevelUpClientEvents {
    private LevelUpClientEvents() {}

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(LevelUpEntities.LEVEL_ORB.get(), LevelUpXpOrbRenderer::new);
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, TopCenterLevelOverlay.layerId(), TopCenterLevelOverlay::render);
    }

    @EventBusSubscriber(modid = LevelUpMod.MOD_ID, value = Dist.CLIENT)
    public static final class GameEvents {
        private GameEvents() {}

        @SubscribeEvent
        public static void onContainerForeground(ContainerScreenEvent.Render.Foreground event) {
            InventoryLevelBarRenderer.render(event);
        }

        @SubscribeEvent
        public static void onHudDisplay(LevelUpHudDisplayEvent event) {
            TopCenterLevelOverlay.showCustom(event.getLabel(), event.getProgress(), event.getDurationMillis());
        }

        @SubscribeEvent
        public static void onHudEnabled(LevelUpHudEnabledEvent event) {
            TopCenterLevelOverlay.setEventHudEnabled(event.isEnabled());
        }

        @SubscribeEvent
        public static void onHudStayOnScreen(LevelUpHudStayOnScreenEvent event) {
            TopCenterLevelOverlay.setEventStayOnScreenLock(event.shouldStayOnScreen());
        }

        @SubscribeEvent
        public static void onHudPosition(LevelUpHudPositionEvent event) {
            TopCenterLevelOverlay.setEventHudPosition(event.getPosition());
        }

        @SubscribeEvent
        public static void onRenderGuiLayerPre(RenderGuiLayerEvent.Pre event) {
            if (!TopCenterLevelOverlay.shouldHideVanillaExperienceBar()) {
                return;
            }
            if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR)
                    || event.getName().equals(VanillaGuiLayers.JUMP_METER)) {
                event.setCanceled(true);
            }
        }
    }
}
