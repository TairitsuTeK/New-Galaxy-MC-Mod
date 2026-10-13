package com.tairitsutek.newgc.item;

import com.tairitsutek.newgc.event.oxygen.ModAttachments;
import com.tairitsutek.newgc.event.oxygen.OxygenData;
import com.tairitsutek.newgc.event.oxygen.OxygenSyncPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class OxygenTankItem extends Item {

    public static final int OXYGEN_PER_USE = 500;

    public OxygenTankItem(Properties properties) {
        super(properties);
    }

    @Override
    public @NotNull InteractionResultHolder<ItemStack> use(Level level, Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        int remaining = stack.getMaxDamage() - stack.getDamageValue();

        // 耐久耗尽 → 不能用
        if (remaining <= 0) {
            player.displayClientMessage(
                    Component.translatable("message.newgc.oxygen_tank.empty")
                            .withStyle(ChatFormatting.RED),
                    true
            );
            return InteractionResultHolder.fail(stack);
        }

        OxygenData data = player.getData(ModAttachments.OXYGEN.get());

        int amount = Math.min(remaining, OXYGEN_PER_USE);

        // 玩家 TANK 提升
        data.setTankMax(data.getTankMax() + amount);
        data.setTankOxygen(data.getTankOxygen() + amount);

        // 消耗耐久（不 shrink）
        stack.setDamageValue(stack.getDamageValue() + amount);

        // 音效
        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.BOTTLE_FILL, SoundSource.PLAYERS,
                1.0f, 1.0f);

        player.displayClientMessage(
                Component.translatable("message.newgc.oxygen_tank.used", amount)
                        .withStyle(ChatFormatting.AQUA),
                true
        );

        if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp,
                    new OxygenSyncPacket(
                            data.getOxygen(),
                            data.getTankOxygen(),
                            data.getTankMax()));
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, List<Component> tooltip, @NotNull TooltipFlag flag) {
        int remaining = stack.getMaxDamage() - stack.getDamageValue();

        // 1. 剩余氧气
        tooltip.add(Component.translatable("tooltip.newgc.oxygen_tank.remaining", remaining, stack.getMaxDamage())
                .withStyle(ChatFormatting.AQUA));

        // 2. 使用方法
        tooltip.add(Component.translatable("tooltip.newgc.oxygen_tank.usage")
                .withStyle(ChatFormatting.GRAY));

        // 3. 每次充氧量
        tooltip.add(Component.translatable("tooltip.newgc.oxygen_tank.per_use", OXYGEN_PER_USE)
                .withStyle(ChatFormatting.DARK_GRAY));

        // 4. 补充说明
        tooltip.add(Component.translatable("tooltip.newgc.oxygen_tank.refill")
                .withStyle(ChatFormatting.DARK_AQUA));
    }
}