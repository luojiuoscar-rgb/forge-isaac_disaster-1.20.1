package net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.normal;

import net.luojiuoscar.isaac_disaster.manager.TagManager;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.IAbilityEffect;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public final class D12 implements IAbilityEffect {
    private static volatile List<BlockState> cachedCandidates;

    @Override
    public boolean applyEffect(ExecutableEffectContext context) {
        Vec3 position = context.get(ContextKeys.TARGET_POSITION);
        if (position == null || !(context.getEntity().level() instanceof ServerLevel level)) return false;

        List<BlockState> candidates = candidateStates();
        if (candidates.isEmpty()) return false;

        RandomSource random = level.getRandom();
        for (BlockPos pos : cubePositions(BlockPos.containing(position))) {
            if (isExcluded(level.getBlockState(pos))) continue;

            BlockState replacement = candidates.get(random.nextInt(candidates.size()));
            level.setBlock(pos, replacement,
                    Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
        }
        return true;
    }

    static boolean isExcluded(BlockState state) {
        return state.is(TagManager.D12_EXCLUDED_BLOCKS)
                || state.isAir()
                || !state.getFluidState().isEmpty();
    }

    static List<BlockPos> cubePositions(BlockPos center) {
        List<BlockPos> positions = new ArrayList<>(125);
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    positions.add(center.offset(x, y, z));
                }
            }
        }
        return positions;
    }

    private static List<BlockState> candidateStates() {
        List<BlockState> candidates = cachedCandidates;
        if (candidates != null) return candidates;

        synchronized (D12.class) {
            candidates = cachedCandidates;
            if (candidates == null) {
                candidates = BuiltInRegistries.BLOCK.stream()
                        .map(Block::defaultBlockState)
                        .filter(state -> !isExcluded(state))
                        .toList();
                cachedCandidates = candidates;
            }
        }
        return candidates;
    }
}
