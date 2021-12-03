package tesseract.graph;

import java.util.EnumMap;
import java.util.Map;

import net.minecraft.util.Direction;
import tesseract.api.IConnectable;
import tesseract.graph.Graph.INodeGetter;

public class NodeCache<T> implements IConnectable {
    private byte bitMap;
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
        this.bitMap = 0;
        for (Direction d : Graph.DIRECTIONS) {
            if (!graph.validate(d, pos)) continue;
            T t = getter.get(pos, d, () -> graph.onCapabilityInvalidate(pos));
            if (t != null) {
                value.put(d, t);
                if (t != null) setSide(d);
            }
        }
    }

    public boolean connects(Direction side) {
        return Connectivity.has(bitMap, side.get3DDataValue());
    }

    public boolean setSide(Direction side) {
        byte old = bitMap;
        if (!graph.validate(side, pos)) return count() > 0;
        this.bitMap = Connectivity.set(bitMap, side.get3DDataValue());
        this.value.put(side, getter.get(pos, side, () -> graph.onCapabilityInvalidate(pos)));
        return old != bitMap;
    }

    public boolean clearSide(Direction side) {
        this.bitMap= Connectivity.clear(bitMap, side.get3DDataValue());
        this.value.remove(side);
        return count() > 0;
    }

    public T value(Direction side) {
        return value.get(side);
    }

    public Iterable<Map.Entry<Direction, T>> values() {
        return value.entrySet();
    }

    public int count() {
        return Integer.bitCount(bitMap);
    }

    @Override
    public boolean validate(Direction dir) {
        return false;
    }
}
