package net.assassinreport.betterenchanting.block.entity;

import net.assassinreport.betterenchanting.screen.NewEnchantingScreenHandler;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NewEnchantingTableBlockEntity extends BlockEntity implements ExtendedScreenHandlerFactory, ImplementedInventory {
    private final DefaultedList<ItemStack> inventory = DefaultedList.ofSize(1, ItemStack.EMPTY);
    public int ticks;
    public float nextPageAngle;
    public float pageAngle;
    public float flipRandom;
    public float flipTurn;
    public float nextPageTurningSpeed;
    public float pageTurningSpeed;
    public float bookRotation;
    public float lastBookRotation;
    public float targetBookRotation;
    private static final Random RANDOM = Random.create();

    private Map<EnchantmentLevel, Integer> cachedEnchantments = new HashMap<>();


    @Override
    public Text getDisplayName() {
        return Text.translatable("Enchant");
    }

    public NewEnchantingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NEW_ENCHANTING_TABLE_BLOCK_ENTITY, pos, state);
        }

    public static Map<EnchantmentLevel, Integer> getNearbyEnchantmentCounts(World world, BlockPos center, int radius) {
        Map<EnchantmentLevel, Integer> counts = new HashMap<>();
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dy = -radius; dy <= radius; dy++) {
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    BlockEntity be = world.getBlockEntity(pos);
                    if (be instanceof NewChiseledBookshelfBlockEntity bookshelf) {
                        for (int i = 0; i < bookshelf.size(); i++) {
                            ItemStack book = bookshelf.getStack(i);
                            if (book.getItem() == Items.ENCHANTED_BOOK) {
                                Map<Enchantment, Integer> enchants = EnchantmentHelper.get(book);
                                for (Map.Entry<Enchantment, Integer> entry : enchants.entrySet()) {
                                    EnchantmentLevel el = new EnchantmentLevel(entry.getKey(), entry.getValue());
                                    counts.merge(el, 1, Integer::sum);
                                }
                            }
                        }
                    }
                }
            }
        }

        return counts;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        refreshCachedEnchantmentsAndSync();
        return new NewEnchantingScreenHandler(syncId, playerInventory, this);
    }

    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt() {
        NbtCompound nbt = new NbtCompound();
        this.writeNbt(nbt);
        return nbt;
    }

    public void tryEnchantItem(PlayerEntity player) {
        ItemStack stack = this.getStack(0);

        if (!stack.isEmpty() && player.experienceLevel >= 20) {
            List<Enchantment> valid;

            if (stack.isOf(Items.BOOK)) {
                valid = Registries.ENCHANTMENT.stream().toList(); // All enchantments
            } else {
                valid = Registries.ENCHANTMENT.stream()
                        .filter(e -> e.isAcceptableItem(stack)) // Only those applicable to the item
                        .toList();
            }

            Map<EnchantmentLevel, Integer> bonus = getNearbyEnchantmentCounts(this.world, this.pos, 3);

            SelectedEnchantment selected = getWeightedRandomEnchantment(valid, bonus, player.getRandom());

            if (selected != null) {
                int cost = getEnchantingCost(selected, bonus);
                if (player.experienceLevel >= cost) {
                    if (stack.isOf(Items.BOOK)) {
                        // Replace book with enchanted book
                        ItemStack newStack = new ItemStack(Items.ENCHANTED_BOOK);
                        EnchantedBookItem.addEnchantment(newStack, new EnchantmentLevelEntry(selected.enchantment(), selected.level()));
                        this.setStack(0, newStack);
                    } else {
                        // Apply enchantment directly to item
                        Map<Enchantment, Integer> currentEnchants = EnchantmentHelper.get(stack);
                        int currentLevel = currentEnchants.getOrDefault(selected.enchantment(), 0);

                        if (currentLevel < selected.level()) {
                            stack.addEnchantment(selected.enchantment(), selected.level());
                        }
                    }

                    player.addExperienceLevels(-cost);
                }
            }
        }
    }

    public int getEnchantingCost(SelectedEnchantment selected, Map<EnchantmentLevel, Integer> bonusCounts) {
        EnchantmentLevel levelKey = new EnchantmentLevel(selected.enchantment(), selected.level());
        int count = bonusCounts.getOrDefault(levelKey, 0);

        if (count >= 6) {
            return 0; // Permanently unlocked, reduced cost
        } else {
            return 20 + (count * 2); // Increases up to 30
        }
    }

    private SelectedEnchantment getWeightedRandomEnchantment(
            List<Enchantment> baseCandidates,
            Map<EnchantmentLevel, Integer> bonusCounts,
            Random random) {

        final int baseWeight = 100;
        final int bonusPerBook = 165;

        List<SelectedEnchantment> weightedList = new ArrayList<>();

        // Tracks the highest level suppressed for each enchantment
        Map<Enchantment, Integer> suppressedLevels = new HashMap<>();
        // Tracks which new levels have been unlocked
        Map<Enchantment, Integer> unlockedLevels = new HashMap<>();

        // Determine suppressed and unlocked levels
        for (Map.Entry<EnchantmentLevel, Integer> entry : bonusCounts.entrySet()) {
            Enchantment enchant = entry.getKey().enchantment();
            int level = entry.getKey().level();
            int count = entry.getValue();

            if (count >= 6) {
                // Suppress this level and below
                suppressedLevels.merge(enchant, level, Math::max);

                // Unlock next level (if within range)
                if (level < enchant.getMaxLevel()) {
                    unlockedLevels.merge(enchant, level + 1, Math::max);
                }
            }
        }

        // Build weighted pool
        for (Enchantment enchant : baseCandidates) {
            int maxLevel = enchant.getMaxLevel();

            // Add unlocked levels
            if (unlockedLevels.containsKey(enchant)) {
                int level = unlockedLevels.get(enchant);
                int count = getCount(bonusCounts, enchant, level);
                int weight = baseWeight + (bonusPerBook * Math.min(count, 5));

                for (int i = 0; i < weight; i++) {
                    weightedList.add(new SelectedEnchantment(enchant, level));
                }

                System.out.println("[DEBUG] Unlocked: " + enchant.getTranslationKey() + " level " + level + " weight=" + weight);
            }

            // Add non-suppressed levels from books
            for (int level = 1; level <= maxLevel; level++) {
                // Skip suppressed levels
                if (suppressedLevels.getOrDefault(enchant, 0) >= level) continue;

                int count = getCount(bonusCounts, enchant, level);

                if (count > 0 || level == 1) {
                    int weight = baseWeight + (bonusPerBook * Math.min(count, 5));

                    for (int i = 0; i < weight; i++) {
                        weightedList.add(new SelectedEnchantment(enchant, level));
                    }

                    System.out.println("[DEBUG] Normal: " + enchant.getTranslationKey() + " level " + level + " weight=" + weight);
                }
            }
        }

        if (weightedList.isEmpty()) return null;

        return weightedList.get(random.nextInt(weightedList.size()));
    }

    public void sync() {
        if (this.world != null && !this.world.isClient) {
            this.world.updateListeners(this.pos, this.getCachedState(), this.getCachedState(), 3);
        }
    }

    public static void tick(World world, BlockPos pos, BlockState state, NewEnchantingTableBlockEntity blockEntity) {
        blockEntity.pageTurningSpeed = blockEntity.nextPageTurningSpeed;
        blockEntity.lastBookRotation = blockEntity.bookRotation;
        PlayerEntity playerEntity = world.getClosestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
        if (playerEntity != null) {
            double d = playerEntity.getX() - (pos.getX() + 0.5);
            double e = playerEntity.getZ() - (pos.getZ() + 0.5);
            blockEntity.targetBookRotation = (float) MathHelper.atan2(e, d);
            blockEntity.nextPageTurningSpeed += 0.1F;
            if (blockEntity.nextPageTurningSpeed < 0.5F || RANDOM.nextInt(40) == 0) {
                float f = blockEntity.flipRandom;

                do {
                    blockEntity.flipRandom = blockEntity.flipRandom + (RANDOM.nextInt(4) - RANDOM.nextInt(4));
                } while (f == blockEntity.flipRandom);
            }
        } else {
            blockEntity.targetBookRotation += 0.02F;
            blockEntity.nextPageTurningSpeed -= 0.1F;
        }

        while (blockEntity.bookRotation >= (float) Math.PI) {
            blockEntity.bookRotation -= (float) (Math.PI * 2);
        }

        while (blockEntity.bookRotation < (float) -Math.PI) {
            blockEntity.bookRotation += (float) (Math.PI * 2);
        }

        while (blockEntity.targetBookRotation >= (float) Math.PI) {
            blockEntity.targetBookRotation -= (float) (Math.PI * 2);
        }

        while (blockEntity.targetBookRotation < (float) -Math.PI) {
            blockEntity.targetBookRotation += (float) (Math.PI * 2);
        }

        float g = blockEntity.targetBookRotation - blockEntity.bookRotation;

        while (g >= (float) Math.PI) {
            g -= (float) (Math.PI * 2);
        }

        while (g < (float) -Math.PI) {
            g += (float) (Math.PI * 2);
        }

        blockEntity.bookRotation += g * 0.4F;
        blockEntity.nextPageTurningSpeed = MathHelper.clamp(blockEntity.nextPageTurningSpeed, 0.0F, 1.0F);
        blockEntity.ticks++;
        blockEntity.pageAngle = blockEntity.nextPageAngle;
        float h = (blockEntity.flipRandom - blockEntity.nextPageAngle) * 0.4F;
        float i = 0.2F;
        h = MathHelper.clamp(h, -0.2F, 0.2F);
        blockEntity.flipTurn = blockEntity.flipTurn + (h - blockEntity.flipTurn) * 0.9F;
        blockEntity.nextPageAngle = blockEntity.nextPageAngle + blockEntity.flipTurn;

        if (!world.isClient && blockEntity.ticks % 20 == 0) {
            blockEntity.cachedEnchantments = getNearbyEnchantmentCounts(world, pos, 2);
            blockEntity.sync();
        }

        blockEntity.ticks++;
    }

    public Map<EnchantmentLevel, Integer> getCachedEnchantments() {
        return cachedEnchantments;
    }

    @Override
    public void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, inventory);

        NbtCompound enchantsNbt = new NbtCompound();
        int i = 0;
        for (Map.Entry<EnchantmentLevel, Integer> entry : cachedEnchantments.entrySet()) {
            EnchantmentLevel key = entry.getKey();
            int count = entry.getValue();

            NbtCompound entryNbt = new NbtCompound();
            entryNbt.putString("enchantment", Registries.ENCHANTMENT.getId(key.enchantment()).toString());
            entryNbt.putInt("level", key.level());
            entryNbt.putInt("count", count);

            enchantsNbt.put(String.valueOf(i++), entryNbt);
        }
        nbt.put("cachedEnchantments", enchantsNbt);
    }

    @Override
    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        Inventories.readNbt(nbt, inventory);

        cachedEnchantments.clear();
        if (nbt.contains("cachedEnchantments", 10)) {
            NbtCompound enchantsNbt = nbt.getCompound("cachedEnchantments");
            for (String key : enchantsNbt.getKeys()) {
                NbtCompound entryNbt = enchantsNbt.getCompound(key);
                Enchantment enchantment = Registries.ENCHANTMENT.get(new Identifier(entryNbt.getString("enchantment")));
                int level = entryNbt.getInt("level");
                int count = entryNbt.getInt("count");
                cachedEnchantments.put(new EnchantmentLevel(enchantment, level), count);
            }
        }
    }

    @Override
    public DefaultedList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    public void writeScreenOpeningData(ServerPlayerEntity serverPlayerEntity, PacketByteBuf buf) {
        buf.writeBlockPos(this.pos);
    }

    public void refreshCachedEnchantmentsAndSync() {
        if (world != null && !world.isClient) {
            this.cachedEnchantments = getNearbyEnchantmentCounts(world, pos, 3);
            sync();
        }
    }

    public record EnchantmentLevel(Enchantment enchantment, int level) {}
    public record SelectedEnchantment(Enchantment enchantment, int level) {}

    private int getCount(Map<EnchantmentLevel, Integer> map, Enchantment enchantment, int level) {
        for (Map.Entry<EnchantmentLevel, Integer> entry : map.entrySet()) {
            if (entry.getKey().enchantment.equals(enchantment) && entry.getKey().level == level) {
                return entry.getValue();
            }
        }
        return 0;
    }
}

