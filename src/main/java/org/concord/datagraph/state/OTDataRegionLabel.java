package org.concord.datagraph.state;

public interface OTDataRegionLabel
    extends OTDataAnnotation
{
	// x1 and x2 are the start and end points of a highlighted region, in
	// no particular order. Note that, as they are applied to a specific
	// datagraphable, the y's are implicit.
	
	public static float DEFAULT_x1 = Float.NaN;
	public float getX1();
	public void setX1(float x1);

	public static float DEFAULT_x2 = Float.NaN;
	public float getX2();
	public void setX2(float x2);
	
	public static boolean DEFAULT_showLabel = true;
	public boolean getShowLabel();
	public void setShowLabel(boolean showLabel);
	
	public static boolean DEFAULT_showHighlight = true;
	public boolean getShowHighlight();
	public void setShowHighlight(boolean flag);
	
	public static int DEFAULT_opacity = 200;
	public int getOpacity();
	public void setOpacity(int opacity);
}
