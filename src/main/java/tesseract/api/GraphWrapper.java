package tesseract.api;

import tesseract.api.electric.IElectricCable;
import tesseract.api.electric.IElectricNode;
import tesseract.graph.Graph;

public class GraphWrapper {

    protected Graph<IElectricCable, IElectricNode> graph;
    protected long position;

    public GraphWrapper(Graph<IElectricCable, IElectricNode> graph, long position) {
        this.graph = graph;
        this.position = position;
    }

    public void update() {
        //NOOP
    }

    public void remove() {
        graph.remove(position);
    }
}
