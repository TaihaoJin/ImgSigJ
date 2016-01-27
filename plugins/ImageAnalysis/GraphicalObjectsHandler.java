/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ImageAnalysis;
import ImageAnalysis.GraphicalObject;
import java.util.ArrayList;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import java.awt.Image;
import java.awt.Image.*;
import ImageAnalysis.GraphicalObjectProperty;
import ij.IJ;
import utilities.CustomDataTypes.intRange;
import utilities.CommonMethods;
import utilities.ArrayofArrays.IntArray;
import utilities.ArrayofArrays.IntRangeArray;
import ij.IJ;

/**
 *
 * @author Taihao
 */
public class GraphicalObjectsHandler {//At the time being, this handler handles 
    //rgb images only.
    //From now on, my image define as [height][width]; just be consistent with iteration index; scanning line by line
    //This is to be consistent with matrix convention, i.e., first index indicates the vertical position. This is 
    //oposite to x and y convention. And also is oposite to imagej convention.
    //This class has been restructured after ImageJNB65V5 (May of 2009)
    //The original class has been moved to the package BrainSimulations.
//    ArrayList <GraphicalObject> graphicalObjects;
    ArrayList <GraphicalObjectProperty> GOPList;
    ArrayList <GraphicalObject> GOList;
    int pixels[][];
    IntArray GOIndexGrids[][];
    public static final int GRAY=0;
    public static final int RGB=1;
    int height,width;
    boolean bIncludeBackground;
    int GOIGridSize=20;
        //diagonaly neibhoring pixels will be regarded as the same objects. 
    ArrayList <connectionNode> connectionNodes;
    boolean randomGOColor;
    protected int pixelType;
    GraphicalObjectProperty background;
    
    public GraphicalObjectsHandler(){
        height=0;
        width=0;
        int i,j,k;
        GOPList=new ArrayList <GraphicalObjectProperty> ();
        GOList=new ArrayList<GraphicalObject>();
        bIncludeBackground=true;
        connectionNodes =new ArrayList <connectionNode> ();
        pixelType=GRAY;
        setBackground();
    }

    
    class connectionNode {
        public int[] or1,or2;
    }

    void setBackground() {
        background=new GraphicalObjectProperty();
        background.setAsBackground();
    }

    public void setGOPList(ArrayList <GraphicalObjectProperty> GOPList0){
        this.GOPList.clear();
        this.GOPList=GOPList0;
    }
    public ArrayList<GraphicalObject> getGOList(){
        return GOList;
    }
    public void drawGraphicalObjects(){
        int size=GOList.size();
        int pixels0[]=new int[width*height];
        for(int i=0;i<size;i++){
            GOList.get(i).draw(pixels0, height, width);
        }
        ImagePlus impl=CommonMethods.getRGBImage("Recognized Graphical Objects", width, height, pixels0);
        impl.show();
    }
    
    void markOrigions(int[][][] origins, int GOPType[][]){        
        int i,j,type=0;
        intRange cSeg;
        connectionNodes.clear();
        for(i=0;i<height;i++){
            CommonMethods.showStatusAndProgress("Marking Origins Line " +i+ " of "+ height, (double)i/(double)height);
            cSeg=new intRange(0,0);
            type=GOPType[i][0];
            for(j=1;j<width;j++){
                type=GOPType[i][j];
                if(type!=GOPType[i][j-1]){
                    cSeg.expandRange(j-1);
                    markSegment(origins,GOPType,i,GOPType[i][j-1],cSeg);
                    cSeg=new intRange(j,j);
                }
            }
            cSeg.expandRange(width-1);
            markSegment(origins,GOPType,i,GOPType[i][width-1],cSeg);
        }
        int updatedNodes=0;
        int size=connectionNodes.size();
        connectionNode aNode;
        do{
            updatedNodes=0;
            for(i=0;i<size;i++){
                aNode=connectionNodes.get(i);
                if(aNode.or1[0]!=aNode.or2[0]||aNode.or1[1]!=aNode.or2[1]){
                    setMinimalOrigin(aNode.or1,aNode.or2);
                    updatedNodes++;
                }
            }
        }while(updatedNodes>0);
    }
    
    void setOriginValues(int[] or, int y, int x){        
        or[0]=y;
        or[1]=x;
    }
    boolean eqOrigins(int o1[], int o2[]){
        if(o1==null||o2==null) return false;
        if(o1.length!=2||o2.length!=2) return false;
        return (o1[0]==o2[0]&&o1[1]==o2[1]);
    }
    void setMinimalOrigin(int o1[], int o2[]){
        //minimal origin is defined as the one with smaller y. being defined such 
        //can make sure the minimal origin is the point that will be scanned first
        //when scanning the image line by line, from top to bottom.
        int[] o=o1;
        if(o1[0]<o2[0]) o=o1;
        if(o2[0]<o1[0]) o=o2;
        if(o1[0]==o2[0]){            
            if(o1[1]<o2[1]) o=o1;
            if(o2[1]<o1[1]) o=o2;
        }
        for(int i=0;i<2;i++){
            o1[i]=o[i];
            o2[i]=o[i];
        }
    }
    int endOfSegment(int [][] GOPType, int y, int x0){
        int type=GOPType[y][x0];
        int xMax=GOPType[y].length;
        for(int x=x0;x<xMax;x++){
            if(GOPType[y][x]!=type) {
                return x-1;
            }
        }
        return xMax-1;
    }
    void connectDiagonally(String name, boolean b){
        int size= GOPList.size();
        GraphicalObjectProperty gop;
        for(int i=0;i<size;i++){
            gop=GOPList.get(i);
            if(gop.name.equalsIgnoreCase(name))gop.connectDiagonally(b);
        }
    }
    void markSegment(int[][][] origins, int GOPType[][], int y,int type, intRange ir){
        int[] or=null;
        int x=ir.getMin();
        if(y==0){
            or=new int[2];
            setOriginValues(or,y,x);
            origins[y][x]=or;
        }else{
            int min=ir.getMin();
            if(GOPList.get(type).bConnectDiagonally)min--;
            if(min<0)min=0;
            int max=ir.getMax()+1;
            if(GOPList.get(type).bConnectDiagonally)max++;
            if(max>=width) max=width-1;
            for(x=min;x<=max;x++){
                if(type==GOPType[y-1][x]){
                    if(or==null){
                        or=origins[y-1][x];
                    }else{                        
                        setMinimalOrigin(origins[y-1][x],or);
                        connectionNode aNode=new connectionNode();
                        aNode.or1=origins[y-1][x];
                        aNode.or2=or;
                        connectionNodes.add(aNode);
                    }
                }
                x=endOfSegment(GOPType,y-1,x);
            }
            if(or==null){//first line of the graphical object.
                x=ir.getMin();
                or=new int[2];
                setOriginValues(or,y,x);
            }
        }
        int min=ir.getMin();
        int max=ir.getMax();
        for(x=min;x<=max;x++){
            origins[y][x]=or;
        }
    }
    
    public void buildGOList(){
        GOList.clear();
        int[][] GOPType=new int[height][width];
        GraphicalObject[][] GOs=new GraphicalObject[height][width];
        int[][][] origins =new int[height][width][2];
        long time=CommonMethods.currentTime();
        markGOPType(GOPType);
        time-=CommonMethods.currentTime();
        markOrigions(origins, GOPType);//The elements of GOPType also assigned.
        int i,j,type,type0;
        intRange cSeg;
        for(i=0;i<height;i++){
            CommonMethods.showStatusAndProgress("Building GO List: Line "+i+" of " +height, (double)i/(double)height);
            cSeg=new intRange(0,0);
            type0=GOPType[i][0];
            for(j=1;j<width;j++){
                type=GOPType[i][j];
                if(type!=type0){
                    cSeg.expandRange(j-1);
                    closeSegment(origins,GOs,i,type0,cSeg);
                    cSeg=new intRange(j,j);
                    type0=type;
                }
            }
            type=GOPType[i][width-1];
            cSeg.expandRange(width-1);
            closeSegment(origins,GOs,i,type,cSeg);
        }
        int size=GOList.size();
        for(i=0;i<size;i++){
            GOList.get(i).buildXStripes();
        }
    }
    void markGOPType(int GOPType[][]){        
        int i,j;
        int type;
        for(i=0;i<height;i++){
            for(j=0;j<width;j++){
                if(pixelType==GRAY){
                    type=getGOPTypeGRAY(pixels[i][j]);
                    GOPType[i][j]=type;
                    if(type>0){
                        type=type;
                    }
                }else if(pixelType==RGB){
                    GOPType[i][j]=getGOPTypeRGB(pixels[i][j]);
                }
            }
        }
    }
    boolean isOrigin(int[] or, int y, int x){
        return (y==or[0]&&x==or[1]);
    }
    
    
    void closeSegment(int origins[][][], GraphicalObject[][] GOs, int y0,int type, intRange ir){
        int x0=ir.getMin();
        int[] or=origins[y0][x0];
        GraphicalObject go=null;
        int x,y;
        y=or[0];
        x=or[1];
        if(GOs[y][x]==null){
            go=new GraphicalObject(GOPList.get(type));
            go.setAnchor(y, x);
            if(randomGOColor) go.setRandomRGB();
            GOs[y][x]=go;
//            if(go.type.StructureId!=-1||bIncludeBackground)GOList.add(go);
            GOList.add(go);
        }else{
            go=GOs[y][x];
        }
        go.addXseg(ir, y0);        
    }
    int getGOPTypeRGB(int pixel){
        int size;
        size=GOPList.size();
        for(int i=1;i<size;i++){
            if(GOPList.get(i).MatchingRGB(pixel)) return i;
        }
        return 0;
    }
    
    int getGOPTypeGRAY(int pixel){
        int size;
        size=GOPList.size();
        for(int i=1;i<size;i++){
            if(GOPList.get(i).MatchingGRAY(pixel)) return i;
        }
        return 0;
    }
    
    public static void showAllGOType(ArrayList <GraphicalObject> GOs){
        int size=GOs.size();
        for(int i=0;i<size;i++){
            GOs.get(i).bShow=true;
        }
    }
    
    public static void hideAllGOType(ArrayList <GraphicalObject> GOs){
        int size=GOs.size();
        for(int i=0;i<size;i++){
            GOs.get(i).bShow=false;
        }
    }
    
    void initGOIGrids(int w, int h){
        int width=w/GOIGridSize+1;
        int height=h/GOIGridSize+1;
        GOIndexGrids=new IntArray[height][width];
        int i,j;
        for(i=0;i<height;i++){
            for(j=0;j<width;j++){
                GOIndexGrids[i][j]=new IntArray();
            }
        }
    }
    void registerGOs(ArrayList<GraphicalObject> GOs){
        int i,j,size,size1,index,x,y,xMin,xMax,yMin,yMax,xi,yi;
        GraphicalObject Go;
        IntRangeArray ira;
        intRange ir;  
        IntArray ia;
        size=GOList.size();
        for(i=0;i<size;i++){
            Go=GOs.get(i); 
            Go.GOHandler=this;
            yMin=Go.yRange.getMin();
            yMax=Go.yRange.getMax();
            for(y=yMin;y<=yMax;y++){
                yi=y/GOIGridSize;
                index=y-yMin;
                ira=Go.yStripes.m_IntRangeArray2.get(index);
                size1=ira.m_intRangeArray.size();
                for(j=0;j<size1;j++){
                    ir=ira.m_intRangeArray.get(j);
                    xMin=ir.getMin();
                    xMax=ir.getMax();
                    for(xi=xMin/GOIGridSize;xi<=xMax/GOIGridSize;xi++){
                        ia=GOIndexGrids[yi][xi];
                        if(!ia.containsContent(i))ia.m_intArray.add(i);
                    }
                }
            }
        }
    }
    public GraphicalObject getGOAt(int x, int y){
        if(x<0)return null;
        if(y<0)return null;
        int lenY=GOIndexGrids.length,lenX=GOIndexGrids[0].length;
        int xi=x/GOIGridSize,yi=y/GOIGridSize;
        if(xi>lenX||yi>lenY) return null;
        IntArray ir=GOIndexGrids[yi][xi];
        GraphicalObject Go=null;
        int size=ir.m_intArray.size();
        int index;
        for(int i=0;i<size;i++){
            index=ir.m_intArray.get(i);
            Go=GOList.get(index);
            if(Go.contains(x, y)) return Go;
        }
        return null;
    }
    public ArrayList <GraphicalObject> findObjects(ImagePlus impl, ArrayList<GraphicalObjectProperty> GOPs){
        int w=impl.getWidth();
        int h=impl.getHeight();
        switch(impl.getType()){
            case ImagePlus.COLOR_RGB:
                updatePixels((int[])impl.getProcessor().getPixels(),w,h);
                break;
            case ImagePlus.COLOR_256:
                updatePixels((byte[])impl.getProcessor().getPixels(),w,h);
                break;
            case ImagePlus.GRAY8:
                updatePixels((byte[])impl.getProcessor().getPixels(),w,h);
                break;
            default:
                IJ.error("The Image type is not supported by the current version of method findObjects.");
                break;
        }
        GOPList.clear();
        GOPList=GOPs;
        GOPs.add(0, background);
        buildGOList();
        return GOList;
    }
    public void updatePixels(int[] pixels, int w, int h){
        if(w!=width||h!=height){
            width=w;
            height=h;
            this.pixels=new int[h][w];
        }
        int x,y,o;
        for(y=0;y<height;y++){
            o=y*width;
            for(x=0;x<width;x++){
                this.pixels[y][x]=pixels[o+x];
            }
        }
    }
    public void updatePixels(byte[] pixels, int w, int h){
        if(w!=width||h!=height){
            width=w;
            height=h;
            this.pixels=new int[h][w];
        }
        int x,y,o;
        for(y=0;y<height;y++){
            o=y*width;
            for(x=0;x<width;x++){
                this.pixels[y][x]=0xff&pixels[o+x];
            }
        }
    }
}

