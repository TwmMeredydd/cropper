package io.github.eman7blue.cropper.block;

import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

import static io.github.eman7blue.cropper.CropperMod.MOD_ID;

public class ModBlocks {

    public static final CropperBlock CROPPER_BLOCK;
    public static final BlockEntityType<CropperBlockEntity> CROPPER_BLOCK_ENTITY_TYPE;

    public static void registerBlocks() {
        Registry.register(Registries.BLOCK, new Identifier(MOD_ID, "cropper"), CROPPER_BLOCK);
        Registry.register(Registries.BLOCK_ENTITY_TYPE, new Identifier(MOD_ID, "cropper"), CROPPER_BLOCK_ENTITY_TYPE);
    }

    static {
        CROPPER_BLOCK = new CropperBlock(FabricBlockSettings.of(Material.SOLID_ORGANIC, MapColor.YELLOW)
                .requiresTool()
                .strength(0.6F)
                .sounds(BlockSoundGroup.GRASS)
                .nonOpaque());
        CROPPER_BLOCK_ENTITY_TYPE = FabricBlockEntityTypeBuilder.create(CropperBlockEntity::new, CROPPER_BLOCK).build();
    }
}
