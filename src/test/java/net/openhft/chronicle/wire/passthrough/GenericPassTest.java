package net.openhft.chronicle.wire.passthrough;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.HexDumpBytes;
import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.wire.*;
import org.junit.Test;

import static org.junit.Assert.*;

public class GenericPassTest {
    @Test
    public void sayingBroker() {
        Wire wire1 = WireType.TEXT.apply(Bytes.allocateElasticOnHeap());
        final SayingBroker sayingBroker = wire1.methodWriter(SayingBroker.class);
        sayingBroker.via("queue1").say("hello");

        assertEquals("" +
                        "via: queue1\n" +
                        "say: hello\n" +
                        "...\n",
                wire1.toString());

        Wire wire2 = new TextWire(Bytes.allocateElasticOnHeap()).useTextDocuments();
        final DocumentContextBroker microService = new MyDocumentContextBroker(wire2);
        final MethodReader reader = wire1.methodReader(microService);
        assertTrue(reader.readOne());
        assertFalse(reader.readOne());
        assertEquals("" +
                        "via: queue1\n" +
                        "say: hello\n" +
                        "...\n",
                wire2.toString());
    }

    @Test
    public void passingOpaqueMessage() {
        Bytes bytes0 = Bytes.allocateElasticOnHeap(1);
        // invalid in every wire
        bytes0.writeUnsignedByte(0x82);
        Wire wire0 = new TextWire(bytes0).useTextDocuments();
        try {
            wire0.getValueIn().object();
            fail();
        } catch (Exception ise) {
            // expected.
        }

        Wire wire1 = new TextWire(Bytes.allocateElasticOnHeap()).useTextDocuments();
        wire1.write("via").text("pass");
        wire1.bytes().writeUnsignedByte(0x82);

        Wire wire2 = new TextWire(Bytes.allocateElasticOnHeap()).useTextDocuments();
        final DocumentContextBroker microService = new MyDocumentContextBroker(wire2);
        final MethodReader reader = wire1.methodReader(microService);
        assertTrue(reader.readOne());
        assertFalse(reader.readOne());
        // the ... is added by the wire format.
        assertEquals("" +
                        "via: pass\n" +
                        "\u0082\n" +
                        "...\n",
                wire2.toString());
    }

    @Test
    public void passingOpaqueMessageBinary() {
        Bytes bytes0 = Bytes.allocateElasticOnHeap(1);
        // invalid in every wire
        bytes0.writeUnsignedByte(0x82);
        Wire wire0 = new BinaryWire(bytes0);
        try {
            wire0.getValueIn().object();
            fail();
        } catch (Exception ise) {
            // expected.
        }

        Wire wire1 = new BinaryWire(Bytes.allocateElasticOnHeap());
        try (DocumentContext dc = wire1.writingDocument()) {
            dc.wire().write("via").text("pass")
                    .bytes().writeUnsignedByte(0x82);
        }

        Wire wire2 = new BinaryWire(new HexDumpBytes());
        final DocumentContextBroker microService = new MyDocumentContextBroker(wire2);
        final MethodReader reader = wire1.methodReader(microService);
        assertTrue(reader.readOne());
        assertFalse(reader.readOne());
        // the ... is added by the wire format.
        assertEquals("" +
                        "0a 00 00 00                                     # msg-length\n" +
                        "c3 76 69 61                                     # via:\n" +
                        "e4 70 61 73 73                                  # pass\n" +
                        "82                                              # passed-through\n",
                wire2.bytes().toHexString());
    }

    interface Broker<T> {
        T via(String name);
    }

    interface Another<T> {
        T also(String name);
    }

    interface DocumentContextBroker extends Broker<DocumentContext>, Another<Saying> {

    }

    interface Saying {
        void say(String msg);
    }

    interface SayingBroker extends Broker<Saying> {

    }

    private static class MyDocumentContextBroker implements DocumentContextBroker {
        private final Wire wire2;

        public MyDocumentContextBroker(Wire wire2) {
            this.wire2 = wire2;
        }

        @Override
        public DocumentContext via(String name) {
            final DocumentContext dc = wire2.writingDocument();
            dc.wire().write("via").text(name);
            return dc;
        }

        @Override
        public Saying also(String name) {
            return null;
        }
    }
}
