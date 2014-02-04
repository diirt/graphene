/**
 * Copyright (C) 2012-14 graphene developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.graphene.profile;

import java.awt.Graphics2D;
import org.epics.graphene.*;

/**
 * Handles profiling for <code>LineGraph2DRenderer</code>.
 * Takes a <code>Point2DDataset</code> dataset and repeatedly renders through a <code>LineGraph2DRenderer</code>.
 * 
 * @author asbarber
 */
public class ProfileLineGraph2D extends ProfileGraph2D<LineGraph2DRenderer, Point2DDataset>{
    
    /**
     * Gets a set of random Gaussian 2D point data.
     * @return the appropriate <code>Point2DDataset</code> data
     */
    @Override
    protected Point2DDataset getDataset() {
        return ProfileGraph2D.makePoint2DGaussianRandomData(getNumDataPoints());
    }

    /**
     * Returns the renderer used in the render loop.
     * The 2D point is rendered by a <code>LineGraph2DRenderer</code>.
     * @param imageWidth width of rendered image in pixels
     * @param imageHeight height of rendered image in pixels
     * @return a line graph to draw the data
     */    
    @Override
    protected LineGraph2DRenderer getRenderer(int imageWidth, int imageHeight) {
        LineGraph2DRenderer renderer = new LineGraph2DRenderer(imageWidth, imageHeight);
        renderer.update(new LineGraph2DRendererUpdate().interpolation(InterpolationScheme.LINEAR));
        
        return renderer;
    }

    /**
     * Draws the 2D point data in a line graph.
     * Primary method in the render loop.
     * @param graphics where image draws to
     * @param renderer what draws the image
     * @param data the 2D point data being drawn
     */    
    @Override
    protected void render(Graphics2D graphics, LineGraph2DRenderer renderer, Point2DDataset data) {
        renderer.draw(graphics, data);  
    }
    
    /**
     * Returns the name of the graph being profiled.
     * @return <code>LineGraph2DRenderer</code> title
     */    
    @Override
    public String getGraphTitle() {
        return "LineGraph2D";
    }   
}
