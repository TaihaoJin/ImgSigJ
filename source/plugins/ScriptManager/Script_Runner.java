/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ScriptManager;
import ij.plugin.PlugIn;
import ij.io.OpenDialog;
import ij.io.SaveDialog;
import ij.ImagePlus;
import ij.IJ;
import utilities.ArrayofArrays.StringArray;
import java.util.ArrayList;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import utilities.io.PrintAssist;
import utilities.CommonMethods;
import ij.WindowManager;
import java.awt.FileDialog;
import javax.swing.JFileChooser;
import stacks.Stack_RunningWindowAverage;
import java.util.Hashtable;
import utilities.ArgumentDecoder;
import Demo.Demo_;
import ImageAnalysis.ContourFollower;
import ImageAnalysis.LandscapeAnalyzer;
import utilities.ArrayofArrays.IntPairArray;
import java.awt.Point;
import utilities.ArrayofArrays.PointArray;
import ImageAnalysis.GeneralGraphicalObject;
import java.util.Queue;
import utilities.CustomDataTypes.intRange;
import FluoObjects.IntensityPeakObjectOrganizer;
import utilities.FormattedReader;
import java.util.Formatter;
import ij.plugin.filter.GaussianBlur;
import utilities.AbfHandler.Abf;
import utilities.statistics.Histogram;
import ImageAnalysis.PercentileFilter;
import utilities.Geometry.ImageShapes.CircleImage;
import utilities.CommonGuiMethods;
import ImageAnalysis.IPOPixelHeightsHandler;
import utilities.io.FileAssist;
import utilities.statistics.MeanSem0;
import utilities.Geometry.ImageShapes.ImageShape;
import utilities.statistics.JarqueBeraTest;
import utilities.CommonStatisticsMethods;
import utilities.AbfHandler.AbfHandler;
import utilities.AbfHandler.AbfTraceFitter;
import utilities.Non_LinearFitting.Fitting_Function;
import utilities.statistics.MeanSemFractional0;
import FluoObjects.IPOGTrackNode;
import FluoObjects.StackIPOGTracksNode;
import utilities.Gui.GeneralManager;
import utilities.Gui.AnalysisMasterForm;
import FluoObjects.IPOAnalyzerForm;
import utilities.Gui.PlotHandlerGui;
import FluoObjects.IPOGaussianNodeHandler;
import utilities.io.AsciiInputAssist;

/*
 *
 *
 * @author Taihao
 */
public class Script_Runner implements PlugIn{
    String m_sScriptFileName;
    String m_sScriptFileDirectory;
    String m_sHomeDir=System.getProperty("user.home");
    ArrayList <Hashtable> m_vcScript;
    Hashtable m_cStoredVariables=new Hashtable();
    Hashtable m_cStoredImages;
    int m_nNumCommands;
    ImagePlus currentImage, previousImage;
    ArrayList <String> m_vsPreviousCommands=new ArrayList();

    public void run(String Arg){
        m_sScriptFileName="Not Opened";
        runScript();
    }
    void runScript(){
        long startTime = System.currentTimeMillis();
        getScriptFileName();
        m_vcScript=new ArrayList();
        m_cStoredImages=new Hashtable();
        inputScript();
        int nSize=m_vcScript.size();
        for(int i=0;i<nSize;i++){
            runCommand(m_vcScript.get(i));
        }
        long endTime = System.currentTimeMillis();
        IJ.showMessage(nSize+" commonds", "Elapsed Time (ms): "+Long.toString(endTime-startTime));
    }
    void getScriptFileName()
    {
        JFileChooser jfc=CommonGuiMethods.openFileDialogExtFilter("input a script file", JFileChooser.OPEN_DIALOG, "script file", "Srp",OpenDialog.getDefaultDirectory());
        m_sScriptFileName=jfc.getSelectedFile().getName();
        m_sScriptFileDirectory=jfc.getSelectedFile().getAbsolutePath();
        m_sScriptFileDirectory=CommonMethods.getDirectory(m_sScriptFileDirectory);
        m_sHomeDir=m_sScriptFileDirectory;
    }
    
    void inputScript () {
        m_vcScript.clear();
        String path = m_sScriptFileDirectory +m_sScriptFileName;
        ArrayList <String> vsStringCollection=new ArrayList <String>();
        AsciiInputAssist.getFileContentsAsStringArray(path, vsStringCollection);
        buildScript(vsStringCollection);
    }
    
    void inputScript0 () {
        m_vcScript.clear();
        File file = new File(m_sScriptFileDirectory , m_sScriptFileName);
        FileReader f=null;
        try{f=new FileReader(file);}
        catch (FileNotFoundException e){
            IJ.error("the script file not found");
        }
        BufferedReader br=new BufferedReader(f);
        m_nNumCommands=0;
        String line="";
        ArrayList <String> vsStringCollection=new ArrayList <String>();
        while (true){
            try{line=br.readLine(); }
            catch (IOException e){
                IJ.error("IOExeption");
            }
            if(endOfScript(line))break;
            if(remarkLine(line)) continue;
            collectStrings(line, vsStringCollection);
        }
        buildScript(vsStringCollection);
//        executeScript();
    }
    boolean remarkLine(String line){
        if(line.startsWith("!"))return true;
        return false;
    }
    void buildScript(ArrayList <String> vsStringCollection){
        int index=0, nSize=vsStringCollection.size();
        while(!vsStringCollection.get(index).contentEquals("Commond")) {
            index++;
        }
//        index++;
        while(index<nSize){
            StringArray sa=new StringArray();
            sa.m_stringArray.add(vsStringCollection.get(index));
            index++;
            while(!vsStringCollection.get(index).contentEquals("Commond")){
                sa.m_stringArray.add(vsStringCollection.get(index));
                index++;
                if(index>=nSize) break;
            }
            if(sa.m_stringArray.size()>0)m_vcScript.add(ArgumentDecoder.buildHashtable(sa.m_stringArray));
        }
    }

    void collectStrings(String line, ArrayList <String> vsStringCollection){
        ArrayList <String> sa=PrintAssist.TokenizeString(line, new String(" "), true);
        int nSize=sa.size();
        for(int i=0;i<nSize;i++){
            vsStringCollection.add(sa.get(i));
        }
    }
    
    boolean endOfScript(String line){
        ArrayList <String> sa=PrintAssist.TokenizeString(line, new String(" "), true);
        if(sa.size()==1){
            if(sa.get(0).contentEquals("EndScript")) return true;
        }
        return false;
    }

    int runScript(ArrayList <String> vsScript)
    {
        return 1;
    }

    void runCommand(Hashtable cCommandTable){
        String sCommandName=(String) cCommandTable.get("Commond");
        ImagePlus impl;
        String key, sImageLable,path,value;
        boolean bValidName=false;

        IJ.showStatus("runing: "+sCommandName);
        m_vsPreviousCommands.add(sCommandName);
        if(sCommandName.contentEquals("OpenImage")){
            path=(String) cCommandTable.get("path:");
            if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
            impl=CommonMethods.importImage(path);
            if(cCommandTable.containsKey("imageLabelForStoring:")){
                key=(String) cCommandTable.get("imageLabelForStoring:");
                m_cStoredImages.put(key, impl);
            }
            impl.show();
            previousImage=currentImage;
            currentImage=impl;
            bValidName=true;
        }
        if(sCommandName.contentEquals("convertToFullRange")){
            CommonMethods.scaleToFullRange(retrieveImages(cCommandTable).get(0));
            bValidName=true;
        }
        if(sCommandName.contentEquals("convertToGray8")){
            impl=CommonMethods.convertImage(retrieveImages(cCommandTable).get(0), ImagePlus.GRAY8, false);
            previousImage=currentImage;
            currentImage=impl;
            impl.show();
            bValidName=true;
        }
        if(sCommandName.contentEquals("testFileOpenDialog")){
            CommonMethods.openFileDialogExtFilter("input a script file", JFileChooser.OPEN_DIALOG, "script file","Scr",OpenDialog.getDefaultDirectory());
            bValidName=true;
        }
        if(sCommandName.contentEquals("makeMIPImage")){
            impl=CommonMethods.getMIPImage(retrieveImages(cCommandTable).get(0));
            previousImage=currentImage;
            currentImage=impl;
            impl.show();
            bValidName=true;
        }
        if(sCommandName.contentEquals("StackRuningWindowAverage")){
            key="WindowSize:";
            int ws=1;
            if(cCommandTable.containsKey(key)){
                value=(String)cCommandTable.get("WindowSize:");
                ws=Integer.valueOf(value);
            }
            impl=retrieveImages(cCommandTable).get(0);
            Stack_RunningWindowAverage.RunningWindowAverage(impl, ws);
            bValidName=true;
        }
        if(sCommandName.contentEquals("MeanFiltering")){
            int radius=Integer.valueOf((String)cCommandTable.get("radius:"));
            impl=retrieveImages(cCommandTable).get(0);
            impl=CommonMethods.cloneImage(sCommandName, impl);
            previousImage=currentImage;
            currentImage=impl;
            impl.show();
            CommonMethods.meanFilteringGray8(impl, radius);
            bValidName=true;
        }
        if(sCommandName.contentEquals("stampPixels")){
            impl=retrieveImages(cCommandTable).get(0);
            CommonMethods.stampPixels(impl);
            bValidName=true;
        }
        if(sCommandName.contentEquals("demo_IPOHOrganizer")){
            Demo_.demo_IPOHOrganizer();
            bValidName=true;
        }
        if(sCommandName.contentEquals("ScalePixelValueToFullRange")){
            impl=retrieveImages(cCommandTable).get(0);
            String label;
            double dRange[]=new double[2];
            dRange[1]=-1;
            dRange[0]=0;
            if(cCommandTable.containsKey("retrievePixelRange")){
                label=(String)cCommandTable.get("retrievePixelRange");
                if(m_cStoredVariables.containsKey(label)){
                    dRange=(double[])m_cStoredVariables.get(label);
                }
            }
            CommonMethods.scaleToFullRange(impl,dRange);
            if(cCommandTable.containsKey("storePixelRange")){
                label=(String)cCommandTable.get("storePixelRange");
                m_cStoredVariables.put(label, dRange);
            }
            bValidName=true;
        }
        if(sCommandName.contentEquals("CloneImage")){
            impl=retrieveImages(cCommandTable).get(0);
            String title=retrieveTitles(cCommandTable).get(0);;
            ImagePlus implc=CommonMethods.cloneImage(title, impl);
            previousImage=currentImage;
            currentImage=implc;
            implc.show();
            bValidName=true;
        }
        if(sCommandName.contentEquals("invertImage")){
            impl=retrieveImages(cCommandTable).get(0);
            CommonMethods.invertImage(impl);
            impl.setTitle("interted "+impl.getTitle());
            bValidName=true;
        }
        if(sCommandName.contentEquals("RunPlugIn")){
            runPlugIn(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("saveImage")){
            saveImage(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("storeImage")){
            impl=currentImage;
            key=ArgumentDecoder.getKeysBeginningWith(cCommandTable, "imageLabel").get(0);
            if(!cCommandTable.containsKey(key)) IJ.error("no image label is given for storeImage");
            String label=(String) cCommandTable.get(key);
            m_cStoredImages.put(label,impl);
            bValidName=true;
        }
        if(sCommandName.contentEquals("markWatershedBoundaries")){
            String key1=(String)cCommandTable.get("imageLabel1:");
            String key2=(String)cCommandTable.get("imageLabel2:");
            ImagePlus implo=(ImagePlus)m_cStoredImages.get(key1);
            previousImage=currentImage;
            currentImage=implo;
            ImagePlus implw=(ImagePlus)m_cStoredImages.get(key2);
            previousImage=currentImage;
            currentImage=implw;
            markWatershedBoundaries(implo,implw);
            bValidName=true;
        }
        if(sCommandName.contentEquals("closeStoredImage")){
            closeStoredImage(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("closeAllStoredImages")){
            closeAllStoredImages();
            bValidName=true;
        }
        if(sCommandName.contentEquals("hideAllImages")){
            hideAllImages();
            bValidName=true;
        }
        if(sCommandName.contentEquals("closeAllImages")){
            closeAllImages();
            bValidName=true;
        }
        if(sCommandName.contentEquals("clearStorages")){
            clearStorages();
            bValidName=true;
        }
        if(sCommandName.contentEquals("breakPoint")){
            breakPoint();
            bValidName=true;
        }
        if(sCommandName.contentEquals("markIPObjects")){
            markIPObjects(cCommandTable);
            bValidName=true;
        }

        if(sCommandName.contentEquals("buildMIPCountours")){
            buildMIPCountours(cCommandTable);
            bValidName=true;
        }

        if(sCommandName.contentEquals("monitorIPObjects")){
            monitorIPObjects(cCommandTable);
            bValidName=true;
        }

        if(sCommandName.contentEquals("monitorIPObjects_IPO")){
            IJ.error("monitorIPObjects_IPO is no longer used. Use precomputed pixel heights!");
//            monitorIPObjects_IPO(cCommandTable);
            bValidName=true;
        }

        if(sCommandName.contentEquals("setHomeDir")){
            m_sHomeDir=(String) retrieveVariable(cCommandTable,"homeDir:");
            bValidName=true;
        }

        if(sCommandName.contentEquals("gaussianBlur")){
            gaussianBlur(cCommandTable);
            bValidName=true;
        }

        if(sCommandName.contentEquals("testAbf")){
            testAbf(cCommandTable);
            bValidName=true;
        }

        if(sCommandName.contentEquals("testPerHist")){
            testPerHist(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("activateStoredImage")){
            activateStoredImage(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("exportPixelHeightIPO_Subpixel")){
            exportPixelHeightIPO_Subpixel(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("MonitorIPO_PrecomputedGaussianNodes")){
            MonitorIPO_PrecomputedGaussianNodes(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("MonitorIPO_PrecomputedGaussianNodeGroups")){
            MonitorIPO_PrecomputedGaussianNodeGroups(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("export_PrecomputedPixelHeights")){
            export_PrecomputedPixelHeights(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("export_PrecomputedPixelHeights_Subpixel")){
            export_PrecomputedPixelHeights_Subpixel(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("testPixelHeightsNormality")){
            testPixelHeightsNormality(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("exportCurrentNoiseCurve")){
            AbfHandler ah=new AbfHandler();
            ah.exportCurrentNoiseCurve(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("fitAbfTrace")){
            AbfHandler ah=new AbfHandler();
//            ah.fitAbfTrace(cCommandTable);
            IJ.error("fitAbfTrace is no longer supported. Use exportFittedTrace commond");
            bValidName=true;
        }
        if(sCommandName.contentEquals("exportFittedTrace")){
            AbfTraceFitter ah=new AbfTraceFitter();
            ah.exportFittedTrace_Enveloping(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("exportISigma_hipass")){
            AbfHandler ah=new AbfHandler();
            ah.exportISigma_hipass(m_sScriptFileDirectory+m_sScriptFileName);
            bValidName=true;
        }
        if(sCommandName.contentEquals("DetectIPOGTLevels_FeatureBased")){
            DetectIPOGTLevels_FeatureBased(cCommandTable);
            bValidName=true;
        }
        
        if(sCommandName.contentEquals("TLG_TLH_Conversion")){
            TLG_TLH_Conversion(cCommandTable);
            bValidName=true;
        }
        
        if(sCommandName.contentEquals("AnalyzeIPOGTTransitionStatistics")){
            AnalyzeIPOGTTransitionStatistics(cCommandTable);
            bValidName=true;
        }
        
        if(sCommandName.contentEquals("StoreFormatter")){
            storeFormatter(cCommandTable);
            bValidName=true;
        }
        if(sCommandName.contentEquals("CloseFormatter")){
            closeFormatter();
            bValidName=true;
        }
        if(sCommandName.contentEquals("Build_ARFF")) {
            buildARFF(cCommandTable);
            bValidName=true;
        }
        if(!bValidName) IJ.error(sCommandName+" is not a valid command");
    }

    void testAbf(Hashtable hTable){
        Abf cAbf=new Abf();
        cAbf.exportDemoAbf(4, 2000);
    }
    
    void testPerHist(Hashtable hTable){
        ImagePlus impl=WindowManager.getCurrentImage();
        double dPer=0.02;
        int radius=25;
        ImagePlus implc=CommonMethods.cloneImage(impl);
        PercentileFilter pf=new PercentileFilter(implc, dPer, new CircleImage(radius),1);
        implc.show();
        ImagePlus implb=pf.getBackgroundImage();
        implb.show();
    }

    void runPlugIn(Hashtable hTable){
        Hashtable ht=(Hashtable)hTable.clone();
        ht.remove("className:");
        String line;
        String className=(String) hTable.get("className:");
        String passImage=(String) hTable.get("passImage:");
        String label;
        ImagePlus impl;
        impl=retrieveImages(hTable).get(0);
        if(passImage.equalsIgnoreCase("true")){
            ArrayList <String> labels =ArgumentDecoder.getKeysBeginningWith(hTable, "imageLabel");
            if(labels.size()>0) {
                label=labels.get(0);
                ht.remove(label);
            }
//            WindowManager.setTempCurrentImage(impl);
            line=ArgumentDecoder.getLine(hTable);
            IJ.runPlugIn(impl,className, line);
        }else{
            line=ArgumentDecoder.getLine(hTable);
            IJ.runPlugIn(className, line);
        }
        impl=WindowManager.getCurrentImage();
        impl.setTitle(CommonMethods.getClassName(className)+" "+impl.getTitle());
        previousImage=currentImage;
        currentImage=impl;
    }
    ImagePlus retrieveStoredImage(String sImageLabel){
        if(!m_cStoredImages.containsKey(sImageLabel)) IJ.error("no stored images matching the lable: "+ sImageLabel);
        return (ImagePlus) m_cStoredImages.get(sImageLabel);
    }
    ArrayList <ImagePlus> retrieveImages(Hashtable hTable){//return the current image if there are no image labels
        ArrayList<ImagePlus> images=new ArrayList();
        ImagePlus impl;
        String key="imageLabel", sImageLabel;
        ArrayList <String> keys=ArgumentDecoder.getKeysBeginningWith(hTable, key);
        int nSize=keys.size();
        if(nSize>0)
        {
            for(int i=0;i<nSize;i++){
                key=keys.get(i);
                sImageLabel=(String)hTable.get(key);
                if(!m_cStoredImages.containsKey(sImageLabel)) continue;
                impl=(ImagePlus)m_cStoredImages.get(sImageLabel);
                previousImage=currentImage;
                currentImage=impl;
                images.add(impl);
            }
        }
        if(images.isEmpty()){
            impl=currentImage;
            images.add(impl);
        }
        
        return images;
    }
    ArrayList <String> retrievePaths(Hashtable hTable){//return the current image if there are no image labels
        ArrayList<String> paths=new ArrayList();
        String path;
        String key="path";
        ArrayList <String> keys=ArgumentDecoder.getKeysBeginningWith(hTable, key);
        int nSize=keys.size();
        for(int i=0;i<nSize;i++){
            key=keys.get(i);
            if(!hTable.containsKey(key)) continue;
            path=(String)hTable.get(key);
            if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
            paths.add(path);
        }
        return paths;
    }

    String retrievePath(Hashtable hTable, String key){//return the current image if there are no image labels
        String path=(String) retrieveVariable(hTable,key);
        if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
        return path;
    }

    ArrayList <String> retrieveTitles(Hashtable hTable){//return the current image if there are no image labels
        ArrayList<String> titles=new ArrayList();
        String title;
        String key="title";
        ArrayList <String> keys=ArgumentDecoder.getKeysBeginningWith(hTable, key);
        int nSize=keys.size();
        for(int i=0;i<nSize;i++){
            key=keys.get(i);
            if(!hTable.containsKey(key)) continue;
            title=(String)hTable.get(key);
            titles.add(title);
        }
        return titles;
    }
    void saveImage(Hashtable hTable){
        ImagePlus impl=retrieveImages(hTable).get(0);
        ArrayList<String> paths=retrievePaths(hTable);
        String path;
        if(paths.size()>0){
            path=paths.get(0);
        }else{
                JFileChooser jfc=CommonMethods.openFileDialogExtFilter("save an image", JFileChooser.SAVE_DIALOG, "tiff file", "tif",OpenDialog.getDefaultDirectory());
                path=jfc.getSelectedFile().getAbsolutePath();
        }
        CommonMethods.saveImage(impl, path);
    }
    public void markWatershedBoundaries(ImagePlus implo, ImagePlus implw){
        ArrayList <GeneralGraphicalObject> ggos=new ArrayList();
        int h=implo.getHeight(),w=implo.getWidth(),i,j,pixel,o;
        int[][] stamp=new int[h][w];
        ImagePlus impll=CommonMethods.copyToRGBImage(implo);
        impll.show();
        int[][] pixelsw=new int[h][w];
        int[][] pixelso=new int[h][w];

        int num=implo.getStackSize();
        for(int is=1;is<=num;is++){
            ArrayList <PointArray> paa=new ArrayList();
            ArrayList <PointArray> paaEP=new ArrayList();
            implw.setSlice(is);
            implo.setSlice(is);
            impll.setSlice(is);
            CommonMethods.getPixelValue(implw, impll.getCurrentSlice(), pixelsw);
            CommonMethods.getPixelValue(implo, implo.getCurrentSlice(),pixelso);
//            CommonMethods.stampPixels(implo, stamp);
            ArrayList <Point> localMaxima=new ArrayList();
            
            CommonMethods.getSpecialLandscapePoints(pixelso, w, h, LandscapeAnalyzer.localMaximum, localMaxima);
   /*         for(i=0;i<h;i++){
                for(j=0;j<w;j++){
                    if(stamp[i][j]==LandscapeAnalyzer.localMaximum){
                        if(pixelsw[i][j]>0) continue;
                        PointArray pa=new PointArray();
                        pa.m_pointArray=ContourFollower.getContour_Out(pixelsw, w, h, new Point(j,i), 0, 10);
                        if(CommonMethods.isEIPoint(pixelso, w,h,j, i)){
                            GeneralGraphicalObject ggo=new GeneralGraphicalObject();
                            CommonMethods.maskEIPoints(pixelso, stamp, w, h, j, i, ggo);
                            ggos.add(ggo);//mark them individually later
                            paaEP.add(pa);
                        }else{
                            paa.add(pa);
                            localMaxima.add(new Point(j,i));
                        }
                    }
                }
            }*/
            int nSize=localMaxima.size();
            Point p;
            int x,y;
            for(i=0;i<nSize;i++){
                p=localMaxima.get(i);
                y=p.y;
                x=p.x;
                if(pixelsw[p.y][p.x]>0) continue;
                PointArray pa=new PointArray();
                pa.m_pointArray=ContourFollower.getContour_Out(pixelsw, w, h, new Point(x,y), 0, 10);
                paa.add(pa);
            }

            nSize=paa.size();
            for(i=0;i<nSize;i++){
                pixel=CommonMethods.randomColor().getRGB();
                CommonMethods.drawDot(impll, localMaxima.get(i), pixel);
                CommonMethods.drawTrail(impll, paa.get(i).m_pointArray, pixel);
            }
            nSize=paaEP.size();
            int nNum;
            ArrayList <Point> ppa;
            for(i=0;i<nSize;i++){
                pixel=CommonMethods.randomColor().getRGB();
                ppa=ggos.get(i).getInnerPoints();
                nNum=ppa.size();
                for(j=0;j<nNum;j++){
                    CommonMethods.drawDot(impll, ppa.get(j), pixel);
                }
                CommonMethods.drawTrail(impll, paaEP.get(i).m_pointArray, pixel);
            }
        }
        impll.show();
    }
    void closeStoredImage(Hashtable hTable){
        String key="imageLabel:";
        String label="";
        if(hTable.containsKey(key)) {
            label= (String)hTable.get(key);
            closeStoredImage(label);
        }else{
            IJ.error("There is no imageLabel");
        }
    }
    void closeStoredImage (String label){        
        ImagePlus impl;
        if(m_cStoredImages.containsKey(label))
        {
            impl=(ImagePlus) m_cStoredImages.get(label);
            impl.close();
            m_cStoredImages.remove(label);
        }
        else
            IJ.error("There is no stored image labeled as: "+ label);
    }
    void closeAllStoredImages(){
        ArrayList <String> labels=ArgumentDecoder.getKeysAsStrings(m_cStoredImages);
        int nSize=labels.size();
        for(int i=0;i<nSize;i++){
            closeStoredImage(labels.get(i));
        }
    }
    void closeAllImages(){
        closeAllStoredImages();
        CommonMethods.closeAllImages();
    }
    void hideAllImages(){
        CommonMethods.hideAllImages();
    }
    void clearStorages(){
        closeAllStoredImages();
        m_cStoredImages.clear();
        m_cStoredVariables.clear();
    }
    void breakPoint(){
        CommonMethods.breakPoint();
    }
    void markIPObjects(Hashtable hTable){
        ImagePlus impl=retrieveImage(hTable,"imageLabel:");
        String IPOLabel=retrieveLabel(hTable, "IPOCenters:");
        String ContourLabel=retrieveLabel(hTable, "MIPContours:");
        ArrayList <Point> IPCenters=(ArrayList<Point>)m_cStoredVariables.get(IPOLabel);
        ArrayList <PointArray> MIPContours=(ArrayList<PointArray>)m_cStoredVariables.get(ContourLabel);

        markIPObjects(impl,IPCenters,MIPContours);
    }
    String retrieveLabel(Hashtable hTable, String key){
        return (String) retrieveVariable(hTable,key);
    }
    String retrieveString(Hashtable hTable, String key){
        return (String) retrieveVariable(hTable,key);
    }
    int retrieveInteger(Hashtable hTable, String key){
        String st=retrieveString(hTable,key);
        return Integer.parseInt(st);
    }
    float retrieveFloat(Hashtable hTable, String key){
        String st=retrieveString(hTable,key);
        return Float.parseFloat(st);
    }
    Object retrieveVariable(Hashtable hTable, String key){
        Object Variable="";
        if(hTable.containsKey(key))
            Variable=hTable.get(key);
        else
            IJ.error("can not find the key word "+key+" in the command line "+ hTable.toString());
        return Variable;
    }
    ImagePlus retrieveImage(Hashtable hTable, String key){
        ImagePlus impl=null;
        String label=retrieveLabel(hTable,key);
        if(m_cStoredImages.containsKey(label))
            impl=(ImagePlus)m_cStoredImages.get(label);
        else
            IJ.error("can not find "+label+" in the stored images");
        return impl;
    }
    public static void markIPObjects(ImagePlus impl, ArrayList <Point> points, ArrayList <PointArray> contours){//this methods store pixel values a integers, each slice to one row.
       ImagePlus implc=CommonMethods.copyToRGBImage(impl);
       int w=impl.getWidth();
       int h=impl.getHeight();
       int pixels[][]=new int[h][w];
       CommonMethods.getPixelValue(impl,impl.getCurrentSlice(), pixels);
       implc.show();
       Point p;
       int nSize=points.size();
       int pixel,r,g,b;
       for(int i=0;i<nSize;i++){
           p=points.get(i);
           pixel=CommonMethods.randomColor().getRGB();
           r=0xff&(pixel>>16);
           g=0xff&(pixel>>8);
           b=0xff&pixels[p.y][p.x];
           pixel=(r<<16)|(g<<8)|(b);
           CommonMethods.drawDot(implc, points.get(i), pixel);
           CommonMethods.drawTrail(implc, contours.get(i).m_pointArray, pixel);
       }
    }

    void buildMIPCountours(Hashtable hTable){
        ImagePlus Mip=retrieveImage(hTable, "imageLabel1:");
        ImagePlus ref=retrieveImage(hTable, "imageLabel2:");
        ImagePlus wsh=retrieveImage(hTable,"imageLabel3:");
        ImagePlus refwsh=retrieveImage(hTable,"imageLabel4:");
        ArrayList <Point> IPOCenters=new ArrayList();
        ArrayList <PointArray> MIPContours=new ArrayList();
        ArrayList <Point> IPOCentersR=new ArrayList();
        ArrayList <PointArray> MIPContoursR=new ArrayList();
        buildMIPCountours(Mip,ref,wsh,refwsh, IPOCenters,MIPContours, IPOCentersR,MIPContoursR);
        m_cStoredVariables.put(retrieveLabel(hTable,"storeIPOCetners:"), IPOCenters);
        m_cStoredVariables.put(retrieveLabel(hTable,"storeMIPContours:"), MIPContours);
        m_cStoredVariables.put(retrieveLabel(hTable,"storeIPOCetnersR:"), IPOCentersR);
        m_cStoredVariables.put(retrieveLabel(hTable,"storeMIPContoursR:"), MIPContoursR);
    }

    void buildMIPCountours(ImagePlus impl, ImagePlus implr, ImagePlus implw, ImagePlus implrw, ArrayList <Point> IPOCenters,ArrayList <PointArray> MIPContours, ArrayList <Point> IPOCentersR, ArrayList <PointArray> MIPContoursR){
        int h=impl.getHeight(), w=impl.getWidth();
        int pixelsl[][]=new int[h][w], pixelsr[][]=new int[h][w], pixelsw[][]=new int[h][w], pixelsrw[][]=new int[h][w];
        CommonMethods.getPixelValue(impl,impl.getCurrentSlice(), pixelsl);
        CommonMethods.getPixelValue(implr,implr.getCurrentSlice(), pixelsr);
        CommonMethods.getPixelValue(implw,implw.getCurrentSlice(), pixelsw);
        CommonMethods.getPixelValue(implrw,implrw.getCurrentSlice(), pixelsrw);
        double dRange[]=new double[2];
        int radius=3;
        intRange xRange=new intRange(radius,w-radius);
        intRange yRange=new intRange(radius,h-radius);
//        CommonMethods.getValueRange(w, h, pixelsr,xRange,yRange, dRange);
        ArrayList <Point> localMaximar=new ArrayList();
        CommonMethods.getSpecialLandscapePoints(pixelsr, w, h, LandscapeAnalyzer.localMaximum,localMaximar);
        double percentile=0.8;
        int pMin=CommonMethods.getValueOfPercentile(CommonMethods.getPixelValues(pixelsr, localMaximar), percentile);
//        double dmax=dRange[1];

        intRange ir=new intRange(pMin,256);

        CommonMethods.getSpecialLandscapePoints(pixelsl, w, h, LandscapeAnalyzer.localMaximum,ir,IPOCenters);
        CommonMethods.getSpecialLandscapePoints(pixelsr, w, h, LandscapeAnalyzer.localMaximum,ir,IPOCentersR);
        ir.setRange(0, 0);
        CommonMethods.getContours(pixelsw,w, h, IPOCenters, ir,MIPContours);
        CommonMethods.getContours(pixelsrw,w, h, IPOCentersR, ir,MIPContoursR);
    }

    void monitorIPObjects(Hashtable hTable){
        ImagePlus impl=retrieveImage(hTable, "imageLabel:");
        ImagePlus implm=retrieveImage(hTable,"imageLabelMIP:");
        ImagePlus implw=retrieveImage(hTable,"imageLabelWatershed:");
        ImagePlus implr=retrieveImage(hTable, "imageLabelR:");
        ImagePlus implmr=retrieveImage(hTable,"imageLabelMIPR:");
        ImagePlus implwr=retrieveImage(hTable,"imageLabelWatershedR:");
        ArrayList <PointArray> MIPContours=(ArrayList <PointArray>)retrieveStoredVariable(hTable, "MIPContours:");
        ArrayList <PointArray> MIPContoursR=(ArrayList <PointArray>)retrieveStoredVariable(hTable, "MIPContoursR:");
        IntensityPeakObjectOrganizer IPOHOrganizer=new IntensityPeakObjectOrganizer();
        IPOHOrganizer.setImage(impl);
        IPOHOrganizer.setImageR(implr);
//        IPOHOrganizer.buildMIPIPOs(implw,implm,MIPContours);
//        IPOHOrganizer.exportROITraces();
//        IPOHOrganizer.buildIPOHs();
//        IPOHOrganizer.markIPOTraces();
//        IPOHOrganizer.refineTracks();
//        IPOHOrganizer.rebuildIPOT_ROIs();
//        IPOHOrganizer.markIPOTracks();
//        IPOHOrganizer.exportTrackParameters();
//        IPOHOrganizer.markIPOs();
        String path=(String) hTable.get("path_IPOPeakIntensity:");
        Formatter fm=CommonMethods.QuickFormatter(path);
//        IPOHOrganizer.exportIPOTraces(fm);
//        IPOHOrganizer.drawIPOTraces();
        fm.close();
    }
    Object retrieveStoredVariable(Hashtable hTable, String key){
        String label;
        Object variable=null;
        if(hTable.containsKey(key)){
            label=(String)hTable.get(key);
            if(m_cStoredVariables.containsKey(label)){
                variable=m_cStoredVariables.get(label);
            }else{
                IJ.error("no stored variable. label: "+label);
            }
        }else{
            IJ.error("no keyword "+key+" is in the command line. "+ hTable.toString());
        }
        return variable;
    }
    void gaussianBlur(Hashtable hTable){
        String sigma= (String) retrieveVariable(hTable,"sigma:");
        float fSigma=Float.valueOf(sigma);
        String accuracy= (String) retrieveVariable(hTable,"accuracy:");
        float fAccuracy=Float.valueOf(accuracy);
        GaussianBlur gb=new GaussianBlur();
        ImagePlus impl=WindowManager.getCurrentImage();
        int num,i;
        num=impl.getStackSize();
        for(i=0;i<num;i++){
            impl.setSlice(i+1);
            gb.blurGaussian(impl.getProcessor(), fSigma, fSigma, fAccuracy);
        }
    }
    void activateStoredImage(Hashtable hTable){
        String label=retrieveLabel(hTable, "imageLabel:");
        ImagePlus impl=retrieveStoredImage(label);
        impl.show();
    }
    void exportPixelHeightIPO(Hashtable hTable){
        String path=(String) hTable.get("path:");
        if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
        ImagePlus impl=CommonMethods.importImage(path);
        impl.show();
        previousImage=currentImage;
        currentImage=impl;
        float xSigma=1,ySigma=1,fAccuracy=0.01f;
        String imageTitle=impl.getTitle();
        int len=imageTitle.length();
        imageTitle=imageTitle.substring(0, len-4);

        ImagePlus implp=CommonMethods.cloneImage(impl);
        implp.setTitle(imageTitle+" -processed image");
        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);

        int w=impl.getWidth();

        ImagePlus implr;
        if(w<250)
            implr=CommonMethods.resizedImage_cropping(impl, 250, 250);
        else
            implr=CommonMethods.cloneImage(impl);

        CommonMethods.randomizeImage(implr);
        implr.setTitle(imageTitle+" -reference image");

        ImagePlus implrp=CommonMethods.cloneImage(implr);
        implrp.setTitle(imageTitle+" -processed reference image");
        CommonMethods.GaussianBlur(implrp, xSigma, ySigma, fAccuracy);

        impl.show();
        implp.show();
        implr.show();
        implrp.show();
        String pathp=m_sHomeDir+implp.getTitle()+".tif";
        CommonMethods.saveImage(implp,pathp);
        String pathr=m_sHomeDir+implr.getTitle()+".tif";
        CommonMethods.saveImage(implr,pathr);
        String pathrp=m_sHomeDir+implrp.getTitle()+".tif";
        CommonMethods.saveImage(implrp,pathrp);

        path=FileAssist.changeExt(path, "phf");
        pathr=FileAssist.changeExt(pathr, "phf");
        ImagePlus implc=CommonMethods.cloneImage(impl);
        implc.setTitle(impl.getTitle()+" compensated pixels for the original image");
        ImagePlus implcp=CommonMethods.cloneImage(impl);
        implcp.setTitle(impl.getTitle()+" compensated pixels for the processed image");
        ImagePlus implcpr=CommonMethods.cloneImage(impl);
        implcpr.setTitle(impl.getTitle()+" compensated pixels for the processed reference image");
        ImagePlus implcr=CommonMethods.cloneImage(impl);
        implcr.setTitle(impl.getTitle()+" compensated pixels for the reference image");
        pathrp=FileAssist.changeExt(pathrp, "phf");
        pathp=FileAssist.changeExt(pathp, "phf");

        IPOPixelHeightsHandler.exportStackPixelHeights(impl, implp, implc, implcp, path, pathp);
        IPOPixelHeightsHandler.exportStackPixelHeights(implr, implrp, implcr, implcpr,pathr, pathrp);
//        IPOPixelHeightsHandler.exportStackPixelHeights(implp,implcp, pathp);
//        IPOPixelHeightsHandler.exportStackPixelHeights(implrp, implcpr, pathrp);

        path=m_sHomeDir+implc.getTitle()+".tif";
        CommonMethods.saveImage(implc, path);
        path=m_sHomeDir+implcp.getTitle()+".tif";
        CommonMethods.saveImage(implcp, path);
        path=m_sHomeDir+implcpr.getTitle()+".tif";
        CommonMethods.saveImage(implcpr, path);
        path=m_sHomeDir+implcr.getTitle()+".tif";
        CommonMethods.saveImage(implcr, path);

        implc.show();
        implcp.show();
        implcpr.show();
        implcr.show();
    }
    void exportPixelHeightIPO_Subpixel(Hashtable hTable){
        String path=(String) hTable.get("path:");
        if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
        ImagePlus impl=CommonMethods.importImage(path);
        impl.show();
        previousImage=currentImage;
        currentImage=impl;
//        float xSigma=1,ySigma=1,fAccuracy=0.01f;
        float xSigma=1f,ySigma=1f,fAccuracy=0.01f;//2/21/2011
        String imageTitle=impl.getTitle();
        int len=imageTitle.length();
        imageTitle=imageTitle.substring(0, len-4);

        ImagePlus implp=CommonMethods.cloneImage(impl);
        implp.setTitle(imageTitle+" -processed image");
        CommonMethods.GaussianBlur(implp, xSigma, ySigma, fAccuracy);

        int w=impl.getWidth(),h=impl.getHeight();

        ImagePlus implr;
        if(w<250)
            implr=CommonMethods.resizedImage_cropping(impl, 250, 250);
        else
            implr=CommonMethods.cloneImage(impl);

        CommonMethods.randomizeImage(implr);
        implr.setTitle(imageTitle+" -reference image");

        ImagePlus implrp=CommonMethods.cloneImage(implr);
        implrp.setTitle(imageTitle+" -processed reference image");
        CommonMethods.GaussianBlur(implrp, xSigma, ySigma, fAccuracy);

//        ImagePlus implt=CommonMethods.getBlankImage(impl.getType(), w, h);


        impl.show();
        implp.show();
        implr.show();
        implrp.show();
//        implt.show();
        String pathp=m_sHomeDir+implp.getTitle()+".tif";
        CommonMethods.saveImage(implp,pathp);
        String pathr=m_sHomeDir+implr.getTitle()+".tif";
        CommonMethods.saveImage(implr,pathr);
        String pathrp=m_sHomeDir+implrp.getTitle()+".tif";
        CommonMethods.saveImage(implrp,pathrp);

        path=FileAssist.changeExt(path, "phf");
        pathr=FileAssist.changeExt(pathr, "phf");
        ImagePlus implc=CommonMethods.cloneImage(impl);
        implc.setTitle(impl.getTitle()+" compensated pixels for the original image");
        ImagePlus implcp=CommonMethods.cloneImage(impl);
        implcp.setTitle(impl.getTitle()+" compensated pixels for the processed image");
        ImagePlus implcpr=CommonMethods.cloneImage(impl);
        implcpr.setTitle(impl.getTitle()+" compensated pixels for the processed reference image");
        ImagePlus implcr=CommonMethods.cloneImage(impl);
        implcr.setTitle(impl.getTitle()+" compensated pixels for the reference image");
        pathrp=FileAssist.changeExt(pathrp, "phf");
        pathp=FileAssist.changeExt(pathp, "phf");

//        IPOPixelHeightsHandler.exportStackPixelHeights_Subpixel(impl, implp, implc, implcp, implt, path, pathp);
        IPOPixelHeightsHandler.exportStackPixelHeights_Subpixel(impl, implp, implc, implcp, path, pathp);
        IPOPixelHeightsHandler.exportStackPixelHeights_Subpixel(implr, implrp, implcr, implcpr, pathr, pathrp);
//        IPOPixelHeightsHandler.exportStackPixelHeights(implp,implcp, pathp);
//        IPOPixelHeightsHandler.exportStackPixelHeights(implrp, implcpr, pathrp);

        path=m_sHomeDir+implc.getTitle()+".tif";
        CommonMethods.saveImage(implc, path);
        path=m_sHomeDir+implcp.getTitle()+".tif";
        CommonMethods.saveImage(implcp, path);
        path=m_sHomeDir+implcpr.getTitle()+".tif";
        CommonMethods.saveImage(implcpr, path);
        path=m_sHomeDir+implcr.getTitle()+".tif";
        CommonMethods.saveImage(implcr, path);

        implc.show();
        implcp.show();
        implcpr.show();
        implcr.show();
    }
    void MonitorIPO_PrecomputedGaussianNodeGroups(Hashtable hTable){
        String path=(String) hTable.get("path:");
        if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
        ImagePlus impl=CommonMethods.importImage(path);
        impl.show();
        previousImage=currentImage;
        currentImage=impl;

//        String pathp=FileAssist.getExtendedFileName(path, " -processed image");
        ImagePlus implp=CommonMethods.cloneImage(impl);
        CommonMethods.GaussianBlur(implp, 1, 1, 0.01f);

        String st=(String) retrieveVariable(hTable,"MinimalLength:");
        int nMinLen=Integer.valueOf(st);
        st=(String) retrieveVariable(hTable,"LatestTrackHeadDelay:");
        int nMaxTrackHeadLatency=Integer.valueOf(st);//delay of the track head from nFirstSlice.

        int nTrackExportMode=retrieveInteger(hTable, "TrackExportMode:");
        path=FileAssist.changeExt(path, "IGN");

        IntensityPeakObjectOrganizer IPOHOrganizer=new IntensityPeakObjectOrganizer();
        IPOHOrganizer.setTrackExportMode(nTrackExportMode);
        IPOHOrganizer.setImage(impl);
        IPOHOrganizer.setImageP(implp);
//        IPOHOrganizer.setImageCompensated(implC);

        IJ.showStatus("buildingIPOs");
        IPOHOrganizer.buildIPOHs_PrecomputedGaussianNodeGroup(path);
        IJ.showStatus("calImageStatistics");
        IPOHOrganizer.calImageStatistics_GaussianNodeGroup();
        IJ.showStatus("assignIPOShapes");
        IPOHOrganizer.assignIPOShapes();
         Runtime.getRuntime().gc();
         IJ.showStatus("buildingIPOTacks");
//        int nFirstSlice=IPOHOrganizer.getFirstHandledSlice();
        int nFirstSlice=CommonMethods.getIntesityPeakSliceIndex(impl)+1;

        IPOHOrganizer.buildIPOTracks_IPOTracker(nFirstSlice);
        IPOHOrganizer.setTrackExportMode();

        IPOHOrganizer.buildIPOTBundles();
        IJ.showStatus("buildingIPOBundles");

        path=FileAssist.changeExt(path, "TRK");
        StackIPOGTracksNode StackIPOTs=new StackIPOGTracksNode(IPOHOrganizer.getIPOTracks());
        StackIPOTs.exportIPOGTracks(path);

        boolean bDisplayAllTracks=false;
//        IPOHOrganizer.setTrackDisplayability(bDisplayAllTracks,nMinLen,nMaxTrackHeadLatency);

//        String pathAbf=FileAssist.getExtendedFileName(path, " -pixel heights-IPOGaussianNode");
//        pathAbf=FileAssist.changeExt(pathAbf, "dat");
//        IPOHOrganizer.exportTrackToAbf(pathAbf);
//        IPOHOrganizer.exportImageStatiticsToAbf(pathAbf);

//        ImagePlus impl_bundles=IPOHOrganizer.markIPOTs_Bundle();
//        impl_bundles.setTitle(impl.getTitle()+" marked IPOTBundles");
//        String pathb=FileAssist.getExtendedFileName(path, " -IPOTBundles-bkgo");
//       pathb=FileAssist.changeExt(pathb, "tif");
//        CommonMethods.saveImage(impl_bundles, pathb);
//        impl_bundles.close();


        bDisplayAllTracks=true;
 //       IPOHOrganizer.setTrackDisplayability(bDisplayAllTracks,nMinLen,nMaxTrackHeadLatency);

//        pathAbf=FileAssist.getExtendedFileName(pathAbf, " -all tracks");
//        IPOHOrganizer.exportTrackToAbf(pathAbf);

//        impl_bundles=IPOHOrganizer.markIPOTs_Bundle();
//        impl_bundles.setTitle(impl.getTitle()+" all tracks");
//        pathb=FileAssist.getExtendedFileName(pathb, " -all tracks");
//        CommonMethods.saveImage(impl_bundles, pathb);
//        impl_bundles.close();

        Runtime.getRuntime().gc();

//        ImagePlus impl_IPOs=IPOHOrganizer.markIPOs();
//        String pathIPO=FileAssist.getExtendedFileName(path, " -IPOs-IPOGaussianNode");
//        pathIPO=FileAssist.changeExt(pathIPO, "tif");
//        impl_IPOs.setTitle(impl.getTitle()+" markedIPOs");
//        CommonMethods.saveImage(impl_IPOs, pathIPO);
        CommonMethods.closeAllImages();
        Runtime.getRuntime().gc();
 //       IPOHOrganizer.exportIPOTBundlesToAbf(pathAbf);
//        IPOHOrganizer.drawIPOTraces();// need to make it work for the case of tracks.
//        fm.close();
    }
    void MonitorIPO_PrecomputedGaussianNodes(Hashtable hTable){
        String path=(String) hTable.get("path:");
        if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
        ImagePlus impl=CommonMethods.importImage(path);
        impl.show();
        previousImage=currentImage;
        currentImage=impl;

        String st=(String) retrieveVariable(hTable,"MinimalLength:");
        int nMinLen=Integer.valueOf(st);
        st=(String) retrieveVariable(hTable,"LatestTrackHeadDelay:");
        int nMaxTrackHeadLatency=Integer.valueOf(st);//delay of the track head from nFirstSlice.

        int nTrackExportMode=retrieveInteger(hTable, "TrackExportMode:");
        path=FileAssist.changeExt(path, "IGN");

        IntensityPeakObjectOrganizer IPOHOrganizer=new IntensityPeakObjectOrganizer();
        IPOHOrganizer.setTrackExportMode(nTrackExportMode);
        IPOHOrganizer.setImage(impl);
//        IPOHOrganizer.setImageP(implp);
//        IPOHOrganizer.setImageCompensated(implC);

        IJ.showStatus("buildingIPOs");
        IPOHOrganizer.buildIPOHs_PrecomputedGaussianNode(path);
        IJ.showStatus("calImageStatistics");
        IPOHOrganizer.calImageStatistics_GaussianNode();
         Runtime.getRuntime().gc();
         IJ.showStatus("buildingIPOTacks");
        int nFirstSlice=IPOHOrganizer.getFirstHandledSlice();
        IPOHOrganizer.buildIPOTracks_IPOTracker(nFirstSlice);
        IPOHOrganizer.setTrackExportMode();

        IPOHOrganizer.buildIPOTBundles();
        IJ.showStatus("buildingIPOBundles");

        path=FileAssist.changeExt(path, "TRK");
        StackIPOGTracksNode StackIPOTs=new StackIPOGTracksNode(IPOHOrganizer.getIPOTracks());
        StackIPOTs.exportIPOGTracks(path);

        boolean bDisplayAllTracks=false;
        IPOHOrganizer.setTrackDisplayability(bDisplayAllTracks,nMinLen,nMaxTrackHeadLatency);

        String pathAbf=FileAssist.getExtendedFileName(path, " -pixel heights-IPOGaussianNode");
        pathAbf=FileAssist.changeExt(pathAbf, "dat");
        IPOHOrganizer.exportTrackToAbf(pathAbf);
        IPOHOrganizer.exportImageStatiticsToAbf(pathAbf);

        ImagePlus impl_bundles=IPOHOrganizer.markIPOTs_Bundle();
        impl_bundles.setTitle(impl.getTitle()+" marked IPOTBundles");
        String pathb=FileAssist.getExtendedFileName(path, " -IPOTBundles-bkgo");
        pathb=FileAssist.changeExt(pathb, "tif");
        CommonMethods.saveImage(impl_bundles, pathb);
        impl_bundles.close();


        bDisplayAllTracks=true;
        IPOHOrganizer.setTrackDisplayability(bDisplayAllTracks,nMinLen,nMaxTrackHeadLatency);

        pathAbf=FileAssist.getExtendedFileName(pathAbf, " -all tracks");
        IPOHOrganizer.exportTrackToAbf(pathAbf);

        impl_bundles=IPOHOrganizer.markIPOTs_Bundle();
        impl_bundles.setTitle(impl.getTitle()+" all tracks");
        pathb=FileAssist.getExtendedFileName(pathb, " -all tracks");
        CommonMethods.saveImage(impl_bundles, pathb);
        impl_bundles.close();

        Runtime.getRuntime().gc();

        ImagePlus impl_IPOs=IPOHOrganizer.markIPOs();
        String pathIPO=FileAssist.getExtendedFileName(path, " -IPOs-IPOGaussianNode");
        pathIPO=FileAssist.changeExt(pathIPO, "tif");
        impl_IPOs.setTitle(impl.getTitle()+" markedIPOs");
        CommonMethods.saveImage(impl_IPOs, pathIPO);
        CommonMethods.closeAllImages();
        Runtime.getRuntime().gc();
        IPOHOrganizer.exportIPOTBundlesToAbf(pathAbf);
//        IPOHOrganizer.drawIPOTraces();// need to make it work for the case of tracks.
//        fm.close();
    }
    void MonitorIPO_PrecomputedPixelHeights(Hashtable hTable){
        String path=(String) hTable.get("path:");
        if(!CommonMethods.absolutePath(path))path=m_sHomeDir+path;
        ImagePlus impl=CommonMethods.importImage(path);
        String pathp=FileAssist.getExtendedFileName(path, " -processed image");
        ImagePlus implp=CommonMethods.importImage(pathp);
//        implp.show();
        impl.show();
        String pathC=FileAssist.getExtendedFileName(path," compensated pixels for the original image");
        ImagePlus implC=CommonMethods.importImage(pathC);
//        implC.show();
        previousImage=currentImage;
        currentImage=impl;

        int nFirstSlice=CommonMethods.getIntesityPeakSliceIndex(impl)+1;
        String sMode=(String) retrieveVariable(hTable,"detectionMode");
        int nDetectionMode=Integer.valueOf(sMode);
        sMode=(String) retrieveVariable(hTable,"backgroundOption");
        int nBackgroundOption=Integer.valueOf(sMode);
        int cutoffSmoothingWS=retrieveInteger(hTable,"cutoffSmoothingWS:");
        String st=(String) retrieveVariable(hTable,"MinimalLength:");
        int nMinLen=Integer.valueOf(st);
        st=(String) retrieveVariable(hTable,"LatestTrackHeadDelay:");
        int nMaxTrackHeadLatency=Integer.valueOf(st);//delay of the track head from nFirstSlice.

        int nTrackExportMode=retrieveInteger(hTable, "TrackExportMode:");
        path=FileAssist.changeExt(path, "phf");
        pathp=FileAssist.changeExt(pathp, "phf");
        String pathrp=FileAssist.getExtendedFileName(path, " -processed reference image");
        String pathr=FileAssist.getExtendedFileName(path, " -reference image");


        IntensityPeakObjectOrganizer IPOHOrganizer=new IntensityPeakObjectOrganizer();
        IPOHOrganizer.setTrackExportMode(nTrackExportMode);
        IPOHOrganizer.setImage(impl);
        IPOHOrganizer.setImageP(implp);
        IPOHOrganizer.setImageCompensated(implC);

        IJ.showStatus("buildingIPOs");
        IPOHOrganizer.buildIPOHs_PrecomputedPixelHeights(path,pathp,pathr,pathrp,nDetectionMode,nBackgroundOption,cutoffSmoothingWS,nFirstSlice-1);
        IJ.showStatus("calImageStatistics");
        IPOHOrganizer.calImageStatistics();
         Runtime.getRuntime().gc();
         IJ.showStatus("buildingIPOTacks");
        IPOHOrganizer.buildIPOTracks_IPOTracker(nFirstSlice);
        IPOHOrganizer.setTrackExportMode();

        IPOHOrganizer.buildIPOTBundles();
        IJ.showStatus("buildingIPOBundles");

        boolean bDisplayAllTracks=false;
        IPOHOrganizer.setTrackDisplayability(bDisplayAllTracks,nMinLen,nMaxTrackHeadLatency);

        String pathAbf=FileAssist.getExtendedFileName(path, " -pixel heights-bkgo"+nBackgroundOption);
        pathAbf=FileAssist.changeExt(pathAbf, "dat");
        IPOHOrganizer.exportTrackToAbf(pathAbf);
        IPOHOrganizer.exportImageStatiticsToAbf(pathAbf);

        ImagePlus impl_bundles=IPOHOrganizer.markIPOTs_Bundle();
        impl_bundles.setTitle(impl.getTitle()+" marked IPOTBundles");
        String pathb=FileAssist.getExtendedFileName(path, " -IPOTBundles-bkgo"+nBackgroundOption);
        pathb=FileAssist.changeExt(pathb, "tif");
        CommonMethods.saveImage(impl_bundles, pathb);
        impl_bundles.close();


        bDisplayAllTracks=true;
        IPOHOrganizer.setTrackDisplayability(bDisplayAllTracks,nMinLen,nMaxTrackHeadLatency);

        pathAbf=FileAssist.getExtendedFileName(pathAbf, " -all tracks");
        IPOHOrganizer.exportTrackToAbf(pathAbf);

        impl_bundles=IPOHOrganizer.markIPOTs_Bundle();
        impl_bundles.setTitle(impl.getTitle()+" all tracks");
        pathb=FileAssist.getExtendedFileName(pathb, " -all tracks");
        CommonMethods.saveImage(impl_bundles, pathb);
        impl_bundles.close();
        
        Runtime.getRuntime().gc();

        ImagePlus impl_IPOs=IPOHOrganizer.markIPOs();
        String pathIPO=FileAssist.getExtendedFileName(path, " -IPOs-bkgo"+nBackgroundOption);
        pathIPO=FileAssist.changeExt(pathIPO, "tif");
        impl_IPOs.setTitle(impl.getTitle()+" markedIPOs");
        CommonMethods.saveImage(impl_IPOs, pathIPO);
        CommonMethods.closeAllImages();
        Runtime.getRuntime().gc();
        IPOHOrganizer.exportIPOTBundlesToAbf(pathAbf);
//        IPOHOrganizer.drawIPOTraces();// need to make it work for the case of tracks.
//        fm.close();
    }
    void export_PrecomputedPixelHeights(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        String pathr=retrievePath(hTable,"pathr:");
        String sFrameNumber=(String)retrieveVariable(hTable,"frameNumber");
        String sMode=(String) retrieveVariable(hTable,"detectionMode");
        int nFrameNumber=Integer.valueOf(sFrameNumber);
        int nDetectionMode=Integer.valueOf(sMode);


        ArrayList<Point> cvLocalMaxima=new ArrayList();
        ArrayList<Double> dvPixelHeights=new ArrayList();
        ArrayList<Double> dvPixelHeights0=new ArrayList();
        ArrayList<Double> dvPixelHeightsC=new ArrayList();
        ArrayList<Point> cvLocalMaximar=new ArrayList();
        ArrayList<Double> dvPixelHeightsr=new ArrayList();
        ArrayList<Double> dvPixelHeights0r=new ArrayList();
        ArrayList<Double> dvPixelHeightsCr=new ArrayList();
        IPOPixelHeightsHandler.importStackPixelHeights(path, nFrameNumber, cvLocalMaxima, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC);
        IPOPixelHeightsHandler.importStackPixelHeights(pathr, nFrameNumber, cvLocalMaximar, dvPixelHeightsr, dvPixelHeights0r, dvPixelHeightsCr);
        ArrayList<Double>[] pdvPixelHeights=new ArrayList[3];
        pdvPixelHeights[0]=dvPixelHeights0;
        pdvPixelHeights[1]=dvPixelHeights;
        pdvPixelHeights[2]=dvPixelHeightsC;

        ArrayList<Double>[] pdvPixelHeightsr=new ArrayList[3];
        pdvPixelHeightsr[0]=dvPixelHeights0r;
        pdvPixelHeightsr[1]=dvPixelHeightsr;
        pdvPixelHeightsr[2]=dvPixelHeightsCr;
        MeanSem0[] MSs=new MeanSem0[3];

        int len=MSs.length;
        for(int i=0;i<len;i++){
            MSs[i]=CommonStatisticsMethods.buildMeanSem(pdvPixelHeightsr[i], 0, pdvPixelHeightsr[i].size()-1, 1);
        }

        ArrayList<ImageShape> cvRings=new ArrayList();
        ArrayList<Double> dvRs=new ArrayList();
        IPOPixelHeightsHandler.importRings(path, cvRings, dvRs);

        ArrayList<MeanSem0[]> cvRingMeansems=new ArrayList();
        ArrayList<MeanSem0[]> cvRingMeansems_Group=new ArrayList();
        ArrayList<MeanSem0[]> cvRingMeansems_Normalized=new ArrayList();
        path=FileAssist.changeExt(path, "phi");
        len=cvLocalMaxima.size();
        IPOPixelHeightsHandler.importRingMeansems(path, nFrameNumber, cvLocalMaxima, cvRingMeansems, cvRingMeansems_Group, cvRingMeansems_Normalized);
        CommonMethods.sortArrays(cvRingMeansems,cvLocalMaxima, pdvPixelHeights, 0, len-1,nDetectionMode);

        path=FileAssist.changeExt(path, "txt");
        String path1=FileAssist.getExtendedFileName(path, "-pixelHeiths frame-"+PrintAssist.ToString(nFrameNumber));
        String path2=FileAssist.getExtendedFileName(path, "-Grouped ringPixels frame-"+PrintAssist.ToString(nFrameNumber));
        IPOPixelHeightsHandler.exportPixelHeights(path1, cvLocalMaxima, pdvPixelHeights, cvRingMeansems, cvRings, dvRs,MSs,nDetectionMode);
        IPOPixelHeightsHandler.exportRingMeanSem(path2, cvRingMeansems_Group, cvRingMeansems_Normalized, cvRings, dvRs);
    }


    void export_PrecomputedPixelHeights_Subpixel(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        String pathr=retrievePath(hTable,"pathr:");
        String sFrameNumber=(String)retrieveVariable(hTable,"frameNumber");
        String sMode=(String) retrieveVariable(hTable,"detectionMode");
        int nFrameNumber=Integer.valueOf(sFrameNumber);
        int nDetectionMode=Integer.valueOf(sMode);

        ArrayList<double[]> IPOCenters=new ArrayList();
        ArrayList<int[]> DistsToBorders=new ArrayList();
        ArrayList<int[]> DistsToBordersr=new ArrayList();
        ArrayList<Double> dvPixelHeights=new ArrayList();
        ArrayList<Double> dvPixelHeights0=new ArrayList();
        ArrayList<Double> dvPixelHeightsC=new ArrayList();
        ArrayList<double[]> IPOCentersr=new ArrayList();
        ArrayList<Double> dvPixelHeightsr=new ArrayList();
        ArrayList<Double> dvPixelHeights0r=new ArrayList();
        ArrayList<Double> dvPixelHeightsCr=new ArrayList();
        ArrayList<Integer> nvOverlappingSatelliteIndexes=new ArrayList();
        ArrayList<Integer> nvSatelliteLMIndexes=new ArrayList();
        ArrayList<Integer> nvOverlappingSatelliteIndexesr=new ArrayList();
        ArrayList<Integer> nvSatelliteLMIndexesr=new ArrayList();
        ArrayList<Double> vdGroupingDelimiters=new ArrayList();
        ArrayList<Double> vdGroupingDelimitersr=new ArrayList();
        ArrayList<Double> dvRs=new ArrayList();
        path=FileAssist.changeExt(path, "phf");
        pathr=FileAssist.changeExt(pathr, "phf");
        IPOPixelHeightsHandler.importStackPixelHeights_Subpixel(path, nFrameNumber, dvRs,IPOCenters,DistsToBorders, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC,nvOverlappingSatelliteIndexes,nvSatelliteLMIndexes,vdGroupingDelimiters);
        IPOPixelHeightsHandler.importStackPixelHeights_Subpixel(pathr, nFrameNumber, dvRs,IPOCentersr,DistsToBordersr, dvPixelHeightsr, dvPixelHeights0r, dvPixelHeightsCr,nvOverlappingSatelliteIndexesr,nvSatelliteLMIndexesr,vdGroupingDelimitersr);
        ArrayList<Double>[] pdvPixelHeights=new ArrayList[3];
        pdvPixelHeights[0]=dvPixelHeights0;
        pdvPixelHeights[1]=dvPixelHeights;
        pdvPixelHeights[2]=dvPixelHeightsC;

        ArrayList<Double>[] pdvPixelHeightsr=new ArrayList[3];
        pdvPixelHeightsr[0]=dvPixelHeights0r;
        pdvPixelHeightsr[1]=dvPixelHeightsr;
        pdvPixelHeightsr[2]=dvPixelHeightsCr;
        MeanSem0[] MSs=new MeanSem0[3];

        int len=MSs.length;
        for(int i=0;i<len;i++){
            MSs[i]=CommonStatisticsMethods.buildMeanSem(pdvPixelHeightsr[i], 0, pdvPixelHeightsr[i].size()-1, 1);
        }


        ArrayList<MeanSemFractional0[]> cvRingMeansems=new ArrayList();
        ArrayList<MeanSemFractional0[]> cvRingMeansems_Group=new ArrayList();
        ArrayList<MeanSemFractional0[]> cvRingMeansems_Normalized=new ArrayList();
        ArrayList<MeanSemFractional0[]> cvRingMeansems_background=new ArrayList();
        ArrayList<MeanSemFractional0[]> cvRingMeansems_Group_background=new ArrayList();
        ArrayList<MeanSemFractional0[]> cvRingMeansems_Normalized_background=new ArrayList();
        path=FileAssist.changeExt(path, "phi");
        len=IPOCenters.size();
        IPOPixelHeightsHandler.importRingMeansems_Subpixel(path, nFrameNumber, IPOCenters, DistsToBorders,cvRingMeansems, cvRingMeansems_Group, cvRingMeansems_Normalized,
                cvRingMeansems_background, cvRingMeansems_Group_background, cvRingMeansems_Normalized_background,
                nvOverlappingSatelliteIndexes,nvSatelliteLMIndexes,vdGroupingDelimiters);

        int[] pnIndexes=new int[len];
//        CommonMethods.sortArrays(dvPixelHeights0, dvPixelHeights, pnIndexes,nDetectionMode);

        path=FileAssist.changeExt(path, "txt");
        String path1=FileAssist.getExtendedFileName(path, "-pixelHeiths frame-"+PrintAssist.ToString(nFrameNumber));
        String path2=FileAssist.getExtendedFileName(path, "-Grouped ringPixels frame-"+PrintAssist.ToString(nFrameNumber));
        IPOPixelHeightsHandler.exportPixelHeights_Subpixel(path1, IPOCenters, DistsToBorders,pdvPixelHeights, cvRingMeansems, cvRingMeansems_background,dvRs,MSs,cvRingMeansems_Group.size(),
                nDetectionMode,vdGroupingDelimiters,nvOverlappingSatelliteIndexes,nvSatelliteLMIndexes);
        IPOPixelHeightsHandler.exportRingMeanSem_Subpixel(path2, cvRingMeansems_Group, cvRingMeansems_Normalized, dvRs,vdGroupingDelimiters);
    }
    void testPixelHeightsNormality(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        String sFrameNumber=(String)retrieveVariable(hTable,"frameNumber");
        int nFrameNumber=Integer.valueOf(sFrameNumber);
        ArrayList<Point> cvLocalMaxima=new ArrayList();
        ArrayList< Double> dvPixelHeights=new ArrayList();
        ArrayList<Double> dvPixelHeights0=new ArrayList();
        ArrayList<Double> dvPixelHeightsC=new ArrayList();
        IPOPixelHeightsHandler.importStackPixelHeights(path, nFrameNumber, cvLocalMaxima, dvPixelHeights, dvPixelHeights0, dvPixelHeightsC);
        ArrayList<ImageShape> cvRings=new ArrayList();
        ArrayList<Double> dvRs=new ArrayList();
        IPOPixelHeightsHandler.importRings(path, cvRings, dvRs);
        double[] pd=CommonStatisticsMethods.getDoubleArray(dvPixelHeights);
        JarqueBeraTest jbt=new JarqueBeraTest("",pd);
        double pV=jbt.getPValue();
        path=FileAssist.changeExt(path, "txt");        
        String path1=FileAssist.getExtendedFileName(path, "-pixelHeiths normality test frame-"+PrintAssist.ToString(nFrameNumber));
        Formatter fm=CommonMethods.QuickFormatter(path1);
        PrintAssist.printString(fm, "the pValue for the normality test:", 100);
        PrintAssist.endLine(fm);
        PrintAssist.printNumber(fm, pV, 12, 6);
        fm.close();
    }
    void DetectIPOGTLevels_FeatureBased(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        AnalysisMasterForm form=GeneralManager.getMasterForm();
        IPOAnalyzerForm analyzer=form.getIPOAnalyzer();
        IPOAnalyzerForm.setInteractive(false);        
        analyzer.detectTransitions_Batch(path,true,true);
        analyzer.exportTracks();
        path=FileAssist.getExtendedFileName(path, "_LevelInfo_Auto");
        path=FileAssist.changeExt(path, "txt");
        analyzer.exportLevelInfo(path);
        analyzer.removeAllTrackWindows();
//        PlotHandlerGui.closeAllPlotWindowPlus();
        analyzer.closeAll();
        long m=CommonMethods.freeMemory();
        CommonMethods.CollectGargages();
        long m1=CommonMethods.freeMemory();
    }
    void TLG_TLH_Conversion(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        AnalysisMasterForm form=GeneralManager.getMasterForm();
        IPOAnalyzerForm analyzer=form.getIPOAnalyzer();
        IPOAnalyzerForm.setInteractive(false);
        analyzer.TLG_TLH_Conversion(path,true);
        analyzer.exportTracks();
    }
    void AnalyzeIPOGTTransitionStatistics(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        String sQuantityName=(String)retrieveVariable(hTable,"QuantityName:");
        IPOAnalyzerForm.analyzeIPOGTTransitionStatistics(path,sQuantityName);
    }
    void DetectIPOGTLevels_FeatureBasedRefit(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        AnalysisMasterForm form=GeneralManager.getMasterForm();
        IPOAnalyzerForm analyzer=form.getIPOAnalyzer();
        IPOAnalyzerForm.setInteractive(false);        
        analyzer.detectTransitions_Batch(path,true,true);
        analyzer.exportTracks();
        path=FileAssist.getExtendedFileName(path, "_LevelInfo_Auto");
        path=FileAssist.changeExt(path, "txt");
        analyzer.exportLevelInfo(path);
        analyzer.removeAllTrackWindows();
//        PlotHandlerGui.closeAllPlotWindowPlus();
        analyzer.closeAll();
        long m=CommonMethods.freeMemory();
        CommonMethods.CollectGargages();
        long m1=CommonMethods.freeMemory();
    }
    void storeFormatter(Hashtable hTable){
        String path=retrieveString(hTable,"FormatterPath:");
        String ShapePath=FileAssist.getExtendedFileName(path, "_IPOGShape");
        String ContourPath=FileAssist.getExtendedFileName(path, "_IPOGContour");
        Formatter fmShape=CommonMethods.QuickFormatter(ShapePath);
        m_cStoredVariables.put("ShapeFormatter:", fmShape);
        Formatter fmContour=CommonMethods.QuickFormatter(ContourPath);
        m_cStoredVariables.put("ContourFormatter:", fmContour);
        
        IPOGaussianNodeHandler.exportIPOGShapeHeader(fmShape);
        IPOGaussianNodeHandler.exportIPOGContourHeader(fmContour);        
    }
    void buildARFF(Hashtable hTable){
        String path=retrievePath(hTable,"path:");
        AnalysisMasterForm form=GeneralManager.getMasterForm();
        IPOAnalyzerForm analyzer=form.getIPOAnalyzer();
        IPOAnalyzerForm.setInteractive(false);  
        String sQuantityName=(String)retrieveVariable(hTable,"QuantityName:");
        Formatter Shapefm=(Formatter)retrieveVariable(m_cStoredVariables,"ShapeFormatter:"),Contourfm=(Formatter)retrieveVariable(m_cStoredVariables,"ContourFormatter:");
        analyzer.exportAFRR(path,Shapefm,Contourfm,sQuantityName);
    }
    void closeFormatter(){
        Formatter fm=(Formatter)retrieveVariable(m_cStoredVariables,"ShapeFormatter:");
        fm.close();
        fm=(Formatter)retrieveVariable(m_cStoredVariables,"ContourFormatter:");
        fm.close();
    }
}
