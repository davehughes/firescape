package org.firescape.server.util;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

import org.firescape.server.util.ISAAC;

public class ISAACTest {

    @Test
    public void testInstantiation() {
        ISAAC i = new ISAAC(new int[]{1,2,3});
        i.getNextValue();
    }
}
