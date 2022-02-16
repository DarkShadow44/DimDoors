package org.dimdev.dimdoors.listener.pocket;

import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.InteractionEvent.RightClickItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

import java.util.List;

public class UseItemCallbackListener implements RightClickItem {
	@Override
	public CompoundEventResult<ItemStack> click(PlayerEntity player, Hand hand) {
		List<RightClickItem> applicableAddons;
		if (player.world.isClient) applicableAddons = PocketListenerUtil.applicableAddonsClient(RightClickItem.class, player.world, player.getBlockPos());
		else applicableAddons = PocketListenerUtil.applicableAddons(RightClickItem.class, player.world, player.getBlockPos());

		for (RightClickItem listener : applicableAddons) {
			CompoundEventResult<ItemStack> result = listener.click(player, hand);
			if (result.result().interruptsFurtherEvaluation()) return result;
		}
		return CompoundEventResult.pass();
	}
}
