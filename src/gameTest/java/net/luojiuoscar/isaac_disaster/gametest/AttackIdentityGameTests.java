package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.capability.entity.EffectModulesProvider;
import net.luojiuoscar.isaac_disaster.event.custom.attack.AttackContextPrepareEvent;
import net.luojiuoscar.isaac_disaster.event.custom.attack.tear_bullet.BulletSplitEvent;
import net.luojiuoscar.isaac_disaster.registries.attack_type.*;
import net.luojiuoscar.isaac_disaster.registries.attack_type.impl.LaserAttack;
import net.luojiuoscar.isaac_disaster.registries.split_module.*;
import net.luojiuoscar.isaac_disaster.registries.trajectory.ModTrajectoryModules;
import net.luojiuoscar.isaac_disaster.registries.trajectory.TrajectorySpec;
import net.luojiuoscar.isaac_disaster.registries.trigger_module.ModTriggerModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.RegisterEvent;
import net.minecraftforge.registries.RegistryManager;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

@GameTestHolder(IsaacDisaster.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AttackIdentityGameTests {
    @Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static final class Types {
        static final ResourceLocation STREAM = ResourceLocation.parse("trajectory_test:stream");
        static final CaptureType ICE = new CaptureType(ResourceLocation.parse("trajectory_test:ice_laser"), ModAttackTypes.LASER.getId());
        static final CaptureType ROOT = new CaptureType(STREAM, STREAM);
        static final CaptureType FIRE = new CaptureType(ResourceLocation.parse("trajectory_test:fire_stream"), STREAM);
        @SubscribeEvent public static void register(RegisterEvent event) {
            event.register(ModAttackTypes.ATTACK_TYPE_KEY, helper -> {
                for (var type : List.of(ICE, ROOT, FIRE)) helper.register(type.getId(), type);
            });
        }
    }

    @GameTest(template = "trajectory_empty")
    public static void builtInIdsAndRootsMatchRegisteredDefinitions(GameTestHelper helper) {
        var entries = List.of(ModAttackTypes.BULLET, ModAttackTypes.LASER, ModAttackTypes.BRIMSTONE,
                ModAttackTypes.C_SECTION, ModAttackTypes.CURSED_EYE, ModAttackTypes.NEPTUNUS,
                ModAttackTypes.TECHNOLOGY2, ModAttackTypes.SHOOP_DA_WHOOP);
        var names = List.of("bullet", "laser", "brimstone", "c_section", "cursed_eye", "neptunus",
                "technology2", "shoop_da_whoop");
        var roots = List.of(ModAttackTypes.BULLET, ModAttackTypes.LASER, ModAttackTypes.LASER,
                ModAttackTypes.BULLET, ModAttackTypes.CURSED_EYE, ModAttackTypes.NEPTUNUS,
                ModAttackTypes.LASER, ModAttackTypes.LASER);
        var registry = RegistryManager.ACTIVE.getRegistry(ModAttackTypes.ATTACK_TYPE_KEY);
        for (int i = 0; i < entries.size(); i++) {
            var entry = entries.get(i);
            var definition = entry.get();
            helper.assertTrue(entry.getId().equals(ResourceLocation.fromNamespaceAndPath(IsaacDisaster.MOD_ID, names.get(i))),
                    "Registration retains its own name: " + names.get(i));
            helper.assertTrue(entry.getId().equals(definition.getId()), "Definition ID comes from its registration");
            helper.assertTrue(entry.getId().equals(registry.getKey(definition)), "Registry key matches definition ID");
            helper.assertTrue(roots.get(i).getId().equals(definition.getRootId()), "Root family is preserved: " + names.get(i));
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void registeredTypesAreAvailableBeforeHighestPriorityPrepare(GameTestHelper helper) {
        var player = player(helper);
        var modules = player.getCapability(EffectModulesProvider.EFFECT_MODULES).orElseThrow(IllegalStateException::new).getTriggerModules();
        modules.add(ModTriggerModules.TINY_PLANET.getId(), 2);
        var registry = RegistryManager.ACTIVE.getRegistry(ModAttackTypes.ATTACK_TYPE_KEY);
        for (var type : registry.getValues()) helper.assertTrue(type.getId().equals(registry.getKey(type)), "Declared ID matches registration");
        var observed = new AtomicInteger();
        Consumer<AttackContextPrepareEvent> listener = event -> {
            if (event.getOwner() != player) return;
            helper.assertTrue(event.getAttackType().getId().equals(event.getAttackContext().getTypeId()), "Concrete ID before highest listener");
            helper.assertTrue(event.getAttackType().getRootId().equals(event.getAttackContext().getRootTypeId()), "Root ID before highest listener");
            observed.incrementAndGet();
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.HIGHEST, false, AttackContextPrepareEvent.class, listener);
        try {
            for (var definition : List.of(Types.ICE, Types.FIRE)) {
                var type = (CaptureType) registry.getValue(definition.getId());
                helper.assertTrue(type != null, "External registration is available");
                var initial = AttackContext.builder(player, player).build();
                AttackPipeline.executeRequest(AttackRequest.withContexts(player, type, AttackOrigin.ABILITY_EXTRA,
                        AttackPipelineMode.PREPARE_AND_EXECUTE, List.of(initial), false));
                var prepared = type.executed.get(0);
                var expected = type == Types.ICE ? List.of(new TrajectorySpec(ModTrajectoryModules.TINY_PLANET_LASER.getId(), 1)) : List.<TrajectorySpec>of();
                helper.assertTrue(prepared.getTrajectorySpecs().equals(expected), "Only supported roots get built-in family modules");
                helper.assertTrue(prepared.isFrozen(), "Execution receives a frozen snapshot");
                AttackPipeline.executeRequest(AttackRequest.withContexts(player, type, AttackOrigin.SPLIT_CHILD,
                        AttackPipelineMode.EXECUTE_ONLY, List.of(prepared.copy()), false));
                helper.assertTrue(type.executed.get(0).getTrajectorySpecs().equals(expected), "Direct execution does not attach again");
            }
            helper.assertTrue(observed.get() == 2, "Direct execution publishes no Prepare event");
        } finally { MinecraftForge.EVENT_BUS.unregister(listener); }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty")
    public static void parasiteRetainsExternalLaserAndCricketRetypesChildren(GameTestHelper helper) {
        var player = player(helper);
        var parentContext = AttackContext.builder(player, player).attackType(Types.ICE).damage(8.0).build();
        var parent = new LaserAttack.LaserProjectile(parentContext);
        var sequence = new SplitSequence();
        sequence.add(ModSplitModules.PARASITE.getId(), 1);
        var event = new BulletSplitEvent(parent, sequence, parent.getAttackContext(), SplitTriggerType.ENTITY);
        var requests = sequence.createChildRequests(event);
        helper.assertTrue(requests.size() == 1 && requests.get(0).getAttackType() == Types.ICE, "Parasite preserves the registered derived laser");
        AttackPipeline.executeRequest(requests.get(0));
        helper.assertTrue(Types.ICE.executed.size() == 2, "Both children execute");
        for (var child : Types.ICE.executed) {
            helper.assertTrue(child.getTypeId().equals(Types.ICE.getId()), "Child retains exact identity");
            helper.assertTrue(child.getRootTypeId().equals(ModAttackTypes.LASER.getId()), "Child retains root identity");
            helper.assertTrue(child.isFrozen(), "Child freezes before execution");
        }
        sequence = new SplitSequence();
        sequence.add(ModSplitModules.CRICKETS_BODY.getId(), 1);
        event = new BulletSplitEvent(parent, sequence, parent.getAttackContext(), SplitTriggerType.ENTITY);
        requests = sequence.createChildRequests(event);
        helper.assertTrue(!requests.isEmpty(), "Cricket produces ordinary children");
        for (var request : requests) for (var child : request.getProvidedContexts()) {
            helper.assertTrue(child.getTypeId().equals(request.getAttackType().getId()), "Changing backend also changes context identity");
            helper.assertTrue(child.getRootTypeId().equals(request.getAttackType().getRootId()), "Changing backend also changes context root");
        }
        helper.assertTrue(parent.getTypeId().equals(Types.ICE.getId()), "Parent identity is unchanged");
        helper.succeed();
    }

    private static ServerPlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "attack-id-test"));
    }
    private static final class CaptureType extends AttackType {
        private final ResourceLocation id, root;
        private List<AttackContext> executed = List.of();
        private CaptureType(ResourceLocation id, ResourceLocation root) { super(0); this.id = id; this.root = root; }
        @Override public ResourceLocation getId() { return id; }
        @Override public ResourceLocation getRootId() { return root; }
        @Override public List<AttackContext> getAttackContexts(ServerPlayer player, int count) { return List.of(); }
        @Override public void performAttack(List<AttackContext> contexts) { executed = List.copyOf(contexts); }
        @Override public void makeSound(LivingEntity entity) { }
        @Override public void shoot(AttackContext context) { }
    }
}
