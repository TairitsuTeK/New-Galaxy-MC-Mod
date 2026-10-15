package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class JupiterEnvironmentHandler {

    private static final int BASE_DAMAGE_INTERVAL = 20;
    private static final float BASE_DAMAGE_AMOUNT = 3.0f;

    private static final int BASE_ARMOR_INTERVAL = 100;
    private static final int BASE_ARMOR_AMOUNT = 6;

    private static final int MESSAGE_INTERVAL = 20;

    private static final float SMOOTH_FACTOR = 0.01f;

    private static float smoothedStrength = 1.0f;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        // 目标强度
        float targetStrength = JupiterStormManager.getStormStrength();
        targetStrength = Math.max(0.5f, Math.min(2.5f, targetStrength));

        // 平滑跟随
        smoothedStrength += (targetStrength - smoothedStrength) * SMOOTH_FACTOR;

        // 兜底
        if (Float.isNaN(smoothedStrength)) smoothedStrength = 1.0f;
        smoothedStrength = Math.max(0.5f, Math.min(2.5f, smoothedStrength));

        // 用平滑后的强度算伤害/耐久
        int damageInterval = Math.max(5, (int) (BASE_DAMAGE_INTERVAL / smoothedStrength));
        float damageAmount = BASE_DAMAGE_AMOUNT * smoothedStrength;

        int armorInterval = Math.max(20, (int) (BASE_ARMOR_INTERVAL / smoothedStrength));
        int armorAmount = Math.max(1, (int) (BASE_ARMOR_AMOUNT * smoothedStrength));

        boolean protectedByArmor = hasFullProtectiveArmor(player);

        if (!protectedByArmor) {
            if (player.tickCount % damageInterval == 0) {
                player.hurt(player.damageSources().generic(), damageAmount);
            }
            if (player.tickCount % MESSAGE_INTERVAL == 0) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.jupiter.no_armor"),
                        true
                );
            }
        } else {
            if (player.tickCount % armorInterval == 0) {
                damageArmorPiece(player, EquipmentSlot.HEAD, armorAmount);
                damageArmorPiece(player, EquipmentSlot.CHEST, armorAmount);
            }
        }
    }

    private static boolean hasFullProtectiveArmor(ServerPlayer player) {
        EquipmentSlot[] slots = {
                EquipmentSlot.HEAD,
                EquipmentSlot.CHEST,
                EquipmentSlot.LEGS,
                EquipmentSlot.FEET
        };
        for (EquipmentSlot slot : slots) {
            ItemStack stack = player.getItemBySlot(slot);
            if (!isProtectiveArmor(stack)) return false;
        }
        return true;
    }

    private static boolean isProtectiveArmor(ItemStack stack) {
        if (stack.isEmpty()) return false;
        if (!(stack.getItem() instanceof ArmorItem armor)) return false;
        Holder<ArmorMaterial> mat = armor.getMaterial();
        return mat.is(ArmorMaterials.IRON)
                || mat.is(ArmorMaterials.GOLD)
                || mat.is(ArmorMaterials.DIAMOND)
                || mat.is(ArmorMaterials.NETHERITE);
    }

    private static void damageArmorPiece(ServerPlayer player, EquipmentSlot slot, int amount) {
        ItemStack stack = player.getItemBySlot(slot);
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ArmorItem)) return;
        if (!stack.isDamageableItem()) return;
        stack.hurtAndBreak(amount, player, slot);
    }
}