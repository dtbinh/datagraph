
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
 * Created on Feb 15, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.concord.datagraph.ui;

import org.concord.data.Unit;
import org.concord.framework.data.DataDimension;
import org.concord.graph.ui.SingleAxisGrid;

/**
 * @author scott
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SingleDataAxisGrid 
	extends SingleAxisGrid 
{
	DataDimension unit = null;
	
	public SingleDataAxisGrid(int orientation)
	{
		super(orientation);
	}

	public void setUnit(DataDimension unit)
	{
		this.unit = unit;
	}
	
	public String getUnitString()
	{
		if (unit != null){
			return unit.getDimension();			
		}
		
		return null;
	}
	
	protected String getAxisLabelToDraw()
	{
		String unitStr = getUnitString();
		if (unitStr != null){ 
			return getAxisLabel() + " (" + unitStr + ")";
		}
		
		return getAxisLabel();
	}
	
}
