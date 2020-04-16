package tesseract.api;

/**
 * A class that acts as absorber of the data that has passed thought connectors.
 */
public class Absorber {

    protected int value;
    protected final int max;

    /**
     * Creates instance of the absorber.
     *
     * @param max The maximum value.
     * @param value The initial value.
     */
    public Absorber(int max, int value) {
        this.max = max;
        this.value = value;
    }

    /**
     * Adds a new value.
     *
     * @param value The added value.
     */
    public void add(int value) {
        this.value += value;
    }

    /**
     * @return Gets a current value.
     */
    public int get() {
        return value;
    }

    /**
     * @return Checks that the absorber is not able to handle it.
     */
    public boolean isOver() {
        return max < value;
    }
}
