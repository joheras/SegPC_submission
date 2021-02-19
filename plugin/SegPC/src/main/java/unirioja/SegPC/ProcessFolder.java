package unirioja.SegPC;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.ImageCalculator;
import ij.plugin.frame.RoiManager;

public class ProcessFolder {

	public static String step1() throws IOException {
		List<String> files = Utils.searchFiles();
		String dir = files.get(0);
		// Create save directory
		files.remove(0);
		JFrame frame = new JFrame("Work in progress");
		JProgressBar progressBar = new JProgressBar();
		int n = 0;
		progressBar.setValue(0);
		progressBar.setString("");
		progressBar.setStringPainted(true);
		progressBar.setMaximum(files.size());
		Border border = BorderFactory.createTitledBorder("Processing Step 1...");
		progressBar.setBorder(border);
		Container content = frame.getContentPane();
		content.add(progressBar, BorderLayout.NORTH);
		frame.setSize(300, 100);
		frame.setVisible(true);

		Path path1 = Paths.get(dir + "/preds");
		Path path2 = Paths.get(dir + "/he2");

		// java.nio.file.Files;

		Files.createDirectories(path1);
		Files.createDirectories(path2);

		// For each file in the folder we detect the esferoid on it.
		for (String name : files) {
			ImagePlus imp = IJ.openImage(name);
			// Utils.processImageGiemsa(imp);
			ProcessImage.processImageVoronoiMaskRCNNCombiningOriginal(imp, dir);
			n++;
			progressBar.setValue(n);

		}
		frame.setVisible(false);
		frame.dispose();
		RoiManager rm = RoiManager.getRoiManager();
		rm.setVisible(true);
		rm.close();
		// IJ.showMessage("Process finished");
		return dir;

	}

	public static void step2(String dir) {

		List<String> files = new ArrayList<String>();

		File folder = new File(dir + "/preds/");

		Utils.search(".*bmp", folder, files);

		Collections.sort(files);

		JFrame frame = new JFrame("Work in progress");
		JProgressBar progressBar = new JProgressBar();
		int n = 0;
		progressBar.setValue(0);
		progressBar.setString("");
		progressBar.setStringPainted(true);
		progressBar.setMaximum(files.size());
		Border border = BorderFactory.createTitledBorder("Processing Step 2...");
		progressBar.setBorder(border);
		Container content = frame.getContentPane();
		content.add(progressBar, BorderLayout.NORTH);
		frame.setSize(300, 100);
		frame.setVisible(true);

		// For each file in the folder we detect the esferoid on it.
		for (String name : files) {
			ImagePlus imp = IJ.openImage(name);
			imp = postProcess(imp);
			IJ.saveAs(imp, "BMP", name);
			n++;
			progressBar.setValue(n);

		}
		frame.setVisible(false);
		frame.dispose();
		RoiManager rm = RoiManager.getRoiManager();
		rm.setVisible(true);
		rm.close();
		// IJ.showMessage("Process finished");
	}

	private static ImagePlus postProcess(ImagePlus imp1) {
		ImagePlus imp = imp1.duplicate();
		ImagePlus imp2 = imp.duplicate();
		IJ.setAutoThreshold(imp, "Default dark");
		IJ.setRawThreshold(imp, 20, 255, null);
		IJ.run(imp, "Convert to Mask", "");

		IJ.run(imp, "Replace value", "pattern=255 replacement=20");

		IJ.setAutoThreshold(imp2, "Default dark");
		IJ.setRawThreshold(imp2, 40, 255, null);
		IJ.run(imp2, "Convert to Mask", "");

		IJ.run(imp2, "Analyze Particles...", "size=0-Infinity add");
		RoiManager rm = RoiManager.getInstance();
		Utils.keepBiggestROI(rm);
		imp2.setRoi(rm.getRoi(0));
		IJ.setForegroundColor(255, 255, 255);
		ImagePlus imp3 = IJ.createImage("Untitled", "8-bit black", imp2.getWidth(), imp2.getHeight(), 1);
		imp3.setRoi(rm.getRoi(0));
		// IJ.run(imp3, "Convex Hull", "");
		IJ.run(imp3, "Fill", "slice");
		imp2.changes = false;
		imp2.close();

		IJ.run(imp3, "Replace value", "pattern=255 replacement=40");
		ImageCalculator ic = new ImageCalculator();
		rm.runCommand("Delete");
		imp = ic.run("Max create", imp, imp3);
		IJ.run(imp, "Invert LUT", "");

		imp3.changes = false;
		imp3.close();
		imp.changes = false;
		return imp;

	}

	public static void step3(String dir) {
		List<String> files = new ArrayList<String>();

		File folder = new File(dir + "/images/");

		Utils.search(".*bmp", folder, files);

		Collections.sort(files);
		JFrame frame = new JFrame("Work in progress");
		JProgressBar progressBar = new JProgressBar();
		int n = 0;
		progressBar.setValue(0);
		progressBar.setString("");
		progressBar.setStringPainted(true);
		progressBar.setMaximum(files.size());
		Border border = BorderFactory.createTitledBorder("Processing Step 3...");
		progressBar.setBorder(border);
		Container content = frame.getContentPane();
		content.add(progressBar, BorderLayout.NORTH);
		frame.setSize(300, 100);
		frame.setVisible(true);

		// For each file in the folder we detect the esferoid on it.
		for (String name : files) {
			ImagePlus imp = IJ.openImage(name);
			ProcessImage.processImageHE2(imp, dir);
			// IJ.saveAs(imp, "BMP", name);
			n++;
			progressBar.setValue(n);

		}
		frame.setVisible(false);
		frame.dispose();
		RoiManager rm = RoiManager.getRoiManager();
		rm.setVisible(true);
		rm.close();
		// IJ.showMessage("Process finished");

	}

	public static void step4(String dir) {

		List<String> files = new ArrayList<String>();

		File folder = new File(dir + "/he2/");

		Utils.search(".*bmp", folder, files);

		Collections.sort(files);

		JFrame frame = new JFrame("Work in progress");
		JProgressBar progressBar = new JProgressBar();
		int n = 0;
		progressBar.setValue(0);
		progressBar.setString("");
		progressBar.setStringPainted(true);
		progressBar.setMaximum(files.size());
		Border border = BorderFactory.createTitledBorder("Processing Step 4...");
		progressBar.setBorder(border);
		Container content = frame.getContentPane();
		content.add(progressBar, BorderLayout.NORTH);
		frame.setSize(300, 100);
		frame.setVisible(true);

		// For each file in the folder we detect the esferoid on it.
		for (String name : files) {
			ImagePlus imp = IJ.openImage(name);
			String title = imp.getTitle();
			title = title.substring(0, title.lastIndexOf("."));
			List<String> result = new ArrayList<String>();
			String folder1 = dir + "/preds/";
			Utils.search2(title + "_", new File(folder1), result);

			int found = result.size() + 1;

			// We clean what was previously detected
			for (String s : result) {
				ImagePlus imp2 = IJ.openImage(s);
				IJ.setAutoThreshold(imp2, "Default dark");
				IJ.setRawThreshold(imp2, 1, 255, null);
				IJ.run(imp2, "Convert to Mask", "");
				IJ.run(imp2, "Analyze Particles...", "add");
				RoiManager rm = RoiManager.getInstance();
				IJ.setBackgroundColor(0, 0, 0);
				imp.setRoi(rm.getRoi(0));
				IJ.run(imp, "Clear", "slice");
				rm.close();
				imp2.changes = false;
				imp2.close();
			}
			imp.deleteRoi();
			IJ.setAutoThreshold(imp, "Default dark");
			IJ.setRawThreshold(imp, 1, 255, null);
			IJ.run(imp, "Convert to Mask", "");

			IJ.run(imp, "Analyze Particles...", "size=10000-150000 circularity=0.50-1.00 add");

			RoiManager rm = RoiManager.getInstance();
			if (rm != null && rm.getRoisAsArray().length > 0) {

				int len = rm.getRoisAsArray().length;
				for (int i = 0; i < len; i++) {

					ImagePlus imp1 = IJ.createImage("Untitled", "8-bit black", imp.getWidth(), imp.getHeight(), 1);
					// IJ.setTool("dropper");
					// IJ.setTool("rectangle");

					rm.select(i);
					imp1.setRoi(rm.getRoi(i));
					IJ.run(imp1, "Enlarge...", "enlarge=5");
					IJ.setForegroundColor(20, 20, 20);
					rm.addRoi(imp1.getRoi());
					rm.select(len);
					rm.runCommand(imp1, "Fill");
					rm.select(len);
					rm.runCommand(imp1, "Delete");

					rm.select(i);
					IJ.setForegroundColor(40, 40, 40);

					imp1.setRoi(rm.getRoi(i));
					rm.runCommand(imp1, "Fill");

					IJ.saveAs(imp1, "BMP", "/home/jonathan/Escritorio/SegPC/submission_and_evaluation_scripts/preds3/"
							+ title + "_" + (i + found) + ".bmp");
					imp1.changes = false;
					imp1.close();
				}
				rm.close();

			}

			imp.changes = false;
			imp.close();

			// IJ.saveAs(imp, "BMP", name);
			n++;
			progressBar.setValue(n);

		}
		frame.setVisible(false);
		frame.dispose();

		// IJ.showMessage("Process finished");

	}
}
