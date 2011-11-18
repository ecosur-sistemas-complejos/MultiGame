package mx.ecosur.multigame.impl.util.manantiales;

import java.awt.Point;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * An adjacency graph as a bean.
 */
public class AdjGraph {

    private int E, V;

    private boolean[][] adj;

    private LinkedHashMap<Point,Integer> map;

    public AdjGraph(int V) {
        this.V = V;
        init();
    }

    private void init() {
        E = 0;
        adj = new boolean[V][V];
    }

    public void addEdge(int v, int w) {
        if (!adj[v][w]) E++;
        adj[v][w] = true;
        adj[w][v] = true;
    }

    public boolean containsEdge(int v, int w) {
        return (adj[v][w]);
    }

    public int findNode (Point point) {
        return map.get(point);
    }

    public Point findPoint(int node) {
        Point ret = null;
        for (Point p : map.keySet()) {
            Integer i = map.get(p);
            if (i.intValue() == node) {
                ret = p;
                break;
            }
        }
        return ret;
    }

    public void addPoint(int node, Point point) {
        if (map == null)
            map = new LinkedHashMap();
        map.put(point, node);
    }

    public int size() {
        return V;
    }

}
