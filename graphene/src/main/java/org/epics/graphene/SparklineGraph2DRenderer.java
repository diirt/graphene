/**
 * Copyright (C) 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.graphene;

import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.util.Arrays;
import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListNumber;
import org.epics.util.array.SortedListView;

/**
 *
 * @author asbarber
 * @author jkfeng
 * @author sjdallst
 * 
 */
public class SparklineGraph2DRenderer extends Graph2DRenderer<Graph2DRendererUpdate>{

    /**
     * Creates a new sparkline graph renderer.
     * Will draw a circle at the max value, min value, and last value.
     * 
     * @param imageWidth the graph width
     * @param imageHeight the graph height
     */    
    public SparklineGraph2DRenderer(int imageWidth, int imageHeight){
        super(imageWidth, imageHeight);
        super.xLabelMargin = 0;
        super.yLabelMargin = 0;        
    }
    
    //Parameters
    private int     circleDiameter = 3;
    private Color   minValueColor = new Color(28, 160, 232),
                    maxValueColor = new Color(28, 160, 232),
                    firstValueColor = new Color(223, 59, 73),            
                    lastValueColor = new Color(223, 59, 73);
    private boolean drawCircles = true;
    
    //Min, Max, Last Values and Indices
    private int     maxIndex, 
                    minIndex,
                    firstIndex,
                    lastIndex;
    private double  maxValueY = -1, 
                    minValueY = -1,
                    firstValueY = -1,
                    lastValueY = -1;
    private Double  aspectRatio = null,
                    numDataPoints = null;

    //Scaling Schemes    
    public static java.util.List<InterpolationScheme> supportedInterpolationScheme = Arrays.asList(InterpolationScheme.NEAREST_NEIGHBOUR, InterpolationScheme.LINEAR, InterpolationScheme.CUBIC);
    public static java.util.List<ReductionScheme> supportedReductionScheme = Arrays.asList(ReductionScheme.FIRST_MAX_MIN_LAST, ReductionScheme.NONE); 

    private InterpolationScheme interpolation = InterpolationScheme.LINEAR;

    
    //DRAWING FUNCTIONS
    
    /**
     * Draws the graph on the given graphics context.
     * 
     * @param g the graphics on which to display the data
     * @param data the data to display
     */
    public void draw(Graphics2D g, Point2DDataset data) {
        this.g = g;
        
        
        
        if(drawCircles){
            setCircleMargins();
        }

        if(numDataPoints != null){
            if(aspectRatio != null){
                aspectRatio *= (1+((data.getCount()-numDataPoints))/numDataPoints);
            }
        }
        
        numDataPoints = (double)data.getCount();
        //If we want to use the aspect ratio, we change the start and end of the coordinate plot,
        //so that the total height is equal to the width of the xplot divided by the aspect ratio. 
        //TODO: make better tests for this (ones that test when aspect ratio causes y to go out of range), make aspectRatio change with a lessening of points. 
        if(aspectRatio != null){
            adjustGraphToAspectRatio();
        }
        //System.out.println("left: "+leftAreaMargin+"right: "+rightAreaMargin+"bot: "+bottomAreaMargin+"top: "+topAreaMargin);
        //General Rendering
        calculateRanges(data.getXStatistics(), data.getYStatistics());
        calculateGraphArea();
        drawBackground();
        //System.out.println("xEnd: "+xPlotCoordEnd);
        g.setColor(Color.BLACK);        
  
        //Calculates data values
        SortedListView xValues = org.epics.util.array.ListNumbers.sortedView(data.getXValues());
        ListNumber yValues = org.epics.util.array.ListNumbers.sortedView(data.getYValues(), xValues.getIndexes());        
        setClip(g);
        
        //Draws Line  
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);        
        drawValueExplicitLine(xValues, yValues, interpolation, ReductionScheme.FIRST_MAX_MIN_LAST);
        
        
        g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

        //Draws a circle at the max, min, and last value
        //FIXME: Potential problems arise when circles overlap with transparency
        if(drawCircles){
            AlphaComposite ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.7F);
            g.setComposite(ac);        
            
            //Min
            double x = Math.floor(scaledX(data.getXValues().getDouble(minIndex)))+.5;
            double y = Math.floor(scaledY(data.getYValues().getDouble(minIndex)))+.5;
            g.setColor(minValueColor);
            Shape circle = createShape(x, y, circleDiameter);
            g.fill(circle);
            g.draw(circle);
            
            //Max
            x = Math.floor(scaledX(data.getXValues().getDouble(maxIndex)))+.5;
            y = Math.floor(scaledY(data.getYValues().getDouble(maxIndex)))+.5;
            g.setColor(maxValueColor);
            circle = createShape(x, y, circleDiameter);
            g.fill(circle);
            g.draw(circle);
            
            //First
            x = Math.floor(scaledX(data.getXValues().getDouble(firstIndex)))+.5;
            y = Math.floor(scaledY(data.getYValues().getDouble(firstIndex)))+.5;
            g.setColor(firstValueColor);
            circle = createShape(x, y, circleDiameter);
            g.fill(circle); 
            g.draw(circle);    
            
            //Last
            x = Math.floor(scaledX(data.getXValues().getDouble(lastIndex)))+.5;
            y = Math.floor(scaledY(data.getYValues().getDouble(lastIndex)))+.5;
            g.setColor(lastValueColor);
            circle = createShape(x, y, circleDiameter);
            g.fill(circle); 
            g.draw(circle); 
            ac = java.awt.AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0F);
            g.setComposite(ac);
        }
    }
    
    /**
     * Creates a circle shape at the given position with given size.
     * @param x x position of shape
     * @param y y position of shape
     * @param size Diameter of circle
     * @return Ellipse (circle) shape
     */
    protected Shape createShape(double x, double y, double size) {
        double halfSize = size / 2;
        Ellipse2D.Double circle = new Ellipse2D.Double(x-halfSize, y-halfSize, size, size);
        return circle;
    } 
    
    
    @Override
    protected void processScaledValue(int index, double valueX, double valueY, double scaledX, double scaledY) {
        //Checks if new value is the new min or the new max
        
        //Base Case
        if (index == 0){
            firstIndex = 0;
            firstValueY = valueY;
            
            maxValueY = valueY;
            minValueY = valueY;
        }
        else{
            //Max
            if (maxValueY <= valueY){
                maxValueY = valueY;
                maxIndex = index;
            }
            //Min
            if (minValueY >= valueY){
                minValueY = valueY;
                minIndex = index;
            }  
        }
        
        //New point is always last point
        lastValueY = valueY;
        lastIndex = index;
    }
    
    /**
     * Applies the update to the renderer.
     * 
     * @param update the update to apply
     */    
    public void update(SparklineGraph2DRendererUpdate update) {
        super.update(update);

        //Applies updates to members of this class
        if (update.getMinValueColor() != null){
            minValueColor = update.getMinValueColor();
        }
        if (update.getMaxValueColor() != null){
            maxValueColor = update.getMaxValueColor();
        }
        if (update.getCircleDiameter() != null){
            circleDiameter = update.getCircleDiameter();
        }
        if (update.getDrawCircles() != null){
            drawCircles = update.getDrawCircles();
        }
        if (update.getInterpolation() != null) {
            interpolation = update.getInterpolation();
        } 
        if (update.getAspectRatio() != null){
            aspectRatio = update.getAspectRatio();
        }
    }
    
    /**
     * A new update object for a Sparkline graph.
     * @return Update of the Sparkline
     */
    @Override
    public Graph2DRendererUpdate newUpdate() {
        return new SparklineGraph2DRendererUpdate();
    }
    
    /**
     * The current interpolation used for the line.
     * 
     * @return the current interpolation
     */
    public InterpolationScheme getInterpolation() {
        return interpolation;
    }  
    
    /**
     * The index corresponding to the maximum value in the data set.
     * If there are multiple maximums, the greatest index is returned.
     * @return The index of the maximum value
     */
    public int getMaxIndex(){
        return maxIndex;
    }
    
    /**
     * The index corresponding to the minimum value in the data set.
     * If there are multiple minimums, the greatest index is returned.
     * @return The index of the minimum value
     */
    public int getMinIndex(){
        return minIndex;
    }
    
    /**
     * The index corresponding to the first value in the data set.
     * @return the index of the first value
     */
    public int getFirstIndex(){
        return firstIndex;
    }
    
    /**
     * The index corresponding to the last value in the data set.
     * @return The index of the last value
     */
    public int getLastIndex(){
        return lastIndex;
    }
    
    /**
     * The maximum y-value in the list of data.
     * If there are multiple maximum values, the last maximum
     * (determined by the greatest index) is the value returned.
     * @return The data value of the maximum
     */
    public double getMaxValue(){
        return maxValueY;
    }
    
    /**
     * The minimum y-value in the list of data.
     * If there are multiple minimum values, the last minimum
     * (determined by the greatest index) is the value returned.
     * @return The data value of the minimum
     */
    public double getMinValue(){
        return minValueY;
    }
    
    /**
     * The first y-value in the list of data.
     * @return the data value for the first index
     */
    public double getFirstValue(){
        return firstValueY;
    }
    
    /**
     * The last y-value in the list of data.
     * @return The data value for the last index
     */
    public double getLastValue(){
        return lastValueY;
    }
    
    public boolean getDrawCircles(){
        return drawCircles;
    }
    
    public Color getMinValueColor(){
        return minValueColor;
    }
    
    public Color getMaxValueColor(){
        return maxValueColor;
    }
    
    public Color getLastValueColor(){
        return lastValueColor;
    }
    
    public int getCircleDiameter(){
        return circleDiameter;
    }
    public double getAspectRatio(){
        return aspectRatio;
    }
    private void setCircleMargins(){
        leftAreaMargin=(2);
        rightAreaMargin=(2);
        bottomAreaMargin=(2);
        topAreaMargin=(2);
    }
    private void adjustGraphToAspectRatio(){
        int newMargin = (int)(((super.getImageHeight()-bottomMargin-topMargin-((super.getImageWidth()-leftMargin-rightMargin))/aspectRatio))/2);
        double xPlotCoordWidthCopy = super.getImageWidth()-rightMargin-leftMargin;
        while(newMargin < 2){
            xPlotCoordWidthCopy-=2;
            newMargin = (int)((super.getImageHeight()-bottomMargin-topMargin-(xPlotCoordWidthCopy/aspectRatio))/2);
        }
        leftAreaMargin = (int)(2+ (super.getImageWidth()-rightMargin-leftMargin-xPlotCoordWidthCopy)/2);
        rightAreaMargin = (int)(2+(super.getImageWidth()-rightMargin-leftMargin-xPlotCoordWidthCopy)/2);
        bottomAreaMargin = newMargin;
        topAreaMargin = newMargin;
    }
}
