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

    // 每 20 刻（1 秒）扣一次
    private static final int DAMAGE_INTERVAL = 20;
    // 每次伤害 3 点
    private static final float DAMAGE_AMOUNT = 3.0f;

    // 提示间隔：每 20 刻（1 秒）
    private static final int MESSAGE_INTERVAL = 20;

    // 每 100 刻（5 秒）损耗一次
    private static final int ARMOR_DAMAGE_INTERVAL = 100;

    // 每次损耗 6 点
    private static final int ARMOR_DAMAGE_AMOUNT = 6;

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (!player.level().dimension().equals(ModDimensions.JUPITER_LEVEL_KEY)) return;
        if (player.isCreative() || player.isSpectator()) return;

        boolean protectedByArmor = hasFullProtectiveArmor(player);

        if (!protectedByArmor) {

            if (player.tickCount % DAMAGE_INTERVAL == 0) {
                player.hurt(player.damageSources().generic(), DAMAGE_AMOUNT);
            }
            if (player.tickCount % MESSAGE_INTERVAL == 0) {
                player.displayClientMessage(
                        Component.translatable("message.newgc.jupiter.no_armor"),
                        true
                );
            }
        } else {

            if (player.tickCount % ARMOR_DAMAGE_INTERVAL == 0) {
                damageArmorPiece(player, EquipmentSlot.HEAD, ARMOR_DAMAGE_AMOUNT);
                damageArmorPiece(player, EquipmentSlot.CHEST, ARMOR_DAMAGE_AMOUNT);
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