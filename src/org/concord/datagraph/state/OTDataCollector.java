
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
 * Created on Mar 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.datagraph.state;

import org.concord.data.state.OTDataStore;
import org.concord.framework.otrunk.OTObject;
import org.concord.framework.otrunk.OTObjectInterface;
import org.concord.framework.otrunk.OTObjectList;

/**
 * @author scott
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface OTDataCollector
    extends OTObjectInterface
{
    public String getTitle();
    
	public OTDataGraphable getSource();
	
	public OTDataAxis getYDataAxis();

	public OTDataAxis getXDataAxis();
	
	public final static boolean DEFAULT_singleValue = false;
	public boolean getSingleValue();
	
	public OTDataStore getSingleDataStore();
	public void setSingleDataStore(OTDataStore dataStore);
	
	// if this is null then this doesn't allow saving 
	// named data sets
	public OTObject getDataSetFolder();
	
	public OTObjectList getGraphables();

	//The labels on this data collector are a separate list
	public OTObjectList getLabels();
}
