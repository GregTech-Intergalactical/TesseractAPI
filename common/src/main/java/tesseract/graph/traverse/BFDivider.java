package tesseract.graph.traverse;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import tesseract.graph.INode;

import java.util.ConcurrentModificationException;
import java.util.function.Consumer;

/**
 * Extension of BFSearcher that helps with dividing up node containers. The BFDivider class executes enough breadth first
 * search operations in order to split up a previously connected set of nodes into divided groups of connected nodes.
 */
public class BFDivider {

    private final BFSearcher searcher;
    private final Long2IntOpenHashMap roots = new Long2IntOpenHashMap();
    private final LongLinkedOpenHashSet lookup = new LongLinkedOpenHashSet();

    /**
     * Creates a reusable BFDivider instance that will devides the provided container.
     *
     * @param container The container to use for devides operations.
     */
    public BFDivider(INode container) {
        searcher = new BFSearcher(container);
        roots.defaultReturnValue(Integer.MAX_VALUE);
    }

    /**
     * Executes the divide operation with the given parameters.
     *
     * @param removed      This function is called once, allowing the caller to provide a list of removed positions. When executing
     *                     breadth first search operations, these positions will not be traversed, making it possible to truly
     *                     remove them from the node contained being searched after divide is complete.
     * @param rootProvider Like the previous parameter, this allows the caller to provide a list of positions. However,
     *                     these positions will be used as positions to initiate the search operations from - usually,
     *                     they will be the neighbors of all items in the removed set.
     * @param split        An acceptor of the sets of divided positions. Each set contains a set of positions determined to be
     *                     connected by the node container.
     * @return The index in the sequence of split position sets corresponding to the largest set of positions, ie. a
     * return value of 0 indicates that the first returned set was the largest.
     */
    public int divide(Consumer<LongSet> removed, Consumer<LongSet> rootProvider, Consumer<LongSet> split) {
        if (!lookup.isEmpty() || !roots.isEmpty()) {
            throw new ConcurrentModificationException("Attempted to run concurrent divide operations on the same BFDivider instance");
        }

        rootProvider.accept(lookup);

        int bestCount = 0;
        int bestColor = 0;
        int currentColor = 0;

        try {
            for (long root : lookup) {
                // Check if this root has already been colored.
                int existingColor = roots.get(root);

                if (existingColor != roots.defaultReturnValue()) {
                    // Already colored! No point in doing it again.
                    continue;
                }

                final int color = currentColor++;
                roots.put(root, color);

                LongSet found = new LongLinkedOpenHashSet();

                searcher.search(root, reached -> {
                    if (lookup.contains(reached)) {
                        roots.put(reached, color);
                    }

                    found.add(reached);
                }, removed);

                if (found.size() > bestCount) {
                    bestCount = found.size();
                    bestColor = color;
                }

                split.accept(found);
            }
        } finally {
            // Clean up the open/closed sets
            lookup.clear();
            roots.clear();
        }

        return bestColor;
    }
}
