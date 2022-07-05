package org.dimdev.dimdoors.block.entity;

import java.util.UUID;

import org.dimdev.dimdoors.api.rift.target.EntityTarget;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import qouteall.imm_ptl.core.portal.Portal;

public class PortalHelper {
	public void destroyPortal(ServerWorld world, String portalId) {
		if (portalId == null) {
			return;
		}
		Entity entity = world.getEntity(UUID.fromString(portalId));
		if (entity == null) {
			return;
		}
		((Portal) entity).remove(RemovalReason.KILLED);
	}

	public String createPortal(EntranceRiftBlockEntity srcEntity, EntityTarget target) {
		if (! (target instanceof EntranceRiftBlockEntity))
			return null;
		
		EntranceRiftBlockEntity dstEntity = (EntranceRiftBlockEntity)target;

		World srcWorld = srcEntity.getWorld();
		BlockPos srcPosBlock = srcEntity.getPos();
		Direction srcOrientation = srcEntity.getOriginalOrientation();

		World dstWorld = dstEntity.getWorld();
		BlockPos dstPosBlock = dstEntity.getPos();

		Direction dstOrientation = dstEntity.getOriginalOrientation().getOpposite();

		Direction srcPosOrientation = srcOrientation;
		Direction dstPosOrientation = dstOrientation;
		if (dstEntity.hasPortal()) {
			srcPosOrientation = srcPosOrientation.getOpposite();
			dstPosOrientation = dstPosOrientation.getOpposite();
		}

		Vec3d srcPos = Vec3d.ofCenter(srcPosBlock).add(0, 0.5, 0);
		srcPos = srcPos.add(Vec3d.of(srcPosOrientation.getOpposite().getVector()).multiply(0.3));
		Vec3d targetPos = Vec3d.ofCenter(dstPosBlock)
				.add(Vec3d.of(dstPosOrientation.getOpposite().getVector()).multiply(0.3)).add(0, 0.5, 0);

		Portal portal = Portal.entityType.create(srcWorld);
		portal.setOriginPos(srcPos);
		portal.setDestinationDimension(dstWorld.getRegistryKey());
		portal.setDestination(targetPos);
		Vec3d axis = Vec3d.of(srcOrientation.rotateClockwise(Axis.Y).getVector());
		portal.setOrientationAndSize(axis, new Vec3d(0, 1, 0), 1, 2);
		float rotFloat = srcOrientation.asRotation() - dstOrientation.asRotation();
		Quaternion rot = Quaternion.fromEulerXyzDegrees(new Vec3f(0, rotFloat, 0));
		portal.setRotationTransformation(rot);
		portal.world.spawnEntity(portal);

		if (!dstEntity.hasPortal()) { // Only rotate target if we create the first portal (aka an entrance)
			// Rotate target door the right way
			BlockState srcState = srcWorld.getBlockState(srcPosBlock);
			BlockState state = dstWorld.getBlockState(dstPosBlock);
			state = state.with(DoorBlock.FACING, dstOrientation);
			if (state.contains(Properties.DOOR_HINGE)) {
				state = state.with(Properties.DOOR_HINGE, srcState.get(Properties.DOOR_HINGE));
			}
			dstWorld.setBlockState(dstPosBlock, state);
		}

		return portal.getUuidAsString();
	}

}
