package tesseract.api.item;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import tesseract.api.ConnectionType;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.api.capability.ITransactionModifier;
import tesseract.graph.*;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController extends Controller<ItemTransaction, IItemPipe, IItemNode> {
    private int transferred;
    private final Long2ObjectMap<Map<Direction, List<ItemConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public ItemController(World dim, Graph.INodeGetter<IItemNode> getter) {
        super(dim, getter);
    }

    @Override
    protected void onFrame() {
        this.group.connectors().forEach(t -> t.value().setHolder(0));
    }

    protected void handleInput(long pos, NodeCache<IItemNode> cache) {
        // if (data.containsKey(pos)) return;
        for (Map.Entry<Direction, IItemNode> tup : cache.values()) {
            IItemNode producer = tup.getValue();
            Direction direction = tup.getKey();
            if (producer.canOutput()) {
                if (producer.canOutput(direction)) {
                    List<ItemConsumer> consumers = new ObjectArrayList<>();
                    long side = Pos.offset(pos, direction);// position.offset(direction).asLong();
                    Grid<IItemPipe> grid = group.getGridAt(side, direction);
                    if (grid != null) {
                        for (Path<IItemPipe> path : grid.getPaths(pos)) {
                            if (!path.isEmpty()) {
                                Node target = path.target();
                                assert target != null;
                                onCheck(producer, consumers, path, target.getDirection(), target.asLong());
                            }
                        }
                    } else if (group.getNodes().containsKey(side)) {
                        onCheck(producer, consumers, null, direction.getOpposite(), side);
                    }

                    if (!consumers.isEmpty()) {
                        data.computeIfAbsent(pos, m -> new EnumMap<>(Direction.class))
                                .put(direction.getOpposite(), consumers);
                    }
                }
            }
        }
    }

    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<NodeCache<IItemNode>> e : group.getNodes().long2ObjectEntrySet()) {
            handleInput(e.getLongKey(), e.getValue());
        }

        for (Map<Direction, List<ItemConsumer>> map : data.values()) {
            for (List<ItemConsumer> consumers : map.values()) {
                consumers.sort(Consumer.COMPARATOR);
            }
        }
    }

    @Override

    public void tick() {
        super.tick();
    }

    @Override
    public void insert(long producerPos, Direction side, ItemTransaction transaction, ITransactionModifier modifier) {
        Map<Direction, List<ItemConsumer>> map = this.data.get(Pos.offset(producerPos, side));
        ItemStack stack = transaction.stack;
        if (map == null)
            return;
        List<ItemConsumer> list = map.get(side);
        if (list == null)
            return;

        // Here the verification starts.
        Long2IntMap tempHolders = new Long2IntOpenHashMap();
        for (ItemConsumer consumer : list) {
            if (!consumer.canAccept(stack)) {
                continue;
            }
            int amount = consumer.insert(stack, true);
            if (amount == stack.getCount()) {
                continue;
            }
            int actual = stack.getCount() - amount;

            if (consumer.getConnection() == ConnectionType.SINGLE) {
                actual = actual;//Math.min(actual, consumer.getMinCapacity());
            } else {
                // Verify cross chain.
                for (Long2ObjectMap.Entry<IItemPipe> p : consumer.getCross().long2ObjectEntrySet()) {
                    long pos = p.getLongKey();
                    IItemPipe pipe = p.getValue();
                    int stacksUsed = pipe.getHolder() + tempHolders.get(pos);
                    if (pipe.getCapacity() == stacksUsed) {
                        actual = 0;
                        break;
                    }

                }
            }

            if (actual == 0)
                continue;

            // Insert the count into the transaction.
            ItemStack insert = stack.copy();
            insert.setCount(actual);
            modifier.modify(insert, null, side, true);
            actual = insert.getCount();
            final int act = actual;
            if (act == 0)
                continue;
            for (Long2ObjectMap.Entry<IItemPipe> p : consumer.getCross().long2ObjectEntrySet()) {
                tempHolders.compute(p.getLongKey(), (a, b) -> {
                    if (b == null) {
                        return 1;
                    }
                    return b + 1;
                });
            }
            transaction.addData(insert, t -> dataCommit(consumer, t, side, modifier, act));
            // stack.setCount(stack.getCount() - actual);
            if (transaction.stack.getCount() == 0)
                return;
        }
    }

    public void dataCommit(ItemConsumer consumer, ItemStack stack, Direction side, ITransactionModifier modifier, int transferred) {
        consumer.insert(stack, false);
        this.transferred += transferred;
        if (consumer.getConnection() == ConnectionType.VARIATE) {
            for (Long2ObjectMap.Entry<IItemPipe> entry : consumer.getCross().long2ObjectEntrySet()) {
                entry.getValue().setHolder(entry.getValue().getHolder()+1);
            }
            modifier.modify(stack, null, side, true);
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path      The paths to consumers.
     * @param dir       The added dir.
     * @param pos       The position of the producer.
     */
    private void onCheck(IItemNode producer, List<ItemConsumer> consumers, Path<IItemPipe> path, Direction dir, long pos) {
        IItemNode node = group.getNodes().get(pos).value(dir);
        if (node != null && node.canInput(dir))
            consumers.add(new ItemConsumer(node,producer, path, dir));
    }

    @Override
    public void getInfo(long pos, @Nonnull List<String> list) {
        if (this.group != null) {
            this.group.getGroupInfo(pos, list);
            list.add(String.format("Item Data size: %d", this.data.size()));
        }
    }

    public int getTransferred() {
        return transferred;
    }

    public int getCableTransferred(long pos) {
        return group.getConnector(pos).value().getHolder();
    }

    @Override
    public ITickingController<ItemTransaction, IItemPipe, IItemNode> clone(INode group) {
        return new ItemController(dim, getter).set(group);
    }
}
