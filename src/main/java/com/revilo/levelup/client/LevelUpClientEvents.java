package com.revilo.levelup.client;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.client.entity.LevelUpXpOrbRenderer;
import com.revilo.levelup.client.gui.InventoryLevelBarRenderer;
import com.revilo.levelup.client.gui.LevelBarRepositionController;
import com.revilo.levelup.client.gui.TopCenterLevelOverlay;
import com.revilo.levelup.config.LevelUpClientConfig;
import com.revilo.levelup.event.LevelUpHudDisplayEvent;
import com.revilo.levelup.event.LevelUpHudEnabledEvent;
import com.revilo.levelup.event.LevelUpHudPositionEvent;
import com.revilo.levelup.event.LevelUpHudStayOnScreenEvent;
import com.revilo.levelup.registry.LevelUpEntities;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ContainerScreenEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityAttachment;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

public final class LevelUpClientEvents {
    private LevelUpClientEvents() {}

    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // level orb renderer
        event.registerEntityRenderer(LevelUpEntities.LEVEL_ORB.get(), LevelUpXpOrbRenderer::new);
    }

    public static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        // custom xp layer
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
        public static void onClientTick(ClientTickEvent.Post event) {
            LevelBarRepositionController.onClientTick();
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
            // bottom hud replacement
            if (event.getName().equals(VanillaGuiLayers.EXPERIENCE_BAR)
                    || event.getName().equals(VanillaGuiLayers.JUMP_METER)) {
                event.setCanceled(true);
            }
        }

        @SubscribeEvent
        public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
            if (!LevelUpClientConfig.CLIENT.showPlayerLevelBelowNametag.get()) {
                return;
            }
            Player player = event.getEntity();
            Minecraft minecraft = Minecraft.getInstance();
            if (player.isInvisibleTo(minecraft.player)) {
                return;
            }
            Vec3 position = player.getAttachments().getNullable(EntityAttachment.NAME_TAG, 0, player.getViewYRot(event.getPartialTick()));
            if (position == null) {
                return;
            }

            Component label = Component.translatable("levelup.ui.level", ClientPlayerLevels.get(player.getUUID()));
            PoseStack poseStack = event.getPoseStack();
            poseStack.pushPose();
            poseStack.translate(position.x, position.y + 0.5D, position.z);
            poseStack.mulPose(minecraft.getEntityRenderDispatcher().cameraOrientation());
            poseStack.scale(0.025F, -0.025F, 0.025F);
            Matrix4f matrix = poseStack.last().pose();
            Font font = event.getRenderer().getFont();
            float x = -font.width(label) / 2.0F;
            boolean seeThrough = !player.isDiscrete();
            int background = (int) (minecraft.options.getBackgroundOpacity(0.25F) * 255.0F) << 24;
            font.drawInBatch(label, x, 0.0F, 553648127, false, matrix, event.getMultiBufferSource(),
                    seeThrough ? Font.DisplayMode.SEE_THROUGH : Font.DisplayMode.NORMAL, background, event.getPackedLight());
            if (seeThrough) {
                font.drawInBatch(label, x, 0.0F, -1, false, matrix, event.getMultiBufferSource(), Font.DisplayMode.NORMAL, 0, event.getPackedLight());
            }
            poseStack.popPose();
        }
    }
}
