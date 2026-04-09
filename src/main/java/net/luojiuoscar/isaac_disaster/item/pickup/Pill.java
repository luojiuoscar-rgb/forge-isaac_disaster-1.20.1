package net.luojiuoscar.isaac_disaster.item.pickup;

import net.luojiuoscar.isaac_disaster.capability.player.PlayerItemUseRecordProvider;
import net.luojiuoscar.isaac_disaster.client.ClientDataManager;
import net.luojiuoscar.isaac_disaster.event.custom.misc.PickupUseEvent;
import net.luojiuoscar.isaac_disaster.item.item.IIgnoreRecord;
import net.luojiuoscar.isaac_disaster.item.pickup.interfaces.ICommonPickup;
import net.luojiuoscar.isaac_disaster.manager.PillEffectManager;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ContextKeys;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.ExecutableEffectContext;
import net.luojiuoscar.isaac_disaster.registries.ability_effect.impl.pill_effect.PillEffect;
import net.minecraft.resources.ResourceLocation;
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

import java.util.List;

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

        // 在注册物品的时候，加入列表
        PillEffectManager.getInstance().registerNewPillId(pillId);
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

            PillEffect effect = (PillEffect) PillEffectManager.getInstance().getEffectFromPill(pillId).get();

            ExecutableEffectContext context = new ExecutableEffectContext(player);
            context.set(ContextKeys.BOOLEAN, List.of(isHorsePill));
            effect.applyEffect(context);

            // 如果没有有正确的药丸记录，则更新
            if (!(stack.getItem() instanceof IIgnoreRecord) &&
                    !ClientDataManager.getInstance().isPillRecordCorrectly(pillId)){

                ResourceLocation effectId =
                        PillEffectManager.getInstance().getEffectFromPill(pillId).getId();


                player.getCapability(PlayerItemUseRecordProvider.PLAYER_ITEM_USE_RECORD).ifPresent(
                        playerItemUseRecord -> {
                            // 添加使用的映射关系，药丸id，效果id
                            playerItemUseRecord.setPillEffectRecord((ServerPlayer) player, pillId, effectId);
                            playerItemUseRecord.addPillRecord(pillId, isHorsePill);
                            playerItemUseRecord.addPillEffectRecord(
                                    PillEffectManager.getInstance().getEffectIdFromPill(pillId).getId(), isHorsePill);
                        });
            }}

        if (!(player.isCreative() || player.isSpectator())) stack.shrink(1);
        return InteractionResultHolder.success(stack);
    }

    @Override
    public @NotNull String getDescriptionId(){
        // 当有办法显示名称的时候
        if (ClientDataManager.getInstance().isPillRecordCorrectly(pillId)
                || ClientDataManager.getInstance().getCountFromId(ItemId.PHD.getId()) > 0
                || ClientDataManager.getInstance().getCountFromId(ItemId.FALSE_PHD.getId()) > 0){

            return ((PillEffect) PillEffectManager.getInstance().getEffectFromPill(pillId).get())
                    .getDescriptionId(ClientDataManager.getInstance().getPillQuality());

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
