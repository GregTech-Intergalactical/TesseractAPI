package tesseract.api.item;

/**
 * A class that acts a holder of the packets that has passed thought pipes.
 */
public class ItemHolder {

    private int capacity; // current capacity for a pipe

    /**
     * Creates instance of the holder.
     *
     * @param pipe The pipe connector.
     */
    protected ItemHolder(IItemPipe pipe) {
        this.capacity = pipe.getCapacity();
    }

    /**
     * Reduces an available capacity.
     *
     * @param capacity The added capacity.
     */
    public void reduce(int capacity) {
        this.capacity -= capacity;
        if (this.capacity < 0) {
            this.capacity = 0;
        }
    }

    /**
     * @return Gets the capacity of the holder.
     */
    public int getCapacity() {
        return capacity;
    }
}