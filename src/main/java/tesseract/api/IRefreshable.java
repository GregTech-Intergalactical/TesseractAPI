package tesseract.api;

public interface IRefreshable {
    /**
     * Used to refresh this node in the network, in the case of updated sides.
     */
    default void refreshNet() {
        deregisterNet();
        registerNet();
    }
    /**
     * Used to register this node to the net.
     */
    default void registerNet() {

    }
    /**
     * Used to deregister this node from the net.
     */
    default void deregisterNet() {

    }
}
