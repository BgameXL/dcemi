package dev.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LevelRenderer;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelRenderer.class)
public class mixinLevel {

    @Inject(method = "renderLevel", at = @At("HEAD"), cancellable = true)
    private void dcemiRender(PoseStack poseStack, float partialTick, long finishNanoTime,
                             boolean renderBlockOutline, net.minecraft.client.Camera camera,
                             net.minecraft.client.renderer.GameRenderer gameRenderer,
                             net.minecraft.client.renderer.LightTexture lightTexture,
                             Matrix4f projectionMatrix, CallbackInfo ci) {
        ci.cancel();
    }
}