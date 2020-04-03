package tesseract.graph;

/**
 * Simple interface for a updates.
 */
public interface IController {

    /**
     * Change callback method.
     * @param primary True when node is a first in the grid.
     */
    void change(boolean primary);
}
