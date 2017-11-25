/**
 * Created by cjk98 on 11/20/2017.
 * For EECS221 TP, testing graph visualization
 */
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationImageServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.decorators.EdgeShape;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.*;
import org.apache.commons.collections15.Transformer;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

public class GraphVisualizer {

    private Graph<Integer, CustomEdge> ig = new DirectedSparseGraph<Integer, CustomEdge>();

    public Graph createGraph(LinkedList<Integer[]> rawGraph) {
        System.out.println("rawGraph size = " + rawGraph.size());
        for (int i = 0; i < rawGraph.size(); ++i) {
            Integer[] item = rawGraph.get(i);
            CustomEdge edge = new CustomEdge(item[0], item[1], item[2]);
            ig.addVertex(item[0]);
            ig.addVertex(item[1]);
            ig.addEdge(edge, item[0], item[1], EdgeType.DIRECTED);
        }
        return ig;
    }

    public void drawGraph(Graph<Integer, CustomEdge> readyGraph) {
        if (readyGraph == null)
            readyGraph = ig;
        if (readyGraph != null) {
            Layout layout = new CircleLayout(readyGraph);
            layout.setSize(new Dimension(500, 500));
            //        layout.initialize();
            VisualizationViewer<Integer, CustomEdge> vv = setViewer(layout);
            vv.setPreferredSize(new Dimension(550, 550)); //Sets the viewing area size

            JFrame frame = new JFrame("Simple Graph View");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.getContentPane().add(vv);
            frame.pack();
            frame.setVisible(true);
        }
    }

    public void saveGraph(Graph<Integer, CustomEdge> readyGraph, String filePath) {
        if (readyGraph == null)
            readyGraph = ig;
        if (readyGraph != null) {
            Layout layout = new CircleLayout(readyGraph);
            layout.setSize(new Dimension(500, 500));

            VisualizationViewer<Integer, CustomEdge> vv = setViewer(layout);
            VisualizationImageServer<Integer, CustomEdge> vis =
                    new VisualizationImageServer<Integer, CustomEdge>(vv.getGraphLayout(),
                            vv.getGraphLayout().getSize());

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

    private VisualizationViewer<Integer, CustomEdge> setViewer(Layout layout) {
        VisualizationViewer<Integer, CustomEdge> vv = new VisualizationViewer<Integer, CustomEdge>(layout);

        Transformer<CustomEdge, Paint> edgeColorTransformer = new Transformer<CustomEdge, Paint>()
        {
            @Override
            public Paint transform(CustomEdge e)
            {
                final int src = ig.getSource(e);
                final int dst = ig.getDest(e);
                System.out.println("src = " + src);
                System.out.println("dst = " + dst);
                if (src == 3 && dst == 10)
                {
                    return Color.RED;
                }
                return Color.BLACK;
            }
        };


        vv.setBackground(Color.WHITE);
        vv.getRenderContext().setArrowFillPaintTransformer(edgeColorTransformer);
        vv.getRenderContext().setArrowDrawPaintTransformer(edgeColorTransformer);
        vv.getRenderContext().setEdgeDrawPaintTransformer(edgeColorTransformer);

        vv.getRenderContext().setEdgeLabelTransformer(new ToStringLabeller<CustomEdge>());
//        vv.getRenderContext().setEdgeShapeTransformer(new EdgeShape.Line<Integer, Integer>());
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<Integer>());
        vv.getRenderer().getVertexLabelRenderer().setPosition(edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position.CNTR);
        return vv;
    }

    public static void main(String[] args){
        CSVParser cp = new CSVParser("F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\Data\\input_sample.csv");
        GraphVisualizer gv = new GraphVisualizer();
        gv.createGraph(cp.parse());
        gv.drawGraph(null);
        gv.saveGraph(null, "F:\\Users\\OneDrive\\Documents\\UCI\\EECS 221 Adv Data Know\\Projects\\Term_Project\\output.png");
    }

}
