package tesseract.api.fluid;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.graph.*;
import tesseract.util.Dir;
import tesseract.util.Node;
import tesseract.util.Pos;
import tesseract.util.RandomIterator;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.EnumMap;
import java.util.Iterator;
import java.util.Map;

import static tesseract.TesseractAPI.GLOBAL_FLUID_EVENT;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController implements ITickingController {

    private final int dim;
    private final Group<IFluidPipe, IFluidNode> group;
    private final Long2ObjectMap<FluidHolder> holders = new Long2ObjectLinkedOpenHashMap<>();
    private final Object2ObjectMap<IFluidNode, Map<Dir, ObjectList<FluidConsumer>>> data = new Object2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public FluidController(int dim, @Nonnull Group<IFluidPipe, IFluidNode> group) {
        this.dim = dim;
        this.group = group;
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
                        ObjectList<FluidConsumer> consumers = new ObjectArrayList<>();
                        long offset = position.offset(direction).asLong();

                        if (group.getNodes().containsKey(offset)) {
                            add(consumers, null, direction.invert(), offset);
                        } else {
                            Grid<IFluidPipe> grid = group.getGridAt(offset, direction);
                            if (grid != null) {
                                for (Path<IFluidPipe> path : grid.getPaths(pos)) {
                                    if (!path.isEmpty()) {
                                        Node target = path.target();
                                        assert target != null;
                                        add(consumers, path, target.getDirection(), target.asLong());
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
    }

    /**
     * Adds available consumers to the list.
     *
     * @param consumers The consumer nodes.
     * @param path The paths to consumers.
     * @param dir The added direction.
     * @param pos The position of the producer.
     */
    private void add(@Nonnull ObjectList<FluidConsumer> consumers, @Nullable Path<IFluidPipe> path, @Nonnull Dir dir, long pos) {
        IFluidNode consumer = group.getNodes().get(pos).value();
        if (consumer.canInput()) consumers.add(new FluidConsumer(consumer, path, dir));
    }

    /**
     * Call on the updates to send fluids.
     */
    @Override
    public void tick() {
        holders.clear();

        for (Object2ObjectMap.Entry<IFluidNode, Map<Dir, ObjectList<FluidConsumer>>> e : data.object2ObjectEntrySet()) {
            IFluidNode producer = e.getKey();

            for (Map.Entry<Dir, ObjectList<FluidConsumer>> c : e.getValue().entrySet()) {
                Dir direction = c.getKey();
                ObjectList<FluidConsumer> list = c.getValue();

                Object tank = producer.getAvailableTank(direction);
                if (tank == null) {
                    continue;
                }

                int outputAmount = producer.getOutputAmount(direction);

                // Using Random Permute to teleport fluids to random consumers in the list (similar round-robin with pseudo-random choice)
                Iterator<FluidConsumer> it = list.size() > 1 ? new RandomIterator<>(list) : list.iterator();
                while (it.hasNext()) {

                    FluidData data = producer.extract(tank, outputAmount, true);
                    if (data == null) {
                        continue;
                    }

                    FluidConsumer consumer = it.next();
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

                            holders.computeIfAbsent(pos, h -> new FluidHolder(pipe.getCapacity(), pipe.getPressure())).add(amount, fluid);
                        }
                    }

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

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        return new FluidController(dim, (Group<IFluidPipe, IFluidNode>) group);
    }
}
