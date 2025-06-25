package net.assassinreport.betterenchanting.block.entity;

import com.mojang.logging.LogUtils;
import net.assassinreport.betterenchanting.block.custom.NewChiseledBookshelfBlock;
import net.assassinreport.betterenchanting.screen.NewChiseledBookshelfScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;

public class NewChiseledBookshelfBlockEntity extends BlockEntity implements ImplementedInventory, ExtendedScreenHandlerFactory {
    private static final Logger LOGGER = LogUtils.getLogger();
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(6, ItemStack.EMPTY);
    private int lastInteractedSlot = -1;

    BooleanProperty[] MY_SLOTS = new BooleanProperty[] {
            NewChiseledBookshelfBlock.SLOT_0_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_1_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_2_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_3_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_4_OCCUPIED,
            NewChiseledBookshelfBlock.SLOT_5_OCCUPIED
    };

    public NewChiseledBookshelfBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEW_CHISELED_BOOKSHELF_BLOCK_ENTITY, pos, state);
    }

    public void updateState(int interactedSlot) {
        if (interactedSlot >= 0 && interactedSlot < 6) {
            this.lastInteractedSlot = interactedSlot;
            BlockState oldState = this.getCachedState();
            BlockState newState = oldState;

            for (int i = 0; i < 6; i++) {
                boolean occupied = !this.getStack(i).isEmpty();
                newState = newState.with(MY_SLOTS[i], occupied);
            }

            // Only update if something changed
            if (!newState.equals(oldState)) {
                this.world.setBlockState(this.pos, newState, Block.NOTIFY_ALL);
            }
        } else {
            LOGGER.error("Expected slot 0-5, got {}", interactedSlot);
        }
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        this.inventory.clear();
        Inventories.readNbt(nbt, this.inventory);
        this.lastInteractedSlot = nbt.getInt("last_interacted_slot");
    }

    @Override
    protected void writeNbt(NbtCompound nbt) {
        Inventories.writeNbt(nbt, this.inventory, true);
        nbt.putInt("last_interacted_slot", this.lastInteractedSlot);
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public int size() {
        return 6;
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        ItemStack itemStack = this.inventory.get(slot);
        this.inventory.set(slot, ItemStack.EMPTY);
        if (!itemStack.isEmpty()) {
            this.updateState(slot);
        }

        return itemStack;
    }

    @Override
    public ItemStack removeStack(int slot) {
        return this.removeStack(slot, this.getStack(slot).getCount());
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        ItemStack currentStack = this.inventory.get(slot);

        if (stack.isIn(ItemTags.BOOKSHELF_BOOKS)) {
            this.inventory.set(slot, stack);
            this.updateState(slot);
            this.markDirty();

            if (world != null && !world.isClient && currentStack.isEmpty() && !stack.isEmpty()) {
                SoundEvent soundToPlay;

                if (stack.getItem() == Items.ENCHANTED_BOOK) {
                    soundToPlay = SoundEvents.BLOCK_END_PORTAL_FRAME_FILL;
                } else if (stack.getItem() == Items.BOOK || stack.getItem() == Items.WRITABLE_BOOK || stack.getItem() == Items.WRITTEN_BOOK) {
                    soundToPlay = SoundEvents.ITEM_BOOK_PUT;
                } else {
                    soundToPlay = SoundEvents.ITEM_BOOK_PUT;
                }

                world.playSound(
                        null,
                        pos,
                        soundToPlay,
                        SoundCategory.BLOCKS,
                        1.0f,
                        1.0f
                );
            }
        }
    }

    @Override
    public boolean canTransferTo(Inventory hopperInventory, int slot, ItemStack stack) {
        return hopperInventory.containsAny(
                itemStack2 -> itemStack2.isEmpty()
                        ? true
                        : ItemStack.canCombine(stack, itemStack2)
                        && itemStack2.getCount() + stack.getCount() <= Math.min(itemStack2.getMaxCount(), hopperInventory.getMaxCountPerStack())
        );
    }

    @Override
    public int getMaxCountPerStack() {
        return 1;
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return Inventory.canPlayerUse(this, player);
    }

    @Override
    public boolean isValid(int slot, ItemStack stack) {
        return stack.isIn(ItemTags.BOOKSHELF_BOOKS) && this.getStack(slot).isEmpty();
    }

    @Override
    public Text getDisplayName() {
        return Text.literal("New Chiseled Bookshelf");
    }

    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new NewChiseledBookshelfScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (world == null || world.isClient) return;

        // Check for nearby enchanting tables and notify them
        BlockPos.streamOutwards(pos, 3, 3, 3).forEach(checkPos -> {
            BlockEntity be = world.getBlockEntity(checkPos);
            if (be instanceof NewEnchantingTableBlockEntity enchantingTable) {
                enchantingTable.sync(); // Forces client update
            }
        });
    }
}

