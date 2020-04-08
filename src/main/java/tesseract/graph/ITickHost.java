package tesseract.graph;

public interface ITickHost {
    void reset(ITickingController oldController, ITickingController newController);
}
