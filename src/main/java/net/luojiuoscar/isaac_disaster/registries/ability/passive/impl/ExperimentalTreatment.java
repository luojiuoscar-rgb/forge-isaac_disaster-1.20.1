package net.luojiuoscar.isaac_disaster.registries.ability.passive.impl;

import net.luojiuoscar.isaac_disaster.event.custom.misc.PassiveItemFirstObtainEvent;
import net.luojiuoscar.isaac_disaster.event.custom.misc.PassiveItemObtainEvent;
import net.luojiuoscar.isaac_disaster.item.item.PassiveItem;
import net.luojiuoscar.isaac_disaster.item.item.custom.ExperimentalTreatmentItem;
import net.luojiuoscar.isaac_disaster.manager.StatManager;
import net.luojiuoscar.isaac_disaster.registries.ability.passive.PassiveAbility;
import net.luojiuoscar.isaac_disaster.registries.ability.set.ModSetAbility;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ExperimentalTreatment extends PassiveAbility {

    public ExperimentalTreatment(int id, int level) {
        super(id, level);
    }

    @Override
    public void onObtain(ServerPlayer player, @Nullable ItemStack stack){
        if (stack != null && !PassiveItem.hasBeenUsed(stack)) {
            PassiveItemFirstObtainEvent e2 = new PassiveItemFirstObtainEvent(player, stack, this);
            MinecraftForge.EVENT_BUS.post(e2);

            handleFirstObtain(player, stack);
        }

        PassiveItemObtainEvent e1 = new PassiveItemObtainEvent(player, stack, this);
        MinecraftForge.EVENT_BUS.post(e1);

        handleObtain(player, stack);
    }

    @Override
    public void handleFirstObtain(ServerPlayer player, @Nullable ItemStack stack) {
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
    public void handleObtain(ServerPlayer player, @Nullable ItemStack stack) {
        if (stack == null) return;
        StatManager.modifySetWithId(player, ModSetAbility.SPUN.getId(), 1);
        Map<UUID, Double> map = ExperimentalTreatmentItem.getModifierMap(stack);

        for (Map.Entry<UUID, Double> entry : map.entrySet()){
            UUID uuid = entry.getKey();
            Double value = entry.getValue();

            StatManager.fromUUID(uuid).apply(player, value);
        }
    }

    @Override
    public void handleRemove(ServerPlayer player, @Nullable ItemStack stack) {
        if (stack == null) return;
        StatManager.modifySetWithId(player, ModSetAbility.SPUN.getId(), -1);

        Map<UUID, Double> map = ExperimentalTreatmentItem.getModifierMap(stack);

        for (Map.Entry<UUID, Double> entry : map.entrySet()){
            UUID uuid = entry.getKey();
            Double value = entry.getValue();

            StatManager.fromUUID(uuid).apply(player, -value);
        }
    }

    @Override
    public List<Component> getDesc(@Nullable ItemStack stack) {
        if (stack != null){
            Map<UUID, Double> map = ExperimentalTreatmentItem.getModifierMap(stack);

            if (!map.isEmpty()) {

                List<Component> desc = new ArrayList<>();

                for (Map.Entry<UUID, Double> entry : map.entrySet()) {
                    UUID key = entry.getKey();
                    Double value = entry.getValue();

                    if (value < 0){
                        desc.add(StatManager.fromUUID(key).description(value, Style.EMPTY.withColor(ChatFormatting.RED)));
                    }else if (value <= 1){
                        desc.add(StatManager.fromUUID(key).description(value, Style.EMPTY.withColor(ChatFormatting.GREEN)));
                    }else {
                        desc.add(StatManager.fromUUID(key).description(value, Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));
                    }
                }

                return desc;
            }
        }

        return List.of(
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.1"),
                Component.translatable("item.isaac_disaster.experimental_treatment.lore.2")
        );
    }

    @Override
    public List<Component> getSynergyDesc(@Nullable ItemStack stack){
        return ModSetAbility.SPUN.get().getSynergyDesc();
    }

    @Override
    public List<Component> getExtraDesc(@Nullable ItemStack stack){
        return ModSetAbility.SPUN.get().getExtraDesc();
    }
}
