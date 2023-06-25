package io.github.eman7blue.cropper.entity;

import io.github.eman7blue.cropper.block.CropperBlockEntity;
import io.github.eman7blue.cropper.block.ModBlocks;
import io.github.eman7blue.cropper.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.HopperMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class CropperMinecartEntity extends HopperMinecartEntity {
    public CropperMinecartEntity(EntityType<? extends HopperMinecartEntity> entityType, World world) {
        super(entityType, world);
    }

    @Override
    protected Item getItem() {
        return ModItems.CROPPER_MINECART;
    }

    @Override
    public Type getMinecartType() {
        return null;
    }

    @Override
    public BlockState getDefaultContainedBlock() {
        return ModBlocks.CROPPER_BLOCK.getDefaultState();
    }

    public boolean isFull() {
        for (ItemStack stack : this.getInventory()) {
            if (stack.getCount() < stack.getMaxCount()) return false;
        }
        return true;
    }

    @Override
    public boolean canOperate() {
        return isFull() ? true : CropperBlockEntity.extract(world, this);
    }

}
