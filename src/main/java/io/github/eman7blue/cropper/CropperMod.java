package io.github.eman7blue.cropper;

import io.github.eman7blue.cropper.block.ModBlocks;
import io.github.eman7blue.cropper.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CropperMod implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("modid");
	public static final String MOD_ID = "cropper";

	@Override
	public void onInitialize() {
		ModBlocks.registerBlocks();
		ModItems.registerItems();
		LOGGER.info("Cropper is a cropular mod!");
	}
}
