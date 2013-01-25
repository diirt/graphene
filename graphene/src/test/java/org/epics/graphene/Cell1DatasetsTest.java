/**
 * Copyright (C) 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 */
package org.epics.graphene;

import org.epics.util.array.ArrayDouble;
import org.epics.util.array.ListNumber;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.hamcrest.Matchers.*;

/**
 *
 * @author carcassi
 */
public class Cell1DatasetsTest {

    @Test
    public void linearRange1() {
        ListNumber values = new ArrayDouble(5, 3, 7, -1, 2);
        Cell1DDataset dataset = Cell1DDatasets.linearRange(values, 0, 10);
        
        assertThat(dataset.getXCount(), equalTo(5));
        assertThat(dataset.getXRange().getMinimum().doubleValue(), equalTo(0.0));
        assertThat(dataset.getXRange().getMaximum().doubleValue(), equalTo(10.0));
        assertThat(dataset.getStatistics().getAverage(), equalTo(3.2));
        assertThat(dataset.getStatistics().getStdDev(), closeTo(2.71293, 0.0001));
        assertThat(dataset.getStatistics().getMinimum(), equalTo((Number) (-1.0)));
        assertThat(dataset.getStatistics().getMaximum(), equalTo((Number) 7.0));
        assertThat(dataset.getStatistics().getCount(), equalTo(5));
        
        // Check values
        assertThat(dataset.getXBoundaries().getDouble(0), equalTo(0.0));
        assertThat(dataset.getValue(0), equalTo(5.0));
        assertThat(dataset.getXBoundaries().getDouble(1), equalTo(2.0));
        assertThat(dataset.getValue(1), equalTo(3.0));
        assertThat(dataset.getXBoundaries().getDouble(2), equalTo(4.0));
        assertThat(dataset.getValue(2), equalTo(7.0));
        assertThat(dataset.getXBoundaries().getDouble(3), equalTo(6.0));
        assertThat(dataset.getValue(3), equalTo(-1.0));
        assertThat(dataset.getXBoundaries().getDouble(4), equalTo(8.0));
        assertThat(dataset.getValue(4), equalTo(2.0));
        assertThat(dataset.getXBoundaries().getDouble(5), equalTo(10.0));
    }
}