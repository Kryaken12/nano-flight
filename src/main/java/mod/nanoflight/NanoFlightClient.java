package mod.nanoflight;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerAbilities;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;

/**
 * Клиентская часть: нано-нагрудник работает как джетпак.
 * Пробел зажат и игрок в воздухе - летит вверх. Отпустил - падает.
 * Сервер по-прежнему выдает способность летать и списывает энергию, пока игрок "летит".
 */
public class NanoFlightClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ClientPlayerEntity player = client.player;
            if (player == null) return;
            if (player.isCreative() || player.isSpectator()) return;

            PlayerAbilities abilities = player.getAbilities();
            if (!abilities.allowFlying) return;
            if (!isNanoChestplate(player.getEquippedStack(EquipmentSlot.CHEST))) return;

            boolean thrust = player.input.jumping && !player.isOnGround();
            if (abilities.flying != thrust) {
                abilities.flying = thrust;
                player.sendAbilitiesUpdate();
            }
        });
    }

    private static boolean isNanoChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (!(item instanceof ArmorItem armor) || armor.getSlotType() != EquipmentSlot.CHEST) return false;
        return Registries.ITEM.getId(item).getPath().contains("nano");
    }
}
