package zap.graph.traverse;

import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;

public interface INodeContainer {
	boolean contains(BlockPos pos);

	default boolean connects(BlockPos pos, EnumFacing towards) {
		return true;
	}
}
