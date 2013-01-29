package org.psics.icing3d;

import java.awt.Color;

import javax.media.j3d.Appearance;
import javax.media.j3d.BranchGroup;
import javax.media.j3d.GeometryArray;
import javax.media.j3d.Material;
import javax.media.j3d.PointAttributes;
import javax.media.j3d.Shape3D;
import javax.media.j3d.TriangleArray;
import javax.media.j3d.TriangleFanArray;
import javax.media.j3d.TriangleStripArray;
import javax.vecmath.Color3f;

import org.psics.begui.Visualizer;


public class ChannelGraphBuilder {

	float[][][] pos = null;

	BranchGroup root;
	Color color;

	Appearance defaultAppearance;

	public ChannelGraphBuilder(float[][][] ds, Color c) {
		pos = ds;
		color = c;
		defaultAppearance = makeDefaultAppearance(color);
	}



	private Appearance makeDefaultAppearance(Color c) {
		float fr = c.getRed() / 255.f;
		float fg = c.getGreen() / 255.f;
		float fb = c.getBlue() / 255.f;


		float famb = 0.5f;
		float fem = 0.2f;
		float fdiff = 1.0f;
		float fspec = 1.0f;

		Color3f aColor  = new Color3f(famb * fr, famb * fg, famb * fb); // ambient
	    Color3f eColor  = new Color3f(fem * fr, fem * fg, fem * fb); // emissive
	    Color3f dColor  = new Color3f(fdiff * fr, fdiff * fg, fdiff * fb); // diffuse
	    Color3f sColor  = new Color3f(fspec * fr, fspec * fg, fspec * fb); // specular

	    Material m = new Material(aColor, eColor, dColor, sColor, 90.0f);
	    // specularity is on a range form 0 to 128
	    Appearance a = new Appearance();
	    m.setLightingEnable(true);
	    a.setMaterial(m);

	    PointAttributes pa = new PointAttributes();
	    pa.setPointSize(3.f);
	    a.setPointAttributes(pa);
	    return a;
	}



	public BranchGroup getRoot() {
		return root;
	}


	public void build(int res, double fac) {
		root = new BranchGroup();
		int ntr = 0;
		int ncset = pos.length;

		float ffac = (float)fac;


		for (int i = 0; i < ncset; i++) {
			float[][] csp = pos[i];

			if (csp != null && csp.length > 0) {
				ntr += csp.length;
			    Shape3D shape = new Shape3D();
			    shape.setAppearance(defaultAppearance);


			    if (res == Visualizer.LOW) {
			    	// GeometryArray ta = makePointsArray(csp);
			    	GeometryArray ta = makeTriangleArray(csp, ffac);
			    	shape.setGeometry(ta);

			    } else if (res == Visualizer.MEDIUM) {
			    	GeometryArray ta = makePyramids(csp, ffac);
			    	shape.setGeometry(ta);

			    } else if (res == Visualizer.HIGH) {
			    	GeometryArray ta = makeDonuts(csp, ffac);
			    	shape.setGeometry(ta);
			    }

			    root.addChild(shape);
			}

		}
	}




// NB it is no good making one channel model and referencing it multiple times - too much memory

/*

		} else if (res == Visualizer.HIGH) {
			Shape3D shape = new Shape3D();
			GeometryArray ta = makeDonut();
			shape.setGeometry(ta);

			SharedGroup sgn = new SharedGroup();
			sgn.addChild(shape);

			shape.setAppearance(defaultAppearance);

			for (int i = 0; i < ncset; i++) {
				float[][] csp = pos[i];

				if (csp != null && csp.length > 0) {
					ntr += csp.length;
					for (int itr = 0; itr < csp.length; itr++) {

						float[] cp = csp[itr];

						Transform3D trans = new Transform3D();
						trans.setTranslation(new Vector3f(fac * cp[0], fac * cp[1], fac * cp[2]));

						TransformGroup tg = new TransformGroup();
						tg.setTransform(trans);

						tg.addChild(new Link(sgn));
						root.addChild(tg);
					}
				}
			}


		}

	//	E.info("built cg with nch=" + ntr);
	}

*/



	private GeometryArray makeTriangleArray(float[][] csp, float fac) {
		float eps = (float)(0.25 * fac);

		int nc = csp.length;

		int nvert = 3 * nc;

		float[] datv = new float[3 * nvert];
		float[] datn = new float[3 * nvert];

		for (int i = 0; i < nc; i++) {
			float vx = csp[i][5];
			float vy = csp[i][6];
			float vz = csp[i][7];

			float x = (float)(fac * (csp[i][0] + 0.01 * vx));
			float y = (float)(fac * (csp[i][1] + 0.01 * vy));
			float z = (float)(fac * (csp[i][2] + 0.01 * vz));



			float lxy = (float)Math.sqrt(vx * vx + vy* vy);

			float px = eps * vy / lxy;
			float py = -eps * vx / lxy;
			float pz = 0.f;


			float qx = -vz * py;
			float qy = vz * px;
			float qz = vx*py - vy*px;

			int ko = 9 * i;

			datv[ko] = x - 0.6f * px;
			datv[ko+1] = y - 0.6f * py;
			datv[ko+2] = z - 0.6f * pz;

			datv[ko+3] = x + 0.3f * px - 0.5f * qx;
			datv[ko+4] = y + 0.3f * py - 0.5f * qy;
			datv[ko+5] = z + 0.3f * pz - 0.5f * qz;

			datv[ko+6] = x + 0.3f * px + 0.5f * qx;
			datv[ko+7] = y + 0.3f * py + 0.5f * qy;
			datv[ko+8] = z + 0.3f * pz + 0.5f * qz;


			datn[ko] = vx;
			datn[ko+1] = vy;
			datn[ko+2] = vz;

			datn[ko+3] = vx;
			datn[ko+4] = vy;
			datn[ko+5] = vz;

			datn[ko+6] = vx;
			datn[ko+7] = vy;
			datn[ko+8] = vz;
		}


		TriangleArray ret = new TriangleArray(nvert, GeometryArray.COORDINATES | GeometryArray.NORMALS);
		ret.setCoordinates(0, datv);
		ret.setNormals(0, datn);

		return ret;
	}


	@SuppressWarnings("unused")
	private GeometryArray makePyramids(float[][] csp, float fac) {
		double ff = 0.001; // *******
		float eps = (float)(0.1 * ff);

		float rr2 = (float)(1 / Math.sqrt(2.));

		int nc = csp.length;

		int nside = 8;

		int nvert = (nside + 1) * nc;

		int[] svcs = new int[nc];
		for (int i = 0; i < nc; i++) {
			svcs[i] = (nside + 1);
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

			float x = (float)(ff * csp[i][0]);
			float y = (float)(ff * csp[i][1]);
			float z = (float)(ff * csp[i][2]);



			float lxy = (float)Math.sqrt(vx * vx + vy* vy);

			float px = vy / lxy;
			float py = -vx / lxy;
			float pz = 0.f;


			float qx = -vz * py;
			float qy = vz * px;
			float qz = vx*py - vy*px;

			int ko = 3 * (nside + 1) * i;

			datv[ko] = x + eps * vx;
			datv[ko+1] = y  + eps * vy;
			datv[ko+2] = z  + eps * vz;

			datn[ko] = vx;
			datn[ko+1] = vy;
			datn[ko+2] = vz;

			for (int js = 0; js < nside; js++) {
				ko += 3;

				float ox = fsa[js] * px + fsb[js] * qx;
				float oy = fsa[js] * py + fsb[js] * qy;
				float oz = fsa[js] * pz + fsb[js] * qz;

				datv[ko] = x + eps * ox;
				datv[ko+1] = y + eps * oy;
				datv[ko+2] = z + eps * oz;

				datn[ko] = rr2 * (ox + vx);
				datn[ko+1] = rr2 * (oy + vy);
				datn[ko+2] = rr2 * (oz + vz);

			}


		}


		TriangleFanArray ret = new TriangleFanArray(nvert,
				GeometryArray.COORDINATES | GeometryArray.NORMALS, svcs);
		ret.setCoordinates(0, datv);
		ret.setNormals(0, datn);

		return ret;
	}








	private GeometryArray makeDonuts(float[][] csp, float fac) {
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

		TriangleStripArray ret = new TriangleStripArray(nvert,
				GeometryArray.COORDINATES | GeometryArray.NORMALS, svcs);
			ret.setCoordinates(0, datv);
			ret.setNormals(0, datn);
			return ret;
	}




/*
	private GeometryArray makeDonut(float fac) {

		int na = 20;

		int nvert = 2 * na;
		int[] svc = {2 * na}; // length of successive strips in array

		float[] datv = new float[3 * nvert];
		float[] datn = new float[3 * nvert];

		float rh = (float)Math.sqrt(0.5);

		double da = 2. * Math.PI / (na - 1.);
		for (int i = 0; i < na; i++) {
			double a = i * da;
			double ah = (i + 0.5) * da;

			float ca = (float)Math.cos(a);
			float sa = (float)Math.sin(a);

			float cha = (float)Math.cos(ah);
			float sha = (float)Math.sin(ah);

			datv[6 * i] = fac * ca;
			datv[6 * i + 1] = fac * sa;
			datv[6 * i + 2] = 0.f;
			datv[6 * i + 3] = fac * cha;
			datv[6 * i + 4] = fac * sha;
			datv[6 * i + 5] = fac;

			datn[6 * i] = fac * ca;
			datn[6 * i + 1] = fac * sa;
			datn[6 * i + 2] = 0.f;
			datn[6 * i + 3] = rh * fac * cha;
			datn[6 * i + 4] = rh * fac * sha;
			datn[6 * i + 5] = rh;
		}


		TriangleStripArray ret = new TriangleStripArray(nvert,
			GeometryArray.COORDINATES | GeometryArray.NORMALS, svc);
		ret.setCoordinates(0, datv);
		ret.setNormals(0, datn);
		return ret;
	}
*/


/*
	private GeometryArray makePointsArray(float[][] csp) {
		double ff = 0.001; // *******
	 	int nc = csp.length;
		float[] pointv = new float[3 * nc];

		for (int i = 0; i < nc; i++) {
			float x = (float)(ff * csp[i][0]);
			float y = (float)(ff * csp[i][1]);
			float z = (float)(ff * csp[i][2]);

			int k = 3 * i;
			pointv[k] = x;
			pointv[k+1] = y;
			pointv[k+2] = z;
		}
		PointArray ret = new PointArray(nc, GeometryArray.COORDINATES);
		ret.setCoordinates(0, pointv);
		return ret;
	}
*/


}
