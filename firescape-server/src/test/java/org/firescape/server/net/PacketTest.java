package org.firescape.server.net;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.junit.Assert;

import org.firescape.server.net.Packet;

public class PacketTest {

    @Test
    public void testConstructionAndReading() {
        Packet p = new Packet(null, "hello world".getBytes());
        Assert.assertEquals(false, p.isBare());
        Assert.assertEquals(11, p.getLength());
        Assert.assertArrayEquals("hello world".getBytes(), p.getData());
        Assert.assertArrayEquals("hello".getBytes(), p.readBytes(5));
        Assert.assertArrayEquals(" world".getBytes(), p.readBytes(6));
        Assert.assertArrayEquals("".getBytes(), p.readBytes(10));
    }

    @Test
    public void testGetRemainingData() {
        Packet p;

        p = new Packet(null, "hello world".getBytes());
        Assert.assertArrayEquals("hello world".getBytes(), p.getRemainingData());

        p = new Packet(null, "hello world".getBytes());
        Assert.assertArrayEquals("hello ".getBytes(), p.readBytes(6));
        Assert.assertArrayEquals("world".getBytes(), p.getRemainingData());
    }
}
