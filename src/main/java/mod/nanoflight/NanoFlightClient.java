package mod.nanoflight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.Vec3d;

/**
 * Клиентская часть: во время полета в нано-нагруднике игрок медленно опускается,
 * пока не нажаты пробел или Shift. Горизонтальное движение не затрагивается.
 */
public class NanoFlightClient implements ClientModInitializer {

    // Скорость снижения (блоков за тик). 0.02 = медленно. Больше число - быстрее вниз.
    private static final double SINK = 0.09;

    @Override
    public void onInitializeClient() {
        ClientTickEvents.START_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null || client.isPaused()) return;
            if (player.isCreative() || player.isSpectator()) return;
            if (!player.getAbilities().flying || player.isOnGround()) return;
            if (player.input.jumping || player.input.sneaking) return;
            if (!isNanoChestplate(player.getEquippedStack(EquipmentSlot.CHEST))) return;

            Vec3d v = player.getVelocity();
            player.setVelocity(v.x, v.y - SINK, v.z);
        });
    }

    private static boolean isNanoChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (!(item instanceof ArmorItem armor) || armor.getSlotType() != EquipmentSlot.CHEST) return false;
        return Registries.ITEM.getId(item).getPath().contains("nano");
    }
}
