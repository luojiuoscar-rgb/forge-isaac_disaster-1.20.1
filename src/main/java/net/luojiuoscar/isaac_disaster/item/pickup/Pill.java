package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerAbilityProvider;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

/**
 * 与PICKUP不属于同类
 */
public class Pill extends Item {
    private final boolean isHorsePill;
    private final int pillId;

    public Pill(Properties pProperties, int pillId, boolean isHorsePill) {
        super(pProperties.rarity(Rarity.UNCOMMON));
        this.isHorsePill = isHorsePill;
        this.pillId = pillId;
        PillEffectManager.getInstance().registerNewPillId(pillId); // 在注册物品的时候，加入列表
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(@NotNull Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // 触发药丸效果
        if (!level.isClientSide){
            if (isHorsePill){
                PillEffectManager.getInstance().getEffectFromPill(pillId).onUseH(player);
            }else {
                PillEffectManager.getInstance().getEffectFromPill(pillId).onUse(player);
            }

            // 如果没有有正确的药丸记录，则更新
            if (!ClientDataManager.getInstance().isPillRecordCorrectly(pillId)){
                int effectId = PillEffectManager.getInstance().getEffectIdFromPill(pillId);
                player.getCapability(PlayerAbilityProvider.PLAYER_ABILITY).ifPresent(
                        playerAbility -> playerAbility.setPillRecord((ServerPlayer) player, pillId, effectId)
                );
            }
        }


        // 物品-1
        if (!(player.isCreative() || player.isSpectator())) {
            stack.shrink(1);
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull String getDescriptionId(){
        if (ClientDataManager.getInstance().isPillRecordCorrectly(pillId) ||
        ClientDataManager.getInstance().getCountFromId(ItemId.PHD.getId()) > 0 ||
                ClientDataManager.getInstance().getCountFromId(ItemId.FALSE_PHD.getId()) > 0){

            return PillEffectManager.getInstance().getEffectFromPill(pillId).getDescriptionId();
        }else{
            // 使用原名
            return this.getOrCreateDescriptionId();
        }
    }


    public boolean isHorsePill() {
        return isHorsePill;
    }
    public int getPillId(){
        return this.pillId;
    }
}
