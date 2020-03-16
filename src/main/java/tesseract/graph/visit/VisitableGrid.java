package tesseract.graph.visit;

import tesseract.util.Pos;
import tesseract.graph.IConnectable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface VisitableGrid<C extends IConnectable> {

	int countConnectors();

	void visitConnectors(BiConsumer<Pos, C> visitor);

	void findPath(Pos start, Pos end, Consumer<Pos> collector);
}
