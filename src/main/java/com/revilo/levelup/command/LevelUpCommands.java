package com.revilo.levelup.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.api.LevelUpSources;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

public final class LevelUpCommands {
    private LevelUpCommands() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("levels")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.literal("xp")
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                                .then(Commands.argument("id", EntityArgument.player())
                                                        .executes(ctx -> addXp(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "id"),
                                                                LongArgumentType.getLong(ctx, "amount")
                                                        )))))
                                .then(Commands.literal("level")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .then(Commands.argument("id", EntityArgument.player())
                                                        .executes(ctx -> addLevel(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "id"),
                                                                IntegerArgumentType.getInteger(ctx, "amount")
                                                        )))))
                                .then(Commands.literal("multiplier")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer())
                                                .then(Commands.argument("id", EntityArgument.player())
                                                        .executes(ctx -> addMultiplier(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "id"),
                                                                IntegerArgumentType.getInteger(ctx, "amount")
                                                        ))))))
                        .then(Commands.literal("set")
                                .then(Commands.literal("xp")
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                                .then(Commands.argument("id", EntityArgument.player())
                                                        .executes(ctx -> setXp(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "id"),
                                                                LongArgumentType.getLong(ctx, "amount")
                                                        )))))
                                .then(Commands.literal("level")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("id", EntityArgument.player())
                                                        .executes(ctx -> setLevel(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "id"),
                                                                IntegerArgumentType.getInteger(ctx, "amount")
                                                        )))))
                                .then(Commands.literal("multiplier")
                                        .then(Commands.argument("amount", IntegerArgumentType.integer(0))
                                                .then(Commands.argument("id", EntityArgument.player())
                                                        .executes(ctx -> setMultiplier(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayer(ctx, "id"),
                                                                IntegerArgumentType.getInteger(ctx, "amount")
                                                        ))))))
                        .then(Commands.literal("reset")
                                .then(Commands.argument("id", EntityArgument.player())
                                        .executes(ctx -> reset(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "id")
                                        ))))
                        .then(Commands.literal("spawnorbs")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> spawnOrbs(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount")
                                        ))))
                        .then(Commands.literal("pause")
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes(ctx -> setPaused(
                                                ctx.getSource(),
                                                BoolArgumentType.getBool(ctx, "value")
                                        ))))
                        .then(Commands.literal("query")
                                .then(Commands.argument("id", EntityArgument.player())
                                        .executes(ctx -> query(
                                                ctx.getSource(),
                                                EntityArgument.getPlayer(ctx, "id")
                                        ))))
        );
    }

    private static int addXp(CommandSourceStack source, ServerPlayer player, long amount) {
        long applied = LevelUpApi.addXp(player, amount, LevelUpSources.COMMAND);
        source.sendSuccess(() -> Component.literal("Added " + applied + " XP to " + player.getGameProfile().getName() + "."), true);
        return applied > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) applied;
    }

    private static int addLevel(CommandSourceStack source, ServerPlayer player, int amount) {
        int nextLevel = Math.max(0, LevelUpApi.getLevel(player) + amount);
        LevelUpApi.setLevel(player, nextLevel);
        source.sendSuccess(() -> Component.literal(player.getGameProfile().getName() + " is now level " + LevelUpApi.getLevel(player) + "."), true);
        return 1;
    }

    private static int addMultiplier(CommandSourceStack source, ServerPlayer player, int amount) {
        int nextMultiplier = Math.max(0, LevelUpApi.getXpMultiplier(player) + amount);
        LevelUpApi.setXpMultiplier(player, nextMultiplier);
        source.sendSuccess(() -> Component.literal("Set " + player.getGameProfile().getName() + " multiplier to " + nextMultiplier + "."), true);
        return nextMultiplier;
    }

    private static int setXp(CommandSourceStack source, ServerPlayer player, long amount) {
        LevelUpApi.setXp(player, amount);
        source.sendSuccess(() -> Component.literal("Set " + player.getGameProfile().getName() + " XP to " + amount + "."), true);
        return 1;
    }

    private static int setLevel(CommandSourceStack source, ServerPlayer player, int amount) {
        LevelUpApi.setLevel(player, amount);
        source.sendSuccess(() -> Component.literal("Set " + player.getGameProfile().getName() + " level to " + LevelUpApi.getLevel(player) + "."), true);
        return 1;
    }

    private static int setMultiplier(CommandSourceStack source, ServerPlayer player, int amount) {
        LevelUpApi.setXpMultiplier(player, amount);
        source.sendSuccess(() -> Component.literal("Set " + player.getGameProfile().getName() + " multiplier to " + amount + "."), true);
        return amount;
    }

    private static int reset(CommandSourceStack source, ServerPlayer player) {
        LevelUpApi.setXpMultiplier(player, 1);
        LevelUpApi.setXp(player, 0L);
        source.sendSuccess(() -> Component.literal("Reset LevelUP data for " + player.getGameProfile().getName() + "."), true);
        return 1;
    }

    private static int spawnOrbs(CommandSourceStack source, int amount) {
        LevelUpApi.spawnLevelUpXpOrb(source.getLevel(), source.getPosition(), amount);
        source.sendSuccess(() -> Component.literal("Spawned LevelUP orbs worth " + amount + " XP."), true);
        return 1;
    }

    private static int setPaused(CommandSourceStack source, boolean value) {
        LevelUpApi.setPaused(value);
        source.sendSuccess(() -> Component.literal("LevelUP XP gains are now " + (value ? "paused" : "active") + "."), true);
        return value ? 1 : 0;
    }

    private static int query(CommandSourceStack source, ServerPlayer player) {
        source.sendSuccess(() -> Component.literal(
                player.getGameProfile().getName()
                        + " -> level=" + LevelUpApi.getLevel(player)
                        + ", xp=" + LevelUpApi.getXp(player)
                        + ", multiplier=" + LevelUpApi.getXpMultiplier(player)
                        + ", paused=" + LevelUpApi.isPaused()
        ), false);
        return 1;
    }
}
