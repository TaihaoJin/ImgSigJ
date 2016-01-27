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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
/**
 *
 * @author Taihao
 */
public class ImageSeriesDisplayer implements MouseListener, MouseMotionListener,HighlightingRoiCollectionContainer,ActionListener{
    ArrayList<HighlightingRoiCollectionNode> m_cvRoiCollectionNodes;
    Color focusFrameColor=Color.YELLOW;
    ImagePlus implSrc,implDisplay;
    int nImgRows,nImgCols,nFrameShift;
    ArrayList <Color> m_cvColors;
    ArrayList <Point> cvDisplayCenters;
    ArrayList <MouseListener> m_cvMouseListeners;
    int nDisplayRadiusx,nDisplayRadiusy;
    int pixels[][];
    double dMag;
    int w,h,wb,hb,nBlocks,increment;
    Point[] pcBlockCenters;
    int nNumBlocks,nNumSeriesPoints;
    String sTitle;
    int sliceI,sliceF,focusSlice;//this slice indexes and pcBlockCenters should be consistent.
    //They need to be assigned only one time per implDisplay update, so should be update through a single
    //method.
    intRange cPixelRange;
    ImageProcessor impr;
    int nMaxBrightness,nMinBrightness;
    ArrayList<Point[]> m_pcvHighlightingSeriesPoints;
    IPOGTrackNode m_cMainIPOGT;
    int[][] pixelsOriginal;
    boolean showHighlight,adjustDisplayingCenter;
    public ImageSeriesDisplayer(){
        m_cvRoiCollectionNodes=new ArrayList();
    }
    public IPOGTrackNode getMainTrack(){
        return m_cMainIPOGT;
    }
    public ImageSeriesDisplayer(String sTitle, ImagePlus implSrc, int rows, int cols,int frameShift, int increment, int displayRadiusx, int displayRadiusy, double mag){
        this();
        this.implSrc=implSrc;
        cPixelRange=new intRange();
        nImgRows=rows;
        nImgCols=cols;
        nFrameShift=frameShift;
        this.increment=increment;
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
        initBlockCenters();
        this.sTitle=sTitle;
        nMaxBrightness=255;
        nMinBrightness=50;
        implDisplay=CommonMethods.getBlankImage(ImagePlus.COLOR_RGB, w, h);
        implDisplay.setTitle(sTitle);
        implDisplay.show();
        implDisplay.getWindow().getCanvas().addMouseMotionListener(this);
        implDisplay.getWindow().getCanvas().addPaintListener(this);
        m_pcvHighlightingSeriesPoints=new ArrayList();
        m_pcvHighlightingSeriesPoints.add(new Point[nNumBlocks]);
        nNumSeriesPoints=1;
        adjustDisplayingCenter=true;;
        createColors();
    }
    public void setSeriesPointsCapacity(int size){
        int size0=m_pcvHighlightingSeriesPoints.size();
        for(int i=size0;i<size;i++){
            m_pcvHighlightingSeriesPoints.add(new Point[nNumBlocks]);
        }
    }
    void initBlockCenters(){
        for(int i=0;i<nNumBlocks;i++){
            pcBlockCenters[i]=null;
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
    int getBlockIndex(int sliceIndex){
        if(sliceIndex<1||sliceIndex>implSrc.getNSlices()) return -1;
        int index=sliceIndex-sliceI;
        if(index<0||index>=nNumBlocks) return -1;
        return index;
    }
    int getSliceIndex(int blockIndex){
        if(blockIndex<0||blockIndex>=nNumBlocks) return -1;
        int sliceIndex=sliceI+blockIndex;
        if(sliceIndex<1||sliceIndex>implSrc.getNSlices()) return -1;
        return sliceIndex;
    }
    public Roi makeHighlighterOnSeriesImage(int x, int y, int slice, Color c){//only to implDisplay
        Point pt=getDisplayCoordinates(x,y,slice);
        if(pt==null) return null;
        Roi roi=new PointRoi(pt.x,pt.y);
        roi.setImage(implDisplay);
        roi.setColor(c);
        return roi;
    }
    public void highlightPoint(ImagePlus impl, int x, int y,Color c){
        Roi roi=new PointRoi(x,y);
        roi.setColor(c);
        roi.setImage(impl);
        roi.draw(impl.getCanvas().getGraphics());
    }
    public void mouseEntered(MouseEvent e){}

    /**
     * Invoked when the mouse exits a component.
     */
    public void mouseExited(MouseEvent e){}
    public void mouseReleased(MouseEvent e){}
    public void mouseDragged(MouseEvent e){}
    public void mouseMoved(MouseEvent e){
        if(CommonGuiMethods.getSourceImage(e)==implDisplay){
            Point pt=CommonGuiMethods.getCursorLocation_ImageCoordinates(implDisplay);
            IJ.showStatus(getStatusString(pt));
        }
    };
    String getStatusString(Point pt){
        int slice=getSliceIndex(pt.x, pt.y);
        Point po=getOriginalCoordinates(pt.x, pt.y);
        if(po==null)
            return "";
        String status="x="+po.x+" y="+po.y+" z="+(slice-1);
        int pixel=pixelsOriginal[pt.y][pt.x];
        status+=" ("+pixel+")";
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
    public void setSlice(int slice){
        if(implSrc!=null) implSrc.setSlice(slice);
    }
    public int refreshDisplayImage(){
        if(reloadPixels()==-1) return -1;
        scaleAndSetPixels();
        refreshImage();
        return 1;
    }
    public void refreshImage(){
        CommonMethods.refreshImage(implDisplay);
//        showHighlightCollections();
    }
    public int reloadPixels(){
        CommonStatisticsMethods.setElements(pixels, 0);
        cPixelRange.resetRange();
        int row,col,slice,index,o,i;
        Point center;
        int cx,cy,cx0=-1,cy0=-1;
        int W=implSrc.getWidth(),H=implSrc.getHeight(),nSlices=implSrc.getNSlices();

        intRange indexRange=new intRange();
        for(i=0;i<nNumBlocks;i++){
            if(pcBlockCenters[i]==null)continue;
            slice=getSliceIndex(i);
            if(slice<=0||slice>nSlices) continue;
            indexRange.expandRange(i);
        }
        if(indexRange.emptyRange()) return -1;
        int indexn=indexRange.getMin();
        center=pcBlockCenters[indexn];
        cx=center.x;
        cy=center.y;
        for(row=0;row<nImgRows;row++){
            o=row*nImgCols;
            for(col=0;col<nImgCols;col++){
                index=o+col;
                slice=getSliceIndex(index);
//                if(!indexRange.contains(index)) continue;
                if(slice<0) continue;

                if(adjustDisplayingCenter){
                    if(index<indexn) {
                        center=pcBlockCenters[indexn];
                        pcBlockCenters[index]=center;
                    }else
                        center=pcBlockCenters[index];
                    if(center!=null){
                        cx=Math.max(nDisplayRadiusx, center.x);
                        cx=Math.min(W-1-nDisplayRadiusx, cx);
                        cy=Math.max(nDisplayRadiusy, center.y);
                        cy=Math.min(H-1-nDisplayRadiusy, cy);
                        pcBlockCenters[index].setLocation(cx,cy);
                        cx0=cx;
                        cy0=cy;
                    }else{
                        cx=cx0;
                        cy=cy0;
                        pcBlockCenters[index]=new Point(cx,cy);
                    }
                }
                loadPixels(cx,cy,slice,row,col);
            }
        }
        implSrc.setSlice(focusSlice);
        CommonStatisticsMethods.copyArray(pixels, pixelsOriginal);
        renewFramePixels();
        return 1;
    }
    public void adjustDisplayingCenter(boolean adjust){
        adjustDisplayingCenter=adjust;
        refreshDisplayImage();
    }
    int loadPixels(int cx, int cy, int slice, int br, int bc){
        if(slice>implSrc.getNSlices()) return -1;
        int i,j,x,y,pixel,x0,y0,cx0=cx-nDisplayRadiusx,cy0=cy-nDisplayRadiusy;
        implSrc.setSlice(slice);
        impr=implSrc.getProcessor();
        for(i=0;i<hb;i++){
            y=i+cy0;
            y0=br*(hb+1)+i;
            for(j=0;j<wb;j++){
                x=j+cx0;
                x0=bc*(wb+1)+j;
                pixel=(int)impr.getPixel(x, y);
                cPixelRange.expandRange(pixel);
                pixels[y0][x0]=pixel;
            }
        }
        return 1;
    }
    void scaleAndSetPixels(){
        int i,j,pixel,px=cPixelRange.getMax(),pn=cPixelRange.getMin();
        double k=(nMaxBrightness-nMinBrightness)/(double)cPixelRange.getRange();
        for(i=0;i<h;i++){
            if((i+1)%(hb+1)==0)continue;//frame lines
            for(j=0;j<w;j++){
                if((j+1)%(wb+1)==0)continue;
                pixel=pixels[i][j];
                pixel=nMinBrightness+(int)((pixel-pn)*k);
                if(pixel>nMaxBrightness) pixel=nMaxBrightness;
                pixel=(pixel<<16)|(pixel<<8)|pixel;
                pixels[i][j]=pixel;
            }
        }
        CommonMethods.setPixels(implDisplay, pixels, true);
    }
    void initSeriesPoints(Point[] points){
        for(int i=0;i<nNumBlocks;i++){
            points[i]=null;
        }
    }
    public int displayIPOGTrack(IPOGTrackNode IPOGT){
        String sTitle=implDisplay.getTitle();
        int index=sTitle.lastIndexOf("Track");
        if(index>0){
            sTitle=sTitle.substring(0,index)+"Track"+IPOGT.TrackIndex;
        }else
            sTitle+=" Track"+IPOGT.TrackIndex;
        implDisplay.setTitle(sTitle);
        
        HighlightingRoiCollectionNode aCollection=getCollection("MainCollection");
        if(aCollection==null){
            aCollection=new HighlightingRoiCollectionNode(implDisplay,"MainCollection");
            addCollection(aCollection);
        }else{
            aCollection.clear();
        }
        initBlockCenters();
        IPOGaussianNode IPOG;
        focusSlice=implSrc.getCurrentSlice();
        sliceI=Math.max(0, focusSlice+nFrameShift);
        sliceF=Math.min(sliceI+nNumBlocks-1,implSrc.getNSlices());
        int slice;
        Point pt=null;
        Color c=getColor(0);
        int indexn=-1;
        for(index=0;index<nNumBlocks;index++){
            slice=getSliceIndex(index);
            if(slice<0) continue;
            IPOG=IPOGT.getIPOG(slice);
            if(IPOG==null) continue;
            if(indexn<0){
                indexn=index;
                pt=IPOG.getCenter();
            }
            if(adjustDisplayingCenter) pt=IPOG.getCenter();
            pcBlockCenters[index]=pt;
        }
        refreshDisplayImage();//the block centers could have been relocated
        for(index=0;index<nNumBlocks;index++){
            slice=getSliceIndex(index);
            if(slice<0) continue;
            IPOG=IPOGT.getIPOG(slice);
            if(IPOG==null) continue;
            pt=IPOG.getCenter();
            aCollection.addRoi(makeHighlighterOnSeriesImage(pt.x,pt.y,slice,c),c);
        }
//        showHighlightCollections();
        m_cMainIPOGT=IPOGT;
        return 1;
    }
    public Color getColor(int index){
        if(index<m_cvColors.size()) return m_cvColors.get(index);
        return CommonMethods.randomColor();
    }
    public int getDisplayScope(intRange sliceRange, ArrayList<intRange> xRanges, ArrayList<intRange> yRanges){
        int i,j;
        Point pt;
        xRanges.clear();
        yRanges.clear();
        sliceRange.resetRange();
        int slice;
        int cx,cy,cx0=-1,cy0=-1,slice0=getSliceIndex(0),maxRanges=nNumBlocks;
        for(i=0;i<nNumBlocks;i++){
            pt=pcBlockCenters[i];
            if(pt!=null) {
                cx=pt.x;
                cy=pt.y;
            }else{
                cx=cx0;
                cy=cy0;
            }
            if(cx==-1) {
                maxRanges--;
                slice0=getSliceIndex(i);
                continue;
            }
            slice=getSliceIndex(i);
            if(slice<0) {
                maxRanges--;
                continue;
            }
            if(slice0>0){
                for(j=slice0+1;j<=slice;j++){
                    xRanges.add(new intRange(cx-nDisplayRadiusx,cx+nDisplayRadiusx));
                    yRanges.add(new intRange(cy-nDisplayRadiusy,cy+nDisplayRadiusy));
                    sliceRange.expandRange(j);
                    i+=slice-slice0-1;
                    if(xRanges.size()==maxRanges) break;
                }
            }else{
                xRanges.add(new intRange(cx-nDisplayRadiusx,cx+nDisplayRadiusx));
                yRanges.add(new intRange(cy-nDisplayRadiusy,cy+nDisplayRadiusy));
                sliceRange.expandRange(slice);
            }
            if(xRanges.size()==maxRanges) break;
            slice0=slice;
        }
        return 1;
    }
    public int frameSlicePixels(int sliceIndex,Color c){
        int blockIndex=getBlockIndex(sliceIndex);
        if(blockIndex<0) return -1;
        frameBlockPixels(blockIndex,c);
        return 1;
    }    
    public int frameSlice(int slice, Color c){
        renewFramePixels();
        frameSlicePixels(slice,c);
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
        frameBlockPixels(getBlockIndex(focusSlice),focusFrameColor);
        return 1;
    }
    int frameBlockPixels(int blockIndex,Color c){
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
    public int getSliceIndex(int x, int y){//x and y are the coordinates in implDisplay
        int row=y/(hb+1),col=x/(wb+1);
        int blockIndex=row*nImgCols+col;
        return getSliceIndex(blockIndex);
    }
    public Point getDisplayCoordinates(int xo, int yo, int slice){
        int blockIndex=getBlockIndex(slice);
        if(blockIndex<0) return null;
        int row=blockIndex/nImgCols,col=blockIndex%nImgCols;
        int x0=(wb+1)*col,y0=(hb+1)*row;
        Point center=pcBlockCenters[blockIndex];
        if(center==null) return null;
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
    public intRange getSliceRange(){
        return new intRange(sliceI,sliceF);
    }
    public void actionPerformed(ActionEvent ae){
        if(ae.getActionCommand().contentEquals("Painted")){
            ImagePlus impl=CommonGuiMethods.getSourceImage(ae);
            if(impl==implDisplay) showHighlightCollections();
        }
    }
    public void setHighlight(boolean highlight){
        showHighlight=highlight;
    }
}
