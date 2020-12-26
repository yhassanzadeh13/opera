package SkipGraph.skipnode;

import java.io.Serializable;

/**
 * Represents the result of a search containing the piggybacked information.
 */
public class SearchResult implements Serializable {
    public final SkipNodeIdentity result;

    public SearchResult(SkipNodeIdentity result) {
        this.result = result;
    }
}
