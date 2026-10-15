package com.tairitsutek.newgc.Planets.mars.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsVolcanoHandler {

    private static final RandomSource RNG = RandomSource.create();

    // ===== 喷发间隔 =====
    private static final int INTERVAL_MIN = 400;
    private static final int INTERVAL_MAX = 1800;

    // ===== 喷发持续时间 =====
    private static final int DURATION_MIN = 100;
    private static final int DURATION_MAX = 280;

    private static final int SEARCH_RADIUS = 48;

    // ===== 爆发阶段参数（分阶段放不同粒子）=====
    // 爆发期（前 15%）每刻粒子数
    private static final int BURST_PARTICLES = 60;
    // 稳定期每刻粒子数
    private static final int STEADY_PARTICLES = 35;

    // 状态
    private static int cooldownTimer = 0;
    private static int eruptionTicks = 0;
    private static int eruptionTotalTicks = 0;
    private static BlockPos volcanoPos = null;

    // ===== Getter =====
    public static int getEruptionTicks()      { return eruptionTicks; }
    public static int getEruptionTotalTicks() { return eruptionTotalTicks; }
    public static int getCooldownTimer()      { return cooldownTimer; }
    public static BlockPos getVolcanoPos()    { return volcanoPos; }

    public static float getEruptionIntensity() {
        if (eruptionTicks <= 0 || eruptionTotalTicks <= 0 || volcanoPos == null) return 0f;
        float progress = 1f - (float) eruptionTicks / eruptionTotalTicks;
        float envelope = 4f * progress * (1f - progress);
        return 0.25f + 0.75f * envelope;
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        if (eruptionTicks > 0 && volcanoPos != null) {
            spawnEruptionParticles(level, volcanoPos);
            eruptionTicks--;
            if (eruptionTicks <= 0) {
                volcanoPos = null;
                cooldownTimer = INTERVAL_MIN + RNG.nextInt(INTERVAL_MAX - INTERVAL_MIN + 1);
            }
            return;
        }

        if (cooldownTimer > 0) {
            cooldownTimer--;
            return;
        }

        startEruption(level, mc.player.position());
    }

    private static void startEruption(ClientLevel level, Vec3 playerPos) {
        double angle = RNG.nextDouble() * Math.PI * 2;
        double dist = 16 + RNG.nextDouble() * SEARCH_RADIUS;

        int x = (int) (playerPos.x + Math.cos(angle) * dist);
        int z = (int) (playerPos.z + Math.sin(angle) * dist);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

        volcanoPos = new BlockPos(x, y, z);
        eruptionTicks = DURATION_MIN + RNG.nextInt(DURATION_MAX - DURATION_MIN + 1);
        eruptionTotalTicks = eruptionTicks;
    }

    private static void spawnEruptionParticles(ClientLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        float intensity = getEruptionIntensity();
        float progress = 1f - (float) eruptionTicks / eruptionTotalTicks;
        boolean burstPhase = progress < 0.15f;   // 前 15% 是爆发期

        // ===== 1) 主喷发柱（柱状 + 顶部蘑菇云）=====
        int columnCount = burstPhase
                ? BURST_PARTICLES
                : (int) (STEADY_PARTICLES * (0.5f + intensity));

        for (int i = 0; i < columnCount; i++) {
            // 水平扩散：越往上扩散越大（蘑菇云）
            double heightFactor = RNG.nextDouble();   // 0=底部 1=顶部
            double spread = 0.4 + heightFactor * 2.5; // 底部窄，顶部宽

            double vx = (RNG.nextDouble() - 0.5) * spread;
            double vy = 1.5 + RNG.nextDouble() * 2.5 + intensity * 1.5;   // 初速大幅提高
            double vz = (RNG.nextDouble() - 0.5) * spread;

            double px = cx + (RNG.nextDouble() - 0.5) * 1.0;
            double py = cy + RNG.nextDouble() * 2.5;
            double pz = cz + (RNG.nextDouble() - 0.5) * 1.0;

            // 底部：FLAME + LAVA；中部：LARGE_SMOKE；顶部：ASH + CAMPFIRE_SMOKE
            if (heightFactor < 0.3) {
                float r = RNG.nextFloat();
                if (r < 0.5f) {
                    level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
                } else if (r < 0.8f) {
                    level.addParticle(ParticleTypes.LAVA, px, py, pz, vx * 0.6, vy * 0.7, vz * 0.6);
                } else {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx, vy * 0.8, vz);
                }
            } else if (heightFactor < 0.7) {
                float r = RNG.nextFloat();
                if (r < 0.6f) {
                    level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx * 1.2, vy * 0.9, vz * 1.2);
                } else if (r < 0.85f) {
                    level.addParticle(ParticleTypes.ASH, px, py, pz, vx, vy * 0.6, vz);
                } else {
                    level.addParticle(ParticleTypes.FLAME, px, py, pz, vx * 0.5, vy * 0.5, vz * 0.5);
                }
            } else {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, px, py, pz,
                        vx * 1.5, vy * 0.4, vz * 1.5);
            }
        }

        // ===== 2) 熔岩喷泉（爆发期大量，稳定期少量）=====
        int lavaCount = burstPhase ? 25 : (int) (8 * intensity);
        for (int i = 0; i < lavaCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double speed = 0.6 + RNG.nextDouble() * 1.8 * intensity;
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            double vy = 2.0 + RNG.nextDouble() * 2.0;

            // LAVA 粒子会掉下来，形成弧线
            level.addParticle(ParticleTypes.LAVA,
                    cx, cy + 1.0, cz,
                    vx, vy, vz);
        }

        // ===== 3) 地面冲击波（火山灰环）=====
        int ringCount = (int) (20 * intensity) + (burstPhase ? 20 : 0);
        for (int i = 0; i < ringCount; i++) {
            double angle = RNG.nextDouble() * Math.PI * 2;
            double r = 1.0 + RNG.nextDouble() * 4.0;
            double px = cx + Math.cos(angle) * r;
            double pz = cz + Math.sin(angle) * r;
            double py = cy + 0.1;

            // 向外 + 向上
            double vx = Math.cos(angle) * (0.5 + intensity);
            double vz = Math.sin(angle) * (0.5 + intensity);
            double vy = 0.3 + RNG.nextDouble() * 0.6;

            float r2 = RNG.nextFloat();
            if (r2 < 0.5f) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.ORANGE_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else if (r2 < 0.8f) {
                level.addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST,
                                Blocks.BLACK_CONCRETE.defaultBlockState()),
                        px, py, pz, vx, vy, vz);
            } else {
                level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz,
                        vx * 1.5, vy, vz * 1.5);
            }
        }

        // ===== 4) 火球 / 核心闪光（爆发期重点）=====
        int coreCount = burstPhase ? 15 : (int) (5 * intensity);
        for (int i = 0; i < coreCount; i++) {
            // FLAME 在中心高频闪烁
            level.addParticle(ParticleTypes.FLAME,
                    cx + (RNG.nextDouble() - 0.5) * 1.5,
                    cy + RNG.nextDouble() * 2.0,
                    cz + (RNG.nextDouble() - 0.5) * 1.5,
                    (RNG.nextDouble() - 0.5) * 0.4,
                    0.3 + RNG.nextDouble() * 0.6,
                    (RNG.nextDouble() - 0.5) * 0.4);
        }
        // END_ROD 做光晕点缀
        if (burstPhase || RNG.nextFloat() < 0.4f) {
            level.addParticle(ParticleTypes.END_ROD,
                    cx, cy + 1.0 + RNG.nextDouble() * 1.5, cz,
                    0, 0.1, 0);
        }
        // SOUL_FIRE_FLAME 做偏蓝白的高温点缀
        if (burstPhase || RNG.nextFloat() < 0.25f) {
            level.addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                    cx + (RNG.nextDouble() - 0.5) * 0.8,
                    cy + 0.5 + RNG.nextDouble() * 1.2,
                    cz + (RNG.nextDouble() - 0.5) * 0.8,
                    0, 0.2, 0);
        }

        // ===== 5) 烟柱远飘（模拟高空蘑菇云）=====
        int plumeCount = burstPhase ? 12 : (int) (4 * intensity);
        for (int i = 0; i < plumeCount; i++) {
            double topY = cy + 6.0 + RNG.nextDouble() * 6.0;
            double spread = 2.0 + RNG.nextDouble() * 3.0;
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx + (RNG.nextDouble() - 0.5) * spread,
                    topY,
                    cz + (RNG.nextDouble() - 0.5) * spread,
                    (RNG.nextDouble() - 0.5) * 0.3,
                    0.05,
                    (RNG.nextDouble() - 0.5) * 0.3);
        }
    }
}