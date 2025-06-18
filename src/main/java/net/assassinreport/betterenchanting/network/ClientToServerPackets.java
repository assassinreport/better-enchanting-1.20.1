package net.assassinreport.betterenchanting.network;

import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.screen.NewEnchantingScreenHandler;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public class ClientToServerPackets {
    public static final Identifier ENCHANT_BUTTON_PACKET_ID = new Identifier(BetterEnchanting.MOD_ID, "enchant_button");
    public static final Identifier SELECT_ENCHANTMENT_PACKET_ID = new Identifier(BetterEnchanting.MOD_ID, "select_enchantment");

    public static void register() {
        ServerPlayNetworking.registerGlobalReceiver(ENCHANT_BUTTON_PACKET_ID, (server, player, handler, buf, responseSender) -> {
            server.execute(() -> {
                if (player.currentScreenHandler instanceof NewEnchantingScreenHandler enchantingHandler) {
                    enchantingHandler.tryEnchantItem();
                }
            });
        });

        ServerPlayNetworking.registerGlobalReceiver(SELECT_ENCHANTMENT_PACKET_ID,
                (server, player, handler, buf, responseSender) -> {
                    Identifier enchantId = buf.readIdentifier();
                    int level = buf.readVarInt();

                    server.execute(() -> {
                        Enchantment enchantment = Registries.ENCHANTMENT.get(enchantId);
                        if (enchantment != null && player.currentScreenHandler instanceof NewEnchantingScreenHandler enchantingHandler) {
                            var blockEntity = enchantingHandler.blockEntity;
                            var key = new NewEnchantingTableBlockEntity.EnchantmentLevel(enchantment, level);

                            if (blockEntity.getCachedEnchantments().getOrDefault(key, 0) >= 6) {
                                enchantingHandler.tryApplyEnchantment((ServerPlayerEntity) player, enchantment, level);
                            }
                        }
                    });
                });
    }

    public static void sendSelectEnchantmentPacket(Enchantment enchantment, int level) {
        PacketByteBuf buf = PacketByteBufs.create();
        buf.writeIdentifier(Registries.ENCHANTMENT.getId(enchantment));
        buf.writeVarInt(level);
        ClientPlayNetworking.send(SELECT_ENCHANTMENT_PACKET_ID, buf);
    }

    public static void sendEnchantPacket() {
        PacketByteBuf buf = PacketByteBufs.create();
        ClientPlayNetworking.send(ENCHANT_BUTTON_PACKET_ID, buf);
    }
}
