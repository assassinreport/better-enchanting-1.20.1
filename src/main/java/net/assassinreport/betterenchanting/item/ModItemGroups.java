package net.assassinreport.betterenchanting.item;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.ModBlocks;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.ItemGroups;

public class ModItemGroups {
    public static void registerItemsToVanillaGroups() {
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(ModBlocks.NEW_ENCHANTING_BLOCK);
        });

        ItemGroupEvents.modifyEntriesEvent(ItemGroups.FUNCTIONAL).register(entries -> {
            entries.add(ModBlocks.NEW_CHISELED_BOOKSHELF_BLOCK);
        });
    }


    public static void registerItemGroups() {
        BetterEnchanting.LOGGER.info("Registering Item Groups for " + BetterEnchanting.MOD_ID);
    }
}
