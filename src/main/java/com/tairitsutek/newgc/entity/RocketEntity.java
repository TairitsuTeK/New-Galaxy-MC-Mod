package com.tairitsutek.newgc.entity;

import com.tairitsutek.newgc.Planets.ModDimensions;
import com.tairitsutek.newgc.block.LaunchPadBlock;
import com.tairitsutek.newgc.block.MachinesBlock;
import com.tairitsutek.newgc.event.SpaceReturnData;
import com.tairitsutek.newgc.network.RocketOrbitPacket;
import com.tairitsutek.newgc.space.SpaceLocations;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 可发射的一阶/二阶/三阶火箭。
 * <p>
 * 状态机：停靠（IDLE）→ 点火倒计时（COUNTDOWN）→ 上升（ASCENT）→ 轨道（ORBIT）。
 * 到达轨道后由玩家在星图中选择目标星球，火箭连同玩家一起传送到目的地，不再返回发射平台。
 * <p>
 * 发射时 2x2 发射平台会被拆下并存入火箭储物舱（按 F 打开）。
 */
public class RocketEntity extends Entity {

    public static final int STATE_IDLE = 0;
    public static final int STATE_COUNTDOWN = 1;
    public static final int STATE_ASCENT = 2;
    public static final int STATE_DESCENT = 3;
    public static final int STATE_ORBIT = 4;
    public static final int STATE_DRIFT = 5;

    /** 点火到升空的倒计时（tick）：10 秒。 */
    public static final int COUNTDOWN_TICKS = 200;
    /** 一阶火箭上升到轨道的总时长（tick）：400 tick = 20 秒。 */
    public static final int ASCENT_TICKS_TIER1 = 400;
    /** 二阶火箭上升到轨道的总时长（tick）：340 tick = 17 秒。 */
    public static final int ASCENT_TICKS_TIER2 = 340;
    /** 三阶火箭上升到轨道的总时长（tick）：280 tick = 14 秒。 */
    public static final int ASCENT_TICKS_TIER3 = 280;
    /** 兼容旧调用；默认按一阶火箭计算。 */
    public static final int ASCENT_TICKS = ASCENT_TICKS_TIER1;
    /** 没有可用目标时，星图保留的倒计时；结束后自动进入漂流。 */
    public static final int NO_TARGET_GRACE_TICKS = 100;
    /** 相对发射高度需要爬升的距离（格）。 */
    private static final double ORBIT_CLIMB = 420.0D;
    /** 倒计时烟雾向外扩散时使用的喷口半径。 */
    private static final double COUNTDOWN_SMOKE_RADIUS = 0.42D;
    /** 漂流维度内的起点；X 由发射星系决定，沿 Y 轴向下漂移。 */
    private static final double SPACE_DRIFT_START_Y = 128.0D;
    private static final double SPACE_DRIFT_START_Z = 0.0D;
    private static final double SPACE_DRIFT_SPEED_Y = 0.6D;
    /** 原版虚空判定：Y < minBuildHeight - 64。 */
    public static final double VANILLA_VOID_OFFSET = 64.0D;
    /** 虚空伤害间隔（tick）。 */
    private static final int DRIFT_DAMAGE_INTERVAL = 20;
    /** 未成功创建太空维度时的旧版原维度下降兜底速度。 */
    private static final double LEGACY_DRIFT_FALL_SPEED = 0.6D;
    private static final double LEGACY_DRIFT_DAMAGE_ALTITUDE = 120.0D;
    /** 上升曲线指数：起步慢、后段明显加速。 */
    private static final double ASCENT_CURVE = 1.8D;
    /** 储物舱容量。 */
    public static final int CARGO_SIZE = 9;
    /** 落点需要预留的空方块高度（保证玩家头部不会卡进地形）。 */
    public static final int LANDING_CLEARANCE = 5;

    private static final EntityDataAccessor<Integer> DATA_STATE =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TIMER =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    /** 火箭等级：1 = 一阶，2 = 二阶，3 = 三阶。 */
    private static final EntityDataAccessor<Integer> DATA_TIER =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.INT);
    /** 起飞点维度；太空天空渲染据此选择最近行星图标。 */
    private static final EntityDataAccessor<String> DATA_ORIGIN_DIMENSION =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.STRING);
    /** 起飞点的 Y 坐标，客户端用它复算上升曲线。 */
    private static final EntityDataAccessor<Float> DATA_START_Y =
            SynchedEntityData.defineId(RocketEntity.class, EntityDataSerializers.FLOAT);

    /** 停靠的发射平台位置（2x2 最小角），仅用于停靠状态。 */
    @Nullable
    private BlockPos launchPadPos;
    /** 起飞星球/卫星及平台中心，用于太空漂流死亡后的返航。 */
    @Nullable
    private ResourceKey<Level> originDimension;
    private double originX;
    private double originY;
    private double originZ;
    private int flightTicks;
    /** 轨道通知剩余 tick：进入轨道或重新登录后的一小段时间内会重复通知客户端打开星图。 */
    private int announceTicks;

    /** 储物舱：发射时收纳拆下的发射平台。 */
    private final SimpleContainer cargo = new SimpleContainer(CARGO_SIZE);

    public RocketEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    //region 同步数据
    @Override
    protected void defineSynchedData(SynchedEntityData.@NotNull Builder builder) {
        builder.define(DATA_STATE, STATE_IDLE);
        builder.define(DATA_TIMER, 0);
        builder.define(DATA_TIER, RocketDestinations.TIER_1);
        builder.define(DATA_ORIGIN_DIMENSION, "");
        builder.define(DATA_START_Y, 0.0F);
    }

    public int getState() {
        return this.entityData.get(DATA_STATE);
    }

    private void setState(int state) {
        this.entityData.set(DATA_STATE, state);
    }

    public int getTimer() {
        return this.entityData.get(DATA_TIMER);
    }

    private void setTimer(int timer) {
        this.entityData.set(DATA_TIMER, timer);
    }

    /** 火箭等级：一阶、二阶或三阶。 */
    public int getTier() {
        return this.entityData.get(DATA_TIER);
    }

    public void setTier(int tier) {
        this.entityData.set(DATA_TIER,
                Math.max(RocketDestinations.TIER_1,
                        Math.min(RocketDestinations.TIER_3, tier)));
    }

    /** 当前等级对应的上升时长；HUD 与客户端插值共用。 */
    public int getAscentTicks() {
        if (this.getTier() >= RocketDestinations.TIER_3) {
            return ASCENT_TICKS_TIER3;
        }
        return this.getTier() >= RocketDestinations.TIER_2
                ? ASCENT_TICKS_TIER2
                : ASCENT_TICKS_TIER1;
    }

    /** 当前等级下，从所在天体可前往的目标名称。 */
    public String destinationSummary() {
        return RocketDestinations.targetSummary(this.level().dimension(), this.getTier());
    }

    public Component destinationSummaryComponent() {
        return RocketDestinations.targetSummaryComponent(
                this.level().dimension(), this.getTier());
    }

    /** 当前等级下是否有可前往的目标。 */
    public boolean hasDestination() {
        return RocketDestinations.hasAnyTarget(this.level().dimension(), this.getTier());
    }

    /** 没有目标时，轨道阶段剩余自动漂流倒计时；其他状态返回 0。 */
    public int getDriftCountdownTicks() {
        return this.getState() == STATE_ORBIT && !this.hasDestination()
                ? Math.max(0, this.getTimer()) : 0;
    }

    /** 起飞点维度；客户端太空天空用它选择最近行星图标。 */
    @Nullable
    public ResourceKey<Level> getOriginDimension() {
        String value = this.entityData.get(DATA_ORIGIN_DIMENSION);
        if (value.isEmpty()) {
            return null;
        }
        ResourceLocation id = ResourceLocation.tryParse(value);
        return id == null ? null : ResourceKey.create(Registries.DIMENSION, id);
    }

    public void setOrigin(@Nullable ResourceKey<Level> dimension,
                          double x, double y, double z) {
        this.originDimension = dimension;
        this.originX = x;
        this.originY = y;
        this.originZ = z;
        this.entityData.set(DATA_ORIGIN_DIMENSION,
                dimension == null ? "" : dimension.location().toString());
    }

    public double getOriginX() {
        return this.originX;
    }

    public double getOriginY() {
        return this.originY;
    }

    public double getOriginZ() {
        return this.originZ;
    }

    /** 把旧火箭的储物舱复制到太空中的新火箭。 */
    public void copyCargoFrom(RocketEntity source) {
        for (int i = 0; i < CARGO_SIZE; i++) {
            this.cargo.setItem(i, source.cargo.getItem(i).copy());
        }
    }

    /** 抵达太空维度后启动漂流，不再依赖原维度的轨道状态。 */
    public void startSpaceDrift() {
        this.setState(STATE_DRIFT);
        this.flightTicks = 0;
        this.setTimer(0);
        this.announceTicks = 0;
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
    }

    /** 起飞点高度。 */
    public double getStartY() {
        return this.entityData.get(DATA_START_Y).doubleValue();
    }

    private void setStartY(double y) {
        this.entityData.set(DATA_START_Y, (float) y);
    }

    /**
     * 轨道高度（客户端与服务端使用同一公式，保证两侧位置一致）。
     * <p>
     * 不再被原版建筑高度上限截断：火箭是实体，可以飞到建筑高度之上。
     * 这样低海拔发射时也能获得完整的爬升距离，而不是很快“到顶”。
     */
    private double orbitY() {
        return this.getStartY() + ORBIT_CLIMB;
    }

    /**
     * 上升曲线：{@code t^1.8} 的缓入曲线，在等级对应的上升时长结束时到达轨道高度。
     *
     * @param ticks 已经飞行的 tick 数
     */
    private double ascentY(int ticks) {
        double startY = this.getStartY();
        double t = Mth.clamp(ticks / (double) this.getAscentTicks(), 0.0D, 1.0D);
        double eased = Math.pow(t, ASCENT_CURVE);
        return startY + (this.orbitY() - startY) * eased;
    }

    /** 太空漂流的 Y 危险线：与原版虚空判定一致。 */
    public double getSpaceVoidY() {
        return this.level().getMinBuildHeight() - VANILLA_VOID_OFFSET;
    }

    /** 旧版原维度漂流兜底危险高度；太空维度不会使用它。 */
    public double getLegacyDriftDamageY() {
        double desired = this.getStartY() + LEGACY_DRIFT_DAMAGE_ALTITUDE;
        double belowOrbit = this.orbitY() - 64.0D;
        double floor = this.level().getMinBuildHeight() + 48.0D;
        return Mth.clamp(Math.min(desired, belowOrbit), floor, this.orbitY());
    }
    //endregion
    //region 发射平台
    @Nullable
    public BlockPos getLaunchPadPos() {
        return this.launchPadPos;
    }

    public void setLaunchPadPos(@Nullable BlockPos pos) {
        this.launchPadPos = pos;
    }

    /**
     * 火箭是否正停靠在完整的 2x2 发射平台上。
     */
    public boolean isOnLaunchPad() {
        BlockPos pad = this.launchPadPos;
        if (pad == null) {
            return false;
        }
        return LaunchPadBlock.isCompletePad(this.level(), pad);
    }

    /** 是否处于可以点火的停靠状态。 */
    public boolean isReadyToLaunch() {
        return this.getState() == STATE_IDLE
                && this.launchPadPos != null
                && LaunchPadBlock.isCompletePad(this.level(), this.launchPadPos);
    }

    /**
     * 点火：进入倒计时。发射平台此时**不再回收**，火箭仍然立在平台上；
     * 平台会在倒计时结束、真正起飞的瞬间才拆下并收进储物舱。
     *
     * @return 是否成功进入倒计时
     */
    public boolean ignite() {
        if (!this.isReadyToLaunch()) {
            return false;
        }
        BlockPos pad = this.launchPadPos;

        this.setState(STATE_COUNTDOWN);
        this.setTimer(COUNTDOWN_TICKS);
        this.flightTicks = 0;
        this.announceTicks = 0;
        this.noPhysics = true;
        this.setStartY(this.getY());
        this.setOrigin(this.level().dimension(), this.getX(), this.getY(), this.getZ());

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FLINTANDSTEEL_USE, SoundSource.NEUTRAL, 1.5F, 0.7F);
        return true;
    }

    /**
     * 拆掉 2x2 发射平台，并把平台物品放进储物舱。
     */
    private void dismantleLaunchPad(BlockPos pad) {
        int destroyed = 0;
        for (int dx = 0; dx < LaunchPadBlock.MULTIBLOCK_SIZE; dx++) {
            for (int dz = 0; dz < LaunchPadBlock.MULTIBLOCK_SIZE; dz++) {
                BlockPos pos = pad.offset(dx, 0, dz);
                if (this.level().getBlockState(pos).is(MachinesBlock.LAUNCH_PAD.get())) {
                    this.level().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    destroyed++;
                }
            }
        }
        this.launchPadPos = null;

        if (destroyed > 0) {
            ItemStack pads = new ItemStack(MachinesBlock.LAUNCH_PAD_ITEM.get(), destroyed);
            ItemStack leftover = this.cargo.addItem(pads);
            if (!leftover.isEmpty() && this.level() instanceof ServerLevel serverLevel) {
                Block.popResource(serverLevel, this.blockPosition(), leftover);
            }
        }
    }

    /** 是否处于上升或轨道状态。 */
    public boolean isFlying() {
        int state = this.getState();
        return state == STATE_ASCENT || state == STATE_ORBIT || state == STATE_DRIFT;
    }

    /** 拆除/收回时返回对应等级的火箭物品。 */
    public Item getDropItem() {
        if (this.getTier() >= RocketDestinations.TIER_3) {
            return MachinesBlock.TIER3_ROCKET_ITEM.get();
        }
        return this.getTier() >= RocketDestinations.TIER_2
                ? MachinesBlock.TIER2_ROCKET_ITEM.get()
                : MachinesBlock.ROCKET_ITEM.get();
    }

    /** 储物舱容器，供 GUI 使用。 */
    public SimpleContainer getCargo() {
        return this.cargo;
    }

    /** 把储物舱物品掉落在火箭位置。 */
    private void dropCargo() {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        NonNullList<ItemStack> items = this.cargo.getItems();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                Block.popResource(serverLevel, this.blockPosition(), stack.copy());
            }
        }
        this.cargo.clearContent();
    }
    //endregion
    //region 生命周期
    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide()) {
            this.clientTick();
            return;
        }

        this.enforceNoFlight();

        switch (this.getState()) {
            case STATE_IDLE -> this.idleTick();
            case STATE_COUNTDOWN -> this.countdownTick();
            case STATE_ASCENT -> this.ascentTick();
            case STATE_ORBIT -> this.orbitTick();
            case STATE_DRIFT -> this.driftTick();
            case STATE_DESCENT -> this.land();
            default -> this.setState(STATE_IDLE);
        }
    }

    private void idleTick() {
        this.flightTicks = 0;
        this.setDeltaMovement(Vec3.ZERO);

        if (!this.isOnLaunchPad() && !this.onGround()) {
            // 脱离平台后自由落体，避免火箭悬空
            this.setDeltaMovement(0.0D, -0.04D, 0.0D);
            this.move(net.minecraft.world.entity.MoverType.SELF, this.getDeltaMovement());
        }
    }

    private void countdownTick() {
        int timer = this.getTimer() - 1;
        this.setTimer(timer);

        if (this.level() instanceof ServerLevel serverLevel) {
            // 发动机从倒计时第一 tick 就开始排气：四个喷口同时向外喷烟。
            for (int i = 0; i < 4; i++) {
                double angle = Math.PI * 0.5D * i + this.tickCount * 0.07D;
                double dx = Math.cos(angle) * COUNTDOWN_SMOKE_RADIUS;
                double dz = Math.sin(angle) * COUNTDOWN_SMOKE_RADIUS;
                serverLevel.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        this.getX() + dx, this.getY() + 0.12D, this.getZ() + dz,
                        2, 0.18D, 0.08D, 0.18D, 0.012D);
                serverLevel.sendParticles(ParticleTypes.LARGE_SMOKE,
                        this.getX() + dx * 0.72D, this.getY() + 0.08D, this.getZ() + dz * 0.72D,
                        1, 0.12D, 0.04D, 0.12D, 0.008D);
            }
        }

        if (timer > 0 && timer % 20 == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.NEUTRAL,
                    1.0F, 1.0F + (COUNTDOWN_TICKS - timer) / (float) COUNTDOWN_TICKS);
        }

        if (timer <= 0) {
            // 起飞瞬间才拆掉 2x2 发射平台并收进储物舱：
            // 倒计时期间火箭必须仍然立在平台上。
            BlockPos pad = this.launchPadPos;
            if (pad != null) {
                this.dismantleLaunchPad(pad);
            }

            this.setState(STATE_ASCENT);
            this.flightTicks = 0;
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 4.0F, 0.6F);
            for (Entity passenger : this.getPassengers()) {
                if (passenger instanceof Player player) {
                    player.displayClientMessage(
                            Component.translatable("message.newgc.rocket.launched"), true);
                }
            }
        }
    }

    /**
     * 上升阶段（服务端权威）。
     * <p>
     * 位置完全由 {@link #ascentY(int)} 决定：不调用 {@code move}，不会被方块碰撞打断，
     * 也不依赖速度累加，保证在当前等级对应的上升时长后精确到达轨道高度。
     * <p>
     * 同时把进度写入同步数据，客户端会用同一公式自行推进（见 {@link #clientTick()}），
     * 这样客户端不会再被服务端位置包反复插值，从而消除上下抖动。
     */
    private void ascentTick() {
        this.flightTicks++;
        this.setTimer(this.flightTicks);

        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(this.getX(), this.ascentY(this.flightTicks), this.getZ());

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.FLAME,
                    this.getX(), this.getY() + 0.1D, this.getZ(),
                    6, 0.18D, 0.1D, 0.18D, 0.02D);
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY() + 0.05D, this.getZ(),
                    4, 0.22D, 0.1D, 0.22D, 0.03D);
        }

        if (this.flightTicks % 30 == 0) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.NEUTRAL, 3.0F, 0.7F);
        }

        if (this.flightTicks >= this.getAscentTicks()) {
            this.enterOrbit();
        }
    }

    /**
     * 进入轨道：停在轨道高度并通知乘客打开星图。
     */
    private void enterOrbit() {
        this.setState(STATE_ORBIT);
        this.flightTicks = 0;
        boolean hasDestination = this.hasDestination();
        this.setTimer(hasDestination ? 0 : NO_TARGET_GRACE_TICKS);
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(this.getX(), this.orbitY(), this.getZ());
        this.noPhysics = true;

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 2.0F, 0.6F);

        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof ServerPlayer serverPlayer) {
                if (hasDestination) {
                    serverPlayer.displayClientMessage(Component.translatable(
                            "message.newgc.rocket.orbit_targets",
                            Component.translatable(
                                    RocketDestinations.tierNameKey(this.getTier())),
                            this.destinationSummaryComponent()), false);
                } else {
                    serverPlayer.displayClientMessage(Component.translatable(
                            "message.newgc.rocket.no_targets",
                            NO_TARGET_GRACE_TICKS / 20), false);
                }
                serverPlayer.displayClientMessage(
                        Component.translatable("message.newgc.rocket.orbit"), true);
            }
        }
        this.announceOrbit();
    }

    private void orbitTick() {
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(this.getX(), this.orbitY(), this.getZ());

        // 没有可用目标：星图会显示警告，倒计时结束后自动进入漂流。
        if (!this.hasDestination()) {
            int timer = this.getTimer();
            if (timer > 0) {
                this.setTimer(timer - 1);
                if ((timer - 1) % 20 == 0 && timer > 1) {
                    for (Entity passenger : this.getPassengers()) {
                        if (passenger instanceof ServerPlayer serverPlayer) {
                            serverPlayer.displayClientMessage(Component.translatable(
                                    "message.newgc.rocket.no_targets_countdown",
                                    (timer - 1) / 20), true);
                        }
                    }
                }
            } else {
                this.beginDrift();
                return;
            }
        } else if (this.getTimer() != 0) {
            this.setTimer(0);
        }

        if (this.announceTicks > 0) {
            this.announceTicks--;
            // 每 20 tick 重复通知一次，方便网络卡顿或重新登录后仍能弹出星图
            if (this.announceTicks % 20 == 0) {
                this.sendOrbitPacket();
            }
        }
    }

    /**
     * 进入漂流：没有可用目的地时由倒计时或玩家关闭星图触发。
     * <p>
     * 正常路径会把火箭和玩家送入独立的 {@code newgc:space} 空维度，
     * 沿 Y 轴向下漂移；到达原版虚空高度 {@code minBuildHeight - 64} 后
     * 每 20 tick 受到虚空伤害。进入前会记录起飞星球和平台坐标，死亡重生时返回那里。
     *
     * @return 是否成功进入漂流
     */
    public boolean beginDrift() {
        if (this.getState() != STATE_ORBIT || this.hasDestination()) {
            return false;
        }
        if (!(this.level() instanceof ServerLevel sourceLevel)) {
            return false;
        }

        ServerPlayer passenger = null;
        for (Entity entity : this.getPassengers()) {
            if (entity instanceof ServerPlayer serverPlayer) {
                passenger = serverPlayer;
                break;
            }
        }
        if (passenger == null) {
            return false;
        }

        ServerLevel spaceLevel = sourceLevel.getServer().getLevel(ModDimensions.SPACE_LEVEL_KEY);
        if (spaceLevel == null) {
            return this.beginLegacyDrift(passenger);
        }

        ResourceKey<Level> originKey = this.originDimension != null
                ? this.originDimension : sourceLevel.dimension();
        double originX = this.originDimension != null ? this.originX : this.getX();
        double originY = this.originDimension != null ? this.originY : this.getStartY();
        double originZ = this.originDimension != null ? this.originZ : this.getZ();

        RocketEntity arrival = ModEntities.ROCKET.get().create(spaceLevel);
        if (arrival == null) {
            return this.beginLegacyDrift(passenger);
        }

        SpaceReturnData.store(passenger, originKey,
                originX, originY, originZ, passenger.getYRot(), passenger.getXRot());
        passenger.setRespawnPosition(originKey,
                BlockPos.containing(originX, originY, originZ),
                passenger.getYRot(), true, false);

        arrival.setTier(this.getTier());
        arrival.setOrigin(originKey, originX, originY, originZ);
        double startX = SpaceLocations.startX(originKey);
        arrival.copyCargoFrom(this);
        arrival.startSpaceDrift();
        arrival.moveTo(startX, SPACE_DRIFT_START_Y, SPACE_DRIFT_START_Z,
                passenger.getYRot(), passenger.getXRot());

        passenger.stopRiding();
        this.discard();
        spaceLevel.addFreshEntity(arrival);
        passenger.teleportTo(spaceLevel,
                startX, SPACE_DRIFT_START_Y, SPACE_DRIFT_START_Z,
                passenger.getYRot(), passenger.getXRot());
        passenger.startRiding(arrival, true);
        passenger.displayClientMessage(Component.translatable(
                "message.newgc.rocket.drift_start",
                String.format(java.util.Locale.ROOT, "%.1f", this.getSpaceVoidY())), false);
        spaceLevel.playSound(null, startX, SPACE_DRIFT_START_Y, SPACE_DRIFT_START_Z,
                SoundEvents.ELYTRA_FLYING, SoundSource.NEUTRAL, 1.2F, 0.55F);
        return true;
    }

    /** 太空维度不可用时的旧版兜底：仍留在原维度，按高度触发伤害。 */
    private boolean beginLegacyDrift(ServerPlayer passenger) {
        this.setState(STATE_DRIFT);
        this.flightTicks = 0;
        this.setTimer(0);
        this.announceTicks = 0;
        this.noPhysics = true;
        this.setDeltaMovement(Vec3.ZERO);
        passenger.displayClientMessage(Component.translatable(
                "message.newgc.rocket.drift_start_legacy",
                String.format(java.util.Locale.ROOT, "%.1f", this.getLegacyDriftDamageY())), false);
        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.ELYTRA_FLYING, SoundSource.NEUTRAL, 1.2F, 0.55F);
        return true;
    }

    private void driftTick() {
        this.flightTicks++;
        this.setDeltaMovement(Vec3.ZERO);
        this.noPhysics = true;

        if (this.level().dimension().equals(ModDimensions.SPACE_LEVEL_KEY)) {
            spaceDriftTick();
        } else {
            legacyDriftTick();
        }
    }

    /** 太空漂流：沿 Y 轴下降，到达原版虚空高度后开始伤害。 */
    private void spaceDriftTick() {
        double swayZ = Math.cos(this.flightTicks * 0.017D) * 0.04D;
        double voidY = this.getSpaceVoidY();
        double nextY = Math.max(voidY, this.getY() - SPACE_DRIFT_SPEED_Y);
        this.setPos(this.getX(), nextY, this.getZ() + swayZ);

        spawnDriftParticles();
        if (this.getY() > voidY) {
            return;
        }
        if (this.flightTicks % DRIFT_DAMAGE_INTERVAL == 0) {
            damageSurvivalPassengers();
        }
    }

    /** 旧版兜底漂流：原维度缓慢下降，低于安全高度后伤害。 */
    private void legacyDriftTick() {
        double nextY = this.getY() - LEGACY_DRIFT_FALL_SPEED;
        double swayZ = Math.cos(this.flightTicks * 0.017D) * 0.04D;
        this.setPos(this.getX(), nextY, this.getZ() + swayZ);

        spawnDriftParticles();
        if (this.getY() > this.getLegacyDriftDamageY()) {
            return;
        }
        if (this.flightTicks % DRIFT_DAMAGE_INTERVAL == 0) {
            damageSurvivalPassengers();
        }

        if (this.getY() < this.level().getMinBuildHeight() - 64.0D
                && this.getPassengers().isEmpty()) {
            this.discard();
        }
    }

    private void spawnDriftParticles() {
        if (this.flightTicks % 4 == 0 && this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.CLOUD,
                    this.getX(), this.getY() + 0.3D, this.getZ(),
                    2, 0.25D, 0.15D, 0.25D, 0.01D);
        }
    }

    /** 创造/旁观模式不受伤，也不发送伤害提示。 */
    private void damageSurvivalPassengers() {
        for (Entity passenger : this.getPassengers()) {
            if (!(passenger instanceof ServerPlayer serverPlayer)
                    || serverPlayer.isCreative() || serverPlayer.isSpectator()) {
                continue;
            }
            serverPlayer.hurt(serverPlayer.damageSources().fellOutOfWorld(), 2.0F);
            serverPlayer.displayClientMessage(
                    Component.translatable("message.newgc.rocket.drift_damage"), true);
        }
    }

    /**
     * 重新通知乘客打开星图（重新登录恢复轨道界面时使用）。
     */
    public void announceOrbit() {
        this.announceTicks = 60;
        this.sendOrbitPacket();
    }

    private void sendOrbitPacket() {
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof ServerPlayer serverPlayer) {
                PacketDistributor.sendToPlayer(serverPlayer, new RocketOrbitPacket());
            }
        }
    }

    /**
     * 直接落到当前高度（没有平台时作为安全兜底）。
     */
    private void land() {
        this.setDeltaMovement(Vec3.ZERO);
        this.flightTicks = 0;
        this.noPhysics = false;
        this.setState(STATE_IDLE);

        this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                SoundEvents.FIREWORK_ROCKET_BLAST, SoundSource.NEUTRAL, 1.5F, 0.8F);
    }

    /**
     * 客户端只做少量本地粒子，保证在网络延迟下也能看到火焰。
     */
    private void clientTick() {
        int state = this.getState();
        // 上升阶段由客户端用与服务端相同的公式推进位置：每 tick 连续变化，
        // 再由渲染器做帧间插值，避免服务端位置包反复插值造成的上下抖动。
        if (state == STATE_ASCENT) {
            int tick = this.getTimer();
            if (tick > 0) {
                this.setPos(this.getX(), this.ascentY(tick), this.getZ());
            }
        }

        if (state != STATE_ASCENT && state != STATE_COUNTDOWN) {
            return;
        }

        if (state == STATE_COUNTDOWN) {
            // 本地也生成一圈向外扩散的烟，避免网络延迟时看不到发动机预燃。
            for (int i = 0; i < 4; i++) {
                double angle = Math.PI * 0.5D * i + this.tickCount * 0.07D;
                double dx = Math.cos(angle) * COUNTDOWN_SMOKE_RADIUS;
                double dz = Math.sin(angle) * COUNTDOWN_SMOKE_RADIUS;
                this.level().addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        this.getX() + dx, this.getY() + 0.12D, this.getZ() + dz,
                        dx * 0.045D, 0.018D, dz * 0.045D);
            }
        }

        for (int i = 0; i < 2; i++) {
            double dx = (this.random.nextDouble() - 0.5D) * 0.25D;
            double dz = (this.random.nextDouble() - 0.5D) * 0.25D;
            this.level().addParticle(
                    state == STATE_ASCENT ? ParticleTypes.FLAME : ParticleTypes.SMOKE,
                    this.getX() + dx, this.getY() + 0.1D, this.getZ() + dz,
                    0.0D, state == STATE_ASCENT ? -0.05D : 0.02D, 0.0D);
        }
    }

    /**
     * 上升阶段的位置完全由客户端推进，这里忽略服务端的插值位置，避免两种来源互相拉扯。
     */
    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps) {
        if (this.level().isClientSide() && this.getState() == STATE_ASCENT) {
            return;
        }
        super.lerpTo(x, y, z, yRot, xRot, steps);
    }
    //endregion
    //region 交互与骑乘
    @Override
    public @NotNull InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (this.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (this.getState() != STATE_IDLE) {
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.busy"), true);
            return InteractionResult.CONSUME;
        }

        // 潜行右键：点火
        if (player.isSecondaryUseActive()) {
            if (player instanceof ServerPlayer serverPlayer) {
                RocketLaunchService.LaunchResult result =
                        RocketLaunchService.request(serverPlayer, this);
                if (result == RocketLaunchService.LaunchResult.LAUNCHED) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.newgc.rocket.countdown",
                                    COUNTDOWN_TICKS / 20), true);
                } else if (result == RocketLaunchService.LaunchResult.FAILED) {
                    serverPlayer.displayClientMessage(
                            Component.translatable("message.newgc.rocket.need_pad"), true);
                }
            }
            return InteractionResult.CONSUME;
        }

        // 普通右键：登乘
        if (this.getPassengers().isEmpty()) {
            return player.startRiding(this) ? InteractionResult.CONSUME : InteractionResult.PASS;
        }
        return InteractionResult.PASS;
    }

    /**
     * 左键点击火箭：直接拆成掉落物（火箭物品 + 储物舱全部内容）。
     */
    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) {
        if (this.level().isClientSide()) {
            return true;
        }
        if (!(source.getEntity() instanceof Player player)) {
            return false;
        }
        if (!this.isRemoved()) {
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.destroyed"), true);
            this.breakIntoDrop();
        }
        return true;
    }

    /**
     * 拆除火箭：弹出乘客，掉落火箭物品与储物舱内容。
     */
    public void breakIntoDrop() {
        this.ejectPassengers();
        this.dropCargo();
        if (this.level() instanceof ServerLevel serverLevel) {
            Block.popResource(serverLevel, this.blockPosition(),
                    new ItemStack(this.getDropItem()));
        }
        this.discard();
    }

    @Override
    protected void addPassenger(@NotNull Entity passenger) {
        super.addPassenger(passenger);
        if (!this.level().isClientSide() && passenger instanceof Player player) {
            this.disableFlight(player);
            player.displayClientMessage(
                    Component.translatable("message.newgc.rocket.boarded"), true);
        }
    }

    @Override
    protected void removePassenger(@NotNull Entity passenger) {
        super.removePassenger(passenger);
        if (!this.level().isClientSide() && passenger instanceof Player player) {
            this.restoreFlight(player);
        }
    }

    /**
     * 登乘期间禁用创造/旁观飞行，避免玩家按空格直接脱离火箭。
     */
    private void disableFlight(Player player) {
        if (player.getAbilities().mayfly || player.getAbilities().flying) {
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }
    }

    /**
     * 下船后恢复创造/旁观玩家的飞行权限。
     */
    private void restoreFlight(Player player) {
        if ((player.isCreative() || player.isSpectator()) && !player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            player.onUpdateAbilities();
        }
    }

    private void enforceNoFlight() {
        if (this.getPassengers().isEmpty()) {
            return;
        }
        for (Entity passenger : this.getPassengers()) {
            if (passenger instanceof Player player) {
                this.disableFlight(player);
            }
        }
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, Entity.@NotNull MoveFunction moveFunction) {
        // 让乘客坐在机身内部，与细长化的驾驶舱高度对应
        moveFunction.accept(passenger, this.getX(), this.getY() + 2.3D, this.getZ());
    }

    @Override
    protected boolean canAddPassenger(@NotNull Entity passenger) {
        return this.getPassengers().isEmpty();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        // 有乘客时关闭自身碰撞，避免把骑乘者挤出火箭
        return this.getPassengers().isEmpty();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return distance < 65536.0D;
    }
    //endregion

    //region 存档
    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("RocketState", this.getState());
        tag.putInt("RocketTier", this.getTier());
        tag.putInt("RocketTimer", this.getTimer());
        tag.putInt("FlightTicks", this.flightTicks);
        tag.putDouble("StartY", this.getStartY());
        if (this.originDimension != null) {
            tag.putString("OriginDimension", this.originDimension.location().toString());
            tag.putDouble("OriginX", this.originX);
            tag.putDouble("OriginY", this.originY);
            tag.putDouble("OriginZ", this.originZ);
        }
        if (this.launchPadPos != null) {
            tag.putLong("LaunchPad", this.launchPadPos.asLong());
        }
        ContainerHelper.saveAllItems(tag, this.cargo.getItems(), this.registryAccess());
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.setState(tag.getInt("RocketState"));
        this.setTier(tag.contains("RocketTier")
                ? tag.getInt("RocketTier") : RocketDestinations.TIER_1);
        this.setTimer(tag.getInt("RocketTimer"));
        this.flightTicks = tag.getInt("FlightTicks");
        this.setStartY(tag.getDouble("StartY"));
        if (tag.contains("OriginDimension")) {
            ResourceLocation originId = ResourceLocation.tryParse(tag.getString("OriginDimension"));
            if (originId != null) {
                this.setOrigin(ResourceKey.create(Registries.DIMENSION, originId),
                        tag.getDouble("OriginX"),
                        tag.getDouble("OriginY"),
                        tag.getDouble("OriginZ"));
            }
        }
        if (tag.contains("LaunchPad")) {
            this.launchPadPos = BlockPos.of(tag.getLong("LaunchPad"));
        }
        ContainerHelper.loadAllItems(tag, this.cargo.getItems(), this.registryAccess());
        this.noPhysics = this.getState() == STATE_ASCENT
                || this.getState() == STATE_ORBIT
                || this.getState() == STATE_DRIFT;
    }
    //endregion
}
