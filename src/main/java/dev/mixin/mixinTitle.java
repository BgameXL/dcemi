package dev.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class mixinTitle {

    private static boolean dcemitriggered = false;

    @Inject(method = "init", at = @At("HEAD"))
    private void dcemiautoLoad(CallbackInfo ci) {
        if (dcemitriggered) return;
        dcemitriggered = true;

        Minecraft mc = Minecraft.getInstance();
        String worldName = "dcemi_world";

        if (mc.getLevelSource().levelExists(worldName)) {
            mc.createWorldOpenFlows().loadLevel((TitleScreen) (Object) this, worldName);
        } else {
            System.err.println("[DCEMI World] 'dcemi_world' not found. Please create a singleplayer world named 'dcemi_world' (Superflat, Creative) and restart.");
        }
    }
}