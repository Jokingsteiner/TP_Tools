/**
 * Created by cjk98 on 11/20/2017.
 */
public class CustomEdge {
    private int sourceNode;
    private int targetNode;
    private int weight = 1;
    private int capacity = 1;

    public CustomEdge(int source, int target, int weight) {
        this.sourceNode = source;
        this.targetNode = target;
        this.weight = weight;
    }

    public int getSourceNode() {
        return sourceNode;
    }

    public void setSourceNode(int sourceNode) {
        this.sourceNode = sourceNode;
    }

    public int getTargetNode() {
        return targetNode;
    }

    public void setTargetNode(int targetNode) {
        this.targetNode = targetNode;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public String toString() {
        return Integer.toString(weight);
    }
}
