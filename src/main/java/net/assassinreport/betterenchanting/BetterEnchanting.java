package net.assassinreport.betterenchanting;

import net.assassinreport.betterenchanting.block.ModBlocks;
import net.assassinreport.betterenchanting.block.entity.ModBlockEntities;
import net.assassinreport.betterenchanting.item.ModItemGroups;
import net.assassinreport.betterenchanting.network.ClientToServerPackets;
import net.assassinreport.betterenchanting.screen.ModScreenHandlers;
import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BetterEnchanting implements ModInitializer {
	public static final String MOD_ID = "betterenchanting";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModBlocks.registerModBlocks();
		ModBlockEntities.registerBlockEntities();
		ModScreenHandlers.registerScreenHandlers();
		ModItemGroups.registerItemsToVanillaGroups();
		ClientToServerPackets.register();
	}
}