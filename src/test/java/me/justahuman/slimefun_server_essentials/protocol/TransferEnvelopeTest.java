package me.justahuman.slimefun_server_essentials.protocol;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferEnvelopeTest {
    @Test
    void roundTripsCompressedMultiChunkPayload() throws Exception {
        byte[] raw = new byte[250_000];
        new Random(42).nextBytes(raw);
        TransferEnvelope.EncodedMessage message = TransferEnvelope.encode(raw, true, 1_000_000);

        assertTrue(message.packets().size() > 1);
        assertTrue(message.packets().stream().allMatch(packet -> packet.length <= TransferEnvelope.MAX_PACKET_SIZE));
        assertEquals(raw.length, message.rawBytes());

        ByteArrayOutputStream encoded = new ByteArrayOutputStream();
        byte flags = 0;
        byte[] expectedHash = null;
        for (int index = 0; index < message.packets().size(); index++) {
            ByteBuffer packet = ByteBuffer.wrap(message.packets().get(index));
            assertEquals(TransferEnvelope.MAGIC, packet.getInt());
            assertEquals(TransferEnvelope.PROTOCOL_VERSION, packet.getShort());
            flags = packet.get();
            packet.getInt();
            assertEquals(index, packet.getInt());
            assertEquals(message.packets().size(), packet.getInt());
            assertEquals(raw.length, packet.getInt());
            assertEquals(message.encodedBytes(), packet.getInt());
            byte[] hash = new byte[TransferEnvelope.HASH_SIZE];
            packet.get(hash);
            if (expectedHash == null) expectedHash = hash;
            else assertArrayEquals(expectedHash, hash);
            byte[] chunk = new byte[packet.remaining()];
            packet.get(chunk);
            encoded.writeBytes(chunk);
        }

        byte[] restored;
        if ((flags & TransferEnvelope.FLAG_GZIP) != 0) {
            try (GZIPInputStream gzip = new GZIPInputStream(new ByteArrayInputStream(encoded.toByteArray()))) {
                restored = gzip.readAllBytes();
            }
        } else {
            restored = encoded.toByteArray();
        }
        assertArrayEquals(raw, restored);
        assertTrue(MessageDigest.isEqual(TransferEnvelope.sha256(restored), expectedHash));
    }

    @Test
    void rejectsPayloadOverConfiguredMaximum() {
        byte[] raw = new byte[101];
        assertThrows(IllegalArgumentException.class, () -> TransferEnvelope.encode(raw, true, 100));
    }

    @Test
    void smallPayloadUsesExactPacketLengthWithoutPadding() {
        byte[] raw = "mona".getBytes();
        TransferEnvelope.EncodedMessage message = TransferEnvelope.encode(raw, true, 100);
        assertEquals(1, message.packets().size());
        assertEquals(TransferEnvelope.HEADER_SIZE + raw.length, message.packets().getFirst().length);
        assertArrayEquals(raw, Arrays.copyOfRange(message.packets().getFirst(), TransferEnvelope.HEADER_SIZE,
                message.packets().getFirst().length));
    }
}
