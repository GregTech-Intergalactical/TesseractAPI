package tesseract.api;

public interface IRefreshable {
    /**
     * Used to refresh this node in the network, in the case of updated sides.
     */
    default void refreshNet() {

    }
}
