import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;

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

    /**
     *  Solver for the Base Problem:
     *  Find edge-disjoint paths {P1, P2, …, Pn} from s to t.
    **/
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
            }
        }
        return pathSet;
    }

    private LinkedList<Integer> findAPath(Graph<Integer, CustomEdge> tg, int s, int t) {
        boolean visited[] = new boolean[Math.max(tg.getVertexCount(), maxNodeID)];
        LinkedList<Integer> path = new LinkedList<>();

        dfsSearch(tg, s, t, visited, path);
//        if (path.size() == 0)
//            System.out.println("There is no more path from " + s + " to " + t);
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

    /**
     *  Solver for the Variation1:
     *  Find two edge-disjoint paths from s to t and have minimum total cost.
     **/

    /**
     *  Solver for the Variation2:
     *  Find a set with maximum number of edge-disjoint paths.
     **/
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
            }

            maxflow += pathFlow;
            solveConflict(tg, rg, pathSet, tempPath);
//            drawServer.saveGraph(rg, "F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\residual\\" + ++count + ".png");
        }

        System.out.println("Maximum number of edge-disjoint path: " + maxflow);
        return pathSet;
    }

    private void solveConflict(Graph<Integer, CustomEdge> tg, Graph<Integer, CustomEdge> rg, ArrayList<LinkedList<Integer>> pathSet,  LinkedList<Integer> tempPath) {
        cancleLoop(tempPath);
//        System.out.println("path in the set: ");
//        for (LinkedList<Integer> ll: pathSet) {
//            for (Integer i: ll)
//                System.out.print(i + " ");
//            System.out.println();
//        }
//        System.out.println("new path to add: ");
//        for (Integer i: tempPath)
//            System.out.print(i + " ");
//        System.out.println();
        boolean noConflict = true;
        for (int i = 0; i < tempPath.size() - 2; ++i) {
            Integer tempStart = tempPath.get(i);
            Integer tempEnd = tempPath.get(i+1);
            CustomEdge eInOrg = tg.findEdge(tempStart, tempEnd);
            CustomEdge eInRes = rg.findEdge(tempStart, tempEnd);

            // the edge not existed in original graph, so it is a push back in the residual graph
            if (eInOrg == null || eInRes != null) {
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

    private LinkedList<Integer> cancleLoop(LinkedList<Integer> tempPath) {
        int removeStart, removeEnd;
        HashMap<Integer, Integer> checkSet = new HashMap<>();
        boolean noLoop = true;
//        LinkedList<Integer> result = new LinkedList<>();
        for (int i = 0; i < tempPath.size(); ++i) {
            if (checkSet.containsKey(tempPath.get(i))) {
                noLoop &= false;
                removeStart = checkSet.get(tempPath.get(i));
                removeEnd = i;
                int delNum = removeEnd - removeStart;
                for (int tmp = 0; tmp < delNum; ++tmp)
                    tempPath.remove(removeStart+1);
            }
            else
                checkSet.put(tempPath.get(i), i);
        }
        if(noLoop)
            return tempPath;
        return cancleLoop(tempPath);
    }

    /**
     *  Solver for the Variation3:
     *  Find a set with maximum number of edge-disjoint paths and minimize the total cost at the same time.
     **/
    public ArrayList<LinkedList<Integer>> findMinCostMaxEdjPaths(Graph<Integer, CustomEdge> tg, Integer s, Integer t) {
        //  Modified Ford-Fulkerson Algorithm
        if (tg == null)
            tg = createGraph(rawGraph);

        if (s == null)
            s = minNodeID;
        if (t == null)
            t = maxNodeID;

        int maxflow = 0;
        Graph<Integer, CustomEdge> rg = createGraph(rawGraph);

        ArrayList<LinkedList<Integer>> pathSet = new ArrayList<>();
        LinkedList<Integer> tempPath;
        while ((tempPath = findADijkPath(rg, s, t)).size() != 0) {
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
            }

            maxflow += pathFlow;
            solveConflict(tg, rg, pathSet, tempPath);
        }

        System.out.println("Maximum number of edge-disjoint path: " + maxflow);
        return pathSet;
    }


    private ArrayList<LinkedList<Integer>> findAllDijkPaths(Graph<Integer, CustomEdge> tg, Integer s) {
        if (tg == null)
            tg = createGraph(rawGraph);
        if (s == null)
            s = minNodeID;
        ArrayList<LinkedList<Integer>> pathSet = new ArrayList<>();
        int parent[] = getDijkstraParents(tg, s);
        for (int t = s+1; t <= Math.max(tg.getVertexCount(), maxNodeID); ++t) {
            if(tg.containsVertex(t)) {
                LinkedList<Integer> path = new LinkedList<>();
                tracebackDijk(parent, t, path);
                if (path.size() != 0) {
                    path.addFirst(s);
                    pathSet.add(path);
                }
            }
        }
        return pathSet;
    }

    // find the shortest path from source(defined in dijkstra search) to nodeID
    private LinkedList<Integer> findADijkPath(Graph<Integer, CustomEdge> tg, Integer s, Integer t) {
        if (tg == null)
            tg = createGraph(rawGraph);
        if (s == null)
            s = minNodeID;
        if (t == null)
            t = maxNodeID;
        LinkedList<Integer> path = new LinkedList<>();
        int parent[] = getDijkstraParents(tg, s);
        tracebackDijk(parent, t, path);
        if (path.size() != 0)
            path.addFirst(s);
        return path;
    }

    private int[] getDijkstraParents(Graph<Integer, CustomEdge> tg, Integer src) {
        int arrayLimit = Math.max(tg.getVertexCount(), maxNodeID);
        int dist[] = new int[arrayLimit];
        boolean finalize[] = new boolean[arrayLimit];
        int parent[] = new int[arrayLimit];

        for (int i = 0; i < arrayLimit; ++i) {
            if (i!= 0)
                parent[i] = -2;
            dist[i] = Integer.MAX_VALUE;
            finalize[i] = false;
        }
        parent[0] = -1;
        dist[src-1] = 0;

        for (int nodeIdx = minNodeID-1; nodeIdx < arrayLimit - 1; ++nodeIdx) {
            int pickedIdx = getMinDistNodeIdx(tg, dist, finalize);
            int pickedNode = pickedIdx + 1;

            if (pickedIdx != -1) {
                finalize[pickedIdx] = true;

                // update the distance of all adjacent nodes in every run
                for (Integer adjNode : tg.getSuccessors(pickedNode)) {
                    int adjNodeIdx = adjNode - 1;
                    int edgeWeight = tg.findEdge(pickedNode, adjNode).getWeight();
                    if (!finalize[adjNodeIdx] && (dist[pickedIdx] + edgeWeight < dist[adjNodeIdx])) {
                        parent[adjNodeIdx] = pickedIdx;
                        dist[adjNodeIdx] = dist[pickedIdx] + edgeWeight;
                    }
                }
            }
        }
        return parent;
    }

    private int getMinDistNodeIdx(Graph<Integer, CustomEdge> tg, int dist[], boolean finalize[])
    {
        int arrayLimit = Math.max(tg.getVertexCount(), maxNodeID);
        int min = Integer.MAX_VALUE;
        int minIndex = -1;

        for (int v = minNodeID-1; v < arrayLimit; v++)
            // TODO: check if <= is OK
            if (!finalize[v] && dist[v] < min) {
                min = dist[v];
                minIndex = v;
            }
        return minIndex;
    }

    private void tracebackDijk(int [] parent, Integer nodeID, LinkedList<Integer> path) {
        // traced to the 0 (not existed node head)
        if (parent[nodeID-1] <= -1)
            return;
        tracebackDijk(parent, parent[nodeID-1]+1, path);
        path.add(nodeID);
    }

    public int getPathSetCost(Graph<Integer, CustomEdge> tg, ArrayList<LinkedList<Integer>> pathSet) {
        if (tg == null)
            tg = createGraph(rawGraph);
        int cost = 0;
        for (LinkedList<Integer> path: pathSet)
            cost += getPathCost(tg, path);
        return cost;
    }

    public int getPathCost(Graph<Integer, CustomEdge> tg, LinkedList<Integer> path) {
        if (tg == null)
            tg = createGraph(rawGraph);
        int cost = 0;
        for (int i = 0; i < path.size() - 1; ++i) {
            cost+= tg.findEdge(path.get(i), path.get(i+1)).getWeight();
        }
        return cost;
    }

    public static void main(String[] args){
        CSVParser cp = new CSVParser("F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\Data\\input_sample.csv");
        LinkedList<Integer[]> rawGraph = cp.parse();
        EdgeDisjointPath edp = new EdgeDisjointPath(rawGraph);
//        ArrayList<LinkedList<Integer>> hlPaths = edp.findEdjPaths(null, null, null);
        ArrayList<LinkedList<Integer>> hlPaths = edp.findMaxEdjPaths(null, null, null);
//        ArrayList<LinkedList<Integer>> hlPaths = edp.findAllDijkPaths(null, null);
        System.out.println("Path total cost: " + edp.getPathSetCost(null, hlPaths));
        for (LinkedList<Integer> ll: hlPaths) {
            for (Integer i: ll)
                System.out.print(i + " ");
            System.out.println();
        }

        ArrayList<LinkedList<Integer>> hlPaths1 = edp.findMinCostMaxEdjPaths(null, null, null);
        System.out.println("Path total cost: " + edp.getPathSetCost(null, hlPaths1));
        for (LinkedList<Integer> ll: hlPaths1) {
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
