package com.tairitsutek.newgc.Moons.io.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class IoCameraShake {

    private static final RandomSource RNG = RandomSource.create();

    // ===== 地震参数 =====
    private static final float QUAKE_SMOOTH = 0.1f;
    private static final float QUAKE_SHAKE_AMPLITUDE = 3.5f;
    private static final float QUAKE_SHAKE_FREQUENCY = 0.35f;
    private static final float QUAKE_ROLL_AMPLITUDE = 1.5f;

    private static float quakeSmoothedStrength = 0.0f;

    // ===== 火山参数 =====
    private static final float VOLCANO_SMOOTH = 0.08f;
    private static final float VOLCANO_MAX_AMPLITUDE = 2.5f;
    private static final double VOLCANO_MAX_DISTANCE = 72.0;

    private static float volcanoSmoothedAmplitude = 0f;

    // 共享时间
    private static float shakeTime = 0f;

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        if (!mc.level.dimension().equals(ModDimensions.IO_LEVEL_KEY)) return;

        // 只要在震动，时间就累加
        shakeTime += 1.0f;

        // 基础偏移量（累加地震和火山）
        float finalPitchAdd = 0f;
        float finalRollAdd = 0f;

        // ==========================================
        // 1. 处理地震 (Earthquake)
        // ==========================================
        float quakeStrength = IoQuakeClientCache.getQuakeIntensity();
        if (Float.isNaN(quakeStrength) || Float.isInfinite(quakeStrength)) quakeStrength = 0.0f;
        quakeStrength = Math.max(0.0f, Math.min(2.5f, quakeStrength));

        quakeSmoothedStrength += (quakeStrength - quakeSmoothedStrength) * QUAKE_SMOOTH;
        if (Float.isNaN(quakeSmoothedStrength)) quakeSmoothedStrength = 0.0f;

        if (quakeSmoothedStrength >= 0.05f) {
            float waveRoll = (float) Math.sin(shakeTime * QUAKE_SHAKE_FREQUENCY * 0.8f) * quakeSmoothedStrength * QUAKE_ROLL_AMPLITUDE;
            float wavePitch = (float) Math.cos(shakeTime * QUAKE_SHAKE_FREQUENCY * 1.7f) * quakeSmoothedStrength * QUAKE_SHAKE_AMPLITUDE;
            float jitterPitch = (RNG.nextFloat() - 0.5f) * quakeSmoothedStrength * 1.5f;
            float jitterRoll = (RNG.nextFloat() - 0.5f) * quakeSmoothedStrength * 1.5f;

            finalPitchAdd += wavePitch + jitterPitch;
            finalRollAdd += waveRoll + jitterRoll;
        }

        // ==========================================
        // 2. 处理火山 (Volcano)
        // ==========================================
        float volcanoTargetAmplitude = 0f;
        BlockPos vp = IoVolcanoClientCache.getVolcanoPos();
        float volcanoIntensity = IoVolcanoClientCache.getEruptionIntensity();

        if (vp != null && volcanoIntensity > 0f) {
            Vec3 p = mc.player.position();
            double dx = p.x - (vp.getX() + 0.5);
            double dy = p.y - (vp.getY() + 0.5);
            double dz = p.z - (vp.getZ() + 0.5);
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // 距离衰减计算
            if (dist < VOLCANO_MAX_DISTANCE) {
                double t = dist / VOLCANO_MAX_DISTANCE;
                double distanceFactor = 1.0 - t * t;
                volcanoTargetAmplitude = (float) (VOLCANO_MAX_AMPLITUDE * volcanoIntensity * distanceFactor);
            }
        }

        volcanoSmoothedAmplitude += (volcanoTargetAmplitude - volcanoSmoothedAmplitude) * VOLCANO_SMOOTH;
        if (Float.isNaN(volcanoSmoothedAmplitude)) volcanoSmoothedAmplitude = 0f;

        if (volcanoSmoothedAmplitude >= 0.05f) {
            // 火山是持续、低沉的咆哮感
            float vJitterPitch = (RNG.nextFloat() - 0.5f) * volcanoSmoothedAmplitude;
            float vJitterRoll = (RNG.nextFloat() - 0.5f) * volcanoSmoothedAmplitude * 1.5f;

            float vWavePitch = (float) Math.cos(shakeTime * 0.45f) * volcanoSmoothedAmplitude * 0.5f;
            float vWaveRoll = (float) Math.sin(shakeTime * 0.35f) * volcanoSmoothedAmplitude * 0.4f;

            finalPitchAdd += vJitterPitch + vWavePitch;
            finalRollAdd += vJitterRoll + vWaveRoll;
        }

        // ==========================================
        // 3. 应用综合震动
        // ==========================================
        if (finalPitchAdd != 0f || finalRollAdd != 0f) {
            event.setPitch(event.getPitch() + finalPitchAdd);
            event.setRoll(event.getRoll() + finalRollAdd);
        }
    }
}