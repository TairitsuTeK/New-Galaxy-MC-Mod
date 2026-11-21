package com.tairitsutek.newgc.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import com.tairitsutek.newgc.gui.StarMapScreen;
import com.tairitsutek.newgc.network.RocketLaunchPacket;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * 火箭客户端交互：
 * <ul>
 *     <li>进入火箭后自动切到第三人称；</li>
 *     <li>按空格（跳跃键）发射。</li>
 * </ul>
 * 创造模式飞行在服务端由 {@link RocketEntity} 禁用，这里只负责视角与输入。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class RocketClientHandler {

    /** 上一 tick 跳跃键是否按下，用于检测“按下瞬间”。 */
    private static boolean jumpWasDown;
    private static boolean cameraOverridden;
    private static CameraType cameraBeforeRide = CameraType.FIRST_PERSON;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            jumpWasDown = false;
            cameraOverridden = false;
            return;
        }

        RocketEntity rocket = mc.player.getVehicle() instanceof RocketEntity riding ? riding : null;
        boolean jumpDown = mc.options.keyJump.isDown();

        if (rocket != null) {
            // 上升阶段分 tick 预加载星图图标/文字，避免到轨道瞬间集中加载造成卡顿。
            StarMapPrewarm.tick(rocket);
        }

        if (rocket == null) {
            if (cameraOverridden) {
                mc.options.setCameraType(cameraBeforeRide);
                cameraOverridden = false;
            }
            jumpWasDown = jumpDown;
            return;
        }

        // 服务端倒计时结束或玩家主动漂流后，关闭仍打开的星图，让玩家看到漂流 HUD。
        if (rocket.getState() == RocketEntity.STATE_DRIFT
                && mc.screen instanceof StarMapScreen) {
            mc.setScreen(null);
        }

        // 进入火箭：记录原视角并强制第三人称
        if (!cameraOverridden) {
            cameraBeforeRide = mc.options.getCameraType();
            cameraOverridden = true;
        }
        if (mc.options.getCameraType() != CameraType.THIRD_PERSON_BACK) {
            mc.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }

        // 空格：只在按下的那一 tick 发送一次点火请求
        if (jumpDown && !jumpWasDown && mc.screen == null) {
            PacketDistributor.sendToServer(new RocketLaunchPacket());
        }
        jumpWasDown = jumpDown;
    }

    /**
     * 火箭进入轨道后由服务端通知：自动打开星图，进入“轨道跃迁模式”。
     */
    public static void openStarMapInOrbit() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return;
        }
        // 实体状态同步包可能比自定义包稍晚到达，这里只校验“是否坐在火箭里”；
        // 真正的跃迁条件由服务端复核。
        if (!(mc.player.getVehicle() instanceof RocketEntity)) {
            return;
        }
        if (mc.screen == null) {
            mc.setScreen(new StarMapScreen(true));
        }
    }
}
