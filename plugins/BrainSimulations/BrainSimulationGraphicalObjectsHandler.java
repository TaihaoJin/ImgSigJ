/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package BrainSimulations;

/**
 *
 * @author Taihao
 */
import ImageAnalysis.GraphicalObject;
import java.util.ArrayList;
import ij.ImagePlus;
import ij.process.ImageProcessor;
import ij.io.Opener;
import ij.io.OpenDialog;
import java.awt.Image;
import java.awt.Image.*;
import BrainSimulations.DataClasses.BrainSimulationGraphicalObjectProperty;
import BrainSimulations.DataClasses.BrainStructureNameNode;
import java.io.*;
import java.util.StringTokenizer;
import ij.IJ;
import utilities.CustomDataTypes.intRange;
import utilities.CommonMethods;
import utilities.ArrayofArrays.IntArray;
import BrainSimulations.DataClasses.BrainStructureNameHistNode;
import utilities.ArrayofArrays.IntRangeArray;

/**
 *
 * @author Taihao
 */
public class BrainSimulationGraphicalObjectsHandler {//At the time being, this handler handles
    //rgb images only.
    //From now on, my image define as [height][width]; just be consistent with iteration index; scanning line by line
    //This is to be consistent with matrix convention, i.e., first index indicates the vertical position. This is
    //oposite to x and y convention. And also is oposite to imagej convention.
    //This class has been restructured after ImageJNB65V5 (May of 2009)
    protected ArrayList <BrainStructureNameHistNode> m_vBrainStructureNameHistNodes=new ArrayList<BrainStructureNameHistNode>();
//    ArrayList <GraphicalObject> graphicalObjects;
    ImagePlus impl;
    ArrayList <BrainSimulationGraphicalObjectProperty> GOPList;
    ArrayList <BrainSimulationGraphicalObject> GOList;
    ArrayList <BrainSimulationGraphicalObject> EdgeList;
    IntArray GOPIndexGrids[][][]=new IntArray[26][26][26];
    int pixels[][];
    int [][]R,G,B;
    int r,g,b;
    int height,width;
    boolean bIncludeBackground;
    boolean bRegisteredGOP;
    boolean randomGOColor;
    boolean bFindingEdge;
        //diagonaly neibhoring pixels will be regarded as the same objects.
    IntArray GOPIndexes;
    ArrayList <connectionNode> connectionNodes;
    IntArray GOIndexGrids[][];
    int GOIGridSize=20;

    public BrainSimulationGraphicalObjectsHandler(){
        r=0;
        g=0;
        b=0;
        height=0;
        width=0;
        int i,j,k;
        for(i=0;i<26;i++){
            for(j=0;j<26;j++){
                for(k=0;k<26;k++){
                    GOPIndexGrids[i][j][k]=new IntArray();
                }
            }
        }
        GOPList=new ArrayList <BrainSimulationGraphicalObjectProperty> ();
        GOList=new ArrayList<BrainSimulationGraphicalObject>();
        bIncludeBackground=true;
        bRegisteredGOP=false;
        GOPIndexes=new IntArray();
        randomGOColor=false;
        connectionNodes =new ArrayList <connectionNode> ();
        bFindingEdge=false;
    }

    class connectionNode {
        public int[] or1,or2;
    }

     public void setGOPList(ArrayList <BrainSimulationGraphicalObjectProperty> GOPList0){
        if(GOPList0!=null){
            this.GOPList=GOPList0;
        }else{
            BuildGOPList();
        }
        registerGOP();
    }
    public ArrayList<BrainSimulationGraphicalObject> getGOList(){
        return GOList;
    }

    public void setImage(ImagePlus impl0){
        loadImage(impl0);
    }

    void registerGOP(){
        bRegisteredGOP=true;
        int size=GOPList.size();
        int r=0;g=0;b=0;
        intRange rRange,gRange,bRange;
        BrainSimulationGraphicalObjectProperty gop;
        int i,j,k,l;
        IntArray ia;
//        registerBackGroundGOP();
        for(i=0;i<size;i++){
            CommonMethods.showStatusAndProgress("registering graphical objects "+ i +" of " +size, (double)i/(double)size);
            gop=GOPList.get(i);

            if(gop.bRegisteringGenerally){
                rRange=new intRange(0,255);
                gRange=new intRange(0,255);
                bRange=new intRange(0,255);
            }else{
                rRange=gop.rRange;
                gRange=gop.gRange;
                bRange=gop.bRange;
            }

            int rMin=rRange.getMin()/10,rMax=rRange.getMax()/10;
            int gMin=gRange.getMin()/10,gMax=gRange.getMax()/10;
            int bMin=bRange.getMin()/10,bMax=bRange.getMax()/10;
            for(r=rMin;r<=rMax;r++){
                for(g=gMin;g<=gMax;g++){
                    for(b=bMin;b<=bMax;b++){
                        ia=GOPIndexGrids[r][g][b];
                        if(!CommonMethods.containsContent(ia, i)) ia.m_intArray.add(i);
                    }
                }
            }
        }
    }
    void registerBackGroundGOP(){
        int i,j,k;
        for(i=0;i<26;i++){
            for(j=0;j<26;j++){
                for(k=0;k<26;k++){
                    GOPIndexGrids[i][j][k].m_intArray.add(0);
                }
            }
        }
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
    void connectDiagonally(int structureID, boolean b){
        int size= GOPList.size();
        BrainSimulationGraphicalObjectProperty gop;
        for(int i=0;i<size;i++){
            gop=GOPList.get(i);
            if(gop.StructureId==structureID) gop.connectDiagonally(b);
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
        int[][] GOPType=new int[height][width];
        BrainSimulationGraphicalObject[][] GOs=new BrainSimulationGraphicalObject[height][width];
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
        drawGO(GOs,origins,-2);
    }
    void markGOPType(int GOPType[][]){
        int i,j;
        GOPIndexes.m_intArray.clear();
        if(!bRegisteredGOP){
            for(i=0;i<GOPList.size();i++){
                GOPIndexes.m_intArray.add(i);
            }
        }
        for(i=0;i<height;i++){
            for(j=0;j<width;j++){
                GOPType[i][j]=getGOPType(pixels[i][j]);
            }
        }
    }
    boolean isOrigin(int[] or, int y, int x){
        return (y==or[0]&&x==or[1]);
    }


    void closeSegment(int origins[][][], BrainSimulationGraphicalObject[][] GOs, int y0,int type, intRange ir){
        int x0=ir.getMin();
        int[] or=origins[y0][x0];
        BrainSimulationGraphicalObject go=null;
        int x,y;
        y=or[0];
        x=or[1];
        if(GOs[y][x]==null){
            go=new BrainSimulationGraphicalObject(GOPList.get(type));
            go.setAnchor(y, x);
            if(randomGOColor) go.setRandomRGB();
            GOs[y][x]=go;
            if(go.type.StructureId!=-1||bIncludeBackground)GOList.add(go);
        }else{
            go=GOs[y][x];
        }
        if(go.type.StructureId!=-1||bIncludeBackground) go.addXseg(ir, y0);
    }
    int getGOPType(int pixel){
        int type=0;
        int size;
        double p=0.,pMax=0.;
        int[] rgb=CommonMethods.intTOrgb(pixel);
        int r=rgb[0],g=rgb[1], b=rgb[2];
        IntArray ia;
        if(bRegisteredGOP){
            ia=GOPIndexGrids[r/10][g/10][b/10];
        }else{
            ia=GOPIndexes;
        }
        size=ia.m_intArray.size();
        int index;
        for(int i=0;i<size;i++){
            index=ia.m_intArray.get(i);
            p=GOPList.get(index).MatchingProb_Sum(r,g,b);
            if(p>pMax){
                pMax=p;
                type=index;
            }
        }
        return type;
    }

    public void loadImage(ImagePlus impl0){
        if(impl0==null) impl=CommonMethods.importImage();
        else impl=impl0;

        width=impl.getWidth();
        height=impl.getHeight();
        ImageProcessor impr=impl.getProcessor();
        int i,j;
        pixels=new int[height][width];
        R=new int[height][width];
        G=new int[height][width];
        B=new int[height][width];
        int p;
        int r,g,b;
        for(i=0;i<height;i++){
            for(j=0;j<width;j++){
                p=impr.getPixel(j,i);
                pixels[i][j]=p;
                r=0xff & (p>>16);
                g=0xff & (p>>8);
                b=0xff & p;
                R[i][j]=r;
                G[i][j]=g;
                B[i][j]=b;
            }
        }
    }

    public static int[] copyRGBPixels(ImagePlus impl0){
        int w=impl0.getWidth();
        int h=impl0.getHeight();
        int[] p=new int[h*w];
        int[] p0=(int[])impl0.getProcessor().getPixels();
        int length=w*h;
        for(int i=0;i<length;i++){
            p[i]=p0[i];
        }
        return p;
    }
    public ArrayList<BrainSimulationGraphicalObject> extractGraphicalObjects(ImagePlus impl0){
        ArrayList <BrainSimulationGraphicalObject> gobs=new ArrayList <BrainSimulationGraphicalObject>();
        return gobs;
    }
    void BuildGOPList(){
        int num=0;
        GOPList.clear();
        GOPIndexes.m_intArray.clear();
        BrainSimulationGraphicalObjectProperty gop=new BrainSimulationGraphicalObjectProperty();
        gop.setAsBackground();
        GOPList.add(gop);
        GOPIndexes.m_intArray.add(num);
        num++;
        try {ReadBrainStructureNames_Hist();}
        catch (IOException e){
            IJ.error("IOException at GraphicalObjectsHandler.java line 72");
        }
        int size=m_vBrainStructureNameHistNodes.size();
        for(int i=0;i<size;i++){
            BrainSimulationGraphicalObjectProperty GOP=new BrainSimulationGraphicalObjectProperty(m_vBrainStructureNameHistNodes.get(i));
            GOPList.add(GOP);
            GOPIndexes.m_intArray.add(num);
            num++;
        }
        GOPList.add(blackGOP());
        GOPIndexes.m_intArray.add(num);
        num++;
    }

    BrainSimulationGraphicalObjectProperty blackGOP(){
        BrainSimulationGraphicalObjectProperty gop=new BrainSimulationGraphicalObjectProperty();
        gop.setAbbreviation("blk");
        gop.setStructureName("Black Object");
        gop.setStructureId(-3);
        gop.setBlue(0, 20);
        gop.setGreen(0, 20);
        gop.setRed(0, 20);
        gop.setParentStruct("blk");
        return gop;
    }

    BrainSimulationGraphicalObjectProperty brightGOP(int brightness){
        BrainSimulationGraphicalObjectProperty gop=new BrainSimulationGraphicalObjectProperty();
        gop.setAbbreviation("brt");
        gop.setStructureName("Bright Object");
        gop.setStructureId(GOPList.size());
        gop.setBlue(255,255-brightness);
        gop.setGreen(255,255-brightness);
        gop.setRed(255,255-brightness);
        gop.setParentStruct("brt");
        return gop;
    }

    public void ReadBrainStructureNames_Hist() throws IOException {

        String title="Read a Brain Structure Name Hist File";
//        String path="";
        String dir="C:\\Taihao\\Lab UCSF\\Mouse Brain Library\\Documentations\\data\\";
//        String path="";//The path of the file to open. When specified, the OpenDialog will not open and use the path unchanged.
	String name = "Annotated brainstructures.csv";
        File file=new File(dir,name);
        FileReader f = new FileReader(file);

        BufferedReader br=new BufferedReader(f);
        String s;
        s=br.readLine();
        Integer InTemp=new Integer("19");
        int nNumTokens=0;
        while((s=br.readLine())!=null)
        {
            StringTokenizer st0=new StringTokenizer(s,",",false);
            nNumTokens=st0.countTokens();
            BrainStructureNameHistNode av0=new BrainStructureNameHistNode();
            av0.StructureName=st0.nextToken();
            av0.Abbreviation=st0.nextToken();
            if(nNumTokens==9)
                av0.ParentStruct=st0.nextToken();
            else
                av0.ParentStruct="";
            av0.red=InTemp.valueOf(st0.nextToken());
            av0.green=InTemp.valueOf(st0.nextToken());
            av0.blue=InTemp.valueOf(st0.nextToken());
            av0.informaticsId=InTemp.valueOf(st0.nextToken());
            av0.StructureId=InTemp.valueOf(st0.nextToken());
            av0.imageFileName=st0.nextToken();
            m_vBrainStructureNameHistNodes.add(av0);
        }
    }

    public void drawGO(BrainSimulationGraphicalObject[][] GOs, int[][][] origins,int StructureID){
//        ImagePlus impl0=CommonMethods.cloneImage("structureID:" +StructureID, impl);
        int i,j,x,y,offset;
        BrainSimulationGraphicalObject go;
        int pixels[]=new int[height*width];
        for(i=0;i<height;i++){
            offset=i*width;
            for(j=0;j<width;j++){
                y=origins[i][j][0];
                x=origins[i][j][1];
                go=GOs[y][x];
                if(go==null)continue;
                if(go.type.StructureId==StructureID){
                    pixels[offset+j]=go.pixel;
                }
            }
        }
        ImagePlus impl0=CommonMethods.getRGBImage("structureID:" +StructureID, width, height, pixels);
        impl0.getProcessor().setPixels(pixels);
        impl0.setTitle("StructureID: "+StructureID);
        impl0.show();
    }

    public static void hideGOType(ArrayList <BrainSimulationGraphicalObject> GOs, int structureID){
        int size=GOs.size();
        for(int i=0;i<size;i++){
            if(GOs.get(i).type.StructureId==structureID) GOs.get(i).bShow=false;
        }
    }

    public static void showGOType(ArrayList <BrainSimulationGraphicalObject> GOs, int structureID){
        int size=GOs.size();
        for(int i=0;i<size;i++){
            if(GOs.get(i).type.StructureId==structureID) GOs.get(i).bShow=true;
        }
    }

    public static void showAllGOType(ArrayList <BrainSimulationGraphicalObject> GOs){
        int size=GOs.size();
        for(int i=0;i<size;i++){
            GOs.get(i).bShow=true;
        }
    }

    public static void hideAllGOType(ArrayList <BrainSimulationGraphicalObject> GOs){
        int size=GOs.size();
        for(int i=0;i<size;i++){
            GOs.get(i).bShow=false;
        }
    }


    public void hideGOType(int structureID){
        hideGOType(GOList, structureID);
    }

    public void showGOType(int structureID){
        showGOType(GOList, structureID);
    }

    public void assignGOPType(BrainSimulationGraphicalObject go){
        go.buildHist(pixels);
        int type=0;
        int size=0;
        double p=0.,pMax=0.;
        IntArray ia;
        int i,j,k;
        int a[]=go.getAnchor();
        if(a[0]==134){
            size=size;
        }
        if(bRegisteredGOP){
            ia=new IntArray();
            intRange rRange=go.getRRange();
            intRange gRange=go.getGRange();
            intRange bRange=go.getBRange();
            int rMin=rRange.getMin()/10;
            int gMin=gRange.getMin()/10;
            int bMin=bRange.getMin()/10;
            int rMax=rRange.getMax()/10;
            int gMax=gRange.getMax()/10;
            int bMax=bRange.getMax()/10;
            for(i=rMin;i<=rMax;i++){
                for(j=gMin;j<=gMax;j++){
                    for(k=bMin;k<=bMax;k++){
                        ia.appendContents(GOPIndexGrids[i][j][k]);
                    }
                }
            }
        }else{
            ia=GOPIndexes;
        }


        size=ia.m_intArray.size();
        int index;
        BrainSimulationGraphicalObjectProperty gop;
        for(i=0;i<size;i++){
            index=ia.m_intArray.get(i);
            gop=GOPList.get(index);
            if(gop.StructureId==90){
                i=i;
            }
            p=go.overlapRGBProb(gop);
            if(p>pMax){
                pMax=p;
                type=index;
            }
        }
        go.setGOPType(GOPList.get(type));
        go.setProbType(pMax);
    }

    public void setGOType(){
        BuildGOPList();
        int size=GOList.size();
        for(int i=0;i<size;i++){
            GOList.get(i).buildHist(pixels);
            assignGOPType(GOList.get(i));
        }
    }
    public void setGOType(ArrayList<BrainSimulationGraphicalObject> GOs, IntArray protectedTypes){
        BuildGOPList();
//        registerGOP();
        bRegisteredGOP=false;
        int size=GOs.size();
        BrainSimulationGraphicalObject Go;
        int sID;
        for(int i=0;i<size;i++){
            Go=GOList.get(i);
            sID=Go.getType().StructureId;
            if(protectedTypes.containsContent(sID)) continue;
            assignGOPType(Go);
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
    void registerGOs(ArrayList<BrainSimulationGraphicalObject> GOs){
        int i,j,size,size1,index,x,y,xMin,xMax,yMin,yMax,xi,yi;
        BrainSimulationGraphicalObject Go;
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
    public BrainSimulationGraphicalObject getGOAt(int x, int y){
        if(x<0)return null;
        if(y<0)return null;
        int lenY=GOIndexGrids.length,lenX=GOIndexGrids[0].length;
        int xi=x/GOIGridSize,yi=y/GOIGridSize;
        if(xi>lenX||yi>lenY) return null;
        IntArray ir=GOIndexGrids[yi][xi];
        BrainSimulationGraphicalObject Go=null;
        int size=ir.m_intArray.size();
        int index;
        for(int i=0;i<size;i++){
            index=ir.m_intArray.get(i);
            Go=GOList.get(index);
            if(Go.contains(x, y)) return Go;
        }
        return null;
    }
    public boolean performedEdgeFinding(){
        return bFindingEdge;
    }
    public void buildGOTopology(ArrayList <BrainSimulationGraphicalObject> GOs){
        int w=impl.getProcessor().getWidth();
        int h=impl.getProcessor().getHeight();
        initGOIGrids(w,h);
        registerGOs(GOList);
        int size=GOList.size();
        BrainSimulationGraphicalObject go;
        for(int i=0;i<size;i++){
            go=GOList.get(i);
            if(bFindingEdge&&go.getType().StructureId==-2)continue;
            go.buildEnclosedObjectList(w,h);
        }
    }
    public ArrayList <BrainSimulationGraphicalObject> findBrightObjects(ImagePlus impl, double minThreshold, double maxThreshold){
        loadImage(impl);
        GOPList.clear();
        BrainSimulationGraphicalObjectProperty gop=new BrainSimulationGraphicalObjectProperty();
        gop.setAsBackground();
        GOPList.add(gop);
        gop=new BrainSimulationGraphicalObjectProperty();
        gop.setAsBrightObject((int)maxThreshold);
        GOPList.add(gop);
        buildGOList();
        return GOList;
    }
    public void calCurvatures(){
        int size=GOList.size();
        for(int i=0;i<size;i++){
            GOList.get(i).calCurvature();
        }
    }
}

