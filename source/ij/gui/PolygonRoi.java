package ij.gui;
import ij.*;
import ij.process.*;
import ij.measure.*;
import ij.plugin.frame.*;
import java.awt.*;
import java.awt.image.*;
import java.awt.geom.*;

/** This class represents a polygon region of interest or polyline of interest. */
public class PolygonRoi extends Roi {

	protected int maxPoints = 1000; // will be increased if necessary
	protected int[] xp, yp; 	// image coordinates relative to origin of roi bounding box
	protected int[] xp2, yp2;	// absolute screen coordinates
	protected int nPoints;
	protected float[] xSpline,ySpline; // relative image coordinates
	protected int splinePoints = 200;
    Rectangle clip;
	
	private double angle1, degrees=Double.NaN;
	private int xClipMin, yClipMin, xClipMax, yClipMax;
	private boolean userCreated;

	long mouseUpTime = 0;

	/** Creates a new polygon or polyline ROI from x and y coordinate arrays.
		Type must be Roi.POLYGON, Roi.FREEROI, Roi.TRACED_ROI, Roi.POLYLINE, Roi.FREELINE or Roi.ANGLE.*/
	public PolygonRoi(int[] xPoints, int[] yPoints, int nPoints, int type) {
		super(0, 0, null);
		if (type==POLYGON)
			this.type = POLYGON;
		else if (type==FREEROI)
			this.type = FREEROI;
		else if (type==TRACED_ROI)
			this.type = TRACED_ROI;
		else if (type==POLYLINE)
			this.type = POLYLINE;
		else if (type==FREELINE)
			this.type = FREELINE;
		else if (type==ANGLE)
			this.type = ANGLE;
		else if (type==POINT)
			this.type = POINT;
		else
			throw new IllegalArgumentException("Invalid type");
		maxPoints = nPoints;
		this.nPoints = nPoints;
		xp = xPoints;
		yp = yPoints;
		if (type!=TRACED_ROI) {
			xp = new int[nPoints];
			yp = new int[nPoints];
			for (int i=0; i<nPoints; i++) {
				xp[i] = xPoints[i];
				yp[i] = yPoints[i];
			}
		}
		xp2 = new int[nPoints];
		yp2 = new int[nPoints];
		if (type==ANGLE && nPoints==3)
			getAngleAsString();
		finishPolygon();
	}
	
	/** Creates a new polygon or polyline ROI from a Polygon. Type must be Roi.POLYGON, 
		Roi.FREEROI, Roi.TRACED_ROI, Roi.POLYLINE, Roi.FREELINE or Roi.ANGLE.*/
	public PolygonRoi(Polygon p, int type) {
		this(p.xpoints, p.ypoints, p.npoints, type);
	}

	/** Obsolete */
	public PolygonRoi(int[] xPoints, int[] yPoints, int nPoints, ImagePlus imp, int type) {
		this(xPoints, yPoints, nPoints, type);
		setImage(imp);
	}

	/** Starts the process of creating a new user-generated polygon or polyline ROI. */
	public PolygonRoi(int sx, int sy, ImagePlus imp) {
		super(sx, sy, imp);
		int tool = Toolbar.getToolId();
		if (tool==Toolbar.POLYGON)
			type = POLYGON;
		else if (tool==Toolbar.ANGLE)
			type = ANGLE;
		else
			type = POLYLINE;
		xp = new int[maxPoints];
		yp = new int[maxPoints];
		xp2 = new int[maxPoints];
		yp2 = new int[maxPoints];
		nPoints = 2;
		x = ic.offScreenX(sx);
		y = ic.offScreenY(sy);
		width=1;
		height=1;
		clipX = x;
		clipY = y;
		clipWidth = 1;
		clipHeight = 1;
		state = CONSTRUCTING;
		userCreated = true;
	}

	private void drawStartBox(Graphics g) {
		if (type!=ANGLE)
			g.drawRect(ic.screenX(startX)-4, ic.screenY(startY)-4, 8, 8);
	}
	
	public void draw(Graphics g) {
        updatePolygon();
        g.setColor(instanceColor!=null?instanceColor:ROIColor);
        if (xSpline!=null) {
            if (type==POLYLINE || type==FREELINE) {
                if (lineWidth>1)
                	drawWideLine(g, xSpline, ySpline, splinePoints);
                else
                	drawSpline(g, xSpline, ySpline, splinePoints, false);
            } else
                	drawSpline(g, xSpline, ySpline, splinePoints, true);
        } else {
            if (type==POLYLINE || type==FREELINE || type==ANGLE || state==CONSTRUCTING) {
                if (lineWidth>1 && isLine())
                	drawWideLine(g, toFloat(xp), toFloat(yp), nPoints);
                g.drawPolyline(xp2, yp2, nPoints);
            } else
                g.drawPolygon(xp2, yp2, nPoints);
            if (state==CONSTRUCTING && type!=FREEROI && type!=FREELINE)
                drawStartBox(g);
        }
        //g.drawRect(clip.x, clip.y, clip.width, clip.height);
        if ((xSpline!=null||type==POLYGON||type==POLYLINE||type==ANGLE)
        && state!=CONSTRUCTING && clipboard==null) {
            if (ic!=null) mag = ic.getMagnification();
            int size2 = HANDLE_SIZE/2;
            if (activeHandle>0)
                drawHandle(g, xp2[activeHandle-1]-size2, yp2[activeHandle-1]-size2);
            if (activeHandle<nPoints-1)
                drawHandle(g, xp2[activeHandle+1]-size2, yp2[activeHandle+1]-size2);
            handleColor=instanceColor!=null?instanceColor:ROIColor; drawHandle(g, xp2[0]-size2, yp2[0]-size2); handleColor=Color.white;
            for (int i=1; i<nPoints; i++)
                drawHandle(g, xp2[i]-size2, yp2[i]-size2);
        }
		drawPreviousRoi(g);
        if (!(state==MOVING_HANDLE||state==CONSTRUCTING||state==NORMAL))
            showStatus();
        if (updateFullWindow)
            {updateFullWindow = false; imp.draw();}
	}
	
 	private void drawSpline(Graphics g, float[] xpoints, float[] ypoints, int npoints, boolean closed) {
 		if (ic==null) return;
  		Rectangle srcRect = ic.getSrcRect();
 		float srcx=srcRect.x, srcy=srcRect.y;
 		float mag = (float)ic.getMagnification();
 		float xf=x, yf=y;
		Graphics2D g2d = (Graphics2D)g;
		GeneralPath path = new GeneralPath();
		if (mag==1f && srcx==0f && srcy==0f) {
			path.moveTo(xpoints[0]+xf, ypoints[0]+yf);
			for (int i=0; i<npoints; i++)
				path.lineTo(xpoints[i]+xf, ypoints[i]+yf);
		} else {
			path.moveTo((xpoints[0]-srcx+xf)*mag, (ypoints[0]-srcy+yf)*mag);
			for (int i=0; i<npoints; i++)
				path.lineTo((xpoints[i]-srcx+xf)*mag, (ypoints[i]-srcy+yf)*mag);
		}
		if (closed)
			path.lineTo((xpoints[0]-srcx+xf)*mag, (ypoints[0]-srcy+yf)*mag);
		g2d.draw(path);
	}

 	private void drawWideLine(Graphics g, float[] xpoints, float[] ypoints, int npoints) {
 		if (ic==null) return;
 		Rectangle srcRect = ic.getSrcRect();
 		float srcx=srcRect.x, srcy=srcRect.y;
 		float mag = (float)ic.getMagnification();
 		float xf=x, yf=y;
		Graphics2D g2d = (Graphics2D)g;
		GeneralPath path = new GeneralPath();
		if (mag==1f && srcx==0f && srcy==0f) {
			path.moveTo(xpoints[0]+xf, ypoints[0]+yf);
			for (int i=0; i<npoints; i++)
				path.lineTo(xpoints[i]+xf, ypoints[i]+yf);
		} else {
			path.moveTo((xpoints[0]-srcx+xf)*mag, (ypoints[0]-srcy+yf)*mag);
			for (int i=0; i<npoints; i++)
				path.lineTo((xpoints[i]-srcx+xf)*mag, (ypoints[i]-srcy+yf)*mag);
		}
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,0.25f);
		g2d.setComposite(ac);
		g2d.setStroke(new BasicStroke((float)(lineWidth*ic.getMagnification()),BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
		g2d.draw(path);
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
		ac = AlphaComposite.getInstance(AlphaComposite.SRC_OVER,1f);
		g2d.setStroke(new BasicStroke(1));
		g2d.setComposite(ac);
		g2d.draw(path);
	}

	public void drawPixels(ImageProcessor ip) {
		if (xSpline!=null) {
			ip.moveTo(x+(int)(Math.floor(xSpline[0])+0.5), y+(int)Math.floor(ySpline[0]+0.5));
			for (int i=1; i<splinePoints; i++)
				ip.lineTo(x+(int)(Math.floor(xSpline[i])+0.5), y+(int)Math.floor(ySpline[i]+0.5));
			if (type==POLYGON || type==FREEROI || type==TRACED_ROI)
				ip.lineTo(x+(int)(Math.floor(xSpline[0])+0.5), y+(int)Math.floor(ySpline[0]+0.5));
		} else {
			ip.moveTo(x+xp[0], y+yp[0]);
			for (int i=1; i<nPoints; i++)
				ip.lineTo(x+xp[i], y+yp[i]);
			if (type==POLYGON || type==FREEROI || type==TRACED_ROI)
				ip.lineTo(x+xp[0], y+yp[0]);
		}
		if (xSpline!=null || (lineWidth>1&&isLine()))
			updateFullWindow = true;
	}

	protected void grow(int sx, int sy) {
	// Overrides grow() in Roi class
	}


	protected void updatePolygon() {
		if (ic==null) return;
		Rectangle srcRect = ic.getSrcRect();
		if (ic.getMagnification()==1.0 && srcRect.x==0 && srcRect.y==0) {
			for (int i=0; i<nPoints; i++) {
				xp2[i] = xp[i]+x;
				yp2[i] = yp[i]+y;
			}
		} else {
			for (int i=0; i<nPoints; i++) {
				xp2[i] = ic.screenX(xp[i]+x);
				yp2[i] = ic.screenY(yp[i]+y);
			}
		}
	}

	void handleMouseMove(int ox, int oy) {
	// Do rubber banding
		int tool = Toolbar.getToolId();
		if (!(tool==Toolbar.POLYGON || tool==Toolbar.POLYLINE || tool==Toolbar.ANGLE)) {
			imp.killRoi();
			imp.draw();
			return;
		}
        drawRubberBand(ox, oy);
		degrees = Double.NaN;
		double len = -1;
		if (nPoints>1) {
			int x1 = xp[nPoints-2];
			int y1 = yp[nPoints-2];
			int x2 = xp[nPoints-1];
			int y2 = yp[nPoints-1];
			degrees = getAngle(x1, y1, x2, y2);
			if (tool!=Toolbar.ANGLE) {
				Calibration cal = imp.getCalibration();
				double pw=cal.pixelWidth, ph=cal.pixelHeight;
				if (IJ.altKeyDown()) {pw=1.0; ph=1.0;}
				len = Math.sqrt((x2-x1)*pw*(x2-x1)*pw + (y2-y1)*ph*(y2-y1)*ph);
			}
		}
		if (tool==Toolbar.ANGLE) {
			if (nPoints==2)
				angle1 = degrees;
			else if (nPoints==3) {
				double angle2 = getAngle(xp[1], yp[1], xp[2], yp[2]);
				degrees = Math.abs(180-Math.abs(angle1-angle2));
				if (degrees>180.0)
					degrees = 360.0-degrees;
			}
		}
		String length = len!=-1?", length=" + IJ.d2s(len):"";
		String angle = !Double.isNaN(degrees)?", angle=" + IJ.d2s(degrees):"";
		IJ.showStatus(imp.getLocationAsString(ox,oy) + length + angle);
	}

	void drawRubberBand(int ox, int oy) {
		int x1 = xp[nPoints-2]+x;
		int y1 = yp[nPoints-2]+y;
		int x2 = xp[nPoints-1]+x;
		int y2 = yp[nPoints-1]+y;
		int xmin=9999, ymin=9999, xmax=0, ymax=0;
		if (x1<xmin) xmin=x1;
		if (x2<xmin) xmin=x2;
		if (ox<xmin) xmin=ox;
		if (x1>xmax) xmax=x1;
		if (x2>xmax) xmax=x2;
		if (ox>xmax) xmax=ox;
		if (y1<ymin) ymin=y1;
		if (y2<ymin) ymin=y2;
		if (oy<ymin) ymin=oy;
		if (y1>ymax) ymax=y1;
		if (y2>ymax) ymax=y2;
		if (oy>ymax) ymax=oy;
		//clip = new Rectangle(xmin, ymin, xmax-xmin, ymax-ymin);
		int margin = 4;
		if (lineWidth>margin && isLine())
			margin = lineWidth;
		if (ic!=null) {
			double mag = ic.getMagnification();
			if (mag<1.0) margin = (int)(margin/mag);
		}
		xp[nPoints-1] = ox-x;
		yp[nPoints-1] = oy-y;
		imp.draw(xmin-margin, ymin-margin, (xmax-xmin)+margin*2, (ymax-ymin)+margin*2);
	}

    void finishPolygon() {
		Polygon poly = new Polygon(xp, yp, nPoints);
		Rectangle r = poly.getBounds();
		x = r.x;
		y = r.y;
		width = r.width;
		height = r.height;
		if (nPoints<2 || (!(type==FREELINE||type==POLYLINE||type==ANGLE) && (nPoints<3||width==0||height==0))) {
			if (imp!=null) imp.killRoi();
			if (type!=POINT) return;
		}
        for (int i=0; i<nPoints; i++) {
            xp[i] = xp[i]-x;
            yp[i] = yp[i]-y;
        }
		state = NORMAL;
		if (imp!=null && !(type==TRACED_ROI))
			imp.draw(x-5, y-5, width+10, height+10);
		oldX=x; oldY=y; oldWidth=width; oldHeight=height;
		if (Recorder.record && userCreated && (type==POLYGON||type==POLYLINE||type==ANGLE))
			Recorder.recordRoi(getPolygon(), type);
		if (type!=POINT) modifyRoi();
		LineWidthAdjuster.update();
	}
	
    protected void moveHandle(int sx, int sy) {
		if (clipboard!=null) return;
		int ox = ic.offScreenX(sx);
		int oy = ic.offScreenY(sy);
		xp[activeHandle] = ox-x;
		yp[activeHandle] = oy-y;
		if (xSpline!=null) {
			fitSpline(splinePoints);
			updateClipRect();
			if (Line.getWidth()>1 && isLine())
				imp.draw();
			else
				imp.draw(clipX, clipY, clipWidth, clipHeight);
			oldX = x; oldY = y;
			oldWidth = width; oldHeight = height;
		} else {
			resetBoundingRect();
			updateClipRectAndDraw();
		}
		String angle = type==ANGLE?getAngleAsString():"";
		IJ.showStatus(imp.getLocationAsString(ox,oy) + angle);
	}

   /** After handle is moved, find clip rect and repaint. */
   void updateClipRectAndDraw() {
		int xmin=Integer.MAX_VALUE, ymin=Integer.MAX_VALUE, xmax=0, ymax=0;
		int x2, y2;
		if (activeHandle>0)
		   {x2=x+xp[activeHandle-1]; y2=y+yp[activeHandle-1];}
		else
		   {x2=x+xp[nPoints-1]; y2=y+yp[nPoints-1];}
		if (x2<xmin) xmin = x2;
		if (y2<ymin) ymin = y2;
		if (x2>xmax) xmax = x2;
		if (y2>ymax) ymax = y2;
		x2=x+xp[activeHandle]; y2=y+yp[activeHandle];
		if (x2<xmin) xmin = x2;
		if (y2<ymin) ymin = y2;
		if (x2>xmax) xmax = x2;
		if (y2>ymax) ymax = y2;
		if (activeHandle<nPoints-1)
		   {x2=x+xp[activeHandle+1]; y2=y+yp[activeHandle+1];}
		else
		   {x2=x+xp[0]; y2=y+yp[0];}
		if (x2<xmin) xmin = x2;
		if (y2<ymin) ymin = y2;
		if (x2>xmax) xmax = x2;
		if (y2>ymax) ymax = y2;
		int xmin2=xmin, ymin2=ymin, xmax2=xmax, ymax2=ymax;
		if (xClipMin<xmin2) xmin2 = xClipMin;
		if (yClipMin<ymin2) ymin2 = yClipMin;
		if (xClipMax>xmax2) xmax2 = xClipMax;
		if (yClipMax>ymax2) ymax2 = yClipMax;
		xClipMin=xmin; yClipMin=ymin; xClipMax=xmax; yClipMax=ymax;
		double mag = ic.getMagnification();
		int handleSize = type==POINT?HANDLE_SIZE+8:HANDLE_SIZE;
		if (handleSize<lineWidth && isLine()) handleSize= lineWidth;
		int m = mag<1.0?(int)(handleSize/mag):handleSize;
		imp.draw(xmin2-m, ymin2-m, xmax2-xmin2+m*2, ymax2-ymin2+m*2);
	}

	void resetBoundingRect() {
		int xmin=Integer.MAX_VALUE, xmax=-xmin, ymin=xmin, ymax=xmax;
		int xx, yy;
		for(int i=0; i<nPoints; i++) {
			xx = xp[i];
			if (xx<xmin) xmin=xx;
			if (xx>xmax) xmax=xx;
			yy = yp[i];
			if (yy<ymin) ymin=yy;
			if (yy>ymax) ymax=yy;
		}
		if (xmin!=0)
		   for (int i=0; i<nPoints; i++)
			   xp[i] -= xmin;
		if (ymin!=0)
		   for (int i=0; i<nPoints; i++)
			   yp[i] -= ymin;
		//IJ.log("reset: "+ymin+" "+before+" "+yp[0]);
		x+=xmin; y+=ymin;
		width=xmax-xmin; height=ymax-ymin;
	}

	String getAngleAsString() {
		double angle1 = getAngle(xp[0], yp[0], xp[1], yp[1]);
		double angle2 = getAngle(xp[1], yp[1], xp[2], yp[2]);
		degrees = Math.abs(180-Math.abs(angle1-angle2));
		if (degrees>180.0)
			degrees = 360.0-degrees;
		return ", angle=" + IJ.d2s(degrees);
	}
   
   protected void mouseDownInHandle(int handle, int sx, int sy) {
        if (state==CONSTRUCTING)
            return;
		int ox=ic.offScreenX(sx), oy=ic.offScreenY(sy);
		if (IJ.altKeyDown() && !(nPoints<=3 && type!=POINT)) {
			deleteHandle(ox, oy); 
			return;
		} else if (IJ.shiftKeyDown() && type!=POINT) {
			addHandle(ox, oy); 
			return;
		}
		state = MOVING_HANDLE;
		activeHandle = handle;
		int m = (int)(10.0/ic.getMagnification());
		xClipMin=ox-m; yClipMin=oy-m; xClipMax=ox+m; yClipMax=oy+m;
	}

	void deleteHandle(int ox, int oy) {
		if (imp==null) return;
		if (nPoints<=1)
			{imp.killRoi(); return;}
		boolean splineFit = xSpline != null;
		xSpline = null;
		Polygon points = getPolygon();
		modState = NO_MODS;
		if (previousRoi!=null) previousRoi.modState = NO_MODS;
		int pointToDelete = getClosestPoint(ox, oy, points);
		Polygon points2 = new Polygon();
		for (int i=0; i<points.npoints; i++) {
			if (i!=pointToDelete)
				points2.addPoint(points.xpoints[i], points.ypoints[i]);
		}
		if (type==POINT)
			imp.setRoi(new PointRoi(points2.xpoints, points2.ypoints, points2.npoints));
		else {
			imp.setRoi(new PolygonRoi(points2, type));
			if (splineFit) 
				((PolygonRoi)imp.getRoi()).fitSpline(splinePoints);
		}
	}
	
	void addHandle(int ox, int oy) {
		if (imp==null || type==ANGLE) return;
		boolean splineFit = xSpline != null;
		xSpline = null;
		Polygon points = getPolygon();
		int n = points.npoints;
		modState = NO_MODS;
		if (previousRoi!=null) previousRoi.modState = NO_MODS;
		int pointToDuplicate = getClosestPoint(ox, oy, points);
		Polygon points2 = new Polygon();
		for (int i2=0; i2<n; i2++) {
			if (i2==pointToDuplicate) {
				int i1 = i2-1;
				if (i1==-1) i1 = isLine()?i2:n-1;
				int i3 = i2+1;
				if (i3==n) i3 = isLine()?i2:0;
				int x1 = points.xpoints[i1]  + 2*(points.xpoints[i2] - points.xpoints[i1])/3;
				int y1 = points.ypoints[i1] + 2*(points.ypoints[i2] - points.ypoints[i1])/3;
				int x2 = points.xpoints[i2] + (points.xpoints[i3] - points.xpoints[i2])/3;
				int y2 = points.ypoints[i2] + (points.ypoints[i3] - points.ypoints[i2])/3;
				points2.addPoint(x1, y1);
				points2.addPoint(x2, y2);
			} else
				points2.addPoint(points.xpoints[i2], points.ypoints[i2]);
		}
		if (type==POINT)
			imp.setRoi(new PointRoi(points2.xpoints, points2.ypoints, points2.npoints));
		else {
			imp.setRoi(new PolygonRoi(points2, type));
			if (splineFit) 
				((PolygonRoi)imp.getRoi()).fitSpline(splinePoints);
		}
	}

	int getClosestPoint(int x, int y, Polygon points) {
		int index = 0;
		double distance = Double.MAX_VALUE;
		for (int i=0; i<points.npoints; i++) {
			double dx = points.xpoints[i] - x;
			double dy = points.ypoints[i] - y;
			double distance2 = Math.sqrt(dx*dx+dy*dy);
			if (distance2<distance) {
				distance = distance2;
				index = i;
			}
		}
		return index;
	}

	public void fitSpline(int evaluationPoints) {
		if (xSpline==null || splinePoints!=evaluationPoints) {
			splinePoints = evaluationPoints;
			xSpline = new float[splinePoints];
			ySpline = new float[splinePoints];
		}
		int nNodes = nPoints;
		if (type==POLYGON) {
			nNodes++;
			if (nNodes>=xp.length)
				enlargeArrays();
			xp[nNodes-1] = xp[0];
			yp[nNodes-1] = yp[0];
		}
		int[] xindex = new int[nNodes];
		for(int i=0; i<nNodes; i++)
			xindex[i] = i;
		SplineFitter sfx = new SplineFitter(xindex, xp, nNodes);
		SplineFitter sfy = new SplineFitter(xindex, yp, nNodes);
	   
		// Evaluate the splines at all points
		double scale = (double)(nNodes-1)/(splinePoints-1);
		float xs=0f, ys=0f;
		float xmin=Float.MAX_VALUE, xmax=-xmin, ymin=xmin, ymax=xmax;
		for(int i=0; i<splinePoints; i++) {
			double xvalue = i*scale;
			xs = (float)sfx.evalSpline(xindex, xp, nNodes, xvalue);
			if (xs<xmin) xmin=xs;
			if (xs>xmax) xmax=xs;
			xSpline[i] = xs;
			ys = (float)sfy.evalSpline(xindex, yp, nNodes, xvalue);
			if (ys<ymin) ymin=ys;
			if (ys>ymax) ymax=ys;
			ySpline[i] = ys;
		}
		int ixmin = (int)Math.floor(xmin+0.5f);
		int ixmax = (int)Math.floor(xmax+0.5f);
		int iymin = (int)Math.floor(ymin+0.5f);
		int iymax = (int)Math.floor(ymax+0.5f);
		if (ixmin!=0) {
		   for (int i=0; i<nPoints; i++)
			   xp[i] -= ixmin;
		   for (int i=0; i<splinePoints; i++)
			   xSpline[i] -= ixmin;
		}
		if (iymin!=0) {
		   for (int i=0; i<nPoints; i++)
			   yp[i] -= iymin;
		   for (int i=0; i<splinePoints; i++)
			   ySpline[i] -= iymin;
		}
		x+=ixmin; y+=iymin;
		width=ixmax-ixmin; height=iymax-iymin;
		cachedMask = null;
	}

	public void fitSpline() {
		if (imp==null) return;
		double length = getUncalibratedLength();
		int evaluationPoints = (int)(length/2.0);
		if (ic!=null) {
			double mag = ic.getMagnification();
			if (mag<1.0)
				evaluationPoints *= mag;;
		}
		if (evaluationPoints<100)
			evaluationPoints = 100;
		fitSpline(evaluationPoints);
	}
	
	public void removeSplineFit() {
		xSpline = null;
		ySpline = null;
	}
	
	/** Returns 'true' if this selection has been fitted with a spline. */
	public boolean isSplineFit() {
		return xSpline!=null;
	}

	public void fitSplineForStraightening() {
		fitSpline((int)getUncalibratedLength()*2);
		float[] xpoints = new float[splinePoints*2];
		float[] ypoints = new float[splinePoints*2];
		xpoints[0] = xSpline[0];
		ypoints[0] = ySpline[0];
		int n=1, n2;
		double inc = 0.01;
		double distance=0.0, distance2=0.0, dx=0.0, dy=0.0, xinc, yinc;
		double x, y, lastx, lasty, x1, y1, x2=xSpline[0], y2=ySpline[0];
		for (int i=1; i<splinePoints; i++) {
			x1=x2; y1=y2;
			x=x1; y=y1;
			x2=xSpline[i]; y2=ySpline[i];
			dx = x2-x1;
			dy = y2-y1;
			distance = Math.sqrt(dx*dx+dy*dy);
			xinc = dx*inc/distance;
			yinc = dy*inc/distance;
			lastx=xpoints[n-1]; lasty=ypoints[n-1];
			n2 = (int)(dx/xinc);
			do {
				dx = x-lastx;
				dy = y-lasty;
				distance2 = Math.sqrt(dx*dx+dy*dy);
				//IJ.log(i+"   "+IJ.d2s(xinc,5)+"   "+IJ.d2s(yinc,5)+"   "+IJ.d2s(distance,2)+"   "+IJ.d2s(distance2,2)+"   "+IJ.d2s(x,2)+"   "+IJ.d2s(y,2)+"   "+IJ.d2s(lastx,2)+"   "+IJ.d2s(lasty,2)+"   "+n+"   "+n2);
				if (distance2>=1.0-inc/2.0 && n<xpoints.length-1) {
					xpoints[n] = (float)x;
					ypoints[n] = (float)y;
					//IJ.log("--- "+IJ.d2s(x,2)+"   "+IJ.d2s(y,2)+"  "+n);
					n++;
					lastx=x; lasty=y;
				}
				x += xinc;
				y += yinc;
			} while (--n2>0);
		}
		xSpline = xpoints;
		ySpline = ypoints;
		splinePoints = n;
	}

	public void fitSplineForStraightening2() {
		fitSpline((int)getUncalibratedLength()*2);
		float[] xpoints = new float[splinePoints*2];
		float[] ypoints = new float[splinePoints*2];
		xpoints[0] = xSpline[0];
		ypoints[0] = ySpline[0];
		int n = 1, n2;
		double distance=0.0, distance2=0.0, dx=0.0, dy=0.0, fraction, x=0.0, y=0.0, xinc, yinc;
		double next=1.0;  // distance to next point along path
		for (int i=1; i<splinePoints; i++) {
			dx = xSpline[i]-xpoints[n-1];
			dy = ySpline[i]-ypoints[n-1];
			distance = Math.sqrt(dx*dx+dy*dy);
IJ.log("--- "+i+"   "+IJ.d2s(xSpline[i],2)+"   "+IJ.d2s(ySpline[i],2)+"   "+IJ.d2s(dx,2)+"   "+IJ.d2s(dy,2)+"   "+IJ.d2s(distance,2)+"   "+IJ.d2s(next,2)+"   "+IJ.d2s(next/distance,2));
			if (distance<next) {
				next = next - distance;
IJ.log("--- "+i+"distance<next "+distance+"  "+next);
			} else {
				dx = xSpline[i]-xSpline[i-1];
				dy = ySpline[i]-ySpline[i-1];
				distance2 = Math.sqrt(dx*dx+dy*dy);
			    fraction = next/distance2;
				x= xSpline[i-1]+dx*fraction;
				y = ySpline[i-1]+dy*fraction;
				n2 = 1 + (int)(distance2-next);
IJ.log("--- "+i+"   "+fraction+"  "+IJ.d2s(x,2)+"  "+IJ.d2s(y,2)+"  "+n2);
				if (n2==1) {
					xpoints[n] = (float)x;
					ypoints[n] = (float)y;
					n++;
				} else {
					xinc = dx/distance2;
					yinc = dy/distance2;
					do {
						xpoints[n] = (float)x;
						ypoints[n] = (float)y;
						n++;
						x += xinc;
						y += yinc;
						if (n2>1) next+=1.0;
					} while (--n2>0);
				}
				next = 1.0-(distance-next);
dx = xpoints[n-1]-xpoints[n-2];
dy = ypoints[n-1]-ypoints[n-2];
float distance3 = (float)Math.sqrt(dx*dx+dy*dy);
IJ.log((n-1)+"   "+IJ.d2s(xpoints[n-1],2) +"   "+IJ.d2s(ypoints[n-1],2)+"   "+IJ.d2s(distance3,2)+"   "+IJ.d2s(xSpline[i-2],2)+"   "+IJ.d2s(ySpline[i-2],2));
			}
		}
		if (next<0.5) {
			fraction = next/distance;
			xpoints[n] = (float)(xSpline[splinePoints-1]+dx*fraction);
			ypoints[n] = (float)(ySpline[splinePoints-1]+dy*fraction);
			n++;
		}
		xSpline = xpoints;
		ySpline = ypoints;
		splinePoints = n;
	}

	double getUncalibratedLength() {
		if (imp==null) return nPoints/2;
		Calibration cal = imp.getCalibration();
		double spw=cal.pixelWidth, sph=cal.pixelHeight;
		cal.pixelWidth=1.0; cal.pixelHeight=1.0;
		double length = getLength();
		cal.pixelWidth=spw; cal.pixelHeight=sph;
		return length;
	}
	
	protected void handleMouseUp(int sx, int sy) {
		if (state==MOVING)
			{state = NORMAL; return;}				
		if (state==MOVING_HANDLE) {
			cachedMask = null; //mask is no longer valid
			state = NORMAL;
			updateClipRect();
			oldX=x; oldY=y;
			oldWidth=width; oldHeight=height;
			return;
		}		
		if (state!=CONSTRUCTING)
			return;
		if (IJ.spaceBarDown()) // is user scrolling image?
			return;
		boolean samePoint = (xp[nPoints-2]==xp[nPoints-1] && yp[nPoints-2]==yp[nPoints-1]);
		Rectangle biggerStartBox = new Rectangle(ic.screenX(startX)-5, ic.screenY(startY)-5, 10, 10);
		if (nPoints>2 && (biggerStartBox.contains(sx, sy)
		|| (ic.offScreenX(sx)==startX && ic.offScreenY(sy)==startY)
		|| (samePoint && (System.currentTimeMillis()-mouseUpTime)<=500))) {
            nPoints--;
            addOffset();
			finishPolygon();
			return;
		} else if (!samePoint) {
			mouseUpTime = System.currentTimeMillis();
			if (type==ANGLE && nPoints==3) {
                addOffset();
				finishPolygon();
                return;
            }
			//add point to polygon
			xp[nPoints] = xp[nPoints-1];
			yp[nPoints] = yp[nPoints-1];
			nPoints++;
			if (nPoints==xp.length)
				enlargeArrays();
			//if (lineWidth>1) fitSpline();
		}
	}

    protected void addOffset() {
        for (int i=0; i<nPoints; i++) {
            xp[i] = xp[i]+x;
            yp[i] = yp[i]+y;
        }
    }
    
    public boolean contains(int x, int y) {
		if (!super.contains(x, y))
			return false;
		if (xSpline!=null) {
			FloatPolygon poly = new FloatPolygon(xSpline, ySpline, splinePoints);
			return poly.contains(x-this.x, y-this.y);
		} else {
			Polygon poly = new Polygon(xp, yp, nPoints);
			return poly.contains(x-this.x, y-this.y);
		}
	}
	
	/** Returns a handle number if the specified screen coordinates are  
		inside or near a handle, otherwise returns -1. */
	public int isHandle(int sx, int sy) {
		if (!(xSpline!=null||type==POLYGON||type==POLYLINE||type==ANGLE||type==POINT)||clipboard!=null)
		   return -1;
		int size = HANDLE_SIZE+5;
		int halfSize = size/2;
		int handle = -1;
		int sx2, sy2;
		for (int i=0; i<nPoints; i++) {
			sx2 = xp2[i]-halfSize; sy2=yp2[i]-halfSize;
			if (sx>=sx2 && sx<=sx2+size && sy>=sy2 && sy<=sy2+size) {
				handle = i;
				break;
			}
		}
		return handle;
	}

	/** Override Roi.nudge() to support splines. */
	//public void nudge(int key) {
	//	super.nudge(key);
	//	if (xSpline!=null) {
	//		fitSpline();
	//		updateFullWindow = true;
	//		imp.draw();
	//	}
	//}

	public ImageProcessor getMask() {
		if (cachedMask!=null && cachedMask.getPixels()!=null)
			return cachedMask;
		PolygonFiller pf = new PolygonFiller();
		if (xSpline!=null)
			pf.setPolygon(toInt(xSpline), toInt(ySpline), splinePoints);
		else
			pf.setPolygon(xp, yp, nPoints);
		cachedMask = pf.getMask(width, height);
		return cachedMask;
	}

	/** Returns the length of this line selection after
		smoothing using a 3-point running average.*/
	double getSmoothedLineLength() {
		double length = 0.0;
		double w2 = 1.0;
		double h2 = 1.0;
		double dx, dy;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			w2 = cal.pixelWidth*cal.pixelWidth;
			h2 = cal.pixelHeight*cal.pixelHeight;
		}
		double x1,x2,x3,x4,y1,y2,y3,y4;
		x2=xp[0]; x3=xp[0]; x4=xp[1];
		y2=yp[0]; y3=yp[0]; y4=yp[1];
		for (int i=0; i<(nPoints-1); i++) {
			x1=x2; x2=x3; x3=x4;
			y1=y2; y2=y3; y3=y4;;
			if ((i+2)<nPoints) {
				x4=xp[i+2];
				y4=yp[i+2];
			}
			dx = (x4-x1)/3.0; // = (x2+x3+x4)/3-(x1+x2+x3)/3
			dy = (y4-y1)/3.0; // = (y2+y3+y4)/3-(y1+y2+y3)/3
			length += Math.sqrt(dx*dx*w2+dy*dy*h2);
		}
		return length;
	}

	/** Returns the perimeter of this ROIs after
		smoothing using a 3-point running average.*/
	double getSmoothedPerimeter() {
		double length = 0.0;
		double w2 = 1.0;
		double h2 = 1.0;
		double dx, dy;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			w2 = cal.pixelWidth*cal.pixelWidth;
			h2 = cal.pixelHeight*cal.pixelHeight;
		}
		double x1,x2,x3,x4,y1,y2,y3,y4;
		x2=xp[nPoints-1]; x3=xp[0]; x4=xp[1];
		y2=yp[nPoints-1]; y3=yp[0]; y4=yp[1];
		for (int i=0; i<(nPoints-1); i++) {
			x1=x2; x2=x3; x3=x4;
			y1=y2; y2=y3; y3=y4;;
			if ((i+2)<nPoints) {
				x4=xp[i+2];
				y4=yp[i+2];
			} else {
				x4=xp[0];
				y4=yp[0];
			}
			dx = (x4-x1)/3.0; // = (x2+x3+x4)/3-(x1+x2+x3)/3
			dy = (y4-y1)/3.0; // = (y2+y3+y4)/3-(y1+y2+y3)/3
			length += Math.sqrt(dx*dx*w2+dy*dy*h2);
		}
		x1=x2; x2=x3; x3=x4; x4=xp[1];
		y1=y2; y2=y3; y3=y4; y4=yp[1];
		dx = (x4-x1)/3.0;
		dy = (y4-y1)/3.0;
		length += Math.sqrt(dx*dx*w2+dy*dy*h2);
		return length;
	}

	/** Returns the perimeter length of ROIs created using the
		wand tool and the particle analyzer. The algorithm counts
		edge pixels as 1 and corner pixels as sqrt(2). It does this by
		calculating the total length of the ROI boundary and subtracting
		2-sqrt(2) for each non-adjacent corner. For example, a 1x1 pixel
		ROI has a boundary length of 4 and 2 non-adjacent edges so the
		perimeter is 4-2*(2-sqrt(2)). A 2x2 pixel ROI has a boundary length
		of 8 and 4 non-adjacent edges so the perimeter is 8-4*(2-sqrt(2)).
	*/
	double getTracedPerimeter() {
		int sumdx = 0;
		int sumdy = 0;
		int nCorners = 0;
		int dx1 = xp[0] - xp[nPoints-1];
		int dy1 = yp[0] - yp[nPoints-1];
		int side1 = Math.abs(dx1) + Math.abs(dy1); //one of these is 0
		boolean corner = false;
		int nexti, dx2, dy2, side2;
		for (int i=0; i<nPoints; i++) {
			nexti = i+1;
			if (nexti==nPoints)
			  nexti = 0;
			dx2 = xp[nexti] - xp[i];
			dy2 = yp[nexti] - yp[i];
			sumdx += Math.abs(dx1);
			sumdy += Math.abs(dy1);
			side2 = Math.abs(dx2) + Math.abs(dy2);
			if (side1>1 || !corner) {
			  corner = true;
			  nCorners++;
			} else
			  corner = false;
			dx1 = dx2;
			dy1 = dy2;
			side1 = side2;
		}
		double w=1.0,h=1.0;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			w = cal.pixelWidth;
			h = cal.pixelHeight;
		}
		return sumdx*w+sumdy*h-(nCorners*((w+h)-Math.sqrt(w*w+h*h)));
	}

	/** Returns the perimeter (for ROIs) or length (for lines).*/
	public double getLength() {
		if (type==TRACED_ROI)
			return getTracedPerimeter();
			
		if (nPoints>2) {
			if (type==FREEROI)
				return getSmoothedPerimeter();
			else if (type==FREELINE && !(width==0 || height==0))
				return getSmoothedLineLength();
		}
		
		double length = 0.0;
		int dx, dy;
		double w2=1.0, h2=1.0;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			w2 = cal.pixelWidth*cal.pixelWidth;
			h2 = cal.pixelHeight*cal.pixelHeight;
		}
		if (xSpline!=null) {
			double fdx, fdy;
			for (int i=0; i<(splinePoints-1); i++) {
				fdx = xSpline[i+1]-xSpline[i];
				fdy = ySpline[i+1]-ySpline[i];
				length += Math.sqrt(fdx*fdx*w2+fdy*fdy*h2);
			}
			if (type==POLYGON) {
				fdx = xSpline[0]-xSpline[splinePoints-1];
				fdy = ySpline[0]-ySpline[splinePoints-1];
				length += Math.sqrt(fdx*fdx*w2+fdy*fdy*h2);
			}
		} else {
			for (int i=0; i<(nPoints-1); i++) {
				dx = xp[i+1]-xp[i];
				dy = yp[i+1]-yp[i];
				length += Math.sqrt(dx*dx*w2+dy*dy*h2);
			}
			if (type==POLYGON) {
				dx = xp[0]-xp[nPoints-1];
				dy = yp[0]-yp[nPoints-1];
				length += Math.sqrt(dx*dx*w2+dy*dy*h2);
			}
		}
		return length;
	}
	
	/** Returns Feret's diameter, the greatest distance between 
		any two points along the ROI boundary. */
	public double getFeretsDiameter() {
		double w2=1.0, h2=1.0;
		if (imp!=null) {
			Calibration cal = imp.getCalibration();
			w2 = cal.pixelWidth*cal.pixelWidth;
			h2 = cal.pixelHeight*cal.pixelHeight;
		}
		double diameter = 0.0, dx, dy, d;
		for (int i=0; i<nPoints; i++) {
			for (int j=i; j<nPoints; j++) {
				dx = xp[i] - xp[j];
				dy = yp[i] - yp[j];
				d = Math.sqrt(dx*dx*w2 + dy*dy*h2);
				if (d>diameter)
					diameter = d;
			}
		}
		return diameter;
	}

	/** Returns the angle in degrees between the first two segments of this polyline.*/
	public double getAngle() {
		return degrees;
	}
	
	/** Returns the number of XY coordinates. */
	public int getNCoordinates() {
		if (xSpline!=null)
			return splinePoints;
		else
			return nPoints;
	}
	
	/** Returns this ROI's X-coordinates, which are relative
		to origin of the bounding box. */
	public int[] getXCoordinates() {
		if (xSpline!=null)
			return toInt(xSpline);
		else
			return xp;
	}

	/** Returns this ROI's Y-coordinates, which are relative
		to origin of the bounding box. */
	public int[] getYCoordinates() {
		if (xSpline!=null)
			return toInt(ySpline);
		else
			return yp;
	}

	/** Returns this PolygonRoi as a Polygon. 
		@see ij.process.ImageProcessor#setRoi
		@see ij.process.ImageProcessor#drawPolygon
		@see ij.process.ImageProcessor#fillPolygon
	*/
	public Polygon getPolygon() {
		int n;
		int[] xpoints1, ypoints1;
		if (xSpline!=null) {
			n = splinePoints;
			xpoints1 = toInt(xSpline);
			ypoints1 = toInt(ySpline);
		} else {
			n = nPoints;
			xpoints1 = xp;
			ypoints1 = yp;
		}
		int[] xpoints2 = new int[n];
		int[] ypoints2 = new int[n];
		for (int i=0; i<n; i++) {
			xpoints2[i] = xpoints1[i] + x;
			ypoints2[i] = ypoints1[i] + y;
		}
		return new Polygon(xpoints2, ypoints2, n);
	}
	
	/** Returns the spline fitted polygon or polyline as float arrays. */
	public FloatPolygon getFloatPolygon() {
		if (xSpline==null) return null;
		int n = splinePoints;
		float[] xpoints2 = new float[n];
		float[] ypoints2 = new float[n];
		for (int i=0; i<n; i++) {
			xpoints2[i] = xSpline[i] + x;
			ypoints2[i] = ySpline[i] + y;
		}
		return new FloatPolygon(xpoints2, ypoints2, n);
	}

	/** Returns a copy of this PolygonRoi. */
	public synchronized Object clone() {
		PolygonRoi r = (PolygonRoi)super.clone();
		r.xp = new int[maxPoints];
		r.yp = new int[maxPoints];
		r.xp2 = new int[maxPoints];
		r.yp2 = new int[maxPoints];
		for (int i=0; i<nPoints; i++) {
			r.xp[i] = xp[i];
			r.yp[i] = yp[i];
			r.xp2[i] = xp2[i];
			r.yp2[i] = yp2[i];
		}
		if (xSpline!=null) {
			r.xSpline = null;
			r.fitSpline(splinePoints);
		}
		return r;
	}

	void enlargeArrays() {
		int[] xptemp = new int[maxPoints*2];
		int[] yptemp = new int[maxPoints*2];
		int[] xp2temp = new int[maxPoints*2];
		int[] yp2temp = new int[maxPoints*2];
		System.arraycopy(xp, 0, xptemp, 0, maxPoints);
		System.arraycopy(yp, 0, yptemp, 0, maxPoints);
		System.arraycopy(xp2, 0, xp2temp, 0, maxPoints);
		System.arraycopy(yp2, 0, yp2temp, 0, maxPoints);
		xp=xptemp; yp=yptemp;
		xp2=xp2temp; yp2=yp2temp;
		if (IJ.debugMode) IJ.log("PolygonRoi: "+maxPoints+" points");
		maxPoints *= 2;
	}
	
	private int[] toInt(float[] arr) {
		int n = arr.length;
		int[] temp = new int[n];
		for (int i=0; i<n; i++)
			temp[i] = (int)Math.floor(arr[i]+0.5);
		return temp;
	}

	private float[] toFloat(int[] arr) {
		int n = arr.length;
		float[] temp = new float[n];
		for (int i=0; i<n; i++)
			temp[i] = arr[i];
		return temp;
	}

}
