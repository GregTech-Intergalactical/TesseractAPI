package tesseract.electric;

import it.unimi.dsi.fastutil.objects.ObjectList;
import tesseract.electric.api.IElectricCable;
import tesseract.electric.api.IElectricNode;
import tesseract.graph.Path;

/**
 * The Consumer is a class that should wrap consumer data.
 */
public class Consumer {

    private long loss;
    private IElectricNode node;
    //private ObjectList<IElectricCable> cables;

    /**
     * Creates instance of consumer.
     *
     * @param node The consumer node.
     * @param path The path information.
     */
    protected Consumer(IElectricNode node, Path<IElectricCable> path) {
        this.node = node;
        //this.cables = path.getCross();
        for (IElectricCable cable : path.getFull()) {
            this.loss += cable.getLoss();
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
     * @return Checks that consumer is able to receive energy.
     */
    public boolean isValid() {
        return node.getPower() < node.getCapacity() && loss < node.getInputVoltage();
    }

    /**
     * @return Gets the consumer node.
     */
    public IElectricNode getNode() {
        return node;
    }
}
