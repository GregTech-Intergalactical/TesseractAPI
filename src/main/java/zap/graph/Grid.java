package zap.graph;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;
import zap.graph.traverse.BFDivider;
import zap.graph.traverse.INodeContainer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

// default: parameters are nonnull, methods return nonnull
public class Grid<C extends IConnectable> implements INodeContainer {
	// To prevent excessive array reallocation
	private static Direction[] DIRECTIONS = Direction.values();

	HashMap<BlockPos, Connectivity.Cache<C>> connectors;
	private BFDivider divider;

	private Grid() {
		connectors = new HashMap<>();
		divider = new BFDivider(this);
	}

	public static <C extends IConnectable> Grid<C> singleConnector(BlockPos pos, C connector) {
		Grid<C> grid = new Grid<>();

		grid.connectors.put(pos, Connectivity.Cache.of(connector));

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
	public boolean wouldLink(BlockPos from, Direction towards, BlockPos to) {
		Connectivity.Cache<C> cacheTo = connectors.get(to);

		if(cacheTo == null) {
			return false;
		}

		return cacheTo.connects(towards.getOpposite());
	}
	
	public void addConnector(BlockPos pos, Connectivity.Cache<C> connector) {
		connectors.put(pos, connector);
	}

	public C remove(BlockPos pos, Consumer<Grid<C>> split) {
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