package net.assassinreport.betterenchanting.block.custom;

import net.assassinreport.betterenchanting.block.entity.NewChiseledBookshelfBlockEntity;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.DirectionProperty;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class NewChiseledBookshelfBlock extends BlockWithEntity implements BlockEntityProvider {
    public static final DirectionProperty FACING = HorizontalFacingBlock.FACING;
    private static final VoxelShape SHAPE = Block.createCuboidShape(0, 0, 0, 16, 16, 16);
    public static final BooleanProperty SLOT_0_OCCUPIED = BooleanProperty.of("slot_0_occupied");
    public static final BooleanProperty SLOT_1_OCCUPIED = BooleanProperty.of("slot_1_occupied");
    public static final BooleanProperty SLOT_2_OCCUPIED = BooleanProperty.of("slot_2_occupied");
    public static final BooleanProperty SLOT_3_OCCUPIED = BooleanProperty.of("slot_3_occupied");
    public static final BooleanProperty SLOT_4_OCCUPIED = BooleanProperty.of("slot_4_occupied");
    public static final BooleanProperty SLOT_5_OCCUPIED = BooleanProperty.of("slot_5_occupied");


    public NewChiseledBookshelfBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(FACING, Direction.NORTH)
                .with(SLOT_0_OCCUPIED, false)
                .with(SLOT_1_OCCUPIED, false)
                .with(SLOT_2_OCCUPIED, false)
                .with(SLOT_3_OCCUPIED, false)
                .with(SLOT_4_OCCUPIED, false)
                .with(SLOT_5_OCCUPIED, false));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return this.getDefaultState()
                .with(FACING, ctx.getHorizontalPlayerFacing().getOpposite())
                .with(SLOT_0_OCCUPIED, false)
                .with(SLOT_1_OCCUPIED, false)
                .with(SLOT_2_OCCUPIED, false)
                .with(SLOT_3_OCCUPIED, false)
                .with(SLOT_4_OCCUPIED, false)
                .with(SLOT_5_OCCUPIED, false);
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
        return SHAPE;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(SLOT_0_OCCUPIED);
        builder.add(SLOT_1_OCCUPIED);
        builder.add(SLOT_2_OCCUPIED);
        builder.add(SLOT_3_OCCUPIED);
        builder.add(SLOT_4_OCCUPIED);
        builder.add(SLOT_5_OCCUPIED);
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }

    @Override
    public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
        return new NewChiseledBookshelfBlockEntity(pos, state);
    }

    @Override
    public void onStateReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean moved) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof NewChiseledBookshelfBlockEntity) {
                ItemScatterer.spawn(world, pos, (NewChiseledBookshelfBlockEntity) blockEntity);
                world.updateComparators(pos,this);
            }
            super.onStateReplaced(state, world, pos, newState, moved);
        }
    }


    @Override
    public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
        if (!world.isClient) {
            BlockEntity be = world.getBlockEntity(pos);
            if (be instanceof NamedScreenHandlerFactory factory) {
                player.openHandledScreen(factory);
            }
        }

        return ActionResult.SUCCESS;
    }
}
