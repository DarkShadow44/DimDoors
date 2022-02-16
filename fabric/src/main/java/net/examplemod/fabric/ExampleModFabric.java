package net.examplemod.fabric;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.block.Material;

public class ExampleModFabric implements ModInitializer {
    @Override
    public void onInitialize() {
		ResourceManagerHelper.registerBuiltinResourcePack()
		ResourceManagerHelper.init();
		FabricBlockSettings.of(Material.AGGREGATE).collidable()
    }
}
