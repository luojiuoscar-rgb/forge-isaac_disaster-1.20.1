package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public class GoldenPill extends Item implements ICommonPickup {
    private final boolean isHorsePill;

    public GoldenPill(Item.Properties pProperties, boolean isHorsePill) {
        super(pProperties.stacksTo(1).rarity(Rarity.RARE));
        this.isHorsePill = isHorsePill;
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 触发药丸效果
        if (player instanceof ServerPlayer serverPlayer){
            PillEffectManager.getInstance().triggerRandomEffect(serverPlayer, isHorsePill);
        }

        // 物品-1
        if (!(player.isCreative() || player.isSpectator())) {
            RandomSource random = player.getRandom();
            // 5%概率不消耗
            if (random.nextDouble() < 0.05){
                stack.shrink(1);
            }
        }

        return InteractionResultHolder.success(stack);
    }

    public boolean isHorsePill() {
        return isHorsePill;
    }}
