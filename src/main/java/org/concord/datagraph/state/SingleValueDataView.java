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
 * Last modification information:
 * $Revision: 1.21 $
 * $Date: 2007-09-25 12:47:17 $
 * $Author: scytacki $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.datagraph.state;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JPanel;

import org.concord.data.state.OTDataStore;
import org.concord.data.ui.DataStoreLabel;
import org.concord.framework.data.stream.DefaultDataStore;
import org.concord.framework.data.stream.WritableDataStore;
import org.concord.framework.otrunk.OTControllerService;
import org.concord.framework.otrunk.OTObject;
import org.concord.framework.otrunk.OTObjectService;
import org.concord.framework.otrunk.view.AbstractOTJComponentView;
import org.concord.framework.otrunk.view.OTJComponentViewContext;
import org.concord.framework.otrunk.view.OTJComponentViewContextAware;
import org.concord.graph.util.ui.ResourceLoader;

/**
 * SingleValueDataView
 * 
 * @author scott
 *
 */
public class SingleValueDataView extends AbstractOTJComponentView implements OTJComponentViewContextAware
{
	WritableDataStore dataStore;
	JDialog dialog;
	DataGraphManager dataGraphManager;
	OTDataCollector dataCollector;
	OTControllerService controllerService;
	private OTJComponentViewContext jComponentViewContext;
	
    /**
     * 
     */
    public SingleValueDataView(OTDataCollector collector)
    {
	    dataCollector = collector;
	    
    }
	 
    protected WritableDataStore getDataStore()
    {
    	if(dataStore != null){
    		return dataStore;
    	}
    	
    	OTDataStore otDataStore = dataCollector.getSingleDataStore();
        
	    if(otDataStore == null && dataCollector.getSingleValue()) {
	        // handle the cases where the dataStore has not been
	        // set.  In these case the data cannot be referred to
	        // in other elements of the authoring system
	        System.err.println(" no \"singleDataStore\" defined for a single value data collector");
	        try {
                OTObjectService objService = dataCollector.getOTObjectService();
                otDataStore = (OTDataStore)objService.createObject(OTDataStore.class);
                dataCollector.setSingleDataStore(otDataStore);
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }

	    if(otDataStore == null){
	    	dataStore = new DefaultDataStore();
	    	return dataStore;
	    } 
	    
    	if(controllerService == null){
        	controllerService = createControllerService(dataCollector);
    	}

    	dataStore = (WritableDataStore) controllerService.getRealObject(otDataStore);

    	return dataStore;
    }
    
    public JComponent getComponent(OTObject otObject)
    {
        Box box = new Box(BoxLayout.Y_AXIS);
        
        DataStoreLabel dataLabel = new DataStoreLabel(getDataStore(), 0);
        dataLabel.setColumns(4);
        
        // just the datalabel could be returned here if the view is not supposed to be modified by
        // the user, if that is the case then a second view should be made, or 
        // a custom OTViewEntry should be created with property for turning on and off editability.
        // if (!editable){
        //    return dataLabel;
        // }
        	    
	    dataGraphManager = new DataGraphManager(dataCollector, viewContext, true, jComponentViewContext);
	    
		JPanel bottomPanel = dataGraphManager.getBottomPanel();
		
        JButton record = new JButton("Record");
        record.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dataGraphManager.getSourceDataProducer().stop();
                float currentValue = dataGraphManager.getLastValue();
                int lastSample = getDataStore().getTotalNumSamples();
                getDataStore().setValueAt(lastSample, 0, new Float(currentValue));
                dataGraphManager.getDataGraph().reset();
                dialog.setVisible(false);
            }
        });
        
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent e){
                dataGraphManager.getSourceDataProducer().stop();
                dataGraphManager.getDataGraph().reset();
                dialog.setVisible(false);
            }
        });
        bottomPanel.add(record);
        bottomPanel.add(cancel);

        box.add(dataLabel);
        box.add(Box.createVerticalStrut(3));
        JButton cDataButton = new JButton();
        cDataButton.setAlignmentX(0.5f);
        ImageIcon icon = ResourceLoader.getImageIcon("data_graph_button.gif", "Collect Data");
        cDataButton.setIcon(icon);
        cDataButton.setToolTipText(icon.getDescription());
        cDataButton.setMargin(new Insets(2,2,2,2));
        cDataButton.addActionListener(new ActionListener(){
            public void actionPerformed(ActionEvent event)
            {
                if(dialog == null) {
                    dialog = new JDialog();
                    dialog.setSize(400,400);
                }
                dialog.getContentPane().setLayout(new BorderLayout());
                dialog.getContentPane().removeAll();
                dialog.getContentPane().add(dataGraphManager.getDataGraph(), BorderLayout.CENTER);
                /*
                if(needPack){
                    dialog.pack();
                }
                */
                
                dialog.setVisible(true);
                dataGraphManager.getSourceDataProducer().start();
                dataGraphManager.getDataGraph().start();
            }                
        });
        box.add(cDataButton);

        JPanel outerPanel = new JPanel(new FlowLayout());
        outerPanel.setOpaque(false);
        outerPanel.add(box);
        
        return outerPanel;
    }

    /* (non-Javadoc)
     * @see org.concord.framework.otrunk.view.OTJComponentView#viewClosed()
     */
    public void viewClosed()
    {
    	if(dataGraphManager != null) {
    		dataGraphManager.viewClosed();
    	}
    	
    	if(controllerService != null){
    		controllerService.dispose();
    	}
    }

	public void setOTJComponentViewContext(OTJComponentViewContext viewContext)
    {
	    jComponentViewContext = viewContext;
    }
}
