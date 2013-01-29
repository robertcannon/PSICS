package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.ImportException;
import org.psics.be.Transitional;

public class MorphML implements AddableTo, Transitional {

	public String id;

	// TODO read this all into an xmlns hash map;
	public String xmlns;
	public String xmlns_mml;
	public String xmlns_meta;
	public String xmlns_cml;
	public String xmlns_xsi;
	public String xsi_schemaLocation;


	public String name;
	public String lengthUnits;


	public ArrayList<MorphMLCell> cells = new ArrayList<MorphMLCell>();



	public void add(Object obj) {
		if (obj instanceof MorphMLCell) {
			cells.add((MorphMLCell)obj);
		} else {
			E.typeError(obj);
		}
	}



	public MorphMLCell getMorphMLCell() {
		MorphMLCell ret = null;
		if (cells.size() > 0) {
			Object obj = cells.get(0);
			try {
			if (obj instanceof Transitional) {
				obj = ((Transitional)obj).getFinal();
			}
			} catch (Exception ex) {
				E.error("cant convert from " + obj);
			}
			ret = (MorphMLCell)obj;
		}
		return ret;
	}


	public Object getFinal() throws ImportException {
		Object ret = null;
		if (cells.size() > 0) {
			ret = cells.get(0);

//			E.info("forst item in cells is a " + obj);

			// obj not necessarily a MorphMLCell in fact, because of population by reflection
			if (ret instanceof Transitional) {

				ret = ((Transitional)ret).getFinal();

			}
		}
		return ret;
	}



}
