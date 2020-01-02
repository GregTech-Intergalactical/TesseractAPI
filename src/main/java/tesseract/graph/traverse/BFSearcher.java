package tesseract.graph.traverse;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.Direction;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Breadth-first searcher implementation for determining connectivity within a graph, used for graph splitting upon node removal.
 *
 * The BFSearcher implementation allows for reducing allocations by reusing the allocated HashSets, however, the BlockPos
 * allocations cannot currently be elided - there will be one allocation for each reached node.
 *
 * Due to the above behavior, it is not safe to use BFSearcher from multiple threads. If two search operations happen
 * concurrently from different threads, then they will conflict, mixing results between each operation.
 */
public class BFSearcher {
	// To prevent excessive array reallocation
	private static Direction[] DIRECTIONS = Direction.values();

	private HashSet<BlockPos> closed;
	private ArrayDeque<BlockPos> open;
	private BlockPos.MutableBlockPos searchPos;
	private INodeContainer container;

	/**
	 * Creates a reusable BFSearcher instance that will search the provided container.
	 *
	 * @param container The container to use for search operations
	 */
	public BFSearcher(INodeContainer container) {
		closed = new HashSet<>();
		open = new ArrayDeque<>();
		searchPos = new BlockPos.MutableBlockPos();
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
	public void search(BlockPos from, Consumer<BlockPos> reached, Consumer<Collection<BlockPos>> excluder) {
		if(!closed.isEmpty() || !open.isEmpty()) {
			throw new ConcurrentModificationException("Attempted to run concurrent search operations on the same BFSearcher instance");
		}

		// Exclude all of the provided positions
		if(excluder != null) {
			excluder.accept(closed);
		}

		try {
			if(!container.contains(from)) {
				return;
			}

			open.add(from);

			while(!open.isEmpty()) {
				// Pick a position
				BlockPos current = open.remove();

				if (closed.contains(current)) {
					// I don't think this should happen, but it works as a sanity check.
					// This can happen if the starting point was excluded.
					continue;
				}

				// Process the position
				closed.add(current);
				reached.accept(current);

				// Discover new nodes
				for(Direction direction: DIRECTIONS) {
					searchPos.setPos(current);
					searchPos.offset(direction);

					if(closed.contains(searchPos)) {
						// Already seen, prevent infinite loops.
						continue;
					}

					if(container.linked(current, direction, searchPos)) {
						// Note: this allocates a new position
						open.add(searchPos.toImmutable());
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
