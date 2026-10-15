package com.tairitsutek.newgc.Planets.mars.client;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID, value = Dist.CLIENT)
public class MarsVolcanoHandler {

    private static final RandomSource RNG = RandomSource.create();

    // ===== 喷发间隔随机范围（刻）=====
    private static final int INTERVAL_MIN = 400;    // 20 秒
    private static final int INTERVAL_MAX = 1800;   // 90 秒

    // ===== 喷发持续时间随机范围（刻）=====
    private static final int DURATION_MIN = 60;     // 3 秒
    private static final int DURATION_MAX = 200;    // 10 秒

    // 搜索半径
    private static final int SEARCH_RADIUS = 48;

    // 每刻粒子数
    private static final int PARTICLES_PER_TICK = 8;

    // 状态
    private static int cooldownTimer = 0;
    private static int eruptionTicks = 0;
    private static int eruptionTotalTicks = 0;   // 本次喷发总时长（用于进度显示）
    private static BlockPos volcanoPos = null;

    // ===== HUD 读取用的 getter =====
    public static int getEruptionTicks()      { return eruptionTicks; }
    public static int getEruptionTotalTicks() { return eruptionTotalTicks; }
    public static int getCooldownTimer()      { return cooldownTimer; }
    public static BlockPos getVolcanoPos()    { return volcanoPos; }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        // 1. 正在喷发
        if (eruptionTicks > 0 && volcanoPos != null) {
            spawnEruptionParticles(level, volcanoPos);
            eruptionTicks--;
            if (eruptionTicks <= 0) {
                volcanoPos = null;
                // 随机冷却时间
                cooldownTimer = INTERVAL_MIN + RNG.nextInt(INTERVAL_MAX - INTERVAL_MIN + 1);
            }
            return;
        }

        // 2. 冷却中
        if (cooldownTimer > 0) {
            cooldownTimer--;
            return;
        }

        // 3. 开始新喷发
        startEruption(level, mc.player.position());
    }

    private static void startEruption(ClientLevel level, Vec3 playerPos) {
        double angle = RNG.nextDouble() * Math.PI * 2;
        double dist = 16 + RNG.nextDouble() * SEARCH_RADIUS;

        int x = (int) (playerPos.x + Math.cos(angle) * dist);
        int z = (int) (playerPos.z + Math.sin(angle) * dist);
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

        volcanoPos = new BlockPos(x, y, z);
        // 随机持续时间
        eruptionTicks = DURATION_MIN + RNG.nextInt(DURATION_MAX - DURATION_MIN + 1);
        eruptionTotalTicks = eruptionTicks;   // 记录总时长，供 HUD 显示进度
    }

    private static void spawnEruptionParticles(ClientLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            double vx = (RNG.nextDouble() - 0.5) * 0.3;
            double vy = 0.5 + RNG.nextDouble() * 0.8;
            double vz = (RNG.nextDouble() - 0.5) * 0.3;

            double px = cx + (RNG.nextDouble() - 0.5) * 1.5;
            double py = cy + RNG.nextDouble() * 0.5;
            double pz = cz + (RNG.nextDouble() - 0.5) * 1.5;

            float roll = RNG.nextFloat();
            if (roll < 0.5f) {
                level.addParticle(ParticleTypes.FLAME, px, py, pz, vx, vy, vz);
            } else if (roll < 0.75f) {
                level.addParticle(ParticleTypes.LAVA, px, py, pz, vx * 0.5, vy * 0.3, vz * 0.5);
            } else if (roll < 0.9f) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, px, py, pz, vx * 0.8, vy * 0.6, vz * 0.8);
            } else {
                level.addParticle(ParticleTypes.ASH, px, py, pz, vx * 0.5, vy * 0.4, vz * 0.5);
            }
        }

        if (RNG.nextFloat() < 0.3f) {
            level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, cx, cy + 1.0, cz, 0, 0.05, 0);
        }
    }
}