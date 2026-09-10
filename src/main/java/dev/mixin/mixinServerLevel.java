package dev.mixin;

import dev.runtime.DcemiState;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.function.BooleanSupplier;

// no use
@Mixin(ServerLevel.class)
public class mixinServerLevel {

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void dcemiFreeze(BooleanSupplier hasTimeLeft, CallbackInfo ci) {
        if (DcemiState.RENDER_MODE) {
            ci.cancel();
        }
    }
}
