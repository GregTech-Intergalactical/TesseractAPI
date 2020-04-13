package tesseract.api.item;

/**
 * Interface for handling an item events. (Controller will handle them)
 */
public interface IItemEvent {

    /**
     * Executes when the cable trying to transport higher amount of items than can.
     * @param dim The dimension id.
     * @param pos The pipe position.
     */
    void onPipeOverCapacity(int dim, long pos);
}
