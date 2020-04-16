package tesseract.api.fluid;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.objects.*;
import tesseract.api.ConnectionType;
import tesseract.api.Controller;
import tesseract.graph.*;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static tesseract.TesseractAPI.GLOBAL_FLUID_EVENT;

/**
 * Class acts as a controller in the group of a fluid components.
 */
public class FluidController extends Controller<FluidConsumer, IFluidPipe, IFluidNode> {

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
        absorbs.clear();

        for (Object2ObjectMap.Entry<IFluidNode, ObjectList<FluidConsumer>> e : data.object2ObjectEntrySet()) {
            IFluidNode producer = e.getKey();
            int outputAmount = Math.min(producer.getOutputPressure(), producer.getCapacity());

            for (FluidConsumer consumer : e.getValue()) {

                Object stack = producer.extract(outputAmount, true);
                if (stack == null || !consumer.canHold(stack)) {
                    continue;
                }

                outputAmount = producer.getAmount(stack);
                Object fluid = producer.getFluid(stack);
                int temperature = producer.getTemperature(fluid);
                boolean isGaseous = producer.isGaseous(fluid);

                int amount = consumer.insert(stack, true);
                if (amount <= 0) {
                    continue;
                }

                Object drained = producer.extract(amount, false);
                assert drained != null;

                // If we are here, then path had some invalid pipes which not suits the limits of temp/pressure/gas
                if (!consumer.canHandle(temperature, amount, isGaseous) && consumer.getConnection() != ConnectionType.ADJACENT) { // Fast check by the lowest pipe cable
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
                        IFluidPipe pipe = p.getValue();
                        long pos = p.getLongKey();

                        FluidAbsorber a = (FluidAbsorber) absorbs.get(pos);
                        if (a == null) {
                            absorbs.put(pos, new FluidAbsorber(pipe.getCapacity(), pipe.getPressure(), amount, fluid));
                        } else {
                            a.add(amount, fluid);
                        }
                    }
                }

                consumer.insert(drained, false);

                outputAmount -= amount;
                if (outputAmount <= 0)
                    break;
            }
        }

        for (Long2ObjectMap.Entry<?> e : absorbs.long2ObjectEntrySet()) {
            FluidAbsorber absorber = (FluidAbsorber) e.getValue();
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
        assert (group instanceof Group<?, ?>);
        return new FluidController(dim, (Group<IFluidPipe, IFluidNode>) group);
    }

    @Override
    protected boolean isValid(@Nonnull IFluidNode producer, @Nullable Dir direction) {
        return direction != null ? producer.canOutput(direction) : producer.canOutput() && producer.getOutputPressure() > 0;
    }
}
