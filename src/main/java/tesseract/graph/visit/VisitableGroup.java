package tesseract.graph.visit;

import tesseract.util.Pos;
import tesseract.graph.IConnectable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface VisitableGroup<C extends IConnectable, N extends IConnectable> {

	int countBlocks();

	void visitBlocks(Consumer<Pos> visitor);

	void visitNodes(BiConsumer<Pos, N> visitor);

	void visitGrids(Consumer<VisitableGrid<C>> visitor);
}
