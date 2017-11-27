import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import org.apache.commons.collections15.Transformer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class GraphVisualizer {

    private Graph<Integer, CustomEdge> mg = new DirectedSparseGraph<>();
    private HashMap<CustomEdge, Color> coloredEdge= new HashMap<>();

    public Graph<Integer, CustomEdge> createGraph(LinkedList<Integer[]> rawGraph) {
        for (Integer[] item : rawGraph) {
            CustomEdge edge = new CustomEdge(item[0], item[1], item[2]);
            mg.addVertex(item[0]);
            mg.addVertex(item[1]);
            mg.addEdge(edge, item[0], item[1], EdgeType.DIRECTED);
        }
        return mg;
    }

    public void colorEdges(Graph<Integer, CustomEdge> tg, ArrayList<LinkedList<Integer>> hlPaths) {
        if (tg == mg) {
            coloredEdge.clear();
            for (LinkedList<Integer> ll : hlPaths) {
                Color pathColor = getRdmColor();
                Integer[] pathArray = ll.toArray(new Integer[ll.size()]);
                for (int i = 0; i < pathArray.length - 1; ++i) {
                    CustomEdge ce = tg.findEdge(pathArray[i], pathArray[i + 1]);
                    coloredEdge.put(ce, pathColor);
                }
            }
        }
        else {
            System.out.println("Color edges are not in this graph.");
        }
    }

    private Color getRdmColor() {
        Random rand = new Random();
        final float MIN_BRIGHTNESS = 0.7f;
        final float MIN_SATURATION = 0.5f;
        float h = rand.nextFloat();
        float s = MIN_SATURATION + ((1f - MIN_SATURATION) * rand.nextFloat());
        float b = MIN_BRIGHTNESS + ((1f - MIN_BRIGHTNESS) * rand.nextFloat());
        return Color.getHSBColor(h, s, b);
    }

    public void drawGraph(Graph<Integer, CustomEdge> readyGraph) {
        if (readyGraph == null)
            readyGraph = mg;
        if (readyGraph != null) {
            CircleLayout<Integer,CustomEdge> layout = new CircleLayout<>(readyGraph);
            layout.setSize(new Dimension(500, 500));
            //        layout.initialize();
            VisualizationViewer<Integer, CustomEdge> vv = new VisualizationViewer<>(layout);
            setViewer(vv);
            vv.setPreferredSize(new Dimension(550, 550)); //Sets the viewing area size

            JFrame frame = new JFrame("Simple Graph View");
            frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            frame.getContentPane().add(vv);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public void saveGraph(Graph<Integer, CustomEdge> readyGraph, String filePath) {
        if (readyGraph == null)
            readyGraph = mg;
        if (readyGraph != null) {
            CircleLayout<Integer,CustomEdge> layout = new CircleLayout<>(readyGraph);
            layout.setSize(new Dimension(500, 500));

            VisualizationViewer<Integer, CustomEdge> vv = new VisualizationViewer<>(layout);
            VisualizationImageServer<Integer, CustomEdge> vis =
                    new VisualizationImageServer<>(vv.getGraphLayout(),
                            vv.getGraphLayout().getSize());
            setViewer(vis);
            // Create the buffered image
            BufferedImage image = (BufferedImage) vis.getImage(
                new Point2D.Double(vv.getGraphLayout().getSize().getWidth() / 2,
                                   vv.getGraphLayout().getSize().getHeight() / 2),
                new Dimension(vv.getGraphLayout().getSize()));

        // Write image to a png file
        File outputfile = new File(filePath);
            try {
                ImageIO.write(image, "png", outputfile);
            } catch (IOException e) {
                // Exception handling
            }
        }
    }

    private void setViewer(BasicVisualizationServer<Integer, CustomEdge> vv) {

        Transformer<CustomEdge, Paint> edgeColorTransformer = e -> {
            if (coloredEdge.size() != 0) {
                return coloredEdge.getOrDefault(e, Color.BLACK);
            }
            else
                return Color.BLACK;
        };

        Transformer<CustomEdge, Stroke> edgeStrokeTransformer = e -> {
            if (coloredEdge.size() != 0) {
                if (coloredEdge.containsKey(e))
                    return new BasicStroke(2.5f);
                else
                    return new BasicStroke(1.0f);
            }
            else
                return new BasicStroke(1.0f);
        };

        vv.setBackground(Color.WHITE);
        vv.getRenderContext().setArrowFillPaintTransformer(edgeColorTransformer);
        vv.getRenderContext().setArrowDrawPaintTransformer(edgeColorTransformer);

        vv.getRenderContext().setEdgeDrawPaintTransformer(edgeColorTransformer);
        vv.getRenderContext().setEdgeStrokeTransformer(edgeStrokeTransformer);
        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<>());
//        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Integer, Integer>());

        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<>());
        vv.getRenderer().getVertexLabelRenderer().setPosition(edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position.CNTR);
    }

    public static void main(String[] args){
        CSVParser cp = new CSVParser("F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\Data\\input_sample.csv");
        GraphVisualizer gv = new GraphVisualizer();
        gv.createGraph((cp.parse()));
        gv.drawGraph(null);
        gv.saveGraph(null, "F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\output.png");
    }

}
