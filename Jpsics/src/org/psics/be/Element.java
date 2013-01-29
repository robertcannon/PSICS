package org.psics.be;


 public interface Element extends Named, Elemented, Attributed {

     boolean hasText();

     String getText();

   String serialize();

boolean singleLine();

}
