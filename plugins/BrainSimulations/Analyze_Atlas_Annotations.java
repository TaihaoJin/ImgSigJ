/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;

import ij.CompositeImage;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.LookUpTable;
import ij.io.FileInfo;
import ij.io.ImageReader;
import ij.io.OpenDialog;
import ij.io.RandomAccessStream;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import BrainSimulations.DataClasses.*;

import java.awt.Color;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.io.*;
import java.util.ArrayList;
import BrainSimulations.SubstructureEssential;
import BrainSimulations.StructureFrame;
import ij.gui.*;

public class Analyze_Atlas_Annotations implements PlugIn {
    public void run(String Arg)
    {
        Arg=Arg;
    }
}

