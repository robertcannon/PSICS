package org.psics.morph.unused;

import java.io.File;

import java.util.HashMap;

import org.psics.morph.TreePoint;
import org.psics.morph.TreeWriter;


public class TreeBoxDiscretizer {

   TreePoint[] srcPoints;


   public TreeBoxDiscretizer(TreePoint[] points) {
      srcPoints = points;
   }


   public void buildGrid(double d, HashMap<String, Double> resHM) {
      TreeDiscretizer ss = new TreeDiscretizer(srcPoints);

      TreePoint[] slicedPoints = ss.getFixedWidthSlices(d, resHM);

      TreeWriter tw = new TreeWriter(slicedPoints);
      tw.writeSWC(new File("discretized-tree.swc"));


   }


}
