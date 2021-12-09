package tesseract.graph;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.core.Direction;
import tesseract.Tesseract;
import tesseract.api.IConnectable;
import tesseract.graph.Graph.INodeGetter;

public class NodeCache<T> implements IConnectable {
    private final EnumMap<Direction, T> value;
    private final INodeGetter<T> getter;
    private final Graph<?,?,T> graph;
    private final long pos;
    public final IConnectable pipe;

    /**
     * Creates a cache instance.
     */
    public NodeCache(long pos, INodeGetter<T> getter, Graph<?,?,T> graph) {
        this.value = new EnumMap<>(Direction.class);
        this.getter = getter;
        this.pos = pos;
        this.graph = graph;
        this.pipe = null;
        for (Direction d : Graph.DIRECTIONS) {
            updateSide(d);
        }
    }

    public NodeCache(long pos, IConnectable pipe, INodeGetter<T> getter) {
        this.value = new EnumMap<>(Direction.class);
        this.getter = getter;
        this.pos = pos;
        this.graph = null;
        this.pipe = pipe;
        for (Direction d : Graph.DIRECTIONS) {
            updateSide(d);
        }
    }


    public boolean connects(Direction side) {
        return value.get(side) != null;
    }

    public boolean updateSide(Direction side) {
        if (pipe == null && !graph.validate(side, pos)) {
            value.remove(side);
            return false;
        }
        //if we have this key it means the capability is still valid.
        if (this.value.containsKey(side)) return true;
        T t = getter.get(pos, side, graph == null ? () -> {} : () -> graph.update(pos, side, getter, true));
        if (t == null) {
            if (pipe == null) Tesseract.LOGGER.info("NULL returned in NodeCache when not expected!");
            this.value.remove(side);
            return false;
        }
        this.value.put(side, t);
        return true;
    }

    public boolean clearSide(Direction side) {
        value.remove(side);
        return count() > 0;
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
