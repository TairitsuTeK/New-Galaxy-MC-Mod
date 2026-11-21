package com.tairitsutek.newgc.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.entity.RocketEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * 火箭倒计时相机抖动。
 * <p>
 * 骑乘正在倒计时的火箭时，只对相机的 **yaw（水平方向）** 施加高频小幅抖动，
 * 越接近点火抖动越强；火箭本体（{@code RocketModel}）不做任何摆动，
 * 因此看起来是"镜头在震"，而不是"火箭在晃"。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class RocketLaunchCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    /** 抖动的最高频率推进量。 */
    private static final float SHAKE_STEP_MIN = 1.7F;
    private static final float SHAKE_STEP_RANDOM = 0.9F;
    /** 正弦抖动最大幅度（度）。 */
    private static final float WAVE_AMPLITUDE = 0.45F;
    /** 随机抖动最大幅度（度）。 */
    private static final float JITTER_AMPLITUDE = 0.30F;

    private static float shakeTime;

    private RocketLaunchCameraShake() {
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            shakeTime = 0.0F;
            return;
        }
        if (!(mc.player.getVehicle() instanceof RocketEntity rocket)
                || rocket.getState() != RocketEntity.STATE_COUNTDOWN) {
            shakeTime = 0.0F;
            return;
        }

        // 倒计时进度：1.0 表示即将点火，抖动最强
        float progress = 1.0F - Mth.clamp(
                rocket.getTimer() / (float) RocketEntity.COUNTDOWN_TICKS, 0.0F, 1.0F);
        float intensity = 0.4F + 0.6F * progress;

        shakeTime += SHAKE_STEP_MIN + RNG.nextFloat() * SHAKE_STEP_RANDOM;

        float wave = Mth.sin(shakeTime) * WAVE_AMPLITUDE * intensity;
        float jitter = (RNG.nextFloat() - 0.5F) * JITTER_AMPLITUDE * intensity;

        // 只动水平方向，不修改 pitch / roll
        event.setYaw(event.getYaw() + wave + jitter);
    }
}
