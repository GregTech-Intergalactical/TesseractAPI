package tesseract.graph;

import javax.annotation.Nonnull;

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
    @Nonnull
    ITickingController clone(@Nonnull INode group);

    /**
     * @return To get simple things like a some information.
     */
    @Nonnull
    String[] getInfo();
}
