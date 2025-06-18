package net.assassinreport.betterenchanting.block;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.custom.NewChiseledBookshelfBlock;
import net.assassinreport.betterenchanting.block.custom.NewEnchantingBlock;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlocks {

    public static final Block NEW_ENCHANTING_BLOCK = registerBlock("new_enchanting_table",
            new NewEnchantingBlock(FabricBlockSettings.copyOf(Blocks.OBSIDIAN).nonOpaque()));

    public static final Block NEW_CHISELED_BOOKSHELF_BLOCK= registerBlock("new_chiseled_bookshelf",
            new NewChiseledBookshelfBlock(FabricBlockSettings.copyOf(Blocks.CHISELED_BOOKSHELF).nonOpaque()));

    private static Block registerBlock(String name, Block block) {
        registerBlockItem(name, block);
        return Registry.register(Registries.BLOCK, new Identifier(BetterEnchanting.MOD_ID, name), block);
    }

    private static Item registerBlockItem(String name, Block block) {
        return Registry.register(Registries.ITEM, new Identifier(BetterEnchanting.MOD_ID, name),
                new BlockItem(block, new FabricItemSettings()));
    }

    public static void registerModBlocks() {
        BetterEnchanting.LOGGER.info("Registering ModBlocks for " + BetterEnchanting.MOD_ID);
    }
}
