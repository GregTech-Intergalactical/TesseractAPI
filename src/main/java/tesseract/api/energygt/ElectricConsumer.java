package tesseract.api.energygt;

import tesseract.api.Consumer;
import tesseract.graph.Path;

/**
 * A class that acts as a container for an electrical consumer.
 */
public class ElectricConsumer extends Consumer<IElectricCable, IElectricNode> {

    private int loss;
    private int minVoltage = Integer.MAX_VALUE;
    private int minAmperage = Integer.MAX_VALUE;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     */
    protected ElectricConsumer(IElectricNode consumer, Path<IElectricCable> path) {
        super(consumer, path);
    }

    /**
     * Adds energy to the node. Returns quantity of energy that was accepted.
     *
     * @param maxReceive Amount of energy to be inserted.
     * @param simulate If true, the insertion will only be simulated.
     */
    public void insert(long maxReceive, boolean simulate) {
        node.insert(maxReceive, simulate);
    }

    /**
     * @return Gets the amperage required for the consumer.
     */
    public int getRequiredAmperage(int voltage) {
        return voltage > loss ? (int) Math.min(((node.getCapacity() - node.getEnergy())) / (voltage - loss), node.getInputAmperage()) : 0;
    }

    /**
     * @return Gets the total loss for the given consumer.
     */
    public int getLoss() {
        return loss;
    }

    /**
     * @param voltage The current voltage.
     * @param amperage The current amperage.
     * @return Checks that the consumer is able to receive energy.
     */
    public boolean canHandle(int voltage, int amperage) {
        return minVoltage >= voltage && minAmperage >= amperage;
    }

    /**
     * Copy the data from another consumer instance.
     *
     * @param consumer An another consumer.
     */
    public void copy(ElectricConsumer consumer) {
        loss = consumer.loss;
        full = consumer.full;
        cross = consumer.cross;
    }

    @Override
    protected void onConnectorCatch(IElectricCable cable) {
        loss += cable.getLoss();
        minVoltage = Math.min(minVoltage, cable.getVoltage());
        minAmperage = Math.min(minAmperage, cable.getAmps());
    }
}
