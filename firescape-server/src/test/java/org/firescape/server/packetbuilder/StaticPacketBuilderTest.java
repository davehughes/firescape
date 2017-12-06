package org.firescape.server.packetbuilder;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.firescape.server.net.Packet;
import org.firescape.server.packetbuilder.StaticPacketBuilder;

public class StaticPacketBuilderTest {

    @Test
    public void test() {
        StaticPacketBuilder builder = new StaticPacketBuilder();
        Packet packet = builder.toPacket();
    }
}
