package org.concord.datagraph.ui;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Composite;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.concord.datagraph.engine.DataGraphable;
import org.concord.framework.data.stream.DataListener;
import org.concord.framework.data.stream.DataProducer;
import org.concord.framework.data.stream.DataStreamEvent;
import org.concord.framework.startable.StartableEvent;
import org.concord.framework.startable.StartableListener;
import org.concord.graph.engine.CoordinateSystem;
import org.concord.graph.engine.DefaultGraphable;
import org.concord.graph.engine.Graphable;
import org.concord.graph.engine.GraphableList;

public class VerticalPlaybackLine extends DefaultGraphable implements DataAnnotation {
    private DataGraphable graphable;
    private GraphableList otherGraphables;
    private DataProducer playbackProducer;
    private float currentX = 0;
    
    private static Stroke lineStroke = new BasicStroke(2.5f,       // Width
                                                   BasicStroke.CAP_SQUARE,     // End cap
                                                   BasicStroke.JOIN_MITER,     // Join style
                                                   1.0f);
    private static Color lineColor = new Color(0x00ff00); // 0x5058ba
    private static Composite lineComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f);
    
    public VerticalPlaybackLine(DataProducer playbackDataProducer) {
        super();
        this.playbackProducer = playbackDataProducer;
        this.playbackProducer.addDataListener(new DataListener() {
            public void dataReceived(DataStreamEvent dataEvent) {
                updateX(dataEvent);
            }
            
            public void dataStreamEvent(DataStreamEvent dataEvent) {
                updateX(dataEvent);
            }

            private void updateX(DataStreamEvent dataEvent) {
                if (dataEvent.numSamples > 0) {
                    // System.out.println("Setting current x to: " + dataEvent.data[0]);
                    currentX = dataEvent.data[0];
                    VerticalPlaybackLine.this.setVisible(true);
                    VerticalPlaybackLine.this.notifyChange();
                }
            }
        });
        
        this.playbackProducer.addStartableListener(new StartableListener() {
            public void startableEvent(StartableEvent event) {
                if (event.getType().equals(StartableEvent.StartableEventType.RESET)) {
                    setVisible(false);
                    currentX = 0;
                } else {
                    setVisible(true);
                }
            }
        });
        
        this.setVisible(false);
    }

    public DataGraphable getDataGraphable() {
        return graphable;
    }

    public void setDataGraphable(DataGraphable dataGraphable) {
        graphable = dataGraphable;
    }

    public void setGraphableList(GraphableList gList) {
        otherGraphables = gList;
    }

    public Graphable getCopy() {
        return null;
    }

    public void draw(Graphics2D g) {
        double top = graphArea.getUpperRightCornerWorld().getY();
        double bottom = graphArea.getLowerLeftCornerWorld().getY();
        
        CoordinateSystem cs = graphArea.getCoordinateSystem();
        Point2D lineStart = cs.transformToDisplay(new Point2D.Double(currentX, bottom));
        Point2D lineEnd = cs.transformToDisplay(new Point2D.Double(currentX, top));
        
        Color originalColor = g.getColor();
        Stroke originalStroke = g.getStroke();
        Composite originalComposite = g.getComposite();
        
        g.setColor(lineColor);
        g.setStroke(lineStroke);
        g.setComposite(lineComposite);
        
        g.draw(new Line2D.Double(lineStart, lineEnd));
        
        // TODO Draw indicators (with values?) at each graphables y point for this x value
        
        g.setColor(originalColor);
        g.setStroke(originalStroke);
        g.setComposite(originalComposite);
    }

}
