package org.concord.datagraph.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.text.NumberFormat;

import org.concord.datagraph.engine.DataGraphable;
import org.concord.framework.data.stream.DataStore;
import org.concord.framework.data.stream.DataStoreEvent;
import org.concord.framework.data.stream.DefaultDataStore;

public class DataRegionLabel extends DataPointLabel
{
	private float xUpperBounds;
	private float xLowerBounds;
	private Color DEFAULT_COLOR = new Color(100,255,100,150);
	private DefaultDataStore highliterDataStore;
	private boolean dsNeedsUpdate = true;
	
	protected int xPrecision = 1;
	protected int yPrecision = 1;
	
	private float x1;
	private float x2;
	private float y1;
	private float y2;
	private float xMiddle;
	private float yMiddle;
	private boolean showLabel = true;
	private boolean showHighlight = true;
	
	private int opacity;
	
	@Override
    public void draw(Graphics2D g)
	{
		if (dsNeedsUpdate){
			processDataStore(dataGraphable);
			updateDataPointLabels();
			setDataPoint(new Point2D.Float(xMiddle, yMiddle));
			setAdjustDisplayDataPointY(-5);
			dsNeedsUpdate = false;
		}
		
		setDrawDataPoint(false);
		
		if (showLabel){
			super.draw(g);
		}
		
		if(showHighlight && dataGraphable != null && dataGraphable.isVisible()) {
			DataGraphable highlighter = new DataGraphable();
			highlighter.setGraphArea(dataGraphable.getGraphArea());
			highlighter.setDataStore(highliterDataStore);
			highlighter.setChannelX(dataGraphable.getChannelX());
			highlighter.setChannelY(dataGraphable.getChannelY());
			highlighter.setLineWidth(12);
			Color color = new Color(getBackground().getRed(), getBackground().getGreen(), getBackground().getBlue(), opacity);
			highlighter.setColor(color);
			highlighter.draw(g);
		}
	}
	
	private void processDataStore(DataStore dataStore){
		highliterDataStore = new DefaultDataStore();
		highliterDataStore.clearValues();
		
		x1 = Float.POSITIVE_INFINITY;
		x2 = Float.NEGATIVE_INFINITY;
		float middleDifference = Float.POSITIVE_INFINITY;
		float middle = xLowerBounds + ((xUpperBounds - xLowerBounds) / 2);
		Float xValue = null;
		Float yValue = null;
		
		Float tooLowX = null;
		Float tooLowY = null;
		
		Float tooHighX = null;
		Float tooHighY = null;
		
		boolean firstPointFound = false;
		
		for (int i = 0; i < dataStore.getTotalNumSamples(); i++) {
			xValue = (Float) dataStore.getValueAt(i, 0);
			float currentX = xValue.floatValue();
			
            yValue = (Float) dataStore.getValueAt(i, 1);
            float currentY = yValue.floatValue();
            
            if (currentX < xLowerBounds) {
                // this point is out of bounds, too low
                tooLowX = currentX;
                tooLowY = currentY;
            } else if (currentX <= xUpperBounds){
                // this point is in-bounds
			    // if this is the first qualifying point, check to see if the previous point is closer than the current point
			    // if so, include it in the highlight region
			    if (! firstPointFound) {
			        if (i > 0) {
			            float currentDiff = Math.abs(currentX - xLowerBounds);
			            float prevDiff = Math.abs(tooLowX - xLowerBounds);
			            if (prevDiff < currentDiff) {
			                highliterDataStore.setValueAt(i-1, 0, tooLowX);
			                highliterDataStore.setValueAt(i-1, 1, tooLowY);
			            }
			            
			        }
			        firstPointFound = true;
			    }
			    


	        	highliterDataStore.setValueAt(i, 0, xValue);
	        	highliterDataStore.setValueAt(i, 1, yValue);
	        	
                if (currentX < x1){
	        		x1 = currentX;
	        		y1 = currentY;
	        	} else if (currentX > x2){
	        		x2 = currentX;
	        		y2 = currentY;
	        	}
	        	
	        	if (Math.abs(currentX - middle) < Math.abs(middleDifference)){
	        		xMiddle = currentX;
	        		yMiddle = currentY;
	        		middleDifference = xMiddle - middle;
	        	}
	        } else {
	            // this point it out-of-bounds, too high
                if (i < dataStore.getTotalNumSamples()-1) {
                    float currentDiff = Math.abs(currentX - xUpperBounds);
                    float prevDiff = Math.abs((Float)dataStore.getValueAt(i-1, 0) - xUpperBounds);
                    if (currentDiff < prevDiff) {
                        highliterDataStore.setValueAt(i, 0, xValue);
                        highliterDataStore.setValueAt(i, 1, yValue);
                    }
                }
                // no need to process any more points
                break;
	        }
        }
	}
	
	@Override
    protected void updateDataPointLabels()
    {
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(xPrecision);
        pointLabel = "(" + nf.format(x1) + ((xUnits== null)?"":xUnits) + ", ";
        nf.setMaximumFractionDigits(yPrecision);
        pointLabel += nf.format(y1) + ((yUnits== null)?"":yUnits) + ") - ";
        
        nf.setMaximumFractionDigits(xPrecision);
        pointLabel += "(" + nf.format(x2) + ((xUnits== null)?"":xUnits) + ", ";
        nf.setMaximumFractionDigits(yPrecision);
        pointLabel += nf.format(y2) + ((yUnits== null)?"":yUnits) + ")";
    }
	
	public float getXUpperBounds(){
		return xUpperBounds;
	}
	
	public float getXLowerBounds(){
		return xLowerBounds;
	}
	
	public void setXUpperBounds(float startX){
		this.xUpperBounds = startX;
	}
	
	public void setXLowerBounds(float endX){
		this.xLowerBounds = endX;
	}
	
	public void setRegion(float x1, float x2){
		setXUpperBounds(x1 > x2 ? x1 : x2);
		setXLowerBounds(x1 > x2 ? x2 : x1);
		dataGraphable.getDataStore().addDataStoreListener(this);
	}
	
	public void setShowLabel(boolean showLabel){
		this.showLabel = showLabel;
	}
	
	public boolean getShowLabel(){
		return showLabel;
	}
	
	public void setShowHighlight(boolean showHighlight){
		this.showHighlight = showHighlight;
	}
	
	public boolean getShowHighlight(){
		return showHighlight;
	}
	
	/**
	 * @see org.concord.framework.data.stream.DataStoreListener#dataChanged(org.concord.framework.data.stream.DataStoreEvent)
	 */
	@Override
    public void dataChanged(DataStoreEvent evt)
	{
		super.dataChanged(evt);
		dsNeedsUpdate = true;
	}

    public void setOpacity(int opacity) {
        this.opacity = opacity;
    }

    public int getOpacity() {
        return opacity;
    }
}
