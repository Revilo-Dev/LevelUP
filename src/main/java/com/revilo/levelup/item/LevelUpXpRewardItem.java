package com.revilo.levelup.item;

import com.revilo.levelup.api.LevelUpApi;
import com.revilo.levelup.api.LevelUpSources;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class LevelUpXpRewardItem extends Item {
    private final long xpReward;

    public LevelUpXpRewardItem(long xpReward, Properties properties) {
        super(properties);
        this.xpReward = Math.max(0L, xpReward);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, net.minecraft.world.entity.player.Player player, InteractionHand usedHand) {
        ItemStack stack = player.getItemInHand(usedHand);
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            LevelUpApi.awardXp(serverPlayer, xpReward, LevelUpSources.ITEM_USE);
            player.getCooldowns().addCooldown(this, 10);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
