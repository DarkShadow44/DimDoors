package org.dimdev.dimdoors.util;

import net.minecraft.entity.data.TrackedDataHandler;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.math.Vec3d;

public class TrackDataHandlers {
    public static final TrackedDataHandler<Vec3d> VEC3D = new TrackedDataHandler<Vec3d>() {
        @Override
        public void write(PacketByteBuf buf, Vec3d value) {
            buf.writeDouble(value.x);
            buf.writeDouble(value.y);
            buf.writeDouble(value.z);
        }

        @Override
        public Vec3d read(PacketByteBuf buf) {
            return new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }

        @Override
        public Vec3d copy(Vec3d value) {
            return value;
        }
    };

    static {
        TrackedDataHandlerRegistry.register(VEC3D);
    }
}
