package tesseract.api.item;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.RandomPermuteIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController extends Controller<ItemConsumer, IItemPipe, IItemNode> {

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
        holders.clear();

        for (Object2ObjectMap.Entry<IItemNode, ObjectList<ItemConsumer>> e : data.object2ObjectEntrySet()) {
            IItemNode producer = e.getKey();
            int outputAmount = producer.getOutputAmount();
            int[] slots = producer.getAvailableSlots();

            ObjectList<ItemConsumer> c = e.getValue();
            RandomPermuteIterator it = new RandomPermuteIterator(c.size());
            X:while (it.hasNext()) {
                ItemConsumer consumer = c.get(it.next());
                
                for (int slot : slots) {
                    ItemData item = producer.extract(slot, outputAmount, true);
                    if (item == null) {
                        continue;
                    }

                    Object stack = item.getStack();
                    if (!consumer.canAccept(stack)) {
                        continue;
                    }

                    int amount = consumer.insert(stack, true);
                    if (amount <= 0) {
                        continue;
                    }

                    // Stores the pressure into holder for path only for variate connection
                    int limit;
                    switch (consumer.getConnection()) {
                        case SINGLE:
                            limit = consumer.getMinCapacity(); // Fast check by the lowest cost pipe
                            if (limit < amount) {
                                amount = limit;
                            }
                            break;

                        case VARIATE:
                            limit = -1; // For init
                            for (Long2ObjectMap.Entry<IItemPipe> p : consumer.getCross()) {
                                long pos = p.getLongKey();

                                ItemHolder h = holders.get(pos);
                                if (h == null) {
                                    IItemPipe pipe = p.getValue();
                                    h = new ItemHolder(pipe.getCapacity());
                                    holders.put(pos, h);
                                }

                                limit = Math.min(limit != -1 ? limit : amount, h.getCapacity());
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

                    // Not null 100%
                    consumer.insert(producer.extract(slot, amount, false), false);

                    outputAmount -= amount;
                    if (outputAmount <= 0)
                        break X;
                }
            }
        }
    }

    @Override
    protected void onMerge(@Nonnull IItemNode producer, @Nonnull ObjectList<ItemConsumer> consumers) {
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
    protected void onCheck(@Nonnull IItemNode producer, @Nonnull ObjectList<ItemConsumer> consumers, @Nullable Path<IItemPipe> path, long pos) {
        IItemNode c = group.getNodes().get(pos).value();
        if (c.canInput()) consumers.add(new ItemConsumer(c, path));
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        assert (group instanceof Group<?, ?>);
        return new ItemController(dim, (Group<IItemPipe, IItemNode>) group);
    }

    @Override
    protected boolean isValid(@Nonnull IItemNode producer, @Nullable Dir direction) {
        return direction != null ? producer.canOutput(direction) : producer.canOutput() && producer.getOutputAmount() > 0;
    }
}
