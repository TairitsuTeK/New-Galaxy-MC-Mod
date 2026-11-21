package com.tairitsutek.newgc.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 发射前检查与“无目标漂流”二次确认。
 * <p>
 * 当前天体没有可抵达目标时，不立即点火，而是登记一个 60 秒有效的待确认请求。
 * 玩家必须在聊天框发送 {@code confirm}，服务端校验火箭仍可发射且玩家没有走远后，
 * 才会真正点火。有目标时完全不弹提示，直接走原发射流程。
 */
public final class RocketLaunchService {

    private static final long CONFIRM_TIMEOUT_MS = 60_000L;
    private static final double CONFIRM_DISTANCE_SQR = 16.0D * 16.0D;

    public enum LaunchResult {
        LAUNCHED,
        CONFIRM_REQUIRED,
        FAILED
    }

    private record PendingLaunch(UUID rocketId, ResourceKey<Level> dimension, long expiresAt) {
    }

    private static final Map<UUID, PendingLaunch> PENDING = new ConcurrentHashMap<>();

    private RocketLaunchService() {
    }

    /**
     * 玩家发起发射：有目标直接点火；无目标则暂存并提示输入 confirm。
     */
    public static LaunchResult request(ServerPlayer player, RocketEntity rocket) {
        if (!rocket.isReadyToLaunch()) {
            PENDING.remove(player.getUUID());
            return LaunchResult.FAILED;
        }

        // 有目标：保持原版流程，不显示任何额外提示。
        if (RocketDestinations.hasAnyTarget(
                rocket.level().dimension(), rocket.getTier())) {
            PENDING.remove(player.getUUID());
            return rocket.ignite() ? LaunchResult.LAUNCHED : LaunchResult.FAILED;
        }

        long now = System.currentTimeMillis();
        PendingLaunch existing = PENDING.get(player.getUUID());
        boolean samePending = existing != null
                && existing.rocketId().equals(rocket.getUUID())
                && now <= existing.expiresAt();

        PENDING.put(player.getUUID(), new PendingLaunch(
                rocket.getUUID(),
                rocket.level().dimension(),
                now + CONFIRM_TIMEOUT_MS
        ));

        if (samePending) {
            // 连续按发射键只刷新确认窗口，不重复刷屏聊天栏。
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.drift_confirm_waiting"), true);
            return LaunchResult.CONFIRM_REQUIRED;
        }

        player.displayClientMessage(
                Component.translatable("message.newgc.rocket.drift_warning"), false);
        player.displayClientMessage(
                Component.translatable("message.newgc.rocket.drift_confirm_hint"), false);
        return LaunchResult.CONFIRM_REQUIRED;
    }

    /**
     * 聊天框二次确认。
     *
     * @return true 表示这是一条待确认发射消息，应取消原聊天广播
     */
    public static boolean tryConfirm(ServerPlayer player, String rawText) {
        if (!"confirm".equalsIgnoreCase(rawText == null ? "" : rawText.trim())) {
            return false;
        }

        PendingLaunch pending = PENDING.get(player.getUUID());
        if (pending == null) {
            return false;
        }

        if (System.currentTimeMillis() > pending.expiresAt()) {
            PENDING.remove(player.getUUID());
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.confirm_expired"), true);
            return true;
        }

        ServerLevel level = player.server.getLevel(pending.dimension());
        Entity entity = level == null ? null : level.getEntity(pending.rocketId());
        if (!(entity instanceof RocketEntity rocket) || !rocket.isReadyToLaunch()) {
            PENDING.remove(player.getUUID());
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.confirm_invalid"), true);
            return true;
        }

        if (player.distanceToSqr(rocket) > CONFIRM_DISTANCE_SQR) {
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.confirm_too_far"), true);
            return true;
        }

        if (!rocket.ignite()) {
            PENDING.remove(player.getUUID());
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.confirm_invalid"), true);
            return true;
        }

        PENDING.remove(player.getUUID());
        player.displayClientMessage(
                Component.translatable("message.newgc.rocket.confirm_success"), false);
        player.displayClientMessage(
                Component.translatable("message.newgc.rocket.countdown",
                        RocketEntity.COUNTDOWN_TICKS / 20), true);
        return true;
    }

    public static void clear(ServerPlayer player) {
        if (player != null) {
            PENDING.remove(player.getUUID());
        }
    }
}
