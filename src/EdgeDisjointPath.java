import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by cjk98 on 11/25/2017.
 * The main source code file for the Term Project
 */
public class EdgeDisjointPath {
    private Graph<Integer, CustomEdge> mg = new DirectedSparseGraph<>();
    private int maxNodeID = Integer.MIN_VALUE;

    EdgeDisjointPath (LinkedList<Integer[]> rawGraph) {
        mg = createGraph(rawGraph);
    }

    public Graph<Integer, CustomEdge> createGraph(LinkedList<Integer[]> rawGraph) {
        Graph<Integer, CustomEdge> cg = new DirectedSparseGraph<>();
        for (Integer[] item : rawGraph) {
            if (Math.max(item[0], item[1]) > maxNodeID)
                maxNodeID = Math.max(item[0], item[1]);
            CustomEdge edge = new CustomEdge(item[0], item[1], item[2]);
            cg.addVertex(item[0]);
            cg.addVertex(item[1]);
            cg.addEdge(edge, item[0], item[1], EdgeType.DIRECTED);
        }
        return cg;
    }

    public ArrayList<LinkedList<Integer>> findEdjPaths(Graph<Integer, CustomEdge> tg, int s, int t) {
        if (tg == null)
            tg = mg;

        ArrayList<LinkedList<Integer>> pathSet = new ArrayList<>();
        LinkedList<Integer> tempPath;
        while ((tempPath = findAPath(tg, s, t)).size() != 0) {
            pathSet.add(tempPath);
            Integer[] pathArray = tempPath.toArray(new Integer[tempPath.size()]);
            for (int i = 0; i < pathArray.length - 1; ++i) {
                tg.removeEdge(tg.findEdge(pathArray[i], pathArray[i+1]));
            }
        }
        return pathSet;
    }

    private LinkedList<Integer> findAPath(Graph<Integer, CustomEdge> tg, int s, int t) {
        boolean visited[] = new boolean[Math.max(tg.getVertexCount(), maxNodeID)];
        LinkedList<Integer> path = new LinkedList<>();

        dfsSearch(tg, s, t, visited, path);
        if (path.size() == 0)
            System.out.println("There is no more path from " + s + " to " + t);
        return path;
    }

    private Boolean dfsSearch(Graph<Integer, CustomEdge> tg, int curr, int t, boolean[] visited, LinkedList<Integer> path) {
        // assume this node is in the path first
        visited[curr-1] = true;
        path.add(curr);

        if (curr == t) {
//            for (int i: path)
//                System.out.printf(String.format("%d ", i));
//            System.out.printf("\n");
            return true;
        }
        else {
            for (CustomEdge e: tg.getOutEdges(curr)){
                int nextNode = e.getTargetNode();
                if (!visited[nextNode-1]) {
                    if (dfsSearch(tg, nextNode, t, visited, path)) return true;
                }
            }
//            visited[curr] = false;
            // jump to here if there's no out edges or finish recursion
            path.removeLast();
            return false;
        }
    }

    public static void main(String[] args){
        CSVParser cp = new CSVParser("F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\Data\\input_sample.csv");
        LinkedList<Integer[]> rawGraph = cp.parse();
        EdgeDisjointPath edp = new EdgeDisjointPath(rawGraph);
        ArrayList<LinkedList<Integer>> hlPaths = edp.findEdjPaths(null, 4, 6);
//        for (LinkedList<Integer> ll: hlPaths) {
//            for (Integer i: ll)
//                System.out.print(i + " ");
//            System.out.println();
//        }
        GraphVisualizer gv = new GraphVisualizer();
        Graph<Integer, CustomEdge> g = gv.createGraph(rawGraph);
        gv.colorEdges(g, hlPaths);

        gv.drawGraph(null);
        gv.saveGraph(null, "F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\output.png");
    }
}
