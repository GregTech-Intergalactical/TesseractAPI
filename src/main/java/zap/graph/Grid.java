package zap.graph;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import zap.graph.traverse.BFDivider;
import zap.graph.traverse.INodeContainer;
import zap.graph.visit.VisitableGrid;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

// default: parameters are nonnull, methods return nonnull
public class Grid<C extends IConnectable> implements INodeContainer, VisitableGrid<C> {
	// To prevent excessive array reallocation
	private static Direction[] DIRECTIONS = Direction.values();

	private HashMap<BlockPos, Connectivity.Cache<C>> connectors;
	private BFDivider divider;

	private Grid() {
		connectors = new HashMap<>();
		divider = new BFDivider(this);
	}

	public static <C extends IConnectable> Grid<C> singleConnector(BlockPos pos, Connectivity.Cache<C> connector) {
		Grid<C> grid = new Grid<>();

		grid.connectors.put(Objects.requireNonNull(pos), Objects.requireNonNull(connector));

		return grid;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return connectors.containsKey(pos);
	}

	@Override
	public boolean linked(BlockPos from, Direction towards, BlockPos to) {
		Connectivity.Cache<C> cacheFrom = connectors.get(from);
		Connectivity.Cache<C> cacheTo = connectors.get(to);
		
		if(cacheFrom == null || cacheTo == null) {
			return false;
		}

		return cacheFrom.connects(towards) && cacheTo.connects(towards.getOpposite());
	}

	@Override
	public boolean connects(BlockPos position, Direction towards) {
		Connectivity.Cache<C> cache = connectors.get(position);

		if(cache == null) {
			return false;
		}

		return cache.connects(towards);
	}

	@Override
	public int countConnectors() {
		return connectors.size();
	}

	@Override
	public void visitConnectors(BiConsumer<BlockPos, C> visitor) {
		for(Map.Entry<BlockPos, Connectivity.Cache<C>> entry: connectors.entrySet()) {
			visitor.accept(entry.getKey(), entry.getValue().value());
		}
	}

	/**
	 * Merges all of the elements from the other provided grid into this grid.
	 * @param other The other grid to merge elements from
	 */
	void mergeWith(BlockPos at, Grid<C> other) {
		// TODO: Validate that the other grid touches the specified position.

		connectors.putAll(other.connectors);
	}

	/**
	 * Gets a potentially random position from the grid.
	 * @return A random position from the grid
	 */
	BlockPos sampleConnector() {
		return connectors.keySet().iterator().next();
	}

	public void addConnector(BlockPos pos, Connectivity.Cache<C> connector) {
		// TODO: Validate that the other grid touches the specified position.

		connectors.put(Objects.requireNonNull(pos), Objects.requireNonNull(connector));
	}

	public C remove(BlockPos pos, Consumer<Grid<C>> split) {
		Objects.requireNonNull(split);

		if(!contains(pos)) {
			throw new IllegalArgumentException("Tried to call Grid::remove with a position that does not exist within the grid.");
		}

		if(isExternal(pos)) {
			return this.connectors.remove(pos).value();
		}

		ArrayList<HashSet<BlockPos>> colored = new ArrayList<>();

		int bestColor = divider.divide(
				removed -> removed.add(pos),
				roots -> {
					for(Direction direction: DIRECTIONS) {
						BlockPos side = pos.offset(direction);

						if(this.linked(pos, direction, side)) {
							roots.add(side);
						}
					}
				},
				colored::add
		);

		for(int i = 0; i < colored.size(); i++) {
			if (i == bestColor) {
				// These nodes will be kept.
				continue;
			}

			Grid<C> newGrid = new Grid<>();
			HashSet<BlockPos> found = colored.get(i);

			for (BlockPos reached : found) {
				newGrid.connectors.put(reached, this.connectors.remove(reached));
			}

			split.accept(newGrid);
		}

		return this.connectors.remove(pos).value();
	}

	/**
	 * Tests if a particular position is only connected to the grid on a single side, or is the only entry in the grid.
	 * @param pos The position to test
	 * @return Whether the position only has a single neighbor in the group, or is the only entry in the group.
	 */
	private boolean isExternal(BlockPos pos) {
		// If the group contains less than 2 blocks, neighbors cannot exist.
		if(this.connectors.size() <= 1) {
			return true;
		}

		int neighbors = 0;
		for(Direction direction: DIRECTIONS) {
			BlockPos face = pos.offset(direction);

			if(this.linked(pos, direction, face)) {
				neighbors += 1;
			}
		}

		return neighbors <= 1;
	}
}