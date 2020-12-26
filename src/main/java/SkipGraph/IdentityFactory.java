package SkipGraph;

import SkipGraph.skipnode.SkipNodeIdentity;

public class IdentityFactory {

    private static int lastNumID = 0;

    public static synchronized SkipNodeIdentity newIdentity(int identityLength) {
        // Construct a name ID with the size identityLength
        StringBuilder nameId = new StringBuilder(Integer.toBinaryString(lastNumID));
        while(nameId.length() < identityLength) nameId.insert(0, '0');
        return new SkipNodeIdentity(nameId.toString(), lastNumID++, "0", 0);
    }
}
