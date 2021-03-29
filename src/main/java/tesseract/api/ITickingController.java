package tesseract.api;

import tesseract.graph.INode;
import tesseract.util.Dir;
import tesseract.util.Pos;

/**
 * Interface abstracting ticking behaviour for the groups in the graph.
 */
public interface ITickingController<T, C extends IConnectable, N extends IConnectable> {

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
    ITickingController<T, C, N> clone(INode group);

    /**
     * @return To get simple things like a some information.
     */
    String[] getInfo();

    /**
     * Core method of tesseract. Inserts an object into this pipe.
     * @param producerPos position of producer (pipe).
     * @param direction direction inserted into pipe.
     * @param stack the object inserted.
     * @param simulate to simulate insertion.
     * @return controller-sensitive insertion information(amount inserted).
     */
    int insert(Pos producerPos, Dir direction, T stack, boolean simulate);
}
