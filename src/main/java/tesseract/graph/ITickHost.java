package tesseract.graph;
import javax.annotation.Nullable;

/**
 * Represents a tile entity providing server ticks for the group controller
 */
public interface ITickHost {
    /**
     * Set new controller pointer (or null)
     * If the host already contains new non-null controller, then don't reset it to null
     * @param oldController
     * @param newController
     */
    void reset(@Nullable ITickingController oldController, @Nullable ITickingController newController);
}
