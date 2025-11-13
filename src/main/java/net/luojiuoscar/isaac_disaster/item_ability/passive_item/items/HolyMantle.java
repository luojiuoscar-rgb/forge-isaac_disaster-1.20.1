package net.luojiuoscar.isaac_disaster.item_ability.passive_item.items;

import net.luojiuoscar.isaac_disaster.effect.ModEffects;
import net.luojiuoscar.isaac_disaster.helper.EntityHelper;
import net.luojiuoscar.isaac_disaster.item_ability.passive_item.IRecursivePassiveItem;
import net.luojiuoscar.isaac_disaster.manager.id.ItemId;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class HolyMantle implements IRecursivePassiveItem {
    @Override
    public int getItemId() {
        return ItemId.HOLY_MANTLE.getId();
    }

    @Override
    public void handleFirstObtain(Player player, @Nullable ItemStack stack) {

    }

    @Override
    public void handleObtain(Player player, @Nullable ItemStack stack) {
        // 考虑到可能可拆装，不会在获取时立刻给予一层神圣护盾
    }

    @Override
    public void handleRemove(Player player, @Nullable ItemStack stack) {
    }

    @Override
    public List<Component> getDescription() {
        return List.of(
                Component.translatable("item.isaac_disaster.holy_mantle.lore.1")
        );
    }

    @Override
    public List<Component> getExplain(){
        List<Component> description = new ArrayList<>();

        description.add(Component.translatable("effect.isaac_disaster.holy_shield").append(": ")
                .append(Component.translatable("effect.isaac_disaster.holy_shield.explain.1")));
        description.add(Component.translatable("effect.isaac_disaster.holy_shield.explain.2"));

        return description;
    }

    @Override
    public int getTickInterval() {
        return 400; // 20s
    }

    @Override
    public void recursiveEffect(Player player) {
        EntityHelper.addAmplifier(player, ModEffects.HOLY_SHIELD.get());
    }
}
