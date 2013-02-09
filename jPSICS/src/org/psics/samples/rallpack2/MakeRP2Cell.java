package org.psics.samples.rallpack2;

import java.io.File;

import org.psics.be.E;
import org.psics.util.FileUtil;

/*
The detailed geometry of the compartments is
Depth		Number		Length (microns)	Diameter (microns)
0		1		32.0			16.0
1		2		25.4			10.08
2		4		20.16			6.35
3		8		16.0			4.0
4		16		12.7			2.52
5		32		10.08			1.587
6		64		8.0			1.0
7		128		6.35			0.63
8		256		5.04			0.397
9		512		4.0			0.25
*/

public class MakeRP2Cell {



	public static void main(String[] argv) {
		double length = 32;
		int n = 1;
		double radius = 8.;



		double fl = Math.pow(2., 1./3.);
		double fr = fl * fl;

		StringBuffer sb = new StringBuffer();
		sb.append("<CellMorphology id=\"cell\">\n");
		sb.append("<Point id=\"p0\" x=\"0\" y=\"0\" z=\"0\" r=\"" + radius + "\"/>\n");
		sb.append("<Point id=\"p1\" parent=\"p0\" minor=\"true\" x=\"" + length + "\" y=\"0\" z=\"0\" r=\"" + radius + "\"/>\n");

		int ipn = 2;
		double dy = 20.;

		double[] yoffs  = new double[1];
		yoffs[0] = 0.;
		double xtot = length;
		int[] pidx = {1};


		for (int ilev = 1; ilev < 10; ilev++) {
			 length = length / fl;
			 radius = radius / fr;

			 E.info("length and dima for level " + ilev +" " + length + " " + (2 * radius));

			 double[] yoffnew = new double[2 * n];
			 int[] pidxnew = new int[2 * n];
			 int inew = 0;

			 double dx = Math.sqrt(length*length - dy * dy);
			 xtot += dx;

			 for (int i = 0; i < n; i++) {
				 double y = yoffs[i] - dy;
				 yoffnew[inew] = y;
				 pidxnew[inew] = ipn;
				 sb.append("<Point id=\"p" + ipn + "\" parent=\"p" + pidx[i] + "\" minor=\"true\" x=\"" + xtot + "\" y=\"" + y + "\" z=\"0\" r=\"" + radius + "\"/>\n");
				 ipn += 1;
				 inew += 1;

				 y = yoffs[i] + dy;
				 yoffnew[inew] = y;
				 pidxnew[inew] = ipn;
				 sb.append("<Point id=\"p" + ipn + "\" parent=\"p" + pidx[i] + "\" minor=\"true\" x=\"" + xtot + "\" y=\"" + y + "\" z=\"0\" r=\"" + radius + "\"/>\n");
				 ipn += 1;
				 inew += 1;
			 }

			 yoffs = yoffnew;
			 pidx = pidxnew;


			 n *= 2;
			 dy /= 2.;
		}


		sb.append("</CellMorphology>\n");
		FileUtil.writeStringToFile(sb.toString(), new File("cell.xml"));
	}


}
