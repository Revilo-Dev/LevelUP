package com.revilo.levelup.registry;

import com.revilo.levelup.LevelUpMod;
import com.revilo.levelup.item.LevelUpXpRewardItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class LevelUpItems {
    public static final DeferredRegister.Items REGISTER = DeferredRegister.createItems(LevelUpMod.MOD_ID);

    public static final Supplier<Item> TEST_SKILL_ORB = REGISTER.register(
            "test_skill_orb",
            () -> new LevelUpXpRewardItem(20L, new Item.Properties().stacksTo(64))
    );

    private LevelUpItems() {}
}
