package dev.runtime;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class Channel {

    private final DataOutputStream out;

    public Channel(OutputStream raw) {
        this.out = new DataOutputStream(new BufferedOutputStream(raw));
    }

    public synchronized void writeLine(String text) {
        try {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.write('\n');
            out.flush();
        } catch (IOException e) {
            fail(e);
        }
    }

    public synchronized void writeFrame(byte[] payload) {
        try {
            out.writeInt(payload.length);   // writeInt is defined big-endian
            out.write(payload);
            out.flush();
        } catch (IOException e) {
            fail(e);
        }
    }

    public synchronized void writeErrorFrame() {
        try {
            out.writeInt(0);
            out.flush();
        } catch (IOException e) {
            fail(e);
        }
    }

    private static void fail(IOException e) {
        System.err.println("[DCEMI] channel write failed: " + e.getMessage());
    }
}