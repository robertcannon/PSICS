package org.psics.be;

import java.util.ArrayList;



public class Meta implements AddableTo {


	public ArrayList<MetaItem> items = new ArrayList<MetaItem>();

	public void add(Object obj) {
		if (obj instanceof MetaItem) {
			items.add((MetaItem)obj);
		} else {
			E.error("cant add " + obj);
		}
	}

	public MetaItem newItem(String nm) {
		 MetaItem ret = new MetaItem(nm);
		 items.add(ret);
		 return ret;
	}


}
