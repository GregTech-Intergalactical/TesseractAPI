package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import static tesseract.TesseractAPI.GLOBAL_FLUID_EVENT;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<IFluidPipe, IFluidNode> {

    private long totalPressure, lastPressure;
    private int maxTemperature, isLeaking, lastTemperature, lastLeaking;
    private final Long2ObjectMap<FluidHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<IFluidNode, Map<Dir, List<FluidConsumer>>> data = new Object2ObjectLinkedOpenHashMap<>();

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
        for (Long2ObjectMap.Entry<Cache<IFluidNode>> e : group.getNodes().long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            IFluidNode producer = e.getValue().value();

            if (producer.canOutput()) {
                Pos position = new Pos(pos);
                for (Dir direction : Dir.VALUES) {
                    if (producer.canOutput(direction)) {
                        List<FluidConsumer> consumers = new ObjectArrayList<>();
                        long side = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(side)) {
                            onCheck(consumers, null, direction.invert(), side);
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

        for (Map<Dir, List<FluidConsumer>> map : data.values()) {
            for (List<FluidConsumer> consumers : map.values()) {
                consumers.sort(Comparator.comparingInt(FluidConsumer::getPriority));
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
    private void onCheck(@Nonnull List<FluidConsumer> consumers, @Nullable Path<IFluidPipe> path, @Nonnull Dir dir, long pos) {
        IFluidNode consumer = group.getNodes().get(pos).value();
        if (consumer.canInput()) consumers.add(new FluidConsumer(consumer, path, dir));
    }

    @Override
    public void tick() {
        super.tick();
        holders.clear();

        for (Object2ObjectMap.Entry<IFluidNode, Map<Dir, List<FluidConsumer>>> e : data.object2ObjectEntrySet()) {
            IFluidNode producer = e.getKey();

            for (Map.Entry<Dir, List<FluidConsumer>> c : e.getValue().entrySet()) {
                Dir direction = c.getKey();

                Object tank = producer.getAvailableTank(direction);
                if (tank == null) {
                    continue;
                }

                int outputAmount = producer.getOutputAmount(direction);

                for (FluidConsumer consumer : c.getValue()) {

                    FluidData data = producer.extract(tank, outputAmount, true);
                    if (data == null) {
                        continue;
                    }

                    Object fluid = data.getFluid();
                    if (!consumer.canHold(fluid)) {
                        continue;
                    }

                    int amount = consumer.insert(data, true);
                    if (amount <= 0) {
                        continue;
                    }

                    int temperature = data.getTemperature();
                    boolean isGaseous = data.isGaseous();

                    FluidData drained = producer.extract(tank, amount, false);

                    assert drained != null;

                    // If we are here, then path had some invalid pipes which not suits the limits of temp/pressure/gas
                    if (!consumer.canHandle(temperature, amount, isGaseous) && consumer.getConnection() != ConnectionType.ADJACENT) { // Fast check by the lowest cost pipe
                        // Find corrupt pipe and return
                        for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull().long2ObjectEntrySet()) {
                            long pos = p.getLongKey();
                            IFluidPipe pipe = p.getValue();

                            switch (pipe.getHandler(temperature, amount, isGaseous)) {
                                case FAIL_TEMP:
                                    GLOBAL_FLUID_EVENT.onPipeOverTemp(dim, pos, temperature);
                                    return;
                                case FAIL_PRESSURE:
                                    GLOBAL_FLUID_EVENT.onPipeOverPressure(dim, pos, amount);
                                    return;
                                case FAIL_LEAK:
                                    GLOBAL_FLUID_EVENT.onPipeGasLeak(dim, pos, drained);
                                    break;
                            }
                        }
                    }

                    // Stores the pressure into holder for path only for variate connection
                    if (consumer.getConnection() == ConnectionType.VARIATE) {
                        for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getCross().long2ObjectEntrySet()) {
                            long pos = p.getLongKey();
                            IFluidPipe pipe = p.getValue();

                            holders.computeIfAbsent(pos, h -> new FluidHolder(pipe)).add(amount, fluid);
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

        for (Long2ObjectMap.Entry<FluidHolder> e : holders.long2ObjectEntrySet()) {
            long pos = e.getLongKey();
            FluidHolder absorber = e.getValue();

            // TODO: Find proper path to destroy

            if (absorber.isOverPressure()) {
                GLOBAL_FLUID_EVENT.onPipeOverPressure(dim, pos, absorber.getPressure());
            }
            if (absorber.isOverCapacity()) {
                GLOBAL_FLUID_EVENT.onPipeOverCapacity(dim, pos, absorber.getCapacity());
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

    @Nonnull
    @Override
    public String[] getInfo() {
        return new String[]{
            "Maximum Temperature: ".concat(Integer.toString(lastTemperature)),
            "Total Pressure: ".concat(Long.toString(lastPressure)),
            "Any Leaks: ".concat(lastLeaking == 1 ? "Yes" : "No")
        };
    }

    @Nonnull
    @Override
    public ITickingController clone(@Nonnull INode group) {
        Controller<?, ?> controller = new FluidController(dim);
        controller.setGroup(group);
        return controller;
    }
}
