package tesseract.graph;

import tesseract.util.Pos;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * A simple interface for representing objects that contain sets of positions that are may or may not be linked.
 */
public interface IGroup<C extends IConnectable, N extends IConnectable> {

	/**
	 * @return Gets the number of blocks.
	 */
	int countBlocks();

	/**
	 * @return Returns blocks.
	 */
	LinkedHashSet<Pos> getBlocks();

	/**
	 * @return Returns nodes.
	 */
	HashMap<Pos, N> getNodes();

	/**
	 * @return Returns grids.
	 */
	LinkedHashSet<IGrid<C>> getGrids();
}
