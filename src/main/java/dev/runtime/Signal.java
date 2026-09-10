package dev.runtime;

import dev.emi.emi.api.EmiApi;
import dev.emi.emi.runtime.EmiReloadManager;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@OnlyIn(Dist.CLIENT)
public class Signal {

    private boolean signaled = false;

    public static void register() {
        MinecraftForge.EVENT_BUS.register(new Signal());
    }

    @SubscribeEvent
    public void onclientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        boolean ready = Minecraft.getInstance().level != null
                && EmiReloadManager.isLoaded() && !EmiApi.getIndexStacks().isEmpty();
        DcemiState.RENDER_MODE = ready;
        if (ready && !signaled) {
            signaled = true;

            CommandServer.start();
            System.err.println("DCEMI_READY");
            System.err.flush();
        }
    }
}
