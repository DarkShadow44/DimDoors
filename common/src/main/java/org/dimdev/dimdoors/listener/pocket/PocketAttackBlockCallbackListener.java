package org.dimdev.dimdoors.listener.pocket;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

import java.util.List;

public class PocketAttackBlockCallbackListener implements InteractionEvent.LeftClickBlock {
	@Override
	public EventResult click(PlayerEntity player, Hand hand, BlockPos pos, Direction direction) {
		List<InteractionEvent.LeftClickBlock> applicableAddons;
		if (player.world.isClient) applicableAddons = PocketListenerUtil.applicableAddonsClient(InteractionEvent.LeftClickBlock.class, player.world, pos);
		else applicableAddons = PocketListenerUtil.applicableAddons(InteractionEvent.LeftClickBlock.class, player.world, pos);

		EventResult result;
		for (InteractionEvent.LeftClickBlock listener : applicableAddons) {
			result = listener.click(player, hand, pos, direction);
			if (result.interruptsFurtherEvaluation()) return result;
		}
		return EventResult.pass();
	}
}
