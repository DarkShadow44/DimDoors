package org.dimdev.dimdoors.block.entity;

import java.util.UUID;

import org.dimdev.dimdoors.api.rift.target.EntityTarget;
import org.dimdev.dimdoors.rift.targets.Targets;

import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.Entity.RemovalReason;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Pair;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;
import qouteall.imm_ptl.core.portal.Portal;

public class PortalHelper {
	private void destroyPortalById(ServerWorld world, String portalId) {
		Entity entity = world.getEntity(UUID.fromString(portalId));
		if (entity == null) {
			return;
		}
		((Portal) entity).remove(RemovalReason.KILLED);
	}

	public void destroyOtherPortal(EntranceRiftBlockEntity srcEntity) {
		EntityTarget target = srcEntity.getTarget().as(Targets.ENTITY);
		if (target instanceof EntranceRiftBlockEntity) {
			EntranceRiftBlockEntity dstEntity = (EntranceRiftBlockEntity) target;
			dstEntity.tryDestroyPortal();
		}
	}

	public void destroyPortal(EntranceRiftBlockEntity srcEntity, ServerWorld world, Pair<String, String> portalIds) {
		if (portalIds == null) {
			return;
		}

		destroyPortalById(world, portalIds.getLeft());
		destroyPortalById(world, portalIds.getRight());
	}

	public void createOtherPortal(EntranceRiftBlockEntity srcEntity) {
		EntityTarget target = srcEntity.getTarget().as(Targets.ENTITY);
		if (!(target instanceof EntranceRiftBlockEntity))
			return;
		EntranceRiftBlockEntity dstEntity = (EntranceRiftBlockEntity) target;
		dstEntity.tryCreatePortal();
	}

	public Pair<String, String> createPortal(EntranceRiftBlockEntity srcEntity) {
		EntityTarget target = srcEntity.getTarget().as(Targets.ENTITY);
		if (!(target instanceof EntranceRiftBlockEntity))
			return null;

		EntranceRiftBlockEntity dstEntity = (EntranceRiftBlockEntity) target;

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

		float portalDistance = 0.32f;

		Vec3d srcPos = Vec3d.ofCenter(srcPosBlock).add(0, 0.5, 0);
		srcPos = srcPos.add(Vec3d.of(srcPosOrientation.getOpposite().getVector()).multiply(portalDistance));
		Vec3d targetPos = Vec3d.ofCenter(dstPosBlock)
				.add(Vec3d.of(dstPosOrientation.getOpposite().getVector()).multiply(portalDistance)).add(0, 0.5, 0);

		Portal portal1 = Portal.entityType.create(srcWorld);
		Portal portal2 = Portal.entityType.create(srcWorld);
		portal1.setOriginPos(srcPos);
		portal2.setOriginPos(srcPos);
		portal1.setDestinationDimension(dstWorld.getRegistryKey());
		portal2.setDestinationDimension(dstWorld.getRegistryKey());
		portal1.setDestination(targetPos);
		portal2.setDestination(targetPos);
		Vec3d axis = Vec3d.of(srcOrientation.rotateClockwise(Axis.Y).getVector());
		portal1.setOrientationAndSize(axis, new Vec3d(0, 1, 0), 1, 2);
		portal2.setOrientationAndSize(axis.multiply(-1), new Vec3d(0, 1, 0), 1, 2);
		float rotFloat = srcOrientation.asRotation() - dstOrientation.asRotation();
		Quaternion rot = Quaternion.fromEulerXyzDegrees(new Vec3f(0, rotFloat, 0));
		portal1.setRotationTransformation(rot);
		portal2.setRotationTransformation(rot);
		portal1.world.spawnEntity(portal1);
		portal2.world.spawnEntity(portal2);

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

		return new Pair<>(portal1.getUuidAsString(), portal2.getUuidAsString());
	}

}
