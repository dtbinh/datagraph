/*
 * Created on Apr 4, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.datagraph.state;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.concord.data.state.OTDataStore;
import org.concord.data.ui.DataStoreLabel;
import org.concord.framework.data.stream.DefaultDataStore;
import org.concord.framework.data.stream.WritableDataStore;
import org.concord.framework.otrunk.OTrunk;
import org.concord.framework.otrunk.view.OTObjectView;
import org.concord.graph.util.ui.ResourceLoader;

/**
 * @author scott
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SingleValueDataView extends JPanel
	implements OTObjectView
{
	WritableDataStore dataStore;
	JDialog dialog;
	DataGraphManager dataGraphManager;
	OTDataCollector dataCollector;
	
    /**
     * 
     */
    public SingleValueDataView(OTDataCollector collector)
    {        
        dataStore = collector.getSingleDataStore();
	    if(dataStore == null && collector.getSingleValue()) {
	        // handle the cases where the dataStore has not been
	        // set.  In these case the data cannot be referred to
	        // in other elements of the authoring system
	        System.err.println(" no \"singleDataStore\" defined for a single value data collector");
	        OTrunk otrunk = collector.getOTDatabase();
	        try {
	            dataStore = (OTDataStore)otrunk.createObject(OTDataStore.class);
	            collector.setSingleDataStore((OTDataStore)dataStore);
	        } catch (Exception e) {
	            e.printStackTrace();
	            dataStore = new DefaultDataStore();
	        }
	    }

	    dataCollector = collector;
	    
    }
	 
    public JComponent getComponent(boolean editable)
    {
        DataStoreLabel dataLabel = new DataStoreLabel(dataStore, 0);
        dataLabel.setColumns(4);
        if (!editable){
            return dataLabel;
        }
        	    
	    dataGraphManager = new DataGraphManager(dataCollector);
	    
		JPanel bottomPanel = dataGraphManager.getBottomPanel();
		
        JButton record = new JButton("Record");
        record.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dataGraphManager.getSourceDataProducer().stop();
                float currentValue = dataGraphManager.getValueLabel().getValue();
                int lastSample = dataStore.getTotalNumSamples();
                dataStore.setValueAt(lastSample, 0, new Float(currentValue));
                dataGraphManager.getDataGraph().reset();
                dialog.hide();
            }
        });
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dataGraphManager.getSourceDataProducer().stop();
                dataGraphManager.getDataGraph().reset();
                dialog.hide();
            }
        });
        bottomPanel.add(record);
        bottomPanel.add(cancel);

        setLayout(new FlowLayout());
        add(dataLabel);
        JButton cDataButton = new JButton();
        ImageIcon icon = ResourceLoader.getImageIcon("data_graph_button.gif", "Collect Data");
        cDataButton.setIcon(icon);
        cDataButton.setToolTipText(icon.getDescription());
        cDataButton.setMargin(new Insets(2,2,2,2));
        cDataButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                boolean needPack = false;
                if(dialog == null) {
                    dialog = new JDialog();
                    dialog.setSize(400,400);
                    needPack = true;
                }
                dialog.getContentPane().setLayout(new BorderLayout());
                dialog.getContentPane().removeAll();
                dialog.getContentPane().add(dataGraphManager.getDataGraph(), BorderLayout.CENTER);
                /*
                if(needPack){
                    dialog.pack();
                }
                */
                
                dialog.show();
                dataGraphManager.getSourceDataProducer().start();
                dataGraphManager.getDataGraph().start();
            }                
        });
        add(cDataButton);
        
        return this;
    }

}
