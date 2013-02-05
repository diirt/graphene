/**
 * Copyright (C) 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.graphene;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import org.epics.util.array.ArrayInt;
import org.epics.util.array.ListInt;

/**
 *
 * @author carcassi
 */
public abstract class Graph2DRenderer<T extends Graph2DRendererUpdate> {
    protected double endXPlot;
    protected double endYPlot;
    protected int plotHeight;
    protected int plotWidth;
    protected double startXPlot;
    protected double startYPlot;
    protected int xEndGraph;
    protected int xStartGraph;
    protected int yEndGraph;
    protected int yStartGraph;
    private ListInt verticalTickPositions;

    public Graph2DRenderer(int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
    }
    
    private int imageWidth;
    private int imageHeight;

    public int getImageHeight() {
        return imageHeight;
    }

    public int getImageWidth() {
        return imageWidth;
    }
    
    private AxisRange xAxisRange = AxisRanges.integrated();
    private AxisRange yAxisRange = AxisRanges.integrated();
    private Range xAggregatedRange;
    private Range yAggregatedRange;
    private Range xPlotRange;
    private Range yPlotRange;

    public AxisRange getXAxisRange() {
        return xAxisRange;
    }

    public AxisRange getYAxisRange() {
        return yAxisRange;
    }

    public Range getXAggregatedRange() {
        return xAggregatedRange;
    }

    public Range getYAggregatedRange() {
        return yAggregatedRange;
    }

    public Range getXPlotRange() {
        return xPlotRange;
    }

    public Range getYPlotRange() {
        return yPlotRange;
    }

    public void update(T update) {
        if (update.getImageHeight() != null) {
            imageHeight = update.getImageHeight();
        }
        if (update.getImageWidth() != null) {
            imageWidth = update.getImageWidth();
        }
        if (update.getXAxisRange() != null) {
            xAxisRange = update.getXAxisRange();
        }
        if (update.getYAxisRange() != null) {
            yAxisRange = update.getYAxisRange();
        }
    }
    
    static Range aggregateRange(Range dataRange, Range aggregatedRange) {
        if (aggregatedRange == null) {
            return dataRange;
        } else {
            return RangeUtil.sum(dataRange, aggregatedRange);
        }
    }
    
    public abstract T newUpdate();
    
    protected void calculateRanges(Range xDataRange, Range yDataRange) {
        xAggregatedRange = aggregateRange(xDataRange, xAggregatedRange);
        yAggregatedRange = aggregateRange(yDataRange, yAggregatedRange);
        xPlotRange = xAxisRange.axisRange(xDataRange, xAggregatedRange);
        yPlotRange = yAxisRange.axisRange(yDataRange, yAggregatedRange);
    }

    protected void drawAxis(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Color.BLACK);
        // Determine range of the plot.
        // If no range is set, use the one from the dataset
        startXPlot = getXPlotRange().getMinimum().doubleValue();
        startYPlot = getYPlotRange().getMinimum().doubleValue();
        endXPlot = getXPlotRange().getMaximum().doubleValue();
        endYPlot = getYPlotRange().getMaximum().doubleValue();
        int margin = 3;
        // Compute axis
        ValueAxis xAxis = ValueAxis.createAutoAxis(startXPlot, endXPlot, Math.max(2, getImageWidth() / 60));
        ValueAxis yAxis = ValueAxis.createAutoAxis(startYPlot, endYPlot, Math.max(2, getImageHeight() / 60));
        HorizontalAxisRenderer xAxisRenderer = new HorizontalAxisRenderer(xAxis, margin, g);
        yAxisRenderer = new VerticalAxisRenderer(yAxis, margin, g);
        // Compute graph area
        xStartGraph = yAxisRenderer.getAxisWidth();
        xEndGraph = getImageWidth() - margin;
        yStartGraph = margin;
        yEndGraph = getImageHeight() - xAxisRenderer.getAxisHeight();
        plotWidth = xEndGraph - xStartGraph;
        plotHeight = yEndGraph - yStartGraph;
        // Draw axis
        xAxisRenderer.draw(g, 0, xStartGraph, xEndGraph, getImageWidth(), yEndGraph);
        yAxisRenderer.draw(g, 0, yStartGraph, yEndGraph, getImageHeight(), xStartGraph);
        // Draw reference lines
        g.setColor(new Color(240, 240, 240));
        int[] xTicks = xAxisRenderer.horizontalTickPositions();
        for (int xTick : xTicks) {
            g.drawLine(xTick, yStartGraph, xTick, yEndGraph);
        }
        int[] yTicks = yAxisRenderer.verticalTickPositions();
        for (int yTick : yTicks) {
            g.drawLine(xStartGraph, getImageHeight() - yTick, xEndGraph, getImageHeight() - yTick);
        }
    }
    
    private VerticalAxisRenderer yAxisRenderer;
    
    protected void drawHorizontalReferenceLines(Graphics2D g) {
        int[] yTicks = yAxisRenderer.verticalTickPositions();
        for (int yTick : yTicks) {
            g.drawLine(xStartGraph, getImageHeight() - yTick, xEndGraph, getImageHeight() - yTick);
        }
    }
    
    protected Color backgroundColor = Color.WHITE;

    protected void drawBackground(Graphics2D g) {
        g.setColor(backgroundColor);
        g.fillRect(0, 0, getImageWidth(), getImageHeight());
    }

    protected final double scaledX(double value) {
        return xStartGraph + NumberUtil.scale(value, startXPlot, endXPlot, plotWidth);
    }

    protected final double scaledY(double value) {
        return yEndGraph - NumberUtil.scale(value, startYPlot, endYPlot, plotHeight);
    }
    
    protected void setClip(Graphics2D g) {
        // Make sure that the line does not go ouside the chart
        g.setClip(xStartGraph - 1, yStartGraph - 1, plotWidth + 2, plotHeight + 2);
    }

}