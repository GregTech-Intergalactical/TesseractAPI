package zap.graph;

import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
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
	private Object2ByteOpenHashMap<BlockPos> linkedNodes;
	private BFDivider divider;

	private Grid() {
		connectors = new HashMap<>();
		linkedNodes = new Object2ByteOpenHashMap<>();
		linkedNodes.defaultReturnValue(Byte.MAX_VALUE);
		divider = new BFDivider(this);
	}

	public static <C extends IConnectable> Grid<C> singleConnector(BlockPos pos, Connectivity.Cache<C> connector) {
		Grid<C> grid = new Grid<>();

		grid.connectors.put(Objects.requireNonNull(pos), Objects.requireNonNull(connector));

		return grid;
	}

	@Override
	public boolean contains(BlockPos pos) {
		return connectors.containsKey(pos) || linkedNodes.containsKey(pos);
	}

	@Override
	public boolean linked(BlockPos from, Direction towards, BlockPos to) {
		Connectivity.Cache<C> cacheFrom = connectors.get(from);
		Connectivity.Cache<C> cacheTo = connectors.get(to);

		byte connectivityFrom = linkedNodes.getByte(from);
		byte connectivityTo = linkedNodes.getByte(to);

		boolean validLink = false;

		if(cacheFrom != null) {
			validLink = true;
			connectivityFrom = cacheFrom.connectivity;
		}

		if(cacheTo != null) {
			validLink = true;
			connectivityTo = cacheTo.connectivity;
		}

		if(connectivityFrom == Byte.MAX_VALUE || connectivityTo == Byte.MAX_VALUE) {
			return false;
		}

		return validLink && Connectivity.has(connectivityFrom, towards) && Connectivity.has(connectivityTo, towards.getOpposite());
	}

	@Override
	public boolean connects(BlockPos position, Direction towards) {
		Connectivity.Cache<C> cache = connectors.get(position);
		byte connectivity = linkedNodes.getByte(position);

		if(cache != null) {
			connectivity = cache.connectivity;
		}

		if(connectivity == Byte.MAX_VALUE) {
			return false;
		}

		return Connectivity.has(connectivity, towards);
	}

	// TODO: Count / visit linked nodes

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
		linkedNodes.putAll(other.linkedNodes);
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

	public void addLinkedNode(BlockPos pos, byte connectivity) {
		linkedNodes.put(Objects.requireNonNull(pos), connectivity);
	}

	public C remove(BlockPos pos, Consumer<Grid<C>> split) {
		Objects.requireNonNull(split);

		if(!contains(pos)) {
			throw new IllegalArgumentException("Tried to call Grid::remove with a position that does not exist within the grid.");
		}

		if(isExternal(pos)) {
			return removeFinal(pos);
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

		// TODO: Properly split / remove relevant linkedNodes, verify that this works.
		HashSet<BlockPos> check = new HashSet<>();

		for(int i = 0; i < colored.size(); i++) {
			if (i == bestColor) {
				// These nodes will be kept.
				continue;
			}

			Grid<C> newGrid = new Grid<>();
			HashSet<BlockPos> found = colored.get(i);

			for (BlockPos reached : found) {
				byte connectivity = linkedNodes.getByte(reached);

				if(connectivity != Byte.MAX_VALUE) {
					check.add(reached);
					newGrid.linkedNodes.put(reached, connectivity);
				} else {
					newGrid.connectors.put(reached, this.connectors.remove(reached));
				}
			}

			split.accept(newGrid);
		}

		C connector = removeFinal(pos);

		for(BlockPos toCheck: check) {
			if(isExternal(toCheck)) {
				linkedNodes.removeByte(toCheck);
			}
		}

		return connector;
	}

	private C removeFinal(BlockPos pos) {
		C connector = this.connectors.remove(pos).value();

		for(Direction direction: DIRECTIONS) {
			BlockPos face = pos.offset(direction);

			if(linkedNodes.containsKey(face) && isExternal(face)) {
				linkedNodes.removeByte(face);
			}
		}

		return connector;
	}

	/**
	 * Tests if a particular position is only connected to the grid on a single side, or is the only entry in the grid.
	 * @param pos The position to test
	 * @return Whether the position only has a single neighbor in the group, or is the only entry in the group.
	 */
	private boolean isExternal(BlockPos pos) {
		// If the grid contains less than 2 blocks, neighbors cannot exist.
		if(this.connectors.size() <= 1) {
			return true;
		}

		int neighbors = 0;
		for(Direction direction: DIRECTIONS) {
			BlockPos face = pos.offset(direction);

			if(!linkedNodes.containsKey(face) && this.linked(pos, direction, face)) {
				neighbors += 1;
			}
		}

		return neighbors <= 1;
	}
}