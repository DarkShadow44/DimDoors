package org.dimdev.dimdoors.block;

import dev.architectury.registry.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.util.DyeColor;

public class AncientFabricBlock extends Block {
	public AncientFabricBlock(DyeColor color) {
		super(BlockProperties.of(Material.STONE, color).strength(-1.0F, 3600000.0F).dropsNothing());
	}
}
