package zap.graph.visit;

import net.minecraft.util.math.BlockPos;
import zap.graph.IConnectable;

import java.util.function.BiConsumer;

public interface VisitableGrid<C extends IConnectable> {
	int countConnectors();
	void visitConnectors(BiConsumer<BlockPos, C> visitor);
}
