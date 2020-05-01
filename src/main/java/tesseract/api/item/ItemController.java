package tesseract.api.item;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController extends Controller<IItemPipe, IItemNode> {

    private int transferred;
    private final Long2ObjectMap<ItemHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<IItemNode, Map<Dir, List<ItemConsumer>>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public ItemController(int dim) {
        super(dim);
    }

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param container The group this controller handles.
     */
    public ItemController(int dim, @Nonnull INode container) {
        super(dim, container);
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
                        List<ItemConsumer> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(consumers, null, direction.invert(), side);
                        } else {
                            Grid<IItemPipe> grid = group.getGridAt(side, direction);
                            if (grid != null) {
                                for (Path<IItemPipe> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        onCheck(consumers, path, target.getDirection(), target.asLong());
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

        for (Map<Dir, List<ItemConsumer>> map : data.values()) {
            for (List<ItemConsumer> consumers : map.values()) {
                consumers.sort(Comparator.comparingInt(ItemConsumer::getPriority));
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
    private void onCheck(@Nonnull List<ItemConsumer> consumers, @Nullable Path<IItemPipe> path, @Nonnull Dir dir, long pos) {
        IItemNode consumer = group.getNodes().get(pos).value();
        if (consumer.canInput()) consumers.add(new ItemConsumer(consumer, path, dir));
    }

    @Override
    protected void onFrame() {
        transferred = 0;
        holders.clear();

        for (Object2ObjectMap.Entry<IItemNode, Map<Dir, List<ItemConsumer>>> e : data.object2ObjectEntrySet()) {
            IItemNode producer = e.getKey();

            for (Map.Entry<Dir, List<ItemConsumer>> c : e.getValue().entrySet()) {
                Dir direction = c.getKey();

                IntList slots = producer.getAvailableSlots(direction);
                if (slots.isEmpty()) {
                    continue;
                }

                IntIterator it = slots.iterator();
                int outputAmount = producer.getOutputAmount(direction);

                I:for (ItemConsumer consumer : c.getValue()) {
                    while (it.hasNext()) {
                        int slot = it.nextInt();

                        ItemData data = producer.extract(slot, outputAmount, true);
                        if (data == null) {
                            continue;
                        }

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

                                    limit = Math.min(limit, holders.computeIfAbsent(pos, h -> new ItemHolder(pipe)).getCapacity());
                                }

                                for (long pos : consumer.getCross().keySet()) {
                                    holders.get(pos).reduce(limit);
                                }

                                amount = limit;
                                break;
                        }

                        if (amount <= 0) {
                            continue I;
                        }

                        ItemData extracted = producer.extract(slot, amount, false);

                        assert extracted != null;
                        transferred += amount;

                        consumer.insert(extracted, false);

                        outputAmount -= amount;
                        if (outputAmount <= 0) {
                            break I;
                        }

                        if (producer.isEmpty(slot)) {
                            it.remove();
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public String[] getInfo() {
        return new String[]{"Total Transferred: ".concat(Integer.toString(transferred))};
    }

    @Nonnull
    @Override
    public ITickingController clone(@Nonnull INode group) {
        return new ItemController(dim, group);
    }
}
