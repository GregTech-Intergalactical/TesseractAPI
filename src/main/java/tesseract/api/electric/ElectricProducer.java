package tesseract.api.electric;

import tesseract.api.Producer;

import javax.annotation.Nonnull;
/**
 * A class that acts as a container for a electrical producer.
 */
public class ElectricProducer extends Producer<IElectricNode> {

    /**
     * Creates instance of the producer.
     *
     * @param producer The producer node.
     */
    protected ElectricProducer(@Nonnull IElectricNode producer) {
        super(producer);
    }
}

