package tesseract.api.gt;

import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import tesseract.api.Consumer;
import tesseract.graph.Path;

import java.util.Comparator;

import static java.lang.Integer.compare;

/**
 * A class that acts as a container for an electrical consumer.
 */
public class GTConsumer extends Consumer<IGTCable, IGTNode> {

    private double loss;
    private long minVoltage = Integer.MAX_VALUE;
    private int minAmperage = Integer.MAX_VALUE;

    // Way of the sorting by the loss and the distance to the node
    public static final Comparator<GTConsumer> COMPARATOR = (t1, t2) -> (t1.getDistance() == t2.getDistance()) ? Double.compare(t1.getLoss(), t2.getLoss()) : compare(t1.getDistance(), t2.getDistance());

    public final LongSet uninsulatedCables = new LongOpenHashSet();

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path     The path information.
     */
    public GTConsumer(IGTNode consumer,IGTNode producer, Path<IGTCable> path) {
        super(consumer,producer, path);
        init();
    }

    /**
     * @return Gets the amperage required for the consumer.
     */
    public long getRequiredAmperage(long voltage) {
        return node.availableAmpsInput(voltage);//Math.min(((node.getCapacity() - node.getEnergy())) / voltage, node.getInputAmperage());
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
    public double getLoss() {
        return loss;
    }

    /**
     * @param voltage The current voltage.
     * @return Checks that the consumer is able to receive energy.
     */
    public boolean canHandle(long voltage) {
        return minVoltage >= voltage;
    }

    public boolean canHandleAmp(long minAmperage) {
        return this.minAmperage >= minAmperage;
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
    protected void onConnectorCatch(long pos, IGTCable cable) {
        loss += cable.getLoss();
        minVoltage = Math.min(minVoltage, cable.getVoltage());
        minAmperage = Math.min(minAmperage, cable.getAmps());
        if (!cable.insulated()) {
            this.uninsulatedCables.add(pos);
        }
    }

    public static class State {
        long ampsReceived;
        long ampsSent;
        long euReceived;
        long euSent;
        public final IGTNode handler;

        public State(IGTNode handler) {
            ampsReceived = 0;
            euReceived = 0;
            this.handler = handler;
        }

        public void onTick() {
            ampsReceived = 0;
            euReceived = 0;
            ampsSent = 0;
            euSent = 0;
        }

        public long extract(boolean simulate, long amps) {
            if (handler.canOutput()) {
                if (simulate) {
                    return Math.min(amps, handler.getOutputAmperage() - (ampsSent));
                }
                if (ampsSent + amps > handler.getOutputAmperage()) {
                    return 0;
                }
                if (!simulate) {
                    ampsSent += amps;
                }
                return amps;
            }
            return 0;
        }

        public long receive(boolean simulate, long amps) {
            if (handler.canInput()) {
                if (simulate) {
                    return Math.min(amps, handler.getInputAmperage() - (ampsReceived));
                }
                if (ampsReceived + amps > handler.getInputAmperage()) {
                    return 0;
                }
                if (!simulate) {
                    ampsReceived += amps;
                }
                return amps;
            }
            return 0;
        }

        public long getAmpsReceived() {
            return ampsReceived;
        }

        public long getAmpsSent() {
            return ampsSent;
        }
    }
}
