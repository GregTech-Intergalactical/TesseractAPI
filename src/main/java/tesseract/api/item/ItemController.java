package tesseract.api.item;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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

        // TODO: Rework that, probably not works like suppose
        for (Object2ObjectMap.Entry<IItemNode, ObjectList<ItemConsumer>> e : data.object2ObjectEntrySet()) {
            IItemNode producer = e.getKey();
            int outputAmount = producer.getOutputAmount();
            int prevSlot = 0;

            for (ItemConsumer consumer : e.getValue()) {

                int slot = producer.nextSlot(prevSlot);
                if (slot == -1) {
                    break;
                }

                prevSlot = slot;
                ItemData data = producer.extract(slot, outputAmount, true);
                Object stack = data.getStack();
                if (!consumer.canAccept(stack)) {
                    continue;
                }

                outputAmount = data.getCount();

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

                consumer.insert(producer.extract(slot, amount, false), false);

                outputAmount -= amount;
                if (outputAmount <= 0)
                    prevSlot++;
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
