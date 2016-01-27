/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.Gui;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import ij.ImagePlus;
import ij.gui.PointRoi;
import ij.gui.Roi;
import utilities.CommonMethods;
import utilities.CommonStatisticsMethods;
import utilities.CommonGuiMethods;
import java.awt.Color;
import java.util.ArrayList;
import java.awt.Point;
import FluoObjects.IPOGTrackNode;
import FluoObjects.IPOGaussianNode;
import utilities.CustomDataTypes.intRange;
import ij.process.ImageProcessor;
import ij.IJ;
import utilities.Gui.HighlightingRoiCollectionContainer;
import utilities.Gui.HighlightingRoiCollectionNode;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
/**
 *
 * @author Taihao
 */
public class AssociatedImageDisplayer implements MouseListener, MouseMotionListener,HighlightingRoiCollectionContainer,ActionListener{
    ArrayList<HighlightingRoiCollectionNode> m_cvRoiCollectionNodes;
    Color focusFrameColor=Color.YELLOW;
    ImagePlus implDisplay;
    ArrayList<ImagePlus> m_cvSrcImages;
    int nImgRows,nImgCols;
    ArrayList <Color> m_cvColors;
    ArrayList <MouseListener> m_cvMouseListeners;
    int nDisplayRadiusx,nDisplayRadiusy;
    int pixels[][];
    double dMag;
    int w,h,wb,hb,nBlocks,W,H,nNumImages;
    Point[] pcBlockCenters;
    int[] pnSliceIndexes;//slice index of the associated images to display
    int nNumBlocks;
    String sTitle;
    intRange[] m_pcPixelDisplayRanges,m_pcPixelRanges;
    ImageProcessor impr;
    int nMaxBrightness,nMinBrightness;
    int[][] pixelsOriginal,pixelsTemp;
    boolean showHighlight;
    public AssociatedImageDisplayer(){
        m_cvRoiCollectionNodes=new ArrayList();
        m_cvMouseListeners=new ArrayList();
    }
    public AssociatedImageDisplayer(String sTitle, ArrayList<ImagePlus> implSrcs, int rows, int cols, int displayRadiusx, int displayRadiusy, double mag){
        this();
        this.m_cvSrcImages=implSrcs;
        nImgRows=rows;
        nImgCols=cols;
        this.nDisplayRadiusx=displayRadiusx;
        this.nDisplayRadiusy=displayRadiusy;
        dMag=mag;
        wb=2*nDisplayRadiusx+1;
        hb=2*nDisplayRadiusy+1;
        w=nImgCols*(wb+1)-1;
        h=nImgRows*(hb+1)-1;
        pixels=new int[h][w];
        pixelsOriginal=new int[h][w];
        nNumBlocks=nImgRows*nImgCols;
        pcBlockCenters=new Point[nNumBlocks];
        m_pcPixelRanges=new intRange[nNumBlocks];
        initBlockCenters();
        this.sTitle=sTitle;
        nMaxBrightness=255;
        nMinBrightness=50;
        implDisplay=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w, h);
        implDisplay.setTitle(sTitle);
        implDisplay.show();
        implDisplay.getWindow().getCanvas().addMouseMotionListener(this);
        implDisplay.getWindow().getCanvas().initPaintListeners();
        implDisplay.getWindow().getCanvas().addPaintListener(this);
        nNumImages=m_cvSrcImages.size();
        W=m_cvSrcImages.get(0).getWidth();
        H=m_cvSrcImages.get(0).getHeight();
        pixelsTemp=new int[H][W];
        createColors();
        showHighlight=true;
    }
    void initBlockCenters(){
        for(int i=0;i<nNumBlocks;i++){
            pcBlockCenters[i]=null;
            m_pcPixelRanges[i]=new intRange();
        }
    }
    public ImagePlus getDisplayImage(){
        return implDisplay;
    }
    public void mousePressed(MouseEvent e){}
    public void mouseClicked(MouseEvent eo){
        if(CommonGuiMethods.getSourceImage(eo)==implDisplay){
            int i,len=m_cvMouseListeners.size();
            for(i=0;i<len;i++){
                m_cvMouseListeners.get(i).mouseClicked(eo);
            }
        }
    }
    int getBlockIndex(ImagePlus impl){
        int len=m_cvSrcImages.size();
        for(int i=0;i<len;i++){
            if(impl==m_cvSrcImages.get(i)) return i;
        }
        return -1;//this is not one of the associated images
    }
    public Roi makeHighlighterOnDisplayImage(int x, int y, int blockIndex, Color c){//only to implDisplay
        Point pt=getDisplayCoordinates(x,y,blockIndex);
        if(pt==null) return null;
        Roi roi=new PointRoi(pt.x,pt.y);
        roi.setImage(implDisplay);
        roi.setColor(c);
        return roi;
    }
    public int highlightCorrespondingPoints(Point pt,Color c){
        HighlightingRoiCollectionNode aCollection=getCollection("CorrespondingPoints");
        if(aCollection==null){
            aCollection=new HighlightingRoiCollectionNode(implDisplay,"CorrespondingPoints");
            addCollection(aCollection);
        }
        aCollection.clear();
        for(int i=0;i<nNumImages;i++){
            aCollection.addRoi(makeHighlighterOnDisplayImage(pt.x,pt.y,i,c), c);
        }
//        refreshImage();
        return 1;
    }
    public HighlightingRoiCollectionNode makeCorrespondingPointsHLCollection(String sID,Point pt, Color c){
        HighlightingRoiCollectionNode aCollection=new HighlightingRoiCollectionNode(implDisplay,sID);
        Roi roi;
        for(int i=0;i<nNumImages;i++){
            roi=makeHighlighterOnDisplayImage(pt.x,pt.y,i,c);
            if(roi==null) continue;
            aCollection.addRoi(roi, c);
        }
        return aCollection;
    }
    public void highlightPoint(ImagePlus impl, int x, int y,Color c){
        Roi roi=new PointRoi(x,y);
        roi.setColor(c);
        roi.setImage(impl);
        roi.draw(impl.getCanvas().getGraphics());
    }
    public ArrayList<ImagePlus> getSrcImages(){
        return m_cvSrcImages;
    }
    public int getBlockHeight(){
        return hb;
    }
    public int getBlockWidth(){
        return wb;
    }
    public void mouseEntered(MouseEvent e){}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(final MouseEvent e){
                if(CommonGuiMethods.getSourceImage(e)==implDisplay){
                    Point pt=CommonGuiMethods.getCursorLocation_ImageCoordinates(implDisplay);
                    IJ.showStatus(getStatusString(pt));
                }
    };
    String getStatusString(Point pt){
        int blockIndex=getBlockIndex(pt.x, pt.y);
        if(blockIndex>=nNumBlocks) return "";
        Point po=getOriginalCoordinates(pt.x, pt.y);
        if(po==null)
            return "";

        ImagePlus impl=m_cvSrcImages.get(blockIndex);
        String status="x="+po.x+", y="+po.y+", z="+(impl.getCurrentSlice()-1);
        int pixel=pixelsOriginal[pt.y][pt.x];
        status+=", value="+pixel;
        status+=" "+m_cvSrcImages.get(blockIndex).getTitle();
        return status;
    }
    void createColors(){
        m_cvColors=new ArrayList();
        m_cvColors.add(Color.red);
        m_cvColors.add(Color.blue);
//        m_cvColors.add(Color.yellow);
        m_cvColors.add(Color.green);
        m_cvColors.add(Color.pink);
        m_cvColors.add(Color.cyan);
        m_cvColors.add(Color.orange);
        m_cvColors.add(Color.magenta);
    }
    public void updateDisplayImage(Point[] pcBlockCenters, int[] pnSliceIndexes, intRange[] cvPixelDisplayRanges){
        m_pcPixelDisplayRanges=cvPixelDisplayRanges;
        this.pcBlockCenters=pcBlockCenters;
        this.pnSliceIndexes=pnSliceIndexes;
        reloadPixels();
        scaleAndSetPixels();
        refreshImage();
    }
    public void refreshImage(){
        CommonMethods.refreshImage(implDisplay);
//        showHighlightCollections();
    }
    public void reloadPixels(){
        CommonStatisticsMethods.setElements(pixels, 0);
        intRange pixelRange;
        int row,col,slice,index,o,i;
        Point center;
        int cx,cy,cx0=-1,cy0=-1,nSlices;
//        int W,H;
        ImagePlus impl;

        intRange indexRange=new intRange();

        for(row=0;row<nImgRows;row++){
            o=row*nImgCols;
            for(col=0;col<nImgCols;col++){
                index=o+col;
                if(index>=nNumImages) continue;
                impl=m_cvSrcImages.get(index);
//                W=impl.getWidth();
//                H=impl.getHeight();
                slice=pnSliceIndexes[index];
                nSlices=impl.getStackSize();
                if(slice<=0||slice>nSlices) continue;
                impl.setSlice(slice);
                center=pcBlockCenters[index];
                if(center!=null){
                    cx=Math.max(nDisplayRadiusx, center.x);
                    cx=Math.min(W-1-nDisplayRadiusx, center.x);
                    cy=Math.max(nDisplayRadiusy, center.y);
                    cy=Math.min(H-1-nDisplayRadiusy, center.y);
                    pcBlockCenters[index].setLocation(cx,cy);
                    loadPixels(index,cx,cy,row,col);
                }
            }
        }
        CommonStatisticsMethods.copyArray(pixels, pixelsOriginal);
        renewFramePixels();
    }
    void loadPixels(int blockIndex, int cx, int cy, int br, int bc){
        int i,j,x,y,pixel,x0,y0,cx0=cx-nDisplayRadiusx,cy0=cy-nDisplayRadiusy;
        if(cx0<0) cx0=0;
        if(cy0<0) cy0=0;
        ImagePlus impl=m_cvSrcImages.get(blockIndex);
        if(impl==null) {
            impl=impl;
        }
        int slice=pnSliceIndexes[blockIndex];
        int pnPixelRange[]=new int[2];
        impr=impl.getProcessor();
        intRange pixelRange=m_pcPixelRanges[blockIndex];
        CommonMethods.getPixelValue(impl, slice, pixelsTemp, cx0, cy0, wb, hb, pnPixelRange);
        x0=(wb+1)*bc;
        y0=(hb+1)*br;
        for(i=0;i<hb;i++){
            for(j=0;j<wb;j++){
                pixels[y0+i][x0+j]=pixelsTemp[i][j];
            }
        }
        pixelRange.setRange(pnPixelRange[0],pnPixelRange[1]);
    } 
    void scaleAndSetPixels(){
        int x,y,row,col,pixel,px,pn,o,index,x0,y0;
        Point center;
        intRange[] pcDisplayRangest=m_pcPixelDisplayRanges;
        if(pcDisplayRangest==null) pcDisplayRangest=m_pcPixelRanges;
        intRange displayRange;
        double k;
        for(row=0;row<nImgRows;row++){
            y0=row*(hb+1);
            o=row*nImgCols;
            for(col=0;col<nImgCols;col++){
                x0=col*(wb+1);
                index=o+col;
                if(index>=nNumImages) continue;
                displayRange=pcDisplayRangest[index];
                if(displayRange==null) continue;
                pn=displayRange.getMin();
                px=displayRange.getMax();
                k=(nMaxBrightness-nMinBrightness)/(double)displayRange.getRange();
                center=pcBlockCenters[index];
                for(y=0;y<hb;y++){
                    for(x=0;x<wb;x++){
                        pixel=pixels[y0+y][x0+x];
                        pixel=nMinBrightness+(int)(k*(pixel-pn)+0.5);
                        if(pixel>nMaxBrightness) pixel=nMaxBrightness;
                        pixels[y+y0][x0+x]=(pixel<<16)|(pixel<<8)|(pixel);
                    }
                }
            }
        }
        CommonMethods.setPixels(implDisplay, pixels, true);
    }

    public Color getColor(int index){
        if(index<m_cvColors.size()) return m_cvColors.get(index);
        return CommonMethods.randomColor();
    }

    public int frameBlock(int blockIndex, Color c){
        renewFramePixels();
        frameBlockPixels(blockIndex,c);
        CommonMethods.setPixels(implDisplay, pixels, true);
        refreshImage();
        return 1;
    }
    int renewFramePixels(){
        int x,y,r,c;
        for(r=1;r<nImgRows;r++){
            y=(hb+1)*r-1;
            for(x=0;x<w;x++){
                pixels[y][x]=0;
            }
        }
        for(c=1;c<nImgCols;c++){
            x=(wb+1)*c-1;
            for(y=0;y<h;y++){
                pixels[y][x]=0;
            }
        }
        return 1;
    }
    int frameBlockPixels(int blockIndex,Color c){
        if(blockIndex<0||blockIndex>=nNumBlocks) return -1;
        int row=blockIndex/nImgCols,col=blockIndex%nImgCols;
        int x0=(wb+1)*col-1,y0=(hb+1)*row-1,x,y,i;
        y=y0;
        for(i=0;i<=wb;i++){
            x=x0+i;
            if(x<0||x>=w) continue;
            if(y<0||y>=h) continue;
            pixels[y][x]=c.getRGB();
        }
        y=y0+hb+1;
        for(i=0;i<=wb+1;i++){
            x=x0+i;
            if(x<0||x>=w) continue;
            if(y<0||y>=h) continue;
            pixels[y][x]=c.getRGB();
        }
        x=x0;
        for(i=0;i<=hb+1;i++){
            y=y0+i;
            if(x<0||x>=w) continue;
            if(y<0||y>=h) continue;
            pixels[y][x]=c.getRGB();
        }
        x=x0+wb+1;
        for(i=0;i<=hb+1;i++){
            y=y0+i;
            if(x<0||x>=w) continue;
            if(y<0||y>=h) continue;
            pixels[y][x]=c.getRGB();
        }
        return 1;
    }
    public Point getOriginalCoordinates(int x, int y){//x and y are the coordinates in implDisplay
        int row=y/(hb+1),col=x/(wb+1);
        int blockIndex=row*nImgCols+col;
        Point center=pcBlockCenters[blockIndex];
        if(center==null) return null;
        x-=(wb+1)*col;
        y-=(hb+1)*row;
        x+=center.x-nDisplayRadiusx;
        y+=center.y-nDisplayRadiusy;
        return new Point(x,y);
    }
    public int getBlockIndex(int x, int y){//x and y are the coordinates in implDisplay
        int row=y/hb,col=x/wb;
        return row*nImgCols+col;
    }

    Point getDisplayCoordinates(int xo, int yo, int blockIndex){
        if(blockIndex<0||blockIndex>=nNumBlocks) return null;
        int row=blockIndex/nImgCols,col=blockIndex%nImgCols;
        int x0=(wb+1)*col,y0=(hb+1)*row;
        Point center=pcBlockCenters[blockIndex];
        int dx=xo-center.x,dy=yo-center.y;
        if(dx<-nDisplayRadiusx||dx>nDisplayRadiusx) return null;
        if(dy<-nDisplayRadiusy||dy>nDisplayRadiusy) return null;
        int x=x0+nDisplayRadiusx+(xo-center.x),y=y0+nDisplayRadiusy+(yo-center.y);
        return new Point(x,y);
    }

    public void addCollection(HighlightingRoiCollectionNode aCollection){
        m_cvRoiCollectionNodes.add(aCollection);
    }
    public HighlightingRoiCollectionNode getCollection(String sID){
        int i,len=m_cvRoiCollectionNodes.size();
        for(i=0;i<len;i++){
            if(m_cvRoiCollectionNodes.get(i).matchingID(sID)) return m_cvRoiCollectionNodes.get(i);
        }
        return null;
    }
    public void removeCollection(String sID){
        int i,len=m_cvRoiCollectionNodes.size();
        for(i=len-1;i>=0;i--){
            if(m_cvRoiCollectionNodes.get(i).matchingID(sID)) m_cvRoiCollectionNodes.remove(i);
        }
    }
    public void showHighlightCollections(){
        int i,len=m_cvRoiCollectionNodes.size();
        if(!showHighlight) len=-1;//not showing
        for(i=0;i<len;i++){
            m_cvRoiCollectionNodes.get(i).highlight();
        }
    }
    public int getDisplayRadiusx() {
        return nDisplayRadiusx;
    }
    public int getDisplayRadiusy(){
        return nDisplayRadiusy;
    }
    public void actionPerformed(ActionEvent ae){
        String st=ae.getActionCommand();
        if(st.contentEquals("Painted"))showHighlightCollections();
    }
    public void setHighlight(boolean highlight){
        showHighlight=highlight;
    }
}
