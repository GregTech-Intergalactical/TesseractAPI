package tesseract.api.item;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;
import tesseract.util.RandomIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController implements ITickingController {

    private int tick;
    private final int dim;
    private final Group<IItemPipe, IItemNode> group;
    private final Long2ObjectMap<ItemHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<IItemNode, Map<Dir, ObjectList<ItemConsumer>>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public ItemController(int dim, @Nonnull Group<IItemPipe, IItemNode> group) {
        this.dim = dim;
        this.group = group;
    }

    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<Cache<IItemNode>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            IItemNode producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        ObjectList<ItemConsumer> consumers = new ObjectArrayList<>();
                        long offset = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(offset)) {
                            add(consumers, null, direction.invert(), offset);
                        } else {
                            Grid<IItemPipe> grid = group.getGridAt(offset, direction);
                            if (grid != null) {
                                for (Path<IItemPipe> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        add(consumers, path, target.getDirection(), target.asLong());
                                    }
                                }
                            }
                        }

                        if (!consumers.isEmpty()) {
                            data.computeIfAbsent(producer, map -> new EnumMap<>(Dir.class)).put(direction, consumers);
                        }
                    }
                }
            }
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param dir The added direction.
     * @param pos The position of the producer.
     */
    private void add(@Nonnull ObjectList<ItemConsumer> consumers, @Nullable Path<IItemPipe> path, @Nonnull Dir dir, long pos) {
        IItemNode consumer = group.getNodes().get(pos).value();
        if (consumer.canInput()) consumers.add(new ItemConsumer(consumer, path, dir));
    }

    @Override
    public void tick() {
        tick++; if (tick % 20 != 0) return; // Limitation of the tick rate
        holders.clear();

        for (Object2ObjectMap.Entry<IItemNode, Map<Dir, ObjectList<ItemConsumer>>> e : data.object2ObjectEntrySet()) {
            IItemNode producer = e.getKey();

            for (Map.Entry<Dir, ObjectList<ItemConsumer>> c : e.getValue().entrySet()) {
                Dir direction = c.getKey();
                ObjectList<ItemConsumer> list = c.getValue();

                IntList slots = producer.getAvailableSlots(direction);
                if (slots.isEmpty()) {
                    continue;
                }

                IntIterator id = slots.iterator();
                int outputAmount = producer.getOutputAmount(direction);

                // Using Random Permute to teleport items to random consumers in the list (similar round-robin with pseudo-random choice)
                Iterator<ItemConsumer> it = list.size() > 1 ? new RandomIterator<>(list) : list.iterator();
                I:while (it.hasNext()) {
                    while (id.hasNext()) {
                        int slot = id.nextInt();

                        ItemData data = producer.extract(slot, outputAmount, true);
                        if (data == null) {
                            continue;
                        }

                        ItemConsumer consumer = it.next();
                        Object item = data.getItem();
                        if (!consumer.canAccept(item)) {
                            continue;
                        }

                        int amount = consumer.insert(data, true);
                        if (amount <= 0) {
                            continue;
                        }

                        // Stores the pressure into holder for path only for variate connection
                        switch (consumer.getConnection()) {
                            case SINGLE:
                                int min = consumer.getMinCapacity(); // Fast check by the lowest cost pipe
                                if (min < amount) {
                                    amount = min;
                                }
                                break;

                            case VARIATE:
                                int limit = amount;
                                for (Long2ObjectMap.Entry<IItemPipe> p : consumer.getCross().long2ObjectEntrySet()) {
                                    long pos = p.getLongKey();
                                    IItemPipe pipe = p.getValue();

                                    limit = Math.min(limit, holders.computeIfAbsent(pos, h -> new ItemHolder(pipe.getCapacity())).getCapacity());
                                }

                                for (long pos : consumer.getCross().keySet()) {
                                    holders.get(pos).reduce(limit);
                                }

                                amount = limit;
                                break;
                        }

                        if (amount <= 0) {
                            //TODO: Switch to next slot or to next consumer ?
                            continue I;
                        }

                        ItemData extracted = producer.extract(slot, amount, false);

                        assert extracted != null;

                        consumer.insert(extracted, false);

                        outputAmount -= amount;
                        if (outputAmount <= 0) {
                            break I;
                        }

                        if (producer.isEmpty(slot)) {
                            id.remove();
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        return new ItemController(dim, (Group<IItemPipe, IItemNode>) group);
    }
}
