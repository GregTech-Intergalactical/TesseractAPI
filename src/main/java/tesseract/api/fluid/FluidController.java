package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import tesseract.api.ConnectionType;
import tesseract.api.Consumer;
import tesseract.api.Controller;
import tesseract.api.ITickingController;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController<T, N extends IFluidNode<T>> extends Controller<IFluidPipe, N> implements IFluidEvent<T> {

    private long totalPressure, lastPressure;
    private int maxTemperature, isLeaking, lastTemperature, lastLeaking;
    private final Long2ObjectMap<FluidHolder<T>> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<N, Map<Dir, List<FluidConsumer<T>>>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     */
    public FluidController(int dim) {
        super(dim);
    }

    @Override
    public void change() {
        data.clear();

        for (Long2ObjectMap.Entry<Cache<N>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            N producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        List<FluidConsumer<T>> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(consumers, null, direction.getOpposite(), side);
                        } else {
                            Grid<IFluidPipe> grid = group.getGridAt(side, direction);
                            if (grid != null) {
                                for (Path<IFluidPipe> path : grid.getPaths(pos)) {
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

        for (Map<Dir, List<FluidConsumer<T>>> map : data.values()) {
            for (List<FluidConsumer<T>> consumers : map.values()) {
                consumers.sort(Consumer.COMPARATOR);
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
    private void onCheck(List<FluidConsumer<T>> consumers, Path<IFluidPipe> path, Dir dir, long pos) {
        N node = group.getNodes().get(pos).value();
        if (node.canInput()) consumers.add(new FluidConsumer<>(node, path, dir));
    }

    @Override
    public void tick() {
        super.tick();
        holders.clear();

        for (Object2ObjectMap.Entry<N, Map<Dir, List<FluidConsumer<T>>>> e : data.object2ObjectEntrySet()) {
            N producer = e.getKey();

            for (Map.Entry<Dir, List<FluidConsumer<T>>> c : e.getValue().entrySet()) {
                Dir direction = c.getKey();

                int tank = producer.getAvailableTank(direction);
                if (tank == -1) {
                    continue;
                }

                int outputAmount = producer.getOutputAmount(direction);

                for (FluidConsumer<T> consumer : c.getValue()) {

                    FluidData<T> data = producer.extract(tank, outputAmount, true);
                    if (data == null) {
                        continue;
                    }

                    T stack = data.getStack();
                    if (!consumer.canHold(stack)) {
                        continue;
                    }

                    int amount = consumer.insert(data, true);
                    if (amount <= 0) {
                        continue;
                    }

                    int temperature = data.getTemperature();
                    boolean isGaseous = data.isGaseous();

                    FluidData<T> drained = producer.extract(tank, amount, false);

                    assert drained != null;

                    // If we are here, then path had some invalid pipes which not suits the limits of temp/pressure/gas
                    if (consumer.getConnection() != ConnectionType.ADJACENT && !consumer.canHandle(temperature, amount, isGaseous)) {
                        // Find corrupt pipe and return
                        for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                            long pos = p.getLongKey();
                            IFluidPipe pipe = p.getValue();

                            switch (pipe.getHandler(temperature, amount, isGaseous)) {
                                case FAIL_TEMP:
                                    onPipeOverTemp(dim, pos, temperature);
                                    return;
                                case FAIL_PRESSURE:
                                    onPipeOverPressure(dim, pos, amount);
                                    return;
                                case FAIL_LEAK:
                                    onPipeGasLeak(dim, pos, drained);
                                    break;
                            }
                        }
                    }

                    // Stores the pressure into holder for path only for variate connection
                    if (consumer.getConnection() == ConnectionType.VARIATE) {
                        for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getCross().long2ObjectEntrySet()) {
                            long pos = p.getLongKey();
                            IFluidPipe pipe = p.getValue();

                            holders.computeIfAbsent(pos, h -> new FluidHolder<>(pipe)).add(amount, stack);
                        }
                    }

                    maxTemperature = Math.max(temperature, maxTemperature);
                    isLeaking = Math.max(isGaseous ? 1: 0, isLeaking);
                    totalPressure += amount;

                    consumer.insert(drained, false);

                    outputAmount -= amount;
                    if (outputAmount <= 0) {
                        break;
                    }
                }
            }
        }

        for (Long2ObjectMap.Entry<FluidHolder<T>> e : holders.long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            FluidHolder<T> absorber = e.getValue();

            // TODO: Find proper path to destroy

            if (absorber.isOverPressure()) {
                onPipeOverPressure(dim, pos, absorber.getPressure());
            }
            if (absorber.isOverCapacity()) {
                onPipeOverCapacity(dim, pos, absorber.getCapacity());
            }
        }
    }

    @Override
    protected void onFrame() {
        lastTemperature = maxTemperature;
        lastPressure = totalPressure;
        lastLeaking = isLeaking;
        totalPressure = 0L;
        maxTemperature = isLeaking = 0;
    }

    @Override
    public String[] getInfo() {
        return new String[]{
            "Maximum Temperature: ".concat(Integer.toString(lastTemperature)),
            "Total Pressure: ".concat(Long.toString(lastPressure)),
            "Any Leaks: ".concat(lastLeaking == 1 ? "Yes" : "No")
        };
    }

    @Override
    public ITickingController clone(INode group) {
        return new FluidController<>(dim).set(group);
    }
}
