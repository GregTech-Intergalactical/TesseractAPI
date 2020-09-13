package tesseract.api.gt;

import tesseract.api.Consumer;
import tesseract.graph.Path;

import java.util.Comparator;

import static java.lang.Integer.compare;

/**
 * A class that acts as a container for an electrical consumer.
 */
public class GTConsumer extends Consumer<IGTCable, IGTNode> {

    private int loss;
    private int minVoltage = Integer.MAX_VALUE;
    private int minAmperage = Integer.MAX_VALUE;

    // Way of the sorting by the loss and the distance to the node
    public static final Comparator<GTConsumer> COMPARATOR = (t1, t2) -> (t1.getDistance() == t2.getDistance()) ? compare(t1.getLoss(), t2.getLoss()) : compare(t1.getDistance(), t2.getDistance());

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path The path information.
     */
    protected GTConsumer(IGTNode consumer, Path<IGTCable> path) {
        super(consumer, path);
        init();
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
        return (int) Math.min(((node.getCapacity() - node.getEnergy())) / voltage, node.getInputAmperage());
    }

    /**
     * @return Returns the priority of this node as a number.
     */
    public int getPriority() {
        return 0;
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
    public void copy(GTConsumer consumer) {
        loss = consumer.loss;
        full = consumer.full;
        cross = consumer.cross;
        minVoltage = consumer.minVoltage;
        minAmperage = consumer.minAmperage;
    }

    @Override
    protected void onConnectorCatch(IGTCable cable) {
        loss += cable.getLoss();
        minVoltage = Math.min(minVoltage, cable.getVoltage());
        minAmperage = Math.min(minAmperage, cable.getAmps());
    }
}
