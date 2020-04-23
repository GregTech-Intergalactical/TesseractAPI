package tesseract.api.item;

import tesseract.api.Producer;

import javax.annotation.Nonnull;

/**
 * A class that acts as a container for a item producer.
 */
public class ItemProducer extends Producer<IItemNode> {

    /**
     * Creates instance of the producer.
     *
     * @param producer The producer node.
     */
    protected ItemProducer(@Nonnull IItemNode producer) {
        super(producer);
    }


}
