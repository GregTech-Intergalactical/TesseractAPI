package tesseract.graph.traverse;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import tesseract.util.Dir;
import tesseract.util.Pos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.function.Consumer;

public class AStarPathfinder {

    private INodeContainer container;

    private HashSet<Pos> open;
    private HashSet<Pos> closed;
    private HashMap<Pos, Pos> cameFrom;
    private Object2IntMap<Pos> gScore;
    private Object2IntMap<Pos> fScore;

    public AStarPathfinder(INodeContainer container) {
        this.container = container;

        open = new HashSet<>();
        closed = new HashSet<>();
        cameFrom = new HashMap<>();
        gScore = new Object2IntOpenHashMap<>();
        fScore = new Object2IntOpenHashMap<>();

        gScore.defaultReturnValue(Integer.MAX_VALUE);
        fScore.defaultReturnValue(Integer.MAX_VALUE);
    }

    private static int heuristic(Pos current, Pos end) {
        return Math.abs(current.getX() - end.getX()) + Math.abs(current.getY() - end.getY()) + Math.abs(current.getZ() - end.getZ());
    }

    // TODO: Verify that this works
    public void findPath(Pos start, Pos end, Consumer<Pos> fromEnd) {
        Pos current = new Pos(start);
        Pos neighbor = new Pos();

        open.add(start);
        fScore.put(start, heuristic(start, end));

        while (!current.equals(end)) {
            open.remove(current);
            closed.add(current);

            int currentGScore = gScore.getInt(current);

            for (Dir direction : Dir.VALUES) {
                neighbor.set(current).offset(direction);

                if (closed.contains(neighbor) || !container.linked(current, direction, neighbor)) {
                    continue;
                }

                int neighborGScore = currentGScore + 1;

                if (!open.contains(current)) {
                    // note: this allocates
                    open.add(current/*.toImmutable()*/);
                } else {
                    int existingGScore = gScore.getInt(current);

                    if (neighborGScore >= existingGScore) {
                        continue;
                    }
                }

                // note: this allocates
                Pos neighborImmutable = neighbor/*.toImmutable()*/;

                cameFrom.put(neighborImmutable, current/*.toImmutable()*/);
                gScore.put(neighborImmutable, neighborGScore);
                fScore.put(neighborImmutable, neighborGScore + heuristic(neighborImmutable, end));
            }

            current.set(findBestOpenNode());
        }


        open.clear();
        closed.clear();
        gScore.clear();
        fScore.clear();

        fromEnd.accept(end);

        while (!current.equals(start)) {
            Pos node = cameFrom.remove(current);
            current.set(node);

            fromEnd.accept(node);
        }

        cameFrom.clear();
    }

    private Pos findBestOpenNode() {
        Pos best = null;
        int bestScore = Integer.MAX_VALUE;

        for (Pos pos : open) {
            int score = fScore.getInt(pos);

            if (score < bestScore) {
                bestScore = score;
                best = pos;
            }
        }

        return best;
    }
}