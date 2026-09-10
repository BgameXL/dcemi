package dev.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.widget.Widget;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.widget.RecipeBackground;
import dev.runtime.Channel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class Reciperenderer {

    private static final int PADDING = 8;

    private static final int DEFAULT_SCALE = 4;

    public static void execute(String args, Channel channel) {
        if (args.isEmpty()) {
            writerror(channel, "Usage: /render <recipe_id>");
            return;
        }

        ResourceLocation recipeId;
        try {
            recipeId = new ResourceLocation(args.trim());
        } catch (Exception e) {
            writerror(channel, "Invalid resource location: " + args);
            return;
        }

        EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(recipeId);

        if (recipe == null) {
            writerror(channel, "Recipe not found: " + args);
            return;
        }

        renderR(recipe, channel);
    }

    private static void renderR(EmiRecipe recipe, Channel channel) {
        Minecraft mc = Minecraft.getInstance();
        int width = recipe.getDisplayWidth() + PADDING * 2;
        int height = recipe.getDisplayHeight() + PADDING * 2;
        int scale = scale();
        RenderTarget fbo = new TextureTarget(width * scale, height * scale, true, Minecraft.ON_OSX);
        fbo.setClearColor(0.15f, 0.15f, 0.15f, 1.0f);
        fbo.clear(Minecraft.ON_OSX);
        fbo.bindWrite(true);

        RenderSystem.setProjectionMatrix(
                new Matrix4f().ortho(0, width, height, 0, 1000, 3000),
                com.mojang.blaze3d.vertex.VertexSorting.ORTHOGRAPHIC_Z
        );
        PoseStack mvs = RenderSystem.getModelViewStack();
        mvs.pushPose();
        mvs.setIdentity();
        mvs.translate(0, 0, -2000);
        RenderSystem.applyModelViewMatrix();

        GuiGraphics graphics = new GuiGraphics(mc, mc.renderBuffers().bufferSource());

        widgetHolder holder = new widgetHolder(recipe.getDisplayWidth(), recipe.getDisplayHeight());
        holder.add(new RecipeBackground(-4, -4, recipe.getDisplayWidth() + 8, recipe.getDisplayHeight() + 8));
        recipe.addWidgets(holder);

        graphics.pose().pushPose();
        graphics.pose().translate(PADDING, PADDING, 0);
        float partialTick = mc.getFrameTime();
        for (Widget w : holder.widgets) {
            w.render(graphics, -1000, -1000, partialTick);
        }
        graphics.pose().popPose();
        graphics.flush();

        mvs.popPose();
        RenderSystem.applyModelViewMatrix();
        fbo.unbindWrite();

        writefbopng(fbo, channel);
        fbo.destroyBuffers();
    }

    private static void writefbopng(RenderTarget fbo, Channel channel) {
        try (NativeImage img = Screenshot.takeScreenshot(fbo)) {
            byte[] png = img.asByteArray();

            try {
                img.writeToFile(new File(Minecraft.getInstance().gameDirectory, ""));
            } catch (IOException ignored) {
            }

            channel.writeFrame(png);
        } catch (IOException e) {
            System.err.println("[DCEMI] PNG write failed: " + e.getMessage());
        }
    }

    private static void writerror(Channel channel, String msg) {
        channel.writeErrorFrame();
        System.err.println("[DCEMI] " + msg);
    }

    private static int scale() {
        Integer sys = Integer.getInteger("dcemi.scale");
        if (sys != null) return clampScale(sys);
        String env = System.getenv("DCEMI_SCALE");
        if (env != null) {
            try {
                return clampScale(Integer.parseInt(env.trim()));
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_SCALE;
    }

    private static int clampScale(int v) {
        return Math.max(1, Math.min(v, 16));
    }

    private static class widgetHolder implements WidgetHolder {
        final List<Widget> widgets = new ArrayList<>();
        private final int w, h;

        widgetHolder(int w, int h) {
            this.w = w;
            this.h = h;
        }

        @Override
        public int getWidth() {
            return w;
        }

        @Override
        public int getHeight() {
            return h;
        }

        @Override
        public <T extends Widget> T add(T widget) {
            widgets.add(widget);
            return widget;
        }
    }
}