package org.psics.model.neuroml;

import java.util.ArrayList;

import org.psics.be.AddableTo;
import org.psics.be.E;
import org.psics.be.ImportException;
import org.psics.be.Meta;
import org.psics.be.MetaContainer;
import org.psics.be.MetaItem;
import org.psics.be.Transitional;


public class NeuroML implements AddableTo, Transitional, MetaContainer {
	public String id;

	// TODO read this all into an xmlns hash map;
	public String xmlns;
	public String xmlns_mml;
	public String xmlns_meta;
	public String xmlns_cml;
	public String xmlns_xsi;
	public String xmlns_bio;
	public String xsi_schemaLocation;


	public String name;
	public String lengthUnits;


	public ArrayList<MorphMLCell> cells = new ArrayList<MorphMLCell>();

	public NeuroMLChannels channels;

	public MorphML morphml;

	Meta meta;


	public void addMetaItem(MetaItem mi) {
		if (meta == null) {
			meta = new Meta();
		}
		meta.add(mi);
	}


	public void add(Object obj) {
		if (obj instanceof MorphMLCell) {
			cells.add((MorphMLCell)obj);

		} else if (obj instanceof NeuroMLBiophysics) {
			// just ignore it
		} else {
			E.typeError(obj);
		}
	}


	public MorphMLCell getMorphMLCell() {
		MorphMLCell ret = null;
		if (cells.size() > 0) {
			ret = cells.get(0);
		} else if (morphml != null) {
			ret = morphml.getMorphMLCell();
		}
		return ret;
	}



	public Object getFinal() throws ImportException {
		Object ret = null;
		if (cells.size() > 0) {
			ret = cells.get(0);
		} else if (morphml != null) {
			ret = morphml.getFinal();
		}
		if (ret instanceof Transitional) {
			ret = ((Transitional)ret).getFinal();
		}
		return ret;
	}




}
