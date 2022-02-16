package org.dimdev.dimdoors.block;

import dev.architectury.hooks.tags.TagHooks;
import dev.architectury.registry.block.BlockProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.tag.Tag;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

public class FabricBlock extends Block {
	public static final Tag<Block> BLOCK_TAG = TagHooks.optionalBlock(new Identifier("dimdoors", "fabric"));

	FabricBlock(DyeColor color) {
		super(BlockProperties.of(Material.STONE, color).strength(1.2F).luminance(state -> 15));
	}

	@Override
	public boolean canReplace(BlockState state, ItemPlacementContext context) {
		if (context.getPlayer().isSneaking()) return false;
		Block heldBlock = Block.getBlockFromItem(context.getPlayer().getStackInHand(context.getHand()).getItem());
		if (!heldBlock.getDefaultState().isFullCube(context.getWorld(), context.getBlockPos())) return false;
		if (heldBlock instanceof BlockEntityProvider || heldBlock instanceof FabricBlock) return false;

		return true;
	}
}
