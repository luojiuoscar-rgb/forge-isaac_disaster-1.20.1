package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.event.custom.misc.PickupUseEvent;
import net.luojiuoscar.isaac_disaster.item.item.IIgnoreRecord;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.PillEffectManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.NotNull;

/**
 * 与PICKUP不属于同类
 */
public class Pill extends Item implements ICommonPickup {
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

        // 触发事件
        PickupUseEvent event = new PickupUseEvent(player, stack);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) return InteractionResultHolder.pass(stack);

        // 触发药丸效果
        if (!level.isClientSide){
            if (isHorsePill){
                PillEffectManager.getInstance().getEffectFromPill(pillId).onUseH(player, true);
            }else {
                PillEffectManager.getInstance().getEffectFromPill(pillId).onUse(player, true);
            }

            // 如果没有有正确的药丸记录，则更新
            if (!(stack.getItem() instanceof IIgnoreRecord) &&
                    !ClientDataManager.getInstance().isPillRecordCorrectly(pillId)){
                int effectId = PillEffectManager.getInstance().getEffectIdFromPill(pillId);

                player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                        playerItemUseRecord -> {
                            playerItemUseRecord.setPillEffectRecord((ServerPlayer) player, pillId, effectId);
                            playerItemUseRecord.addPillRecord(pillId, isHorsePill);
                            playerItemUseRecord.addPillEffectRecord(
                                    PillEffectManager.getInstance().getEffectIdFromPill(pillId), isHorsePill);
                        });
            }}

        if (!(player.isCreative() || player.isSpectator())) stack.shrink(1);
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
