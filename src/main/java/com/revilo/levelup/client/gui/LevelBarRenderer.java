package com.revilo.levelup.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.revilo.levelup.config.LevelUpClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class LevelBarRenderer {
    private static final ResourceLocation BAR_BACKGROUND =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-background.png");
    private static final ResourceLocation BAR_PROGRESS =
            ResourceLocation.fromNamespaceAndPath("gui", "skill_bar/xp-bar-progress.png");
    public static final int BAR_WIDTH = 184;
    public static final int BAR_HEIGHT = 11;
    private static final int DEFAULT_COLOR = 0x53A4BC;

    private LevelBarRenderer() {}

    public static void render(GuiGraphics guiGraphics, int x, int y, float progress, Component label) {
        render(guiGraphics, x, y, progress, label, true, 0, true, 1.0F);
    }

    public static void render(GuiGraphics guiGraphics, int x, int y, float progress, Component label, boolean drawBackground) {
        render(guiGraphics, x, y, progress, label, drawBackground, 0, true, 1.0F);
    }

    public static void render(GuiGraphics guiGraphics, int x, int y, float progress, Component label, boolean drawBackground, int labelYOffset) {
        render(guiGraphics, x, y, progress, label, drawBackground, labelYOffset, true, 1.0F);
    }

    public static void render(
            GuiGraphics guiGraphics,
            int x,
            int y,
            float progress,
            Component label,
            boolean drawBackground,
            int labelYOffset,
            boolean drawLabel,
            float alpha
    ) {
        float clampedAlpha = Math.max(0.0F, Math.min(1.0F, alpha));
        int progressWidth = Math.max(0, Math.min(BAR_WIDTH, Math.round(progress * BAR_WIDTH)));

        if (drawBackground) {
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, clampedAlpha);
            guiGraphics.blit(BAR_BACKGROUND, x, y, 0, 0, BAR_WIDTH, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }

        if (progressWidth > 0) {
            tint(getConfiguredColor(), clampedAlpha);
            guiGraphics.blit(BAR_PROGRESS, x, y, 0, 0, progressWidth, BAR_HEIGHT, BAR_WIDTH, BAR_HEIGHT);
        }
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        if (!drawLabel || label == null || clampedAlpha <= 0.0F) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Font font = minecraft.font;
        int textWidth = font.width(label);
        int textX = x + (BAR_WIDTH - textWidth) / 2;
        int textY = y - 10 + labelYOffset;
        int outlineColor = withAlpha(darken(getConfiguredColor()), clampedAlpha);
        int textColor = withAlpha(getConfiguredColor(), clampedAlpha);

        guiGraphics.drawString(font, label, textX - 1, textY, outlineColor, false);
        guiGraphics.drawString(font, label, textX + 1, textY, outlineColor, false);
        guiGraphics.drawString(font, label, textX, textY - 1, outlineColor, false);
        guiGraphics.drawString(font, label, textX, textY + 1, outlineColor, false);
        guiGraphics.drawString(font, label, textX, textY, textColor, false);
    }

    public static int getConfiguredColor() {
        String raw = LevelUpClientConfig.CLIENT.levelHudColor.get();
        if (raw == null) {
            return DEFAULT_COLOR;
        }

        String normalized = raw.trim();
        if (normalized.startsWith("#")) {
            normalized = normalized.substring(1);
        }
        if (normalized.length() != 6) {
            return DEFAULT_COLOR;
        }

        try {
            return Integer.parseInt(normalized, 16);
        } catch (NumberFormatException ignored) {
            return DEFAULT_COLOR;
        }
    }

    private static void tint(int color, float alpha) {
        float red = ((color >> 16) & 0xFF) / 255.0F;
        float green = ((color >> 8) & 0xFF) / 255.0F;
        float blue = (color & 0xFF) / 255.0F;
        RenderSystem.setShaderColor(red, green, blue, alpha);
    }

    private static int darken(int color) {
        int red = (int) (((color >> 16) & 0xFF) * 0.35F);
        int green = (int) (((color >> 8) & 0xFF) * 0.35F);
        int blue = (int) ((color & 0xFF) * 0.35F);
        return (red << 16) | (green << 8) | blue;
    }

    private static int withAlpha(int color, float alpha) {
        int alphaByte = Math.max(0, Math.min(255, Math.round(alpha * 255.0F)));
        return (alphaByte << 24) | (color & 0xFFFFFF);
    }
}
