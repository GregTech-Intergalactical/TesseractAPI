package zap.graph.traverse;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.BlockPos;

/**
 * A simple interface for representing objects that contain groups of positions that are connected in various ways.
 * Used by traversal classes in order to determine how they may move from one element to the next.
 */
public interface INodeContainer {
	/**
	 * Tests whether this container contains the specified position.
	 *
	 * @param pos The position that the container may potentially contain
	 * @return Whether the container contains the specified position
	 */
	boolean contains(BlockPos pos);

	/**
	 * Tests whether adjacent positions are linked.
	 * @param from The starting position
	 * @param towards The face on the starting position
	 * @param to The target position, must be equal to <code>from.offset(towards)</code>
	 * @return Whether the positions are linked. If a position is not contained within this container, returns false.
	 */
	boolean linked(BlockPos from, Direction towards, BlockPos to);

	/**
	 * Tests whether the from position, if it were present in the container, would link to the target position.
	 * @param from The starting position, which can potentially not exist in the container.
	 * @param towards The face on the starting position
	 * @param to The target position, must be equal to <code>from.offset(towards)</code>
	 * @return Whether the positions are linked. If a position is not contained within this container, returns false.
	 */
	default boolean wouldLink(BlockPos from, Direction towards, BlockPos to) {
		return contains(to);
	}
}
