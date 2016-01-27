

package ImageAnalysis;
import java.util.ArrayList;
import utilities.ArrayofArrays.IntPair;
import utilities.CustomDataTypes.intRange;
import utilities.Geometry.Point2D;
import utilities.CommonMethods;

/**
 *
 * @author Taihao
 */
public class FiniteResolutionRotation {
    int numOCLPO;
    int numOCLP;//number of off-centerline pixels, computed in function buildDisplacementMatrices
    int[] eD;//direction of the edge (dy,dx)
    int[] gD;//direction of the gradient (dyo, dxo)
    int ddx[], ddy[];//Displacement array
    int cddx[][], cddy[][];//cumulated displacement array, the first index is the phase of the pixel.
    int ddxo, ddyo;//coordinates of the first pixel along the orthogonal line, phase independent.
    int cddxo[], cddyo[];//cumulated orthogonal displacement arry, this is a phase independent variable.
    int dddxo[][], dddyo[][];//cumulated orthogonal displacement array, one pixel from each pixel line, the first index is the phase of the pixel.
    int cycle;//cycles is the distance (in number of pixel lines) between two pixels on the same orthogonal pixel line with the same phase.
                   //The pixels of the pixel line within the half clsoed region between the two pixels
                   //form a repeating unit of the orthogonal pixel line.
    int dxLine[][], dyLine[][], dxPhase[][], dyPhase[][];//dxLine[i][j] and dxLine[i][numOCLP+1+j] store the change in the line number when x coordinate of a pixe
                                                         //with phase i increase and decrease by j, respectively.
    int samePL[][][];//samePL[i][deltaX][deltaY] is the distance (in number of pixels) from the pixel (x+deltaX,y+deltaY) to the pixel (x,y)if they are on the same pixel line, otherwise the value is w+h+1.
    PixelPositionNode origin;
    int deltaX,deltaY;
    int length;
    public FiniteResolutionRotation(){
    }
    public FiniteResolutionRotation (int[] eD, int[] gD){
        this.eD=eD;
        if(gD!=null){
            this.gD=gD;
        }else{
            gD[0]=eD[1];
            gD[1]=-eD[0];
        }
        origin=new PixelPositionNode(0,0,0,0,0,eD[1],eD[0]);
        buildDisplacementArrays();
    }
    int length10;
    void buildDisplacementArrays(){
        int i,j,k;
        EdgeElementNode_E eeNode=new EdgeElementNode_E();
        ArrayList <IntPair> ipa=new ArrayList <IntPair>();
        IntPair ip1=new IntPair(0,0), ip2=new IntPair(eD[1],eD[0]);
        eeNode.buildConnection(ip1, ip2, ipa);

        numOCLP=ipa.size();
        length=numOCLP+1;
        length10=10*length;
        ddx=new int[numOCLP+1];
        ddy=new int[numOCLP+1];
        cddx=new int[numOCLP+1][2*(numOCLP+1)];
        cddy=new int[numOCLP+1][2*(numOCLP+1)];


        //computing displacement arrays ddx, ddy
        int aDx=Math.abs(eD[1]);
        int aDy=Math.abs(eD[0]);

        samePL=new int[numOCLP+1][2*(numOCLP+1)+1][2*(numOCLP+1)+1];
        for(i=0;i<=numOCLP;i++){
            for(j=0;j<2*(numOCLP+1)+1;j++){
                for(k=0;k<2*(numOCLP+1)+1;k++){
                    samePL[i][j][k]=length10;
                }
            }
        }

        int x,y,x0=0,y0=0;
        for(i=0;i<numOCLP;i++){
            x=ipa.get(i).x;
            y=ipa.get(i).y;
            ddx[i]=x-x0;
            ddy[i]=y-y0;
            x0=x;
            y0=y;
        }
        if(numOCLP>0){
            ddx[numOCLP]=eD[1]-ipa.get(numOCLP-1).x;
            ddy[numOCLP]=eD[0]-ipa.get(numOCLP-1).y;
        }else{
            ddx[numOCLP]=eD[1];
            ddy[numOCLP]=eD[0];
        }

        //computing the shiftment of pixel line deltaX and deltaY
        deltaX=0;
        deltaY=0;
        for(i=1;i<=numOCLP;i++){
            deltaX=ddx[i-1]-ddx[i];
            deltaY=ddy[i-1]-ddy[i];
            if(deltaX!=0||deltaY!=0) break;
        }

        //computing cumulative displacement arrays cddx and cddy
        int phaseP, phaseN, xP, xN, yP, yN;
        for(i=0;i<=numOCLP;i++){
            phaseP=i;
            phaseN=circularAddition(numOCLP+1,i,-1);
            xP=0;
            xN=0;
            yP=0;
            yN=0;
            for(j=0;j<=numOCLP;j++){
                xP+=ddx[phaseP];
                yP+=ddy[phaseP];
                xN-=ddx[phaseN];
                yN-=ddy[phaseN];
                phaseP=circularAddition(numOCLP+1,phaseP,1);
                phaseN=circularAddition(numOCLP+1,phaseN,-1);
                cddx[i][j]=xP;
                cddy[i][j]=yP;
                cddx[i][j+numOCLP+1]=xN;
                cddy[i][j+numOCLP+1]=yN;
            }
        }
        int xPs,yPs,xNs,yNs,pSteps,nSteps;
        for(i=0;i<=numOCLP;i++){
            phaseP=i;
            phaseN=circularAddition(numOCLP+1,i,-1);
            xP=0;
            xN=0;
            yP=0;
            yN=0;
            pSteps=0;
            nSteps=0;
            while (true){
                if (Math.abs(xP)>numOCLP+1&&Math.abs(xN)>numOCLP+1) break;
                if (Math.abs(yP)>numOCLP+1&&Math.abs(yN)>numOCLP+1) break;

                if(Math.abs(xP)<=numOCLP+1&&Math.abs(yP)<=numOCLP+1){
                    xPs=shiftNegative0(numOCLP+1,xP);
                    yPs=shiftNegative0(numOCLP+1,yP);
                    samePL[i][xPs][yPs]=pSteps;
                    pSteps++;
                }
                if(Math.abs(xN)<=numOCLP+1&&Math.abs(yN)<=numOCLP+1){
                    xNs=shiftNegative0(numOCLP+1,xN);
                    yNs=shiftNegative0(numOCLP+1,yN);
                    samePL[i][xNs][yNs]=nSteps;
                    nSteps--;
                }
                xP+=ddx[phaseP];
                yP+=ddy[phaseP];
                xN-=ddx[phaseN];
                yN-=ddy[phaseN];
                phaseP=circularAddition(numOCLP+1,phaseP,1);
                phaseN=circularAddition(numOCLP+1,phaseN,-1);
            }
        }

        //computting the changes in line number and phase when x and y coordinates change by negative and positive 1.
        int dxL[][]=new int[numOCLP+1][2],dxP[][]=new int[numOCLP+1][2],dyL[][]=new int[numOCLP+1][2],dyP[][]=new int[numOCLP+1][2];
        int delX,delY;
        for(i=0;i<=numOCLP;i++){
            delX=ddx[i];
            delY=ddy[i];
            if(eD[1]==0){//gD[1]==1(-1)&&gD[0]==0
                dxL[i][0]=gD[1]/Math.abs(gD[1]);
                dxL[i][1]=-gD[1]/Math.abs(gD[1]);
                dxP[i][0]=0;
                dxP[i][1]=0;

            }else{
                dxP[i][0]=eD[1]*Math.abs(deltaY)/Math.abs(eD[1]);
                dxP[i][1]=-eD[1]*Math.abs(deltaY)/Math.abs(eD[1]);
                if(samePL[i][1][0]<length10){
                    dxL[i][0]=0;
                }else{//gD[1]!=0
                         dxL[i][0]=gD[1]/Math.abs(gD[1]);
                }
                if(samePL[i][numOCLP+2][0]<length10){//Total number of pixels per pixel line repeat unit is numOCLP+1.
                    dxL[i][1]=0;
                }else{
                    dxL[i][1]=-gD[1]/Math.abs(gD[1]);
                }
            }

            if(eD[0]==0){
                dyL[i][0]=gD[0]/Math.abs(gD[0]);
                dyL[i][1]=-gD[0]/Math.abs(gD[0]);
                dyP[i][0]=0;
                dyP[i][1]=0;
            }else{
                dyP[i][0]=eD[0]*Math.abs(deltaX)/Math.abs(eD[0]);
                dyP[i][1]=-eD[0]*Math.abs(deltaX)/Math.abs(eD[0]);
                if(samePL[i][0][1]<length10){
                    dyL[i][0]=0;
                }else{
                    dyL[i][0]=gD[0]/Math.abs(gD[0]);
                }
                if(samePL[i][0][numOCLP+2]<length10){
                    dyL[i][1]=0;
                }else{
                    dyL[i][1]=-gD[0]/Math.abs(gD[0]);
                }
            }
        }

        //computting the cumulative changes in line number by change
        //x and y coordinates, and the phase of the pixel after change the x and y coordinates.
        int dLx0=0, dLy0=0, dPx0=0, dPy0=0;
        int dLx1=0, dLy1=0, dPx1=0, dPy1=0;

        int phase0x,phase1x,phase0y,phase1y;
        dxPhase=new int[numOCLP+1][2*(numOCLP+1)+1];//dxPhase[i][delX] is the phase of the pixel (x+delX',y), where i is the phase of the pixel (x,y). delX'=delX when delX<=numOCLP, and delX'=-(delX-numOCLP) when delX>numOCLP.
        dxLine=new int[numOCLP+1][2*(numOCLP+1)+1];//dxPhase[i][delX] is the difference of the line between the pixel (x+delX,y) and (x,y), where i is the phase of the pixel (x,y)
        dyPhase=new int[numOCLP+1][2*(numOCLP+1)+1];
        dyLine=new int[numOCLP+1][2*(numOCLP+1)+1];
        for(i=0;i<=numOCLP;i++){
            phase0x=i;
            phase1x=i;
            phase0y=i;
            phase1y=i;
            dLx0=0;
            dLy0=0;
            dLx1=0;
            dLy1=0;
            dxPhase[i][0]=i;
            dxLine[i][0]=0;
            dyPhase[i][0]=i;
            dyLine[i][0]=0;

            for(j=1;j<=numOCLP+1;j++){
                dLx0+=dxL[phase0x][0];
                dPx0=circularAddition(numOCLP+1,phase0x,dxP[phase0x][0]);
                dLy0+=dyL[phase0y][0];
                dPy0=circularAddition(numOCLP+1,phase0y,dyP[phase0y][0]);
                dLx1+=dxL[phase1x][1];
                dPx1=circularAddition(numOCLP+1,phase1x,dxP[phase1x][1]);
                dLy1+=dyL[phase1y][1];
                dPy1=circularAddition(numOCLP+1,phase1y,dyP[phase1y][1]);
                dxPhase[i][j]=dPx0;
                dxLine[i][j]=dLx0;
                dyPhase[i][j]=dPy0;
                dyLine[i][j]=dLy0;
                dxPhase[i][numOCLP+1+j]=dPx1;
                dxLine[i][numOCLP+1+j]=dLx1;
                dyPhase[i][numOCLP+1+j]=dPy1;
                dyLine[i][numOCLP+1+j]=dLy1;
                phase0x=dPx0;
                phase1x=dPx1;
                phase0y=dPy0;
                phase1y=dPy1;
            }
        }

        //computing cddxo[] and cddyo[], (x+cddxo[i], y+cddyo[i]) are the
        //coordinates of the (i+1)-th pixels from (x,y) along the pixel
        //line defined by (x,y) and gD[].
        ArrayList <IntPair> ipao=new ArrayList <IntPair>();
        IntPair ipo1=new IntPair(0,0), ipo2=new IntPair(gD[1],gD[0]);
        eeNode.buildConnection(ipo1, ipo2, ipao);
        numOCLPO=ipao.size();
        cddxo=new int[2*(numOCLPO+1)];
        cddyo=new int[2*(numOCLPO+1)];
        x0=0;
        y0=0;

        for(i=0;i<numOCLPO;i++){
            x=ipao.get(i).x;
            y=ipao.get(i).y;
            cddxo[i]=x;
            cddyo[i]=y;
            cddxo[i+numOCLPO+1]=-x;
            cddyo[i+numOCLPO+1]=-y;
        }

        cddxo[numOCLPO]=gD[1];
        cddyo[numOCLPO]=gD[0];
        cddxo[2*numOCLPO+1]=-gD[1];
        cddyo[2*numOCLPO+1]=-gD[0];
        ddxo=cddxo[0];
        ddyo=cddyo[0];

        //computing dddxo[] and dddyo[]. (x+dddxo[i][j],y+ddyo[i][j]) is the
        //pixel from the (j+1)-th pixel line from the pixel (x,y) (along the
        //direction of gD) that is closet to the line defined by (x,y) and gD.
        //i is the phase of the pixel (x,y).
        calCycle();
        int nL=0;
        int phase=0;
        int phase0=0;
        ArrayList <IntPair> ipaL=new ArrayList <IntPair>();
        IntPair ipo=new IntPair(0,0);
        IntPair ip;
        dddxo=new int[numOCLP+1][2*cycle];
        dddyo=new int[numOCLP+1][2*cycle];
        int r=0;
        int length=numOCLP+1;

        for(i=0;i<=numOCLP;i++){
            nL=0;
            x=0;
            y=0;
            x0=0;
            y0=0;
            phase=i;
            phase0=i;
            for(j=1;true;j++){
                x=gD[1]*(j/length);
                y=gD[0]*(j/length);
                r=j%length;
                if(r>0){
                    x+=cddxo[r-1];
                    y+=cddyo[r-1];
                }
                delX=x-x0;
                delY=y-y0;
                if(samePL[phase][shiftNegative0(length,delX)][shiftNegative0(length,delY)]==length10){
                    nL++;
                    if(nL>cycle+1) break;
                    if(nL>1&&ipaL.size()>0){
                        ip=ClosestPointToLine(ipo,gD[1],gD[0],ipaL);
                        dddxo[i][nL-2]=ip.x;
                        dddyo[i][nL-2]=ip.y;
                    }
                    ipaL.clear();
                    ipaL.add(new IntPair(x,y));
                    phase=calPhase(phase,x0,y0,x,y);
                    x0=x;
                    y0=y;
                }else{
                    ipaL.add(new IntPair(x,y));
                }
            }

            x0=0;
            y0=0;
            nL=0;
            phase=i;

            for(j=1;true;j++){
                x=-gD[1]*(j/length);
                y=-gD[0]*(j/length);
                r=j%length;
                if(r>0){
                    x-=cddxo[r-1];
                    y-=cddyo[r-1];
                }
                delX=x-x0;
                delY=y-y0;
                if(samePL[phase][shiftNegative0(length,delX)][shiftNegative0(length,delY)]==length10){
                    nL++;
                    if(nL>cycle+1) break;
                    if(nL>1&&ipaL.size()>0){
                        ip=ClosestPointToLine(ipo,gD[1],gD[0],ipaL);
                        dddxo[i][cycle+nL-2]=ip.x;
                        dddyo[i][cycle+nL-2]=ip.y;
                    }
                    ipaL.clear();
                    ipaL.add(new IntPair(x,y));
                    phase=calPhase(phase,x0,y0,x,y);
                    x0=x;
                    y0=y;
                }else{
                    ipaL.add(new IntPair(x,y));
                }
            }
        }
    }
    IntPair ClosestPointToLine(IntPair ipo, double dx, double dy, ArrayList <IntPair> ipa){
        Point2D p1, p2, p3;
        int len=ipa.size();
        p1=new Point2D(ipo.x,ipo.y);
        p2=new Point2D(ipo.x+dx,ipo.y+dy);
        p3=new Point2D();
        double dist=0.,minDist=9999999.;
        int minI=0;
        for(int i=0;i<len;i++){
            p3.update(ipa.get(i).x, ipa.get(i).y);
            dist=CommonMethods.getDistanceToLine(p1, p2, p3);
            if(Math.abs(dist)<minDist){
                minI=i;
                minDist=Math.abs(dist);
            }
        }
        return new IntPair(ipa.get(minI).x, ipa.get(minI).y);
    }
    void calCycle(){
        int x=0,y=0;
        for(int i=1; true; i++){
            x=i*gD[1];
            y=i*gD[0];
            if(calPhase(0,0,0,x,y)==0) break;
        }
        cycle=calLine(0,0,0,x,y);
    }
    int calPhase(int phase0, int x0, int y0, int x, int y){
        int dx=eD[1], dy=eD[0];
        int length=numOCLP+1;
        int delX=x-x0;
        int delY=y-y0;
        delX=delX%length;
        delY=delY%length;
        int phase=dxPhase[phase0][shiftNegative0(length,delX)];
        phase=dyPhase[phase][shiftNegative0(length,delY)];
        return phase;
    }
    int calLine(int phase0, int x0, int y0, int x, int y){
        int dx=eD[1], dy=eD[0];
        int length=numOCLP+1;
        int delX=x-x0;
        int delY=y-y0;
        int nx=delX/length;
        int ny=delY/length;
        delX=delX%length;
        delY=delY%length;
        int dL=dxLine[phase0][length]*nx;//This is correct regardless the sign of nx.
        dL+=dxLine[phase0][shiftNegative0(length,delX)];//now dL is the linenumber of (x,y0).
        int phase=calPhase(phase0,x0,y0,x,y0);
        dL+=dyLine[phase][length]*ny;
        dL+=dyLine[phase][shiftNegative0(length,delY)];
        return dL ;
    }
    int shiftNegative0(int length, int position){
        if(position<0) position=length-position;
        return position;
    }
    int shiftNegative1(int length, int position){
        if(position<0) position=length-position-1;
        return position;
    }
    int circularAddition(int length, int position, int delta){
        position+=delta;
        position=position%length;
        if(position<0) position+=length;
        return position;
    }
    int calOrthogonalPosition(PixelPositionNode ppn){//This implementation assures each pixel has unique orthogonal position value.
                                                     //It also assures that the displacement between to pixels of the same pixel line is the difference of the OP values /3/17/2009
        int op=0;
        int line=calLine(0,0,0,ppn.x,ppn.y);
        ppn.setLineNumber(line);
        PixelPositionNode ppn1=orthogonalPositionDisplacement(origin,line,0);
        op=calDisplacement(ppn1,ppn);
        return op;
    }
    int calDisplacement(PixelPositionNode ppn1, PixelPositionNode ppn2){//this method returns the distance (in number of pixels) between ppn2 and ppn1. it returns w+h+1 if they are not on the same pixel line.
        int dist=0;
        int x1=ppn1.x, y1=ppn1.y, phase1=ppn1.phase;
        int x2=ppn2.x, y2=ppn2.y, phase2=ppn2.phase;
        int dx=eD[1],dy=eD[0];
        int n,r,delta,x,y;
        if(dx!=0){
            delta=x2-x1;
            n=delta/dx;
            r=delta%dx;
        }else{
            delta=y2-y1;
            n=delta/dy;
            r=delta%dy;
        }
        x=x1+dx*n;
        y=y1+dy*n;
        if(y2-y>102||x2-x>102||y2-y<-100||x2-x<-100){
            y=y;
        }
        int delX=shiftNegative0(numOCLP+1,x2-x);
        int delY=shiftNegative0(numOCLP+1,y2-y);
        if(delX>=2*(numOCLP+1)+1||delY>=2*(numOCLP+1)+1){
            delX=delX;
        }
        dist=samePL[phase1][delX][delY];
        if(dist<length10) dist+=n*(numOCLP+1);
        return dist;
    }
    public int getSign(int x){
        if(x>0) return 1;
        if(x<0) return -1;
        return 0;
    }
    public PixelPositionNode positionDisplacement(PixelPositionNode ppn, int steps){
        int phase=ppn.phase;
        int y=0,x=0,r=0;
        int length=numOCLP+1;
        if(steps>0){
            x=eD[1]*(steps/length);
            y=eD[0]*(steps/length);
            r=steps%length;
            if(r>0){
                x+=cddx[phase][r-1];
                y+=cddy[phase][r-1];
            }
        }else{
            x=eD[1]*(steps/length);
            y=eD[0]*(steps/length);
            r=steps%length;
            if(r<0){
                x+=cddx[phase][length-r-1];
                y+=cddy[phase][length-r-1];
            }
        }
        PixelPositionNode ppn1=new PixelPositionNode(ppn.x+x,ppn.y+y,calPhase(0,0,0,ppn.x+x,ppn.y+y),calLine(0,0,0,ppn.x+x,ppn.y+y),0,eD[1],eD[0]);
        int op=calOrthogonalPosition(ppn1);
        ppn1.setOP(op);
        return ppn1;
    }

    PixelPositionNode orthogonalPositionDisplacement(PixelPositionNode ppn, int steps){
        PixelPositionNode ppn0=orthogonalPositionDisplacement(ppn,steps,0);
        int op=calOrthogonalPosition(ppn0);
        ppn0.setOP(op);
        return  ppn0;
    }

    PixelPositionNode orthogonalPositionDisplacement(PixelPositionNode ppn, int steps, int key){
        //Current implementation of orthogonal position displacement implemented addativity and reversible.
        int phase=ppn.phase;
        int y=ppn.y,x=ppn.x,r=0;
        int n=steps/cycle;

        x+=dddxo[phase][cycle-1]*n;
        y+=dddyo[phase][cycle-1]*n;

        r=steps%cycle;
        if(r>0){
            x+=dddxo[phase][r-1];
            y+=dddyo[phase][r-1];
        }else if(r<0){
            r=cycle+r;            
            x+=dddxo[phase][r-1]-dddxo[phase][cycle-1];
            y+=dddyo[phase][r-1]-dddyo[phase][cycle-1];
        }

        PixelPositionNode ppn1=new PixelPositionNode(x,y,calPhase(0,0,0,x,y),calLine(0,0,0,x,y),0,eD[1],eD[0]);
//        int op=calOrthogonalPosition(ppn1);//Calling this method cause stack overflow because the methods is calling the current method.
//        ppn1.setOP(op);
        return ppn1;
    }
    public PixelPositionNode getPPN_XY(int x, int y){
        PixelPositionNode ppn=new PixelPositionNode();
        updatePPN_XY(ppn,x,y);
        return ppn;
    }
    public void updatePPN_XY(PixelPositionNode ppn, int x, int y){
        ppn.x=x;
        ppn.y=y;
        ppn.lineNumber=calLine(0,0,0,x,y);
        ppn.op=calOrthogonalPosition(ppn);
        ppn.phase=calPhase(0,0,0,x,y);
    }
    public PixelPositionNode getPPN_LO(int l, int o){
        PixelPositionNode ppn=new PixelPositionNode();
        updatePPN_LO(ppn,l,o);
        return ppn;
    }
    public void updatePPN_LO(PixelPositionNode ppn, int l, int o){
        PixelPositionNode ppn1=orthogonalPositionDisplacement(origin,l);
        ppn.update(positionDisplacement(ppn1,o-ppn1.op));
    }

    public PixelPositionNode getPPN_LY(int l, int y, int index, intRange xRange){//In case of multiple pixels in the same pixel line have the same y and the x is in xRange, it returns first (index >0) or last (index <0) pixel position.
        //This method returns null if no points is in xRange.
        PixelPositionNode ppn=orthogonalPositionDisplacement(origin,l);
        int dy=eD[0],dx=eD[1];
        int yo=ppn.y;
        if(dy==0){
            if(yo!=y) return null;
            if(index>0){
                return getPPN_XY(xRange.getMin(),y);
            }else{
                return getPPN_XY(xRange.getMax(),y);
            }
        }
        int xo=ppn.x,x;
        int delta=y-yo;
        int n=delta/dy;
        int r=delta%dy;

        if(delta*dy>=0){
            if(r==0) n--;
        }else{
            n--;
        }

        int phase=ppn.phase;
        x=xo+dx*n;
        yo+=n*dy;

        int xi=xRange.getMin()-1,xf=xRange.getMax()+1;

        int y1=yo;
        while((y1-yo)*(y1-y)<=0){
            y1+=ddy[phase];
            x+=ddx[phase];
            phase=circularAddition(length,phase,1);
            if(xRange.contains(x)&&y1==y){
                if(!xRange.contains(xi)) xi=x;
                xf=x;
            }
        }
        if(index>0)x=xi;
        else x=xf;
        if(xRange.contains(x)) {
            ppn=getPPN_XY(x,y);
        }
        else {
            ppn=null;
        }
        return ppn;
    }

    public PixelPositionNode getPPN_LX(int l, int x, int index, intRange yRange){//In case of multiple pixels in the same pixel line have the same y and the x is in xRange, it returns first (index >0) or last (index <0) pixel position.
        //This method returns null if no points is in xRange.
        PixelPositionNode ppn=orthogonalPositionDisplacement(origin,l);
        int dy=eD[0],dx=eD[1];
        int xo=ppn.x;
        if(dx==0){
            if(xo!=x) return null;
            if(index>0){
                return getPPN_XY(x,yRange.getMin());
            }else{
                return getPPN_XY(x,yRange.getMax());
            }
        }
        int yo=ppn.y,y;
        int delta=x-xo;
        int n=delta/dx;
        int r=delta%dx;

        if(delta*dx>=0){
            if(r==0) n--;
        }else{
            n--;
        }

        int phase=ppn.phase;
        y=yo+dy*n;
        xo+=n*dx;

        int yi=yRange.getMin()-1,yf=yRange.getMax()+1;

        int x1=xo;
        while((x1-xo)*(x1-x)<=0){
            x1+=ddx[phase];
            y+=ddy[phase];
            phase=circularAddition(length,phase,1);
            if(yRange.contains(y)&&x1==x){
                if(!yRange.contains(yi)) yi=y;
                yf=y;
            }
        }
        if(index>0)y=yi;
        else y=yf;
        if(yRange.contains(y)) {
            ppn=getPPN_XY(x,y);
        }
        else {
            ppn=null;
        }
        return ppn;
    }
    
    public int ddx(int phase){
        return ddx[phase];
    }
    public int ddy(int phase){
        return ddy[phase];
    }
    public int cddx(int phase,int index){
        return cddx[phase][index];
    }
    public int cddy(int phase,int index){
        return cddy[phase][index];
    }

    public int xDisplacement(int phase, int steps){
        int x=0,r=0;
        int length=numOCLP+1;
        if(steps>0){
            x=eD[1]*(steps/length);
            r=steps%length;
            if(r>0)x+=cddx[phase][r-1];
        }else{
            x=eD[1]*(steps/length);
            r=steps%length;
            if(r<0)x+=cddx[phase][length-r-1];
        }
        return x;
    }

    public void moveForward(PixelPositionNode ppn, int steps){
        ppn.update(positionDisplacement(ppn,steps));
    }

    public void moveLeft(PixelPositionNode ppn, int steps){
        ppn.update(getPPN_LO(ppn.lineNumber-steps,ppn.op));
    }

    public int yDisplacement(int phase, int steps){
        int y=0,r=0;
        int length=numOCLP+1;
        if(steps>0){
            y=eD[0]*(steps/length);
            r=steps%length;
            if(r>0)y+=cddy[phase][r-1];
        }else{
            y=eD[0]*(steps/length);
            r=steps%length;
            if(r<0)y+=cddy[phase][length-r-1];
        }
        return y;
    }
    public int dddxo(int phase,int index){
        return dddxo[phase][index];
    }
    public int dddyo(int phase,int index){
        return dddyo[phase][index];
    }
 }
