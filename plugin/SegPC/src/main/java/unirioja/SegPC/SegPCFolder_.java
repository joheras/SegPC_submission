package unirioja.SegPC;

import java.awt.BorderLayout;
import java.awt.Container;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JProgressBar;
import javax.swing.border.Border;

import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.frame.RoiManager;

@Plugin(type = Command.class, headless = true, menuPath = "Plugins>SegPC")
public class SegPCFolder_ implements Command {

	public void run() {
		try {
			String dir = ProcessFolder.step1();
			ProcessFolder.step2(dir);
			ProcessFolder.step3(dir);
			ProcessFolder.step4(dir);
			IJ.showMessage("Process finished");
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	

}
