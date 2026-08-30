package net.luojiuoscar.isaac_disaster.client.particle;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** A direct-texture equivalent of vanilla's item-breaking particle. */
public final class TearShatterParticle extends Particle {
    private static final int MIN_LIGHT = 4;
    private static final Map<ResourceLocation, ParticleRenderType> RENDER_TYPES = new ConcurrentHashMap<>();

    private final ParticleRenderType renderType;
    private final float minU;
    private final float maxU;
    private final float minV;
    private final float maxV;
    private final float fragmentQuadSize;

    TearShatterParticle(ClientLevel level, Vec3 position, Vec3 velocity, ResourceLocation texture,
                        float quadSize, int color, float alpha) {
        super(level, position.x, position.y, position.z, 0.0D, 0.0D, 0.0D);
        // Match BreakingItemParticle: retain the base particle's randomized motion at 10%, then add the explicit velocity.
        xd = xd * 0.1D + velocity.x;
        yd = yd * 0.1D + velocity.y;
        zd = zd * 0.1D + velocity.z;
        renderType = RENDER_TYPES.computeIfAbsent(texture, DirectTextureRenderType::new);
        minU = random.nextFloat() * 3.0F / 4.0F;
        maxU = minU + 0.25F;
        minV = random.nextFloat() * 3.0F / 4.0F;
        maxV = minV + 0.25F;
        fragmentQuadSize = quadSize;
        float collisionSize = quadSize * 2.0F;
        setSize(collisionSize, collisionSize);
        setBoundingBox(AABB.ofSize(position, collisionSize, collisionSize, collisionSize));
        gravity = 1.0F;
        rCol = ((color >> 16) & 0xFF) / 255.0F;
        gCol = ((color >> 8) & 0xFF) / 255.0F;
        bCol = (color & 0xFF) / 255.0F;
        this.alpha = alpha;
    }

    @Override
    public void render(VertexConsumer buffer, Camera camera, float partialTick) {
        Vec3 cameraPosition = camera.getPosition();
        float x = (float) (Mth.lerp(partialTick, xo, this.x) - cameraPosition.x);
        float y = (float) (Mth.lerp(partialTick, yo, this.y) - cameraPosition.y);
        float z = (float) (Mth.lerp(partialTick, zo, this.z) - cameraPosition.z);
        Quaternionf rotation = new Quaternionf(camera.rotation());
        float size = fragmentQuadSize;

        Vector3f[] vertices = {
                new Vector3f(-1.0F, -1.0F, 0.0F), new Vector3f(-1.0F, 1.0F, 0.0F),
                new Vector3f(1.0F, 1.0F, 0.0F), new Vector3f(1.0F, -1.0F, 0.0F)
        };
        for (Vector3f vertex : vertices) {
            rotation.transform(vertex);
            vertex.mul(size).add(x, y, z);
        }

        int light = getMinimumLightColor(partialTick);
        buffer.vertex(vertices[0].x(), vertices[0].y(), vertices[0].z()).uv(maxU, maxV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[1].x(), vertices[1].y(), vertices[1].z()).uv(maxU, minV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[2].x(), vertices[2].y(), vertices[2].z()).uv(minU, minV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
        buffer.vertex(vertices[3].x(), vertices[3].y(), vertices[3].z()).uv(minU, maxV).color(rCol, gCol, bCol, alpha).uv2(light).endVertex();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return renderType;
    }

    private int getMinimumLightColor(float partialTick) {
        int packedLight = getLightColor(partialTick);
        return LightTexture.pack(Math.max(MIN_LIGHT, LightTexture.block(packedLight)),
                Math.max(MIN_LIGHT, LightTexture.sky(packedLight)));
    }

    private record DirectTextureRenderType(ResourceLocation texture) implements ParticleRenderType {
        @Override
        public void begin(BufferBuilder builder, TextureManager textureManager) {
            RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();
            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.setShader(GameRenderer::getParticleShader);
            RenderSystem.setShaderTexture(0, texture);
            builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.PARTICLE);
        }

        @Override
        public void end(Tesselator tesselator) {
            tesselator.end();
        }
    }
}
