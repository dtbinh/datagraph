/*
 *  Copyright (C) 2004  The Concord Consortium, Inc.,
 *  10 Concord Crossing, Concord, MA 01742
 *
 *  Web Site: http://www.concord.org
 *  Email: info@concord.org
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * END LICENSE */

/*
 * Created on Apr 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.datagraph.state;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.concord.data.stream.TimerDataProducer;
import org.concord.data.ui.DataFlowControlAction;
import org.concord.data.ui.DataFlowControlButton;
import org.concord.data.ui.DataFlowControlToolBar;
import org.concord.data.ui.DataStoreLabel;
import org.concord.datagraph.engine.ControllableDataGraphable;
import org.concord.datagraph.engine.DataGraphable;
import org.concord.datagraph.ui.DataGraph;
import org.concord.datagraph.ui.SingleDataAxisGrid;
import org.concord.framework.data.stream.DataProducer;
import org.concord.framework.otrunk.OTChangeEvent;
import org.concord.framework.otrunk.OTChangeListener;
import org.concord.framework.otrunk.OTObjectList;
import org.concord.framework.otrunk.OTWrapper;
import org.concord.graph.event.GraphableListListener;
import org.concord.graph.examples.GraphWindowToolBar;
import org.concord.graph.ui.GraphTreeView;
import org.concord.graph.ui.Grid2D;
import org.concord.graph.ui.SingleAxisGrid;
import org.concord.graph.util.control.DrawingAction;
import org.concord.swing.CCJCheckBoxRenderer;
import org.concord.swing.CCJCheckBoxTree;
import org.concord.swing.SelectableToggleButton;

/**
 * @author scott
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataGraphManager
	implements GraphableListListener, OTChangeListener, ChangeListener
{
    OTDataCollector dataCollector;
    DataGraph dataGraph;
    
	DataGraphable sourceGraphable;
	DataProducer sourceDataProducer;
	DataStoreLabel valueLabel;
	
	OTDataAxis xOTAxis;
	OTDataAxis yOTAxis;
	
    Hashtable otDataGraphableMap = new Hashtable();
    
	JPanel bottomPanel;
	DataFlowControlToolBar toolBar = null;
	
	CCJCheckBoxTree cTree = new CCJCheckBoxTree("Data Collector");
	TreePath lastSelectedPath;
	
	HashMap nodeGraphableMap = new HashMap();
	Vector checkedTreeNodes = new Vector();

	boolean isCausingOTChange = false;
	boolean shoeDataControls;
	Color[] colors = {Color.BLUE, Color.CYAN, Color.GRAY, Color.GREEN, Color.MAGENTA,
			Color.ORANGE, Color.PINK, Color.RED, Color.YELLOW, Color.BLACK};
	Vector usedColors = new Vector();
	
    /**
     * 
     */
    public DataGraphManager(OTDataCollector collector, boolean showDataControls)
    {
        dataCollector = collector;
        this.shoeDataControls = showDataControls;

        dataGraph = new DataGraph();
		dataGraph.changeToDataGraphToolbar();
		dataGraph.setAutoFitMode(DataGraph.AUTO_SCROLL_RUNNING_MODE);
		
        dataGraph.setFocusable(true);
        
        KeyStroke keyStroke = KeyStroke.getKeyStroke(new Character('t'), InputEvent.CTRL_MASK | InputEvent.SHIFT_MASK);
        dataGraph.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(keyStroke, "ShowTree");
        dataGraph.getActionMap().put("ShowTree", new AbstractAction(){
            /**
             * First version of action
             */
            private static final long serialVersionUID = 1L;

            public void actionPerformed(ActionEvent e)
            {
                System.out.println("Got event: " + e);
                GraphTreeView gtv = new GraphTreeView();
                gtv.setGraph(dataGraph.getGraph());
                GraphTreeView.showAsDialog(gtv, "graph tree");                
            }            
        });
        
		xOTAxis = dataCollector.getXDataAxis();
		xOTAxis.addOTChangeListener(this);
		yOTAxis = dataCollector.getYDataAxis();
		yOTAxis.addOTChangeListener(this);

		OTObjectList pfGraphables = dataCollector.getGraphables();

		dataGraph.setLimitsAxisWorld(xOTAxis.getMin(), xOTAxis.getMax(),
				yOTAxis.getMin(), yOTAxis.getMax());

		Grid2D grid = dataGraph.getGrid();

		SingleDataAxisGrid sXAxis = (SingleDataAxisGrid)grid.getXGrid();

		DataGraphStateManager.setupAxisLabel(sXAxis, xOTAxis);
		
		SingleDataAxisGrid sYAxis = (SingleDataAxisGrid)grid.getYGrid();
		DataGraphStateManager.setupAxisLabel(sYAxis, yOTAxis);

		Vector realGraphables = new Vector();
		
		// for each list item get the data producer object
		// add it to the data graph
		for(int i=0; i<pfGraphables.size(); i++) {
			OTDataGraphable otGraphable = (OTDataGraphable)pfGraphables.get(i);
			
			// dProducer.getDataDescription().setDt(0.1f);
			DataGraphable realGraphable = (DataGraphable)otGraphable.createWrappedObject();

			if (realGraphable.getDataProducer() != null){
			    System.err.println("Trying to display a background graphable with a data producer");
			}
			
			realGraphables.add(realGraphable);
		    dataGraph.addDataGraphable(realGraphable);
		    //dataGraph.addBackgroundDataGraphable(realGraphable);
            otDataGraphableMap.put(otGraphable, realGraphable);
		}

		OTDataGraphable source = dataCollector.getSource();
		if(source != null) {
			String title = dataCollector.getTitle(); 
			if(title == null) {
			    title = source.getName();			    
			}
			
			if(title != null) {
			    dataGraph.setTitle(title);
			}

			sourceGraphable = (DataGraphable)source.createWrappedObject();
			sourceDataProducer = sourceGraphable.findDataProducer();
			
			// dProducer.getDataDescription().setDt(0.1f);
			if(sourceGraphable instanceof ControllableDataGraphable) {

			    bottomPanel = new JPanel(new FlowLayout());
			    JButton clearButton = new JButton("Clear");
			    clearButton.addActionListener(new ActionListener(){
			       public void actionPerformed(ActionEvent e){
			           dataGraph.reset();			           
			       }
			    });
			    
				DrawingAction a = new DrawingAction();
				a.setDrawingObject((ControllableDataGraphable)sourceGraphable);
				GraphWindowToolBar gwToolbar = dataGraph.getToolBar();
				gwToolbar.addButton(new SelectableToggleButton(a), "Draw a function");
				
			    bottomPanel.add(clearButton);

			    dataGraph.add(bottomPanel, BorderLayout.SOUTH);
			    
			} else if(showDataControls) {
			    bottomPanel = new JPanel(new FlowLayout());
			    valueLabel = new DataStoreLabel(sourceGraphable, 1);
			    valueLabel.setColumns(4);
			    bottomPanel.add(valueLabel);

			    toolBar = createFlowToolBar();
			    toolBar.addDataFlowObject(sourceDataProducer);
			    bottomPanel.add(toolBar);
			    
			    if(dataCollector.getShowTare()){
			        // need to add a button that runs the sourceGraphable
			        // for some amount of time and then offsets the input
			        // values after that.
			        // The code for the Tare can be put into a dataproducer
			        // that wraps the input dataproducer
			    }
			    
			    dataGraph.add(bottomPanel, BorderLayout.SOUTH);  			    
			}

			if(sourceGraphable != null) {
			    realGraphables.insertElementAt(sourceGraphable, 0);
			    dataGraph.addDataGraphable(sourceGraphable);
                otDataGraphableMap.put(source, sourceGraphable);
			}
		}

		if(realGraphables.size() > 1 || dataCollector.getMultipleGraphableEnabled()) {
		    
		    cTree.setCellRenderer(new CCJCheckBoxRenderer());
		    cTree.setRootVisible(false);

		    for(int i=0; i<realGraphables.size(); i++){
		    	DataGraphable dataGraphable = (DataGraphable) realGraphables.get(i);
		        String name = dataGraphable.getLabel();
		        Color color = dataGraphable.getColor();
		        CCJCheckBoxTree.NodeHolder nodeHolder = 
		        	new CCJCheckBoxTree.NodeHolder(name, true, color);
		        usedColors.addElement(color);
		        cTree.setSelectionPath(null);
		        cTree.addObject(nodeHolder);
		        nodeGraphableMap.put(nodeHolder, dataGraphable);
		    }
		    
		    TreeNode rootNode = cTree.getRootNode();
		    if(rootNode.getChildCount() > 1) {
		    	lastSelectedPath = cTree.getPathForRow(0);
		    	cTree.setSelectionPath(lastSelectedPath);
		    }
		    
		    JPanel treePanel = new JPanel();
		    treePanel.setLayout(new BorderLayout());
		    JButton newButton = new JButton("New");
		    newButton.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		addDataGraphable();
		    	}
		    });
		    JButton deleteButton = new JButton("Delete");
		    deleteButton.addActionListener(new ActionListener() {
		    	public void actionPerformed(ActionEvent e) {
		    		removeNode();
		    	}
		    });
		    
		    JPanel controlPanel = new JPanel();
		    controlPanel.add(newButton);
		    controlPanel.add(deleteButton);
		    controlPanel.setBackground(Color.WHITE);
		    
		    treePanel.setBackground(UIManager.getColor("Tree.textBackground"));
		    treePanel.add(cTree, BorderLayout.CENTER);
		    treePanel.add(controlPanel, BorderLayout.NORTH);
		   
		    dataGraph.add(treePanel, BorderLayout.WEST);
		}
		
		// FIXME This should be changed.  The graphables should
		// listen to themselves, and this code then can just listen
		// to the graph area for graph area changes.
		// right now both graph area and source graphable changes
		// are listened too
		// GraphableList graphableList = dataGraph.getObjList();
		// graphableList.addGraphableListListener(this);
		
		dataGraph.setPreferredSize(new Dimension(400,320));
		
		dataGraph.getGraphArea().addChangeListener(this);
		
		cTree.addTreeSelectionListener(tsl);
    }

    public DataGraphable getDataGraphable(OTDataGraphable otGraphable)
    {
        return (DataGraphable)otDataGraphableMap.get(otGraphable);
    }
    
    public DataProducer getSourceDataProducer()
    {
        return sourceDataProducer;
    }

    public float getLastValue()
    {
        if(valueLabel == null) {
            return Float.NaN;
        }
        return valueLabel.getValue();
    }
    
    /**
     * @return
     */
    public JPanel getBottomPanel()
    {
        return bottomPanel;
    }

    public DataGraph getDataGraph()
    {
        return dataGraph;
    }
    
	public DataFlowControlToolBar createFlowToolBar()
	{
	    DataFlowControlToolBar toolbar = 
	        new DataFlowControlToolBar(false);

		DataFlowControlButton b = null;
		
		b = new DataFlowControlButton(DataFlowControlAction.FLOW_CONTROL_START);
		toolbar.add(b);

		b = new DataFlowControlButton(DataFlowControlAction.FLOW_CONTROL_STOP);
		toolbar.add(b);
		
		b = new DataFlowControlButton(DataFlowControlAction.FLOW_CONTROL_RESET);
		b.setText("Clear");
		toolbar.add(b);
	 
		toolbar.addDataFlowObject(dataGraph);
		
	    return toolbar;
	}

	public void setToolbarVisible(boolean visible)
	{
		GraphWindowToolBar gwToolbar = dataGraph.getToolBar();
		if(gwToolbar != null) {
		    // FIXME
			gwToolbar.setVisible(visible);
		}
	}		
	
	/**
	 * This only works for graphables that came from a loaded
	 * pfgraphables.  It doesn't yet handel cases where new
	 * graphables are created by some external thing
	 *
	 */
	public void updateState(Object obj)
	{
		Grid2D grid = dataGraph.getGrid();

		xOTAxis.setDoNotifyChangeListeners(false);
		yOTAxis.setDoNotifyChangeListeners(false);
		
		xOTAxis.setMin((float)dataGraph.getMinXAxisWorld());
		xOTAxis.setMax((float)dataGraph.getMaxXAxisWorld());
		yOTAxis.setMin((float)dataGraph.getMinYAxisWorld());
		yOTAxis.setMax((float)dataGraph.getMaxYAxisWorld());

		SingleAxisGrid sXAxis = grid.getXGrid();
		if(sXAxis.getAxisLabel() != null){
			xOTAxis.setLabel(sXAxis.getAxisLabel());
		}
		
		SingleAxisGrid sYAxis = grid.getYGrid();
		if(sYAxis.getAxisLabel() != null){
			yOTAxis.setLabel(sYAxis.getAxisLabel());
		}

		isCausingOTChange = true;
		xOTAxis.notifyOTChange();
		yOTAxis.notifyOTChange();
		isCausingOTChange = false;

		/*
		if(obj instanceof DataGraphable) {
		    OTDataGraphable source = dataCollector.getSource();
		    
		    source.saveObject(obj);		
		}
		*/
	}
	
	/**
	 * @see org.concord.graph.event.GraphableListListener#listGraphableAdded(java.util.EventObject)
	 */
	public void listGraphableAdded(EventObject e)
	{
	}
		
	/* (non-Javadoc)
	 * @see org.concord.graph.event.GraphableListListener#listGraphableChanged(java.util.EventObject)
	 */
	public void listGraphableChanged(EventObject e)
	{
		updateState(e.getSource());
	}
	
	/* (non-Javadoc)
	 * @see org.concord.graph.event.GraphableListListener#listGraphableRemoved(java.util.EventObject)
	 */
	public void listGraphableRemoved(EventObject e)
	{
		Object obj = e.getSource();
		
		OTWrapper otWrapper = dataCollector.getOTObjectService().getWrapper(obj);
		
		if (otWrapper != null){
			if(otWrapper instanceof OTDataPointLabel)
				dataCollector.getLabels().remove(otWrapper);
		}
	}

	/* (non-Javadoc)
	 * @see org.concord.framework.otrunk.OTChangeListener#stateChanged(org.concord.framework.otrunk.OTChangeEvent)
	 */
	public void stateChanged(OTChangeEvent e)
	{
	    if(isCausingOTChange) {
	        // we are the cause of this change
	        return;
	    }

	    if(e.getSource() == xOTAxis || e.getSource() == yOTAxis) {
	        dataGraph.setLimitsAxisWorld(xOTAxis.getMin(), xOTAxis.getMax(),
	                yOTAxis.getMin(), yOTAxis.getMax());
	    }	    	    
	}
	
	/* (non-Javadoc)
     * @see javax.swing.event.ChangeListener#stateChanged(javax.swing.event.ChangeEvent)
     */
    public void stateChanged(ChangeEvent e)
    {
        Object source = e.getSource();
        updateState(source);
    }
    
    TreeSelectionListener tsl = new TreeSelectionListener() {
    	public void valueChanged(TreeSelectionEvent e) {
    		if(e.getSource() == cTree) {
   				TreePath newSelectedPath = cTree.getSelectionPath();
				if(newSelectedPath != null && newSelectedPath != lastSelectedPath) {
					lastSelectedPath = newSelectedPath;
    				DefaultMutableTreeNode node = 
    					(DefaultMutableTreeNode)lastSelectedPath.getLastPathComponent();
    				CCJCheckBoxTree.NodeHolder nodeHolder = 
    					(CCJCheckBoxTree.NodeHolder)node.getUserObject();
    				DataGraphable dataGraphable = 
    					(DataGraphable) nodeGraphableMap.get(nodeHolder);

    				// disconnect toolBar from the last data producer
    				toolBar.removeDataFlowObject(sourceDataProducer);
    				Dimension labelSize = valueLabel.getSize();
    				Point labelLocation = valueLabel.getLocation();
    				bottomPanel.remove(valueLabel);
    				bottomPanel.remove(toolBar);
    				
    				sourceDataProducer = dataGraphable.findDataProducer();

    				//Connect new data producer with toolBar and valueLabel
    				toolBar.addDataFlowObject(sourceDataProducer);
    			    valueLabel = new DataStoreLabel(dataGraphable, 1);
    			    valueLabel.setColumns(4);
    			    
    			    bottomPanel.setLayout(new FlowLayout());
    			    valueLabel.setSize(labelSize);
    			    valueLabel.setLocation(labelLocation);
    			    bottomPanel.add(valueLabel);
    			    bottomPanel.add(toolBar);
    			    
    			    drawCheckedDataGraphables();
    			    dataGraph.repaint();
				}
    		}
    	}
    };
    
    private void drawCheckedDataGraphables() {
	    checkedTreeNodes = cTree.getCheckedNodes();
    	Vector allNodes = cTree.getAllNodes();
    	int size = allNodes.size();
    	for(int i = 0; i < size; i ++) {
    		Object obj = allNodes.elementAt(i);
    		DataGraphable dataGraphable = (DataGraphable)nodeGraphableMap.get(obj);
    		if(dataGraphable != null) {
    			if(checkedTreeNodes.contains(obj)) {
    				//dataGraphable.setVisible(true);
        			dataGraph.removeDataGraphable(dataGraphable);
        			dataGraph.addDataGraphable(dataGraphable);
    			}
    			else {
    				//dataGraphable.setVisible(false);
        			dataGraph.removeDataGraphable(dataGraphable);
        			//dataGraph.addDataGraphable(dataGraphable);
    			}
    		}
    	}
    	
    	DefaultMutableTreeNode node = 
    		(DefaultMutableTreeNode)cTree.getLastSelectedPathComponent();
    	Object object = node.getUserObject();
       	DataGraphable dataGraphable = (DataGraphable)nodeGraphableMap.get(object);
		dataGraphable.setVisible(true);
		dataGraph.removeDataGraphable(dataGraphable);
		dataGraph.addDataGraphable(dataGraphable);
    }
    
    private void addDataGraphable() {
    	
    	// get color and name of the new node
    	Color color = getNewColor();
    	String name = null;
    	while(name == null || name.trim().length() == 0)
    		name = 
    			JOptionPane.showInputDialog(null, "Enter the name of new DataGraphable");
    	
    	// add new node to tree
    	cTree.removeTreeSelectionListener(tsl);
    	cTree.setSelectionPath(null);
    	CCJCheckBoxTree.NodeHolder newNodeHolder = 
    		new CCJCheckBoxTree.NodeHolder(name, true, color);
    	cTree.addObject(newNodeHolder);
    	cTree.setSelectionPath(lastSelectedPath);
    	cTree.addTreeSelectionListener(tsl);
    	
    	// create new datagraphable and add it to datagraph
    	DataGraphable dataGraphable = new DataGraphable();
    	dataGraphable.setColor(color);
    	DataProducer dataProducer = null;
    	dataProducer = sourceDataProducer;
    	dataGraphable.setDataProducer(dataProducer);
    	dataGraph.addDataGraphable(dataGraphable);
    	
    	// add node-datagraphable pair to hashmap
    	nodeGraphableMap.put(newNodeHolder, dataGraphable);
    }
    
    /**
     * FIXME get color for new node. If all colors are used in colors array,
     * just use black color.
     * @return Color color - unused color or black color if all are used.
     */
    private Color getNewColor() {
    	Color color = null;
    	
    	for(int i = 0; i < colors.length; i++) {
    		if(!usedColors.contains(colors[i])) {
    			color = colors[i];
    			usedColors.addElement(color);
    			break;
    		}
    	}

    	if(color == null) color = Color.BLACK;

    	return color;    	
    }
    
    /**
     * remove node from tree, also removes datagraphable from datagraph,
     * key-value pair from hashmap, color related to that node also removed.
     *
     */
    private void removeNode() {
    	cTree.removeTreeSelectionListener(tsl);
    	Object obj = cTree.removeCurrentNode();
    	cTree.addTreeSelectionListener(tsl);
    	//System.out.println(obj + " removed from tree");
    	if(obj != null) {
	    	DataGraphable dataGraphable = (DataGraphable)nodeGraphableMap.get(obj);
	    	nodeGraphableMap.remove(obj);
	   		dataGraph.removeDataGraphable(dataGraphable);
	   		dataGraphable.setVisible(false);
	    	OTDataGraphable otDataGraphable = getOTDataGraphable(dataGraphable);
	   		dataCollector.getGraphables().remove(otDataGraphable);
	   		removeLabelsFrom(otDataGraphable);
	   		otDataGraphableMap.remove(dataGraphable);
	   		usedColors.removeElement(dataGraphable.getColor());
	    	if(checkedTreeNodes.contains(obj))checkedTreeNodes.removeElement(obj);
	    	//System.out.println(dataGraphable + " removed too");
    	}
    }
    
    private OTDataGraphable getOTDataGraphable(DataGraphable dataGraphable) {
   		Enumeration enu = otDataGraphableMap.keys();
   		while(enu.hasMoreElements()) {
   			OTDataGraphable otDataGraphable = (OTDataGraphable) enu.nextElement();
   			if(otDataGraphableMap.get(otDataGraphable).equals(dataGraphable)) {
   		   		dataCollector.getGraphables().remove(otDataGraphable);
   				otDataGraphableMap.remove(dataGraphable);
   	   			return otDataGraphable;
   			}
   		}
    	return null;
    }
    
    private void removeLabelsFrom(OTDataGraphable dataGraphable) {
    	OTObjectList labelList = dataCollector.getGraphables();
    	for(int i = 0; i < labelList.size(); i++) {
    		Object obj = labelList.get(i);
    		if(obj.equals(dataGraphable)) {
    			labelList.remove(i);
        		//return;
    		}
    		//System.out.println("label: " + obj.toString());
    		if(obj instanceof OTDataPointLabel) {
    			System.out.println("ot datapoint label");
    			OTDataPointLabel dataPointLabel = (OTDataPointLabel)obj;
    			if(dataPointLabel.getDataGraphable().equals(dataGraphable)) {
    				labelList.remove(dataPointLabel);
    			}
    		}
    	}
    }
}
