package zap.graph.traverse;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Iterator;
import java.util.function.Consumer;

public class BFSearcher {
	private HashSet<BlockPos> closed;
	private HashSet<BlockPos> open;
	private BlockPos.MutableBlockPos searchPos;
	private INodeContainer container;

	public BFSearcher(INodeContainer container) {
		closed = new HashSet<>();
		open = new HashSet<>();
		searchPos = new BlockPos.MutableBlockPos();
		this.container = container;
	}

	public void search(BlockPos from, Consumer<BlockPos> reached) {
		if(!container.contains(from)) {
			return;
		}

		open.add(from);

		while(!open.isEmpty()) {
			// Pick a position
			Iterator<BlockPos> iterator = open.iterator();
			BlockPos current = iterator.next();
			iterator.remove();

			// Process the position
			closed.add(current);
			reached.accept(current);

			// Discover new nodes
			for(EnumFacing facing: EnumFacing.VALUES) {
				searchPos.setPos(current);
				searchPos.move(facing);

				if(container.contains(searchPos)) {
					open.add(searchPos.toImmutable());
				}
			}
		}

		closed.clear();
		open.clear();
	}
}
