package dev.mixin;

import net.minecraft.client.sounds.SoundEngine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SoundEngine.class)
public class mixinSound {

    @Inject(method = "play", at = @At("HEAD"), cancellable = true)
    private void dcemiPlay(net.minecraft.client.resources.sounds.SoundInstance instance, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "playDelayed", at = @At("HEAD"), cancellable = true)
    private void dcemiDelayed(net.minecraft.client.resources.sounds.SoundInstance instance,
                              int delay, CallbackInfo ci) {
        ci.cancel();
    }
}