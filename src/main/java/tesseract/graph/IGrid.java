package tesseract.graph;

import tesseract.util.Pos;

import java.util.HashMap;
import java.util.LinkedHashSet;

/**
 * A simple interface for representing objects that contain sets of positions that are linked to each other.
 */
public interface IGrid<C extends IConnectable> {

	/**
	 * @return Gets the number of connectors.
	 */
	int countConnectors();

	/**
	 * @return Returns connectors.
	 */
	HashMap<Pos, C> getConnectors();

	/**
	 * Begins a find operation from the specified start position to the end position.
	 * @param start The start position of the finds operation.
	 * @param end The end position of the finds operation.
	 * @param crossroad If true will generate path only with crossroad nodes, false for all nodes.
	 * @return An set of path points.
	 */
	LinkedHashSet<Pos> getPath(Pos start, Pos end, boolean crossroad);
}
