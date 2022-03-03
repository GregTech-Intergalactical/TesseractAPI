package tesseract.graph;

import java.util.EnumMap;
import net.minecraft.util.Direction;
import tesseract.Tesseract;
import tesseract.api.IConnectable;
import tesseract.graph.Graph.INodeGetter;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public class NodeCache<T> implements IConnectable {

    private final EnumMap<Direction, T> value;
    public final INodeGetter<T> getter;
    private final BiPredicate<Direction, Long> validator;
    private final BiConsumer<Direction, Long> callback;
    private final long pos;
    private final boolean pipe;

    /**
     * Creates a cache instance.
     */
    public NodeCache(long pos, INodeGetter<T> getter, BiPredicate<Direction, Long> validator, BiConsumer<Direction, Long> callback) {
        this.value = new EnumMap<>(Direction.class);
        this.getter = getter;
        this.pos = pos;
        this.validator = validator;
        this.callback = callback;
        this.pipe = false;
        for (Direction d : Graph.DIRECTIONS) {
            updateSide(d);
        }
    }

    public NodeCache(long pos, INodeGetter<T> getter) {
        this.value = new EnumMap<>(Direction.class);
        this.getter = getter;
        this.pos = pos;
        this.pipe = true;
        this.validator = null;
        this.callback = null;
        for (Direction d : Graph.DIRECTIONS) {
            updateSide(d);
        }
    }


    public boolean connects(Direction side) {
        return value.get(side) != null;
    }

    public boolean updateSide(Direction side) {
        if (!pipe && !validator.test(side, pos)) {
            value.remove(side);
            return false;
        }
        //if we have this key it means the capability is still valid.
        if (this.value.containsKey(side)) return true;
        T t = getter.get(pos, side, () -> callback.accept(side, pos));
        if (t == null) {
            if (!pipe) Tesseract.LOGGER.info("NULL returned in NodeCache when not expected!");
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
