package tesseract.graph;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Direction;
import tesseract.api.Controller;
import tesseract.api.IConnectable;
import tesseract.util.CID;
import tesseract.util.Pos;

import java.util.List;
import java.util.function.Supplier;

/**
 * Class provides the functionality of any set of nodes.
 */
public class Graph<T, C extends IConnectable, N> implements INode {

  public static final Direction[] DIRECTIONS = Direction.values();
  private final Int2ObjectMap<Group<T, C, N>> groups = new Int2ObjectLinkedOpenHashMap<>();
  private final Long2IntMap positions = new Long2IntLinkedOpenHashMap(); // group positions
  private final Supplier<Controller<T, C, N>> controller;

  public Graph(Supplier<Controller<T, C, N>> controller) {
    positions.defaultReturnValue(CID.INVALID);
    this.controller = controller;
  }

  @Override
  public boolean contains(long pos) {
    return positions.containsKey(pos);
  }

  @Override
  public boolean linked(long from, Direction towards, long to) {
    return positions.containsKey(from) && positions.containsKey(to) && positions.get(from) == positions.get(to);
  }

  @Override
  public boolean connects(long pos, Direction towards) {
    return contains(pos);
  }

  /**
   * @return Gets the size of the groups map.
   */
  public int countGroups() {
    return groups.size();
  }

  /**
   * @return Gets the groups map.
   */
  public Int2ObjectMap<Group<T, C, N>> getGroups() {
    return Int2ObjectMaps.unmodifiable(groups);
  }

  public void addNode(NodeCache<N> cache) {
    if (cache.count() == 0)
      return;
    Group<T, C, N> group = add(cache.pos, () -> Group.singleNode(cache.pos, cache, controller.get()));
    if (group != null)
      group.addNode(cache.pos, cache, controller.get());
  }

  /**
   * Primary Tesseract interaction. Adds a connector to the graph at the specified
   * position while adding listeners to blocks
   * around it.
   *
   * @param pos       the connector position.
   * @param connector the cached connector.
   */
  public void addConnector(long pos, Cache<C> connector) {
    if (!contains(pos)) {
      Group<T, C, N> group = add(pos, () -> Group.singleConnector(pos, connector, controller.get()));
      if (group != null)
        group.addConnector(pos, connector, controller.get());
    }
  }

  public int size() {
    return positions.size();
  }

  /**
   * Adds an item to the Graph, in a manner generic across nodes and connectors.
   *
   * @param pos    The position at which the item will be added.
   * @param single A group containing a single entry, if the position is not
   *               touching any existing positions.
   * @return An existing group, that the caller should add the entry to.
   */
  private Group<T, C, N> add(long pos, Supplier<Group<T, C, N>> single) {
    int id;
    IntSet mergers = getNeighboringGroups(pos);
    switch (mergers.size()) {
      case 0 -> {
        id = CID.nextId();
        positions.put(pos, id);
        groups.put(id, single.get());
        return null;
      }
      case 1 -> {
        id = mergers.iterator().nextInt();
        positions.put(pos, id);
        return groups.get(id);
      }
      default -> {
        Merged<T, C, N> data = beginMerge(mergers);
        positions.put(pos, data.bestId);
        for (Group<T, C, N> other : data.merged) {
          data.best.mergeWith(other, pos);
        }
        return data.best;
      }
    }
  }

  /**
   * Removes an entry from the Group, potentially splitting it if needed. By
   * calling this function, the caller asserts that this group contains the
   * specified position; the function may misbehave if the group does not actually
   * contain the specified position.
   *
   * @param pos The position of the entry to remove.
   */
  public boolean removeAt(long pos) {
    return removeInternal(pos);
  }

  private boolean removeInternal(long pos) {
    int id = positions.get(pos);

    if (id == CID.INVALID) {
      return false;
    }
    Group<T, C, N> group = groups.get(id);

    boolean ok = group.removeAt(pos, newGroup -> {
      int newId = CID.nextId();
      groups.put(newId, newGroup);

      // Mark the nodes as pointing at the new group
      for (long part : newGroup.getNodes().keySet()) {
        positions.put(part, newId);
      }

      // Mark the connectors as pointing at the new group
      for (Grid<C> grid : newGroup.getGrids().values()) {
        for (long part : grid.getConnectors().keySet()) {
          positions.put(part, newId);
        }
      }
    });
    if (ok) {
      positions.remove(pos);
    }
    if (group.countBlocks() == 0) {
      groups.remove(id);
    }
    return ok;
  }

  /**
   * Gets the group by a given position.
   *
   * @param pos The position of the group.
   * @return The group, guaranteed to not be null.
   */
  public Group<T, C, N> getGroupAt(long pos) {
    int id = positions.get(pos);
    return (id != CID.INVALID) ? groups.get(id) : null;
  }

  /**
   * Starts a merging process for a given groups.
   *
   * @param mergers An array of neighbors groups id.
   * @return The wrapper with groups which should be merged.
   */
  private Merged<T, C, N> beginMerge(IntSet mergers) {
    int bestId = mergers.iterator().nextInt();
    Group<T, C, N> best = groups.get(bestId);
    int bestSize = best.countBlocks();

    for (int id : mergers) {
      Group<T, C, N> candidate = groups.get(id);
      int size = candidate.countBlocks();

      if (size > bestSize) {
        best = candidate;
        bestId = id;
        bestSize = size;
      }
    }

    List<Group<T, C, N>> mergeGroups = new ObjectArrayList<>(mergers.size() - 1);

    for (int id : mergers) {
      if (id == bestId) {
        continue;
      }

      Group<T, C, N> removed = groups.remove(id);

      // Remap each position to point to the correct group.
      for (long pos : removed.getBlocks()) {
        positions.put(pos, bestId);
      }

      mergeGroups.add(removed);
    }

    return new Merged<>(bestId, best, mergeGroups);
  }

  /**
   * Lookups for neighbors groups around given position.
   *
   * @param pos The search position.
   * @return The set of the groups which are neighbors to each other.
   */
  private IntSet getNeighboringGroups(long pos) {
    IntSet neighbors = new IntLinkedOpenHashSet(6);

    Pos position = new Pos(pos);
    for (Direction direction : Graph.DIRECTIONS) {
      long side = position.offset(direction).asLong();
      int id = positions.get(side);

      if (id != CID.INVALID) {
        neighbors.add(id);
      }
    }

    return neighbors;
  }

  /**
   * @apiNote Wrapper for merged groups.
   */
  private record Merged<T, C extends IConnectable, N> (int bestId, Group<T, C, N> best,
      List<Group<T, C, N>> merged) {
    /**
     * Constructs a new Merged of the groups.
     */
    private Merged {
    }
  }

  public interface INodeGetter<T> {
    T get(long pos, Direction capSide, Runnable capCallback);
  }
}
