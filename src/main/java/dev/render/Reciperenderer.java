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
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Matrix4f;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class Reciperenderer {

    private static final int PADDING = 8;

    public static void execute(String args) {
        if (args.isEmpty()) {
            writerror("Usage: /render <recipe_id>");
            return;
        }

        ResourceLocation recipeId;
        try {
            recipeId = new ResourceLocation(args.trim());
        } catch (Exception e) {
            writerror("Invalid resource location: " + args);
            return;
        }

        EmiRecipe recipe = EmiApi.getRecipeManager().getRecipe(recipeId);

        if (recipe == null) {
            writerror("Recipe not found: " + args);
            return;
        }

        renderR(recipe);
    }

    private static void renderR(EmiRecipe recipe) {
        Minecraft mc = Minecraft.getInstance();
        int width = recipe.getDisplayWidth() + PADDING * 2;
        int height = recipe.getDisplayHeight() + PADDING * 2;

        RenderTarget fbo = new TextureTarget(width, height, true, Minecraft.ON_OSX);
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
            w.render(graphics, 0, 0, partialTick);
        }
        graphics.pose().popPose();
        graphics.flush();

        mvs.popPose();
        RenderSystem.applyModelViewMatrix();
        fbo.unbindWrite();

        writefbopng(fbo);
        fbo.destroyBuffers();
    }

    private static void writefbopng(RenderTarget fbo) {
        try (NativeImage img = Screenshot.takeScreenshot(fbo)) {
            byte[] png = img.asByteArray();

            // --- temporary dev-aid: also dump to <gameDir>/dcemi_last.png so the
            //     render can be eyeballed without capturing the binary stdout.
            //     Remove once the Python harness is verified.
            try {
                img.writeToFile(new File(Minecraft.getInstance().gameDirectory, "dcemi_last.png"));
            } catch (IOException ignored) {
            }

            OutputStream out = System.out;
            int len = png.length;
            out.write((len >>> 24) & 0xFF);
            out.write((len >>> 16) & 0xFF);
            out.write((len >>> 8) & 0xFF);
            out.write(len & 0xFF);
            out.write(png);
            out.flush();
        } catch (IOException e) {
            System.err.println("[DCEMI] PNG write failed: " + e.getMessage());
        }
    }

    private static void writerror(String msg) {
        try {
            OutputStream out = System.out;
            out.write(0);
            out.write(0);
            out.write(0);
            out.write(0);
            out.flush();
        } catch (IOException ignored) {
        }
        System.err.println("[DCEMI] " + msg);
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