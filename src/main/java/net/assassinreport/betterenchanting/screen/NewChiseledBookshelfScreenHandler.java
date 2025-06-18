package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.block.entity.NewChiseledBookshelfBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ArrayPropertyDelegate;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NewChiseledBookshelfScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    private final PropertyDelegate propertyDelegate;
    public final NewChiseledBookshelfBlockEntity blockEntity;
    private final PlayerEntity player;
    private final World world;
    private final BlockPos pos;

    public NewChiseledBookshelfScreenHandler(int syncId, PlayerInventory playerInventory, PacketByteBuf buf) {
        this(syncId, playerInventory, playerInventory.player.getWorld().getBlockEntity(buf.readBlockPos()));
    }

    public NewChiseledBookshelfScreenHandler(int syncId, PlayerInventory playerInventory, BlockEntity blockEntity) {
        this(syncId, playerInventory, blockEntity, new ArrayPropertyDelegate(2));
    }

    public NewChiseledBookshelfScreenHandler(int syncId, PlayerInventory playerInventory,
                                             BlockEntity blockEntity, PropertyDelegate arrayPropertyDelegate) {
        super(ModScreenHandlers.NEW_CHISELED_BOOKSHELF_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;
        if (!(blockEntity instanceof NewChiseledBookshelfBlockEntity be)) {
            throw new IllegalStateException("Block entity is not a NewChiseledBookshelfBlockEntity!");
        }
        checkSize(be, 6);
        this.inventory = be;
        this.blockEntity = be;
        this.world = be.getWorld();
        this.pos = be.getPos();
        inventory.onOpen(playerInventory.player);
        this.propertyDelegate = arrayPropertyDelegate;

        for (int i = 0; i < 6; i++) {
            final int slotIndex = i;
            this.addSlot(new Slot(inventory, i, 8 + i * 18, 8) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.isIn(net.minecraft.registry.tag.ItemTags.BOOKSHELF_BOOKS);
                }

                @Override
                public int getMaxItemCount() {
                    return 1;
                }
            });
        }


        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);

        addProperties(arrayPropertyDelegate);
    }

    @Override
    protected boolean insertItem(ItemStack stack, int startIndex, int endIndex, boolean fromLast) {
        int step = fromLast ? -1 : 1;
        int i = fromLast ? endIndex - 1 : startIndex;

        while (stack.getCount() > 0 && (fromLast ? i >= startIndex : i < endIndex)) {
            Slot slot = this.slots.get(i);
            ItemStack slotStack = slot.getStack();

            if (slotStack.isEmpty() && slot.canInsert(stack)) {
                // Insert exactly 1 item only
                ItemStack oneItem = stack.copy();
                oneItem.setCount(1);
                slot.setStack(oneItem);
                slot.markDirty();

                stack.decrement(1);

                // Update bookshelf block entity state for this slot
                if (slot.inventory == this.inventory && this.blockEntity != null) {
                    this.blockEntity.updateState(slot.getIndex());
                    this.blockEntity.markDirty();
                }
            }

            i += step;
        }

        return stack.isEmpty();
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int invSlot) {
        ItemStack newStack = ItemStack.EMPTY;
        Slot clickedSlot = this.slots.get(invSlot);

        if (clickedSlot != null && clickedSlot.hasStack()) {
            ItemStack originalStack = clickedSlot.getStack();
            newStack = originalStack.copy();

            int bookshelfSize = this.inventory.size();

            if (invSlot < bookshelfSize) {
                // Move from bookshelf to player
                if (!this.insertItem(originalStack, bookshelfSize, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }

                // Update state for the bookshelf slot item is moved from
                this.blockEntity.updateState(invSlot);
                this.blockEntity.markDirty();
            } else {
                // Move from player to bookshelf
                if (!this.insertItem(originalStack, 0, bookshelfSize, false)) {
                    return ItemStack.EMPTY;
                }

                // Update texture states for bookshelf slots
                for (int i = 0; i < bookshelfSize; i++) {
                    this.blockEntity.updateState(i);
                }
                this.blockEntity.markDirty();
            }

            if (originalStack.isEmpty()) {
                clickedSlot.setStack(ItemStack.EMPTY);
            } else {
                clickedSlot.markDirty();
            }
        }

        return newStack;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return this.inventory.canPlayerUse(player);
    }

    private void addPlayerInventory(PlayerInventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 30 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 88));
        }
    }
}

