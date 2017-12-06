package org.firescape.server.net;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.firescape.server.net.RSCPacket;

public class RSCPacketTest {

    @Test
    public void testInstantiation() {
        int packetID = 123;
        RSCPacket packet = new RSCPacket(null, packetID, new byte[]{});
        assertEquals(packetID, packet.getID());
    }
}
