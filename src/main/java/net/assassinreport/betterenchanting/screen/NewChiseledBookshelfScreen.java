package net.assassinreport.betterenchanting.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.assassinreport.betterenchanting.BetterEnchanting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class NewChiseledBookshelfScreen extends HandledScreen<NewChiseledBookshelfScreenHandler> {

    private static final Identifier TEXTURE = new Identifier(BetterEnchanting.MOD_ID, "textures/gui/chiseled_bookshelf_inventory.png");

    public NewChiseledBookshelfScreen(NewChiseledBookshelfScreenHandler handler, PlayerInventory inventory, Text title) {
        super(handler, inventory, title);
    }

    @Override
    protected void init(){
        this.backgroundWidth = 176;
        this.backgroundHeight = 112;

        super.init();

        titleY = 1000;
        playerInventoryTitleY = 1000;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexProgram);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - backgroundWidth) / 2;
        int y = (height - backgroundHeight) / 2;

        // Draw main background (inventory UI)
        context.drawTexture(TEXTURE, x, y, 0, 0, 176, 112, 176, 112);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        renderBackground(context);
        super.render(context, mouseX, mouseY, delta);
        drawMouseoverTooltip(context, mouseX, mouseY);
    }
}
