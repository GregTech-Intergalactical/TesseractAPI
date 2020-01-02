package tesseract.graph.visit;

import net.minecraft.util.math.BlockPos;
import tesseract.graph.IConnectable;

import java.util.function.BiConsumer;

public interface VisitableGrid<C extends IConnectable> {
	int countConnectors();
	void visitConnectors(BiConsumer<BlockPos, C> visitor);
}
