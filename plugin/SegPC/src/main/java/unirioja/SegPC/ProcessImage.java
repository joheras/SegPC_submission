package unirioja.SegPC;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.Roi;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;
import ij.process.ImageStatistics;

public class ProcessImage {

	public static void processImageHE2(ImagePlus imp, String dir) {
		String name = imp.getTitle();
		name = name.substring(0, name.lastIndexOf('.'));
		imp.show();
		IJ.run(imp, "Colour Deconvolution", "vectors=[H&E 2]");
		WindowManager.getImage(imp.getTitle() + "-(Colour_3)").close();
		WindowManager.getImage(imp.getTitle() + "-(Colour_2)").close();
		imp = WindowManager.getImage(imp.getTitle() + "-(Colour_1)");
		IJ.setAutoThreshold(imp, "Default");
		IJ.run(imp, "Convert to Mask", "");
		IJ.run(imp, "Fill Holes", "");
		IJ.run(imp, "Watershed", "");
		IJ.run(imp, "Analyze Particles...", "size=10000-150000 circularity=0.50-1.00 add");
		ImagePlus imp1 = IJ.createImage("Untitled", "8-bit black", imp.getWidth(), imp.getHeight(), 1);
		// IJ.setTool("dropper");
		// IJ.setTool("rectangle");
		imp.changes = false;
		imp.close();
		IJ.setForegroundColor(40, 40, 40);
		RoiManager rm = RoiManager.getInstance();
		if (rm != null && rm.getRoisAsArray().length > 0) {
			rm.runCommand(imp1, "Fill");
			rm.close();
		}

		IJ.saveAs(imp1, "BMP", dir + "/he2/" + name + ".bmp");
		imp1.changes = false;
		imp1.close();

		WindowManager.closeAllWindows();
	}

	public static void processImageVoronoiMaskRCNNCombiningOriginal(ImagePlus imp, String dir) {
		IJ.run("Set Measurements...", "mean redirect=None decimal=3");
		IJ.setBackgroundColor(0, 0, 0);

		ImagePlus imp1;
		IJ.setAutoThreshold(imp, "Default dark");
		IJ.setRawThreshold(imp, 200, 255, null);
		IJ.run(imp, "Convert to Mask", "");

		ImagePlus impT = imp.duplicate();

		IJ.run(impT, "Dilate", "");
		IJ.run(impT, "Dilate", "");
		IJ.run(impT, "Dilate", "");
		IJ.run(impT, "Dilate", "");
		IJ.run(impT, "Dilate", "");
		IJ.run(impT, "Fill Holes", "");
		IJ.run(impT, "Replace value", "pattern=255 replacement=20");

		IJ.run(imp, "Voronoi", "");
		IJ.setRawThreshold(imp, 0, 1, null);
		IJ.run(imp, "Analyze Particles...", "size=10000-Infinity add");
		RoiManager rm = RoiManager.getInstance();
		String name = imp.getTitle();
		name = name.substring(0, name.lastIndexOf('.'));

		imp1 = IJ.openImage(dir + "/combination/" + name + ".bmp");
		IJ.run(imp1, "8-bit", "");
		ImageCalculator ic = new ImageCalculator();
		imp1 = ic.run("Max create", imp1, impT);

		ImagePlus imp2;
		ImagePlus imp3;
		int counter = 0;
		Roi[] rois = rm.getRoisAsArray();
		rm.runCommand("Delete");
		for (int i = 0; i < rois.length; i++) {
			imp2 = imp1.duplicate();
			imp2.setRoi(rois[i]);
			IJ.run(imp2, "Clear Outside", "");

			if (isBlack(imp2)) {// && containsNucleus(imp2)
				ImagePlus imp4 = imp2.duplicate();
				IJ.setAutoThreshold(imp4, "Default dark");
				IJ.setRawThreshold(imp4, 20, 255, null);
				IJ.run(imp4, "Convert to Mask", "");
				IJ.run(imp4, "Analyze Particles...", "size=0-Infinity add");
				rm = RoiManager.getInstance();
				Utils.keepBiggestROI(rm);
				imp2.setRoi(rm.getRoi(0));
				IJ.run(imp2, "Clear Outside", "");

				IJ.run(imp2, "8-bit", "");

				/// Modification
				ImagePlus impT2 = IJ.openImage(dir + "/images/" + name + ".bmp");
				IJ.run(impT2, "8-bit", "");
				impT2.setRoi(rm.getRoi(0));
				IJ.setAutoThreshold(impT2, "Default");
				IJ.run(impT2, "Convert to Mask", "");
				IJ.run(impT2, "Dilate", "");
				IJ.run(impT2, "Fill Holes", "");

				IJ.run(impT2, "Replace value", "pattern=255 replacement=40");
				impT2.setRoi(rm.getRoi(0));
				IJ.run(impT2, "Clear Outside", "");
				IJ.run(impT2, "Replace value", "pattern=255 replacement=0");
				rm.runCommand("Delete");

				imp2 = ic.run("Max create", imp2, impT2);
				impT2.changes = false;
				impT2.close();

				/*
				 * impT3.changes = false; impT3.close();
				 */

				///

				if (isBlack(imp2) && containsNucleus(imp2)) {
					IJ.saveAs(imp2, "BMP", dir + "/preds/" + name + "_" + (counter + 1) + ".bmp");
					counter++;
				}

			}
			imp2.changes = false;
			imp2.close();

		}
		imp.changes = false;
		imp.close();
		impT.changes = false;
		impT.close();
		imp1.changes = false;
		imp1.close();
		// rm.runCommand("Delete");
		rm.close();
		WindowManager.closeAllWindows();
	}

	protected static boolean isBlack(ImagePlus imp) {
		ImageStatistics is = imp.getStatistics();
		double m2 = is.mean;
		return m2 > 0;
	}

	protected static boolean containsNucleus(ImagePlus imp) {
		ImagePlus imp3 = imp.duplicate();
		IJ.run(imp3, "8-bit", "");
		IJ.setAutoThreshold(imp3, "Default dark");
		IJ.setRawThreshold(imp3, 40, 255, null);
		IJ.run(imp3, "Convert to Mask", "");
		ImageStatistics is = imp3.getStatistics();
		double m2 = is.mean;
		imp3.close();
		return m2 > 0;
	}
}
