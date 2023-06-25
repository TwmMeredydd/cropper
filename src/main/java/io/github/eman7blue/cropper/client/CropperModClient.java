package io.github.eman7blue.cropper.client;

import io.github.eman7blue.cropper.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.EntityModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.render.entity.MinecartEntityRenderer;
import net.minecraft.client.render.entity.model.EntityModelLayer;
import net.minecraft.client.render.entity.model.MinecartEntityModel;
import net.minecraft.util.Identifier;

import static io.github.eman7blue.cropper.CropperMod.MOD_ID;

public class CropperModClient implements ClientModInitializer {
    public static final EntityModelLayer CROPPER_MINECART_LAYER = new EntityModelLayer(new Identifier(MOD_ID, "cropper_minecart"), "main");

    @Override
    public void onInitializeClient() {
        EntityRendererRegistry.register(ModEntityTypes.CROPPER_MINECART, ctx -> new MinecartEntityRenderer<>(ctx, CROPPER_MINECART_LAYER));
        EntityModelLayerRegistry.registerModelLayer(CROPPER_MINECART_LAYER, MinecartEntityModel::getTexturedModelData);
    }
}
