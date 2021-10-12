package org.dimdev.dimdoors.client;

import java.io.IOException;
import java.io.InputStream;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import org.dimdev.dimdoors.block.entity.FoldingRiftBlockEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.block.entity.BlockEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(EnvType.CLIENT)
public class FoldingRiftBlockEntityRenderer implements BlockEntityRenderer<FoldingRiftBlockEntity> {
    private static Obj obj;

    @Override
    public void render(FoldingRiftBlockEntity rift, float tickDelta, MatrixStack matrices, VertexConsumerProvider vcs, int breakProgress, int alpha) {
        render(vcs.getBuffer(MyRenderLayer.FOLDING), rift, matrices);
    }

    public static void render(VertexConsumer buffer, FoldingRiftBlockEntity rift, MatrixStack matrices) {
        matrices.push();

        matrices.translate(0.5,0.5,0.5);

        float scale = (float) rift.getRadius();

        matrices.scale(scale, scale, scale);

        for (int i = 0; i < obj.getNumFaces(); i++) {
            ObjFace face = obj.getFace(i);

            FloatTuple v1 = obj.getVertex(face.getNormalIndex(0));
            FloatTuple v2 = obj.getVertex(face.getNormalIndex(1));
            FloatTuple v3 = obj.getVertex(face.getNormalIndex(2));

            buffer.vertex(matrices.peek().getModel(), v1.getX(), v1.getY(), v1.getZ()).color(0.0f, 0.0f, 0.0f, 1.0f).next();
            buffer.vertex(matrices.peek().getModel(), v2.getX(), v2.getY(), v2.getZ()).color(0.0f, 0.0f, 0.0f, 1.0f).next();
            buffer.vertex(matrices.peek().getModel(), v3.getX(), v3.getY(), v3.getZ()).color(0.0f, 0.0f, 0.0f, 1.0f).next();
        }

        matrices.pop();
    }

    private static Obj readObj(Resource objUri) throws IOException {

        try (InputStream objInputStream =  objUri.getInputStream()) {
            Obj obj = ObjReader.read(objInputStream);
            return ObjUtils.convertToRenderable(obj);
        } catch (IOException e) {
            throw e;
        }
    }

    static {
        try {
            obj = readObj(MinecraftClient.getInstance().getResourceManager().getResource(new Identifier("dimdoors:models/obj/sphere.obj")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

