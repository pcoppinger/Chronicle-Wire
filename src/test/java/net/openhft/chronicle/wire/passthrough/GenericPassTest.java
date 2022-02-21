package net.openhft.chronicle.wire.passthrough;

import net.openhft.chronicle.bytes.Bytes;
import net.openhft.chronicle.bytes.MethodReader;
import net.openhft.chronicle.wire.DocumentContext;
import net.openhft.chronicle.wire.TextWire;
import net.openhft.chronicle.wire.Wire;
import net.openhft.chronicle.wire.WireType;
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
        final DocumentContextBroker microService = new DocumentContextBroker() {
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
        };
        final MethodReader reader = wire1.methodReader(microService);
        assertTrue(reader.readOne());
        assertFalse(reader.readOne());
        assertEquals("" +
                        "via: queue1\n" +
                        "say: hello\n" +
                        "...\n",
                wire2.toString());
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
}
