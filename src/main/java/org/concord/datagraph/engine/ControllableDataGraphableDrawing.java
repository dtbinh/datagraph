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
 * $Revision: 1.13 $
 * $Date: 2007-09-07 11:02:39 $
 * $Author: scytacki $
 *
 * Licence Information
 * Copyright 2004 The Concord Consortium 
*/
package org.concord.datagraph.engine;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Vector;

import org.concord.graph.engine.CoordinateSystem;
import org.concord.graph.engine.Graphable;
import org.concord.graph.engine.MathUtil;
import org.concord.graph.engine.SelectableByTool;
import org.concord.graph.event.GraphableListener;
import org.concord.graph.event.SelectableListener;
import org.concord.graph.util.engine.DrawingObject;


/**
 * ControllableDataGraphableDrawing
 * Class name and description
 *
 * Date created: Mar 19, 2005
 *
 * @author imoncada<p>
 *
 */
public class ControllableDataGraphableDrawing extends ControllableDataGraphable
	implements SelectableByTool
{
	protected boolean selected = false;
	protected boolean selectable = true;
	protected Vector selectableListeners;
	
	private boolean wasSelectedBySelectionTool = false;

	private Color boundingBoxColor;
	
	// FIXME this variable is never read
	private boolean clickOnBoundingBox = false;
	private boolean showBoundingBox;
	
	/**
	 * 
	 */
	public ControllableDataGraphableDrawing()
	{
		super();
		selectableListeners = new Vector();
		boundingBox = new Rectangle2D.Double();
		boundingBoxColor = new Color(100, 100, 100);
	}
	
	public void setShowBoundingBox(boolean showBoundingBox){
		this.showBoundingBox = showBoundingBox;
	}

	/**
	 * @see org.concord.graph.engine.Drawable#draw(java.awt.Graphics2D)
	 */
	public void draw(Graphics2D g)
	{
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		Shape oldClip = g.getClip();
		Color oldColor = g.getColor();
		Stroke oldStroke = g.getStroke();
		
		if (needUpdate){
			update();
			needUpdate = false;
		}
		
		//Draw a bounding box around the graphable
		if (isShowBoundingBox()){  // && !bNewShape){
			g.setColor(boundingBoxColor);
			g.draw(boundingBox);
		}
		
		super.draw(g);

		//Draw the little squares in the corners
		if (isShowBoundingBox() && isResizeEnabled()){
			g.setColor(boundingBoxColor);

			g.fillRect((int)(boundingBox.getX()) - 2, (int)(boundingBox.getY()) - 2, 5, 5);
			g.fillRect((int)(boundingBox.getX() + boundingBox.getWidth()) - 2, (int)(boundingBox.getY()) - 2, 5, 5);
			g.fillRect((int)(boundingBox.getX()) - 2, (int)(boundingBox.getY() + boundingBox.getHeight()) - 2, 5, 5);
			g.fillRect((int)(boundingBox.getX() + boundingBox.getWidth()) - 2, (int)(boundingBox.getY() + boundingBox.getHeight()) - 2, 5, 5);
		}

		g.setColor(oldColor);
		g.setStroke(oldStroke);
		g.setClip(oldClip);
	}

	/**
	 * @return
	 */
	public boolean isShowBoundingBox()
	{
		//if (isMouseControlled()){
		if (isSelected() && showBoundingBox){
			if (!MathUtil.equalsDouble(boundingBox.getHeight(), 0) && 
					!MathUtil.equalsDouble(boundingBox.getWidth(), 0)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * @see org.concord.graph.engine.Graphable#update()
	 */
	public void update()
	{
		super.update();
		updateBoundingBox();
	}
	
	/**
	 * @param g
	 */
	protected void updateBoundingBox()
	{
		CoordinateSystem cs = graphArea.getCoordinateSystem();
		
		if (getTotalNumSamples() == 0) return;
		
		Point2D p1 = new Point2D.Double(getMinXValue(), getMaxYValue());
		Point2D p2 = new Point2D.Double(getMaxXValue(), getMinYValue());
		
		Point2D p1W = cs.transformToDisplay(p1);
		Point2D p2W = cs.transformToDisplay(p2);
		
		boundingBox.setRect(p1W.getX(), p1W.getY(), 
				(p2W.getX() - p1W.getX()), (p2W.getY() - p1W.getY()));
	}
	
	/**
	 * @see org.concord.graph.util.engine.DrawingObject#setDrawingDragMode(int)
	 */
	public boolean setDrawingDragMode(int mode)
	{
		try{
			if (super.setDrawingDragMode(mode)){
				return true;
			}
		}
		catch (Exception ex) {}
		
		if (mode == DrawingObject.DRAWING_DRAG_MODE_SELECT ||
				mode == DrawingObject.DRAWING_DRAG_MODE_MOVE){

			setDragMode(mode);
			return true;
		}
		return true;
	}
	
	/**
	 * @see org.concord.graph.engine.MouseSensitive#isPointInProximity(java.awt.Point)
	 */
	public boolean isPointInProximity(Point p)
	{
		if (super.isPointInProximity(p)){
			return true;
		}
		
		if (dragMode == DrawingObject.DRAWING_DRAG_MODE_SELECT ||
				dragMode == DRAGMODE_NONE){ 
			if (isPointAValue(p)){
				return true;
			}
		}
		
		//if (dragMode == DrawingObject.DRAWING_DRAG_MODE_MOVE || true){
			if (isSelected()){
				return isPointInBoundingBox(p);
			//	return true;
			}
	//	}
		
		return false;
		
	}
	
	/**
	 * @param p
	 * @return
	 */
	protected boolean isPointInBoundingBox(Point p)
	{
		return boundingBox.contains(p);
	}

	/**
	 * @see org.concord.graph.engine.MouseControllable#mousePressed(java.awt.Point)
	 */
	public boolean mousePressed(Point p)
	{
		if (!isPointInBoundingBox(p))
			bNewShape = true;
		boolean b = super.mousePressed(p);
		if (dragMode == DrawingObject.DRAWING_DRAG_MODE_MOVE){			
			clickOnBoundingBox = isPointInBoundingBox(p);
		}		
		return b;
	}
	
	/**
	 * @see org.concord.graph.engine.MouseControllable#mouseReleased(java.awt.Point)
	 */
	public boolean mouseReleased(Point p)
	{
		clickOnBoundingBox = false;
		
		boolean returnValue = super.mouseReleased(p);
		
		action.finish();
		
		return returnValue;
	}
	/**
	 * @see org.concord.graph.engine.MouseControllable#mouseDragged(java.awt.Point)
	 */
	public boolean mouseDragged(Point p)
	{
		double dx = 0;
		double dy = 0;
				
		if (lastPointW != null){
			dx = lastPointW.getX();
			dy = lastPointW.getY();
		}
		
		if (super.mouseDragged(p)){
			return true;
		}
		if (dragMode == DrawingObject.DRAWING_DRAG_MODE_MOVE ||true){
		//	int a = 1/0;
		//	System.out.println("mouse dragged");
			
		//	if (!clickOnBoundingBox) return false;
			
			CoordinateSystem cs = graphArea.getCoordinateSystem();
			Point2D pW = cs.transformToWorld(p);

			dx = pW.getX() - dx;
			dy = pW.getY() - dy;
			
			//System.out.println("move  " + dx + "," + dy);
			
			move(dx, dy);
			//lastPointW
			
			for (int i = 0; i < allSelectedDrawingObjects.length; i++) {
				if (!allSelectedDrawingObjects[i].equals(this)){
					allSelectedDrawingObjects[i].moveInRelation(lastPointW, pW);
				}
			}
			
		}
		
		return false;
	}
	
	public void moveInRelation(Point2D start, Point2D end) {
		
		double x = 0;
		double y = 0;
		
		CoordinateSystem cs = graphArea.getCoordinateSystem();
		
		//Point2D lastLocationW = cs.transformToWorld(lastLocation);
		
		if (lastLocation != null){
			x = lastLocation.getX();
		}
		if (lastLocation != null){
			y = lastLocation.getY();
		}
		
		
		Point2D startW = cs.transformToDisplay(cs.transformToDisplay(start));
		Point2D endW = cs.transformToDisplay(end);
		double dx = end.getX() - start.getX();
		double dy = end.getY() - start.getY();
		
		Point2D dp = new Point2D.Double(dx, dy);
		Point2D dpW = cs.transformToWorld(dp);// .transformToWorld(dp);
		
		//System.out.println("dp  = "+dp);
		//System.out.println("dpW = "+dpW);
		
		double dxW = dp.getX() - x;
		double dyW = dp.getY() - y;
		
	//	System.out.println("x   = "+x+  ", y   = "+y);
		
		move(dx, dy);
		//lastPointW
		
	}
	
	/**
	 * @param dx
	 * @param dy
	 */
	public void move(double dx, double dy)
	{
		float x, y;
		
		for (int i=0; i<getTotalNumSamples(); i++){
			x = handleValue(getValueAt(i, 0)) + (float)dx;
			y = handleValue(getValueAt(i, 1)) + (float)dy;
			
			setValueAt(i, 0, new Float(x));
			setValueAt(i, 1, new Float(y));
		}
	}

	/**
	 * Selects this object. 
	 * This also fires an objectSelected(obj) event.
	 */
	public void select()
	{
		if (!selected){
			selected = true;
			if (dragMode == DrawingObject.DRAWING_DRAG_MODE_SELECT ||
					dragMode == DRAGMODE_NONE){
				dragMode = DrawingObject.DRAWING_DRAG_MODE_MOVE;
			}
			notifySelect();
		}
	}
	
	/** Deselects this object
	 * This also fires an objectDeselected(obj) event.
	 */
	public void deselect()
	{
		if (selected && !wasSelectedBySelectionTool){
			selected = false;
			dragMode = DrawingObject.DRAWING_DRAG_MODE_SELECT;
			notifyDeselect();
		}
	}

	/**
	 * Returns if the object is selected or not 
	 */
	public boolean isSelected()
	{
		return selected;
	}

	/**
	 * Sets if the object can be selected or not 
	 */
	public void setSelectionEnabled(boolean b)
	{
		selectable = b;
	}

	/**
	 * Returns if the object can be selected or not 
	 */
	public boolean isSelectionEnabled()
	{
		return selectable;
	}

	/*
	 * Adds a SelectableListener to listen for selection events 
	 */
	public void addSelectableListener(SelectableListener l)
	{
		selectableListeners.add(l);
	}
	
	/*
	 * Removes a SelectableListener.
	 */
	public void removeSelectableListener(SelectableListener l)
	{
		selectableListeners.remove(l);
	}

	protected void notifySelect() 
	{		
		EventObject e = new EventObject(this);
		Enumeration lists = selectableListeners.elements();
		while (lists.hasMoreElements()) {			
			GraphableListener gl = (GraphableListener)lists.nextElement();
			if (gl instanceof SelectableListener) {
				((SelectableListener)gl).graphableSelected(e);
			}
		}
	}

	protected void notifyDeselect() 
	{
		EventObject e = new EventObject(this);
		Enumeration lists = selectableListeners.elements();
		while (lists.hasMoreElements()) {			
			GraphableListener gl = (GraphableListener)lists.nextElement();
			if (gl instanceof SelectableListener) {
				((SelectableListener)gl).graphableDeselected(e);
			}
		}
	}
	
	/**
	 * @see org.concord.graph.engine.Graphable#getCopy()
	 */
	public Graphable getCopy()
	{
		ControllableDataGraphableDrawing g = new ControllableDataGraphableDrawing();
		g.setColor(lineColor);
		g.setLineWidth(lineWidth);
		g.setDataStore(dataStore);
		
		//FIXME Add values to the vector... is that enough?
		//for(int i=0; i<yValues.size(); i++){
		//	g.yValues.add(yValues.elementAt(i));
		//}
		
		return g;
	}

	public void forceDeselect() {
		wasSelectedBySelectionTool = false;
		deselect();
		
	}

	public void selectBySelectionTool() {
		wasSelectedBySelectionTool = true;
		select();
		
	}
	
	public void setSelectedBySelectionTool(boolean b){
		wasSelectedBySelectionTool = b;
	}
}
