package net.luojiuoscar.isaac_disaster.gametest;

import com.mojang.authlib.GameProfile;
import net.luojiuoscar.isaac_disaster.bullet.core.BulletState;
import net.luojiuoscar.isaac_disaster.event.custom.attack.IsaacAttackHitBlockEvent;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.FireMindAttachment;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.attachment.TerraAttachment;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal.BreakBlockAndDrop;
import net.luojiuoscar.isaac_disaster.registries.attack_type.AttackContext;
import net.luojiuoscar.isaac_disaster.registries.attack_type.ModAttackTypes;
import net.luojiuoscar.isaac_disaster.registries.bullet_color.ModBulletColors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GameTestHolder("isaac_disaster_fire_mind_terra")
@PrefixGameTestTemplate(false)
public final class FireMindTerraGameTests {
    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void terraAloneKeepsOriginalDropsAndFireMindSmeltsNewDrops(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var normal = f.breakBlock(Blocks.IRON_ORE, false);
            helper.assertTrue(count(normal, Items.RAW_IRON) == 1 && count(normal, Items.IRON_INGOT) == 0,
                    "Terra without the attachment retains original ore drops");
            helper.assertTrue(normal.stream().noneMatch(ItemEntity::isInvulnerable), "Ordinary drops are not globally protected");
            var hot = f.breakBlock(Blocks.IRON_ORE, true);
            helper.assertTrue(count(hot, Items.IRON_INGOT) == 1 && count(hot, Items.RAW_IRON) == 0,
                    "The combined projectile smelts raw iron into an ingot");
            helper.assertTrue(hot.stream().allMatch(ItemEntity::isInvulnerable), "New drops are already invulnerable on spawn");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void onlyOneSmeltingStepIsAppliedAndMissingRecipesKeepTheirDrops(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var stone = f.breakBlock(Blocks.STONE, true);
            helper.assertTrue(count(stone, Items.STONE) == 1 && count(stone, Items.SMOOTH_STONE) == 0,
                    "The original cobblestone drop is smelted once, never recursively");
            var diamond = f.breakBlock(Blocks.DIAMOND_ORE, true);
            helper.assertTrue(count(diamond, Items.DIAMOND) == 1 && diamond.stream().allMatch(ItemEntity::isInvulnerable),
                    "A drop with no smelting recipe is retained and protected");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void nearbyExistingDropsAreProtectedWithinASphereButAreNotSmelted(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ItemEntity inside = f.item(new ItemStack(Items.RAW_IRON, 4), f.center.add(2.9, 0, 0));
            ItemEntity outside = f.item(new ItemStack(Items.RAW_IRON, 3), f.center.add(3.1, 0, 0));
            ItemEntity corner = f.item(new ItemStack(Items.RAW_IRON), f.center.add(2.5, 0, 2.5));
            f.breakBlock(Blocks.STONE, true);
            helper.assertTrue(inside.isInvulnerable() && inside.getItem().is(Items.RAW_IRON) && inside.getItem().getCount() == 4,
                    "Nearby old drops are protected without being smelted");
            helper.assertTrue(!outside.isInvulnerable() && !corner.isInvulnerable(), "Only the three-block spherical area is protected");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void unbreakableBlockImpactsStillProtectNearbyDrops(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ItemEntity nearby = f.item(new ItemStack(Items.APPLE), f.center.add(1, 0, 0));
            var drops = f.breakBlock(Blocks.BEDROCK, true);
            helper.assertTrue(drops.isEmpty() && f.level.getBlockState(f.block).is(Blocks.BEDROCK), "Terra hardness restrictions remain intact");
            helper.assertTrue(nearby.isInvulnerable(), "The collision still protects nearby drops even when Terra cannot break the block");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void protectedDropsUseVanillaInvulnerability(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ItemEntity drop = f.breakBlock(Blocks.IRON_ORE, true).get(0);
            helper.assertTrue(!drop.hurt(f.level.damageSources().inFire(), 100), "Contact fire cannot destroy a protected drop");
            helper.assertTrue(!drop.hurt(f.level.damageSources().lava(), 100), "Lava cannot damage an invulnerable drop");
            helper.assertTrue(drop.isInvulnerable() && !drop.hurt(f.level.damageSources().generic(), 100) && drop.isAlive(),
                    "Protection uses vanilla invulnerability, including ordinary non-fire damage");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void protectionSurvivesEntitySaveButDoesNotBecomeAnItemStackProperty(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            ItemEntity original = f.breakBlock(Blocks.IRON_ORE, true).get(0);
            ItemStack pickedUp = original.getItem().copy();
            CompoundTag saved = new CompoundTag();
            original.saveWithoutId(saved);
            original.discard();
            ItemEntity loaded = new ItemEntity(f.level, 0, 0, 0, ItemStack.EMPTY);
            loaded.load(saved);
            helper.assertTrue(f.level.addFreshEntity(loaded) && loaded.isInvulnerable(), "Vanilla invulnerability survives saved NBT");
            ItemEntity rethrown = f.item(pickedUp, f.center.add(1, 0, 0));
            helper.assertTrue(!pickedUp.hasTag() && !rethrown.isInvulnerable(), "Picking up and rethrowing does not grant a permanent item property");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void chestContentsFollowTheOriginalDropPipelineWithoutDuplication(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            f.level.setBlockAndUpdate(f.block, Blocks.CHEST.defaultBlockState());
            var chest = (ChestBlockEntity) f.level.getBlockEntity(f.block);
            chest.setItem(0, new ItemStack(Items.RAW_IRON, 6));
            var drops = f.breakExistingBlock(true);
            helper.assertTrue(count(drops, Items.IRON_INGOT) == 6 && count(drops, Items.RAW_IRON) == 0
                    && count(drops, Items.CHEST) == 1, "Container contents and the block drop are each processed exactly once");
            helper.assertTrue(drops.stream().allMatch(ItemEntity::isInvulnerable), "All resulting items are protected");
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void recipeOutputsMultiplyCountsAndSplitWithoutOverstacking(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var manager = f.level.getRecipeManager();
            List<Recipe<?>> original = new ArrayList<>(manager.getRecipes());
            List<Recipe<?>> changed = new ArrayList<>(original);
            changed.removeIf(recipe -> recipe.getType() == RecipeType.SMELTING
                    && recipe.getIngredients().stream().anyMatch(ingredient -> ingredient.test(new ItemStack(Items.COBBLESTONE))));
            changed.add(new SmeltingRecipe(ResourceLocation.fromNamespaceAndPath("isaac_disaster", "test_terra_three_bricks"),
                    "", CookingBookCategory.BLOCKS, Ingredient.of(Items.COBBLESTONE), new ItemStack(Items.BRICK, 3), 0, 200));
            try {
                manager.replaceRecipes(changed);
                f.level.setBlockAndUpdate(f.block, Blocks.CHEST.defaultBlockState());
                ((ChestBlockEntity) f.level.getBlockEntity(f.block)).setItem(0, new ItemStack(Items.COBBLESTONE, 64));
                var drops = f.breakExistingBlock(true);
                helper.assertTrue(count(drops, Items.BRICK) == 192, "The actual recipe output count is multiplied by the original drop count");
                helper.assertTrue(drops.stream().allMatch(item -> item.getItem().getCount() <= item.getItem().getMaxStackSize()), "Output stacks respect their item limit");
                helper.assertTrue(drops.stream().allMatch(ItemEntity::isInvulnerable), "Split output entities inherit invulnerability");
            } finally {
                manager.replaceRecipes(original);
            }
        }
        helper.succeed();
    }

    @GameTest(template = "trajectory_empty", batch = "fire_mind_terra")
    public static void tileDropGameruleAndOtherBreaksAreNotChanged(GameTestHelper helper) {
        try (Fixture f = new Fixture(helper)) {
            var rule = f.level.getGameRules().getRule(GameRules.RULE_DOBLOCKDROPS);
            boolean old = rule.get();
            try {
                rule.set(false, f.level.getServer());
                helper.assertTrue(f.breakBlock(Blocks.IRON_ORE, true).isEmpty(), "The original doTileDrops rule still suppresses drops");
            } finally {
                rule.set(old, f.level.getServer());
            }
            var normal = f.breakBlock(Blocks.IRON_ORE, false);
            helper.assertTrue(count(normal, Items.RAW_IRON) == 1 && normal.stream().noneMatch(ItemEntity::isInvulnerable),
                    "The temporary synergy processing does not leak into later ordinary breaks");
        }
        helper.succeed();
    }

    private static int count(List<ItemEntity> entities, Item item) {
        return entities.stream().filter(entity -> entity.getItem().is(item)).mapToInt(entity -> entity.getItem().getCount()).sum();
    }

    private static final class Fixture implements AutoCloseable {
        private final ServerLevel level;
        private final ServerPlayer owner;
        private final BlockPos block;
        private final Vec3 center;
        private final AABB bounds;
        private final Set<UUID> existing;

        private Fixture(GameTestHelper helper) {
            level = helper.getLevel();
            block = helper.absolutePos(new BlockPos(2, 2, 2));
            center = block.getCenter();
            bounds = new AABB(block).inflate(5);
            existing = items().stream().map(Entity::getUUID).collect(Collectors.toSet());
            UUID id = UUID.randomUUID();
            owner = FakePlayerFactory.get(level, new GameProfile(id, "terra_" + id.toString().substring(0, 7)));
            owner.setPos(center.add(0, 6, 0));
            level.addNewPlayer(owner);
        }

        private ItemEntity item(ItemStack stack, Vec3 position) {
            ItemEntity entity = new ItemEntity(level, position.x, position.y, position.z, stack);
            entity.setNoGravity(true);
            entity.setDeltaMovement(Vec3.ZERO);
            level.addFreshEntity(entity);
            return entity;
        }

        private List<ItemEntity> items() {
            return level.getEntitiesOfClass(ItemEntity.class, bounds, item -> !item.isRemoved());
        }

        private List<ItemEntity> breakBlock(Block state, boolean fireMind) {
            level.setBlockAndUpdate(block, state.defaultBlockState());
            return breakExistingBlock(fireMind);
        }

        private List<ItemEntity> breakExistingBlock(boolean fireMind) {
            Set<UUID> before = items().stream().map(Entity::getUUID).collect(Collectors.toSet());
            AttackContext attack = AttackContext.builder(owner, owner).attackType(ModAttackTypes.BULLET.get())
                    .damage(2.0).bulletScaleModifier(0.0).range(8).speed(1).position(center)
                    .mainAxis(new Vec3(1, 0, 0)).color(ModBulletColors.BASE.getId()).build();
            var preparation = new ExecutableEffectContext(owner);
            preparation.set(ContextKeys.ATTACK_CONTEXT, attack);
            new TerraAttachment().apply(preparation);
            if (fireMind) new FireMindAttachment().apply(preparation);
            var bullet = BulletState.from(attack).position(center).velocity(Vec3.ZERO).lifetime(8).build();
            var context = new ExecutableEffectContext(owner);
            context.set(ContextKeys.BULLET, bullet);
            context.set(ContextKeys.TARGET_POSITION, center);
            context.set(ContextKeys.EVENT, new IsaacAttackHitBlockEvent(bullet, owner, bullet.getTypeId(),
                    bullet.getTriggers(), new BlockHitResult(center, Direction.UP, block, false)));
            new BreakBlockAndDrop().applyEffect(context);
            return items().stream().filter(item -> !before.contains(item.getUUID())).toList();
        }

        @Override
        public void close() {
            for (ItemEntity item : items()) if (!existing.contains(item.getUUID())) item.discard();
            if (!owner.isRemoved()) level.removePlayerImmediately(owner, Entity.RemovalReason.DISCARDED);
        }
    }
}
