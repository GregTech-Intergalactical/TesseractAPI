package tesseract.graph;

import it.unimi.dsi.fastutil.longs.Long2ByteMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.util.Node;

import java.util.ArrayDeque;

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
	 * @return Returns nodes map.
	 */
	Long2ByteMap getNodes();

	/**
	 * Lazily generates full paths from the linked node to another linked nodes.
	 * @param pos The position of the linked node.
	 * @return Returns full paths for the linked node.
	 */
	ObjectList<ArrayDeque<Node>> getPath(long pos);

	/**
	 * Begins a find operation from the specified start position to the end position.
	 * @param start The start position of the finds operation.
	 * @param end The end position of the finds operation.
	 * @return An set of path points.
	 */
	ArrayDeque<Node> findPath(long start, long end);
}
