package org.dimdev.dimdoors.client;

import java.io.IOException;
import java.io.InputStream;

import de.javagl.obj.FloatTuple;
import de.javagl.obj.Obj;
import de.javagl.obj.ObjFace;
import de.javagl.obj.ObjReader;
import de.javagl.obj.ObjUtils;
import org.dimdev.dimdoors.block.entity.FoldingRiftBlockEntity;
import org.dimdev.dimdoors.entity.FoldingEntity;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

public class FoldingRenderer extends EntityRenderer<FoldingEntity> {
    private static Obj obj;

    public FoldingRenderer(EntityRendererFactory.Context ctx) {
        super(ctx);
    }

    @Override
    public void render(FoldingEntity entity, float yaw, float tickDelta, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
        render(vertexConsumers.getBuffer(MyRenderLayer.FOLDING), entity, matrices);
    }

    public static void render(VertexConsumer buffer, FoldingEntity rift, MatrixStack matrices) {
        matrices.push();

        float scale = (float) rift.getRenderRadius();

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

    @Override
    public Identifier getTexture(FoldingEntity entity) {
        return null;
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
