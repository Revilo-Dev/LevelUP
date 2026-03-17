package com.revilo.levelup.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.LongArgumentType;
import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.api.LevelUpSources;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.Collection;

public final class LevelUpCommands {
    private LevelUpCommands() {}

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    private static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("levelup")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("addxp")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                                .executes(ctx -> addXp(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        LongArgumentType.getLong(ctx, "amount"),
                                                        LevelUpSources.COMMAND
                                                ))
                                                .then(Commands.argument("source", ResourceLocationArgument.id())
                                                        .executes(ctx -> addXp(
                                                                ctx.getSource(),
                                                                EntityArgument.getPlayers(ctx, "targets"),
                                                                LongArgumentType.getLong(ctx, "amount"),
                                                                ResourceLocationArgument.getId(ctx, "source")
                                                        )))
                                        )))
                        .then(Commands.literal("setxp")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("amount", LongArgumentType.longArg(0))
                                                .executes(ctx -> setXp(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        LongArgumentType.getLong(ctx, "amount")
                                                )))))
                        .then(Commands.literal("setlevel")
                                .then(Commands.argument("targets", EntityArgument.players())
                                        .then(Commands.argument("level", IntegerArgumentType.integer(0))
                                                .executes(ctx -> setLevel(
                                                        ctx.getSource(),
                                                        EntityArgument.getPlayers(ctx, "targets"),
                                                        IntegerArgumentType.getInteger(ctx, "level")
                                                )))))
                        .then(Commands.literal("spawnorb")
                                .then(Commands.argument("amount", IntegerArgumentType.integer(1))
                                        .executes(ctx -> spawnOrb(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "amount"),
                                                ctx.getSource().getPosition()
                                        ))
                                        .then(Commands.argument("pos", Vec3Argument.vec3())
                                                .executes(ctx -> spawnOrb(
                                                        ctx.getSource(),
                                                        IntegerArgumentType.getInteger(ctx, "amount"),
                                                        Vec3Argument.getVec3(ctx, "pos")
                                                )))))
        );

        dispatcher.register(
                Commands.literal("level")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("reset")
                                .executes(ctx -> levelReset(ctx.getSource())))
                        .then(Commands.literal("set")
                                .then(Commands.argument("value", IntegerArgumentType.integer(0))
                                        .executes(ctx -> levelSet(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value")
                                        ))))
                        .then(Commands.literal("add")
                                .then(Commands.argument("value", IntegerArgumentType.integer())
                                        .executes(ctx -> levelAdd(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "value")
                                        ))))
        );
    }

    private static int addXp(CommandSourceStack source, Collection<ServerPlayer> targets, long amount, ResourceLocation xpSource) {
        for (ServerPlayer player : targets) {
            LevelUpApi.awardXp(player, amount, xpSource);
        }
        source.sendSuccess(() -> Component.literal("Granted " + amount + " LevelUP XP to " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int setXp(CommandSourceStack source, Collection<ServerPlayer> targets, long amount) {
        for (ServerPlayer player : targets) {
            LevelUpApi.setXp(player, amount);
        }
        source.sendSuccess(() -> Component.literal("Set LevelUP XP to " + amount + " for " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int setLevel(CommandSourceStack source, Collection<ServerPlayer> targets, int level) {
        for (ServerPlayer player : targets) {
            LevelUpApi.setLevel(player, level);
        }
        source.sendSuccess(() -> Component.literal("Set LevelUP level to " + level + " for " + targets.size() + " player(s)."), true);
        return targets.size();
    }

    private static int spawnOrb(CommandSourceStack source, int amount, Vec3 pos) {
        LevelUpApi.spawnLevelUpXpOrb(source.getLevel(), pos, amount);
        source.sendSuccess(() -> Component.literal("Spawned LevelUP orb reward for " + amount + " XP."), true);
        return 1;
    }

    private static int levelReset(CommandSourceStack source) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        LevelUpApi.setLevel(player, 0);
        LevelUpApi.setXp(player, 0L);
        source.sendSuccess(() -> Component.literal("Level reset to 0."), false);
        return 1;
    }

    private static int levelSet(CommandSourceStack source, int value) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        LevelUpApi.setLevel(player, value);
        source.sendSuccess(() -> Component.literal("Level set to " + LevelUpApi.getLevel(player) + "."), false);
        return 1;
    }

    private static int levelAdd(CommandSourceStack source, int value) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        int nextLevel = Math.max(0, LevelUpApi.getLevel(player) + value);
        LevelUpApi.setLevel(player, nextLevel);
        source.sendSuccess(() -> Component.literal("Level is now " + LevelUpApi.getLevel(player) + "."), false);
        return 1;
    }
}
