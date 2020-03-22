package tesseract.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongList;

/**
 * A simple interface for representing objects that contain sets of positions that are may or may not be linked.
 */
public interface IGroup<C extends IConnectable, N extends IConnectable> {

	/**
	 * @return Gets the number of blocks.
	 */
	int countBlocks();

	/**
	 * @return Returns blocks set.
	 */
	LongList getBlocks();

	/**
	 * @return Returns nodes map.
	 */
	Long2ObjectMap<Connectivity.Cache<N>> getNodes();

	/**
	 * @return Returns grids set.
	 */
	Int2ObjectMap<Grid<C>> getGrids();
}
