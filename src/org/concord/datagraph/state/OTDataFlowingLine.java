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
 * $Revision: 1.1 $
 * $Date: 2006-02-06 18:24:08 $
 * $Author: scytacki $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/

package org.concord.datagraph.state;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.EventObject;

import javax.swing.ImageIcon;

import org.concord.datagraph.ui.DataFlowingLine;
import org.concord.framework.data.stream.DataProducer;
import org.concord.framework.otrunk.DefaultOTObject;
import org.concord.framework.otrunk.OTResourceSchema;
import org.concord.framework.otrunk.OTWrapper;
import org.concord.graph.event.GraphableListener;
import org.concord.graph.util.state.OTDrawingImageIcon;
import org.concord.graph.util.state.OTDrawingToolView;
import org.concord.graph.util.ui.ImageStamp;

/**
 * OTDrawingImageIcon
 * Class name and description
 *
 * Date created: Apr 07, 2005
 *
 * @author imoncada<p>
 *
 */
public class OTDataFlowingLine extends DefaultOTObject
	implements OTWrapper, GraphableListener
{
	public static interface ResourceSchema extends OTResourceSchema
	{		
	    public OTDrawingImageIcon getImage1();
        public void setImage1(OTDrawingImageIcon image);
        
        public OTDrawingImageIcon getImage2();
        public void setImage2(OTDrawingImageIcon image);
        
    	public DataProducer getDataProducer();
	}
    
	private ResourceSchema resources;
	
	public OTDataFlowingLine(ResourceSchema resources)
	{
		super(resources);
		this.resources = resources;
	}
	
	public Object createWrappedObject()
	{
        DataFlowingLine fLine = new DataFlowingLine();
        
		registerWrappedObject(fLine);
		
		return fLine;
	}
	
    public void initWrappedObject(Object container, Object wrappedObject)
    {
        // this should do any tasks needed to setup this
        // wrapped object in its container. 
        // the container is generally a vew object.  
        
        // in this particular case we need to get the graphable object
        // map from our container, and look up our graphable objects
        if(container instanceof OTDrawingToolView) {
            OTDrawingToolView drawingView = (OTDrawingToolView)container;
            Object image1 = drawingView.getWrappedObject(resources.getImage1());
            Object image2 = drawingView.getWrappedObject(resources.getImage2());
            
            DataFlowingLine fLine = (DataFlowingLine)wrappedObject;
            fLine.setImage1((ImageStamp)image1);
            fLine.setImage2((ImageStamp)image2);
            
            fLine.setCycleDistance(20);
            
            fLine.addDataProducer(resources.getDataProducer());
        }
        
    }
    
	/**
	 * @see org.concord.framework.otrunk.OTWrapper#saveObject(java.lang.Object)
	 */
	public void saveObject(Object wrappedObject)
	{
	    // we currently don't support saving
	}

	/**
	 * @see org.concord.framework.otrunk.OTWrapper#getWrappedObjectClass()
	 */
	public Class getWrappedObjectClass()
	{
		return DataFlowingLine.class;
	}

	/**
	 * @see org.concord.framework.otrunk.OTWrapper#registerWrappedObject(java.lang.Object)
	 */
	public void registerWrappedObject(Object wrappedObject)
	{
		DataFlowingLine fLine = (DataFlowingLine)wrappedObject;
		
		//Now, listen to this object so I can be updated automatically when it changes
		fLine.addGraphableListener(this);		
		getOTObjectService().putWrapper(wrappedObject, this);
        fLine.start();
	}

	/**
	 * @see org.concord.graph.event.GraphableListener#graphableChanged(java.util.EventObject)
	 */
	public void graphableChanged(EventObject e)
	{
		saveObject(e.getSource());
	}

	/**
	 * @see org.concord.graph.event.GraphableListener#graphableRemoved(java.util.EventObject)
	 */
	public void graphableRemoved(EventObject e)
	{
		// TODO Auto-generated method stub
		
	}	
}