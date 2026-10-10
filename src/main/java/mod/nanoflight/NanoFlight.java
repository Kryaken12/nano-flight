package mod.nanoflight;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Полет в нано-нагруднике.
 * Стоимость = TechRebornConfig.quantumSuitFlyingCost * 3 (за тик полета).
 */
public class NanoFlight implements ModInitializer {
    private static final Logger LOG = LoggerFactory.getLogger("nano_flight");

    // Во сколько раз полет нано дороже, чем у квантовой брони
    private static final long COST_MULTIPLIER = 3;

    private static final AbilitySource SOURCE =
            Pal.getAbilitySource(new Identifier("nano_flight", "nano_armor"), -1000);

    private static Class<?> energyClass;
    private static Method getStored;
    private static Method tryUse;
    private static Field costField;

    @Override
    public void onInitialize() {
        try {
            energyClass = Class.forName("reborncore.common.powerSystem.RcEnergyItem");
            getStored = energyClass.getMethod("getStoredEnergy", ItemStack.class);
            tryUse = energyClass.getMethod("tryUseEnergy", ItemStack.class, long.class);
            costField = Class.forName("techreborn.config.TechRebornConfig")
                    .getField("quantumSuitFlyingCost");
        } catch (Throwable t) {
            LOG.error("Tech Reborn API not found, nano flight disabled", t);
            return;
        }

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    private static boolean isNanoChestplate(ItemStack stack) {
        if (stack.isEmpty()) return false;
        Item item = stack.getItem();
        if (!(item instanceof ArmorItem armor) || armor.getSlotType() != EquipmentSlot.CHEST) return false;
        if (!energyClass.isInstance(item)) return false;
        return Registries.ITEM.getId(item).getPath().contains("nano");
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        if (player.isSpectator()) return;

        ItemStack stack = player.getEquippedStack(EquipmentSlot.CHEST);
        boolean canFly = false;

        try {
            if (isNanoChestplate(stack)) {
                long cost = costField.getLong(null) * COST_MULTIPLIER;
                Object item = stack.getItem();
                long stored = (long) getStored.invoke(item, stack);
                canFly = stored >= cost;

                if (canFly && player.getAbilities().flying && !player.isCreative()) {
                    canFly = (boolean) tryUse.invoke(item, stack, cost);
                }
            }
        } catch (Throwable t) {
            canFly = false;
        }

        boolean granted = SOURCE.grants(player, VanillaAbilities.ALLOW_FLYING);
        if (canFly && !granted) {
            SOURCE.grantTo(player, VanillaAbilities.ALLOW_FLYING);
        } else if (!canFly && granted) {
            SOURCE.revokeFrom(player, VanillaAbilities.ALLOW_FLYING);
            if (!player.isCreative() && !VanillaAbilities.ALLOW_FLYING.isEnabledFor(player)
                    && player.getAbilities().flying) {
                player.getAbilities().flying = false;
                player.sendAbilitiesUpdate();
            }
        }
    }
}
