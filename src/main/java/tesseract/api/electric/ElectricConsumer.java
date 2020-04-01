package tesseract.api.electric;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tesseract.api.ConnectionType;
import tesseract.graph.Path;

/**
 * A class that acts as a container for a consumer.
 */
public class ElectricConsumer {

    private long loss;
    public long impulse;
    private long voltage = Long.MAX_VALUE;
    private long amperage = Long.MAX_VALUE;
    private IElectricNode consumer;
    private Long2ObjectMap<IElectricCable> full;
    private Long2ObjectMap<IElectricCable> cross;

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     */
    protected ElectricConsumer(IElectricNode consumer, Path<IElectricCable> path) {
        this.consumer = consumer;
        this.full = path.getFull();
        this.cross = path.getCross();

        for (IElectricCable cable : full.values()) {
            loss += cable.getLoss();
            voltage = Math.min(voltage, cable.getVoltage());
            amperage = Math.min(amperage, cable.getAmps());
        }
    }

    /**
     * Adds energy to the node. Returns quantity of energy that was accepted.
     * @param maxReceive Maximum amount of energy to be inserted.
     * @param simulate If true, the insertion will only be simulated.
     */
    public void insert(long maxReceive, boolean simulate) {
        consumer.insert(maxReceive, simulate);
    }

    /**
     * @param crossroad If true will return crossroad cables, false to get full amount.
     * @return Gets the cables list.
     */
    public Long2ObjectMap<IElectricCable> getCables(boolean crossroad) {
        return crossroad ? cross : full;
    }

    /**
     * @return Gets the cables loss.
     */
    public long getLoss() {
        return loss;
    }

    /**
     * Sets the impulse voltage.
     * @param impulse The voltage with loss.
     */
    public void setVoltage(long impulse) {
        this.impulse = impulse;
    }

    /**
     * @return Gets the impulse voltage.
     */
    public long getVoltage() {
        return impulse;
    }

    /**
     * @return Gets the amperage required for the consumer.
     */
    public long getRequiredAmperage() {
        return Math.min(((consumer.getCapacity() - consumer.getPower()) + impulse - 1) / impulse, consumer.getInputAmperage());
    }

    /**
     * @return Gets the consumer connection type.
     */
    public ConnectionType getConnectionType() {
        if (cross.size() == 0) {
            if (full.size() == 2) return ConnectionType.ADJACENT;
            else if (full.size() > 2) return ConnectionType.SINGLE;
            else return ConnectionType.INVALID; // How it possible ???
        }
        return ConnectionType.VARIATE;
    }

    /**
     * @param voltage The current voltage.
     * @param amperage The current amperage.
     * @return Checks that consumer is able to receive energy.
     */
    public boolean canReceive(long voltage, long amperage) {
        return this.voltage >= voltage && this.amperage >= amperage;
    }

    /**
     * @return Checks that consumer is need energy.
     */
    public boolean isValid() {
        return consumer.getPower() < consumer.getCapacity() && loss < consumer.getInputVoltage();
    }
}
