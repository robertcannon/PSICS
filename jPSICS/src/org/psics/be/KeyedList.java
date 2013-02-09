package org.psics.be;

import java.util.ArrayList;
import java.util.HashMap;

public class KeyedList<V> {

  ArrayList<V> items;
  HashMap<String, V> listHM;

  Class<V> itemClass;

public KeyedList() {
     items = new ArrayList<V>();
     listHM = new HashMap<String, V>();
  }

  public KeyedList(Class<V> c) {
     this();
     itemClass = c;
  }
  
  
  public int size() {
	  return items.size();
  }


  @SuppressWarnings("unchecked")
public KeyedList(String s) {
     this();
     try {
       itemClass = (Class<V>)Class.forName(s);
     } catch (Exception ex) {
        E.error("cant find class " + s + " " + ex);
     }
  }





  public ArrayList<V> getItems() {
     return items;
  }

  public boolean hasItem(String s) {
     return listHM.containsKey(s);
  }

  public V getItem(String s) {
     V ret = null;
     if (listHM.containsKey(s)) {
        ret = listHM.get(s);
     }
     return ret;
  }




  public void add(V obj) {

     String sid = "";
     if (obj instanceof IDd) {
        sid = ((IDd)obj).getID();
     } else {
        sid = obj.toString();
     }
     add(sid, obj);
  }



  public void add(String sid, V obj) {

     if (listHM.containsKey(sid)) {
        E.error("adding a duplicate in a keyed list? " + sid);

     } else {
        items.add(obj);
        listHM.put(sid, obj);
     }
  }

  // TODO this is slow, but ok if key may have changed
  public void remove(V obj) {
	  items.remove(obj);
	  String ky = null;
	  for (String s : listHM.keySet()) {
		  if (listHM.get(s).equals(obj)) {
			  ky = s;
		  }
	  }
	  if (ky != null) {
		  listHM.remove(ky);
	  } else {
		  E.error("removing something that isnt in the list? " + obj);
	  }

  }

  public void moveUp(V p) {
	  int ind = items.indexOf(p);
	  if (ind > 0) {
		  items.remove(p);
		  items.add(ind - 1, p);
	  }
  }

  public void moveDown(V p) {
	  int ind = items.indexOf(p);
	  if (ind < items.size() - 1) {
		  items.remove(p);
		  items.add(ind + 1, p);
	  }
  }



}
