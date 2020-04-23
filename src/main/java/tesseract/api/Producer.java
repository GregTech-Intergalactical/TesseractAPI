package tesseract.api;

import tesseract.graph.IConnectable;

import javax.annotation.Nonnull;

/**
 * A class that acts as a container for a producer component.
 */
public class Producer<N extends IConnectable> {

    protected final N producer;

    /**
     * Creates instance of the producer.
     *
     * @param producer The producer node.
     */
    protected Producer(@Nonnull N producer) {
        this.producer = producer;
    }

    /**
     * @return Gets the producer ref.
     */
    @Nonnull
    public N getProducer() {
        return producer;
    }
}
