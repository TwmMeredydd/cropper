package io.github.eman7blue.cropper.entity;

import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import static io.github.eman7blue.cropper.CropperMod.MOD_ID;

public class ModEntityTypes {
    public static final EntityType<CropperMinecartEntity> CROPPER_MINECART = Registry.register(Registries.ENTITY_TYPE,
            new Identifier(MOD_ID, "cropper_minecart"),
            FabricEntityTypeBuilder.create(SpawnGroup.MISC, CropperMinecartEntity::new).dimensions(EntityDimensions.changing(0.98f, 0.7f)).build()
    );
}
