package tesseract.graph.traverse;

import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;
import it.unimi.dsi.fastutil.longs.LongSet;
import tesseract.graph.INode;
import tesseract.util.Dir;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ConcurrentModificationException;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

/**
 * Breadth-first searcher implementation for determining connectivity within a graph, used for graph splitting upon node removal.
 * <p>
 * The BFSearcher implementation allows for reducing allocations by reusing the allocated HashSets, however, the Pos
 * allocations cannot currently be elided - there will be one allocation for each reached node.
 * <p>
 * Due to the above behavior, it is not safe to use BFSearcher from multiple threads. If two search operations happen
 * concurrently from different threads, then they will conflict, mixing results between each operation.
 */
public class BFSearcher {

    private final INode container;
    private final LongPriorityQueue open = new LongArrayFIFOQueue();
    private final LongOpenHashSet closed = new LongOpenHashSet();

    /**
     * Creates a reusable BFSearcher instance that will search the provided container.
     *
     * @param container The container to use for search operations.
     */
    public BFSearcher(@Nonnull INode container) {
        this.container = container;
    }

    /**
     * Begins a search operation from the specified start position. The searcher will report each new reached position
     * to the provided consumer. As a result of the algorithm, each reported position is guaranteed to be connected to
     * an existing position, or in the case of the first reported position, it will be identical to from.
     *
     * @param from The start position of the search operation. This will be the first position reported to the consumer.
     * @param reached The receiver of the discovered positions
     * @param excluder A function that can add values to the closed set prior to the search operation.
     *                 They will not be reported or traversed; null is interpreted to mean no exclusions.
     */
    public void search(long from, @Nonnull LongConsumer reached, @Nullable Consumer<LongSet> excluder) {
        if (!closed.isEmpty() || !open.isEmpty()) {
            throw new ConcurrentModificationException("Attempted to run concurrent search operations on the same BFSearcher instance");
        }

        // Exclude all of the provided positions
        if (excluder != null) {
            excluder.accept(closed);
        }

        try {
            if (!container.contains(from)) {
                return;
            }

            open.enqueue(from);

            while (!open.isEmpty()) {
                // Pick a position
                long current = open.dequeueLong();

                if (closed.contains(current)) {
                    // I don't think this should happen, but it works as a sanity check.
                    // This can happen if the starting point was excluded.
                    continue;
                }

                // Process the position
                closed.add(current);
                reached.accept(current);

                Pos position = new Pos(current);
                // Discover new nodes
                for (Dir direction : Dir.VALUES) {
                    long pos = position.offset(direction).asLong();

                    if (closed.contains(pos)) {
                        // Already seen, prevent infinite loops.
                        continue;
                    }

                    if (container.linked(current, direction, pos)) {
                        // Note: this allocates a new position
                        open.enqueue(pos);
                    }
                }
            }
        } finally {
            // Clean up the open/closed sets
            open.clear();
            closed.clear();
        }
    }
}
