package net.luojiuoscar.isaac_disaster.gametest;

import net.luojiuoscar.isaac_disaster.manager.ModDamageType;
import net.luojiuoscar.isaac_disaster.system.damage.ModDamageSources;
import net.luojiuoscar.isaac_disaster.system.damage.HurtKnockback;
import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;
import java.util.function.BiConsumer;

@GameTestHolder("isaac_disaster_blue_fly")
@PrefixGameTestTemplate(false)
public final class DamageKnockbackGameTests {
    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void familiarDispatchesUncancelledZeroStrengthEvent(GameTestHelper helper) {
        Cow owner = cow(helper, 0);
        Cow target = cow(helper, 2);
        Cow direct = cow(helper, 4);
        Vec3 initial = new Vec3(0.2, 0.37, -0.1);
        target.setDeltaMovement(initial);
        int[] calls = {0};
        Consumer<LivingKnockBackEvent> listener = event -> {
            if (event.getEntity() != target) return;
            calls[0]++;
            helper.assertTrue(!event.isCanceled(), "Familiar strategy must preserve event cancellation semantics");
            close(helper, event.getStrength(), 0, "Familiar reports zero event strength");
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingKnockBackEvent.class, listener);
        try {
            DamageSource source = ModDamageSources.familiar(helper.getLevel(), direct, owner);
            helper.assertTrue(source.is(ModDamageType.FAMILIAR) && source.getDirectEntity() == direct
                            && source.getEntity() == owner,
                    "Familiar factory keeps the distinct direct entity and owning attacker");
            helper.assertTrue(target.hurt(source, 1), "Familiar damage is accepted");
            helper.assertTrue(calls[0] == 1, "Familiar damage dispatches exactly one Forge knockback event");
            helper.assertTrue(target.getDeltaMovement().equals(initial), "Familiar preserves all velocity components");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            owner.discard();
            target.discard();
            direct.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void laserDispatchesZeroEvenWhenListenerRequestsMovement(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            Vec3 initial = new Vec3(0.2, 0.37, -0.1);
            target.setDeltaMovement(initial);
            LivingKnockBackEvent[] observed = {null};
            listen(target, event -> {
                observed[0] = event;
                close(helper, event.getStrength(), 0, "Laser starts with zero strength");
                helper.assertTrue(!event.isCanceled(), "Laser reaches listeners without cancellation");
                event.setStrength(0.8F);
            }, () -> {
                DamageSource source = ModDamageSources.laser(helper.getLevel(), owner);
                helper.assertTrue(source.getDirectEntity() == owner && source.getEntity() == owner,
                        "Laser retains direct and owning attacker attribution");
                target.hurt(source, 1);
            });
            helper.assertTrue(observed[0] != null, "Laser dispatches Forge event");
            close(helper, observed[0].getStrength(), 0.8, "Strategy does not rewrite the dispatched event");
            helper.assertTrue(!observed[0].isCanceled(), "Suppressing native motion does not cancel the event");
            vector(helper, target.getDeltaMovement(), initial, "Laser preserves velocity after listeners finish");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void tearUsesSnapshotDirectionAndAddsTwoSameTickImpulses(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            Vec3 initial = new Vec3(0.2, 0.37, -0.1);
            target.setOnGround(true);
            target.setDeltaMovement(initial);
            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25);
            DamageSource source = ModDamageSources.tear(helper.getLevel(), owner, new Vec3(3, 2, 4));
            helper.assertTrue(source.is(ModDamageType.TEAR) && source.getDirectEntity() == null
                    && source.getEntity() == owner, "Tear preserves registered type and owner without a fake entity");
            owner.setDeltaMovement(-100, 0, 0);
            int[] calls = {0};
            listen(target, event -> {
                calls[0]++;
                close(helper, event.getStrength(), 0.1, "Tear sends its intended strength");
                close(helper, event.getRatioX(), -3, "Forge X ratio opposes the captured velocity");
                close(helper, event.getRatioZ(), -4, "Forge Z ratio opposes the captured velocity");
                helper.assertTrue(!event.isCanceled(), "Tear is visible to ordinary listeners");
            }, () -> {
                helper.assertTrue(target.hurt(source, 1), "First tear hit is accepted");
                helper.assertTrue(target.hurt(source, 1), "Second same-tick tear hit is accepted");
            });
            helper.assertTrue(calls[0] == 2, "Each accepted hit gets a fresh event and context");
            vector(helper, target.getDeltaMovement(), new Vec3(0.29, 0.37, 0.02),
                    "Tears add horizontal impulses with resistance and preserve grounded vertical velocity");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void tearUsesFinalEventDirectionAndStrength(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            target.setDeltaMovement(0.2, 0.37, -0.1);
            target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.25);
            LivingKnockBackEvent[] observed = {null};
            listen(target, event -> {
                observed[0] = event;
                event.setStrength(0.4F);
                event.setRatioX(0);
                event.setRatioZ(2);
            }, () -> target.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
            vector(helper, target.getDeltaMovement(), new Vec3(0.2, 0.37, -0.4),
                    "Final Forge direction and strength control the additive tear impulse");
            close(helper, observed[0].getStrength(), 0.4, "Event strength is retained after strategy");
            helper.assertTrue(!observed[0].isCanceled(), "Event remains uncanceled after strategy");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void cancellationPreventsTearMotion(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            Vec3 initial = new Vec3(0.2, 0.37, -0.1);
            target.setDeltaMovement(initial);
            listen(target, event -> event.setCanceled(true),
                    () -> target.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
            vector(helper, target.getDeltaMovement(), initial, "Canceled tear event cannot apply its impulse");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void eventCallbackDirectKnockbackUsesNativeFormula(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            target.setOnGround(true);
            target.setDeltaMovement(Vec3.ZERO);
            int[] calls = {0};
            listen(target, event -> {
                if (++calls[0] == 1) target.knockback(0.4, 1, 0);
            }, () -> target.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
            helper.assertTrue(calls[0] == 2, "Callback direct knockback dispatches its own event");
            vector(helper, target.getDeltaMovement(), new Vec3(-0.3, 0.4, 0),
                    "Context is claimed before dispatch so callback knockback remains native");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void nestedHurtOwnsASeparateContext(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            target.setDeltaMovement(Vec3.ZERO);
            int[] calls = {0};
            listen(target, event -> {
                if (++calls[0] == 1) {
                    close(helper, event.getStrength(), 0.1, "Outer event is a tear");
                    target.hurt(ModDamageSources.familiar(helper.getLevel(), owner, owner), 1);
                } else close(helper, event.getStrength(), 0, "Nested event owns the familiar policy");
            }, () -> target.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
            helper.assertTrue(calls[0] == 2, "Both nested hurt calls dispatch");
            vector(helper, target.getDeltaMovement(), new Vec3(0.1, 0, 0), "Nested suppression preserves outer tear impulse");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void rejectedHitAndThrowingScopeCannotLeaveStaleContext(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            DamageSource tear = ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0));
            target.setInvulnerable(true);
            helper.assertTrue(!target.hurt(tear, 1), "Invulnerable victim rejects the hit before knockback");
            target.setInvulnerable(false);
            IllegalStateException expected = new IllegalStateException("intentional knockback-scope failure");
            try {
                HurtKnockback.withSource(target, tear, () -> { throw expected; });
                helper.fail("Scope must propagate the failure");
            } catch (IllegalStateException failure) {
                helper.assertTrue(failure == expected, "Original failure is preserved");
            }
            target.setOnGround(true);
            target.setDeltaMovement(Vec3.ZERO);
            target.knockback(0.4, 1, 0);
            vector(helper, target.getDeltaMovement(), new Vec3(-0.4, 0.4, 0),
                    "Rejected damage and scope exceptions leave later direct knockback native");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void sourceContextMatchesItsTargetOnly(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            owner.setOnGround(true);
            owner.setDeltaMovement(Vec3.ZERO);
            HurtKnockback.withSource(target, ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)),
                    () -> owner.knockback(0.4, 1, 0));
            vector(helper, owner.getDeltaMovement(), new Vec3(-0.4, 0.4, 0),
                    "A different entity cannot claim the target's pending source context");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void throwingEventCannotLeaveAClaimedSourceInLaterKnockback(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            RuntimeException expected = new RuntimeException("intentional Forge callback failure");
            try {
                listen(target, event -> { throw expected; },
                        () -> target.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
                helper.fail("Forge callback failure must propagate");
            } catch (RuntimeException failure) {
                helper.assertTrue(failure == expected, "The original event callback failure propagates");
            }
            target.setDeltaMovement(Vec3.ZERO);
            target.setOnGround(true);
            target.knockback(0.4, 1, 0);
            vector(helper, target.getDeltaMovement(), new Vec3(-0.4, 0.4, 0),
                    "A failed event dispatch unwinds the hurt scope before later knockback");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void nestedOrdinaryHurtDoesNotInheritTearStrategy(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            target.setOnGround(true);
            target.setDeltaMovement(Vec3.ZERO);
            int[] calls = {0};
            listen(target, event -> {
                if (++calls[0] == 1) {
                    target.invulnerableTime = 0;
                    helper.assertTrue(target.hurt(target.damageSources().mobAttack(owner), 1),
                            "Nested ordinary damage is accepted");
                } else close(helper, event.getStrength(), 0.4, "Nested ordinary event retains native strength");
            }, () -> target.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
            helper.assertTrue(calls[0] == 2, "Nested ordinary hit and outer tear both dispatch");
            vector(helper, target.getDeltaMovement(), new Vec3(0.5, 0.4, 0),
                    "Nested native impulse finishes before additive outer tear impulse");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback", timeoutTicks = 40)
    public static void shieldedTearCannotAffectLaterDirectKnockback(GameTestHelper helper) {
        Cow owner = cow(helper, 0);
        var target = EntityType.ZOMBIE.create(helper.getLevel());
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(helper.absoluteVec(new Vec3(3, 2, 1)));
        target.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.DIAMOND_HELMET));
        target.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SHIELD));
        target.setYRot(180);
        target.setYHeadRot(180);
        owner.setPos(target.getX(), target.getY(), target.getZ() - 3);
        helper.getLevel().addFreshEntity(target);
        target.startUsingItem(InteractionHand.MAIN_HAND);
        helper.runAfterDelay(6, () -> {
            try {
                target.setYRot(180);
                target.setYHeadRot(180);
                target.setXRot(0);
                // Entity-free runtime tears have no source position; provide an explicit registered
                // source position here to exercise the real shield rejection branch itself.
                DamageSource source = new DamageSource(helper.getLevel().registryAccess()
                        .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageType.TEAR),
                        null, owner, owner.position());
                helper.assertTrue(target.isBlocking() && target.isDamageSourceBlocked(source),
                        "Real shield faces the tear's owning attacker after warmup");
                float health = target.getHealth();
                helper.assertTrue(!target.hurt(source, 1) && target.getHealth() == health,
                        "The real shield rejects tear damage");
                target.stopUsingItem();
                target.setOnGround(true);
                target.setDeltaMovement(Vec3.ZERO);
                target.knockback(0.4, 1, 0);
                vector(helper, target.getDeltaMovement(), new Vec3(-0.4, 0.4, 0),
                        "Shield processing cannot retain tear policy for the next direct knockback");
                helper.succeed();
            } finally {
                owner.discard();
                target.discard();
            }
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void rawTearHasNoDirectionAndOrdinaryHitStaysNative(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            target.setOnGround(true);
            target.setDeltaMovement(0.2, 0.37, -0.1);
            DamageSource raw = new DamageSource(helper.getLevel().registryAccess()
                    .registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(ModDamageType.TEAR), null, owner);
            target.hurt(raw, 1);
            vector(helper, target.getDeltaMovement(), new Vec3(0.2, 0.37, -0.1),
                    "Registered tear without a velocity snapshot cannot invent a push direction");
            target.invulnerableTime = 0;
            target.hurt(target.damageSources().mobAttack(owner), 1);
            vector(helper, target.getDeltaMovement(), new Vec3(0.5, 0.4, -0.05),
                    "Ordinary damage retains native velocity halving and ground lift");
        });
    }

    @GameTest(template = "trajectory_empty", batch = "damage_knockback")
    public static void freezeAndHolyShieldCancelTearThroughRealForgeListeners(GameTestHelper helper) {
        pair(helper, (owner, target) -> {
            Vec3 initial = new Vec3(0.2, 0.37, -0.1);
            target.addEffect(new MobEffectInstance(ModEffects.PETRIFIED.get(), 100));
            target.setDeltaMovement(initial);
            canceledEvent(helper, target,
                    () -> target.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
            vector(helper, target.getDeltaMovement(), initial, "Petrified Forge listener prevents tear movement");
            var player = helper.makeMockSurvivalPlayer();
            try {
                player.setPos(target.position());
                player.addEffect(new MobEffectInstance(ModEffects.HOLY_SHIELD.get(), 100));
                player.setDeltaMovement(initial);
                canceledEvent(helper, player,
                        () -> player.hurt(ModDamageSources.tear(helper.getLevel(), owner, new Vec3(1, 0, 0)), 1));
                vector(helper, player.getDeltaMovement(), initial, "Holy shield Forge listener prevents tear movement");
            } finally {
                player.discard();
            }
        });
    }

    private static void canceledEvent(GameTestHelper helper, LivingEntity target, Runnable action) {
        int[] calls = {0};
        Consumer<LivingKnockBackEvent> listener = event -> {
            if (event.getEntity() != target) return;
            calls[0]++;
            helper.assertTrue(event.isCanceled(), "Existing Forge effect listener cancels the dispatched event");
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, LivingKnockBackEvent.class, listener);
        try {
            action.run();
            helper.assertTrue(calls[0] == 1, "Effect policy receives an actual hurt knockback event");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    private static void pair(GameTestHelper helper, BiConsumer<Cow, Cow> test) {
        Cow owner = cow(helper, 0);
        Cow target = cow(helper, 2);
        try {
            test.accept(owner, target);
        } finally {
            owner.discard();
            target.discard();
        }
        helper.succeed();
    }

    private static void listen(LivingEntity target, Consumer<LivingKnockBackEvent> callback, Runnable action) {
        Consumer<LivingKnockBackEvent> listener = event -> {
            if (event.getEntity() == target) callback.accept(event);
        };
        MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, LivingKnockBackEvent.class, listener);
        try {
            action.run();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    private static void vector(GameTestHelper helper, Vec3 actual, Vec3 expected, String message) {
        helper.assertTrue(actual.distanceToSqr(expected) < 1e-14, message + ": " + actual + " != " + expected);
    }

    private static Cow cow(GameTestHelper helper, int offset) {
        Cow cow = EntityType.COW.create(helper.getLevel());
        cow.setNoAi(true);
        cow.setPos(helper.absoluteVec(new Vec3(1 + offset, 2, 1)));
        helper.getLevel().addFreshEntity(cow);
        return cow;
    }

    private static void close(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < 1e-7, message + ": " + actual + " != " + expected);
    }
}
