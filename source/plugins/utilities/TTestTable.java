/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities;
import java.util.*;
import java.io.*;
import utilities.Constants;
import java.util.ArrayList;
import java.util.StringTokenizer;
import utilities.QuickFormatter;
import org.apache.commons.math.distribution.TDistributionImpl;
import ij.IJ;

/**;
 *
 * @author Taihao
 */
public class TTestTable {
    final int rows=37, columns0=12, columns=28;
    double [][] table;
    String sTable;
//    String sTableTotal;
    final int infi=Constants.largestInteger;
    int dfs[],effectiveColumns[];
    double pValues[];
    public TTestTable(){
        table=new double[rows][columns];
/*        sTable[0]="1            1.376	1.963	3.078	6.314	12.71	15.89	31.82	63.66	127.3	318.3	636.6";
        sTable[1]="0.816	1.061	1.386	1.886	2.92	4.303	4.849	6.965	9.925	14.09	22.33	31.6";
        sTable[2]="0.765	0.978	1.25	1.638	2.353	3.182	3.482	4.541	5.841	7.453	10.21	12.92";
        sTable[3]="0.741	0.941	1.19	1.533	2.132	2.776	2.999	3.747	4.604	5.598	7.173	8.61";
        sTable[4]="0.727	0.92	1.156	1.476	2.015	2.571	2.757	3.365	4.032	4.773	5.893	6.869";
        sTable[5]="0.718	0.906	1.134	1.44	1.943	2.447	2.612	3.143	3.707	4.317	5.208	5.959";
        sTable[6]="0.711	0.896	1.119	1.415	1.895	2.365	2.517	2.998	3.499	4.029	4.785	5.408";
        sTable[7]="0.706	0.889	1.108	1.397	1.86	2.306	2.449	2.896	3.355	3.833	4.501	5.041";
        sTable[8]="0.703	0.883	1.1	1.383	1.833	2.262	2.398	2.821	3.25	3.69	4.297	4.781";
        sTable[9]="0.7          0.879	1.093	1.372	1.812	2.228	2.359	2.764	3.169	3.581	4.144	4.587";
        sTable[10]="0.697	0.876	1.088	1.363	1.796	2.201	2.328	2.718	3.106	3.497	4.025	4.437";
        sTable[11]="0.695	0.873	1.083	1.356	1.782	2.179	2.303	2.681	3.055	3.428	3.93	4.318";
        sTable[12]="0.694	0.87	1.079	1.35	1.771	2.16	2.282	2.65	3.012	3.372	3.852	4.221";
        sTable[13]="0.692	0.868	1.076	1.345	1.761	2.145	2.264	2.624	2.977	3.326	3.787	4.14";
        sTable[14]="0.691	0.866	1.074	1.341	1.753	2.131	2.249	2.602	2.947	3.286	3.733	4.073";
        sTable[15]="0.69	0.865	1.071	1.337	1.746	2.12	2.235	2.583	2.921	3.252	3.686	4.015";
        sTable[16]="0.689	0.863	1.069	1.333	1.74	2.11	2.224	2.567	2.898	3.222	3.646	3.965";
        sTable[17]="0.688	0.862	1.067	1.33	1.734	2.101	2.214	2.552	2.878	3.197	3.611	3.922";
        sTable[18]="0.688	0.861	1.066	1.328	1.729	2.093	2.205	2.539	2.861	3.174	3.579	3.883";
        sTable[19]="0.687	0.86	1.064	1.325	1.725	2.086	2.197	2.528	2.845	3.153	3.552	3.85";
        sTable[20]="0.663	0.859	1.063	1.323	1.721	2.08	2.189	2.518	2.831	3.135	3.527	3.819";
        sTable[21]="0.686	0.858	1.061	1.321	1.717	2.074	2.183	2.508	2.819	3.119	3.505	3.792";
        sTable[22]="0.685	0.858	1.06	1.319	1.714	2.069	2.177	2.5	2.807	3.104	3.485	3.768";
        sTable[23]="0.685	0.857	1.059	1.318	1.711	2.064	2.172	2.492	2.797	3.091	3.467	3.745";
        sTable[24]="0.684	0.856	1.058	1.316	1.708	2.06	2.167	2.485	2.787	3.078	3.45	3.725";
        sTable[25]="0.684	0.856	1.058	1.315	1.706	2.056	2.162	2.479	2.779	3.067	3.435	3.707";
        sTable[26]="0.684	0.855	1.057	1.314	1.703	2.052	2.15	2.473	2.771	3.057	3.421	3.69";
        sTable[27]="0.683	0.855	1.056	1.313	1.701	2.048	2.154	2.467	2.763	3.047	3.408	3.674";
        sTable[28]="0.683	0.854	1.055	1.311	1.699	2.045	2.15	2.462	2.756	3.038	3.396	3.659";
        sTable[29]="0.683	0.854	1.055	1.31	1.697	2.042	2.147	2.457	2.75	3.03	3.385	3.646";
        sTable[30]="0.681	0.851	1.05	1.303	1.684	2.021	2.123	2.423	2.704	2.971	3.307	3.551";
        sTable[31]="0.679	0.849	1.047	1.295	1.676	2.009	2.109	2.403	2.678	2.937	3.261	3.496";
        sTable[32]="0.679	0.848	1.045	1.296	1.671	2	2.099	2.39	2.66	2.915	3.232	3.46";
        sTable[33]="0.678	0.846	1.043	1.292	1.664	1.99	2.088	2.374	2.639	2.887	3.195	3.416";
        sTable[34]="0.677	0.845	1.042	1.29	1.66	1.984	2.081	2.364	2.626	2.871	3.174	3.39";
        sTable[35]="0.675	0.842	1.037	1.282	1.646	1.962	2.056	2.33	2.581	2.813	3.098	3.3";
        sTable[36]="0.674	0.841	1.036	1.282	1.64	1.96	2.054	2.326	2.576	2.807	3.091	3.291";
        */
        sTable="df,0.25,0.2,0.15,0.1,0.05,0.025,0.02,0.01,0.005,0.0025,0.001,0.0005,"
            +"1,1,1.376,1.963,3.078,6.314,12.71,15.89,31.82,63.66,127.3,318.3,636.6,"
            +"2,0.816,1.061,1.386,1.886,2.92,4.303,4.849,6.965,9.925,14.09,22.33,31.6,"
            +"3,0.765,0.978,1.25,1.638,2.353,3.182,3.482,4.541,5.841,7.453,10.21,12.92,"
            +"4,0.741,0.941,1.19,1.533,2.132,2.776,2.999,3.747,4.604,5.598,7.173,8.61,"
            +"5,0.727,0.92,1.156,1.476,2.015,2.571,2.757,3.365,4.032,4.773,5.893,6.869,"
            +"6,0.718,0.906,1.134,1.44,1.943,2.447,2.612,3.143,3.707,4.317,5.208,5.959,"
            +"7,0.711,0.896,1.119,1.415,1.895,2.365,2.517,2.998,3.499,4.029,4.785,5.408,"
            +"8,0.706,0.889,1.108,1.397,1.86,2.306,2.449,2.896,3.355,3.833,4.501,5.041,"
            +"9,0.703,0.883,1.1,1.383,1.833,2.262,2.398,2.821,3.25,3.69,4.297,4.781,"
            +"10,0.7,0.879,1.093,1.372,1.812,2.228,2.359,2.764,3.169,3.581,4.144,4.587,"
            +"11,0.697,0.876,1.088,1.363,1.796,2.201,2.328,2.718,3.106,3.497,4.025,4.437,"
            +"12,0.695,0.873,1.083,1.356,1.782,2.179,2.303,2.681,3.055,3.428,3.93,4.318,"
            +"13,0.694,0.87,1.079,1.35,1.771,2.16,2.282,2.65,3.012,3.372,3.852,4.221,"
            +"14,0.692,0.868,1.076,1.345,1.761,2.145,2.264,2.624,2.977,3.326,3.787,4.14,"
            +"15,0.691,0.866,1.074,1.341,1.753,2.131,2.249,2.602,2.947,3.286,3.733,4.073,"
            +"16,0.69,0.865,1.071,1.337,1.746,2.12,2.235,2.583,2.921,3.252,3.686,4.015,"
            +"17,0.689,0.863,1.069,1.333,1.74,2.11,2.224,2.567,2.898,3.222,3.646,3.965,"
            +"18,0.688,0.862,1.067,1.33,1.734,2.101,2.214,2.552,2.878,3.197,3.611,3.922,"
            +"19,0.688,0.861,1.066,1.328,1.729,2.093,2.205,2.539,2.861,3.174,3.579,3.883,"
            +"20,0.687,0.86,1.064,1.325,1.725,2.086,2.197,2.528,2.845,3.153,3.552,3.85,"
            +"21,0.663,0.859,1.063,1.323,1.721,2.08,2.189,2.518,2.831,3.135,3.527,3.819,"
            +"22,0.686,0.858,1.061,1.321,1.717,2.074,2.183,2.508,2.819,3.119,3.505,3.792,"
            +"23,0.685,0.858,1.06,1.319,1.714,2.069,2.177,2.5,2.807,3.104,3.485,3.768,"
            +"24,0.685,0.857,1.059,1.318,1.711,2.064,2.172,2.492,2.797,3.091,3.467,3.745,"
            +"25,0.684,0.856,1.058,1.316,1.708,2.06,2.167,2.485,2.787,3.078,3.45,3.725,"
            +"26,0.684,0.856,1.058,1.315,1.706,2.056,2.162,2.479,2.779,3.067,3.435,3.707,"
            +"27,0.684,0.855,1.057,1.314,1.703,2.052,2.15,2.473,2.771,3.057,3.421,3.69,"
            +"28,0.683,0.855,1.056,1.313,1.701,2.048,2.154,2.467,2.763,3.047,3.408,3.674,"
            +"29,0.683,0.854,1.055,1.311,1.699,2.045,2.15,2.462,2.756,3.038,3.396,3.659,"
            +"30,0.683,0.854,1.055,1.31,1.697,2.042,2.147,2.457,2.75,3.03,3.385,3.646,"
            +"40,0.681,0.851,1.05,1.303,1.684,2.021,2.123,2.423,2.704,2.971,3.307,3.551,"
            +"50,0.679,0.849,1.047,1.295,1.676,2.009,2.109,2.403,2.678,2.937,3.261,3.496,"
            +"60,0.679,0.848,1.045,1.296,1.671,2,2.099,2.39,2.66,2.915,3.232,3.46,"
            +"80,0.678,0.846,1.043,1.292,1.664,1.99,2.088,2.374,2.639,2.887,3.195,3.416,"
            +"100,0.677,0.845,1.042,1.29,1.66,1.984,2.081,2.364,2.626,2.871,3.174,3.39,"
            +"1000,0.675,0.842,1.037,1.282,1.646,1.962,2.056,2.33,2.581,2.813,3.098,3.3,"
            +"inf,0.674,0.841,1.036,1.282,1.64,1.96,2.054,2.326,2.576,2.807,3.091,3.291";
        buildTable();
        extendsTTable();
     }

    public int getRows(){
        return rows;
    }
    
    public int getColumns(){
        return columns;
    }

    public void getPValues(double[] pValues0){
        int c=pValues0.length;
        c=Math.min(columns, c);
        for(int i=0;i<c;i++){
            pValues0[i]=this.pValues[i];
        }
    }

    public void getTable(double[][] tTable){
        int r=tTable.length;
        int c=tTable[0].length;
        r=Math.min(r, rows);
        c=Math.min(c, columns);
        int i,j;
        for(i=0;i<rows;i++){
            for(j=0;j<columns;j++){
                tTable[i][j]=this.table[i][j];
            }
        }
    }

    void buildTable(){
        StringTokenizer st=new StringTokenizer(sTable,",",false);
        String s;
        int i,j;
        pValues=new double[columns];
        dfs=new int[rows];
        int elements=st.countTokens();
        effectiveColumns=new int[rows];
        s=st.nextToken();
        for(j=1;j<=columns0;j++){
            s=st.nextToken();
            pValues[j-1]=Float.valueOf(s);
        }
        for(i=1;i<rows;i++){
            s=st.nextToken();
            dfs[i-1]=Integer.valueOf(s);
            for(j=1;j<=columns0;j++){
                s=st.nextToken();
                table[i-1][j-1]=Float.valueOf(s);
            }
        }
        s=st.nextToken();
        dfs[rows-1]=Constants.largestInteger;
        for(j=1;j<=columns0;j++){
            s=st.nextToken();
            table[rows-1][j-1]=Float.valueOf(s);
        }
    }
    public double getPValue(int df,double t){
        double p=1.1f;
        int row=getRow_approx(df);
        int column=getColumn(row,t);
        p=pValues[column];
        return p;
    }
    int getColumn(int row, double t){
//        int columnf=effectiveColumns[row];
        int columnf=27;
        if(t<table[row][0]) return 0;
        if(t>table[row][columnf]) return columnf;
        int c0=0,c1=columns-1,c=(c0+c1)/2;
        while(c0!=c){
            if(table[row][c]>=t) c1=c;
            else c0=c;
            c=(c0+c1)/2;
        }
        return c;
    }

    int getRow(int df){
        if(df<0)return 0;
        if(df<=30)return df-1;
        if(df<=60)return 29+(df-30)/10;
        if(df<100)return 32+(df-60)/20;
        if(df<1000)return 34;
        return 35;
    }

    int getRow_approx(int df){
        if(df<0)return 0;
        if(df<=30)return df-1;
        return 32;
    }
    void extendsTTable(){
        double p0=0.0005f,factor=0.2f,pValue,t=0,t0=0.;
        int i,j,df;
        for(i=0;i<rows-1;i++){
            t0=0.;
            t=0.;
            df=dfs[i];
            pValue=p0;
            df=dfs[i];
            double x[]=new double[df+1];
            for(j=0;j<df+1;j++){
                x[j]=Math.random();
            }

            factor=0.2f;
            TDistributionImpl tdist=new TDistributionImpl(df);
            
            for(j=columns0;j<columns;j++){
                if(j>20) factor=0.1f;
                if(j>30) factor=0.01f;
                if(j>35) factor=0.0001f;
                pValue*=factor;
                pValues[j]=pValue;

                try{
//                    t=tdist.inverseCumulativeProbability(1-pValue);
                    t=getLargestT(pValue,tdist);
                }
                catch (org.apache.commons.math.MathException e){
                    IJ.error("MathException in method extendsTTable");
                }
                if(t>t0) effectiveColumns[i]=j;
                else
                {
                    break;
                }
                table[i][j]=t;
                t0=t;
            }
        }
    }
    double getLargestT (double pValue0, TDistributionImpl tdist)throws org.apache.commons.math.MathException{
        double mean,t,t0=0.1,t1,pValue;
        pValue=1-tdist.cumulativeProbability(t0);
        t1=t0;
        while(pValue>pValue0){
            t0=t1;
            t1*=2;
            pValue=1-tdist.cumulativeProbability(t1);
        }
        t=0;
        while((Math.abs(t1-t0)/t1)>0.00000000001){
            t=0.5*(t0+t1);
            pValue=1-tdist.cumulativeProbability(t);
            if(pValue>pValue0){
                t0=t;
            }else{
                t1=t;
            }
        }
        return t1;
    }
    public void OutputTTable(String filePath){
        QuickFormatter qf=new QuickFormatter(filePath);
        String newLine=Constants.newline;
        int i,j;
        qf.m_fm.format("   %s  ","df\\pValue");
        for(i=0;i<columns;i++){
            qf.m_fm.format("  %6.2e ", pValues[i]);
        }
        qf.m_fm.format("%s",newLine);
        for(i=0;i<rows;i++){
            qf.m_fm.format("%5d        ", dfs[i]);
            for(j=0;j<columns;j++){
                qf.m_fm.format(" %6.3f ", table[i][j]);
            }
            qf.m_fm.format("%s",newLine);
        }
        qf.m_fm.close();
    }
}
