package tesseract.electric;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.Path;

/**
 * A class that acts as a container for a consumer.
 */
public class Consumer {

    private long loss;
    private long voltage;
    private long amperage;
    private IElectricNode node;
    private Long2ObjectMap<IElectricCable> full;
    private Long2ObjectMap<IElectricCable> cross;

    /**
     * Creates instance of the consumer.
     *
     * @param node The consumer node.
     * @param path The path information.
     */
    protected Consumer(IElectricNode node, Path<IElectricCable> path) {
        this.node = node;
        this.full = path.getFull();
        this.cross = path.getCross();

        for (IElectricCable cable : full.values()) {
            loss += cable.getLoss();
            voltage = Math.min(voltage, cable.getVoltage());
            amperage = Math.min(amperage, cable.getAmps());
        }
    }

    /**
     * @param voltage The producer voltage.
     * @return Gets the packet required for consumer.
     */
    public Packet getEnergyRequired(long voltage) {
        double required = node.getCapacity() - node.getPower();
        double energy = voltage - loss;
        double packs = Math.ceil(required / energy);
        double amperage = Math.min(packs, node.getInputAmperage());
        return new Packet(energy, voltage, amperage);
    }

    /**
     * Adds energy to the node. Returns quantity of energy that was accepted.
     * @param maxReceive Maximum amount of energy to be inserted.
     * @param simulate If true, the insertion will only be simulated.
     */
    public void insert(long maxReceive, boolean simulate) {
        node.insert(maxReceive, simulate);
    }

    /**
     * @param crossroad If true will return crossroad cables, false to get full amount.
     * @return Gets the cables list.
     */
    public Long2ObjectMap<IElectricCable> getCables(boolean crossroad) {
        return crossroad ? cross : full;
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
     * @return Checks that consumer is able to receive energy.
     */
    public boolean canReceive(Packet packet) {
        // Fast check by the lowest cost cable
        return amperage >= packet.getAmps() || voltage * amperage >= packet.getSend();
    }

    /**
     * @return Checks that consumer is need energy.
     */
    public boolean isValid() {
        return node.getPower() < node.getCapacity() && loss < node.getInputVoltage();
    }
}
