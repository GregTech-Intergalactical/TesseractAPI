package tesseract.api;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Class acts as a controller in the group of components.
 */
public abstract class Controller<W extends Consumer<C, N>, C extends IConnectable, N extends IConnectable> implements ITickingController {

    protected final int dim;
    protected final Group<C, N> group;
    protected final Object2ObjectMap<N, ObjectList<W>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    protected Controller(int dim, @Nonnull Group<C, N> group) {
        this.dim = dim;
        this.group = group;
    }

    /**
     * Executes when the group structure has changed.
     * <p>
     * First, it clears previous controller map, after it lookup for the position of node and looks for the around grids.
     * Second, it collects all producers and collectors for the grid and stores it into data map.
     * Finally, it will pre-build consumer objects which are available for the producers. So each producer has a list of possible
     * consumers with unique information about paths, loss, ect.
     * </p>
     * @see tesseract.graph.Grid (Cache)
     */
    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<Cache<N>> e : group.getNodes().long2ObjectEntrySet()) {
            N producer = e.getValue().value();
            long pos = e.getLongKey();

            if (isValid(producer, null)) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (isValid(producer, direction)) {
                        ObjectList<W> consumers = new ObjectArrayList<>();
                        long offset = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(offset)) {
                            onCheck(producer, consumers, null, offset);
                        } else {
                            Grid<C> grid = group.getGridAt(offset, direction);
                            if (grid != null) {
                                for (Path<C> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        onCheck(producer, consumers, path, path.target().asLong());
                                    }
                                }
                            }
                        }

                        if (!consumers.isEmpty()) {
                            if (data.containsKey(producer)) {
                                onMerge(producer, consumers);
                            } else {
                                data.put(producer, consumers);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds available consumers to the list.
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param pos The position of the producer.
     */
    protected abstract void onCheck(@Nonnull N producer, @Nonnull ObjectList<W> consumers, @Nullable Path<C> path, long pos);

    /**
     * Merge the existing consumers with new ones.
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     */
    protected abstract void onMerge(@Nonnull N producer, @Nonnull ObjectList<W> consumers);

    /**
     * Used to determine valid producers.
     * @param producer The producer node.
     * @param direction Direction to the out.
     * @return Returns true if the given direction is output side.
     */
    protected abstract boolean isValid(@Nonnull N producer, @Nullable Dir direction);
}
