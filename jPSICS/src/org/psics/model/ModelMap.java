package org.psics.model;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.psics.be.E;
import org.psics.quantity.DimensionalExpression;
import org.psics.quantity.DimensionalItem;
import org.psics.quantity.DimensionalQuantity;
import org.psics.quantity.annotation.Container;
import org.psics.quantity.annotation.Identifier;
import org.psics.quantity.annotation.IntegerNumber;
import org.psics.quantity.annotation.Quantity;
import org.psics.quantity.annotation.ReferenceByIdentifier;
import org.psics.quantity.annotation.SubComponent;
import org.psics.quantity.phys.IntegerQuantity;
import org.psics.quantity.phys.PhysicalExpression;
import org.psics.quantity.phys.PhysicalQuantity;


public class ModelMap {

	HashMap<String, DimensionalItem> localHM;

	HashMap<String, Object> idHM;


	public static ModelMap buildMap(Object root) {
		ModelMap ret = new ModelMap();

		HashSet<Object> doneHS = new HashSet<Object>();
		ret.recPopulate(root, doneHS);
		return ret;
	}


	public ModelMap() {
		idHM = new HashMap<String, Object>();
		localHM = new HashMap<String, DimensionalItem>();
	}

	  @SuppressWarnings("unchecked")
	private void recPopulate(Object obj, HashSet<Object> doneHS) {
		if (doneHS.contains(obj)) {
			return;
		}
		doneHS.add(obj);
		ArrayList<Object> toAdd = new ArrayList<Object>();

		try {
			for (Field f : obj.getClass().getFields()) {
				for (Annotation ant : f.getAnnotations()) {


					if (ant instanceof Identifier) {
						String s = (String) (f.get(obj));
						if (s != null && s.trim().length() > 0) {
							idHM.put(s, obj);
						}


					} else if (ant instanceof SubComponent) {
						Object osub = f.get(obj);
						if (osub != null) {
							toAdd.add(osub);
						}


					} else if (ant instanceof Container) {
						ArrayList<? extends Object> alo = (ArrayList<? extends Object>) f.get(obj);

	 					for (Object ch : alo) {

							toAdd.add(ch);
						}

					} else if (ant instanceof ReferenceByIdentifier) {
						Field fo = obj.getClass().getField("r_" + f.getName());
						Object ch = fo.get(obj);
						if (ch != null) {
							toAdd.add(ch);
						}

					} else if (ant instanceof Quantity) {
						String s = f.getName();
						if (localHM.containsKey(s)) {
							// skip;
						} else {
							Object ch = f.get(obj);
							if (ch == null) {
								// OK
							} else if (ch instanceof PhysicalQuantity) {
								localHM.put(s, (PhysicalQuantity)f.get(obj));

							} else if (ch instanceof IntegerQuantity) {
								localHM.put(s, (IntegerQuantity)f.get(obj));

							} else {
								E.warning("field " + f.getName() + " in " + obj + " should be a typed quantity");
							}
						}

					} else if (ant instanceof IntegerNumber) {
						String s = f.getName();
						if (localHM.containsKey(s)) {
							// skip;
						} else {
							Object ch = f.get(obj);
							if (ch == null) {
								// E.warning("null number? " + f.getName());
								// OK
							} else if (ch instanceof IntegerQuantity) {
								localHM.put(s, (IntegerQuantity)f.get(obj));
							} else {
								E.warning("field " + f.getName() + " in " + obj + " should be a typed quantity");
							}
						}
					}



				}
			}
		} catch (Exception ex) {
			E.error("model map exception: " + ex + " while processing " + obj);
		}

		for (Object ch : toAdd) {
			recPopulate(ch, doneHS);
		}
	}
	public DimensionalQuantity getQuantityField(Object obj, String fnm) {
		return getQuantityField(obj, fnm, true);
	}

	public DimensionalQuantity getQuantityField(Object obj, String fnm, boolean reportne) {
		DimensionalQuantity ret = null;
		
		if (obj instanceof ModelElement && ((ModelElement)obj).hasParameter(fnm)) {
			ret = ((ModelElement)obj).getParameter(fnm).getValueDQ();
			
		} else {
		try {
		for (Field f : obj.getClass().getFields()) {
			if (f.getName().equals(fnm)) {
				Object oret = f.get(obj);
				if (oret instanceof DimensionalQuantity) {
					ret = (DimensionalQuantity)oret;
				} else {
					if (reportne) {
						// if reportne is false, we're probably in a query where it
						// is fine to return null
						E.error("found quantity " + fnm + " but has wrong type " + oret);
					}
				}
			}
		}
		} catch (Exception ex) {
			E.error("reflection problems? " + ex);
		}
	}
		return ret;
	}


	public PhysicalExpression getExpressionField(Object obj, String fnm) {
		PhysicalExpression ret = null;
		try {
		for (Field f : obj.getClass().getFields()) {
			if (f.getName().equals(fnm)) {
				Object oret = f.get(obj);
				if (oret instanceof PhysicalExpression) {
					ret = (PhysicalExpression)oret;
				}
			}
		}
		} catch (Exception ex) {
			E.error("reflection problems? " + ex);
		}
		return ret;
	}


	public void setFieldValue(Object obj, String tgt, PhysicalQuantity pqv) {

		for (Field f : obj.getClass().getFields()) {
			if (f.getName().equals(tgt)) {
				try {
					PhysicalQuantity pq = (PhysicalQuantity) (f.get(obj));
					pq.setValue(pqv);

				} catch (Exception ex) {
					E.error("? " + ex);
				}

				break;
			}
		}

	}


	public boolean hasItem(String vary) {
		DimensionalQuantity pq = getQuantity(vary);
		boolean ret = false;
		if (pq != null) {
			ret = true;
		}
		if (!ret) {
			DimensionalExpression pe = getExpression(vary);
			if (pe != null) {
				ret = true;
			}
		}
		return ret;
	}



	public boolean hasQuantityItem(String vary) {
		DimensionalQuantity pq = getQuantity(vary, false);
		boolean ret = false;
		if (pq != null) {
			ret = true;
		}
		return ret;
	}


	public boolean hasExpressionItem(String vary) {
		boolean ret = false;
		DimensionalExpression pe = getExpression(vary);
		if (pe != null) {
			ret = true;
		}
		return ret;
	}


	public DimensionalQuantity getQuantity(String vary) {
		return getQuantity(vary, true);
	}

	public DimensionalQuantity getQuantity(String vary, boolean reportne) {
		DimensionalQuantity ret = null;

		int ic = vary.indexOf(":");
		if (ic > 0) {
			String sid = vary.substring(0, ic);
			String sfn = vary.substring(ic + 1, vary.length());
			if (idHM.containsKey(sid)) {
				E.info("looking for qf " + sfn + " in " + sid + " " + idHM.get(sid));
				ret = getQuantityField(idHM.get(sid), sfn, reportne);
			} else {
				// E.info("qu not found " + sid);
			}

		} else {
			if (localHM.containsKey(vary)) {
				ret = (DimensionalQuantity)localHM.get(vary);
			}
		}
		return ret;
	}


	public DimensionalExpression getExpression(String vary) {
		DimensionalExpression ret = null;

		int ic = vary.indexOf(":");
		if (ic > 0) {
			String sid = vary.substring(0, ic);
			String sfn = vary.substring(ic + 1, vary.length());
			if (idHM.containsKey(sid)) {
				ret = getExpressionField(idHM.get(sid), sfn);
			}


		} else {
			if (localHM.containsKey(vary)) {
				ret = (DimensionalExpression)localHM.get(vary);
			}
		}
		return ret;
	}







	public void printAvailableSimple() {
		{
		StringBuffer sb = new StringBuffer();
		int na = 0;
		for (String s : localHM.keySet()) {
			sb.append(s);
			sb.append(", ");
			na += 1;
			if (na == 8) {
				sb.append("\n");
				na = 0;
			}
		}
		E.info("simple fields: " + sb.toString());
		}
	}

	public void printAvailableObjects() {
		{
			int na = 0;
			StringBuffer sb = new StringBuffer();
			for (String s : idHM.keySet()) {
				if (hasLetters(s)) {
				sb.append(s);
				sb.append(", ");
				na += 1;
				if (na == 8) {
					sb.append("\n");
					na = 0;
				}
				}
			}
			E.info("identified components: " + sb.toString());
			}
	}


	private boolean hasLetters(String s) {
		boolean ret = false;
		for (char c : s.toCharArray()) {
			if (Character.isLetter(c)) {
				ret = true;
			}
		}
		return ret;
	}

}
