package tesseract.api;

/**
 * Represents a tile entity providing server ticks for the group controller
 */
public interface ITickHost {

    /**
     * Set new controller pointer (or null).
     * If the host already contains new non-null controller, then don't reset it to null.
     * @param oldController The previous controller node.
     * @param newController The new controller node.
     */
    void reset(ITickingController oldController, ITickingController newController);
}
