package tesseract.graph;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.util.Direction;
import tesseract.Tesseract;
import tesseract.api.IConnectable;
import tesseract.graph.Graph.INodeGetter;

public class NodeCache<T> implements IConnectable {
    private final EnumMap<Direction, T> value;
    private final INodeGetter<T> getter;
    private final Graph<?,?,T> graph;
    private final long pos;

    /**
     * Creates a cache instance.
     */
    public NodeCache(long pos, INodeGetter<T> getter, Graph<?,?,T> graph) {
        this.value = new EnumMap<>(Direction.class);
        this.getter = getter;
        this.pos = pos;
        this.graph = graph;
        for (Direction d : Graph.DIRECTIONS) {
            updateSide(d);
        }
    }

    public boolean connects(Direction side) {
        return value.get(side) != null;
    }

    public boolean updateSide(Direction side) {
        if (!graph.validate(side, pos)) {
            value.remove(side);
            return count() > 0;
        }
        T t = getter.get(pos, side, () -> graph.onCapabilityInvalidate(pos));
        if (t == null) {
            Tesseract.LOGGER.info("NULL returned in NodeCache when not expected!");
            this.value.remove(side);
            return count() > 0;
        }
        this.value.put(side, getter.get(pos, side, () -> graph.onCapabilityInvalidate(pos)));
        return value.size() > 0;
    }

    public T value(Direction side) {
        return value.get(side);
    }

    public Iterable<Map.Entry<Direction, T>> values() {
        return value.entrySet();
    }

    public int count() {
        return value.size();
    }

    @Override
    public boolean validate(Direction dir) {
        return false;
    }
}
