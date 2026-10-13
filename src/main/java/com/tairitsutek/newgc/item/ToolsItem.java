package com.tairitsutek.newgc.item;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ToolsItem {

    //region 注册器
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(NewGalaxyMain.MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NewGalaxyMain.MODID);
    //endregion

    //region 工具与装备
    public static final DeferredItem<Item> TELEPORTER = registerItem("teleporter");
    public static final DeferredItem<Item> IRON_HAMMER = ITEMS.register("iron_hammer", () -> new HammerItem(new Item.Properties()));
    public static final DeferredItem<Item> OXYGEN_TANK_BASIC = ITEMS.register("oxygen_tank_basic", () -> new OxygenTankItem(new Item.Properties().durability(1500)));
    public static final DeferredItem<Item> OXYGEN_TANK_INTERMEDIATE = ITEMS.register("oxygen_tank_intermediate", () -> new OxygenTankItem(new Item.Properties().durability(3000)));
    public static final DeferredItem<Item> OXYGEN_TANK_ADVANCED = ITEMS.register("oxygen_tank_advanced", () -> new OxygenTankItem(new Item.Properties().durability(9000)));
    public static final DeferredItem<Item> OXYGEN_TANK_ULTIMATE = ITEMS.register("oxygen_tank_ultimate", () -> new OxygenTankItem(new Item.Properties().durability(27000)));
    public static final DeferredItem<Item> OXYGEN_TANK_SUPREME = ITEMS.register("oxygen_tank_supreme", () -> new OxygenTankItem(new Item.Properties().durability(81000)));
    //endregion

    //region 注册辅助
    private static DeferredItem<Item> registerItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }
    //endregion

    //region 创造模式物品栏
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TOOLS_TAB =
            CREATIVE_MODE_TABS.register("tools_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.newgc.tools"))
                    .icon(() -> new ItemStack(TELEPORTER.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(TELEPORTER.get());
                        output.accept(IRON_HAMMER.get());

                        output.accept(OXYGEN_TANK_BASIC.get());
                        output.accept(OXYGEN_TANK_INTERMEDIATE.get());
                        output.accept(OXYGEN_TANK_ADVANCED.get());
                        output.accept(OXYGEN_TANK_ULTIMATE.get());
                        output.accept(OXYGEN_TANK_SUPREME.get());
                    })
                    .build());
    //endregion

    //region 注册入口
    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
        CREATIVE_MODE_TABS.register(eventBus);
    }
    //endregion
}