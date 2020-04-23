package tesseract.api.item;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import org.apache.commons.collections4.SetUtils;
import tesseract.api.Producer;
import tesseract.util.Dir;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * A class that acts as a container for a item producer.
 */
public class ItemProducer extends Producer<IItemNode> {

    //private Int2ObjectMap<ObjectSet<?>> filter = new Int2ObjectLinkedOpenHashMap<>(6);

    /**
     * Creates instance of the producer.
     *
     * @param producer The producer node.
     */
    protected ItemProducer(@Nonnull IItemNode producer) {
        super(producer);
        /*for (Dir direction : Dir.VALUES) {
            filter.put(direction.getIndex(), producer.getOutputFilter(direction));
        }*/
    }

    /**
     * Extracts an item from an available slot.
     * @param slot The slot to extract from.
     * @param amount Amount to extract (may be greater than the current stack's max limit)
     * @param simulate If true, the extraction is only simulated
     * @return ItemData extracted from the slot, must be null if nothing can be extracted.
     *         The returned ItemData can be safely modified after, so item handlers should return a new or copied stack.
     **/
    @Nullable
    public ItemData extract(int slot, int amount, boolean simulate) {
        return producer.extract(slot, amount, simulate);
    }

    /**
     * @return Gets all available slots.
     **/
    @Nonnull
    public IntList getAvailableSlots() {
        return producer.getAvailableSlots();
    }

    /**
     * @param slot The slot index.
     * @return It returns True if the slot has no items else it returns false.
     */
    public boolean isEmpty(int slot) {
        return producer.isEmpty(slot);
    }

    /**
     * @return Gets the initial amount of items that can be output.
     */
    public int getOutputAmount() {
        return producer.getOutputAmount();
    }

    @Override
    protected boolean canOutput(@Nullable Dir direction) {
        return direction != null ? producer.canOutput(direction) : producer.canOutput() && producer.getOutputAmount() > 0;
    }
}
