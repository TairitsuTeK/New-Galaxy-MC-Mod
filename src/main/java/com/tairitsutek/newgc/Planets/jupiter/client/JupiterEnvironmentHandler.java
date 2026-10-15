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

    // ===== 基础伤害（风暴强度 1.0 时）=====
    private static final int BASE_DAMAGE_INTERVAL = 20;      // 1 秒一次
    private static final float BASE_DAMAGE_AMOUNT = 3.0f;    // 每次 3 点

    // ===== 基础盔甲损耗（风暴强度 1.0 时）=====
    private static final int BASE_ARMOR_INTERVAL = 100;      // 5 秒一次
    private static final int BASE_ARMOR_AMOUNT = 6;          // 每次 6 点

    // ===== 提示间隔 =====
    private static final int MESSAGE_INTERVAL = 20;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        // 读取当前风暴强度
        float strength = JupiterStormManager.getStormStrength();
        // 钳制到 0.5 ~ 2.5，避免异常
        strength = Math.max(0.5f, Math.min(2.5f, strength));

        // 根据强度计算当前间隔和数值
        // 强度越高：间隔越短、单次伤害越高
        int damageInterval = Math.max(5, (int) (BASE_DAMAGE_INTERVAL / strength));
        float damageAmount = BASE_DAMAGE_AMOUNT * strength;

        int armorInterval = Math.max(20, (int) (BASE_ARMOR_INTERVAL / strength));
        int armorAmount = Math.max(1, (int) (BASE_ARMOR_AMOUNT * strength));

        boolean protectedByArmor = hasFullProtectiveArmor(player);

        if (!protectedByArmor) {
            // 没穿盔甲：扣血 + 提示
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
            // 穿盔甲：损耗头盔和胸甲
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