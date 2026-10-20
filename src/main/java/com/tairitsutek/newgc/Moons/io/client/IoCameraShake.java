package com.tairitsutek.newgc.Moons.io.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class IoCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    // 响应速度（因为有包络线，可以稍微快一点）
    private static final float SMOOTH = 0.1f;

    // 震动幅度：地震的幅度通常比风暴大
    private static final float SHAKE_AMPLITUDE = 3.5f;

    // 震动频率：地震的抖动频率更高、更急促
    private static final float SHAKE_FREQUENCY = 0.35f;

    // 滚转（Roll）幅度，模拟地面倾斜感
    private static final float ROLL_AMPLITUDE = 1.5f;

    private static float smoothedStrength = 0.0f;
    private static float shakeTime = 0f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        // TODO: 这里请确保替换为你项目里木卫一的真实维度 Key
        if (!mc.level.dimension().equals(ModDimensions.IO_LEVEL_KEY)) return;

        // 【关键修改】直接调用你写好的 Cache 类获取强度
        float strength = IoQuakeClientCache.getQuakeIntensity();

        // 容错处理
        if (Float.isNaN(strength) || Float.isInfinite(strength)) strength = 0.0f;
        strength = Math.max(0.0f, Math.min(2.5f, strength));

        smoothedStrength += (strength - smoothedStrength) * SMOOTH;
        if (Float.isNaN(smoothedStrength)) smoothedStrength = 0.0f;

        // 如果强度极低（没地震或已经结束），直接跳过
        if (smoothedStrength < 0.05f) return;

        // 每次触发累加时间
        shakeTime += 1.0f;

        // 木卫一的地震：主要是上下颠簸（Pitch）和倾斜（Roll）
        // 使用不同的频率倍数，让运动轨迹看起来不规则

        // 倾斜（Roll） - 模拟地震时地面歪斜
        float waveRoll = (float) Math.sin(shakeTime * SHAKE_FREQUENCY * 0.8f) * smoothedStrength * ROLL_AMPLITUDE;

        // 俯仰（Pitch） - 上下颠簸
        float wavePitch = (float) Math.cos(shakeTime * SHAKE_FREQUENCY * 1.7f) * smoothedStrength * SHAKE_AMPLITUDE;

        // 高频随机抖动（模拟岩石碎裂的震感）
        float jitterPitch = (RNG.nextFloat() - 0.5f) * smoothedStrength * 1.5f;
        float jitterRoll = (RNG.nextFloat() - 0.5f) * smoothedStrength * 1.5f;

        // 应用修改
        event.setPitch(event.getPitch() + wavePitch + jitterPitch);
        event.setRoll(event.getRoll() + waveRoll + jitterRoll);
    }
}