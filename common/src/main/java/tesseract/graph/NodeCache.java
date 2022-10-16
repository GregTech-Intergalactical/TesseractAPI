package tesseract.graph;

import net.minecraft.core.Direction;
import tesseract.api.IConnectable;
import tesseract.graph.Graph.INodeGetter;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 A NodeCache handles updating its internal capabilites, caching the relevant capabilities
 from nearby tiles, hence why it is more complicated than a regular Cache.
**/
public class NodeCache<T> implements IConnectable {

  private final EnumMap<Direction, T> value;
  public final INodeGetter<T> getter;
  private final Predicate<CapabilityRecord> validator;
  private final Consumer<CapabilityRecord> callback;
  public final long pos;

  /**
   * Creates a cache instance.
   */
  public NodeCache(long pos, INodeGetter<T> getter, Predicate<CapabilityRecord> validator,
      Consumer<CapabilityRecord> callback) {
    this.value = new EnumMap<>(Direction.class);
    this.getter = getter;
    this.pos = pos;
    this.validator = validator;
    this.callback = callback;
    for (Direction d : Graph.DIRECTIONS) {
      updateSide(d);
    }
  }

  public static <T> NodeCache<T> of(long pos, T value) {
    return new NodeCache<>(pos, value);
  }

  public NodeCache(long pos, T value) {
    this.value = new EnumMap<>(Direction.class);
    this.getter = null;
    this.pos = pos;
    this.validator = null;
    this.callback = null;
    for (Direction d : Graph.DIRECTIONS) {
      this.value.put(d, value);
    }
  }

  public boolean connects(Direction side) {
    return value.get(side) != null;
  }

  public boolean updateSide(Direction side) {
    if (getter == null)
      return true;
    var rec = new CapabilityRecord(side, pos);
    if (validator != null & !validator.test(rec)) {
      value.remove(side);
      return false;
    }
    // if we have this key it means the capability is still valid.
    if (this.value.containsKey(side))
      return true;
    T t = getter.get(pos, side, () -> callback.accept(rec));
    if (t == null) {
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

  public record CapabilityRecord(Direction direction, long position) {
  }
}
