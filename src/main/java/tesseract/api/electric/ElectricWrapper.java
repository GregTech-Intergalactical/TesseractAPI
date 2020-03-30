package tesseract.api.electric;

import tesseract.graph.Graph;

public class ElectricWrapper {

    protected Graph<IElectricCable, IElectricNode> graph;
    protected long position;

    public ElectricWrapper(Graph<IElectricCable, IElectricNode> graph, long position) {
        this.graph = graph;
        this.position = position;
    }

    public void update() {
        //NOOP
    }

    public void remove() {
        graph.removeAt(position);
    }
}
