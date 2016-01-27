/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package utilities.MachineLearning;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.FastVector;
import weka.core.Attribute;
import java.lang.Exception;
import java.util.ArrayList;
import weka.core.converters.ConverterUtils.DataSource;
import weka.core.Attribute;
import utilities.CommonStatisticsMethods;

/**
 *
 * @author Taihao
 */
public class WekaDriver {
    static public void test(){
        RandomForest rf=new RandomForest();
        
        Attribute cTemperature = new Attribute("Temperature");
        
        FastVector places = new FastVector();
        places.addElement("home");
        places.addElement("outSide");     
        
        FastVector skyOutlook = new FastVector();
        skyOutlook.addElement("sunny");
        skyOutlook.addElement("cloudy");
        skyOutlook.addElement("ranning");
        
        Attribute cPlaceToBe = new Attribute("placeToBe", places);        
        Attribute cSkyOutlook = new Attribute("skyOutlook", skyOutlook);       
        
        FastVector fv=new FastVector();
        fv.addElement(cTemperature);
        fv.addElement(cSkyOutlook);
        fv.addElement(cPlaceToBe);
        
        int num=100,i,index;
        Instances cTrainingSet=new Instances("trainingSet",fv,num);
        Instances cTestSet=new Instances("testSet",fv,num);
                
        cTrainingSet.setClassIndex(2);
        cTestSet.setClassIndex(2);
                
        Instance cEvent;
        
        double dTemp, dRand, weight=1;
        double[] pdValues=new double[3];
//        String sSky,sSunny="sunny",sCloudy="cloudy",sRanning="randing",sPlace,sHome="home",sOut="outSide";
        for(i=0;i<2*num;i++){
            dRand=Math.random();
            dTemp=50+dRand*50;
            pdValues=new double[3];
            pdValues[0]=dTemp;
            
            dRand=Math.random();
            if(dRand<0.3) 
                index=0;
            else if(dRand<0.6)
                index=1;
            else
                index=2;
            
            pdValues[1]=index;
            
            if(dTemp>60&&dTemp<=85&&index==0) 
                pdValues[2]=1;
            else
                pdValues[2]=0;
            
            cEvent=new Instance(weight,pdValues);
            if(i%2==0)
                cTrainingSet.add(cEvent);
            else
                cTestSet.add(cEvent);
        }
        String error;
        try{rf.buildClassifier(cTrainingSet);}
        catch (Exception e){
            error=e.toString();
        }
        
        int numFeatures=rf.getNumFeatures();
        int numTrees=rf.getNumTrees();
        String st=rf.toString();
        
        double dValue=0;
        String prediction,result;
        double accuracy=0;
        for(i=0;i<num;i++){
            cEvent=cTestSet.instance(i);
            try{dValue=rf.classifyInstance(cEvent);}
            catch(Exception e){
                error=e.toString();
                continue;
            }
            index=(int)(dValue+0.5);
            prediction=cPlaceToBe.value(index);
            result=cEvent.toString(2);
            if(prediction.contentEquals(result))
                accuracy+=1.;            
        }        
    }
    public static int getIPOGClassifiers(ArrayList<RandomForest> cvClassifiers,ArrayList<Instances> cvDatasets){
        if(cvClassifiers==null)cvClassifiers=new ArrayList();
        if(cvDatasets==null) cvDatasets=new ArrayList();
        String ContourPath="D:\\Taihao\\Work\\Imaging\\images\\Single Molecule\\100204\\AutoMated\\IPOGShapeTrainingSet_IPOGContour.arff";
        String ShapePath="D:\\Taihao\\Work\\Imaging\\images\\Single Molecule\\100204\\AutoMated\\IPOGShapeTrainingSet_IPOGShape.arff";
        Instances shapeInstances=null,contourInstances=null;
        RandomForest shapeClassifier=new RandomForest(),contourClassifier=new RandomForest();
        String st;
        try{shapeInstances=DataSource.read(ShapePath);}
        catch (Exception e){
            st=e.toString();
        }
        shapeInstances.setClassIndex(shapeInstances.numAttributes() - 1);
        
        try{
            contourInstances=DataSource.read(ContourPath);
//            contourInstances=DataSource.read(ShapePath);
        }
        catch (Exception e){
            st=e.toString();
        }
        contourInstances.setClassIndex(contourInstances.numAttributes() - 1);
        
        try{shapeClassifier.buildClassifier(shapeInstances);}
        catch (Exception e){
            st=e.toString();
        }
        try{contourClassifier.buildClassifier(contourInstances);}
        catch (Exception e){
            st=e.toString();
        }
        cvClassifiers.clear();
        cvDatasets.clear();
        cvDatasets.add(shapeInstances);
        cvDatasets.add(contourInstances);
        cvClassifiers.add(shapeClassifier);
        cvClassifiers.add(contourClassifier);
        
//        testRFClassifier(contourInstances);
//        testRFClassifier(shapeInstances);
        boolean selfTest=false;
        if(!selfTest) return 1;
        int i,len=contourInstances.numInstances(),index,index0;
        double acc=0;
        Instance event;
        double dt=10;
        
        Attribute attribute;
        int num;
        double[] pdSumO={0.,0.},pdSumP={0.,0.},pdAcc={0,0};
        for(i=0;i<len;i++){
            event=contourInstances.instance(i);
            try{dt=contourClassifier.classifyInstance(event);}
            catch(Exception e){}
            index=(int)(dt+0.5);
            pdSumO[index]+=1.;
            
            num=event.numAttributes();
            attribute=event.attribute(num-1);
            st=event.stringValue(attribute);
            if(st.contentEquals("Normal"))
                index0=0;
            else
                index0=1;
            pdSumP[index0]+=1.;
        }
        for(i=0;i<2;i++){
            pdAcc[i]=2.*(pdSumP[i]-pdSumO[i])/(pdSumP[i]+pdSumO[i]);
        }
        return 1;
    }
    public static int testRFClassifier(Instances Dataset){
        ArrayList<Double> dvPartitions=new ArrayList();
        dvPartitions.add(0.8);
        dvPartitions.add(0.2);
        
        ArrayList<Instances> cvDatasets=splitDataset(Dataset,dvPartitions);
        
        Instances trainingInstances=cvDatasets.get(0),testingInstances=cvDatasets.get(1);
        RandomForest RFClassifier=new RandomForest();
        String st;
        
        try{RFClassifier.buildClassifier(trainingInstances);}
        catch (Exception e){
            st=e.toString();
        }
        
        int i,len=testingInstances.numInstances(),index,index0;
        double acc=0;
        Instance event;
        double dt=10;
        
        Attribute attribute;
        int num;
        double[] pdSumO={0.,0.},pdSumP={0.,0.},pdAcc={0,0};
        double numErrs=0;
        for(i=0;i<len;i++){
            event=testingInstances.instance(i);
            try{dt=RFClassifier.classifyInstance(event);}
            catch(Exception e){}
            index=(int)(dt+0.5);
            pdSumO[index]+=1.;
            
            num=event.numAttributes();
            attribute=event.attribute(num-1);
            st=event.stringValue(attribute);
            if(st.contentEquals("Normal"))
                index0=0;
            else
                index0=1;
            pdSumP[index0]+=1.;
            if(index!=index0){
                numErrs++;
            }
        }
        numErrs/=len;
        for(i=0;i<2;i++){
            pdAcc[i]=2.*(pdSumP[i]-pdSumO[i])/(pdSumP[i]+pdSumO[i]);
        }
        return 1;
    }
    public static ArrayList<Instances> splitDataset(Instances Dataset,ArrayList<Double> dvPartitions){
        ArrayList<Instances> cvDatasets=new ArrayList();
        int i,len=Dataset.numInstances(),len1=dvPartitions.size(),ix,num,it;
        ArrayList<Integer> nvLens=new ArrayList();
        double sum=0,dt;
        for(i=0;i<len1;i++){
            sum+=dvPartitions.get(i);
        }
        double pdt[]=new double[len1];
        
        ix=0;num=0;
        for(i=0;i<len1;i++){
            dt=dvPartitions.get(i)/sum;
            pdt[i]=dt;
            cvDatasets.add(new Instances(Dataset,len));
            num=(int)(dt*len);
            nvLens.add(num);
        }
        int index=0;
        
        nvLens.set(0,1600);
        nvLens.set(1,500);
        
        Instances Datasett;
        boolean added;
        int[] pnIndexes=CommonStatisticsMethods.getRandomizedArrayIndexes(len);
        it=0;
        while(it<len){            
            added=false;
            for(i=0;i<len1;i++){                
                Datasett=cvDatasets.get(i);
                index=pnIndexes[it];
                if(Datasett.numInstances()<nvLens.get(i)) {
                    Datasett.add(Dataset.instance(index));
                    added=true;
                    it++;
                }                
            }
            if(!added) break;
        }
        return cvDatasets;
    }    
}
