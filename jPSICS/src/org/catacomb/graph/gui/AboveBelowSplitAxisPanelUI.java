package org.catacomb.graph.gui;

import java.awt.Graphics;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;


final class AboveBelowSplitAxisPanelUI extends BasicSplitPaneUI {

   GraphColors gcols;

   AboveBelowSplitAxisPanelUI(GraphColors gc) {
      super();
      gcols = gc;

   }

   @SuppressWarnings("unused")
   public static ComponentUI createUI(JComponent jcomponent, GraphColors gc) {
         return (new AboveBelowSplitAxisPanelUI(gc));
   }

   public BasicSplitPaneDivider createDefaultDivider() {
      return (new HorizontalAxisAxisDivider(this, gcols));
   }


   public void paint(Graphics graphics, JComponent jcomponent) {

   }


   protected void uninstallDefaults() {
   }

}

