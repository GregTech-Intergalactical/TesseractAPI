package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;
import tesseract.api.ConnectionType;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.*;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<FluidTransaction, IFluidPipe, IFluidNode> implements IFluidEvent<FluidStack> {

    // TODO: assign the value from Antimatter config
    public final static boolean HARDCORE_PIPES = false;
    public final static boolean SLOOSH = false;
    public static double PIPE_LEAK = 0.8;
    private long totalPressure, lastPressure;
    private int maxTemperature, lastTemperature;
    private boolean isLeaking, lastLeaking;
    private final Long2ObjectMap<FluidHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Long2ObjectMap<Map<Direction, List<FluidConsumer>>> data = new Long2ObjectLinkedOpenHashMap<>();

    private final Long2IntMap pressureData = new Long2IntOpenHashMap(10);

    /**
     * Creates instance of the controller.
     *
     * @param world the world.
     */
    public FluidController(World world) {
        super(IFluidNode::fromPipe, world);
    }

    private void handleInput(long pos, IFluidNode producer) {
        if (data.containsKey(pos)) return;

        if (producer.canOutput()) {
            for (Direction direction : Graph.DIRECTIONS) {
                if (producer.canOutput(direction)) {
                    List<FluidConsumer> consumers = new ObjectArrayList<>();
                    long side = Pos.offset(pos, direction);// position.offset(direction).asLong();

                    Grid<IFluidPipe> grid = group.getGridAt(side, direction);
                    if (grid != null) {
                        for (Path<IFluidPipe> path : grid.getPaths(pos, direction)) {
                            if (!path.isEmpty()) {
                                Node target = path.target();
                                assert target != null;
                                onCheck(consumers, path, target.getDirection(), target.asLong());
                            }
                        }
                    }

                    if (!consumers.isEmpty()) {
                        data.computeIfAbsent(pos, map -> new EnumMap<>(Direction.class)).put(getMapDirection(pos, direction.getOpposite()), consumers);
                    }
                }
            }
        }
    }

    @Override
    public void change() {
        if (!SLOOSH) {
            data.clear();
            holders.clear();
            for (Long2ObjectMap.Entry<NodeCache<IFluidNode>> e : group.getNodes().long2ObjectEntrySet()) {
                handleInput(e.getLongKey(), e.getValue().value());
            }
            for (Long2ObjectMap.Entry<Cache<IFluidPipe>> e : group.getPipes()) {
                handleInput(e.getLongKey(), wrapPipe(e.getValue().value()));
            }
            for (Map<Direction, List<FluidConsumer>> map : data.values()) {
                for (List<FluidConsumer> consumers : map.values()) {
                    consumers.sort(Consumer.COMPARATOR);
                }
            }
            this.data.values().stream().flatMap(t -> t.values().stream().flatMap(Collection::stream)).flatMap(t -> {
                if (t.getConnection() == ConnectionType.VARIATE) {
                    return t.getCross().long2ObjectEntrySet().stream().map(i -> new Tuple<>(i.getLongKey(), i.getValue().connector));
                } else if (t.getConnection() == ConnectionType.SINGLE) {
                    Cache<IFluidPipe> conn = this.group.getConnector(t.lowestPipePosition); //Conn can be null if there is a
                    return conn == null ? Stream.empty() : Stream.of(new Tuple<>(t.lowestPipePosition, conn.value()));
                }
                return Stream.empty();
            }).forEach(a -> this.holders.putIfAbsent(a.getA(), new FluidHolder(a.getB())));
        }
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path      The paths to consumers.
     * @param dir       The added direction.
     * @param pos       The position of the producer.
     */
    private void onCheck(List<FluidConsumer> consumers, Path<IFluidPipe> path, Direction dir, long pos) {
        IFluidNode node = group.getNodes().get(pos).value();
        if (node.canInput()) consumers.add(new FluidConsumer(node, path, dir));
    }

    @Override
    public void insert(long producerPos, long pipePos, FluidTransaction transaction) {
        if (SLOOSH) return;
        if (!transaction.isValid()) return;
        long key = producerPos == pipePos ? pipePos : Pos.sub(producerPos, pipePos);
        Direction dir = producerPos == pipePos ? Direction.NORTH : Direction.fromNormal(Pos.unpackX(key), Pos.unpackY(key), Pos.unpackZ(key));
        Map<Direction, List<FluidConsumer>> map = this.data.get(producerPos);
        if (map == null) return;
        List<FluidConsumer> list = map.get(dir);
        if (list == null) return;

        pressureData.clear();

        loop:
        for (FluidConsumer consumer : list) {
            FluidStack data = transaction.stack.copy();
            if (!consumer.canHold(data)) {
                continue;
            }

            int amount = consumer.insert(data, true);
            if (amount <= 0) {
                continue;
            }
            if (!HARDCORE_PIPES) {
                if (consumer.getConnection() == ConnectionType.SINGLE) {
                    amount = Math.min(amount, consumer.getMinPressure() * 20);
                } else {
                    for (Long2ObjectMap.Entry<Path.PathHolder<IFluidPipe>> entry : consumer.getCross().long2ObjectEntrySet()) {
                        FluidHolder holder = holders.get(entry.getLongKey());
                        if (!holder.allowFluid(data.getFluid())) {
                            amount = 0;
                            break;
                        }
                        int tempData = pressureData.get(entry.getLongKey());
                        amount = Math.min(amount, holder.getPressureAvailable() - tempData);
                        if (amount == 0) continue loop;
                    }
                }
            }
            data.setAmount(amount);
            if (data.isEmpty()) continue;
            if (consumer.getConnection() == ConnectionType.VARIATE) {
                for (Long2ObjectMap.Entry<Path.PathHolder<IFluidPipe>> p : consumer.getCross().long2ObjectEntrySet()) {
                    final int finalAmount = amount;
                    pressureData.compute(p.getLongKey(), (k, v) -> v == null ? finalAmount : v + finalAmount);
                }
            }
            for (Path.PathHolder<IFluidPipe> modifier : consumer.getModifiers()) {
                modifier.connector.modify(modifier.from, modifier.to, data, true);
            }
            transaction.addData(data.copy(), a -> dataCommit(consumer, a));

            if (transaction.stack.isEmpty()) break;
        }
    }

    protected void dataCommit(FluidConsumer consumer, FluidStack stack) {
        int temperature = stack.getFluid().getAttributes().getTemperature();
        int amount = stack.getAmount();
        boolean isGaseous = stack.getFluid().getAttributes().isGaseous();
        boolean cantHandle = !consumer.canHandle(temperature, isGaseous);
        if (!cantHandle) {
            for (Long2ObjectMap.Entry<Path.PathHolder<IFluidPipe>> p : consumer.getFull().long2ObjectEntrySet()) {
                long pos = p.getLongKey();
                IFluidPipe pipe = p.getValue().connector;
                switch (pipe.getHandler(temperature, isGaseous)) {
                    case FAIL_TEMP:
                        onPipeOverTemp(getWorld(), pos, temperature);
                        return;
                    case FAIL_LEAK:
                        stack = onPipeGasLeak(getWorld(), pos, stack);
                        isLeaking = true;
                        break;
                    default:
                        break;
                }
            }
        }
        if (consumer.getConnection() == ConnectionType.SINGLE) {
            FluidHolder holder = holders.get(consumer.lowestPipePosition);
            holder.use(stack.getAmount(), stack.getFluid(), getWorld().getGameTime());
            if (holder.isOverPressure()) {
                onPipeOverPressure(getWorld(), consumer.lowestPipePosition, amount, stack);
                return;
            }
            if (holder.isOverCapacity()) {
                onPipeOverCapacity(getWorld(), consumer.lowestPipePosition, amount, stack);
                return;
            }
        } else if (consumer.getConnection() == ConnectionType.VARIATE) {
            for (Long2ObjectMap.Entry<Path.PathHolder<IFluidPipe>> pathHolderEntry : consumer.getCross().long2ObjectEntrySet()) {
                FluidHolder holder = holders.get(pathHolderEntry.getLongKey());
                holder.use(stack.getAmount(), stack.getFluid(), getWorld().getGameTime());
                if (holder.isOverPressure()) {
                    onPipeOverPressure(getWorld(), pathHolderEntry.getLongKey(), amount, stack);
                    return;
                }
                if (holder.isOverCapacity()) {
                    onPipeOverCapacity(getWorld(), pathHolderEntry.getLongKey(), amount, stack);
                    return;
                }
            }
        }
        for (Path.PathHolder<IFluidPipe> modifier : consumer.getModifiers()) {
            modifier.connector.modify(modifier.from, modifier.to, stack, false);
        }
        maxTemperature = Math.max(temperature, maxTemperature);
        totalPressure += amount;
        consumer.insert(stack, false);
    }

    @Override
    public void tick() {
        super.tick();
        for (FluidHolder pipe : this.holders.values()) {
            pipe.tick(getWorld().getGameTime());
        }
    }

    @Override
    protected void onFrame() {
        lastTemperature = maxTemperature;
        lastPressure = totalPressure;
        lastLeaking = isLeaking;
        totalPressure = 0L;
        maxTemperature = 0;
        isLeaking = false;
    }

    @Nullable
    public FluidHolder getCableHolder(long pos) {
        return holders.get(pos);
    }

    @Override
    public void getInfo(long pos, @Nonnull List<String> list) {
        this.group.getGroupInfo(pos, list);
        list.add(String.format("Fluid Data size: %d", this.data.size()));
    }

    @Override
    public ITickingController clone(INode group) {
        return new FluidController(dim).set(group);
    }

}
