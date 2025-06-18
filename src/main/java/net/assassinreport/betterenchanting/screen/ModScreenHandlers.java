package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.fabricmc.fabric.api.screenhandler.v1.ScreenHandlerRegistry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;


public class ModScreenHandlers {
    public static ScreenHandlerType<NewEnchantingScreenHandler> NEW_ENCHANTING_SCREEN_HANDLER;
    public static ScreenHandlerType<NewChiseledBookshelfScreenHandler> NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER;

    public static void registerScreenHandlers() {
        NEW_ENCHANTING_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(
                new Identifier(BetterEnchanting.MOD_ID, "new_enchanting_screen"),
                (syncId, inventory, buf) -> {
                    return new NewEnchantingScreenHandler(syncId, inventory, buf);
                }
        );

        NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER = ScreenHandlerRegistry.registerExtended(
                new Identifier(BetterEnchanting.MOD_ID, "new_chiseled_bookshelf_screen"),
                (syncId, inventory, buf) -> {
                    return new NewChiseledBookshelfScreenHandler(syncId, inventory, buf);
                }
        );

        BetterEnchanting.LOGGER.info("Registering Screen Handlers for " + BetterEnchanting.MOD_ID);
    }
}
