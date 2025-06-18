package net.assassinreport.betterenchanting.block.entity;


import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.ModBlocks;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

public class ModBlockEntities {
    public static final BlockEntityType<NewEnchantingTableBlockEntity> NEW_ENCHANTING_TABLE_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(BetterEnchanting.MOD_ID, "new_enchanting_table"),
                    FabricBlockEntityTypeBuilder.create(NewEnchantingTableBlockEntity::new,
                            ModBlocks.NEW_ENCHANTING_BLOCK).build());

    public static final BlockEntityType<NewChiseledBookshelfBlockEntity> NEW_CHISELED_BOOKSHELF_BLOCK_ENTITY =
            Registry.register(Registries.BLOCK_ENTITY_TYPE,
                    new Identifier(BetterEnchanting.MOD_ID, "new_chiseled_bookshelf"),
                    FabricBlockEntityTypeBuilder.create(NewChiseledBookshelfBlockEntity::new,
                            ModBlocks.NEW_CHISELED_BOOKSHELF_BLOCK).build(null));

    public static void registerBlockEntities() {
        BetterEnchanting.LOGGER.info("Registering Block Entities for " + BetterEnchanting.MOD_ID);
    }
}
