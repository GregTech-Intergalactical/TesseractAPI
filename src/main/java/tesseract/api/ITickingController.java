package tesseract.api;

import tesseract.graph.INode;

/**
 * Interface abstracting ticking behaviour for the groups in the graph.
 */
public interface ITickingController {

    /**
     * Server tick handler.
     */
    void tick();

    /**
     * Notify the controller about group structure change.
     */
    void change();

    /**
     * Creates new controller for split group.
     * @param group New group.
     * @return New controller for the group.
     */
    ITickingController clone(INode group);

    /**
     * @return To get simple things like a some information.
     */
    String[] getInfo();
}
