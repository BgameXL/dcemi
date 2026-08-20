package dev;

import dev.command.Consolehandler;
import dev.runtime.Signal;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("dcemi")
public class dcemi {

    public static final String MOD_ID = "dcemi";

    public dcemi() {
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(this::clientside);
    }

    private void clientside(FMLClientSetupEvent event) {
        Consolehandler.start();
        Signal.register();
    }
}