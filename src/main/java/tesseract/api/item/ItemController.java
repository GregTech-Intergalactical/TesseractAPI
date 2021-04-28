package tesseract.api.item;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.item.ItemStack;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Class acts as a controller in the group of an item components.
 */
public class ItemController<N extends IItemNode> extends Controller<ItemStack, IItemPipe, N> {
    private int transferred;
    private final Long2IntMap holders = new Long2IntOpenHashMap();
    private final Object2ObjectMap<N, Map<Dir, List<ItemConsumer>>> data = new Object2ObjectLinkedOpenHashMap<>();
    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public ItemController(Function<RegistryKey<World>, ServerWorld> supplier, RegistryKey<World> dim) {
        super(supplier, dim);
        holders.defaultReturnValue(-1);
    }

    @Override
    protected void onFrame() {

    }

    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<NodeCache<N>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            N producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        List<ItemConsumer> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(consumers, null, direction.getOpposite(), side);
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
                            data.computeIfAbsent(producer, m -> new EnumMap<>(Dir.class)).put(direction, consumers);
                        }
                    }
                }
            }
        }

        for (Map<Dir, List<ItemConsumer>> map : data.values()) {
            for (List<ItemConsumer> consumers : map.values()) {
                consumers.sort(Consumer.COMPARATOR);
            }
        }
    }

    public int insert(Pos producerPos, Dir direction, ItemStack stack, boolean simulate) {
        NodeCache<N> node = this.group.getNodes().get(producerPos.offset(direction).asLong());
        if (node == null) return stack.getCount();
        Map<Dir, List<ItemConsumer>> map = this.data.get(node.value());
        if (map == null) return stack.getCount();
        List<ItemConsumer> list = map.get(direction.getOpposite());
        if (list == null) return stack.getCount();
        for (ItemConsumer consumer : list) {
            if (!consumer.canAccept(stack)) {
                continue;
            }

            int amount = consumer.insert(stack, true);
            if (amount == stack.getCount()) {
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

                        int capacity = holders.get(pos);
                        if (capacity == -1) {
                            capacity = pipe.getCapacity();
                            holders.put(pos, capacity);
                        }
                        limit = Math.min(limit, capacity);
                    }

                    if (!simulate && limit < stack.getCount()) {
                        for (long pos : consumer.getFull().keySet()) {
                            holders.put(pos, Math.max(holders.get(pos) - (stack.getCount()-limit), 0));
                        }
                    }

                    amount = limit;
                    break;
            }
            if (simulate) {
                return amount;
            }
            if (amount == stack.getCount()) {
                return stack.getCount();
            } else {
                consumer.insert(stack, false);
                transferred += stack.getCount() - amount;
                return amount;
            }
        }
        return stack.getCount();
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param dir The added direction.
     * @param pos The position of the producer.
     */
    private void onCheck(List<ItemConsumer> consumers, Path<IItemPipe> path, Dir dir, long pos) {
        N node = group.getNodes().get(pos).value();
        if (node.canInput(dir)) consumers.add(new ItemConsumer(node, path, dir));
    }

    @Override
    public String[] getInfo(long pos) {
        return new String[]{"Total Transferred: ".concat(Integer.toString(transferred))};
    }

    @Override
    public ITickingController<ItemStack, IItemPipe, N> clone(INode group) {
        return new ItemController<N>(WORLD_SUPPLIER,dim).set(group);
    }
}
