
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
 * Last modification information:
 * $Revision: 1.3 $
 * $Date: 2005-03-06 06:52:49 $
 * $Author: imoncada $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.datagraph.ui;

import org.concord.framework.data.stream.DataStore;
import org.concord.graph.engine.GraphableList;
import org.concord.graph.util.control.AddLabelAction;
import org.concord.graph.util.ui.BoxTextLabel;


/**
 * AddDataPointLabelAction
 * Class name and description
 *
 * Date created: Mar 5, 2005
 *
 * @author imoncada<p>
 *
 */
public class AddDataPointLabelAction extends AddLabelAction
{
	protected DataStore dataStore;
	protected GraphableList dataGraphablesList;
	
	/**
	 * @param gList
	 */
	public AddDataPointLabelAction(GraphableList gList, GraphableList objList)
	{
		super(gList);
		dataGraphablesList = objList;
		setName("Note");
	}

	/**
	 * @see org.concord.graph.util.control.AddLabelAction.createTextLabel
	 */
	protected BoxTextLabel createTextLabel()
	{
		DataPointLabel label = new DataPointLabel(true);
		label.setGraphableList(dataGraphablesList);
		label.setMessage("Data Point");
		return label;
	}
	
	/**
	 * @return Returns the dataStore.
	 */
	public DataStore getDataStore()
	{
		return dataStore;
	}
	
	/**
	 * @param dataStore The dataStore to set.
	 */
	public void setDataStore(DataStore dataStore)
	{
		this.dataStore = dataStore;
	}
}