/*
 * Copyright 2016-2022 chronicle.software
 *
 *       https://chronicle.software
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.openhft.chronicle.wire;

import net.openhft.chronicle.bytes.Bytes;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class OutOfOrderTest extends WireTestCommon {
    static final String start = "{ \"a\": 1, ";
    static final String records = "\"records\":[{\"id\":1}], ";
    static final String missing = "\"missing\": 111, ";
    static final String end = "\"z\": 99 }";

    @Test
    public void outOfOrder() {
        doTest(start + end, "{\"a\":1,\"b\":null,\"records\":null,\"z\":99}");
        doTest(start + missing + records + end, "{\"a\":1,\"b\":null,\"records\":[ {\"id\":1} ],\"z\":99}");
    }

    void doTest(String input, String expected) {
        Bytes<?> from = Bytes.from(input);
        JSONWire wire = new JSONWire(from);
        OOOT ooot = wire.getValueIn()
                .object(OOOT.class);
        from.releaseLast();

        JSONWire wire2 = new JSONWire(Bytes.allocateElasticOnHeap(64));
        wire2.getValueOut().object(ooot);
        assertEquals(expected, wire2.toString());
    }

    static class OOOT extends SelfDescribingMarshallable {
        int a;
        String b;
        List<OOOT2> records;
        int z;
    }

    static class OOOT2 extends SelfDescribingMarshallable {
        int id;
    }
}
