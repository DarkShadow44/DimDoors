package org.dimdev.dimdoors.block.entity;

import org.dimdev.dimdoors.api.rift.target.EntityTarget;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.state.property.Properties;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import qouteall.imm_ptl.core.portal.Portal;

public class PortalHelper {

	Portal portal = null;
	boolean hasPortal = false;

	public boolean getHasPortal() {
		return hasPortal;
	}

	public String getPortalId() {
		if (hasPortal) {
			return portal.getUuid().toString();
		}
		return null;
	}
	
	public void loadPortal(Entity portal) {
		this.portal = (Portal)portal;
	}

	public void setHasPortal(boolean hasPortal) {
		this.hasPortal = hasPortal;
	}

	public void destroyPortal() {
		if (!hasPortal || portal == null) {
			return;
		}
		portal.remove(RemovalReason.KILLED);
		portal = null;
		hasPortal = false;
	}

	public String createPortal(EntityTarget target, World srcWorld, BlockPos srcPosBlock, Direction srcOrientation) {
		EntranceRiftBlockEntity dstEntity = target.as(EntranceRiftBlockEntity.class);
		if (dstEntity == null)
			return null;
		
		World dstWorld = dstEntity.getWorld();
		BlockPos dstPosBlock = dstEntity.getPos();
		
		BlockState srcState = srcWorld.getBlockState(srcPosBlock);
		BlockState state = dstWorld.getBlockState(dstPosBlock);
		state = state.rotate(BlockRotation.CLOCKWISE_180);
		if (state.contains(Properties.DOOR_HINGE)) {
			state = state.with(Properties.DOOR_HINGE, srcState.get(Properties.DOOR_HINGE));
		}
		dstWorld.setBlockState(dstPosBlock, state);
		
		Direction dstOrientation = target.getDoorTargetOrientation();

		if (dstOrientation == null) {
			dstOrientation = Direction.NORTH;
		}

		Vec3d srcPos = Vec3d.ofCenter(srcPosBlock).add(0, 0.5, 0);
		srcPos = srcPos.add(Vec3d.of(srcOrientation.getOpposite().getVector()).multiply(0.3));
		Vec3d targetPos = Vec3d.ofCenter(dstPosBlock)
				.add(Vec3d.of(dstOrientation.getOpposite().getVector()).multiply(0.3)).add(0, 0.5, 0);

		portal = Portal.entityType.create(srcWorld);
		portal.setOriginPos(srcPos);
		portal.setDestinationDimension(dstWorld.getRegistryKey());
		portal.setDestination(targetPos);
		Vec3d axis = Vec3d.of(srcOrientation.rotateClockwise(Axis.Y).getVector());
		portal.setOrientationAndSize(axis, new Vec3d(0, 1, 0), 1, 2);
		float rotFloat = srcOrientation.asRotation() - dstOrientation.asRotation();
		Quaternion rot = Quaternion.fromEulerXyzDegrees(new Vec3f(0, rotFloat, 0));
		portal.setRotationTransformation(rot);
		portal.world.spawnEntity(portal);
		hasPortal = true;
	}

}
