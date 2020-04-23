package tesseract.api.fluid;

import tesseract.api.Producer;

import javax.annotation.Nonnull;

/**
 * A class that acts as a container for a fluid producer.
 */
public class FluidProducer extends Producer<IFluidNode> {

    /**
     * Creates instance of the producer.
     *
     * @param producer The producer node.
     */
    protected FluidProducer(@Nonnull IFluidNode producer) {
        super(producer);
    }
}
