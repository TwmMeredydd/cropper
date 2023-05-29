package io.github.eman7blue.cropper.datagen;

import io.github.eman7blue.cropper.item.ModItems;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.advancement.criterion.InventoryChangedCriterion;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapedRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;

import java.util.function.Consumer;

public class DataGen implements DataGeneratorEntrypoint {

    @Override
    public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
        FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
        pack.addProvider(CropperRecipeGenerator::new);
    }

    private static class CropperRecipeGenerator extends FabricRecipeProvider {

        public CropperRecipeGenerator(FabricDataOutput output) {
            super(output);
        }

        @Override
        public void generate(Consumer<RecipeJsonProvider> exporter) {
            ShapedRecipeJsonBuilder.create(RecipeCategory.REDSTONE, ModItems.CROPPER)
                    .criterion("has_hopper_and_hay", InventoryChangedCriterion.Conditions.items(Items.HOPPER, Items.HAY_BLOCK))
                    .input('H', Items.HAY_BLOCK)
                    .input('P', Items.HOPPER)
                    .pattern("H H")
                    .pattern("HPH")
                    .pattern(" H ")
                    .offerTo(exporter);
        }
    }
}
