package tesseract.api.electric;

import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectList;

/**
 * A class that acts as a container for a producer.
 */
public class ElectricProducer {

    private int it;
    private long voltage;
    private long amperage;
    private IElectricNode producer;
    private ElectricConsumer consumer;
    private ObjectIterator<IElectricNode> producers;
    private ObjectList<ElectricConsumer> consumers;
    private Object2ObjectMap<IElectricNode, ObjectList<ElectricConsumer>> controller;

    /**
     * Creates instance of the producer.
     *
     * @param controller The controller map.
     */
    protected ElectricProducer(Object2ObjectMap<IElectricNode, ObjectList<ElectricConsumer>> controller) {
        this.controller = controller;
        this.producers = controller.keySet().iterator();
    }

    /**
     * Moves to the next available producer.
     * @return  Returns true if producers available.
     */
    public boolean hasNext() {
        while (!isValid()) {
            if (!producers.hasNext()) return false;
            producer = producers.next();
            voltage = producer.getOutputVoltage();
            amperage = producer.getOutputAmperage();
            consumers = controller.get(producer);
            consumer = null; // Reset here for next()
        }
        return true;
    }

    /**
     * @return Gets the next needed consumer at index.
     */
    public ElectricConsumer next() {
        while (consumer == null || !consumer.isValid()) {
            if (consumers.size() == it) return null;
            consumer = consumers.get(it);
            it++; // As as same iterator for producers
        }
        return consumer;
    }

    /**
     * @param amperage The amperage of the consumer.
     * @return Gets the amperage required for the producer.
     */
    public long updateAmperage(long amperage) {
        long temp = this.amperage - amperage;
        if (temp < 0) {
            amperage = this.amperage;
            this.amperage = 0;
            it--; // Move back, we run out of amps for that producer
        } else {
            this.amperage = temp;
        }
        return amperage;
    }

    /**
     * @return Gets the producer voltage.
     */
    public long getVoltage() {
        return voltage;
    }

    /**
     * Removes energy from the node. Returns quantity of energy that was removed.
     * @param maxExtract Maximum amount of energy to be extracted.
     * @param simulate If true, the extraction will only be simulated.
     */
    public void extract(long maxExtract, boolean simulate) {
        producer.insert(maxExtract, simulate);
    }

    /**
     * @return Checks that provider can supply energy.
     */
    public boolean isValid() {
        return producer != null && producer.getPower() > 0 && amperage > 0;
    }
}
