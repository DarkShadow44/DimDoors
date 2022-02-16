package org.dimdev.dimdoors.listener;

import dev.architectury.event.EventResult;
import dev.architectury.event.events.common.InteractionEvent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.dimdev.dimdoors.api.item.ExtendedItem;
import org.dimdev.dimdoors.network.client.ClientPacketHandler;
import org.dimdev.dimdoors.network.packet.c2s.HitBlockWithItemC2SPacket;

public class AttackBlockCallbackListener implements InteractionEvent.LeftClickBlock {

	@Override
	public EventResult click(PlayerEntity player, Hand hand, BlockPos pos, Direction face) {
		if (!player.world.isClient) return EventResult.pass();
		Item item = player.getStackInHand(hand).getItem();
		if (!(item instanceof ExtendedItem)) {
			return EventResult.pass();
		}
		TypedActionResult<Boolean> result = ((ExtendedItem) item).onAttackBlock(player.world, player, hand, pos, face);
		if (result.getValue()) {
			if (!ClientPacketHandler.sendPacket(new HitBlockWithItemC2SPacket(hand, pos, face))) {
				return EventResult.interruptFalse();
			}
		}

		return result.getResult() == ActionResult.SUCCESS ? EventResult.interruptTrue() : result.getResult() == ActionResult.FAIL ? EventResult.interruptFalse() : EventResult.pass();
	}
}
