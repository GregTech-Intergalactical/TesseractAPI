package zap.graph.visit;

import net.minecraft.util.math.BlockPos;
import zap.graph.IConnectable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface VisitableGroup<C extends IConnectable, N extends IConnectable> {
	int countBlocks();
	void visitBlocks(Consumer<BlockPos> visitor);
	void visitNodes(BiConsumer<BlockPos, N> visitor);
	void visitGrids(Consumer<VisitableGrid<C>> visitor);
}
