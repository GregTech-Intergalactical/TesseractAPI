package tesseract.api.item;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Iterator;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController extends Controller<ItemProducer, ItemConsumer, IItemPipe, IItemNode> {

    private int tick;
    private final Long2ObjectMap<ItemHolder> holders = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public ItemController(int dim, @Nonnull Group<IItemPipe, IItemNode> group) {
        super(dim, group);
    }

    @Override
    public void tick() {
        tick++; if (tick % 20 != 0) return; // Limitation of the tick rate
        holders.clear();

        for (Object2ObjectMap.Entry<ItemProducer, ObjectList<ItemConsumer>> e : data.object2ObjectEntrySet()) {
            ItemProducer producer = e.getKey();
            int outputAmount = producer.getOutputAmount();
            IntList slots = producer.getAvailableSlots();
            if (slots.isEmpty()) {
                continue;
            }
            IntIterator id = slots.iterator();

            // Using Random Permute to teleport items to random consumers in the list (similar round-robin with pseudo-random choice)
            Iterator<ItemConsumer> it = toIterator(e.getValue());
            X: while (it.hasNext()) {
                while (id.hasNext()) {
                    int slot = id.nextInt();

                    ItemData data = producer.extract(slot, outputAmount, true);
                    if (data == null) {
                        continue;
                    }

                    ItemConsumer consumer = it.next();
                    Object item = data.getItem();
                    if (!consumer.canQueried(item)) {
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
                            for (Long2ObjectMap.Entry<IItemPipe> p : consumer.getCross()) {
                                long pos = p.getLongKey();

                                ItemHolder h = holders.get(pos);
                                if (h == null) {
                                    IItemPipe pipe = p.getValue();
                                    h = new ItemHolder(pipe.getCapacity());
                                    holders.put(pos, h);
                                }

                                limit = Math.min(limit, h.getCapacity());
                            }

                            for (Long2ObjectMap.Entry<IItemPipe> p : consumer.getCross()) {
                                long pos = p.getLongKey();

                                ItemHolder h = holders.get(pos);
                                if (h != null) {
                                    h.reduce(limit);
                                }
                            }

                            amount = limit;
                            break;
                    }

                    ItemData extracted = producer.extract(slot, amount, false);

                    assert extracted != null;

                    consumer.insert(extracted, false);

                    outputAmount -= amount;
                    if (outputAmount <= 0)
                        break X;

                    if (producer.isEmpty(slot)) {
                        id.remove();
                    }
                }
            }
        }
    }

    @Override
    protected void onMerge(@Nonnull ItemProducer producer, @Nonnull ObjectList<ItemConsumer> consumers) {
        ObjectList<ItemConsumer> existingConsumers = data.get(producer);
        for (ItemConsumer c : consumers) {
            boolean found = false;
            for (ItemConsumer ec : existingConsumers) {
                if (ec.getConsumer() == c.getConsumer()) found = true;
                if (!found) existingConsumers.add(c);
            }
        }
    }

    @Override
    protected void onCheck(@Nonnull ItemProducer producer, @Nonnull ObjectList<ItemConsumer> consumers, @Nonnull Dir direction, @Nullable Path<IItemPipe> path, long pos) {
        IItemNode c = group.getNodes().get(pos).value();
        if (c.canInput()) consumers.add(new ItemConsumer(c, path, direction));
    }

    @Override
    protected ItemProducer onChange(IItemNode node) {
        return new ItemProducer(node);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        return new ItemController(dim, (Group<IItemPipe, IItemNode>) group);
    }

    @Override
    protected boolean isValid(@Nonnull ItemProducer producer, @Nullable Dir direction) {
        return direction != null ? producer.canOutput(direction) : producer.canOutput() && producer.getOutputAmount() > 0;
    }
}
