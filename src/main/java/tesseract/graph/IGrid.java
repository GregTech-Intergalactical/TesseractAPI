package tesseract.graph;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;

/**
 * A simple interface for representing objects that contain sets of positions that are linked to each other.
 */
public interface IGrid<C extends IConnectable> {

	/**
	 * @return Gets the number of connectors.
	 */
	int countConnectors();

	/**
	 * @return Gets the number of linked nodes.
	 */
	int countNodes();

	/**
	 * @return Returns connectors map.
	 */
	Long2ObjectMap<Connectivity.Cache<C>> getConnectors();

	/**
	 * @return Returns linked nodes map.
	 */
	Long2ByteMap getNodes();

	/**
	 * Begins a find operation from the specified start position to the end position.
	 * @param start The start position of the finds operation.
	 * @param end The end position of the finds operation.
	 * @param crossroad If true will generate path only with crossroad nodes, false for all nodes.
	 * @return An set of path points.
	 */
	LongSet getPath(long start, long end, boolean crossroad);
}
