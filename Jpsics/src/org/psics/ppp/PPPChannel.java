package org.psics.ppp;

import java.io.BufferedReader;
import java.io.IOException;

public class PPPChannel {

	String id;

	@SuppressWarnings("unused")
	public void populateFrom(BufferedReader br) throws IOException {
		String sid = br.readLine();
		id = sid.trim();
		// E.info("reading channel " + id);
		String sint = br.readLine();
		int[] wk = PPP.readInts(sint, 5);
		int nv = wk[2];
		int ncomplex = wk[3];

		String sgb = br.readLine();

		for (int icplx = 0; icplx < ncomplex; icplx++) {
			String sscheme = br.readLine();
			String scind = br.readLine();
			for (int i = 0; i < nv; i++) {
				String vline = br.readLine();
			}
		}

	}

}
