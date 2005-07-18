
/*
 *  Copyright (C) 2004  The Concord Consortium, Inc.,
 *  10 Concord Crossing, Concord, MA 01741
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
 */

/*
 * Created on Apr 5, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.datagraph.state;

import java.io.File;

import javax.swing.JComponent;

import org.concord.framework.otrunk.OTObject;
import org.concord.framework.otrunk.view.OTObjectView;
import org.concord.framework.otrunk.view.OTViewContainer;

/**
 * @author scott
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class OTDataCollectorView
    implements OTObjectView
{
    OTObjectView view;
    OTViewContainer viewContainer;
    OTDataCollector dataCollector;
    
    
    /**
     * 
     */
    public void initialize(OTObject otObject, OTViewContainer container)
    {
        this.dataCollector = (OTDataCollector)otObject;
        if(dataCollector.getSingleValue()) {
            view = new SingleValueDataView(dataCollector);
        }
        else {
            view = new DataCollectorView(dataCollector);
        }
        this.viewContainer = container;
        
    }

    /* (non-Javadoc)
     * @see org.concord.framework.otrunk.view.OTObjectView#getComponent(boolean)
     */
    public JComponent getComponent(boolean editable)
    {
        return view.getComponent(editable);
    }

    /* (non-Javadoc)
     * @see org.concord.framework.otrunk.view.OTObjectView#viewClosed()
     */
    public void viewClosed()
    {
        if(view != null) {
            view.viewClosed();
        }
    }

	public String getXHTMLText(File folder, int containerDisplayWidth, int containerDisplayHeight) {
		// TODO Auto-generated method stub
		JComponent comp = getComponent(false);
		comp.setSize(comp.getPreferredSize());
		String url = viewContainer.saveImage(comp, 1, 1, folder, dataCollector);
		url = "<img src='" + url + "'>";
		return url;
	}
}
