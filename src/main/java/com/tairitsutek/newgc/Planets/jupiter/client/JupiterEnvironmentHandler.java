package com.tairitsutek.newgc.Planets.jupiter.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.Planets.jupiter.server.JupiterStormManager;
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

    // ===== 无护甲时：伤害间隔范围 =====
    // 平静期：120 刻一次（6 秒，伤害概率低）
    // 暴烈期：8 刻一次（0.4 秒，非常密）
    private static final int DAMAGE_INTERVAL_CALM = 120;
    private static final int DAMAGE_INTERVAL_STORM = 8;

    // ===== 无护甲时：伤害值范围 =====
    // 平静期：0.3 点
    // 暴烈期：8 点（原 7.5，略提）
    private static final float DAMAGE_AMOUNT_CALM = 0.3f;
    private static final float DAMAGE_AMOUNT_STORM = 8.0f;

    // ===== 有护甲时：护甲耐久损耗 =====
    private static final int ARMOR_INTERVAL_BASE = 100;
    private static final int ARMOR_AMOUNT_BASE = 6;

    private static final int MESSAGE_INTERVAL = 20;
    private static final float SMOOTH_FACTOR = 0.01f;

    private static float smoothedStrength = 1.0f;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        // 目标强度（不再强制下限 0.5）
        float targetStrength = JupiterStormManager.getStormStrength();
        targetStrength = Math.max(0f, Math.min(2.5f, targetStrength));

        // 平滑跟随
        smoothedStrength += (targetStrength - smoothedStrength) * SMOOTH_FACTOR;
        if (Float.isNaN(smoothedStrength)) smoothedStrength = 0f;
        smoothedStrength = Math.max(0f, Math.min(2.5f, smoothedStrength));

        // ===== 归一化：0 = 平静，1 = 暴烈顶点 =====
        float t = smoothedStrength / 2.5f;
        t = Math.max(0f, Math.min(1f, t));

        // ===== 曲线：t^2.5 让平静期伤害几乎为零 =====
        float curve = (float) Math.pow(t, 2.5);

        // ===== 伤害间隔：曲线决定，平静期极长 =====
        int damageInterval = (int) (DAMAGE_INTERVAL_CALM
                - (DAMAGE_INTERVAL_CALM - DAMAGE_INTERVAL_STORM) * curve);
        damageInterval = Math.max(DAMAGE_INTERVAL_STORM, damageInterval);

        // ===== 伤害值：曲线决定 =====
        float damageAmount = DAMAGE_AMOUNT_CALM
                + (DAMAGE_AMOUNT_STORM - DAMAGE_AMOUNT_CALM) * curve;

        // ===== 护甲耐久损耗（随强度）=====
        int armorInterval = Math.max(20,
                (int) (ARMOR_INTERVAL_BASE / (0.5f + smoothedStrength)));
        int armorAmount = Math.max(1,
                (int) (ARMOR_AMOUNT_BASE * (0.5f + smoothedStrength)));

        boolean protectedByArmor = hasFullProtectiveArmor(player);

        if (!protectedByArmor) {
            if (player.tickCount % damageInterval == 0) {
                // 平静期伤害极小，暴烈期致命
                if (damageAmount >= 0.05f) {
                    player.hurt(player.damageSources().generic(), damageAmount);
                }
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