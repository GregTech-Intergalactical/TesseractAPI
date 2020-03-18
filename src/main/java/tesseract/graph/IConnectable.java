package tesseract.graph;

import tesseract.util.Dir;

/**
 *
 */
public interface IConnectable {

    /**
     *
     * @param direction
     * @return
     */
    boolean connects(Dir direction);
}
