package com.tairitsutek.newgc.event;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class RespawnDimensionHandler {

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        ResourceKey<Level> deathDimension = oldPlayer.level().dimension();
        ServerLevel targetLevel = newPlayer.server.getLevel(deathDimension);
        if (targetLevel == null) return;

        // 用共享出生点的 X/Z，但 Y 用地表高度
        BlockPos sharedSpawn = targetLevel.getSharedSpawnPos();
        int x = sharedSpawn.getX();
        int z = sharedSpawn.getZ();

        // 获取该 X/Z 位置的地表高度
        int surfaceY = targetLevel.getHeight(
                Heightmap.Types.MOTION_BLOCKING,
                x, z
        );

        // 从地表往上找安全位置
        BlockPos safePos = findSafeSpot(targetLevel, x, surfaceY, z);

        newPlayer.teleportTo(targetLevel,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                newPlayer.getYRot(),
                newPlayer.getXRot());
    }

    /**
     * 从给定 Y 开始往上找，直到找到两个连续空气方块。
     */
    private static BlockPos findSafeSpot(ServerLevel level, int x, int startY, int z) {
        for (int y = startY; y < startY + 20; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            BlockPos above = pos.above();

            if (level.isEmptyBlock(pos) && level.isEmptyBlock(above)) {
                return pos;
            }
        }
        // 找不到安全点，就用地表 + 1
        return new BlockPos(x, startY + 1, z);
    }
}