package org.dimdev.dimdoors.block.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.dimdev.dimdoors.DimensionalDoorsInitializer;
import org.dimdev.dimdoors.block.ModBlocks;
import org.dimdev.dimdoors.pockets.PocketLoader;
import org.dimdev.dimdoors.pockets.generator.LazyPocketGenerator;
import org.dimdev.dimdoors.world.level.registry.DimensionalRegistry;
import org.dimdev.dimdoors.world.pocket.type.AbstractPocket;
import org.dimdev.dimdoors.world.pocket.type.LazyGenerationPocket;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EulerAngle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class FoldingRiftBlockEntity extends RiftBlockEntity {
    private int radius = 9;
    private boolean expanding = true;
    private boolean worldConsumed = false;
    private int time = 0;

    public FoldingRiftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntityTypes.FOLDING_RIFT, pos, state);
    }

    public static void tick(World world, BlockPos pos, BlockState state, FoldingRiftBlockEntity blockEntity) {
        if (world == null) {
            return;
        }

        if (state.getBlock() != ModBlocks.FOLDING_RIFT) {
            blockEntity.markRemoved();
            return;
        }

        if(!blockEntity.worldConsumed) {
            if (blockEntity.expanding) {
                if (MAX_TIME >= blockEntity.time) {
                    blockEntity.time += 1;
                } else {
                    blockEntity.expanding = false;
                    getAffectedPoints(world, pos, blockEntity.radius);
                }
            } else {
                if (blockEntity.time > 0) {
                    blockEntity.time -= 1;
                } else {
                    world.createExplosion(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 15, Explosion.DestructionType.NONE);
                    blockEntity.worldConsumed = true;
                }
            }
        }
    }

    @Override
    public NbtCompound serialize(NbtCompound nbt) {
        super.serialize(nbt);
        nbt.putInt("radius", radius);
        nbt.putBoolean("worldConsumed", worldConsumed);
        nbt.putBoolean("expanding", expanding);

        return nbt;
    }

    @Override
    public void deserialize(NbtCompound nbt) {
        super.deserialize(nbt);
        radius = nbt.getInt("radius");
        worldConsumed = nbt.getBoolean("worldConsumed");
        expanding = nbt.getBoolean("expanding");
    }

    public static List<BlockPos> getAffectedPoints(World world, BlockPos origin, int radius) {
        List<BlockPos> affectedPositions = new ArrayList<>();

        BlockPos newOrigin = new BlockPos(origin.getX(), 142, origin.getZ());

        for (int x = -radius+1; x < radius; x++) {
            for (int y = -radius+1; y < radius; y++) {
                for (int z = -radius+1; z < radius; z++) {
                    BlockPos pos = origin.add(x, y, z);

                    if (pos.equals(origin)) continue;

                    if (origin.isWithinDistance(pos, radius)) {
                        BlockState state = world.getBlockState(pos);

                        world.setBlockState(newOrigin.add(x, y, z), state);
                        world.setBlockState(pos, Blocks.AIR.getDefaultState());
                    } else {
                        if (!world.isAir(pos))
                            world.setBlockState(newOrigin.add(x, y, z), ModBlocks.BLACK_FABRIC.getDefaultState());
                    }
                }
            }
        }

        return affectedPositions;
    }

    public double getRadius() {
        return (time/ (double) MAX_TIME) * radius;
    }

    @Override
    public boolean receiveEntity(Entity entity, Vec3d relativePos, EulerAngle relativeAngle, Vec3d relativeVelocity) {
        return false;
    }

    @Override
    public boolean isDetached() {
        return true;
    }

    @Override
    public void setLocked(boolean locked) {

    }

    @Override
    public boolean isLocked() {
        return false;
    }

    private static final int MAX_TIME;

    static {
        MAX_TIME = 20 * 30;
    }
}
