package org.dimdev.dimdoors.listener.pocket;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent.LeftClickBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class UseBlockCallbackListener implements LeftClickBlock {

	@Override
	public EventResult click(PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
		List<LeftClickBlock> applicableAddons;
		if (player.world.isClient) applicableAddons = PocketListenerUtil.applicableAddonsClient(LeftClickBlock.class, player.world, player.getBlockPos());
		else applicableAddons = PocketListenerUtil.applicableAddons(LeftClickBlock.class, player.world, player.getBlockPos());

		for (LeftClickBlock listener : applicableAddons) {
			EventResult result = listener.click(player, hand, pos, face);
			if (result.interruptsFurtherEvaluation()) return result;
		}
		return EventResult.pass();
	}
}
