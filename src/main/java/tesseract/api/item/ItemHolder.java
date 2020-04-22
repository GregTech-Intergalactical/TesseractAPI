package tesseract.api.item;

/**
 * A class that acts a holder of the packets that has passed thought cables.
 */
public class ItemHolder {

    private int capacity;

    /**
     * Creates instance of the holder.
     *
     * @param capacity The maximum capacity.
     */
    protected ItemHolder(int capacity) {
        this.capacity = capacity;
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