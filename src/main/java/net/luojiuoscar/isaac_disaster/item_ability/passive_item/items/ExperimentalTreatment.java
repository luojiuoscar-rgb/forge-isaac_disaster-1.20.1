package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.item.ModItems;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.item.custom.ExperimentalTreatmentItem;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IPassiveItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.manager.id_managers.ItemId;
import net.luojiuoscar.isaac_disaster.manager.id_managers.SetId;
import net.luojiuoscar.isaac_disaster.manager.item_managers.SetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExperimentalTreatment implements IPassiveItem {
    @Override
    public int getItemId() {
        return ItemId.EXPERIMENTAL_TREATMENT.getId();
    }

    @Override
    public void onObtain(Player player, @Nullable ItemStack stack){
        if (stack != null && !PassiveItem.isConsumed(stack)) onFirstObtain(player, stack);
        onObtainEffect(player, stack);
    }

    @Override
    public void onFirstObtain(Player player, @Nullable ItemStack stack) {
        if (stack == null) return;

        // 当玩家首次获得该物品时触发，生成若干随机属性
        RandomSource random = player.getRandom();
        // 随机 3~8 条属性
        int total = random.nextInt(3, 9);
        List<UUID> uuidList = new ArrayList<>(StatManager.getAllNormalTypeUUID().stream().toList());
        Collections.shuffle(uuidList);
        Set<UUID> picked = new HashSet<>(uuidList.subList(0, total));

        for (UUID uuid : picked){
            double ratio = Math.round(random.nextDouble() * 250 - 100) / 100.0; // 保留两位小数
            ExperimentalTreatmentItem.setModifier(stack, uuid, ratio); // 记录筛选出来的内容
        }
    }

    @Override
    public void onObtainEffect(Player player, @Nullable ItemStack stack) {
        if (stack == null) return;
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), 1);
        Map<UUID, Double> map = ExperimentalTreatmentItem.getModifierMap(stack);

        for (Map.Entry<UUID, Double> entry : map.entrySet()){
            UUID uuid = entry.getKey();
            Double value = entry.getValue();

            StatManager.fromUUID(uuid).apply(player, value);
        }
    }

    @Override
    public void onRemove(Player player, @Nullable ItemStack stack) {
        if (stack == null) return;
        StatManager.modifySetWithId(player, SetId.SPUN.getId(), -1);

        Map<UUID, Double> map = ExperimentalTreatmentItem.getModifierMap(stack);

        for (Map.Entry<UUID, Double> entry : map.entrySet()){
            UUID uuid = entry.getKey();
            Double value = entry.getValue();

            StatManager.fromUUID(uuid).apply(player, -value);
        }
    }

    @Override
    public ItemStack getItemStack(){
        return new ItemStack(ModItems.EXPERIMENTAL_TREATMENT.get());
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.1"),
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDescription(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getSynergyDescription();
    }

    @Override
    public List<Component> getExplain(){
        return SetManager.getInstance().getSetFromId(SetId.SPUN.getId()).getExplain();
    }
}
