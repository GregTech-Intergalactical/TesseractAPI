package tesseract.graph;

/**
 * Simple interface for a updates.
 */
public interface IController {

    /**
     * Executes when the grid is having any updates of any nodes/connectors inside.
     * @param primary True when node is a first in the grid.
     */
    void change(boolean primary);
}
