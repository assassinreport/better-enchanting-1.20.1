package net.assassinreport.betterenchanting.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.assassinreport.betterenchanting.BetterEnchanting;
import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.assassinreport.betterenchanting.network.ClientToServerPackets;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class NewEnchantingScreen extends HandledScreen<NewEnchantingScreenHandler> {

    private ItemStack lastInputStack = ItemStack.EMPTY;

    private static final Map<Enchantment, Integer> weights = Map.ofEntries(
            Map.entry(Enchantments.SILK_TOUCH, 0),
            Map.entry(Enchantments.LURE, 1),
            Map.entry(Enchantments.LUCK_OF_THE_SEA, 2),
            Map.entry(Enchantments.FORTUNE, 3),
            Map.entry(Enchantments.EFFICIENCY, 4),
            Map.entry(Enchantments.UNBREAKING, 5),
            Map.entry(Enchantments.MENDING, 6),
            Map.entry(Enchantments.THORNS, 7),
            Map.entry(Enchantments.SWIFT_SNEAK, 8),
            Map.entry(Enchantments.SOUL_SPEED, 9),
            Map.entry(Enchantments.RESPIRATION, 10),
            Map.entry(Enchantments.PROTECTION, 11),
            Map.entry(Enchantments.PROJECTILE_PROTECTION, 12),
            Map.entry(Enchantments.FROST_WALKER, 13),
            Map.entry(Enchantments.FIRE_PROTECTION, 14),
            Map.entry(Enchantments.FEATHER_FALLING, 15),
            Map.entry(Enchantments.DEPTH_STRIDER, 16),
            Map.entry(Enchantments.BLAST_PROTECTION, 17),
            Map.entry(Enchantments.AQUA_AFFINITY, 18),
            Map.entry(Enchantments.SWEEPING, 19),
            Map.entry(Enchantments.SMITE, 20),
            Map.entry(Enchantments.SHARPNESS, 21),
            Map.entry(Enchantments.RIPTIDE, 22),
            Map.entry(Enchantments.QUICK_CHARGE, 23),
            Map.entry(Enchantments.PUNCH, 24),
            Map.entry(Enchantments.POWER, 25),
            Map.entry(Enchantments.PIERCING, 26),
            Map.entry(Enchantments.MULTISHOT, 27),
            Map.entry(Enchantments.LOYALTY, 28),
            Map.entry(Enchantments.LOOTING, 29),
            Map.entry(Enchantments.KNOCKBACK, 30),
            Map.entry(Enchantments.INFINITY, 31),
            Map.entry(Enchantments.IMPALING, 32),
            Map.entry(Enchantments.FLAME, 33),
            Map.entry(Enchantments.FIRE_ASPECT, 34),
            Map.entry(Enchantments.CHANNELING, 35),
            Map.entry(Enchantments.BANE_OF_ARTHROPODS, 36),
            Map.entry(Enchantments.VANISHING_CURSE, 37),
            Map.entry(Enchantments.BINDING_CURSE, 38)
    );

    private static final Enchantment[] ORDERED_ENCHANTMENTS = new Enchantment[] {
            Enchantments.SILK_TOUCH,
            Enchantments.LURE,
            Enchantments.LUCK_OF_THE_SEA,
            Enchantments.FORTUNE,
            Enchantments.EFFICIENCY,
            Enchantments.UNBREAKING,
            Enchantments.MENDING,
            Enchantments.THORNS,
            Enchantments.SWIFT_SNEAK,
            Enchantments.SOUL_SPEED,
            Enchantments.RESPIRATION,
            Enchantments.PROTECTION,
            Enchantments.PROJECTILE_PROTECTION,
            Enchantments.FROST_WALKER,
            Enchantments.FIRE_PROTECTION,
            Enchantments.FEATHER_FALLING,
            Enchantments.DEPTH_STRIDER,
            Enchantments.BLAST_PROTECTION,
            Enchantments.AQUA_AFFINITY,
            Enchantments.SWEEPING,
            Enchantments.SMITE,
            Enchantments.SHARPNESS,
            Enchantments.RIPTIDE,
            Enchantments.QUICK_CHARGE,
            Enchantments.PUNCH,
            Enchantments.POWER,
            Enchantments.PIERCING,
            Enchantments.MULTISHOT,
            Enchantments.LOYALTY,
            Enchantments.LOOTING,
            Enchantments.KNOCKBACK,
            Enchantments.INFINITY,
            Enchantments.IMPALING,
            Enchantments.FLAME,
            Enchantments.FIRE_ASPECT,
            Enchantments.CHANNELING,
            Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.VANISHING_CURSE,
            Enchantments.BINDING_CURSE,
    };


    private final List<EnchantmentButton> enchantmentButtons = new ArrayList<>();

    private static final Identifier TEXTURE = new Identifier(BetterEnchanting.MOD_ID, "textures/gui/enchanting_menu.png");

    public NewEnchantingScreen(NewEnchantingScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }
    private class EnchantmentButton extends ButtonWidget {
        private final Enchantment enchant;

        public EnchantmentButton(int x, int y, int width, int height, Enchantment enchant, PressAction onPress) {
            super(x, y, width, height, Text.empty(), onPress, button -> Text.empty());
            this.enchant = enchant;
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            boolean active = false;
            Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = handler.blockEntity.getCachedEnchantments();

            for (var entry : cached.entrySet()) {
                if (entry.getKey().enchantment() == enchant && entry.getValue() >= 6) {
                    active = true;
                    break;
                }
            }

            int row = 0;

            if (active) {
                if (isHovered()) {
                    row = 2;  // hover texture for active enchantments
                } else {
                    row = 1;  // active texture
                }
            }

            context.drawTexture(TEXTURE, getX(), getY(), weights.get(enchant) * 22, 204 + row * 20, 22, 20, 858, 264);
        }

        public Enchantment getEnchantment() {
            return enchant;
        }
    }

    @Override
    protected void init() {
        super.init();

        this.backgroundWidth = 223;
        this.backgroundHeight = 199;
        this.x = (this.width - this.backgroundWidth) / 2;
        this.y = (this.height - this.backgroundHeight) / 2;

        rebuildButtons();

        this.addDrawableChild(new IconButton(
                x + 5, y + 39, 22, 20,
                223, 3, 23, // U, V for normal; V for hover
                22, 20,     // width, height
                button -> ClientToServerPackets.sendEnchantPacket()
        ));

        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    private class IconButton extends ButtonWidget {
        private final int textureU;
        private final int textureV;
        private final int textureVHover;
        private final int textureWidth;
        private final int textureHeight;

        public IconButton(int x, int y, int width, int height,
                          int textureU, int textureV, int textureVHover,
                          int textureWidth, int textureHeight,
                          PressAction onPress) {
            super(x, y, width, height, Text.empty(), onPress, button -> Text.empty());
            this.textureU = textureU;
            this.textureV = textureV;
            this.textureVHover = textureVHover;
            this.textureWidth = textureWidth;
            this.textureHeight = textureHeight;
        }

        @Override
        public void renderButton(DrawContext context, int mouseX, int mouseY, float delta) {
            int drawV = isHovered() ? textureVHover : textureV;

            context.drawTexture(
                    TEXTURE,
                    getX(), getY(),
                    textureU, drawV,
                    textureWidth, textureHeight,
                    858, 264
            );

            if (isHovered()) {
                context.drawTooltip(
                        NewEnchantingScreen.this.textRenderer,
                        Text.literal("Random"),
                        mouseX,
                        mouseY
                );
            }
        }
    }

    private void rebuildButtons() {
        for (EnchantmentButton btn : enchantmentButtons) {
            this.remove(btn);
        }
        enchantmentButtons.clear();

        ItemStack inputStack = handler.getSlot(0).getStack();

        if (inputStack.isEmpty()) return;

        int iconWidth = 22;
        int iconHeight = 20;
        int iconStartX = this.x + 32;
        int iconStartY = this.y + 6;

        int layoutIndex = 0;
        for (Enchantment enchantment : ORDERED_ENCHANTMENTS) {
            if (!(enchantment.isAcceptableItem(inputStack) || inputStack.isOf(Items.BOOK))) continue;

            Integer iconIndex = weights.get(enchantment);
            if (iconIndex == null) continue;

            int btnX = iconStartX + (layoutIndex % 8) * (iconWidth + 1);
            int btnY = iconStartY + (layoutIndex / 8) * (iconHeight + 1);
            layoutIndex++;

            EnchantmentButton button = new EnchantmentButton(
                    btnX,
                    btnY,
                    iconWidth,
                    iconHeight,
                    enchantment,
                    btn -> {
                        int unlockedLevel = 1; // Default to level 1
                        Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = handler.blockEntity.getCachedEnchantments();

                        for (var entry : cached.entrySet()) {
                            if (entry.getKey().enchantment() == enchantment && entry.getValue() >= 6) {
                                unlockedLevel = Math.max(unlockedLevel, entry.getKey().level());
                            }
                        }

                        ClientToServerPackets.sendSelectEnchantmentPacket(enchantment, unlockedLevel);
                    });



            this.addDrawableChild(button);
            enchantmentButtons.add(button);
        }
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Draw main background (inventory UI)
        context.drawTexture(TEXTURE, x, y, 0, 0, 223, 199, 858, 264);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Let buttons handle clicks
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        ItemStack currentInput = handler.getSlot(0).getStack();
        if (!ItemStack.areEqual(currentInput, lastInputStack)) {
            lastInputStack = currentInput.copy();
            rebuildButtons();
        }

        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);

        // Show tooltip for hovered enchantment button
        for (EnchantmentButton btn : enchantmentButtons) {
            if (btn.isHovered()) {
                Enchantment enchantment = btn.getEnchantment(); // Define enchantment from the button
                int unlockedLevel = 0;
                Map<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> cached = handler.blockEntity.getCachedEnchantments();

                for (Map.Entry<NewEnchantingTableBlockEntity.EnchantmentLevel, Integer> entry : cached.entrySet()) {
                    NewEnchantingTableBlockEntity.EnchantmentLevel el = entry.getKey();
                    if (el.enchantment() == enchantment && entry.getValue() >= 6) {
                        unlockedLevel = Math.max(unlockedLevel, el.level());
                    }
                }

                Text tooltipText = unlockedLevel > 0
                        ? enchantment.getName(unlockedLevel) // Shows "Protection II" for example
                        : Text.translatable(enchantment.getTranslationKey());

                context.drawTooltip(this.textRenderer, tooltipText, mouseX, mouseY);
                break;
            }
        }
    }
}
