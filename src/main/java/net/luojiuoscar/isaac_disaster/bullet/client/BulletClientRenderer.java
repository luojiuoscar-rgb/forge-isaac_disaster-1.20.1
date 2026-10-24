package net.luojiuoscar.isaac_disaster.bullet.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import java.util.*;
import java.util.function.IntFunction;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.client.model.familiar.mom_knife.MomKnifeModel;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.FetusMomsKnifeState;
import net.luojiuoscar.isaac_disaster.entity.projectile.moms_knife.PlayerMomsKnifeMotion;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.BulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.FetusBulletVisual;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.ModBulletVisuals;
import net.luojiuoscar.isaac_disaster.registries.bullet_visual.TearBulletVisual;
import net.luojiuoscar.isaac_disaster.renderer.visual.ProjectileRenderTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.SkeletonModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryManager;

/**
 * Client-only rendering bridge for lightweight bullets.
 *
 * <p>It consumes stream states directly, avoiding both Minecraft entity rendering and the particle
 * manager's one-object-per-tear cost. Geometry is submitted as one shared render-buffer batch per
 * texture during the world render pass.</p>
 */
@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, value = Dist.CLIENT)
public final class BulletClientRenderer {
    private static PlayerModel<AbstractClientPlayer> fetusWideModel;
    private static PlayerModel<AbstractClientPlayer> fetusSlimModel;
    private static SkeletonModel<Skeleton> skeletonModel;
    private static final MomKnifeModel<Entity> FETUS_KNIFE_MODEL = new MomKnifeModel<>();
    private static final ResourceLocation KNIFE_TEXTURE = ResourceLocation.fromNamespaceAndPath(
            IsaacDisaster.MOD_ID, "textures/entity/familiar/mom_knife.png");
    private static final Map<Set<ResourceLocation>, TearVisual> TEAR_VISUAL_CACHE = new HashMap<>();
    private static final Map<Set<ResourceLocation>, FetusVisual> FETUS_VISUAL_CACHE = new HashMap<>();

    private BulletClientRenderer() { }

    /** Submits all visible predicted bullets after vanilla weather has been drawn. */
    @SubscribeEvent
    public static void render(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_WEATHER) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null || minecraft.player == null) return;

        MultiBufferSource.BufferSource buffers = minecraft.renderBuffers().bufferSource();
        Vec3 camera = event.getCamera().getPosition();
        Set<RenderType> usedTypes = new HashSet<>();
        float partialTicks = event.getPartialTick();
        PoseStack poseStack = new PoseStack();
        ClientBulletRuntime.INSTANCE.stream().forEachState(state -> {
            Vec3 position = interpolatedPosition(state, partialTicks);
            poseStack.pushPose();
            Vec3 relative = position.subtract(camera);
            poseStack.translate(relative.x, relative.y, relative.z);
            if (ModAttackTypes.C_SECTION.getId().equals(state.getTypeId())) {
                if (state.fetusKnife() != null) usedTypes.add(renderFetusKnife(state, position, poseStack, buffers));
                usedTypes.add(renderFetus(state, position, poseStack, buffers));
            } else {
                usedTypes.add(renderTear(state, position, poseStack, buffers));
            }
            poseStack.popPose();
        });
        for (RenderType type : usedTypes) buffers.endBatch(type);
    }

    /**
     * Builds the local transform used after the world view matrix has already been installed globally.
     *
     * <p>{@link RenderLevelStageEvent.Stage#AFTER_WEATHER} runs after {@code LevelRenderer} copies
     * the event pose into {@code RenderSystem}'s model-view matrix. Reusing the event pose here would
     * apply camera rotation and translation twice, so vertices must start from an identity pose and
     * contain only their camera-relative world translation.</p>
     */
    static PoseStack afterWeatherPose(Vec3 cameraRelativePosition) {
        PoseStack poseStack = new PoseStack();
        poseStack.translate(cameraRelativePosition.x, cameraRelativePosition.y, cameraRelativePosition.z);
        return poseStack;
    }

    /** Computes the frame-interpolated center directly from the live stream state. */
    private static Vec3 interpolatedPosition(BulletState state, float partialTicks) {
        var level = Minecraft.getInstance().level;
        if (state.isStuck() && level != null) {
            return attachedPosition(state, partialTicks, level::getEntity,
                ClientBulletRuntime.INSTANCE.stream().authoritativePosition(state));
        }
        float partial = Math.max(0.0F, Math.min(1.0F, partialTicks));
        return state.previousPosition().lerp(state.position(), partial);
    }

    /** Resolves both identity components before following the target's current render interpolation. */
    static Vec3 attachedPosition(BulletState state, float partialTicks, IntFunction<Entity> lookup,
                                 Vec3 unresolvedPosition) {
        var attachment = state.stickState();
        Entity target = lookup.apply(attachment.targetEntityId());
        if (target == null || target.isRemoved() || attachment.targetUuid() == null
            || !attachment.targetUuid().equals(target.getUUID())) return unresolvedPosition;
        float partial = Math.max(0.0F, Math.min(1.0F, partialTicks));
        return new Vec3(target.xo, target.yo, target.zo).lerp(target.position(), partial)
            .add(attachment.offset());
    }
    /** Draws one camera-facing tear quad using the resolved, server-selected visual profile. */
    private static RenderType renderTear(BulletState state, Vec3 position, PoseStack poseStack, MultiBufferSource buffers) {
        poseStack.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        float scale = (float) state.renderScale();
        poseStack.scale(scale, scale, scale);

        TearVisual visual = cachedTearVisual(state.visualIds());
        float red = visual.acceptsTint ? ((state.color() >> 16) & 255) / 255.0F : 1.0F;
        float green = visual.acceptsTint ? ((state.color() >> 8) & 255) / 255.0F : 1.0F;
        float blue = visual.acceptsTint ? (state.color() & 255) / 255.0F : 1.0F;
        RenderType renderType = ProjectileRenderTypes.translucent(visual.texture);
        VertexConsumer vertices = buffers.getBuffer(renderType);
        emitQuad(vertices, poseStack, red, green, blue, state.alpha(), lightAt(position));
        return renderType;
    }

    /** Draws the attached blade before applying the fetus model's own transforms. */
    private static RenderType renderFetusKnife(BulletState state, Vec3 position, PoseStack poseStack, MultiBufferSource buffers) {
        Vec3 direction = state.fetusKnife().facing(state.velocity());
        var blade = FetusMomsKnifeState.blade(position, direction, state.renderScale(), state.collisionWidth());
        Vec3 offset = blade.anchor().subtract(position);
        poseStack.pushPose();
        poseStack.translate(offset.x, offset.y, offset.z);
        float scale = (float) FetusMomsKnifeState.scale(state.renderScale());
        poseStack.scale(scale, scale, scale);
        MomKnifeModel.applyPose(poseStack, PlayerMomsKnifeMotion.pose(direction, 0));
        RenderType type = FETUS_KNIFE_MODEL.renderType(KNIFE_TEXTURE);
        FETUS_KNIFE_MODEL.renderToBuffer(poseStack, buffers.getBuffer(type), lightAt(position),
                OverlayTexture.NO_OVERLAY, 1, 1, 1, state.alpha());
        poseStack.popPose();
        return type;
    }

    /** Draws the default Fetus player-model visual without constructing a projectile entity. */
    private static RenderType renderFetus(BulletState state, Vec3 position, PoseStack poseStack, MultiBufferSource buffers) {
        Vec3 facing = state.fetusKnife() == null ? state.velocity() : state.fetusKnife().facing(state.velocity());
        applyFetusPose(poseStack, facing, state.renderScale());
        int color = state.color();
        int light = lightAt(position);
        FetusVisual visual = cachedFetusVisual(state.visualIds());
        if (visual.skeleton()) {
            RenderType renderType = ProjectileRenderTypes.translucent(visual.texture());
            skeletonModel().renderToBuffer(poseStack, buffers.getBuffer(renderType),
                    light, OverlayTexture.NO_OVERLAY,
                    visual.acceptsTint() ? ((color >> 16) & 255) / 255.0F : 1.0F,
                    visual.acceptsTint() ? ((color >> 8) & 255) / 255.0F : 1.0F,
                    visual.acceptsTint() ? (color & 255) / 255.0F : 1.0F, state.alpha());
            return renderType;
        }
        PlayerAppearance appearance = resolvePlayerAppearance(state.ownerUuid());
        RenderType renderType = ProjectileRenderTypes.translucent(appearance.skin());
        appearance.model().renderToBuffer(poseStack,
                buffers.getBuffer(renderType),
                light, OverlayTexture.NO_OVERLAY,
                visual.acceptsTint() ? ((color >> 16) & 255) / 255.0F : 1.0F,
                visual.acceptsTint() ? ((color >> 8) & 255) / 255.0F : 1.0F,
                visual.acceptsTint() ? (color & 255) / 255.0F : 1.0F, state.alpha());
        return renderType;
    }

    static void applyFetusPose(PoseStack poseStack, Vec3 velocity, double renderScale) {
        poseStack.translate(0, FetusMomsKnifeState.modelCenterOffset(renderScale), 0);
        if (velocity.lengthSqr() > 1.0E-6D) {
            poseStack.mulPose(Axis.YP.rotationDegrees((float) Math.toDegrees(Math.atan2(velocity.x, velocity.z))));
        }
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
        float scale = (float) FetusMomsKnifeState.modelScale(renderScale);
        poseStack.scale(scale, scale, scale);
    }

    /** Resolves the highest-priority tear visual that can be drawn by the lightweight renderer. */
    private static TearVisual resolveTearVisual(Iterable<ResourceLocation> visualIds) {
        IForgeRegistry<BulletVisual> registry = RegistryManager.ACTIVE.getRegistry(ModBulletVisuals.BULLET_VISUAL_KEY);
        TearBulletVisual best = null;
        ResourceLocation bestId = null;
        if (registry != null) {
            for (ResourceLocation id : visualIds) {
                BulletVisual candidate = registry.getValue(id);
                if (!(candidate instanceof TearBulletVisual tear)
                        || (best != null && (tear.getPriority() < best.getPriority()
                        || tear.getPriority() == best.getPriority() && id.toString().compareTo(bestId.toString()) >= 0))) continue;
                best = tear;
                bestId = id;
            }
        }
        if (best == null) best = ModBulletVisuals.DEFAULT_TEAR.get();
        return new TearVisual(best.getTexture(), best.acceptsTint());
    }

    private static TearVisual cachedTearVisual(Set<ResourceLocation> visualIds) {
        TearVisual cached = TEAR_VISUAL_CACHE.get(visualIds);
        if (cached != null) return cached;
        TearVisual resolved = resolveTearVisual(visualIds);
        TEAR_VISUAL_CACHE.put(visualIds, resolved);
        return resolved;
    }

    /** Lazily bakes the Fetus model only after the client model set is available. */
    private static PlayerModel<AbstractClientPlayer> fetusWideModel() {
        if (fetusWideModel == null) fetusWideModel = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER), false);
        return fetusWideModel;
    }

    private static PlayerModel<AbstractClientPlayer> fetusSlimModel() {
        if (fetusSlimModel == null) fetusSlimModel = new PlayerModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.PLAYER_SLIM), true);
        return fetusSlimModel;
    }

    private static SkeletonModel<Skeleton> skeletonModel() {
        if (skeletonModel == null) skeletonModel = new SkeletonModel<>(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SKELETON));
        return skeletonModel;
    }

    private static PlayerAppearance resolvePlayerAppearance(UUID ownerUuid) {
        if (ownerUuid != null && Minecraft.getInstance().getConnection() != null) {
            var info = Minecraft.getInstance().getConnection().getPlayerInfo(ownerUuid);
            if (info != null) return new PlayerAppearance(info.getSkinLocation(), "slim".equals(info.getModelName()) ? fetusSlimModel() : fetusWideModel());
        }
        return new PlayerAppearance(DefaultPlayerSkin.getDefaultSkin(), fetusWideModel());
    }

    private static FetusVisual resolveFetusVisual(Iterable<ResourceLocation> ids) {
        IForgeRegistry<BulletVisual> registry = RegistryManager.ACTIVE.getRegistry(ModBulletVisuals.BULLET_VISUAL_KEY);
        FetusBulletVisual best = null;
        if (registry != null) for (ResourceLocation id : ids) {
            BulletVisual candidate = registry.getValue(id);
            if (candidate instanceof FetusBulletVisual fetus && (best == null || fetus.getPriority() > best.getPriority())) best = fetus;
        }
        if (best == null) best = ModBulletVisuals.DEFAULT_FETUS.get();
        boolean skeleton = best == ModBulletVisuals.COMPOUND_FRACTURE_FETUS_SKELETON.get();
        ResourceLocation texture = skeleton ? best.getTexture() : DefaultPlayerSkin.getDefaultSkin();
        return new FetusVisual(texture, best.acceptsTint(), skeleton);
    }

    private static FetusVisual cachedFetusVisual(Set<ResourceLocation> visualIds) {
        FetusVisual cached = FETUS_VISUAL_CACHE.get(visualIds);
        if (cached != null) return cached;
        FetusVisual resolved = resolveFetusVisual(visualIds);
        FETUS_VISUAL_CACHE.put(visualIds, resolved);
        return resolved;
    }

    private record PlayerAppearance(ResourceLocation skin, PlayerModel<AbstractClientPlayer> model) { }
    private record FetusVisual(ResourceLocation texture, boolean acceptsTint, boolean skeleton) { }

    /** Emits a billboard in the same entity vertex format used by the existing tear renderer. */
    private static void emitQuad(VertexConsumer vertices, PoseStack poseStack, float red, float green, float blue, float alpha, int light) {
        emitVertex(vertices, poseStack, -0.1F, -0.1F, 0.0F, 0.0F, 1.0F, red, green, blue, alpha, light);
        emitVertex(vertices, poseStack, 0.1F, -0.1F, 0.0F, 1.0F, 1.0F, red, green, blue, alpha, light);
        emitVertex(vertices, poseStack, 0.1F, 0.1F, 0.0F, 1.0F, 0.0F, red, green, blue, alpha, light);
        emitVertex(vertices, poseStack, -0.1F, 0.1F, 0.0F, 0.0F, 0.0F, red, green, blue, alpha, light);
    }

    /** Emits one vertex with the standard projectile translucent render state. */
    private static void emitVertex(VertexConsumer vertices, PoseStack poseStack, float x, float y, float z, float u, float v,
                                   float red, float green, float blue, float alpha, int light) {
        vertices.vertex(poseStack.last().pose(), x, y, z).color(red, green, blue, alpha).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    /** Samples the block and skylight at a projectile center. */
    private static int lightAt(Vec3 position) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return LightTexture.pack(15, 15);
        BlockPos blockPos = BlockPos.containing(position);
        return LightTexture.pack(minecraft.level.getBrightness(LightLayer.BLOCK, blockPos),
                minecraft.level.getBrightness(LightLayer.SKY, blockPos));
    }

    /** Compact resolved tear rendering material. */
    private record TearVisual(ResourceLocation texture, boolean acceptsTint) { }
}
