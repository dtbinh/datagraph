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
 * $Revision: 1.24 $
 * $Date: 2007-09-25 12:47:18 $
 * $Author: scytacki $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.datagraph.state;

import java.awt.Dimension;
import java.util.logging.Logger;

import javax.swing.JComponent;

import org.concord.datagraph.ui.DataGraph;
import org.concord.datagraph.ui.DataGraph.AspectDimension;
import org.concord.framework.otrunk.OTObject;
import org.concord.framework.otrunk.view.AbstractOTJComponentView;
import org.concord.framework.otrunk.view.OTJComponentViewContext;
import org.concord.framework.otrunk.view.OTJComponentViewContextAware;


/**
 * PfDataGraphView
 * Class name and description
 *
 * Date created: Oct 27, 2004
 *
 * @author scott<p>
 *
 */
public class OTDataGraphView extends AbstractOTJComponentView implements OTJComponentViewContextAware
{
	private static final Logger logger = Logger.getLogger(OTDataGraphView.class.getCanonicalName());
	OTDataGraph otDataGraph;
	DataGraph dataGraph;
	DataGraphManager manager;
	private OTJComponentViewContext jComponentViewContext;
	
	/* (non-Javadoc)
	 * @see org.concord.portfolio.views.PortfolioView#getComponent(boolean)
	 */
	public JComponent getComponent(OTObject otObject)
	{
		this.otDataGraph = (OTDataGraph)otObject;

		manager = new DataGraphManager(otDataGraph, viewContext, otDataGraph.getShowToolbar(), jComponentViewContext);

		dataGraph = manager.getDataGraph();
		
		dataGraph.setAutoFitMode(DataGraph.AUTO_SCROLL_RUNNING_MODE);
		
		dataGraph.setPreferredSize(new Dimension(400,320));
		
		if (otDataGraph.getUseAspectRatio()) {
		    float ratio = otDataGraph.getAspectRatio();
		    AspectDimension dim = otDataGraph.getAspectDimension();
		    dataGraph.setAspectDimension(dim);
		    dataGraph.setAspectRatio(ratio);
		    dataGraph.setUseAspectRatio(true);
		}
		
		return dataGraph;				    
	}
	
	public DataGraphManager getGraphManager() {
		return manager;
	}
	
    /* (non-Javadoc)
     * @see org.concord.framework.otrunk.view.OTJComponentView#viewClosed()
     */
    @Override
    public void viewClosed()
    {
    	manager.viewClosed();
    }

	public void setOTJComponentViewContext(OTJComponentViewContext viewContext)
    {
	    jComponentViewContext = viewContext;
    }
}
