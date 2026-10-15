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

    // 火山数量（同时存在的）
    private static final int MAX_VOLCANOES = 3;

    // 喷发间隔（刻）
    private static final int ERUPTION_INTERVAL = 600;   // 30 秒

    // 喷发持续时间（刻）
    private static final int ERUPTION_DURATION = 100;   // 5 秒

    // 火山搜索半径
    private static final int SEARCH_RADIUS = 48;

    // 每刻生成的粒子数
    private static final int PARTICLES_PER_TICK = 8;

    // 喷发高度
    private static final double ERUPTION_HEIGHT = 6.0;

    // 当前喷发状态
    private static int eruptionTimer = 0;
    private static int currentEruptionTicks = 0;
    private static BlockPos currentVolcanoPos = null;

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null || mc.player == null) return;
        if (!level.dimension().equals(ModDimensions.MARS_LEVEL_KEY)) return;

        // 每刻递减计时器
        if (eruptionTimer > 0) {
            eruptionTimer--;
            return;
        }

        // 如果正在喷发，继续喷
        if (currentEruptionTicks > 0 && currentVolcanoPos != null) {
            spawnEruptionParticles(level, currentVolcanoPos);
            currentEruptionTicks--;
            if (currentEruptionTicks <= 0) {
                currentVolcanoPos = null;
            }
            return;
        }

        // 开始新的喷发
        eruptionTimer = ERUPTION_INTERVAL;
        startEruption(level, mc.player.position());
    }

    private static void startEruption(ClientLevel level, Vec3 playerPos) {
        // 在玩家周围随机找一个火山位置
        double angle = RNG.nextDouble() * Math.PI * 2;
        double dist = 16 + RNG.nextDouble() * SEARCH_RADIUS;

        int x = (int) (playerPos.x + Math.cos(angle) * dist);
        int z = (int) (playerPos.z + Math.sin(angle) * dist);

        // 获取该位置的地表高度
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);

        currentVolcanoPos = new BlockPos(x, y, z);
        currentEruptionTicks = ERUPTION_DURATION;
    }

    private static void spawnEruptionParticles(ClientLevel level, BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 0.5;
        double cz = pos.getZ() + 0.5;

        for (int i = 0; i < PARTICLES_PER_TICK; i++) {
            // 喷发方向：向上 + 随机水平偏移
            double vx = (RNG.nextDouble() - 0.5) * 0.3;
            double vy = 0.5 + RNG.nextDouble() * 0.8;
            double vz = (RNG.nextDouble() - 0.5) * 0.3;

            // 初始位置在火山口附近
            double px = cx + (RNG.nextDouble() - 0.5) * 1.5;
            double py = cy + RNG.nextDouble() * 0.5;
            double pz = cz + (RNG.nextDouble() - 0.5) * 1.5;

            // 随机选择粒子类型
            float roll = RNG.nextFloat();

            if (roll < 0.5f) {
                // 熔岩粒子（橙色）
                level.addParticle(
                        ParticleTypes.FLAME,
                        px, py, pz,
                        vx, vy, vz);
            } else if (roll < 0.75f) {
                // 岩浆粒子
                level.addParticle(
                        ParticleTypes.LAVA,
                        px, py, pz,
                        vx * 0.5, vy * 0.3, vz * 0.5);
            } else if (roll < 0.9f) {
                // 浓烟
                level.addParticle(
                        ParticleTypes.LARGE_SMOKE,
                        px, py, pz,
                        vx * 0.8, vy * 0.6, vz * 0.8);
            } else {
                // 灰烬
                level.addParticle(
                        ParticleTypes.ASH,
                        px, py, pz,
                        vx * 0.5, vy * 0.4, vz * 0.5);
            }
        }

        // 火山口持续冒烟
        if (RNG.nextFloat() < 0.3f) {
            level.addParticle(
                    ParticleTypes.CAMPFIRE_COSY_SMOKE,
                    cx, cy + 1.0, cz,
                    0, 0.05, 0);
        }
    }
}