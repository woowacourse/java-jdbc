package di.depdency.graph;

import di.depdency.GraphCompositionFailureException;
import di.depdency.ImpossibleDependencyException;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class DependencyGraph<V> {

    private final Map<V, Integer> inDegrees = new HashMap<>();
    private final Map<V, AdjacentNodes<V>> neighbors = new HashMap<>();

    public DependencyGraph(Set<V> nodes) {
        for (V node : nodes) {
            this.inDegrees.put(node, 0);
            this.neighbors.put(node, new AdjacentNodes<>());
        }
    }

    public void connect(Set<V> froms, V to) {
        for (V from : froms) {
            connect(from, to);
        }
    }

    private void connect(V from, V to) {
        addNeighbors(from, to);
        increaseInDegree(to);
    }

    private void addNeighbors(V from, V to) {
        AdjacentNodes<V> adjacentNodes = neighbors.get(from);
        if (adjacentNodes.contains(to)) {
            String fromName = from.getClass().getSimpleName();
            String toName = to.getClass().getSimpleName();
            throw new GraphCompositionFailureException(
                    String.format("의존성 그래프 구성에 실패했습니다 (%s에 %s의 연관 관계가 이미 존재합니다)", fromName, toName)
            );
        }
        adjacentNodes.add(to);
    }

    private void increaseInDegree(V node) {
        inDegrees.put(node, inDegrees.get(node) + 1);
    }

    public List<V> orderByDependencyAsc() {
        List<V> orders = new ArrayList<>();
        Queue<V> q = new ArrayDeque<>();
        Map<V, Integer> inDegrees = new HashMap<>(this.inDegrees);

        for (Map.Entry<V, Integer> entry : inDegrees.entrySet()) {
            if (entry.getValue() == 0) {
                q.add(entry.getKey());
            }
        }

        while (!q.isEmpty()) {
            V currentNode = q.poll();
            orders.add(currentNode);

            List<V> adjacentNodes = neighbors.get(currentNode).toList();
            for (V nextNode : adjacentNodes) {
                int inDegree = inDegrees.get(nextNode);
                inDegrees.put(nextNode, inDegree - 1);
                if (inDegree == 1) {
                    q.add(nextNode);
                }
            }
        }

        for (Integer inDegree : inDegrees.values()) {
            if (inDegree > 0) {
                throw new ImpossibleDependencyException("의존성 그래프에 사이클이 존재합니다.");
            }
        }
        return orders;
    }
}
