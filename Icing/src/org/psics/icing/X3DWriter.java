package org.psics.icing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;


public class X3DWriter {

	BufferedWriter bw;

	float fac = 0.001f;

	public X3DWriter(File f) throws IOException {
		bw = new BufferedWriter(new FileWriter(f));
		bw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		bw.write("<!DOCTYPE X3D PUBLIC \"ISO//Web3D//DTD X3D 3.1//EN\" " +
				"\"http://www.web3d.org/specifications/x3d-3.1.dtd\">\n");
		bw.write("<X3D profile='Immersive' version='3.0' " +
				"xmlns:xsd='http://www.w3.org/2001/XMLSchema-instance' " +
				"xsd:noNamespaceSchemaLocation=' http://www.web3d.org/specifications/x3d-3.0.xsd '>\n");
		bw.write("<head>\n");
		bw.write(" <meta name='generator' content='PSICS-Icing www.psics.org'/>\n");
		bw.write("</head>\n");
		bw.write("<Scene>\n");

		bw.write("<Viewpoint description='0.5mm view' position='0 0 0.5'/>\n");
		bw.write("<Background groundColor='0.05 0.1 0.3' skyColor='0.05 0.1 0.3'/>\n");
	}


	public void close() throws IOException {
		bw.write("</Scene>\n");
		bw.write("</X3D>\n");
		bw.close();
	}

	private void startShape() throws IOException {
		startShape(0.5, 0.5, 0.5);
	}

	private void startShape(double cr, double cg, double cb) throws IOException {
		bw.write("<Shape>\n");
		bw.write("<Appearance>\n");
		String scol = String.format("%4.2g %4.2g %4.2g", cr, cg, cb);
		bw.write("<Material diffuseColor='" + scol + "'/>\n");
		bw.write("</Appearance>\n");
	}

	private void endShape() throws IOException {
		bw.write("</Shape>\n");
	}
	private void startTranslation(double x, double y, double z) throws IOException {
		bw.write("<Transform translation=\"" + x + " " + y + " " + z + "\">\n");
	}

	private void startRotation(float ax, float ay, float az, double aa) throws IOException {
		bw.write("<Transform rotation=\"" + ax + " " + ay + " " + az + " " + aa + "\">\n");
	}

	private void endTransform() throws IOException {
		bw.write("</Transform>\n");
	}

	private void makeSphere(double r) throws IOException {
		bw.write("<Sphere radius='" + r + "'/>\n");
	}


	// N.B. a lot of this is duplicated with SceneGraphBuilder - not ideal

	public void drawTree(IcingPoint[] points) throws IOException {

		for (IcingPoint p : points) {
			double x = p.getX();
			double y = p.getY();
			double z = p.getZ();
			double r = p.getR();

			IcingPoint ppar = p.getParent();

			if (ppar == null) {
				 // may or may not need a ball for it
				if (p.isBall()) {
					startTranslation(fac * x, fac * y, fac * z);
					startShape();
					makeSphere(fac * r);
					endShape();
					endTransform();
				}


			} else {
				double dx = ppar.getX() - x;
				double dy = ppar.getY() - y;
				double dz = ppar.getZ() - z;
				double lxy = Math.sqrt(dx * dx + dy * dy);

				double d = Math.sqrt(dx*dx + dy*dy + dz*dz);

				double ex = Math.atan2(dz, lxy);
				// double ey = 0.;
				double ez = -Math.atan2(dx, dy);


				startTranslation(fac * x, fac * y, fac * z);
				startRotation(0.f, 0.f, 1.f, ez);
				startRotation(1.f, 0.f, 0.f, ex);

				startShape();

				double pr = ppar.getR();
				if (p.isMinor() || p.uniform()) {
					pr = p.getR();
				}

			    mkCarrotoidTriangles(fac * r, fac * pr, fac * d, 22,
							(p.ball ? 5 : 1),  (ppar.ball ? 5 : 1));

				endShape();

				endTransform();
				endTransform();
				endTransform();

			}
		}
	}



	public void addChannels(float[][][] chp, int cr, int cg, int cb) throws IOException {

		for (int i = 0; i < chp.length; i++) {
			float[][] csp = chp[i];

			if (csp != null && csp.length > 0) {

				startShape(cr/255., cg/255., cb/255.);
				makeDonuts(csp);
				endShape();
			}

		}
	}












	private void mkCarrotoidTriangles(double ra, double rb, double d,
			int nside, int ncapa, int ncapb) throws IOException {

		int nstrip = 1 + ncapa + ncapb;
		int nvert = 2 * nside * nstrip;

		int[] svc = new int[nstrip];
		for (int i = 0; i < nstrip; i++) {
			svc[i] = 2 * nside;
		}

		float[] datv = new float[3 * nvert];
		float[] datn = new float[3 * nvert];


		double dtheta = 2. * Math.PI / (nside-1);
		double[][] csas = new double[nside][2];
		double[][] csbs = new double[nside][2];
		for (int i = 0; i <  nside; i++) {
			double tha = i * dtheta;
			double thb = (i + 0.5) * dtheta;
			csas[i][0] = Math.cos(tha);
			csas[i][1] = Math.sin(tha);

			csbs[i][0] = Math.cos(thb);
			csbs[i][1] = Math.sin(thb);
		}

		double dr = ra - rb;
		double znorm = dr / Math.sqrt(dr*dr + d*d);
		double zr = Math.sqrt(1. - znorm*znorm);
		vnStrip(datv, datn, 0, nside, ra, rb, 0., d, znorm, zr, znorm, zr,  csas, csbs);

		int koff = 0;
		koff += 6 * nside;

		double frad = 1.;
		if (ncapa == 1) {
			frad = 0.;
		}

		for (int ic = 0; ic < ncapa; ic++) {
			double[][] incs = (ic % 2 == 0 ? csbs : csas);
			double[][] outcs = (ic % 2 == 0 ? csas : csbs);

			double t0 = ic * (0.5 * Math.PI / (ncapa + 0.1));
			double t1 = (ic + 1) * (0.5 * Math.PI / (ncapa + 0.1));
			double s0 = Math.sin(t0);
			double c0 = Math.cos(t0);
			double s1 = Math.sin(t1);
			double c1 = Math.cos(t1);
			vnStrip(datv, datn, koff, nside, c1 * ra, c0 * ra, -frad * s1 * ra, -frad * s0 * ra, -s1, c1, -s0, c0, incs, outcs);
			koff += 6 * nside;
		}

		frad = 1.;
		if (ncapb == 1) {
			frad = 0.;
		}
		for (int ic = 0; ic < ncapb; ic++) {
			double[][] incs = (ic % 2 == 0 ? csbs : csas);
			double[][] outcs = (ic % 2 == 0 ? csas : csbs);
			double t0 = ic * (0.5 * Math.PI / (ncapb + 0.1));
			double t1 = (ic + 1) * (0.5 * Math.PI / (ncapb + 0.1));
			double s0 = Math.sin(t0);
			double c0 = Math.cos(t0);
			double s1 = Math.sin(t1);
			double c1 = Math.cos(t1);
			vnStrip(datv, datn, koff, nside, c0 * rb, c1 * rb, d + frad * s0 * rb, d + frad * s1 * rb, s0, c0, s1, c1, incs, outcs);
			koff += 6 * nside;
		}

		StringBuffer sbc = new StringBuffer();
		for (int i = 0; i < svc.length; i++) {
			sbc.append(" " + svc[i]);
		}
		bw.write("<TriangleStripSet ccw='true' solid='false' stripCount='" + sbc.toString() + "' " +
				"colorPerVertex='true' normalPerVertex='true' containerField='geometry'>\n");

		StringBuffer sbp = new StringBuffer();
		for (int i = 0; i < datv.length; i++) {
			sbp.append(" " + datv[i]);
		}
		bw.write("<Coordinate point='" + sbp.toString() + "'/>\n");
		bw.write("</TriangleStripSet>\n");
	}



	private void vnStrip(float[] datv, float[] datn, int koff, int nside, double ra, double rb, double da, double db, double s0, double c0, double s1, double c1, double[][] incs, double[][] outcs) {

		for (int i = 0; i < nside; i++) {
			int k = koff + 6 * i;
			datv[k] = (float)(ra * incs[i][0]);
			datv[k+1] = (float)da;
			datv[k+2] = (float)(ra * incs[i][1]);
			datv[k+3] = (float)(rb * outcs[i][0]);
			datv[k+4] = (float)db;
			datv[k+5] = (float)(rb * outcs[i][1]);

			datn[k] = (float)(c0 * incs[i][0]);
			datn[k+1] = (float)s0;
			datn[k+2] = (float)(c0 * incs[i][1]);
			datn[k+3] = (float)(c1 * outcs[i][0]);
			datn[k+4] = (float)s1;
			datn[k+5] = (float)(c1 * outcs[i][1]);
		}
	}






	private void makeDonuts(float[][] csp) throws IOException {
		float eps = (float)(0.1 * fac);

		float heps = 0.3f * eps;

		float rr2 = (float)(1 / Math.sqrt(2.));

		int nc = csp.length;

		int nside = 15;
		int nstrip = 2;

		int nvert = (2 * nside * nstrip) * nc;

		int[] svcs = new int[nc * nstrip];
		for (int i = 0; i < nstrip * nc; i++) {
			svcs[i] = (2 * nside);
		}


		float[] fsa = new float[nside];
		float[] fsb = new float[nside];
		for (int i = 0; i < nside; i++) {
			double th = i * (2. * Math.PI / (nside-1));
			fsa[i] = (float)Math.cos(th);
			fsb[i] = (float)Math.sin(th);
		}



		float[] datv = new float[3 * nvert];
		float[] datn = new float[3 * nvert];

		for (int i = 0; i < nc; i++) {
			float vx = csp[i][5];
			float vy = csp[i][6];
			float vz = csp[i][7];

			float x = fac * csp[i][0] - 0.1f * eps * vx;
			float y = fac * csp[i][1] - 0.1f * eps * vy;
			float z = fac * csp[i][2]  - 0.1f * eps * vz;
			float lxy = (float)Math.sqrt(vx * vx + vy* vy);

			float px = vy / lxy;
			float py = -vx / lxy;
			float pz = 0.f;

			float qx = -vz * py;
			float qy = vz * px;
			float qz = vx*py - vy*px;


			int ko = 3 * (nside * nstrip * 2) * i;


			for (int js = 0; js < nside; js++) {

				float ox = fsa[js] * px + fsb[js] * qx;
				float oy = fsa[js] * py + fsb[js] * qy;
				float oz = fsa[js] * pz + fsb[js] * qz;


				datv[ko] = x + eps * ox + eps * vx;
				datv[ko+1] = y + eps * oy + eps * vy;
				datv[ko+2] = z + eps * oz + eps * vz;

				datn[ko] = rr2 * (ox + vx);
				datn[ko+1] = rr2 * (oy + vy);
				datn[ko+2] = rr2 * (oz + vz);
				ko += 3;

				datv[ko] = x + eps * ox;
				datv[ko+1] = y + eps * oy;
				datv[ko+2] = z + eps * oz;

				datn[ko] = ox;
				datn[ko+1] = oy;
				datn[ko+2] = oz;

				ko += 3;



			}



			for (int js = 0; js < nside; js++) {

				float ox = fsa[js] * px + fsb[js] * qx;
				float oy = fsa[js] * py + fsb[js] * qy;
				float oz = fsa[js] * pz + fsb[js] * qz;




				datv[ko] = x + heps * ox + eps * vx;
				datv[ko+1] = y + heps * oy + eps * vy;
				datv[ko+2] = z + heps * oz + eps * vz;

				datn[ko] = vx;
				datn[ko+1] = vy;
				datn[ko+2] = vz;
				ko += 3;


				datv[ko] = x + eps * ox + eps * vx;
				datv[ko+1] = y + eps * oy + eps * vy;
				datv[ko+2] = z + eps * oz + eps * vz;

				datn[ko] = rr2 * (ox + vx);
				datn[ko+1] = rr2 * (oy + vy);
				datn[ko+2] = rr2 * (oz + vz);
				ko += 3;
			}
		}
		StringBuffer sbc = new StringBuffer();
		for (int i = 0; i < svcs.length; i++) {
			sbc.append(" " + svcs[i]);
		}
		bw.write("<TriangleStripSet ccw='true' solid='false' stripCount='" + sbc.toString() + "' " +
				"colorPerVertex='true' normalPerVertex='true' containerField='geometry'>\n");

		StringBuffer sbp = new StringBuffer();
		for (int i = 0; i < datv.length; i++) {
			sbp.append(" " + datv[i]);
		}
		bw.write("<Coordinate point='" + sbp.toString() + "'/>\n");
		bw.write("</TriangleStripSet>\n");

	}










}
