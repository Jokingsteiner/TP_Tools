import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Created by cjk98 on 11/25/2017.
 * The main source code file for the Term Project
 */
public class EdgeDisjointPath {
    private Graph<Integer, CustomEdge> mg = new DirectedSparseGraph<>();
    private int maxNodeID = Integer.MIN_VALUE;
    private int minNodeID = Integer.MAX_VALUE;
    LinkedList<Integer[]> rawGraph = new LinkedList<>();

    EdgeDisjointPath (LinkedList<Integer[]> rawGraph) {
        this.rawGraph = rawGraph;
        mg = createGraph(rawGraph);
    }

    public Graph<Integer, CustomEdge> createGraph(LinkedList<Integer[]> rawGraph) {
        Graph<Integer, CustomEdge> cg = new DirectedSparseGraph<>();
        for (Integer[] item : rawGraph) {
            if (Math.max(item[0], item[1]) > maxNodeID)
                maxNodeID = Math.max(item[0], item[1]);
            if (Math.min(item[0], item[1]) < minNodeID)
                minNodeID = Math.min(item[0], item[1]);
            CustomEdge edge = new CustomEdge(item[0], item[1], item[2]);
            cg.addVertex(item[0]);
            cg.addVertex(item[1]);
            cg.addEdge(edge, item[0], item[1], EdgeType.DIRECTED);
        }
        return cg;
    }

    public ArrayList<LinkedList<Integer>> findEdjPaths(Graph<Integer, CustomEdge> tg, Integer s, Integer t) {
        if (tg == null)
            tg = createGraph(rawGraph);
        if (s == null)
            s = minNodeID;
        if (t == null)
            t = maxNodeID;

        ArrayList<LinkedList<Integer>> pathSet = new ArrayList<>();
        LinkedList<Integer> tempPath;
        while ((tempPath = findAPath(tg, s, t)).size() != 0) {
            pathSet.add(tempPath);
            Integer[] pathArray = tempPath.toArray(new Integer[tempPath.size()]);
            for (int i = 0; i < pathArray.length - 1; ++i) {
                CustomEdge fe = tg.findEdge(pathArray[i], pathArray[i+1]);
                tg.removeEdge(fe);
//                tg.addEdge(new CustomEdge(pathArray[i+1], pathArray[i], fe.getWeight()), pathArray[i+1], pathArray[i], EdgeType.DIRECTED);
            }
        }
        return pathSet;
    }

    public ArrayList<LinkedList<Integer>> findMaxEdjPaths(Graph<Integer, CustomEdge> tg, Integer s, Integer t) {
        //  Modified Ford-Fulkerson Algorithm
        if (tg == null)
            tg = createGraph(rawGraph);

        if (s == null)
            s = minNodeID;
        if (t == null)
            t = maxNodeID;

        int maxflow = 0;
        Graph<Integer, CustomEdge> rg = createGraph(rawGraph);
        HashMap<Pair<Integer, Integer>, Boolean> conflictEdges = new HashMap<>();

        ArrayList<LinkedList<Integer>> pathSet = new ArrayList<>();
        LinkedList<Integer> tempPath;
        while ((tempPath = findAPath(rg, s, t)).size() != 0) {
            Integer[] pathArray = tempPath.toArray(new Integer[tempPath.size()]);
            // maybe there's a case all edges in residual graph are > 1, so pathFlow is necessary
            int pathFlow = Integer.MAX_VALUE;
            for (int i = 0; i < pathArray.length - 1; ++i) {
                CustomEdge e = rg.findEdge(pathArray[i], pathArray[i+1]);
                pathFlow = Math.min(pathFlow, e.getCapacity());
            }

            for (int i = 0; i < pathArray.length - 1; ++i) {
                // reduce capacity on the
                CustomEdge fe = rg.findEdge(pathArray[i], pathArray[i+1]);
                if (fe.getCapacity() - pathFlow > 0)
                    fe.setCapacity(fe.getCapacity() - pathFlow);
                else
                    rg.removeEdge(fe);

                // add capacity to the reversed edge
                CustomEdge re = rg.findEdge(pathArray[i+1], pathArray[i]);
                if (re != null)
                    re.setCapacity(re.getCapacity() + pathFlow);
                else
                    rg.addEdge(new CustomEdge(pathArray[i+1], pathArray[i], pathFlow), pathArray[i+1], pathArray[i], EdgeType.DIRECTED);

//                Pair<Integer, Integer> edgePair = new Pair<>(Math.min(pathArray[i], pathArray[i+1]), Math.max(pathArray[i], pathArray[i+1]));
                // if contains then assign true, else false;
//                conflictEdges.put(edgePair, conflictEdges.containsKey(edgePair));
            }
            maxflow += pathFlow;
//            pathSet.add(tempPath);
            solveConflict(tg, rg, pathSet, tempPath);
        }

        System.out.println("Maximum number of edge-disjoint path: " + maxflow);
//        for (Pair<Integer, Integer> p: conflictEdges.keySet())

        return pathSet;
    }

    private void solveConflict(Graph<Integer, CustomEdge> tg, Graph<Integer, CustomEdge> rg, ArrayList<LinkedList<Integer>> pathSet,  LinkedList<Integer> tempPath) {
        System.out.println("path in the set: ");
        for (LinkedList<Integer> ll: pathSet) {
            for (Integer i: ll)
                System.out.print(i + " ");
            System.out.println();
        }
        System.out.println("new path to add: ");
        for (Integer i: tempPath    )
            System.out.print(i + " ");
        System.out.println();
        boolean noConflict = true;
        for (int i = 0; i < tempPath.size() - 2; ++i) {
            Integer tempStart = tempPath.get(i);
            Integer tempEnd = tempPath.get(i+1);
            CustomEdge eInOrg = tg.findEdge(tempStart, tempEnd);
            // the edge not existed in original graph, so it is a push back in the residual graph
            if (eInOrg == null) {
                LinkedList<Integer> newPath1 = new LinkedList<>();
                LinkedList<Integer> newPath2 = new LinkedList<>();
                for (int j = 0; j < pathSet.size(); ++j) {
                    LinkedList<Integer> oldPath = pathSet.get(j);
                    int startIndex;
                    if ( (startIndex = oldPath.indexOf(tempEnd)) != -1) {
                        if (oldPath.get(startIndex+1).equals(tempStart)) {
                            noConflict = false;
                            pathSet.remove(j);
                            // construct P1 with original nodes
                            for (int victimIdx = 0; victimIdx <= startIndex; ++victimIdx)
                                newPath1.add(oldPath.get(victimIdx));

                            // concat P1 with new nodes
                            for (int tmp = i+2; tmp < tempPath.size(); ++tmp)
                                newPath1.add(tempPath.get(tmp));

                            // construct P2 with original nodes
                            for (int tmp = 0; tmp <= i; ++tmp)
                                newPath2.add(tempPath.get(tmp));

                            // concat P2 with new nodes
                            for (int victimIdx = startIndex+2; victimIdx < oldPath.size(); ++victimIdx)
                                newPath2.add(oldPath.get(victimIdx));
                            break;
                        }
                    }
                }
                pathSet.add(newPath2);
                solveConflict(tg, rg, pathSet, newPath1);
//                pathSet.add(newPath1);
                break;
            }
        }
        if (noConflict)
            pathSet.add(tempPath);
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
            return true;
        }
        else {
            for (CustomEdge e: tg.getOutEdges(curr)){
                int nextNode = e.getTargetNode();
                if (!visited[nextNode-1]) {
                    if (dfsSearch(tg, nextNode, t, visited, path)) return true;
                }
            }
            // jump to here if there's no out edges or finish recursion
            path.removeLast();
            return false;
        }
    }

    public static void main(String[] args){
        CSVParser cp = new CSVParser("F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\Data\\input_special2.csv");
        LinkedList<Integer[]> rawGraph = cp.parse();
        EdgeDisjointPath edp = new EdgeDisjointPath(rawGraph);
        ArrayList<LinkedList<Integer>> hlPaths = edp.findMaxEdjPaths(null, null, null);
        for (LinkedList<Integer> ll: hlPaths) {
            for (Integer i: ll)
                System.out.print(i + " ");
            System.out.println();
        }
        GraphVisualizer gv = new GraphVisualizer();
        Graph<Integer, CustomEdge> g = gv.createGraph(rawGraph);
        gv.colorEdges(g, hlPaths);

        gv.drawGraph(null);
        gv.saveGraph(null, "F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\output.png");
    }
}
