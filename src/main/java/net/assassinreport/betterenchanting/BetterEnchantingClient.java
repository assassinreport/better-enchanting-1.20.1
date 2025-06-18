package net.assassinreport.betterenchanting;

import net.assassinreport.betterenchanting.block.entity.ModBlockEntities;
import net.assassinreport.betterenchanting.screen.ModScreenHandlers;
import net.assassinreport.betterenchanting.screen.NewChiseledBookshelfScreen;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.gui.screen.ingame.HandledScreens;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactories;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreen;
import net.assassinreport.betterenchanting.block.entity.renderer.NewEnchantingTableBlockEntityRenderer;

public class BetterEnchantingClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        HandledScreens.register(ModScreenHandlers.NEW_ENCHANTING_SCREEN_HANDLER, NewEnchantingScreen::new);
        HandledScreens.register(ModScreenHandlers.NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER, NewChiseledBookshelfScreen::new);

        BlockEntityRendererFactories.register(ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY, NewEnchantingTableBlockEntityRenderer::new);
    }
}