package zap.graph.traverse;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.math.BlockPos;

import java.util.Collection;
import java.util.HashSet;
import java.util.function.Consumer;

/**
 * Extension of BFSearcher that helps with dividing up node containers. The BFDivider class executes enough breadth first
 * search operations in order to split up a previously connected set of nodes into divided groups of connected nodes.
 */
public class BFDivider {
	private BFSearcher searcher;
	private HashSet<BlockPos> toSearch;

	public BFDivider(INodeContainer container) {
		searcher = new BFSearcher(container);
		toSearch = new HashSet<>();
	}

	/**
	 * Executes the divide operation with the given parameters.
	 *
	 * @param removed This function is called once, allowing the caller to provide a list of removed positions. When executing
	 *                breadth first search operations, these positions will not be traversed, making it possible to truly
	 *                remove them from the node contained being searched after divide is complete.
	 * @param rootProvider Like the previous parameter, this allows the caller to provide a list of positions. However,
	 *                     these positions will be used as positions to initiate the search operations from - usually,
	 *                     they will be the neighbors of all items in the removed set.
	 * @param split An acceptor of the sets of divided positions. Each set contains a set of positions determined to be
	 *              connected by the node container.
	 * @return The index in the sequence of split position sets corresponding to the largest set of positions, ie. a
	 *         return value of 0 indicates that the first returned set was the largest.
	 */
	public int divide(Consumer<Collection<BlockPos>> removed, Consumer<Collection<BlockPos>> rootProvider, Consumer<HashSet<BlockPos>> split) {
		rootProvider.accept(toSearch);

		TObjectIntHashMap<BlockPos> roots = new TObjectIntHashMap<>(6, 0.5F, Integer.MAX_VALUE);

		int bestCount = 0;
		int bestColor = 0;

		int currentColor = 0;

		for(BlockPos root: toSearch) {
			// Check if this root has already been colored.
			int existingColor = roots.get(root);

			if(existingColor != roots.getNoEntryValue()) {
				// Already colored! No point in doing it again.
				continue;
			}

			final int color = currentColor++;
			roots.put(root, color);

			HashSet<BlockPos> found = new HashSet<>();

			searcher.search(root, reached -> {
				if(toSearch.contains(reached)) {
					roots.put(reached, color);
				}

				found.add(reached);
			}, removed);

			if(found.size() > bestCount) {
				bestCount = found.size();
				bestColor = color;
			}

			split.accept(found);
		}

		toSearch.clear();

		return bestColor;
	}
}
