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
 * 骑乘正在倒计时的火箭时，对相机施加高频抖动，越接近点火抖动越强；
 * 火箭本体（{@code RocketModel}）不做任何摆动，因此看起来是"镜头在震"，
 * 而不是"火箭在晃"。10 秒倒计时中段开始加入 pitch / roll，形成更明显的
 * 发动机预燃震动。
 */
@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public final class RocketLaunchCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    /** 抖动的最高频率推进量。 */
    private static final float SHAKE_STEP_MIN = 2.2F;
    private static final float SHAKE_STEP_RANDOM = 1.2F;
    /** 正弦抖动最大幅度（度）。 */
    private static final float WAVE_AMPLITUDE = 0.95F;
    /** 随机抖动最大幅度（度）。 */
    private static final float JITTER_AMPLITUDE = 0.62F;
    /** 俯仰/滚转抖动幅度。 */
    private static final float PITCH_AMPLITUDE = 0.38F;
    private static final float ROLL_AMPLITUDE = 0.30F;

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
        float intensity = 0.55F + 1.25F * progress * progress;

        shakeTime += SHAKE_STEP_MIN + RNG.nextFloat() * SHAKE_STEP_RANDOM;

        float wave = Mth.sin(shakeTime) * WAVE_AMPLITUDE * intensity;
        float jitter = (RNG.nextFloat() - 0.5F) * JITTER_AMPLITUDE * intensity;
        float pitch = Mth.sin(shakeTime * 1.37F + 0.8F) * PITCH_AMPLITUDE * intensity;
        float roll = Mth.cos(shakeTime * 0.91F) * ROLL_AMPLITUDE * intensity;

        event.setYaw(event.getYaw() + wave + jitter);
        event.setPitch(event.getPitch() + pitch);
        event.setRoll(event.getRoll() + roll);
    }
}
