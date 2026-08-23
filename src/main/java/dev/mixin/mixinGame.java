package dev.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public class mixinGame {

    @Inject(method = "renderItemInHand", at = @At("HEAD"), cancellable = true)
    private void dcemiCancel(PoseStack poseStack, net.minecraft.client.Camera camera,
                             float partialTick, CallbackInfo ci) {
        ci.cancel();
    }

    @Inject(method = "bobHurt", at = @At("HEAD"), cancellable = true)
    private void dcemicancelBob(PoseStack poseStack, float partialTick, CallbackInfo ci) {
        ci.cancel();
    }
}