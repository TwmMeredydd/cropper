package io.github.eman7blue.cropper.item;

import io.github.eman7blue.cropper.block.ModBlocks;
import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static io.github.eman7blue.cropper.CropperMod.MOD_ID;

public class ModItems {
    public static final Item CROPPER;

    public static void registerItems() {
        Registry.register(Registries.ITEM, new Identifier(MOD_ID, "cropper"), CROPPER);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.REDSTONE).register(content -> {
            content.addAfter(Items.HOPPER, CROPPER);
        });
    }

    static {
        CROPPER = new BlockItem(ModBlocks.CROPPER_BLOCK, new FabricItemSettings());
    }
}
