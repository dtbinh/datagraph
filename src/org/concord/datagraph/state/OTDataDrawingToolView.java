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
 * $Revision: 1.14 $
 * $Date: 2006-02-01 21:53:28 $
 * $Author: scytacki $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.datagraph.state;

import java.awt.Dimension;
import java.util.EventObject;

import org.concord.data.state.OTDataStore;
import org.concord.datagraph.engine.ControllableDataGraphable;
import org.concord.datagraph.engine.ControllableDataGraphableDrawing;
import org.concord.framework.otrunk.OTObject;
import org.concord.framework.otrunk.OTObjectService;
import org.concord.framework.otrunk.OTWrapper;
import org.concord.framework.otrunk.view.OTPrintDimension;
import org.concord.framework.otrunk.view.OTViewContainer;
import org.concord.graph.util.engine.DrawingObject;
import org.concord.graph.util.state.OTDrawingToolView;
import org.concord.graph.util.ui.EraserStamp;

/**
 * OTDataDrawingToolView
 * Class name and description
 *
 * Date created: Apr 10, 2005
 *
 * @author imoncada<p>
 *
 */
public class OTDataDrawingToolView extends OTDrawingToolView
implements OTPrintDimension {
	/**
     * Not intended to be serialized but this is here so 
     * the compiler warning goes away.
     */
    private static final long serialVersionUID = 1L;
    OTObject tool;
	OTViewContainer viewContainer;
	
    public void initialize(OTObject tool, OTViewContainer container)
    {
    	super.initialize(tool, container);
    	this.tool = tool;
    	this.viewContainer = container;
        //setMaximumSize(new Dimension(550, 220));

    }
    
	/**
	 * @see org.concord.graph.util.engine.DrawingObjectFactory#createNewDrawingObject(int)
	 */
	public DrawingObject createNewDrawingObject(int type)
	{
		//PointsDataStore points = new PointsDataStore();
		OTDataStore otDataStore;
		try{
            OTObjectService objService = drawingTool.getOTObjectService();            
            otDataStore = (OTDataStore)objService.createObject(OTDataStore.class);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
		ControllableDataGraphable dg = new ControllableDataGraphableDrawing();
		dg.setDrawAlwaysConnected(false);
		dg.setDataStore(otDataStore, 0, 1);
		dg.setLineType(ControllableDataGraphable.LINETYPE_FREE);
		objList.add(dg);
		return dg;
	}
	
	/**
	 * @see org.concord.graph.event.GraphableListListener#listGraphableAdded(java.util.EventObject)
	 */
	public void listGraphableAdded(EventObject e)
	{
		Object obj = e.getSource();
		OTWrapper otWrapper = null;
		
		if (obj instanceof ControllableDataGraphableDrawing){
			
			ControllableDataGraphableDrawing drawObj = (ControllableDataGraphableDrawing)obj;
			OTDataGraphable otDataGraphable;
		
			try{
                OTObjectService objService = drawingTool.getOTObjectService();
				otDataGraphable = (OTDataGraphable)objService.createObject(OTDataGraphable.class);
			}
			catch (Exception ex) {
				ex.printStackTrace();
				return;
			}
			otDataGraphable.setDrawing(true);
			otDataGraphable.setDataStore((OTDataStore)drawObj.getDataStore());
			
			otWrapper = otDataGraphable;
			drawingTool.getGraphables().add(otDataGraphable);
			
			if (otWrapper != null){
				otWrapper.registerWrappedObject(obj);
				otWrapper.saveObject(obj);
			}
			
		}
		else{
			super.listGraphableAdded(e);
		}
	}
	
	public Dimension getPrintDimention(int containerDisplayWidth, int containerDisplayHeight) {
		Dimension dim = new Dimension(550, 225);
        if(dim.height > containerDisplayHeight) 
            dim.height = containerDisplayHeight;
        if(dim.width > containerDisplayWidth) 
            dim.width = containerDisplayWidth;
        return dim;
	}    
}
