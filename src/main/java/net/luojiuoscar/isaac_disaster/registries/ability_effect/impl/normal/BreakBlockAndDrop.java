package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ModExecutableEffects;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = IsaacDisaster.MOD_ID)
public class BreakBlockAndDrop implements IAbilityEffect {
    private static final double PROTECTION_RADIUS = 3;
    private static final ThreadLocal<ServerLevel> SMELTING_DROPS = new ThreadLocal<>();

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        Vec3 position = context.get(ContextKeys.TARGET_POSITION);
        int amplifier = context.getOrDefault(ContextKeys.AMPLIFIER, 1.).intValue();
        if (position == null || !Double.isFinite(position.x) || !Double.isFinite(position.y)
                || !Double.isFinite(position.z) || context.getEntity() == null
                || !(context.getEntity().level() instanceof ServerLevel level)) return false;

        BlockPos pos = BlockPos.containing(position);
        if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) return false;
        var bullet = context.get(ContextKeys.BULLET);
        boolean fireMind = bullet != null && bullet.getOwner() == context.getEntity()
                && bullet.getTriggers().contains(ModExecutableEffects.BREAK_BLOCK_AND_DROP.get())
                && bullet.getTriggers().contains(ModExecutableEffects.FIRE_MIND_IMPACT.get());
        if (fireMind) {
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class,
                    new AABB(position, position).inflate(PROTECTION_RADIUS),
                    item -> item.isAlive() && item.distanceToSqr(position) <= PROTECTION_RADIUS * PROTECTION_RADIUS)) {
                item.setInvulnerable(true);
            }
        }
        BlockState state = level.getBlockState(pos);

        // 非气体、液体；硬度在范围内
        if (!state.getCollisionShape(level, pos).isEmpty()){

            var destroySpeed = state.getDestroySpeed(level, pos);

            if (destroySpeed <= (amplifier > 1 ? 50 : 5) && destroySpeed > 0){
                if (!fireMind) {
                    level.destroyBlock(pos, true);
                } else {
                    ServerLevel previous = SMELTING_DROPS.get();
                    SMELTING_DROPS.set(level);
                    try {
                        level.destroyBlock(pos, true);
                    } finally {
                        if (previous == null) SMELTING_DROPS.remove();
                        else SMELTING_DROPS.set(previous);
                    }
                }
            }
        }
        return true;
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onTerraDrop(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide || !(event.getEntity() instanceof ItemEntity item)) return;
        ServerLevel level = SMELTING_DROPS.get();
        if (level == null || level != event.getLevel()) return;
        var stacks = smeltOnce(level, item.getItem());
        if (stacks.isEmpty()) return;
        item.setInvulnerable(true);
        item.setItem(stacks.get(0));
        if (stacks.size() == 1) return;

        // Overflow is already cooked. Spawn outside the scope so it cannot be cooked a second time.
        SMELTING_DROPS.remove();
        try {
            CompoundTag data = new CompoundTag();
            item.saveWithoutId(data);
            data.remove("UUID");
            for (int i = 1; i < stacks.size(); i++) {
                ItemEntity extra = new ItemEntity(level, item.getX(), item.getY(), item.getZ(), ItemStack.EMPTY);
                extra.load(data);
                extra.setItem(stacks.get(i));
                level.addFreshEntity(extra);
            }
        } finally {
            SMELTING_DROPS.set(level);
        }
    }

    private static List<ItemStack> smeltOnce(ServerLevel level, ItemStack stack) {
        if (stack.isEmpty()) return List.of();
        var input = new SimpleContainer(stack.copyWithCount(1));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.SMELTING, input, level);
        if (recipe.isEmpty()) return List.of(stack.copy());
        ItemStack result = recipe.get().assemble(input, level.registryAccess());
        if (result.isEmpty()) return List.of(stack.copy());
        long remaining = (long) stack.getCount() * result.getCount();
        int limit = Math.max(1, result.getMaxStackSize());
        List<ItemStack> output = new ArrayList<>();
        while (remaining > 0) {
            int count = (int) Math.min(remaining, limit);
            output.add(result.copyWithCount(count));
            remaining -= count;
        }
        return output;
    }
}
