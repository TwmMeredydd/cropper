package io.github.eman7blue.cropper.block;

import net.minecraft.block.*;
import net.minecraft.block.entity.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventories;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.SidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Nameable;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.stream.IntStream;

public class CropperBlockEntity extends BlockEntity implements Hopper, Inventory, NamedScreenHandlerFactory, Nameable {
    private DefaultedList<ItemStack> inventory;
    private int transferCooldown;
    private Text customName;
    private long lastTickTime;

    public CropperBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlocks.CROPPER_BLOCK_ENTITY_TYPE, pos, state);
        this.inventory = DefaultedList.ofSize(5, ItemStack.EMPTY);
        this.transferCooldown = -1;
    }

    public void readNbt(NbtCompound nbt) {
        super.readNbt(nbt);
        this.inventory = DefaultedList.ofSize(this.size(), ItemStack.EMPTY);
        Inventories.readNbt(nbt, this.inventory);
        this.transferCooldown = nbt.getInt("TransferCooldown");
    }

    protected void writeNbt(NbtCompound nbt) {
        super.writeNbt(nbt);
        Inventories.writeNbt(nbt, this.inventory);
        nbt.putInt("TransferCooldown", this.transferCooldown);
    }

    public double getHopperX() {
        return ((double) this.pos.getX()) + 0.5;
    }

    public double getHopperY() {
        return ((double) this.pos.getY()) + 0.5;
    }

    public double getHopperZ() {
        return ((double) this.pos.getZ()) + 0.5;
    }

    @Override
    public int size() {
        return this.inventory.size();
    }

    @Override
    public boolean isEmpty() {
        return this.inventory.isEmpty();
    }

    @Override
    public ItemStack getStack(int slot) {
        return this.inventory.get(slot);
    }

    @Override
    public ItemStack removeStack(int slot, int amount) {
        return Inventories.splitStack(this.inventory, slot, amount);
    }

    @Override
    public ItemStack removeStack(int slot) {
        return Inventories.splitStack(this.inventory, slot, 1);
    }

    @Override
    public void setStack(int slot, ItemStack stack) {
        this.inventory.set(slot, stack);
        if (stack.getCount() > this.getMaxCountPerStack()) {
            stack.setCount(this.getMaxCountPerStack());
        }
    }

    @Override
    public boolean canPlayerUse(PlayerEntity player) {
        return true;
    }

    @Override
    public void clear() {
        this.inventory.clear();
    }


    @Override
    public Text getName() {
        return customName != null ? customName : this.getDisplayName();
    }

    public void setCustomName(Text customName) {
        this.customName = customName;
    }

    @Override
    public Text getDisplayName() {
        return Text.translatable("container.cropper");
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new HopperScreenHandler(syncId, playerInventory, this);
    }

    public static void serverTick(World world, BlockPos pos, BlockState state, CropperBlockEntity blockEntity) {
        --blockEntity.transferCooldown;
        blockEntity.lastTickTime = world.getTime();
        if (blockEntity.noCooldown()) {
            blockEntity.setTransferCooldown(0);
            insertAndExtract(world, pos, state, blockEntity, () -> extract(world, blockEntity));
        }
    }

    private boolean noCooldown() {
        return this.transferCooldown <= 0;
    }

    private void setTransferCooldown(int transferCooldown) {
        this.transferCooldown = transferCooldown;
    }

    private static void insertAndExtract(World world, BlockPos pos, BlockState state, CropperBlockEntity blockEntity, BooleanSupplier booleanSupplier) {
        if (!world.isClient) {
            if (blockEntity.noCooldown() && state.get(HopperBlock.ENABLED)) {
                boolean dirty = false;
                if (!blockEntity.isEmpty()) {
                    dirty = insert(world, pos, state, blockEntity);
                }

                if (!blockEntity.isFull()) {
                    dirty |= booleanSupplier.getAsBoolean();
                }

                if (dirty) {
                    blockEntity.setTransferCooldown(8);
                    markDirty(world, pos, state);
                }
            }

        }
    }

    private boolean isFull() {
        Iterator<ItemStack> stackIterator = this.inventory.iterator();

        ItemStack itemStack;
        do {
            if (!stackIterator.hasNext()) {
                return true;
            }

            itemStack = stackIterator.next();
        } while(!itemStack.isEmpty() && itemStack.getCount() == itemStack.getMaxCount());

        return false;
    }

    private static boolean insert(World world, BlockPos pos, BlockState state, Inventory inventory) {
        Inventory outputInventory = getOutputInventory(world, pos, state);
        if (outputInventory != null) {
            Direction direction = state.get(HopperBlock.FACING).getOpposite();
            if (!isInventoryFull(outputInventory, direction)) {
                for (int i = 0; i < inventory.size(); ++i) {
                    if (!inventory.getStack(i).isEmpty()) {
                        ItemStack itemStack = inventory.getStack(i).copy();
                        ItemStack itemStack2 = transfer(inventory, outputInventory, inventory.removeStack(i, 1), direction);
                        if (itemStack2.isEmpty()) {
                            outputInventory.markDirty();
                            return true;
                        }

                        inventory.setStack(i, itemStack);
                    }
                }

            }
        }
        return false;
    }

    private static IntStream getAvailableSlots(Inventory inventory, Direction side) {
        return inventory instanceof SidedInventory ? IntStream.of(((SidedInventory)inventory).getAvailableSlots(side)) : IntStream.range(0, inventory.size());
    }

    private static boolean isInventoryFull(Inventory inventory, Direction direction) {
        return getAvailableSlots(inventory, direction).allMatch((slot) -> {
            ItemStack itemStack = inventory.getStack(slot);
            return itemStack.getCount() >= itemStack.getMaxCount();
        });
    }

    public static boolean extract(World world, Hopper hopper) {
        BlockPos farmPos = BlockPos.ofFloored(hopper.getHopperX(), hopper.getHopperY() + 1.0, hopper.getHopperZ());
        if (world.getBlockState(farmPos).isOf(Blocks.FARMLAND)) {
            BlockPos cropPos = farmPos.up();
            BlockState cropState = world.getBlockState(cropPos);
            if (world.getBlockState(cropPos).getBlock() instanceof CropBlock cropBlock) {
                if (cropBlock.getMaxAge() == cropState.get(cropBlock.getAgeProperty())) {
                    LootContext.Builder builder = new LootContext.Builder((ServerWorld) world)
                            .parameter(LootContextParameters.TOOL, ItemStack.EMPTY)
                            .parameter(LootContextParameters.ORIGIN, farmPos.down().toCenterPos());
                    List<ItemStack> stacks = cropState.getDroppedStacks(builder);
                    for (ItemStack stack : stacks) {
                        ItemStack stack2 = transfer(null, hopper, stack, null);
                        if (!stack2.isEmpty()) {
                            ItemEntity entity = new ItemEntity(world, cropPos.getX(), cropPos.getY(), cropPos.getZ(), stack);
                            world.spawnEntity(entity);
                        }
                    }
                    world.breakBlock(cropPos, false);
                    return true;
                }
            }
        }
        return false;
    }

    public static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, @Nullable Direction side) {
        int i;
        if (to instanceof SidedInventory sidedInventory) {
            if (side != null) {
                int[] is = sidedInventory.getAvailableSlots(side);

                for(i = 0; i < is.length && !stack.isEmpty(); ++i) {
                    stack = transfer(from, to, stack, is[i], side);
                }

                return stack;
            }
        }

        int j = to.size();

        for(i = 0; i < j && !stack.isEmpty(); ++i) {
            stack = transfer(from, to, stack, i, side);
        }

        return stack;
    }

    private static boolean canInsert(Inventory inventory, ItemStack stack, int slot, @Nullable Direction side) {
        if (!inventory.isValid(slot, stack)) {
            return false;
        } else {
            if (inventory instanceof SidedInventory sidedInventory) {
                return sidedInventory.canInsert(slot, stack, side);
            }

            return true;
        }
    }

    private static ItemStack transfer(@Nullable Inventory from, Inventory to, ItemStack stack, int slot, @Nullable Direction side) {
        ItemStack itemStack = to.getStack(slot);
        if (canInsert(to, stack, slot, side)) {
            boolean bl = false;
            boolean bl2 = to.isEmpty();
            if (itemStack.isEmpty()) {
                to.setStack(slot, stack);
                stack = ItemStack.EMPTY;
                bl = true;
            } else if (canMergeItems(itemStack, stack)) {
                int i = stack.getMaxCount() - itemStack.getCount();
                int j = Math.min(stack.getCount(), i);
                stack.decrement(j);
                itemStack.increment(j);
                bl = j > 0;
            }

            if (bl) {
                if (bl2 && to instanceof CropperBlockEntity cropperBlockEntity) {
                    if (!cropperBlockEntity.isDisabled()) {
                        int j = 0;
                        if (from instanceof CropperBlockEntity cropperBlockEntity2) {
                            if (cropperBlockEntity.lastTickTime >= cropperBlockEntity2.lastTickTime) {
                                j = 1;
                            }
                        }

                        cropperBlockEntity.setTransferCooldown(8 - j);
                    }
                }

                to.markDirty();
            }
        }

        return stack;
    }

    private boolean isDisabled() {
        return this.transferCooldown > 8;
    }

    @Nullable
    private static Inventory getOutputInventory(World world, BlockPos pos, BlockState state) {
        Direction direction = state.get(HopperBlock.FACING);
        return getInventoryAt(world, pos.offset(direction));
    }

    @Nullable
    public static Inventory getInventoryAt(World world, BlockPos pos) {
        return getInventoryAt(world, (double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5);
    }

    @Nullable
    private static Inventory getInventoryAt(World world, double x, double y, double z) {
        Inventory inventory = null;
        BlockPos blockPos = BlockPos.ofFloored(x, y, z);
        BlockState blockState = world.getBlockState(blockPos);
        Block block = blockState.getBlock();
        if (block instanceof InventoryProvider) {
            inventory = ((InventoryProvider)block).getInventory(blockState, world, blockPos);
        } else if (blockState.hasBlockEntity()) {
            BlockEntity blockEntity = world.getBlockEntity(blockPos);
            if (blockEntity instanceof Inventory) {
                inventory = (Inventory)blockEntity;
                if (inventory instanceof ChestBlockEntity && block instanceof ChestBlock) {
                    inventory = ChestBlock.getInventory((ChestBlock)block, blockState, world, blockPos, true);
                }
            }
        }

        if (inventory == null) {
            List<Entity> list = world.getOtherEntities(null, new Box(x - 0.5, y - 0.5, z - 0.5, x + 0.5, y + 0.5, z + 0.5), EntityPredicates.VALID_INVENTORIES);
            if (!list.isEmpty()) {
                inventory = (Inventory)list.get(world.random.nextInt(list.size()));
            }
        }

        return inventory;
    }

    private static boolean canMergeItems(ItemStack first, ItemStack second) {
        if (!first.isOf(second.getItem())) {
            return false;
        } else if (first.getDamage() != second.getDamage()) {
            return false;
        } else {
            return first.getCount() <= first.getMaxCount() && ItemStack.areNbtEqual(first, second);
        }
    }

}
