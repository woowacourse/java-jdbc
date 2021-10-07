package di.depdency.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AdjacentNodes<V> {

    private final List<V> adjacents;

    public AdjacentNodes() {
        this(new ArrayList<>());
    }

    public AdjacentNodes(List<V> adjacents) {
        this.adjacents = adjacents;
    }

    public boolean contains(V targetNode) {
        return adjacents.contains(targetNode);
    }

    public void add(V targetNode) {
        adjacents.add(targetNode);
    }

    public List<V> toList() {
        return Collections.unmodifiableList(adjacents);
    }
}
