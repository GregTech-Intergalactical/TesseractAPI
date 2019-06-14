package zap.graph.traverse;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class AStarPathfinder {
	// To prevent excessive array reallocation
	private static Direction[] DIRECTIONS = Direction.values();

	private INodeContainer container;

	private HashSet<BlockPos> open;
	private HashSet<BlockPos> closed;
	private HashMap<BlockPos, BlockPos> cameFrom;
	private Object2IntMap<BlockPos> gScore;
	private Object2IntMap<BlockPos> fScore;

	public AStarPathfinder(INodeContainer container) {
		this.container = container;

		open = new HashSet<>();
		closed = new HashSet<>();
		cameFrom = new HashMap<>();
		gScore = new Object2IntOpenHashMap<>();
		fScore = new Object2IntOpenHashMap<>();

		gScore.defaultReturnValue(Integer.MAX_VALUE);
		fScore.defaultReturnValue(Integer.MAX_VALUE);
	}

	// TODO: Verify that this works
	public void findPath(BlockPos start, BlockPos end, Consumer<BlockPos> fromEnd) {
		BlockPos.Mutable current = new BlockPos.Mutable();
		BlockPos.Mutable neighbor = new BlockPos.Mutable();
		current.set(start);

		open.add(start);
		fScore.put(start, heuristic(start, end));

		while(!current.equals(end)) {
			open.remove(current);
			closed.add(current);

			int currentGScore = gScore.getInt(current);

			for(Direction direction: DIRECTIONS) {
				neighbor.set(current).setOffset(direction);

				if(closed.contains(neighbor) || !container.linked(current, direction, neighbor)) {
					continue;
				}

				int neighborGScore = currentGScore + 1;

				if(!open.contains(current)) {
					// note: this allocates
					open.add(current.toImmutable());
				} else {
					int existingGScore = gScore.getInt(current);

					if(neighborGScore >= existingGScore) {
						continue;
					}
				}

				// note: this allocates
				BlockPos neighborImmutable = neighbor.toImmutable();

				cameFrom.put(neighborImmutable, current.toImmutable());
				gScore.put(neighborImmutable, neighborGScore);
				fScore.put(neighborImmutable, neighborGScore + heuristic(neighborImmutable, end));
			}

			current.set(findBestOpenNode());
		}


		open.clear();
		closed.clear();
		gScore.clear();
		fScore.clear();

		fromEnd.accept(end);

		while(!current.equals(start)) {
			BlockPos node = cameFrom.remove(current);
			current.set(node);

			fromEnd.accept(node);
		}

		cameFrom.clear();
	}

	private BlockPos findBestOpenNode() {
		BlockPos best = null;
		int bestScore = Integer.MAX_VALUE;

		for(BlockPos pos: open) {
			int score = fScore.getInt(pos);

			if(score < bestScore) {
				bestScore = score;
				best = pos;
			}
		}

		return best;
	}

	private static int heuristic(BlockPos current, BlockPos end) {
		return Math.abs(current.getX()-end.getX()) + Math.abs(current.getY()-end.getY()) + Math.abs(current.getZ()-end.getZ());
	}
}