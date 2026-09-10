package dev.mixin;

import dev.runtime.DcemiState;
import net.minecraft.client.multiplayer.ClientLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// no use
@Mixin(ClientLevel.class)
public class mixinClientLevel {

    @Inject(method = "tickEntities", at = @At("HEAD"), cancellable = true)
    private void dcemiFreeze(CallbackInfo ci) {
        if (DcemiState.RENDER_MODE) {
            ci.cancel();
        }
    }
}
