package mod.nanoflight;

import io.github.ladysnake.pal.AbilitySource;
import io.github.ladysnake.pal.Pal;
import io.github.ladysnake.pal.VanillaAbilities;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import reborncore.common.powerSystem.RcEnergyItem;
import techreborn.config.TechRebornConfig;

public class NanoFlight implements ModInitializer {

    private static final Identifier NANO_CHESTPLATE = new Identifier("techreborn", "nano_chestplate");

    private static final AbilitySource SOURCE =
            Pal.getAbilitySource(new Identifier("nano_flight", "nano_armor"), -1000);

    @Override
    public void onInitialize() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                tickPlayer(player);
            }
        });
    }

    private static boolean isNanoChestplate(ItemStack stack) {
        return !stack.isEmpty()
                && Registries.ITEM.getId(stack.getItem()).equals(NANO_CHESTPLATE)
                && stack.getItem() instanceof RcEnergyItem;
    }

    private static void tickPlayer(ServerPlayerEntity player) {
        if (player.isSpectator()) {
            return;
        }

        ItemStack stack = player.getEquippedStack(EquipmentSlot.CHEST);
        long cost = TechRebornConfig.quantumSuitFlyingCost;

        boolean canFly = false;
        if (isNanoChestplate(stack)) {
            RcEnergyItem item = (RcEnergyItem) stack.getItem();
            canFly = item.getStoredEnergy(stack) >= cost;

            if (canFly && player.getAbilities().flying && !player.isCreative()) {
                canFly = item.tryUseEnergy(stack, cost);
            }
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
