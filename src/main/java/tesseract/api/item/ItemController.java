package tesseract.api.item;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import tesseract.api.ConnectionType;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
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
    private final Long2IntMap holders = new Long2IntOpenHashMap();
    private final Long2ObjectMap<Map<Direction, List<ItemConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public ItemController(World dim) {
        super(IItemNode::fromPipe, dim);
        holders.defaultReturnValue(0);
    }

    @Override
    protected void onFrame() {
        holders.clear();
    }

    protected void handleInput(long pos, IItemNode producer) {
        if (data.containsKey(pos)) return;

        if (producer.canOutput()) {
            for (Direction direction : Graph.DIRECTIONS) {
                if (producer.canOutput(direction)) {
                    List<ItemConsumer> consumers = new ObjectArrayList<>();
                    long side = Pos.offset(pos, direction);// position.offset(direction).asLong();
                    Grid<IItemPipe> grid = group.getGridAt(side, direction);
                    if (grid != null) {
                        for (Path<IItemPipe> path : grid.getPaths(pos, direction)) {
                            if (!path.isEmpty()) {
                                Node target = path.target();
                                assert target != null;
                                onCheck(consumers, path, target.getDirection(), target.asLong());
                            }
                        }
                    }


                    if (!consumers.isEmpty()) {
                        data.computeIfAbsent(pos, m -> new EnumMap<>(Direction.class)).put(getMapDirection(pos, direction.getOpposite()), consumers);
                    }
                }
            }
        }
    }

    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<NodeCache<IItemNode>> e : group.getNodes().long2ObjectEntrySet()) {
            handleInput(e.getLongKey(), e.getValue().value());
        }

        for (Long2ObjectMap.Entry<Cache<IItemPipe>> entry : group.getPipes()) {
            handleInput(entry.getLongKey(), wrapPipe(entry.getValue().value()));
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

    public void insert(long producerPos, long pipePos, ItemTransaction transaction) {
        long key = producerPos == pipePos ? pipePos : Pos.sub(producerPos, pipePos);
        Direction dir = producerPos == pipePos ? Direction.NORTH : Direction.fromNormal(Pos.unpackX(key), Pos.unpackY(key), Pos.unpackZ(key));
        Map<Direction, List<ItemConsumer>> map = this.data.get(producerPos);
        ItemStack stack = transaction.stack;
        if (map == null) return;
        List<ItemConsumer> list = map.get(dir);
        if (list == null) return;

        //Here the verification starts.
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
                actual = Math.min(actual, consumer.getMinCapacity());
            } else {
                //Verify cross chain.
                for (Long2ObjectMap.Entry<Path.PathHolder<IItemPipe>> p : consumer.getCross().long2ObjectEntrySet()) {
                    long pos = p.getLongKey();
                    IItemPipe pipe = p.getValue().connector;
                    int stacksUsed = holders.get(pos) + tempHolders.get(pos);
                    if (pipe.getCapacity() == stacksUsed) {
                        actual = 0;
                        break;
                    }
                }
                //Insert temporary capacities
                if (actual > 0) {
                    for (Long2ObjectMap.Entry<Path.PathHolder<IItemPipe>> p : consumer.getCross().long2ObjectEntrySet()) {
                        tempHolders.compute(p.getLongKey(), (a, b) -> {
                            if (b == null) {
                                return 1;
                            }
                            return b + 1;
                        });
                    }
                }
            }

            if (actual == 0) continue;
            //Insert the count into the transaction.
            ItemStack insert = stack.copy();
            insert.setCount(actual);
            for (Path.PathHolder<IItemPipe> modifier : consumer.getModifiers()) {
                modifier.connector.modify(modifier.from, modifier.to, insert, true);
            }
            actual = insert.getCount();
            final int act = actual;
            transaction.addData(insert, t -> dataCommit(consumer, t, act));
            stack.setCount(stack.getCount() - actual);
            return;
        }
    }

    protected void dataCommit(ItemConsumer consumer, ItemStack stack, int transferred) {
        for (Path.PathHolder<IItemPipe> modifier : consumer.getModifiers()) {
            modifier.connector.modify(modifier.from, modifier.to, stack, false);
        }
        consumer.insert(stack, false);
        this.transferred += transferred;
        if (consumer.getConnection() == ConnectionType.VARIATE) {
            for (Long2ObjectMap.Entry<Path.PathHolder<IItemPipe>> entry : consumer.getCross().long2ObjectEntrySet()) {
                this.holders.compute(entry.getLongKey(), (a, b) -> {
                    if (b == null) return 1;
                    return b + 1;
                });
            }
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path      The paths to consumers.
     * @param Direction The added Directionection.
     * @param pos       The position of the producer.
     */
    private void onCheck(List<ItemConsumer> consumers, Path<IItemPipe> path, Direction Direction, long pos) {
        IItemNode node = group.getNodes().get(pos).value();
        if (node.canInput(Direction)) consumers.add(new ItemConsumer(node, path, Direction));
    }

    @Override
    public void getInfo(long pos, @Nonnull List<String> list) {
        this.group.getGroupInfo(pos, list);
        list.add(String.format("Item Data size: %d", this.data.size()));
    }

    public int getTransferred() {
        return transferred;
    }

    public int getCableTransferred(long pos) {
        return holders.get(pos);
    }

    @Override
    public ITickingController<ItemTransaction, IItemPipe, IItemNode> clone(INode group) {
        return new ItemController(dim).set(group);
    }
}
