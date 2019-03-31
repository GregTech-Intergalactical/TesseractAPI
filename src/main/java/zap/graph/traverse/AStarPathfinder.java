package zap.graph.traverse;

import gnu.trove.map.hash.TObjectIntHashMap;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;

public class AStarPathfinder {
	INodeContainer container;

	BlockPos start;
	BlockPos end;

	HashSet<BlockPos> open;
	HashSet<BlockPos> closed;
	HashMap<BlockPos, BlockPos> cameFrom;
	TObjectIntHashMap<BlockPos> gScore;
	TObjectIntHashMap<BlockPos> fScore;

	public AStarPathfinder(INodeContainer container, BlockPos start, BlockPos end) {
		this.start = start;
		this.end = end;

		open = new HashSet<>();
		closed = new HashSet<>();
		cameFrom = new HashMap<>();
		gScore = new TObjectIntHashMap<>();
		fScore = new TObjectIntHashMap<>();

		open.add(start);
		fScore.put(start, heuristic(start));
	}

	public void findPath() {
		BlockPos.MutableBlockPos current = new BlockPos.MutableBlockPos();
		current.setPos(start);

		while(!current.equals(end)) {
			open.remove(current);
			closed.add(current);

			for(EnumFacing facing: EnumFacing.VALUES) {
				current.move(facing);

				if(closed.contains(current)) {
					current.move(facing.getOpposite());
					continue;
				}

				if(container.contains(current)) {
					// TODO
				}

				current.move(facing.getOpposite());
			}

			current.setPos(findBestOpenNode());
		}

		// TODO
	}

	public BlockPos findBestOpenNode() {
		BlockPos best = null;
		int bestScore = Integer.MAX_VALUE;

		for(BlockPos pos: open) {
			int score = fScore.get(pos);

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