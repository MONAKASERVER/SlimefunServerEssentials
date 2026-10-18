package me.justahuman.slimefun_server_essentials.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.GZIPOutputStream;

/** Versioned, bounded transport envelope used on top of Bukkit plugin messaging. */
public final class TransferEnvelope {
    public static final int MAGIC = 0x4D534532; // MSE2
    public static final short PROTOCOL_VERSION = 2;
    public static final byte FLAG_GZIP = 1;
    public static final int MAX_PACKET_SIZE = 32_766;
    public static final int HASH_SIZE = 32;
    public static final int HEADER_SIZE = Integer.BYTES + Short.BYTES + Byte.BYTES
            + Integer.BYTES * 5 + HASH_SIZE;
    public static final int CHUNK_DATA_SIZE = MAX_PACKET_SIZE - HEADER_SIZE;

    private static final AtomicInteger NEXT_MESSAGE_ID = new AtomicInteger();

    private TransferEnvelope() {}

    public static EncodedMessage encode(byte[] raw, boolean gzip, int maximumRawBytes) {
        if (raw.length > maximumRawBytes) {
            throw new IllegalArgumentException("Payload exceeds configured maximum: " + raw.length);
        }

        byte flags = 0;
        byte[] encoded = raw;
        if (gzip && raw.length >= 1_024) {
            byte[] compressed = gzip(raw);
            if (compressed.length < raw.length) {
                encoded = compressed;
                flags = FLAG_GZIP;
            }
        }

        int messageId = NEXT_MESSAGE_ID.getAndIncrement();
        int chunks = Math.max(1, (encoded.length + CHUNK_DATA_SIZE - 1) / CHUNK_DATA_SIZE);
        byte[] hash = sha256(raw);
        List<byte[]> packets = new ArrayList<>(chunks);
        for (int index = 0; index < chunks; index++) {
            int start = index * CHUNK_DATA_SIZE;
            int length = Math.min(CHUNK_DATA_SIZE, encoded.length - start);
            ByteBuffer packet = ByteBuffer.allocate(HEADER_SIZE + length);
            packet.putInt(MAGIC);
            packet.putShort(PROTOCOL_VERSION);
            packet.put(flags);
            packet.putInt(messageId);
            packet.putInt(index);
            packet.putInt(chunks);
            packet.putInt(raw.length);
            packet.putInt(encoded.length);
            packet.put(hash);
            packet.put(encoded, start, length);
            packets.add(packet.array());
        }
        return new EncodedMessage(List.copyOf(packets), raw.length, encoded.length, HexFormat.of().formatHex(hash), flags);
    }

    public static byte[] sha256(byte[] data) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(data);
        } catch (NoSuchAlgorithmException impossible) {
            throw new IllegalStateException("SHA-256 is unavailable", impossible);
        }
    }

    private static byte[] gzip(byte[] data) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             GZIPOutputStream gzip = new GZIPOutputStream(output)) {
            gzip.write(data);
            gzip.finish();
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Could not compress sync payload", exception);
        }
    }

    public record EncodedMessage(List<byte[]> packets, int rawBytes, int encodedBytes, String sha256, byte flags) {}
}
