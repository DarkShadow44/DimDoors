package org.dimdev.dimdoors.block.entity;

import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.dimdev.dimdoors.DimensionalDoorsInitializer;
import org.dimdev.dimdoors.api.client.DefaultTransformation;
import org.dimdev.dimdoors.api.client.Transformer;
import org.dimdev.dimdoors.api.util.EntityUtils;
import org.dimdev.dimdoors.api.util.TeleportUtil;
import org.dimdev.dimdoors.api.util.math.TransformationMatrix3d;
import org.dimdev.dimdoors.block.CoordinateTransformerBlock;
import org.dimdev.dimdoors.block.RiftProvider;
import org.dimdev.dimdoors.item.RiftKeyItem;
import org.dimdev.dimdoors.pockets.DefaultDungeonDestinations;
import org.dimdev.dimdoors.rift.registry.Rift;
import org.dimdev.dimdoors.rift.targets.EscapeTarget;
import org.dimdev.dimdoors.rift.targets.Targets;
import org.dimdev.dimdoors.world.ModDimensions;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalFacingBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class EntranceRiftBlockEntity extends RiftBlockEntity {
	private static final EscapeTarget ESCAPE_TARGET = new EscapeTarget(true);
	private static final Logger LOGGER = LogManager.getLogger();
	private boolean locked;
	private Direction originalOrientation;
	private String portalId = null;
	private static PortalHelper portalHelper;

	public EntranceRiftBlockEntity(BlockPos pos, BlockState state) {
		super(ModBlockEntityTypes.ENTRANCE_RIFT, pos, state);

		if (portalHelper == null && FabricLoader.getInstance().isModLoaded("imm_ptl_core")) {
			portalHelper = new PortalHelper();
		}
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		locked = nbt.getBoolean("locked");
		portalId = nbt.getString("portal_id");
		if (portalId == "") {
			portalId = null;
		}
		String originalOrientationStr = nbt.getString("original_direction");
		if (originalOrientationStr != null) {
			originalOrientation = Direction.byName(originalOrientationStr);
		}
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		nbt.putBoolean("locked", locked);
		if (portalId != null) {
			nbt.putString("portal_id", portalId);
		}
		if (originalOrientation != null) {
			nbt.putString("original_direction", originalOrientation.getName());
		}
		super.writeNbt(nbt);
	}

	@Override
	public boolean teleport(Entity entity) {
		//Sets the location where the player should be teleported back to if they are in limbo and try to escape, to be the entrance of the rift that took them into dungeons.

		if (this.isLocked()) {
			if (entity instanceof LivingEntity) {
				ItemStack stack = ((LivingEntity) entity).getStackInHand(((LivingEntity) entity).getActiveHand());
				Rift rift = this.asRift();

				if (RiftKeyItem.has(stack, rift.getId())) {
					return innerTeleport(entity);
				}

				EntityUtils.chat(entity, new TranslatableText("rifts.isLocked"));
			}
			return false;
		}

		return innerTeleport(entity);
	}

	private boolean innerTeleport(Entity entity) {
		boolean status = super.teleport(entity);

		if (this.riftStateChanged && !this.data.isAlwaysDelete()) {
			this.markDirty();
		}

		return status;
	}

	@Override
	public boolean receiveEntity(Entity entity, Vec3d relativePos, EulerAngle relativeAngle, Vec3d relativeVelocity) {
		BlockState state = this.getWorld().getBlockState(this.getPos());
		Block block = state.getBlock();
		Vec3d targetPos = Vec3d.ofCenter(this.pos).add(Vec3d.of(this.getOrientation().getOpposite().getVector()).multiply(DimensionalDoorsInitializer.getConfig().getGeneralConfig().teleportOffset + 0.01/* slight offset to prevent issues due to mathematical inaccuracies*/));
		/*
		Unused code that needs to be edited if there are other ways to get to limbo
		But if it is only dimteleport and going through rifts then this code isn't nessecary
		if(DimensionalRegistry.getRiftRegistry().getOverworldRift(entity.getUuid()) == null) {
			DimensionalRegistry.getRiftRegistry().setOverworldRift(entity.getUuid(), new Location(World.OVERWORLD, ((ServerPlayerEntity)entity).getSpawnPointPosition()));
		}
		 */
		if (block instanceof CoordinateTransformerBlock) {
			CoordinateTransformerBlock transformer = (CoordinateTransformerBlock) block;

			if (transformer.isExitFlipped()) {
				TransformationMatrix3d flipper = TransformationMatrix3d.builder().rotateY(Math.PI).build();

				relativePos = flipper.transform(relativePos);
				relativeAngle = flipper.transform(relativeAngle);
				relativeVelocity = flipper.transform(relativeVelocity);
			}

			TransformationMatrix3d.TransformationMatrix3dBuilder transformationBuilder = transformer.transformationBuilder(state, this.getPos());
			TransformationMatrix3d.TransformationMatrix3dBuilder rotatorBuilder = transformer.rotatorBuilder(state, this.getPos());
			targetPos = transformer.transformOut(transformationBuilder, relativePos);
			relativeAngle = transformer.rotateOut(rotatorBuilder, relativeAngle);
			relativeVelocity = transformer.rotateOut(rotatorBuilder, relativeVelocity);
		}

		// TODO: open door

		TeleportUtil.teleport(entity, this.world, targetPos, relativeAngle, relativeVelocity);

		return true;
	}

	public Direction getOrientation() {
		//noinspection ConstantConditions
		return Optional.of(this.world.getBlockState(this.pos))
				.filter(state -> state.contains(HorizontalFacingBlock.FACING))
				.map(state -> state.get(HorizontalFacingBlock.FACING))
				.orElse(Direction.NORTH);
	}

	@Environment(EnvType.CLIENT)
	public Transformer getTransformer() {
		return DefaultTransformation.fromDirection(this.getOrientation());
	}

	public boolean hasOrientation() {
		return this.world != null && this.world.getBlockState(this.pos).contains(HorizontalFacingBlock.FACING);
	}

	/**
	 * Specifies if the portal should be rendered two blocks tall
	 */
	@Environment(EnvType.CLIENT)
	public boolean isTall() {
		return ((RiftProvider<?>) this.getCachedState().getBlock()).isTall(this.getCachedState());
	}

	@Override
	public boolean isDetached() {
		return false;
	}

	@Override
	public boolean isLocked() {
		return locked;
	}

	@Override
	public void setLocked(boolean locked) {
		this.locked = locked;
	}

	public void setPortalDestination(ServerWorld world) {
		if (ModDimensions.isLimboDimension(world)) {
			this.setDestination(ESCAPE_TARGET);
		} else {
			this.setDestination(DefaultDungeonDestinations.getGateway());
			this.setProperties(DefaultDungeonDestinations.POCKET_LINK_PROPERTIES);
		}
	}

	public void tryCreatePortal() {
		if (world.isClient || portalHelper == null || portalId != null)
			return;

		portalId = portalHelper.createPortal(this, this.getTarget().as(Targets.ENTITY));
		BlockState state = world.getBlockState(pos);
		world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
	}

	public boolean hasPortal() {
		return portalHelper != null && portalId != null;
	}

	public Direction getOriginalOrientation() {
		if (originalOrientation == null) {
			originalOrientation = this.getOrientation();
			BlockState state = world.getBlockState(pos);
			world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
		}
		return originalOrientation;
	}

	public void tryDestroyPortal() {
		if (!world.isClient && portalHelper != null) {
			portalHelper.destroyPortal((ServerWorld)world, portalId);
			portalId = null;
			originalOrientation = null;
			BlockState state = world.getBlockState(pos);
			world.updateListeners(pos, state, state, Block.NOTIFY_LISTENERS);
		}
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		NbtCompound tag = createNbt();
		writeNbt(tag);
		return tag;
	}
}
