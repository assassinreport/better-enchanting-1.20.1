package net.assassinreport.betterenchanting.block.entity.renderer;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.render.block.entity.BlockEntityRendererFactory;
import net.minecraft.client.render.entity.model.BookModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

public class NewEnchantingTableBlockEntityRenderer implements BlockEntityRenderer<NewEnchantingTableBlockEntity> {
    private static final SpriteIdentifier BOOK_TEXTURE = new SpriteIdentifier(
            SpriteAtlasTexture.BLOCK_ATLAS_TEXTURE, new Identifier("entity/enchanting_table_book")
    );

    private final BookModel book;

    public NewEnchantingTableBlockEntityRenderer(BlockEntityRendererFactory.Context context) {
        this.book = new BookModel(context.getLayerModelPart(EntityModelLayers.BOOK));
    }

    @Override
    public void render(NewEnchantingTableBlockEntity entity, float tickDelta, MatrixStack matrices,
                       VertexConsumerProvider vertexConsumers, int light, int overlay) {
        matrices.push();

        // Center and bob the book
        matrices.translate(0.5F, 0.75F, 0.5F);
        float g = entity.ticks + tickDelta;
        matrices.translate(0.0F, 0.1F + MathHelper.sin(g * 0.1F) * 0.01F, 0.0F);

        // Smooth rotation interpolation
        float rotationDiff = entity.bookRotation - entity.lastBookRotation;
        while (rotationDiff >= Math.PI) rotationDiff -= (float)(Math.PI * 2);
        while (rotationDiff < -Math.PI) rotationDiff += (float)(Math.PI * 2);

        float rotation = entity.lastBookRotation + rotationDiff * tickDelta;
        matrices.multiply(RotationAxis.POSITIVE_Y.rotation(-rotation));
        matrices.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(80.0F));

        // Calculate page angles for animation
        float pageLerp = MathHelper.lerp(tickDelta, entity.pageAngle, entity.nextPageAngle);
        float page1 = MathHelper.fractionalPart(pageLerp + 0.25F) * 1.6F - 0.3F;
        float page2 = MathHelper.fractionalPart(pageLerp + 0.75F) * 1.6F - 0.3F;
        float pageTurnSpeed = MathHelper.lerp(tickDelta, entity.pageTurningSpeed, entity.nextPageTurningSpeed);

        this.book.setPageAngles(g, MathHelper.clamp(page1, 0.0F, 1.0F), MathHelper.clamp(page2, 0.0F, 1.0F), pageTurnSpeed);

        // Get vertex consumer with the book texture
        VertexConsumer vertexConsumer = BOOK_TEXTURE.getVertexConsumer(vertexConsumers, RenderLayer::getEntitySolid);

        // Render the book model
        this.book.renderBook(matrices, vertexConsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);

        matrices.pop();
    }
}
