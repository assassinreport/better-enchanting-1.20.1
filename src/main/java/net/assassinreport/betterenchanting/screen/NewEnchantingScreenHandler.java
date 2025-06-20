package net.assassinreport.betterenchanting.screen;

import net.assassinreport.betterenchanting.block.entity.NewEnchantingTableBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class NewEnchantingScreenHandler extends ScreenHandler {
    private final Inventory inventory;
    public final NewEnchantingTableBlockEntity blockEntity;
    private final PlayerEntity player;
    private final World world;
    private final BlockPos pos;

    public NewEnchantingScreenHandler(int syncId, PlayerInventory inventory, PacketByteBuf buf) {
        this(syncId, inventory, inventory.player.getWorld().getBlockEntity(buf.readBlockPos()));
    }

    public NewEnchantingScreenHandler(int syncId, PlayerInventory playerInventory,
                                      BlockEntity blockEntity) {
        super(ModScreenHandlers.NEW_ENCHANTING_SCREEN_HANDLER, syncId);
        this.player = playerInventory.player;

        if (!(blockEntity instanceof NewEnchantingTableBlockEntity be)) {
            throw new IllegalStateException("Block entity is not a NewEnchantingTableBlockEntity!");
        }

        checkSize(be, 1);
        this.inventory = be;
        this.blockEntity = be;
        this.world = be.getWorld();
        this.pos = be.getPos();
        inventory.onOpen(playerInventory.player);

        this.addSlot(new Slot(inventory, 0, 8, 85) {
            @Override
            public boolean canInsert(ItemStack stack) {
                return stack.isOf(Items.BOOK)
                        || stack.isOf(Items.ENCHANTED_BOOK)
                        || stack.getItem().isEnchantable(stack);
            }

            @Override
            public int getMaxItemCount() {
                return 1;
            }
        });
        addPlayerInventory(playerInventory);
        addPlayerHotbar(playerInventory);
        addArmorSlots(playerInventory);
        addOffhandSlot(playerInventory);
    }

    private boolean hasEnchantment(ItemStack stack, Enchantment target, int level) {
        if (stack.isEmpty()) return false;

        if (stack.isOf(Items.ENCHANTED_BOOK)) {
            var enchantments = EnchantedBookItem.getEnchantmentNbt(stack);
            for (int i = 0; i < enchantments.size(); i++) {
                var tag = enchantments.getCompound(i);
                if (tag.getString("id").equals(Registries.ENCHANTMENT.getId(target).toString()) &&
                        tag.getInt("lvl") >= level) {
                    return true;
                }
            }
        } else {
            var enchantments = stack.getEnchantments(); // works for armor/tools
            for (int i = 0; i < enchantments.size(); i++) {
                var tag = enchantments.getCompound(i);
                if (tag.getString("id").equals(Registries.ENCHANTMENT.getId(target).toString()) &&
                        tag.getInt("lvl") >= level) {
                    return true;
                }
            }
        }

        return false;
    }

    public void tryEnchantItem() {
        if (!player.getWorld().isClient) {
            this.blockEntity.tryEnchantItem(player);
            this.sendContentUpdates();
        }
    }

    public void tryApplyEnchantment(ServerPlayerEntity player, Enchantment enchantment, int level) {
        ItemStack inputStack = this.getSlot(0).getStack();
        if (inputStack.isEmpty()) return;

        if (hasEnchantment(inputStack, enchantment, level)) return;

        if (this.blockEntity != null) {
            var selected = new NewEnchantingTableBlockEntity.SelectedEnchantment(enchantment, level);
            var nearbyCounts = blockEntity.getCachedEnchantments();
            int cost = blockEntity.getEnchantingCost(selected, nearbyCounts);

            if (player.experienceLevel < cost) return;

            if (inputStack.isOf(Items.BOOK)) {
                ItemStack enchantedBook = new ItemStack(Items.ENCHANTED_BOOK);
                EnchantedBookItem.addEnchantment(enchantedBook, new EnchantmentLevelEntry(enchantment, level));
                this.getSlot(0).setStack(enchantedBook);
            } else if (inputStack.isOf(Items.ENCHANTED_BOOK)) {
                EnchantedBookItem.addEnchantment(inputStack, new EnchantmentLevelEntry(enchantment, level));
                this.getSlot(0).setStack(inputStack);
            } else {
                inputStack.addEnchantment(enchantment, level);
                this.getSlot(0).setStack(inputStack);
            }

            player.addExperienceLevels(-cost);
            this.sendContentUpdates();
        }
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

                // Update block entity if needed
                if (slot.inventory == this.inventory && this.blockEntity != null) {
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
        Slot slot = this.slots.get(invSlot);
        if (slot != null && slot.hasStack()) {
            ItemStack originalStack = slot.getStack();
            newStack = originalStack.copy();
            if (invSlot < this.inventory.size()) {
                if (!this.insertItem(originalStack, this.inventory.size(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(originalStack, 0, this.inventory.size(), false)) {
                return ItemStack.EMPTY;
            }

            if (originalStack.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
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
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 30 + l * 18, 117 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(PlayerInventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 30 + i * 18, 175));
        }
    }


    private void addArmorSlots(PlayerInventory playerInventory) {
        EquipmentSlot[] armorSlots = new EquipmentSlot[] {
                EquipmentSlot.HEAD,     // Helmet
                EquipmentSlot.CHEST,    // Chestplate
                EquipmentSlot.LEGS,     // Leggings
                EquipmentSlot.FEET      // Boots
        };

        int[] yPositions = new int[] {
                117,  // Helmet
                135,  // Chestplate
                153,  // Leggings
                175   // Boots — placed farther down to match GUI
        };

        for (int i = 0; i < 4; i++) {
            final int index = 39 - i;
            final EquipmentSlot slotType = armorSlots[i];
            final int y = yPositions[i];

            this.addSlot(new Slot(playerInventory, index, 8, y) {
                @Override
                public boolean canInsert(ItemStack stack) {
                    return stack.getItem() instanceof ArmorItem armorItem &&
                            armorItem.getSlotType() == slotType;
                }

                @Override
                public int getMaxItemCount() {
                    return 1;
                }
            });
        }
    }

    private void addOffhandSlot(PlayerInventory playerInventory) {
        // Slot index: 40 (offhand)
        this.addSlot(new Slot(playerInventory, 40, 196, 175));
    }
}
