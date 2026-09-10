package dev.runtime;

import dev.command.List;
import dev.command.Recipe;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.runtime.EmiReloadManager;
import dev.render.Reciperenderer;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

@OnlyIn(Dist.CLIENT)
public final class CommandServer {

    private static final int DEFAULT_PORT = 25599;
    private static final AtomicBoolean running = new AtomicBoolean(false);

    private CommandServer() {
    }

    public static void start() {
        if (running.getAndSet(true)) return;
        String bind = property("dcemi.bind", "DCEMI_BIND", "0.0.0.0");
        int port = port();
        Thread thread = new Thread(() -> serve(bind, port), "DCEMI-socket");
        thread.setDaemon(true);
        thread.start();
    }

    private static void serve(String bind, int port) {
        try (ServerSocket server = new ServerSocket()) {
            server.setReuseAddress(true);
            server.bind(new InetSocketAddress(bind, port));
            System.err.println("[DCEMI] command server listening on " + bind + ":" + port);
            while (true) {
                Socket socket = server.accept();
                Thread conn = new Thread(() -> handle(socket), "DCEMI-conn");
                conn.setDaemon(true);
                conn.start();
            }
        } catch (java.net.BindException e) {
            System.err.println("[DCEMI] port " + port + " already in use another game instance is"
                    + " still running. Kill it (fuser -k " + port + "/tcp) and relaunch. No socket this run.");
            running.set(false);
        } catch (Exception e) {
            System.err.println("[DCEMI] command server stopped: " + e.getMessage());
            running.set(false);
        }
    }

    private static void handle(Socket socket) {
        Minecraft mc = Minecraft.getInstance();
        try (Socket s = socket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(s.getInputStream(), StandardCharsets.UTF_8))) {
            Channel channel = new Channel(s.getOutputStream());
            String line;
            while ((line = reader.readLine()) != null) {
                String trimmed = line.trim();
                if (trimmed.isEmpty()) continue;

                CompletableFuture<Void> done = new CompletableFuture<>();
                mc.execute(() -> {
                    try {
                        dispatch(trimmed, channel);
                    } finally {
                        done.complete(null);
                    }
                });
                done.join();
            }
        } catch (Exception e) {
            System.err.println("[DCEMI] connection closed: " + e.getMessage());
        }
    }

    private static void dispatch(String line, Channel channel) {
        String[] parts = line.split(" ", 2);
        String cmd = parts[0].toLowerCase();
        String args = parts.length > 1 ? parts[1].trim() : "";
        boolean binary = cmd.equals("/render");

        if (!emiReady()) {
            respondError(channel, binary, "not ready: no world loaded / EMI index cleared");
            return;
        }

        try {
            switch (cmd) {
                case "/list" -> List.execute(args, channel);
                case "/recipe" -> Recipe.execute(args, channel);
                case "/render" -> Reciperenderer.execute(args, channel);
                default -> channel.writeLine("{\"error\":\"Unknown command: " + cmd + "\"}");
            }
        } catch (Throwable t) {
            System.err.println("[DCEMI] command '" + cmd + "' failed: " + t);
            respondError(channel, binary, t.toString());
        }
    }

    private static boolean emiReady() {
        try {
            return EmiReloadManager.isLoaded() && !EmiApi.getIndexStacks().isEmpty();
        } catch (Throwable t) {
            return false;
        }
    }

    private static void respondError(Channel channel, boolean binary, String msg) {
        if (binary) {
            channel.writeErrorFrame();
        } else {
            channel.writeLine("{\"error\":\"" + msg.replace('"', '\'') + "\"}");
        }
    }

    private static int port() {
        Integer sys = Integer.getInteger("dcemi.port");
        if (sys != null) return sys;
        String env = System.getenv("DCEMI_PORT");
        if (env != null) {
            try {
                return Integer.parseInt(env.trim());
            } catch (NumberFormatException ignored) {
            }
        }
        return DEFAULT_PORT;
    }

    private static String property(String sysKey, String envKey, String fallback) {
        String v = System.getProperty(sysKey);
        if (v != null) return v;
        v = System.getenv(envKey);
        return v != null ? v : fallback;
    }
}