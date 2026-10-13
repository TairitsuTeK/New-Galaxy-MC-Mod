package com.tairitsutek.newgc.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.event.oxygen.ResetTankPacket;
import com.tairitsutek.newgc.item.ToolsItem;
import com.tairitsutek.newgc.gui.StarMapScreen;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class KeyBindings {

    public static final KeyMapping OPEN_STAR_MAP = new KeyMapping(
            "key.newgc.star_map",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_M,
            "key.categories.newgc"
    );

    public static final KeyMapping RESET_TANK = new KeyMapping(
            "key.newgc.reset_tank",
            KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            "key.categories.newgc"
    );

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_STAR_MAP);
        event.register(RESET_TANK);
    }

    @SubscribeEvent
    public static void onKeyInput(InputEvent.Key event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        if (OPEN_STAR_MAP.consumeClick()) {
            ItemStack mainHand = mc.player.getMainHandItem();
            ItemStack offHand = mc.player.getOffhandItem();

            boolean holdingTeleporter =
                    mainHand.is(ToolsItem.TELEPORTER.get()) ||
                            offHand.is(ToolsItem.TELEPORTER.get());

            if (!holdingTeleporter) {
                mc.player.displayClientMessage(
                        Component.translatable("message.newgc.need_teleporter"),
                        true
                );
                return;
            }

            mc.setScreen(new StarMapScreen());
        }

        if (RESET_TANK.consumeClick()) {
            PacketDistributor.sendToServer(new ResetTankPacket());
        }
    }
}