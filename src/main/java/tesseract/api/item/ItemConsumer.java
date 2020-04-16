package tesseract.api.item;

import tesseract.api.Consumer;
import tesseract.graph.Path;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a item consumer.
 */
public final class ItemConsumer extends Consumer<IItemPipe, IItemNode> {

    /**
     * Creates instance of the consumer.
     *
     * @param consumer The consumer node.
     * @param path     The path information.
     */
    protected ItemConsumer(@Nonnull IItemNode consumer, @Nullable Path<IItemPipe> path) {
        super(consumer, path);
    }

    @Override
    protected void onConnectorCatch(@Nonnull IItemPipe connector) {

    }
}
