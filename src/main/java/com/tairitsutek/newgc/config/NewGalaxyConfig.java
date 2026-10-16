package com.tairitsutek.newgc.config;

import net.neoforged.neoforge.common.ModConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public class NewGalaxyConfig {

    public static final ModConfigSpec SPEC;
    public static final Client CLIENT;

    static {
        Pair<Client, ModConfigSpec> clientPair =
                new ModConfigSpec.Builder().configure(Client::new);
        CLIENT = clientPair.getLeft();
        SPEC = clientPair.getRight();
    }

    public static class Client {
        public final ModConfigSpec.BooleanValue debugMode;

        public Client(ModConfigSpec.Builder builder) {
            builder.comment("调试设置").push("debug");
            debugMode = builder
                    .comment("开启后，在星球 HUD 下方显示调试按钮，可直接切换环境状态")
                    .define("debugMode", false);
            builder.pop();
        }
    }
}