package dev.command;

import dev.render.Reciperenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.concurrent.atomic.AtomicBoolean;

@OnlyIn(Dist.CLIENT)
public class Consolehandler {

    private static final AtomicBoolean running = new AtomicBoolean(false);

    public static void start() {
        if (running.getAndSet(true)) return;

        Thread thread = new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.trim();
                    if (trimmed.isEmpty()) continue;
                    dispatch(trimmed);
                }
            } catch (Exception e) {
                System.err.println("DCEMI stdin closed: " + e.getMessage());
            }
        }, "DCEMI-stdin");
        thread.setDaemon(true);
        thread.start();
    }

    private static void dispatch(String line) {
        String[] parts = line.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1] : "";

        switch (cmd) {
            case "/list" -> Minecraft.getInstance().execute(() ->
                    List.execute(args.trim()));
            case "/recipe" -> Minecraft.getInstance().execute(() ->
                    Recipe.execute(args.trim()));
            case "/render" -> Minecraft.getInstance().execute(() ->
                    Reciperenderer.execute(args.trim()));
            default -> System.err.println("DCEMI Unknown command: " + cmd);
        }
    }
}