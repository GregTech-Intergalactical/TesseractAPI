package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import java.util.Iterator;

import static tesseract.TesseractAPI.GLOBAL_FLUID_EVENT;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<FluidConsumer, IFluidPipe, IFluidNode> {

    private final Long2ObjectMap<FluidHolder> holders = new Long2ObjectLinkedOpenHashMap<>();

    /**
     * Creates instance of the controller.
     *
     * @param dim The dimension id.
     * @param group The group this controller handles.
     */
    public FluidController(int dim, @Nonnull Group<IFluidPipe, IFluidNode> group) {
        super(dim, group);
    }

    /**
     * Call on the updates to send fluids.
     */
    @Override
    public void tick() {
        holders.clear();

        for (Object2ObjectMap.Entry<IFluidNode, ObjectList<FluidConsumer>> e : data.object2ObjectEntrySet()) {
            IFluidNode producer = e.getKey();
            int outputAmount = Math.min(producer.getOutputPressure(), producer.getCapacity());

            // Using Random Permute to teleport fluids to random consumers in the list (similar round-robin with pseudo-random choice)
            Iterator<FluidConsumer> it = toIterator(e.getValue());
            while (it.hasNext()) {
                FluidConsumer consumer = it.next();

                FluidData data = producer.extract(outputAmount, true);
                if (data == null || !consumer.canHold(data)) {
                    continue;
                }

                int amount = consumer.insert(data, true);
                if (amount <= 0) {
                    continue;
                }

                outputAmount = data.getAmount();
                Object fluid = data.getFluid();
                int temperature = data.getTemperature();
                boolean isGaseous = data.isGaseous();

                FluidData drained = producer.extract(amount, false);

                // If we are here, then path had some invalid pipes which not suits the limits of temp/pressure/gas
                if (!consumer.canHandle(temperature, amount, isGaseous) && consumer.getConnection() != ConnectionType.ADJACENT) { // Fast check by the lowest cost pipe
                    // Find corrupt pipe and return
                    for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getFull()) {
                        IFluidPipe pipe = p.getValue();
                        long pos = p.getLongKey();

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
                    for (Long2ObjectMap.Entry<IFluidPipe> p : consumer.getCross()) {
                        long pos = p.getLongKey();

                        FluidHolder h = holders.get(pos);
                        if (h == null) {
                            IFluidPipe pipe = p.getValue();
                            holders.put(pos, new FluidHolder(pipe.getCapacity(), pipe.getPressure(), amount, fluid));
                        } else {
                            h.add(amount, fluid);
                        }
                    }
                }

                consumer.insert(drained, false);

                outputAmount -= amount;
                if (outputAmount <= 0)
                    break;
            }
        }

        for (Long2ObjectMap.Entry<FluidHolder> e : holders.long2ObjectEntrySet()) {
            FluidHolder absorber = e.getValue();
            long pos = e.getLongKey();

            if (absorber.isOverPressure()) {
                GLOBAL_FLUID_EVENT.onPipeOverPressure(dim, pos, absorber.getPressure());
            }
            if (absorber.isOverCapacity()) {
                GLOBAL_FLUID_EVENT.onPipeOverCapacity(dim, pos, absorber.getCapacity());
            }
        }
    }

    @Override
    protected void onMerge(@Nonnull IFluidNode producer, @Nonnull ObjectList<FluidConsumer> consumers) {
        ObjectList<FluidConsumer> existingConsumers = data.get(producer);
        for (FluidConsumer c : consumers) {
            boolean found = false;
            for (FluidConsumer ec : existingConsumers) {
                if (ec.getConsumer() == c.getConsumer()) found = true;
                if (!found) existingConsumers.add(c);
            }
        }
    }

    @Override
    protected void onCheck(@Nonnull IFluidNode producer, @Nonnull ObjectList<FluidConsumer> consumers, @Nullable Path<IFluidPipe> path, long pos) {
        IFluidNode c = group.getNodes().get(pos).value();
        if (c.canInput()) {
            int pressure = producer.getOutputPressure();
            if (pressure > c.getInputPressure()) {
                GLOBAL_FLUID_EVENT.onNodeOverPressure(dim, pos, pressure);
            } else {
                consumers.add(new FluidConsumer(c, path));
            }
        }
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public ITickingController clone(@Nonnull INode group) {
        return new FluidController(dim, (Group<IFluidPipe, IFluidNode>) group);
    }

    @Override
    protected boolean isValid(@Nonnull IFluidNode producer, @Nullable Dir direction) {
        return direction != null ? producer.canOutput(direction) : producer.canOutput() && producer.getOutputPressure() > 0;
    }
}
