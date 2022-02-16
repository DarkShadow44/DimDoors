package org.dimdev.dimdoors.forge;

import dev.architectury.platform.forge.EventBuses;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.dimdev.dimdoors.DimensionalDoors;

@Mod("dimdoors")
public class ExampleModForge {
    public ExampleModForge() {
		EventBuses.registerModEventBus("dimdoors", FMLJavaModLoadingContext.get().getModEventBus());
        DimensionalDoors.init();
    }
}
