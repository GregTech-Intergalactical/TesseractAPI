package zap.graph.traverse;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;

public class AStarPathfinder {
	// To prevent excessive array reallocation
	private static Direction[] DIRECTIONS = Direction.values();

	INodeContainer container;

	BlockPos start;
	BlockPos end;

	HashSet<BlockPos> open;
	HashSet<BlockPos> closed;
	HashMap<BlockPos, BlockPos> cameFrom;
	Object2IntMap<BlockPos> gScore;
	Object2IntMap<BlockPos> fScore;

	public AStarPathfinder(INodeContainer container, BlockPos start, BlockPos end) {
		this.start = start;
		this.end = end;
		this.container = container;

		open = new HashSet<>();
		closed = new HashSet<>();
		cameFrom = new HashMap<>();
		gScore = new Object2IntOpenHashMap<>();
		fScore = new Object2IntOpenHashMap<>();

		gScore.defaultReturnValue(Integer.MAX_VALUE);
		fScore.defaultReturnValue(Integer.MAX_VALUE);

		open.add(start);
		fScore.put(start, heuristic(start));
	}

	public void findPath() {
		BlockPos.Mutable current = new BlockPos.Mutable();
		current.set(start);

		while(!current.equals(end)) {
			open.remove(current);
			closed.add(current);

			for(Direction direction: DIRECTIONS) {
				current.setOffset(direction);

				if(closed.contains(current)) {
					current.setOffset(direction.getOpposite());
					continue;
				}

				if(container.contains(current)) {
					// TODO
				}

				current.setOffset(direction.getOpposite());
			}

			current.set(findBestOpenNode());
		}

		// TODO
	}

	public BlockPos findBestOpenNode() {
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

	private int heuristic(BlockPos current) {
		return Math.abs(current.getX()-end.getX()) + Math.abs(current.getY()-end.getY()) + Math.abs(current.getZ()-end.getZ());
	}
}