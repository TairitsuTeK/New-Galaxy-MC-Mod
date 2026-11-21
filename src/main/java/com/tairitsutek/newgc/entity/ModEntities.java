package com.tairitsutek.newgc.entity;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 模组实体注册表。
 */
public final class ModEntities {

    private ModEntities() {
    }

    //region 注册器
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, NewGalaxyMain.MODID);
    //endregion

    //region 火箭
    /**
     * 火箭实体：只能停靠在 2x2 发射平台上，并且只有停在发射平台上才能点火。
     * <p>
     * 一阶火箭为 1.4 x 6 格的细长实体。
     */
    public static final DeferredHolder<EntityType<?>, EntityType<RocketEntity>> ROCKET =
            ENTITY_TYPES.register("rocket", () -> EntityType.Builder
                    .<RocketEntity>of(RocketEntity::new, MobCategory.MISC)
                    .sized(1.2F, 6.0F)
                    .clientTrackingRange(12)
                    .updateInterval(1)
                    .build("rocket"));
    //endregion

    //region 注册入口
    public static void register(IEventBus modEventBus) {
        ENTITY_TYPES.register(modEventBus);
    }
    //endregion
}
