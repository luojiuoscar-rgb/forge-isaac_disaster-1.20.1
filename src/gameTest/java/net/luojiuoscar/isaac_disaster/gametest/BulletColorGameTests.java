package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbility;
import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.event.TriggerModuleEvents;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.ModPassiveAbilities;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.Aquarius;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.BulletColor;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.luojiuoscar.isaac_disaster.registries.split_module.ModSplitModules;
import net.luojiuoscar.isaac_disaster.registries.split_module.SplitTriggerType;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerTypes;
import net.luojiuoscar.isaac_disaster.system.combat_effect.groundeffect.server.GroundEffectRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegisterEvent;

import java.util.UUID;

@GameTestHolder("isaac_disaster_bullet_color")
@PrefixGameTestTemplate(false)
public final class BulletColorGameTests {
    private static final ResourceLocation SCOPED = id("scoped");
    private static final ResourceLocation ROOT = id("root");
    private static final ResourceLocation ALPHA_ONLY = id("alpha_only");

    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        private static final BulletColor SCOPED_COLOR = BulletColor.builder().color(0x112233).priority(101, 1)
                .onlyTypes(ModAttackTypes.BRIMSTONE).build();
        private static final BulletColor ROOT_COLOR = BulletColor.builder().color(0x334455).priority(102, 1)
                .onlyRoots(ModAttackTypes.LASER).build();
        private static final BulletColor TRANSLUCENT_COLOR = BulletColor.builder().alpha(0.8F).priority(100, 100).build();

        @SubscribeEvent
        public static void register(RegisterEvent event) {
            event.register(ModBulletColors.BULLET_COLOR_KEY, helper -> {
                helper.register(SCOPED, SCOPED_COLOR);
                helper.register(ROOT, ROOT_COLOR);
                helper.register(ALPHA_ONLY, TRANSLUCENT_COLOR);
            });
        }
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void realObtainAndRemoveKeepOldProjectileSnapshot(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ModPassiveAbilities.OUIJA_BOARD.get().handleObtain(f.player, null);
            helper.assertTrue(f.tear().resolveColor().alpha() == 0.75F
                            && f.tear().getColorId().equals(ModBulletColors.BASE.getId()),
                    "Ouija Board provides ghost opacity without replacing the default RGB");
            ModPassiveAbilities.DEAD_ONION.get().handleObtain(f.player, null);
            ModPassiveAbilities.SACRED_HEART.get().handleObtain(f.player, null);
            var context = f.tear();
            var bullet = BulletState.from(context).build();
            helper.assertTrue(bullet.color() == 0xFFFFFF && bullet.alpha() == 0.75F,
                    "Sacred Heart RGB and Dead Onion alpha compose independently");
            ModPassiveAbilities.SACRED_HEART.get().handleRemove(f.player, null);
            helper.assertTrue(f.tear().getColorId().equals(ModBulletColors.DEAD_ONION.getId()), "Removing the RGB winner reveals the next color");
            ModPassiveAbilities.DEAD_ONION.get().handleRemove(f.player, null);
            helper.assertTrue(f.tear().resolveColor().alpha() == 0.75F
                            && f.tear().getColorId().equals(ModBulletColors.BASE.getId()),
                    "Removing Dead Onion leaves Ouija Board's ghost contribution and restores default RGB");
            ModPassiveAbilities.OUIJA_BOARD.get().handleRemove(f.player, null);
            helper.assertTrue(f.tear().resolveColor().alpha() == 1, "Removing the final ghost source restores opacity");
            helper.assertTrue(bullet.color() == 0xFFFFFF && bullet.alpha() == 0.75F && context.getColorIds().size() == 3,
                    "Already prepared and spawned attacks retain their complete snapshot");
            ModPassiveAbilities.OUIJA_BOARD.get().handleObtain(f.player, null);
            ModPassiveAbilities.OUIJA_BOARD.get().handleObtain(f.player, null);
            ModPassiveAbilities.OUIJA_BOARD.get().handleRemove(f.player, null);
            helper.assertTrue(f.tear().resolveColor().alpha() == 0.75F, "Duplicate copies keep one remaining ghost contribution");
            ModPassiveAbilities.DEAD_ONION.get().handleObtain(f.player, null);
            ModPassiveAbilities.OUIJA_BOARD.get().handleRemove(f.player, null);
            helper.assertTrue(f.tear().resolveColor().alpha() == 0.75F
                            && f.tear().getColorId().equals(ModBulletColors.DEAD_ONION.getId()),
                    "Removing Ouija Board first also preserves Dead Onion's separate color and ghost opacity");
            ModPassiveAbilities.DEAD_ONION.get().handleRemove(f.player, null);
            helper.assertTrue(f.colors.getBulletColorIds().isEmpty(), "Both items clean up their own color contributions");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void probabilityAttachmentCompetesWithPlayerColorsWithoutLosingAlpha(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.colors.addBulletColor(ModBulletColors.DEAD_ONION.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.SPECTRAL.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.POISON.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.SACRED_HEART.getId(), 1);
            ModPassiveAbilities.HOLY_LIGHT.get().handleObtain(f.player, null);
            f.player.getRandom().setSeed(4096);
            var success = f.prepare(f.tear());
            helper.assertTrue(success.getColorId().equals(ModBulletColors.HOLY_LIGHT.getId())
                            && success.resolveColor().alpha() == 0.75F,
                    "A successful holy roll wins the equal-priority ID comparison while retaining Dead Onion alpha");
            f.player.getRandom().setSeed(0);
            var miss = f.prepare(f.tear());
            helper.assertTrue(miss.getColorId().equals(ModBulletColors.POISON.getId())
                            && miss.resolveColor().alpha() == 0.75F,
                    "An unsuccessful roll contributes no color, and the original transparency survives");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void exactTypeAndRootFiltersUseTheActualRegisteredAttack(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.colors.addBulletColor(ModBulletColors.POISON.getId(), 1);
            f.colors.addBulletColor(SCOPED, 1);
            helper.assertTrue(f.tear().getColorId().equals(ModBulletColors.POISON.getId()), "Restricted high-tier color cannot hide the tear fallback");
            helper.assertTrue(ModAttackTypes.LASER.get().createAttackContext(f.player, f.player).getColorId()
                    .equals(ModBulletColors.POISON.getId()), "Exact Brimstone scope does not match ordinary laser");
            helper.assertTrue(ModAttackTypes.BRIMSTONE.get().createAttackContext(f.player, f.player).getColorId().equals(SCOPED),
                    "Exact scope matches Brimstone");
            f.colors.addBulletColor(ROOT, 1);
            helper.assertTrue(ModAttackTypes.TECHNOLOGY2.get().createAttackContext(f.player, f.player).getColorId().equals(ROOT),
                    "Laser root scope matches Technology 2");
            helper.assertTrue(ModAttackTypes.BRIMSTONE.get().createAttackContext(f.player, f.player).getColorId().equals(ROOT),
                    "Higher tier root color wins Brimstone too");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void alphaOnlyAndRepeatedCopiesDoNotReplaceRgbOrMultiplyOpacity(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.colors.addBulletColor(ModBulletColors.POISON.getId(), 1);
            f.colors.addBulletColor(ALPHA_ONLY, 3);
            var tear = f.tear();
            helper.assertTrue(tear.getColorId().equals(ModBulletColors.POISON.getId()) && tear.resolveColor().alpha() == 0.8F,
                    "An alpha-only high-tier definition cannot replace RGB or multiply with its copies");
            f.colors.addBulletColor(ModBulletColors.DEAD_ONION.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.SPECTRAL.getId(), 1);
            helper.assertTrue(f.tear().resolveColor().alpha() == 0.75F, "The smallest candidate alpha wins");
            var laser = ModAttackTypes.BRIMSTONE.get().createAttackContext(f.player, f.player);
            helper.assertTrue(laser.resolveColor().alpha() == 1 && laser.getColorId().equals(ModBulletColors.POISON.getId()),
                    "Laser accepts RGB but never inherits candidate alpha");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void derivedAttackRechecksScopesAndFrozenParentCannotBeChanged(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.colors.addBulletColor(ModBulletColors.DEAD_ONION.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.SPECTRAL.getId(), 1);
            f.colors.addBulletColor(SCOPED, 1);
            var parent = f.tear();
            parent.freeze();
            parent.offerColor(ModBulletColors.IPECAC.getId());
            var child = parent.toBuilder().attackType(ModAttackTypes.BRIMSTONE.get()).build();
            helper.assertTrue(parent.getColorId().equals(ModBulletColors.DEAD_ONION.getId()), "Frozen source rejects later candidates");
            helper.assertTrue(child.getColorIds().equals(parent.getColorIds()) && child.getColorId().equals(SCOPED)
                            && child.resolveColor().alpha() == 1,
                    "Derived type resolves all inherited candidates without re-reading the player");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void actualCricketsBodySplitRetainsEveryColorCandidateAndAlpha(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.colors.addBulletColor(ModBulletColors.POISON.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.DEAD_ONION.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.SPECTRAL.getId(), 1);
            f.colors.addBulletColor(SCOPED, 1);
            var source = f.tear();
            source.addSplitModule(ModSplitModules.CRICKETS_BODY.getId(), 1);
            source.freeze();
            var parent = BulletState.from(source).build();
            f.colors.addBulletColor(ModBulletColors.IPECAC.getId(), 1);
            var event = new BulletSplitEvent(parent, parent.getSplitSequence(), parent.getAttackContext(), SplitTriggerType.END_OF_LIFE);
            var requests = parent.getSplitSequence().createChildRequests(event);
            var children = requests.stream().flatMap(request -> request.getProvidedContexts().stream()).toList();
            helper.assertTrue(children.size() == 4, "The registered Cricket's Body generator actually produces four children");
            for (var child : children) {
                var bullet = BulletState.from(child).build();
                helper.assertTrue(child.getColorIds().equals(source.getColorIds()) && bullet.alpha() == 0.75F
                                && bullet.getColorId().equals(ModBulletColors.POISON.getId()),
                        "Generated children inherit all source candidates and alpha without reading newly obtained colors");
                helper.assertTrue(child.bindAttackTypeOrCopy(ModAttackTypes.BRIMSTONE.get()).getColorId().equals(SCOPED),
                        "The previously inapplicable high-tier candidate survives actual split generation");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void aquariusUsesSharedRgbButRejectsAttackScopesAndKeepsGroundAppearance(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.colors.addBulletColor(ModBulletColors.DEAD_ONION.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.SPECTRAL.getId(), 1);
            f.colors.addBulletColor(SCOPED, 1);
            f.colors.addBulletColor(ROOT, 1);
            f.colors.addBulletColor(ALPHA_ONLY, 1);
            helper.assertTrue(new Aquarius().applyEffect(new ExecutableEffectContext(f.player)), "Aquarius spawns on the supported floor");
            var active = GroundEffectRuntime.INSTANCE.manager(f.level).activeEffects();
            helper.assertTrue(active.size() == 1 && active.get(0).getColor() == ModBulletColors.DEAD_ONION.get().color(),
                    "Water inherits the unrestricted RGB and ignores both attack-only candidates and alpha-only color");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void defaultsAndIndependentRevelationRemainUnchanged(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            helper.assertTrue(f.tear().resolveColor().color() == ModBulletColors.DEFAULT_TEAR_COLOR, "Uncolored tears retain their default");
            helper.assertTrue(ModAttackTypes.LASER.get().createAttackContext(f.player, f.player).resolveColor().color() == 0xFF0000,
                    "Uncolored laser retains red");
            helper.assertTrue(new Aquarius().applyEffect(new ExecutableEffectContext(f.player)), "Default water spawns");
            helper.assertTrue(GroundEffectRuntime.INSTANCE.manager(f.level).activeEffects().get(0).getColor() == 0x3C9BE8,
                    "Default water remains blue");
            f.colors.addBulletColor(ModBulletColors.POISON.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.DEAD_ONION.getId(), 1);
            f.colors.addBulletColor(ModBulletColors.SPECTRAL.getId(), 1);
            var revelation = ModAttackTypes.REVELATION.get().createAttackContext(f.player, f.player);
            helper.assertTrue(revelation.resolveColor().color() == 0xFFFFFF && revelation.resolveColor().alpha() == 1
                            && revelation.getColorIds().size() == 1,
                    "Revelation continues to opt out of player projectile colors and alpha");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "bullet_color")
    public static void playerSaveLoadRetainsCountsWithoutABestColorField(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.colors.addBulletColor(ModBulletColors.DEAD_ONION.getId(), 2);
            f.colors.addBulletColor(SCOPED, 1);
            var tag = new CompoundTag();
            f.colors.saveNBTData(tag);
            helper.assertTrue(!tag.contains("best_bullet_color"), "No derived global-best cache is persisted");
            var restored = new PlayerAbility();
            restored.loadNBTData(tag);
            restored.addBulletColor(ModBulletColors.DEAD_ONION.getId(), -1);
            helper.assertTrue(restored.getBulletColorIds().equals(f.colors.getBulletColorIds()), "Duplicate counts survive save/load");
            restored.addBulletColor(ModBulletColors.DEAD_ONION.getId(), -1);
            helper.assertTrue(!restored.getBulletColorIds().contains(ModBulletColors.DEAD_ONION.getId())
                            && f.colors.getBulletColorIds().contains(ModBulletColors.DEAD_ONION.getId()),
                    "Restored candidate counts are independent and disappear only after the final copy");
        }
        helper.succeed();
    }

    private static ResourceLocation id(String value) { return ResourceLocation.fromNamespaceAndPath("color_test", value); }

    private static final class Fixture implements AutoCloseable {
        final ServerLevel level;
        final ServerPlayer player;
        final PlayerAbility colors;

        Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            Vec3 center = helper.absoluteVec(new Vec3(1.5, 2, 1.5));
            level.setBlockAndUpdate(BlockPos.containing(center).below(), Blocks.STONE.defaultBlockState());
            UUID uuid = UUID.randomUUID();
            player = FakePlayerFactory.get(level, new GameProfile(uuid, "color_" + uuid.toString().substring(0, 8)));
            player.setPos(center);
            level.addNewPlayer(player);
            colors = player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).orElseThrow(IllegalStateException::new);
        }

        AttackContext tear() { return ModAttackTypes.BULLET.get().createAttackContext(player, player); }

        AttackContext prepare(AttackContext attack) {
            var context = new ExecutableEffectContext(player);
            context.set(ContextKeys.ATTACK_CONTEXT, attack);
            context.set(ContextKeys.EFFECT_ATTACHMENT_NODE, attack);
            TriggerModuleEvents.dispatch(context, ModTriggerTypes.EFFECT_ATTACHMENT_PREPARE);
            return attack;
        }

        @Override public void close() {
            GroundEffectRuntime.INSTANCE.manager(level).clear();
            level.removePlayerImmediately(player, Entity.RemovalReason.DISCARDED);
        }
    }
}
