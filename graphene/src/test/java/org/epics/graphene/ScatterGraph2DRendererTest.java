/**
 * Copyright (C) 2012-14 graphene developers. See COPYRIGHT.TXT
 * All rights reserved. Use is subject to license terms. See LICENSE.TXT
 */
package org.epics.graphene;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import junit.framework.AssertionFailedError;
import org.epics.util.array.ArrayDouble;
import org.junit.AfterClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.BeforeClass;

/**
 *
 * @author carcassi
 */
public class ScatterGraph2DRendererTest extends BaseGraphTest <ScatterGraph2DRendererUpdate, ScatterGraph2DRenderer> {
    
    public ScatterGraph2DRendererTest() {
        super("scatterGraph2D");
    }

    @Override
    public ScatterGraph2DRenderer createRenderer() {
        return new ScatterGraph2DRenderer(300, 200);
    }

    @Override
    public BufferedImage draw(ScatterGraph2DRenderer renderer) {
        Random rand = new Random(0);
        int size = 1000;
        double[] x = new double[size];
        double[] y = new double[size];
        for (int i = 0; i < size; i++) {
            x[i] = rand.nextGaussian();
            y[i] = rand.nextGaussian();
        }
        Point2DDataset data = Point2DDatasets.lineData(x,y);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_3BYTE_BGR);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        renderer.draw(graphics, data);
        return image;
    }
    
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }
    
    @Test
    public void randomData() throws Exception {
        Random rand = new Random(0);
        int size = 1000;
        double[] x = new double[size];
        double[] y = new double[size];
        for (int i = 0; i < size; i++) {
            x[i] = rand.nextGaussian();
            y[i] = rand.nextGaussian();
        }
        
        Point2DDataset data = Point2DDatasets.lineData(x,y);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_3BYTE_BGR);
        ScatterGraph2DRenderer renderer = new ScatterGraph2DRenderer(300, 200);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        renderer.draw(graphics, data);
        ImageAssert.compareImages("scatterGraph2D.randomData", image);
    }
    
    @Test
    public void regularData() throws Exception {
        Random rand = new Random(0);
        int size = 1000;
        double[] x = new double[] {0,10,20,30,40,50};
        double[] y = new double[] {0,10,20,30,40,50};
        
        Point2DDataset data = Point2DDatasets.lineData(x,y);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_3BYTE_BGR);
        ScatterGraph2DRenderer renderer = new ScatterGraph2DRenderer(300, 200);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        renderer.draw(graphics, data);
        ImageAssert.compareImages("scatterGraph2D.regularData", image);
    }
    
    @Test
    public void oneNaN() throws Exception {
        double[] x = new double[] {Double.NaN,10, 20,30,40,50};
        double[] y = new double[] {Double.NaN,10,20, 30,40,50};
        
        Point2DDataset data = Point2DDatasets.lineData(x,y);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_3BYTE_BGR);
        ScatterGraph2DRenderer renderer = new ScatterGraph2DRenderer(300, 200);
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        renderer.draw(graphics, data);
        ImageAssert.compareImages("scatterGraph2D.oneNaN", image);
    }
    
    @Test
    public void negativeValues() throws Exception {
        double[] x = new double[] {-7, -10, -7, 0, 7, 10, 7};
        double[] y = new double[] {-7, 0, 7, 10, 7, 0, -7};
        
        Point2DDataset data = Point2DDatasets.lineData(x,y);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_3BYTE_BGR);
        ScatterGraph2DRenderer renderer = new ScatterGraph2DRenderer(300, 200);
        renderer.update(renderer.newUpdate().interpolation(InterpolationScheme.LINEAR));
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        renderer.draw(graphics, data);
        ImageAssert.compareImages("scatterGraph2D.negativeValues", image);
    }
    
    @Test
    public void constantXValues() throws Exception {
        double[] x = new double[] {3, 3, 3, 3, 3, 3, 3};
        double[] y = new double[] {0, 1, 2, 3, 4, 5, 6};
        
        Point2DDataset data = Point2DDatasets.lineData(x,y);
        BufferedImage image = new BufferedImage(300, 200, BufferedImage.TYPE_3BYTE_BGR);
        ScatterGraph2DRenderer renderer = new ScatterGraph2DRenderer(300, 200);
        renderer.update(renderer.newUpdate().interpolation(InterpolationScheme.LINEAR));
        Graphics2D graphics = (Graphics2D) image.getGraphics();
        renderer.draw(graphics, data);
        ImageAssert.compareImages("scatterGraph2D.constantXValues", image);
    }
}
