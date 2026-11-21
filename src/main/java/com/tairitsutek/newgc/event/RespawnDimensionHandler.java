package com.tairitsutek.newgc.event;

import com.tairitsutek.newgc.NewGalaxyMain;
import com.tairitsutek.newgc.Planets.ModDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@EventBusSubscriber(modid = NewGalaxyMain.MODID)
public class RespawnDimensionHandler {

    /**
     * The death level is recorded in PlayerEvent.Clone, then consumed by the
     * actual respawn event. Using the respawn event avoids the vanilla
     * respawn placement overwriting our position afterwards.
     */
    private static final Map<UUID, ResourceKey<Level>> PENDING_RESPAWNS =
            new ConcurrentHashMap<>();

    /** 太空死亡后的精确返航点（起飞星球与平台坐标）。 */
    private static final Map<UUID, SpaceReturnData.Point> PENDING_SPACE_RETURNS =
            new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        if (!(event.getOriginal() instanceof ServerPlayer oldPlayer)) return;
        if (!(event.getEntity() instanceof ServerPlayer newPlayer)) return;

        ResourceKey<Level> deathDimension = oldPlayer.level().dimension();
        if (deathDimension.equals(ModDimensions.SPACE_LEVEL_KEY)) {
            SpaceReturnData.Point returnPoint = SpaceReturnData.read(oldPlayer);
            if (returnPoint != null) {
                PENDING_SPACE_RETURNS.put(newPlayer.getUUID(), returnPoint);
            }
            return;
        }
        if (ModDimensions.ALL_LEVEL_KEYS.contains(deathDimension)) {
            PENDING_RESPAWNS.put(newPlayer.getUUID(), deathDimension);
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        UUID playerId = player.getUUID();

        // 太空死亡优先送回火箭起飞星球/平台，不受原版重生点影响。
        SpaceReturnData.Point spaceReturn = PENDING_SPACE_RETURNS.remove(playerId);
        if (spaceReturn != null) {
            ServerLevel returnLevel = player.server.getLevel(spaceReturn.dimension());
            if (returnLevel != null) {
                player.teleportTo(returnLevel,
                        spaceReturn.x(), spaceReturn.y(), spaceReturn.z(),
                        spaceReturn.yaw(), spaceReturn.pitch());
                player.setDeltaMovement(0.0, 0.0, 0.0);
                SpaceReturnData.clear(player);
                return;
            }
        }

        // A bed or an explicit /spawnpoint must keep working. Only the
        // no-respawn-point path needs the shared surface fallback.
        if (player.getRespawnPosition() != null) return;

        ResourceKey<Level> targetKey = PENDING_RESPAWNS.remove(playerId);

        // If event order ever changes, fall back to the dimension the player
        // actually respawned in.
        if (targetKey == null) {
            ResourceKey<Level> current = player.level().dimension();
            if (!ModDimensions.ALL_LEVEL_KEYS.contains(current)) return;
            targetKey = current;
        }

        ServerLevel targetLevel = player.server.getLevel(targetKey);
        if (targetLevel == null) return;

        BlockPos sharedSpawn = targetLevel.getSharedSpawnPos();
        int x = sharedSpawn.getX();
        int z = sharedSpawn.getZ();
        int surfaceY = getSurfaceHeight(targetLevel, x, z);
        BlockPos safePos = findSafeSpot(targetLevel, x, surfaceY, z);

        player.teleportTo(targetLevel,
                safePos.getX() + 0.5,
                safePos.getY(),
                safePos.getZ() + 0.5,
                player.getYRot(),
                player.getXRot());
        player.setDeltaMovement(0.0, 0.0, 0.0);
    }

    /**
     * Ensure the spawn chunk is generated before asking for its heightmap.
     * On an unloaded chunk getHeight returns the minimum build height, which
     * was the direct cause of respawning inside the planet.
     */
    private static int getSurfaceHeight(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
    }

    private static BlockPos findSafeSpot(ServerLevel level, int x, int startY, int z) {
        int minY = level.getMinBuildHeight() + 1;
        int maxY = level.getMaxBuildHeight() - 3;
        int start = Mth.clamp(startY, minY, maxY);

        for (int y = start; y < start + 32 && y <= maxY; y++) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())) {
                return pos;
            }
        }
        return new BlockPos(x, Math.min(start + 1, maxY), z);
    }

}