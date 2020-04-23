package tesseract.api;

import jdk.internal.jline.internal.Nullable;
import tesseract.graph.IConnectable;
import tesseract.util.Dir;

import javax.annotation.Nonnull;

/**
 * A class that acts as a container for a producer component.
 */
abstract public class Producer<N extends IConnectable> {

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

    /**
     * Used to determine which sides can output item (if any).
     * Output cannot be used as input.
     * @param direction Direction to the out.
     * @return Returns true if the given direction is output side.
     */
    protected abstract boolean canOutput(@Nullable Dir direction);
}
