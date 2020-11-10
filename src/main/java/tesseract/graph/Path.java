package tesseract.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import tesseract.api.IConnectable;
import tesseract.util.Node;

import java.util.Deque;
import java.util.Iterator;

/**
 * The Path is a class that should work with paths for grids.
 */
public class Path<C extends IConnectable> {

    private final Node origin;
    private final Node target;
    private final Long2ObjectMap<C> full = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectMap<C> cross = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Creates a path instance.
     *
     * @param connectors The connectors array.
     * @param path The path queue.
     */
    protected Path(Long2ObjectMap<Cache<C>> connectors, Deque<Node> path) {
        origin = path.pollLast();
        target = path.pollFirst();

        Iterator<Node> it = path.descendingIterator();
        while (it.hasNext()) {
            Node node = it.next();
            long pos = node.asLong();

            Cache<C> cache = connectors.get(pos);
            if (cache != null) {
                C cable = cache.value();
                full.put(pos, cable);
                if (node.isCrossroad()) {
                    cross.put(pos, cable);
                }
            }
        }
    }

    /**
     * @return Gets the origin position.
     */
    public Node origin() {
        return origin;
    }

    /**
     * @return Gets the target position.
     */
    public Node target() {
        return target;
    }

    /**
     * @return Gets the full connectors path.
     */
    public Long2ObjectMap<C> getFull() {
        return full;
    }

    /**
     * @return Gets the crossroad connectors path.
     */
    public Long2ObjectMap<C> getCross() {
        return cross;
    }

    /**
     * @return Checks that the path is empty.
     */
    public boolean isEmpty() {
        return (origin == null || target == null);
    }
}