package org.psics.write;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

import org.psics.be.ContainerForm;
import org.psics.be.E;
import org.psics.be.IDd;
import org.psics.be.LongNamed;
import org.psics.be.TextForm;
import org.psics.be.Textalizer;

public class ModelTextalizer implements Textalizer {

	ArrayList<Object> components = new ArrayList<Object>();
	HashSet<Object> doneHS = new HashSet<Object>();


	public void add(Object obj) {
		components.add(obj);

	}

	public String makeText() {
		StringBuffer sb = new StringBuffer();

		for (Object obj : components) {
			sb.append(makeBlock(obj));
			sb.append("\n\n");
		}

		return sb.toString();
	}


	  @SuppressWarnings("unchecked")
	public String makeBlock(Object ob) {
		StringBuffer sb = new StringBuffer();

		if (ob instanceof LongNamed) {
			sb.append(((LongNamed)ob).getLongName());
		} else {
			sb.append(ob.getClass().getName());
		}
		sb.append(" ");
		if (ob instanceof IDd) {
			sb.append(((IDd)ob).getID());
		} else {
			sb.append("anon");
		}
		sb.append(" ");


		ArrayList<WritableField> wfs = new ArrayList<WritableField>();

		for (Field f : ob.getClass().getFields()) {
			int im = f.getModifiers();
			if (Modifier.isPublic(im)) {
				Annotation[] aa = f.getAnnotations();

			    if (aa != null && aa.length > 0) {
			    	try {
			    	Object fv = f.get(ob);

			    	if (fv != null) {
			    		for (Annotation ant : aa) {
			    			if (ant instanceof TextForm) {
			    				TextForm tf = (TextForm)ant;
			    				if (tf.pos() >= 0) {
			    					WritableField wf = new ValueField(f.getName(), tf.pos(), fv, tf);
			    					wfs.add(wf);
			    				}
			    			}
			    			if (ant instanceof ContainerForm) {
			    				ContainerForm cf = (ContainerForm)ant;
			    				if (cf.pos() >= 0) {
			    					ArrayList<Object> alo = (ArrayList<Object>)fv;
			    					ContainerField cfld = new ContainerField(f.getName(), cf.pos(), alo, cf);
			    					wfs.add(cfld);
			    				}
			    			}
			    		}
			    	}


			    	} catch (IllegalAccessException ex) {
			    		E.info("cant access " + f + " on " + ob);
			    	}
			    }
			}
		}



		Collections.sort(wfs);

		ListPosition prev = ListPosition.LAST;

		int n = wfs.size();
		for (WritableField wf : wfs) {
			n += 1;
			wf.setTextalizer(this);

			ListPosition cur = ListPosition.SOLE;

			boolean islast = false;
			if (wf instanceof ContainerField || n == wfs.size()) {
				islast = true;
			}

			if (prev == ListPosition.LAST) {
				if (islast) {
					cur = ListPosition.SOLE;
				} else {
					cur = ListPosition.FIRST;
				}

			} else if (islast) {
				cur = ListPosition.LAST;

			} else {
				cur = ListPosition.INNER;
			}

			sb.append(wf.write(cur));
			prev = cur;

		}

		return sb.toString();
	}



}
