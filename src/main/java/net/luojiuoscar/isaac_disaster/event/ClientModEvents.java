package net.luojiuoscar.isaac_disaster.event;


import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.block.ModBlockEntities;
import net.luojiuoscar.isaac_disaster.block.renderer.IdentifierRenderer;
import net.luojiuoscar.isaac_disaster.block.renderer.IsaacChestRenderer;
import net.luojiuoscar.isaac_disaster.block.renderer.PedestalRenderer;
import net.luojiuoscar.isaac_disaster.client.ModKeyMappings;
import net.luojiuoscar.isaac_disaster.client.hud.ChargeBarHudOverlay;
import net.luojiuoscar.isaac_disaster.client.hud.FlyHudOverlay;
import net.luojiuoscar.isaac_disaster.entity.ModEntities;
import net.luojiuoscar.isaac_disaster.entity.tnt.CustomTntRenderer;
import net.luojiuoscar.isaac_disaster.renderer.FetusBulletRenderer;
import net.luojiuoscar.isaac_disaster.renderer.InvincibleChargeLayer;
import net.luojiuoscar.isaac_disaster.renderer.IsaacBulletRenderer;
import net.luojiuoscar.isaac_disaster.renderer.familiar.MomKnifeRenderer;
import net.luojiuoscar.isaac_disaster.renderer.layer.frozen.FrozenShellLayer;
import net.luojiuoscar.isaac_disaster.renderer.layer.golden.GoldenLayer;
import net.luojiuoscar.isaac_disaster.renderer.layer.golden.SlimeGoldenLayer;
import net.luojiuoscar.isaac_disaster.renderer.layer.petrified.PetrifiedLayer;
import net.luojiuoscar.isaac_disaster.renderer.layer.petrified.SlimePetrifiedLayer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.SlimeRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.DefaultAttributes;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;


@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {
    // 订阅实体渲染器注册事件
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {

        event.registerEntityRenderer(ModEntities.ISAAC_BOMB.get(), CustomTntRenderer::new);
        event.registerEntityRenderer(ModEntities.GIGA_BOMB.get(), CustomTntRenderer::new);
        event.registerEntityRenderer(ModEntities.TEAR_BULLET.get(), IsaacBulletRenderer::new);
        event.registerEntityRenderer(ModEntities.FETUS_BULLET.get(), FetusBulletRenderer::new);
        event.registerEntityRenderer(ModEntities.SELECTIVE_EFFECT_CLOUD.get(), NoopRenderer::new);
        event.registerEntityRenderer(ModEntities.MOM_KNIFE.get(), MomKnifeRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.PEDESTAL_BLOCK_ENTITY.get(), PedestalRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NORMAL_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LOCKED_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.OLD_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ETERNAL_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.RED_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.WOODEN_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BOMB_CHEST_BLOCK_ENTITY.get(), IsaacChestRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CHEST_PLACEHOLDER_BLOCK_ENTITY.get(), IsaacChestRenderer::new);

        event.registerBlockEntityRenderer(ModBlockEntities.TREASURE_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.NORMAL_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SHOP_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SECRET_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SUPER_SECRET_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ULTRA_SECRET_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.CURSE_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.LIBRARY_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.GAMBLE_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.BOSS_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ELITE_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.ANGEL_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.DEVIL_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.PLANETARIUM_IDENTIFIER_BLOCK_ENTITY.get(), IdentifierRenderer::new);

    }

    // 注册自定义的模型
    @SubscribeEvent
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/locked_chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/old_chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/eternal_chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/red_chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/wooden_chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/bomb_chest_lid"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/treasure_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/normal_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/shop_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/library_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/curse_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/gamble_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/secret_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/super_secret_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/ultra_secret_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/angel_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/devil_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/boss_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/elite_identifier"));
        event.register(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, "block/planetarium_identifier"));



    }

    @SubscribeEvent
    public static void onLayerRegister(EntityRenderersEvent.AddLayers event) {
        for (EntityType<?> entityType : ForgeRegistries.ENTITY_TYPES.getValues()) {
            if (DefaultAttributes.hasSupplier(entityType)) {
                addMaterialLayers(event, entityType);
            }
        }

        for (String skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) {
                renderer.addLayer(new InvincibleChargeLayer<>(renderer));
            }
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static void addMaterialLayers(EntityRenderersEvent.AddLayers event, EntityType<?> entityType) {
        EntityType<? extends LivingEntity> livingEntityType = (EntityType<? extends LivingEntity>) entityType;
        LivingEntityRenderer renderer = event.getRenderer(livingEntityType);
        if (renderer == null) {
            return;
        }

        if (renderer instanceof SlimeRenderer) {
            renderer.addLayer(new SlimeGoldenLayer(renderer, event.getEntityModels()));
            renderer.addLayer(new SlimePetrifiedLayer(renderer, event.getEntityModels()));
            renderer.addLayer(new FrozenShellLayer(renderer));
        } else {
            addMaterialLayers(renderer);
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addMaterialLayers(LivingEntityRenderer renderer) {
        if (renderer == null) {
            return;
        }
        renderer.addLayer(new GoldenLayer(renderer));
        renderer.addLayer(new PetrifiedLayer(renderer));
        renderer.addLayer(new FrozenShellLayer(renderer));
    }

    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.FOOD_LEVEL.id(), "fly", FlyHudOverlay.HUD_FLY);
        event.registerAbove(VanillaGuiOverlay.CROSSHAIR.id(), "charge_bar", ChargeBarHudOverlay.HUD_CHARGE_BAR);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(ModKeyMappings.OPEN_ISAAC_ITEM_SCREEN);
        event.register(ModKeyMappings.TOGGLE_ISAAC_FLIGHT);
    }











}
