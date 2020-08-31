package tesseract.api.fe;

/**
 * A class that acts a holder of the packets that has passed thought cables.
 */
public class FEHolder {

    private long capacity; // current capacity for a cable

    /**
     * Creates instance of the holder.
     *
     * @param cable The pipe connector.
     */
    protected FEHolder(IFECable cable) {
        this.capacity = cable.getCapacity();
    }

    /**
     * Reduces an available capacity.
     *
     * @param capacity The added capacity.
     */
    public void reduce(long capacity) {
        this.capacity -= capacity;
        if (this.capacity < 0L) {
            this.capacity = 0L;
        }
    }

    /**
     * @return Gets the capacity of the holder.
     */
    public long getCapacity() {
        return capacity;
    }
}