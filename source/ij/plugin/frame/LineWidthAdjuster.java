package ij.plugin.frame;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.*;
import ij.*;
import ij.plugin.*;
import ij.process.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.frame.Recorder;
import ij.util.Tools;

/** Adjusts the width of line selections.  */
public class LineWidthAdjuster extends PlugInFrame implements PlugIn,
	Runnable, AdjustmentListener, TextListener, ItemListener {

	int sliderRange = 200;
	Scrollbar slider;
	int value;
	boolean setText;
	static LineWidthAdjuster instance; 
	Thread thread;
	boolean done;
	TextField tf;
	Checkbox checkbox;

	public LineWidthAdjuster() {
		super("Line Width");
		if (instance!=null) {
			instance.toFront();
			return;
		}		
		WindowManager.addWindow(this);
		instance = this;
		slider = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, sliderRange+1);
		
		Panel panel = new Panel();
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints c  = new GridBagConstraints();
		panel.setLayout(grid);
		c.gridx = 0; c.gridy = 0;
		c.gridwidth = 1;
		c.ipadx = 100;
		c.insets = new Insets(5, 15, 5, 5);
		c.anchor = GridBagConstraints.CENTER;
		grid.setConstraints(slider, c);
		panel.add(slider);
		c.ipadx = 0;  // reset
		c.gridx = 1;
		c.insets = new Insets(5, 5, 5, 15);
		tf = new TextField(""+Line.getWidth(), 3);
		tf.addTextListener(this);
		grid.setConstraints(tf, c);
    	panel.add(tf);
		
		c.gridx = 2;
		c.insets = new Insets(5, 25, 5, 5);
		checkbox = new Checkbox("Spline Fit", isSplineFit());
		checkbox.addItemListener(this);
		panel.add(checkbox);
		
		add(panel, BorderLayout.CENTER);
		slider.addAdjustmentListener(this);
		slider.setUnitIncrement(1);
		
		pack();
		setResizable(false);
		GUI.center(this);
		show();
		thread = new Thread(this, "LineWidthAdjuster");
		thread.start();
		setup();
		addKeyListener(IJ.getInstance());
	}
	
	public synchronized void adjustmentValueChanged(AdjustmentEvent e) {
		value = slider.getValue();
		setText = true;
		notify();
	}

    public  synchronized void textValueChanged(TextEvent e) {
        int width = (int)Tools.parseDouble(tf.getText(), -1);
		//IJ.log(""+width);
        if (width==-1) return;
        if (width<0) width=1;
        if (width!=Line.getWidth()) {
			slider.setValue(width);
        	value = width;
        	notify();
        }
    }

	void setup() {
	}
	

	// Separate thread that does the potentially time-consuming processing 
	public void run() {
		while (!done) {
			synchronized(this) {
				try {wait();}
				catch(InterruptedException e) {}
				if (done) return;
				Line.setWidth(value);
				if (setText) tf.setText(""+value);
				setText = false;
				ImagePlus imp = WindowManager.getCurrentImage();
				if (imp!=null) {
					Roi roi = imp.getRoi();
					if (roi!=null) imp.draw();
				}
			}
		}
	}
	
	boolean isSplineFit() {
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null) return false;
		Roi roi = imp.getRoi();
		if (roi==null) return false;
		if (!(roi instanceof PolygonRoi)) return false;
		return ((PolygonRoi)roi).isSplineFit();
	}

    public void windowClosing(WindowEvent e) {
    	close();
	}

    /** Overrides close() in PlugInFrame. */
    public void close() {
    	super.close();
		instance = null;
		done = true;
		synchronized(this) {notify();}
	}

    public void windowActivated(WindowEvent e) {
    	super.windowActivated(e);
    	checkbox.setState(isSplineFit());
	}

	public void itemStateChanged(ItemEvent e) {
		boolean selected = e.getStateChange()==ItemEvent.SELECTED;
		ImagePlus imp = WindowManager.getCurrentImage();
		if (imp==null)
			{checkbox.setState(false); return;};
		Roi roi = imp.getRoi();
		if (roi==null || !(roi instanceof PolygonRoi))
			{checkbox.setState(false); return;};
		int type = roi.getType();
		if (type==Roi.FREEROI || type==Roi.FREELINE)
			{checkbox.setState(false); return;};;
		PolygonRoi poly = (PolygonRoi)roi;
		boolean splineFit = poly.isSplineFit();
		if (selected && !splineFit)
			{poly.fitSpline(); imp.draw();}
		else if (!selected && splineFit)
			{poly.removeSplineFit(); imp.draw();}
	}
	
	public static void update() {
		if (instance!=null)
			instance.checkbox.setState(instance.isSplineFit());
	}

} 

