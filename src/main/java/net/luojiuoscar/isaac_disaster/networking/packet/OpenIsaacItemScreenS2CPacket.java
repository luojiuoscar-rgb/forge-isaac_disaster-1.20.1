package net.luojiuoscar.isaac_disaster.networking.packet;

import net.luojiuoscar.isaac_disaster.IsaacDisaster;
import net.luojiuoscar.isaac_disaster.screen.IsaacItemScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class OpenIsaacItemScreenS2CPacket {

    private final List<ItemStack> passiveItems;  // 第一个列表
    private final List<ItemStack> trinketItems;  // 第二个列表

    // 构造：服务器用
    public OpenIsaacItemScreenS2CPacket(List<ItemStack> passiveItems, List<ItemStack> trinketItems) {
        this.passiveItems = passiveItems;
        this.trinketItems = trinketItems;
    }

    // 解包：客户端接收时读取
    public OpenIsaacItemScreenS2CPacket(FriendlyByteBuf buf) {
        int passiveSize = buf.readInt();
        this.passiveItems = new ArrayList<>();
        for (int i = 0; i < passiveSize; i++) {
            this.passiveItems.add(buf.readItem());
        }

        int trinketSize = buf.readInt();
        this.trinketItems = new ArrayList<>();
        for (int i = 0; i < trinketSize; i++) {
            this.trinketItems.add(buf.readItem());
        }
    }

    // 序列化
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(passiveItems.size());
        for (ItemStack stack : passiveItems) {
            buf.writeItem(stack);
        }

        buf.writeInt(trinketItems.size());
        for (ItemStack stack : trinketItems) {
            buf.writeItem(stack);
        }
    }

    // 客户端处理
    public boolean handle(Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> {
            Minecraft mc = Minecraft.getInstance();

            if (mc.player != null) {

                IsaacDisaster.LOGGER.info("items: {}, trinkets: {}", passiveItems.size(), trinketItems.size());

                mc.setScreen(new IsaacItemScreen(passiveItems, trinketItems));
            }
        });

        return true;
    }
}
