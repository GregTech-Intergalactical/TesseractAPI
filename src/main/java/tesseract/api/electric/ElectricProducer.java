package tesseract.api.electric;

import tesseract.api.Producer;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a electrical producer.
 */
public class ElectricProducer extends Producer<IElectricNode> {

    /**
     * Creates instance of the producer.
     *
     * @param producer The producer node.
     */
    protected ElectricProducer(@Nonnull IElectricNode producer) {
        super(producer);
    }

    /**
     * Removes energy from the node. Returns quantity of energy that was removed.
     * @param maxExtract Maximum amount of energy to be extracted.
     * @param simulate If true, the extraction will only be simulated.
     * @return Amount of energy that was (or would have been, if simulated) extracted from the storage.
     */
    public long extract(long maxExtract, boolean simulate) {
        return producer.extract(maxExtract, simulate);
    }

    /**
     * @return Gets the maximum amount of voltage that can be output.
     */
    public int getOutputVoltage() {
        return producer.getOutputVoltage();
    }

    /**
     * @return Gets the maximum amount of amperage that can be output.
     */
    public int getOutputAmperage() {
        return producer.getOutputAmperage();
    }

    @Override
    protected boolean canOutput(@Nullable Dir direction) {
        return direction != null ? producer.canOutput(direction) : producer.canOutput() && producer.getOutputVoltage() > 0;
    }
}

