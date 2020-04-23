package tesseract.api;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Pos;
import tesseract.util.RandomPermuteIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Class acts as a controller in the group of components.
 */
public abstract class Controller<P extends Producer<N>, S extends Consumer<C, N>, C extends IConnectable, N extends IConnectable> implements ITickingController {

    protected final int dim;
    protected final Group<C, N> group;
    protected final Object2ObjectMap<P, ObjectList<S>> data = new Object2ObjectLinkedOpenHashMap<>();

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
            P producer = onChange(e.getValue().value());
            long pos = e.getLongKey();

            if (producer.canOutput(null)) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        ObjectList<S> consumers = new ObjectArrayList<>();
                        long offset = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(offset)) {
                            onCheck(producer, consumers, direction, null, offset);
                        } else {
                            Grid<C> grid = group.getGridAt(offset, direction);
                            if (grid != null) {
                                for (Path<C> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        onCheck(producer, consumers, direction, path, path.target().asLong());
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
     * Creates a special iterator or just a default iterator for the consumers list.
     *
     * @param consumers The provided consumers list.
     * @return The iterator instance.
     */
    @Nonnull
    public Iterator<S> toIterator(@Nonnull ObjectList<S> consumers) {
        return consumers.size() > 1 ? new RandomIterator(consumers) : consumers.iterator();
    }

    /**
     * Class acts as a wrapper of a random permute iterator over a consumer list.
     */
    private class RandomIterator implements Iterator<S> {

        final ObjectList<S> delegate;
        final RandomPermuteIterator iterator;

        /**
         * Creates a permute iterator wrapper over a consumer list.
         *
         * @param consumers The provided consumers list.
         */
        public RandomIterator(@Nonnull ObjectList<S> consumers) {
            delegate = consumers;
            iterator = new RandomPermuteIterator(consumers.size());
        }

        @Override
        public boolean hasNext() {
            return iterator.hasNext();
        }

        @Override
        public S next() {
            return delegate.get(iterator.nextInt());
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException("RandomIterator::remove is not make sense!");
        }
    }

    /**
     * Adds available consumers to the list.
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     * @param direction The added direction.
     * @param path The paths to consumers.
     * @param pos The position of the producer.
     */
    protected abstract void onCheck(@Nonnull P producer, @Nonnull ObjectList<S> consumers, @Nonnull Dir direction, @Nullable Path<C> path, long pos);

    /**
     * Merge the existing consumers with new ones.
     * @param producer The producer node.
     * @param consumers The consumer nodes.
     */
    protected abstract void onMerge(@Nonnull P producer, @Nonnull ObjectList<S> consumers);

    /**
     * Calls constructor of the producer component.
     * @param node The provided node.
     * @return The producer instance.
     */
    protected abstract P onChange(N node);
}
