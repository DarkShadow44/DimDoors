package org.dimdev.dimdoors.entity;

import static org.dimdev.dimdoors.block.entity.FoldingRiftBlockEntity.getAffectedPoints;

import java.util.Optional;
import java.util.UUID;

import org.dimdev.dimdoors.util.TrackDataHandlers;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.server.ServerConfigHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.explosion.Explosion;

public class FoldingEntity extends Entity {
    private static final int MAX_TIME = 20 * 12;

    protected static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(TameableEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
    protected static final TrackedData<Vec3d> OWNER_POS = DataTracker.registerData(FoldingEntity.class, TrackDataHandlers.VEC3D);
    protected static final TrackedData<Integer> RADIUS = DataTracker.registerData(FoldingEntity.class, TrackedDataHandlerRegistry.INTEGER);
    protected static final TrackedData<Boolean> WORLD_CONSUMED = DataTracker.registerData(FoldingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<Boolean> EXPANDING = DataTracker.registerData(FoldingEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
    protected static final TrackedData<Integer> TIME = DataTracker.registerData(FoldingEntity.class, TrackedDataHandlerRegistry.INTEGER);

    public FoldingEntity(EntityType<?> type, World world) {
        super(type, world);
    }

    public static FoldingEntity create(World world, PlayerEntity owner) {
        FoldingEntity entity = ModEntityTypes.FOLDING.create(world);
        entity.setOwner(owner);

        return entity;
    }

    protected void initDataTracker() {
        this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
        this.dataTracker.startTracking(OWNER_POS, Vec3d.ZERO);
        this.dataTracker.startTracking(RADIUS, 9);
        this.dataTracker.startTracking(EXPANDING, true);
        this.dataTracker.startTracking(WORLD_CONSUMED, false);
        this.dataTracker.startTracking(TIME, 0);
    }

    public void writeCustomDataToNbt(NbtCompound nbt) {
        if (this.getOwnerUuid() != null) {
            nbt.putUuid("Owner", this.getOwnerUuid());
        }

        Vec3d pos = getOwnerPos();

        NbtCompound posNbt = new NbtCompound();
        nbt.putDouble("x", pos.x);
        nbt.putDouble("y", pos.y);
        nbt.putDouble("z", pos.z);

        nbt.put("ownerPos", posNbt);

        nbt.putInt("radius", getRadius());
        nbt.putBoolean("worldConsumed", isWorldConsumed());
        nbt.putBoolean("expanding", isExpanding());
        nbt.putInt("time", getTime());
    }

    private int getTime() {
        return this.dataTracker.get(TIME);
    }

    public boolean isExpanding() {
        return this.dataTracker.get(EXPANDING);
    }

    public boolean isWorldConsumed() {
        return this.dataTracker.get(WORLD_CONSUMED);
    }

    public int getRadius() {
        return this.dataTracker.get(RADIUS);
    }

    public double getRenderRadius() {
        return (getTime() / (double) MAX_TIME) * getRadius();
    }

    public Vec3d getOwnerPos() {
        return this.dataTracker.get(OWNER_POS);
    }

    public void readCustomDataFromNbt(NbtCompound nbt) {
        UUID uuid;
        if (nbt.containsUuid("Owner")) {
            uuid = nbt.getUuid("Owner");
        } else {
            String string = nbt.getString("Owner");
            uuid = ServerConfigHandler.getPlayerUuidByName(this.getServer(), string);
        }

        if (uuid != null) {
            try {
                this.setOwnerUuid(uuid);
            } catch (Throwable ignored) {
            }
        }

        setRadius(nbt.getInt("radius"));
        setWorldConsumed(nbt.getBoolean("worldConsumed"));
        setExpanding(nbt.getBoolean("expanding"));

        NbtCompound nbtPos = nbt.getCompound("ownerPos");

        setOwnerPos(new Vec3d(nbtPos.getDouble("x"), nbtPos.getDouble("y"), nbtPos.getDouble("z")));

        setTime(nbt.getInt("time"));
    }

    private void setTime(int time) {
        this.dataTracker.set(TIME, time);
    }

    private void setOwnerPos(Vec3d vec3d) {
        this.dataTracker.set(OWNER_POS, vec3d);
    }

    private void setExpanding(boolean expanding) {
        this.dataTracker.set(EXPANDING, expanding);
    }

    private void setWorldConsumed(boolean worldConsumed) {
        this.dataTracker.set(WORLD_CONSUMED, worldConsumed);
    }

    private void setRadius(int radius) {
        this.dataTracker.set(RADIUS, radius);
    }

    @Nullable
    public UUID getOwnerUuid() {
        return this.dataTracker.get(OWNER_UUID).orElse(null);
    }

    public void setOwnerUuid(@Nullable UUID uuid) {
        this.dataTracker.set(OWNER_UUID, Optional.ofNullable(uuid));
    }

    public void setOwner(PlayerEntity player) {
        this.setOwnerUuid(player.getUuid());
    }

    @Nullable
    public LivingEntity getOwner() {
        try {
            UUID uUID = this.getOwnerUuid();
            return uUID == null ? null : this.world.getPlayerByUuid(uUID);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isOwner(LivingEntity entity) {
        return entity == this.getOwner();
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.world.isClient) {
            if (!isWorldConsumed()) {
                if (isExpanding()) {
                    if (MAX_TIME >= getTime()) {
                        setTime(getTime() + 1);
                    } else {
                        setExpanding(false);
                        getAffectedPoints(world, getBlockPos(), getRadius());
                    }
                } else {
                    if (getTime() > 0) {
                        setTime(getTime() - 1);
                    } else {
                        world.createExplosion(null, this.getX(), getY(), getZ(), 15, Explosion.DestructionType.NONE);
                        this.remove(RemovalReason.DISCARDED);
                    }
                }
            }
        }
    }

    @Override
    public Packet<?> createSpawnPacket() {
        return new EntitySpawnS2CPacket(this);
    }
}
