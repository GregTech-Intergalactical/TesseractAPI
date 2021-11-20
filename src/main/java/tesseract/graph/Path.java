package tesseract.graph;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import net.minecraft.util.Direction;
import tesseract.api.IConnectable;
import tesseract.util.Node;

import java.util.Deque;

/**
 * The Path is a class that should work with paths for grids.
 */
public class Path<C extends IConnectable> {

    private final Node origin;
    private final Node target;
    private final Long2ObjectMap<PathHolder<C>> full = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectMap<PathHolder<C>> cross = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Creates a path instance.
     *
     * @param connectors The connectors array.
     * @param path       The path queue.
     */
    protected Path(Long2ObjectMap<Cache<C>> connectors, Deque<Node> path) {
        origin = path.pollLast();
        target = path.pollFirst();

        Node node;
        while (!path.isEmpty()) {
            node = path.removeLast();
            long pos = node.asLong();

            Cache<C> cache = connectors.get(pos);
            if (cache != null) {
                C cable = cache.value();
                Direction from = node.getDirection();
                Direction to;
                if (target.getParent() == node) {
                    to = target.getDirection().getOpposite();
                } else {
                    to = path.peekLast().getDirection().getOpposite();
                }
                PathHolder<C> holder = new PathHolder<>(cable, from, to);
                full.put(pos, holder);
                if (node.isCrossroad()) {
                    cross.put(pos, holder);
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
    public Long2ObjectMap<PathHolder<C>> getFull() {
        return Long2ObjectMaps.unmodifiable(full);
    }

    /**
     * @return Gets the crossroad connectors path.
     */
    public Long2ObjectMap<PathHolder<C>> getCross() {
        return Long2ObjectMaps.unmodifiable(cross);
    }

    /**
     * @return Checks that the path is empty.
     */
    public boolean isEmpty() {
        return (origin == null || target == null);
    }

    public static class PathHolder<C> {
        public final C connector;
        public final Direction from;
        public final Direction to;

        public PathHolder(C connector, Direction from, Direction to) {
            this.connector = connector;
            this.from = from;
            this.to = to;
        }
    }
}