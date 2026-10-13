package com.tairitsutek.newgc.event.oxygen;

import com.tairitsutek.newgc.NewGalaxyMain;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModAttachments {

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, NewGalaxyMain.MODID);

    public static final DeferredHolder<AttachmentType<?>, AttachmentType<OxygenData>> OXYGEN =
            ATTACHMENTS.register("oxygen", () -> AttachmentType.builder(OxygenData::new)
                    .serialize(OxygenData.CODEC)
                    .build());

    public static void register(IEventBus bus) {
        ATTACHMENTS.register(bus);
    }
}