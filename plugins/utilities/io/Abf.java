/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package utilities.io;
import java.util.ArrayList;
import java.io.*;
import ij.IJ;
import utilities.CommonMethods;
import utilities.io.LittleEndianIO;
import utilities.io.PrintAssist;
import utilities.io.FileAssist;

/**
 *
 * @author Taihao
 */
public class Abf {

	ArrayList <Byte> m_vectcPreDataBuffer;
	ArrayList <Byte> m_vectcPostDataBuffer;
    String headFileDir;
    int m_nNumChannels;
    String m_sFilePath;
    float m_pfDataRanges[][],m_fMin,m_fMax;



	//Variables in Group 1:
	char m_sFileType[]=new char[5];

	short m_nOperationMode,m_nNumPointsIgnored,m_nFileType,m_nMSBinFormat;
	float m_Amplitude,m_fFileVersionNumber,m_fHeaderVersionNumber;
	int m_lActualAcqLength,m_lActualEpisodes,m_lFileStartDate,m_lFileStartTime,
            m_lStopWatchTime;

	//Variables in Group 2:

	int m_lDataSectionPtr,m_lTagSectionPtr,m_lNumTagEntries,m_lLongDescriptionPtr,m_lLongDescriptionLines,
		m_lDACFilePtr,m_lDACFileNumEpisodes,m_lDeltaArrayPtr,m_lNumDeltas,m_lNotebookPtr,m_lNotebookManEntries,
		m_lNotebookAutoEntries,m_lSynchArrayPtr,m_lSynchArraySize;
	short m_nDataFormat;
	char m_sUnused68[]=new char[5],m_sUnused102[]=new char[19];

	//Variables in Group 3:

	short m_nADCNumChannels,m_nAveragingMode,m_nUndoRunCount,m_nFirstEpisodeInRun,m_nTriggerSource,m_nTriggerAction,
		m_nTriggerPolarity;
	float m_fADCSampleInterval,m_fADCSecondSampleInterval,m_fSynchTimeUnit,m_fSecondsPerRun,
		m_fScopeOutputInterval,m_fEpisodeStartToStart,m_fRunStartToStart,m_fTrialStartToStart,
		m_fTriggerThreshold;
	int m_lNumSamplesPerEpisode,m_lPreTriggerSamples,m_lEpisodesPerRun,m_lRunsPerTrial,
		m_lNumberOfTrials,m_lAverageCount,m_lClockChange;
	char m_sUnused198[]=new char[3];

	//Variables in Group 4:

	short m_nDrawingStrategy,m_nTileDisplay,m_nEraseStrategy,m_nDataDisplayMode,m_nChannelStatsStrategy,
		m_nMultiColor,m_nShowPNRawData;
	int m_lDisplayAverageUpdate,m_lCalculationPeriod,m_lSamplesPerTrace,m_lStartDisplayNum,
		m_lFinishDisplayNum;
	char m_sUnused234[]=new char[11];

	//Variables in Group 5:

	float m_fADCRange,m_fDACRange;
	int m_lADCResolution,m_lDACResolution;

	//Variables in Group 6:

	short m_nExperimentType,m_nAutosampleEnable,m_nAutosampleADCNum,m_nAutosampleInstrument,m_nManualInfoStrategy;
	float m_fAutosampleAdditGain,m_fAutosampleFilter,m_fAutosampleMembraneCap,m_fCellID1,m_fCellID2,m_fCellID3;
	char m_sCreatorInfo[]=new char[17],m_sFileComment[]=new char[57],m_sUnused366[]=new char[13];

	//Variables in Group 7:

	short m_nADCPtoLChannelMap[]=new short[16],m_nADCSamplingSeq[]=new short[16],m_nSignalType;
	char m_sADCChannelName[][]=new char[16][11],m_sADCUnits[][]=new char[16][9],m_sDACChannelName[][]=new char[4][11],
		m_sDACChannelUnits[][]=new char[4][9],m_sUnused1412[]=new char[11];
	float m_fADCProgrammableGain[]=new float[16],m_fADCDisplayAmplification[]=new float[16],m_fADCDisplayOffset[]=new float[16],m_fInstrumentScaleFactor[]=new float[16],
		m_fInstrumentOffset[]=new float[16],m_fSignalGain[]=new float[16],m_fSignalOffset[]=new float[16],m_fSignalLowpassFilter[]=new float[16],m_fSignalHighpassFilter[]=new float[16],
		m_fDACScaleFactor[]=new float[4],m_fDACHoldingLevel[]=new float[4];

	//Variables in Group 8:

	short m_nOUTEnable,m_nSampleNumberOUT1,m_nSampleNumberOUT2,m_nFirstEpisodeOUT,m_nLastEpisodeOUT,
		m_nPulseSamplesOUT1,m_nPulseSamplesOUT2;

	//Variables in Group 9:

	short m_nDigitalEnable,m_nWaveformSource,m_nActiveDACChannel,m_nInterEpisodeLevel,m_nEpochType[]=new short[10],
		m_nEpochInitDuration[]=new short[10],m_nEpochDuration[]=new short[10],m_nEpochDurationInc[]=new short[10],m_nDigitalHolding,
		m_nDigitalInterEpisode,m_nDigitalValue[]=new short[10];
	float m_fEpochInitLevel[]=new float[10],m_fEpochLevelInc[]=new float[10],m_fWaveformOffset;
//	float m_fEpochInitLevel[]=new short[10],m_fEpochLevelInc[]=new short[10],m_fWaveformOffset;
	char m_sUnused1612[]=new char[9];

	//Variables in Group 10:

	float m_fDACFileScale,m_fDACFileOffset;
	char m_sUnused1628[]=new char[3],m_sDACFileName[]=new char[13],m_sDACFilePath[]=new char[61],m_sUnused1706[]=new char[13];
	short m_nDACFileEpisodeNum,m_nDACFileADCNum;

	//Variables in Group 11:

	short m_nConditEnable,m_nConditChannel;
	int m_lConditNumPulses;
	float m_fBaselineDuration,m_fBaselineLevel,m_fStepDuration,m_fStepLevel,m_fPostTrainPeriod,
		m_fPostTrainLevel;
	char m_sUnused1750[]=new char[13];

	//Variables in Group 12:

	short m_nParamToVary;
	char m_sParamValueList[]=new char[81];

	//Variables in Group 13:

	short m_nAutopeakEnable,m_nAutopeakPolarity,m_nAutopeakADCNum,m_nAutopeakSearchMode,m_nAutopeakSmoothing,
		m_nAutopeakBaseline,m_nAutopeakAverage;
	int m_lAutopeakStart,m_lAutopeakEnd;
	char m_sUnused1866[]=new char[15];

	//Variables in Group 14:

	short m_nArithmeticEnable,m_nArithmeticADCNumA,m_nArithmeticADCNumB,m_nArithmeticExpression;
	float m_fArithmeticUpperLimit,m_fArithmeticLowerLimit,m_fArithmeticK1,m_fArithmeticK2,m_fArithmeticK3,
		m_fArithmeticK4,m_fArithmeticK5,m_fArithmeticK6;
	char m_sArithmeticOperator[]=new char[3],m_sArithmeticUnits[]=new char[9],m_sUnused1930[]=new char[3];

	//Variables in Group 15:

	short m_nPNEnable,m_nPNPosition,m_nPNPolarity,m_nPNNumPulses,m_nPNADCNum;
	float m_fPNHoldingLevel,m_fPNSettlingTime,m_fPNInterPulse;
	char m_sUnused1954[]=new char[13];

	//Variables in Group 16:

	short m_nListEnable;
	char m_sUnused1966[]=new char[81];

	//End of the header variables.

	float m_pfData[];
	float m_pfCurrentData[];
	float m_fScale;
	long m_nNumCurrentPoints;
	float m_fCurrentInterval;
	float m_pfPreviousData[];
	float m_pfAdjustedData[];
   	float m_pfPreviousAdjustedData[];
	boolean  m_bImportedAsciiAsData;
	ArrayList <Float> m_vectfScale;
	String m_sAbfFileName;

    public Abf(){
        m_vectfScale=new ArrayList();
        m_vectcPreDataBuffer=new ArrayList();
        m_vectcPostDataBuffer=new ArrayList();
        m_nADCNumChannels=(short) -1;
        m_sFilePath=null;
    }


    void ReadData (String path) throws FileNotFoundException,IOException
    {
        m_sFilePath=new String(path);
        int OffsetOfBytes,i;
        FileInputStream f=new FileInputStream(path);
        DataInputStream ds=new DataInputStream(f);

    //Reading Group 1;

        OffsetOfBytes=0;
//        fread(&m_sFileType, 4,1,fpAbfIn);
        for(i=0;i<4;i++){
            m_sFileType[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=4;
//        fread(&m_fFileVersionNumber,4,1,fpAbfIn);
        m_fFileVersionNumber=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nOperationMode,2,1,fpAbfIn);
        m_nOperationMode=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_lActualAcqLength,4,1,fpAbfIn);
        m_lActualAcqLength=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nNumPointsIgnored,2,1,fpAbfIn);
        m_nNumPointsIgnored=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_lActualEpisodes,4,1,fpAbfIn);
        m_lActualEpisodes=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lFileStartDate,4,1,fpAbfIn);
        m_lFileStartDate=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lFileStartTime,4,1,fpAbfIn);
        m_lFileStartTime=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lStopWatchTime,4,1,fpAbfIn);
        m_lStopWatchTime=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fHeaderVersionNumber,4,1,fpAbfIn);
        m_fHeaderVersionNumber=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nFileType,2,1,fpAbfIn);
        m_nFileType=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nMSBinFormat,2,1,fpAbfIn);
        m_nMSBinFormat=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;

    //Reading Group 2;

//        fread(&m_lDataSectionPtr,4,1,fpAbfIn);
        m_lDataSectionPtr=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lTagSectionPtr,4,1,fpAbfIn);
        m_lTagSectionPtr=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lNumTagEntries,4,1,fpAbfIn);
        m_lNumTagEntries=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lLongDescriptionPtr,4,1,fpAbfIn);
        m_lLongDescriptionPtr=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lLongDescriptionLines,4,1,fpAbfIn);
        m_lLongDescriptionLines=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lDACFilePtr,4,1,fpAbfIn);
        m_lDACFilePtr=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lDACFileNumEpisodes,4,1,fpAbfIn);
        m_lDACFileNumEpisodes=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sUnused68,4,1,fpAbfIn);
        for(i=0;i<4;i++){
            m_sUnused68[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=4;
//        fread(&m_lDeltaArrayPtr,4,1,fpAbfIn);
        m_lDeltaArrayPtr=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lNumDeltas,4,1,fpAbfIn);
        m_lNumDeltas=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lNotebookPtr,4,1,fpAbfIn);
        m_lNotebookPtr=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lNotebookManEntries,4,1,fpAbfIn);
        m_lNotebookManEntries=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lNotebookAutoEntries,4,1,fpAbfIn);
        m_lNotebookAutoEntries=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lSynchArrayPtr,4,1,fpAbfIn);
        m_lSynchArrayPtr=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lSynchArraySize,4,1,fpAbfIn);
        m_lSynchArraySize=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nDataFormat,2,1,fpAbfIn);
        m_nDataFormat=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sUnused102,18,1,fpAbfIn);
        for(i=0;i<18;i++){
            m_sUnused102[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=18;

    //Reading Group 3;

//        fread(&m_nADCNumChannels,2,1,fpAbfIn);
       m_nADCNumChannels=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fADCSampleInterval,4,1,fpAbfIn);
        m_fADCSampleInterval=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fADCSecondSampleInterval,4,1,fpAbfIn);
        m_fADCSecondSampleInterval=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fSynchTimeUnit,4,1,fpAbfIn);
        m_fSynchTimeUnit=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fSecondsPerRun,4,1,fpAbfIn);
        m_fSecondsPerRun=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lNumSamplesPerEpisode,4,1,fpAbfIn);
        m_lNumSamplesPerEpisode=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lPreTriggerSamples,4,1,fpAbfIn);
        m_lPreTriggerSamples=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lEpisodesPerRun,4,1,fpAbfIn);//So far so good!
        m_lEpisodesPerRun=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lRunsPerTrial,4,1,fpAbfIn);
        m_lRunsPerTrial=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lNumberOfTrials,4,1,fpAbfIn);
        m_lNumberOfTrials=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nAveragingMode,2,1,fpAbfIn);
        m_nAveragingMode=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nUndoRunCount,2,1,fpAbfIn);
        m_nUndoRunCount=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nFirstEpisodeInRun,2,1,fpAbfIn);
        m_nFirstEpisodeInRun=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fTriggerThreshold,4,1,fpAbfIn);
        m_fTriggerThreshold=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nTriggerSource,2,1,fpAbfIn);
        m_nTriggerSource=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nTriggerAction,2,1,fpAbfIn);
        m_nTriggerAction=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nTriggerPolarity,2,1,fpAbfIn);
        m_nTriggerPolarity=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fScopeOutputInterval,4,1,fpAbfIn);
        m_fScopeOutputInterval=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fEpisodeStartToStart,4,1,fpAbfIn);
        m_fEpisodeStartToStart=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fRunStartToStart,4,1,fpAbfIn);
        m_fRunStartToStart=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fTrialStartToStart,4,1,fpAbfIn);
        m_fTrialStartToStart=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lAverageCount,4,1,fpAbfIn);
        OffsetOfBytes+=4;
        m_lAverageCount=LittleEndianIO.readIntLittleEndian(ds);
//        fread(&m_lClockChange,4,1,fpAbfIn);
        m_lClockChange=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sUnused198,2,1,fpAbfIn);
        for(i=0;i<2;i++){
            m_sUnused198[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=2;

    // Reading Group 4;

//        fread(&m_nDrawingStrategy,2,1,fpAbfIn);
        m_nDrawingStrategy=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nTileDisplay,2,1,fpAbfIn);
        m_nTileDisplay=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nEraseStrategy,2,1,fpAbfIn);
        m_nEraseStrategy=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nDataDisplayMode,2,1,fpAbfIn);
        m_nDataDisplayMode=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_lDisplayAverageUpdate,4,1,fpAbfIn);
        m_lDisplayAverageUpdate=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nChannelStatsStrategy,2,1,fpAbfIn);
        m_nChannelStatsStrategy=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_lCalculationPeriod,4,1,fpAbfIn);
        m_lCalculationPeriod=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lSamplesPerTrace,4,1,fpAbfIn);
        m_lSamplesPerTrace=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lStartDisplayNum,4,1,fpAbfIn);
        m_lStartDisplayNum=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lFinishDisplayNum,4,1,fpAbfIn);
        m_lFinishDisplayNum=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nMultiColor,2,1,fpAbfIn);
        m_nMultiColor=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nShowPNRawData,2,1,fpAbfIn);
        m_nShowPNRawData=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sUnused234,10,1,fpAbfIn);
        for(i=0;i<10;i++){
            m_sUnused234[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=10;

    //Reading Group 5;

//        fread(&m_fADCRange,4,1,fpAbfIn);
        m_fADCRange=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fDACRange,4,1,fpAbfIn);
        m_fDACRange=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lADCResolution,4,1,fpAbfIn);//good!
        m_lADCResolution=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lDACResolution,4,1,fpAbfIn);
        m_lDACResolution=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;

    //Reading Group 6;

//        fread(&m_nExperimentType,2,1,fpAbfIn);
        m_nExperimentType=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutosampleEnable,2,1,fpAbfIn);
        m_nAutosampleEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutosampleADCNum,2,1,fpAbfIn);
        m_nAutosampleADCNum=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutosampleInstrument,2,1,fpAbfIn);
        m_nAutosampleInstrument=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fAutosampleAdditGain,4,1,fpAbfIn);//500 good!
        m_fAutosampleAdditGain=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fAutosampleFilter,4,1,fpAbfIn);
        m_fAutosampleFilter=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fAutosampleMembraneCap,4,1,fpAbfIn);
        m_fAutosampleMembraneCap=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nManualInfoStrategy,2,1,fpAbfIn);
        m_nManualInfoStrategy=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fCellID1,4,1,fpAbfIn);
        m_fCellID1=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fCellID2,4,1,fpAbfIn);
        m_fCellID2=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fCellID3,4,1,fpAbfIn);
        m_fCellID3=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sCreatorInfo,16,1,fpAbfIn);//"Clampex   "
        for(i=0;i<16;i++){
            m_sCreatorInfo[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=16;
//        fread(&m_sFileComment,56,1,fpAbfIn);
        for(i=0;i<56;i++){
            m_sFileComment[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=56;
//        fread(&m_sUnused366,12,1,fpAbfIn);
        for(i=0;i<12;i++){
            m_sUnused366[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=12;

    //Reading Group 7;

        for(i=0;i<=15;++i)
        {
//            fread(&m_nADCPtoLChannelMap[i],2,1,fpAbfIn);
            m_nADCPtoLChannelMap[i]=LittleEndianIO.readShortLittleEndian(ds);
            OffsetOfBytes+=2;
        }

        for (i=0;i<=15;++i)
        {
//            fread(&m_nADCSamplingSeq[i],2,1,fpAbfIn);
            m_nADCSamplingSeq[i]=LittleEndianIO.readShortLittleEndian(ds);
            OffsetOfBytes+=2;
        }
        int j;//ok so far 3/22/2010
        for (i=0;i<=15;++i)
        {
//            fread(&m_sADCChannelName[i],10,1,fpAbfIn);
            for(j=0;j<10;j++){
                m_sADCChannelName[i][j]=CommonMethods.readChar8(ds);
            }
            OffsetOfBytes+=10;
        }

        for (i=0;i<=15;++i)
        {
//                fread(&m_sADCUnits[i],8,1,fpAbfIn);
            for(j=0;j<8;j++){
                m_sADCUnits[i][j]=CommonMethods.readChar8(ds);
            }
            OffsetOfBytes+=8;
        }

        for (i=0;i<=15;++i)
        {
//            fread(&m_fADCProgrammableGain[i],4,1,fpAbfIn);
            m_fADCProgrammableGain[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fread(&m_fADCDisplayAmplification[i],4,1,fpAbfIn);
            m_fADCDisplayAmplification[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fread(&m_fADCDisplayOffset[i],4,1,fpAbfIn);
            m_fADCDisplayOffset[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }


        for (i=0;i<=15;++i)
        {
//        fread(&m_fInstrumentScaleFactor[i],4,1,fpAbfIn);
            m_fInstrumentScaleFactor[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fread(&m_fInstrumentOffset[i],4,1,fpAbfIn);
            m_fInstrumentOffset[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }


        for (i=0;i<=15;++i)
        {
//            fread(&m_fSignalGain[i],4,1,fpAbfIn);
            m_fSignalGain[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fread(&m_fSignalOffset[i],4,1,fpAbfIn);
            m_fSignalOffset[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }


        for (i=0;i<=15;++i)
        {
//            fread(&m_fSignalLowpassFilter[i],4,1,fpAbfIn);
            m_fSignalLowpassFilter[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fread(&m_fSignalHighpassFilter[i],4,1,fpAbfIn);
            m_fSignalHighpassFilter[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=3;++i)
        {
//            fread(&m_sDACChannelName[i],10,1,fpAbfIn);
            for(j=0;j<10;j++){
                m_sDACChannelName[i][j]=CommonMethods.readChar8(ds);
            }
            OffsetOfBytes+=10;
        }

        for (i=0;i<=3;++i)
        {
//            fread(&m_sDACChannelUnits[i],8,1,fpAbfIn);
            for(j=0;j<8;j++){
                m_sDACChannelUnits[i][j]=CommonMethods.readChar8(ds);
            }
            OffsetOfBytes+=8;
        }

        for (i=0;i<=3;++i)
        {
//            fread(&m_fDACScaleFactor[i],4,1,fpAbfIn);
            m_fDACScaleFactor[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=3;++i)
        {
//            fread(&m_fDACHoldingLevel[i],4,1,fpAbfIn);
            m_fDACHoldingLevel[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

//        fread(&m_nSignalType,2,1,fpAbfIn);
        m_nSignalType=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sUnused1412,10,1,fpAbfIn);
        for(j=0;j<10;j++){
           m_sUnused1412[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=10;

    //Reading Group 8;

//        fread(&m_nOUTEnable,2,1,fpAbfIn);
        m_nOUTEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nSampleNumberOUT1,2,1,fpAbfIn);
        m_nSampleNumberOUT1=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nSampleNumberOUT2,2,1,fpAbfIn);
        m_nSampleNumberOUT2=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nFirstEpisodeOUT,2,1,fpAbfIn);
        m_nFirstEpisodeOUT=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nLastEpisodeOUT,2,1,fpAbfIn);
        m_nLastEpisodeOUT=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nPulseSamplesOUT1,2,1,fpAbfIn);
        m_nPulseSamplesOUT1=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nPulseSamplesOUT2,2,1,fpAbfIn);
        m_nPulseSamplesOUT2=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;

    //Reading Group 9;

//        fread(&m_nDigitalEnable,2,1,fpAbfIn);
        m_nDigitalEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nWaveformSource,2,1,fpAbfIn);
        m_nWaveformSource=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nActiveDACChannel,2,1,fpAbfIn);
        m_nActiveDACChannel=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nInterEpisodeLevel,2,1,fpAbfIn);
        m_nInterEpisodeLevel=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;

        for (i=0;i<=9;++i)
        {
//            fread(&m_nEpochType[i],2,1,fpAbfIn);
            m_nEpochType[i]=LittleEndianIO.readShortLittleEndian(ds);
            OffsetOfBytes+=2;
        }

        for (i=0;i<=9;++i)
        {
//        fread(&m_fEpochInitLevel[i],4,1,fpAbfIn);
            m_fEpochInitLevel[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=9;++i)
        {
//          fread(&m_fEpochLevelInc[i],4,1,fpAbfIn);
            m_fEpochLevelInc[i]=LittleEndianIO.readFloatLittleEndian(ds);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=9;++i)
        {
//            fread(&m_nEpochInitDuration[i],2,1,fpAbfIn);
            m_nEpochInitDuration[i]=LittleEndianIO.readShortLittleEndian(ds);
            OffsetOfBytes+=2;
        }

        for (i=0;i<=9;++i)
        {
//            fread(&m_nEpochDurationInc[i],2,1,fpAbfIn);
            m_nEpochDurationInc[i]=LittleEndianIO.readShortLittleEndian(ds);
            OffsetOfBytes+=2;
        }


//        fread(&m_nDigitalHolding,2,1,fpAbfIn);
        m_nDigitalHolding=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nDigitalInterEpisode,2,1,fpAbfIn);
        m_nDigitalInterEpisode=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;

        for (i=0;i<=9;++i)
        {
//            fread(&m_nDigitalValue[i],2,1,fpAbfIn);
            m_nDigitalValue[i]=LittleEndianIO.readShortLittleEndian(ds);
            OffsetOfBytes+=2;
        }

//        fread(&m_fWaveformOffset,4,1,fpAbfIn);
        m_fWaveformOffset=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sUnused1612,8,1,fpAbfIn);
        for(j=0;j<8;j++){
            m_sUnused1612[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=8;

    //Rreading Group10;

//        fread(&m_fDACFileScale,4,1,fpAbfIn);
        m_fDACFileScale=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fDACFileOffset,4,1,fpAbfIn);
        m_fDACFileOffset=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sUnused1628,2,1,fpAbfIn);
        for(i=0;i<2;i++){
            m_sUnused1628[i]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=2;
//        fread(&m_nDACFileEpisodeNum,2,1,fpAbfIn);
        m_nDACFileEpisodeNum=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nDACFileADCNum,2,1,fpAbfIn);
        m_nDACFileADCNum=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sDACFileName,12,1,fpAbfIn);
        for(j=0;j<12;j++){
            m_sDACFileName[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=12;
//        fread(&m_sDACFilePath,60,1,fpAbfIn);
        for(j=0;j<60;j++){
            m_sDACFilePath[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=60;
//        fread(&m_sUnused1706,12,1,fpAbfIn);
        for(j=0;j<12;j++){
           m_sUnused1706[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=12;

    //Reading Group11;

//        fread(&m_nConditEnable,2,1,fpAbfIn);
        m_nConditEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nConditChannel,2,1,fpAbfIn);
        m_nConditChannel=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_lConditNumPulses,4,1,fpAbfIn);
        m_lConditNumPulses=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fBaselineDuration,4,1,fpAbfIn);
        m_fBaselineDuration=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fBaselineLevel,4,1,fpAbfIn);
        m_fBaselineLevel=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fStepDuration,4,1,fpAbfIn);
        m_fStepDuration=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fStepLevel,4,1,fpAbfIn);
        m_fStepLevel=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fPostTrainPeriod,4,1,fpAbfIn);
        m_fPostTrainPeriod=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fPostTrainLevel,4,1,fpAbfIn);
        m_fPostTrainLevel=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sUnused1750,12,1,fpAbfIn);
        for(j=0;j<12;j++){
            m_sUnused1750[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=12;

    //Reading Group12;

//        fread(&m_nParamToVary,2,1,fpAbfIn);
        m_nParamToVary=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sParamValueList,80,1,fpAbfIn);
        for(j=0;j<80;j++){
           m_sParamValueList[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=80;//checked till here.

    //Reading Group13;

//        fread(&m_nAutopeakEnable,2,1,fpAbfIn);
        m_nAutopeakEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutopeakPolarity,2,1,fpAbfIn);
        m_nAutopeakPolarity=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutopeakADCNum,2,1,fpAbfIn);
        m_nAutopeakADCNum=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutopeakSearchMode,2,1,fpAbfIn);
        m_nAutopeakSearchMode=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_lAutopeakStart,4,1,fpAbfIn);
        m_lAutopeakStart=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_lAutopeakEnd,4,1,fpAbfIn);
        m_lAutopeakEnd=LittleEndianIO.readIntLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nAutopeakSmoothing,2,1,fpAbfIn);
        m_nAutopeakSmoothing=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutopeakBaseline,2,1,fpAbfIn);
        m_nAutopeakBaseline=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nAutopeakAverage,2,1,fpAbfIn);
        m_nAutopeakAverage=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sUnused1866,14,1,fpAbfIn);
        for(j=0;j<14;j++){
            m_sUnused1866[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=14;

    //Reading Group14;

//        fread(&m_nArithmeticEnable,2,1,fpAbfIn);
        m_nArithmeticEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fArithmeticUpperLimit,4,1,fpAbfIn);
        m_fArithmeticUpperLimit=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fArithmeticLowerLimit,4,1,fpAbfIn);
        m_fArithmeticLowerLimit=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nArithmeticADCNumA,2,1,fpAbfIn);
        m_nArithmeticADCNumA=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nArithmeticADCNumB,2,1,fpAbfIn);
        m_nArithmeticADCNumB=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fArithmeticK1,4,1,fpAbfIn);
        m_fArithmeticK1=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fArithmeticK2,4,1,fpAbfIn);
        m_fArithmeticK2=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fArithmeticK3,4,1,fpAbfIn);
        m_fArithmeticK3=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fArithmeticK4,4,1,fpAbfIn);
        m_fArithmeticK4=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sArithmeticOperator,2,1,fpAbfIn);//good!
        for(j=0;j<2;j++){
            m_sArithmeticOperator[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=2;
//        fread(&m_sArithmeticUnits,8,1,fpAbfIn);
        for(j=0;j<8;j++){
            m_sArithmeticUnits[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=8;
//        fread(&m_fArithmeticK5,4,1,fpAbfIn);
        m_fArithmeticK5=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fArithmeticK6,4,1,fpAbfIn);
        m_fArithmeticK6=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_nArithmeticExpression,2,1,fpAbfIn);
        m_nArithmeticExpression=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sUnused1930,2,1,fpAbfIn);
        for(j=0;j<2;j++){
            m_sUnused1930[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=2;

    //Reading Group 15;

//        fread(&m_nPNEnable,2,1,fpAbfIn);
        m_nPNEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nPNPosition,2,1,fpAbfIn);
        m_nPNPosition=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nPNPolarity,2,1,fpAbfIn);
        m_nPNPolarity=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nPNNumPulses,2,1,fpAbfIn);
        m_nPNNumPulses=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_nPNADCNum,2,1,fpAbfIn);
        m_nPNADCNum=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_fPNHoldingLevel,4,1,fpAbfIn);
        m_fPNHoldingLevel=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fPNSettlingTime,4,1,fpAbfIn);
        m_fPNSettlingTime=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_fPNInterPulse,4,1,fpAbfIn);
        m_fPNInterPulse=LittleEndianIO.readFloatLittleEndian(ds);
        OffsetOfBytes+=4;
//        fread(&m_sUnused1954,12,1,fpAbfIn);
        for(j=0;j<12;j++){
            m_sUnused1954[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=12;

    //Reading Group 16;

//        fread(&m_nListEnable,2,1,fpAbfIn);
        m_nListEnable=LittleEndianIO.readShortLittleEndian(ds);
        OffsetOfBytes+=2;
//        fread(&m_sUnused1966,80,1,fpAbfIn);
        for(j=0;j<80;j++){
            m_sUnused1966[j]=CommonMethods.readChar8(ds);
        }
        OffsetOfBytes+=80;

        PreparePointers();


        m_vectfScale.clear();
        for(int k=0;k<m_nADCNumChannels;k++)
        {
            float fScale=1.0f/(m_fInstrumentScaleFactor[k]);
            if(k==m_nAutosampleADCNum)
            {
                if(m_nAutosampleEnable!=0)
                {
                    fScale/=m_fAutosampleAdditGain;
                }
            }
            fScale*=m_fADCRange;
            fScale/=m_lADCResolution;
            m_vectfScale.add(fScale);
        }

        int nBytesToSkip=(m_lDataSectionPtr-4)*512;
        byte bTemp;
        m_vectcPreDataBuffer.clear();
        while(nBytesToSkip>0)
        {
//            fread(&cTemp,sizeof(cTemp),1,fpAbfIn);
            bTemp=ds.readByte();
            nBytesToSkip-=1;
            m_vectcPreDataBuffer.add(bTemp);
        }

        short nAmplitude;
        float fAmplitude=0.f;
        float fScale=0.f;
        int nPosition=0;
        int i0=0;

        if(m_nDataFormat==0)
        {
            for(i=0;i<m_lActualAcqLength/m_nADCNumChannels;i++)
    //		for(i=0;i<m_lActualAcqLength;i++)
            {
                for(i0=0;i0<m_nADCNumChannels;i0++)
                {
//                    fread(&nAmplitude, sizeof(nAmplitude),1,fpAbfIn);
                    nAmplitude=LittleEndianIO.readShortLittleEndian(ds);
                    fScale=m_vectfScale.get(i0);
//                    *(m_pfData+nPosition)=nAmplitude*fScale;
                    m_pfData[nPosition]=nAmplitude*fScale;
                    nPosition++;
                }
            }
        }
        else
        {
            for(i=0;i<m_lActualAcqLength/m_nADCNumChannels;i++)
    //		for(i=0;i<m_lActualAcqLength;i++)
            {
                for(i0=0;i0<m_nADCNumChannels;i0++)
                {
//                    fread(&fAmplitude, sizeof(float),1,fpAbfIn);

//                    *(m_pfData+nPosition)=fAmplitude;
                    m_pfData[nPosition]=LittleEndianIO.readFloatLittleEndian(ds);
                    nPosition++;
                }
            }
        }

        m_vectcPostDataBuffer.clear();

        int nExtraLine=0;
//        while(feof(fpAbfIn))
        while(ds.available()>0)
        {
//            fread(&cTemp, sizeof(char),1,fpAbfIn);
            bTemp=ds.readByte();
            m_vectcPostDataBuffer.add(bTemp);
            nExtraLine++;
        }
        ds.close();
        f.close();
    }


    void PreparePointers()
    {
        m_pfData=new float[m_lActualAcqLength];
    }


    AbfNode GetTrace()
    {
        AbfNode aNode=new AbfNode();
        float fmin=999999.f,fmax=-999999.f;
        float fAmp;
        float[] pfData=new float[m_lActualAcqLength];
        int i;
        for(i=0;i<m_lActualAcqLength;i++)
        {
            fAmp=m_pfData[i];
            if(fAmp<fmin) fmin=fAmp;
            if(fAmp>fmax) fmax=fAmp;
            pfData[i]=fAmp;
        }
        aNode.nSize=m_lActualAcqLength;
        aNode.pfData=pfData;
        aNode.fSampleInterval=m_fADCSampleInterval;
        aNode.nNumChannels=m_nADCNumChannels;
        for(i=0;i<m_nADCNumChannels;i++)
        {
            aNode.fAutosampleAdditGain=m_fAutosampleAdditGain;
        }
        aNode.fMaxAmp=fmax;
        aNode.fMinAmp=fmin;
        aNode.sFileName=m_sAbfFileName;
        return aNode;
    }

    int GetDataSize()
    {
        return m_lActualAcqLength;
    }

    void UpdateSize(int nSize)
    {
        m_lActualAcqLength=nSize;
    }

    void UpdateData(float []pfData)
    {
        PreparePointers();
        int i;
        for(i=0;i<m_lActualAcqLength;i++)
        {
            m_pfData[i]=pfData[i];
        }
    }

    void SetScale(int nChannelIndex, float fFold)
    {
        m_vectfScale.set(nChannelIndex, fFold);
        m_fInstrumentScaleFactor[nChannelIndex]/=fFold;
    }
    void ResetSampleInterval(float fNewSampleInterval)
    {
        m_fADCSampleInterval=fNewSampleInterval;
    }

    void ResetInstrumentScaleFactor(int nChannelIndex, float NewScaleFactor)
    {
        m_fInstrumentScaleFactor[nChannelIndex]=NewScaleFactor;
    }

    void UpdateData(AbfNode NewNode)
    {
        UpdateSize(NewNode.nSize);
        ResetSampleInterval(NewNode.fSampleInterval);
        UpdateData(NewNode.pfData);
        m_fAutosampleAdditGain=NewNode.fAutosampleAdditGain;
    }


    float GetSampleInterval()
    {
        return m_fADCSampleInterval;
    }


    void UpdateSampleInterval(float fSampleInterval)
    {
        m_fADCSampleInterval=fSampleInterval;
    }


    String getAbfFileName(){
        return m_sAbfFileName;
    }

    void WriteData(String path, short nDataFormat)throws FileNotFoundException,IOException
    {
        File f=new File(path);
        FileOutputStream fs=new FileOutputStream(f);
        DataOutputStream ds=new DataOutputStream(fs);
        short nTemp=m_nDataFormat;
        m_nDataFormat=nDataFormat;
        m_nDataFormat=nTemp;
        int i=0,j;

    //Writing Group 1;

        int OffsetOfBytes=0;
//        fwrite(&m_sFileType, 4,1,fpAbfOut);
        for(i=0;i<4;i++){
            CommonMethods.writeChar8(ds,m_sFileType[i]);
        }
        OffsetOfBytes+=4;
//        fwrite(&m_fFileVersionNumber,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fFileVersionNumber);
        OffsetOfBytes+=4;
//        fwrite(&m_nOperationMode,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nOperationMode);
        OffsetOfBytes+=2;


        if(m_bImportedAsciiAsData)
        {
//            fwrite(&m_lActualAcqLength,4,1,fpAbfOut);
            LittleEndianIO.writeIntLittleEndian(ds,m_lActualAcqLength);
        }
        else
        {
//            fwrite(&m_lActualAcqLength,4,1,fpAbfOut);
            LittleEndianIO.writeIntLittleEndian(ds,m_lActualAcqLength);
        }
        OffsetOfBytes+=4;

        if(m_bImportedAsciiAsData)
        {
//            fwrite(&m_nNumPointsIgnored,2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nNumPointsIgnored);
        }
        else
        {
//            fwrite(&m_nNumPointsIgnored,2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nNumPointsIgnored);
        }

        OffsetOfBytes+=2;
//        fwrite(&m_lActualEpisodes,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lActualEpisodes);
        OffsetOfBytes+=4;
//        fwrite(&m_lFileStartDate,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lFileStartDate);
        OffsetOfBytes+=4;
//        fwrite(&m_lFileStartTime,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lFileStartTime);
        OffsetOfBytes+=4;
//        fwrite(&m_lStopWatchTime,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lStopWatchTime);
        OffsetOfBytes+=4;
//        fwrite(&m_fHeaderVersionNumber,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fHeaderVersionNumber);
        OffsetOfBytes+=4;
//        fwrite(&m_nFileType,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nFileType);
        OffsetOfBytes+=2;
//        fwrite(&m_nMSBinFormat,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nMSBinFormat);
        OffsetOfBytes+=2;

    //Writing Group 2;
    //	m_lDataSectionPtr=4;
//        fwrite(&m_lDataSectionPtr,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lDataSectionPtr);
        OffsetOfBytes+=4;
//        fwrite(&m_lTagSectionPtr,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lTagSectionPtr);
        OffsetOfBytes+=4;
//        fwrite(&m_lNumTagEntries,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lNumTagEntries);
        OffsetOfBytes+=4;
//        fwrite(&m_lLongDescriptionPtr,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lLongDescriptionPtr);
        OffsetOfBytes+=4;
//        fwrite(&m_lLongDescriptionLines,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lLongDescriptionLines);
        OffsetOfBytes+=4;
//        fwrite(&m_lDACFilePtr,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lDACFilePtr);
        OffsetOfBytes+=4;
//        fwrite(&m_lDACFileNumEpisodes,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lDACFileNumEpisodes);
        OffsetOfBytes+=4;
//        fwrite(&m_sUnused68,4,1,fpAbfOut);
        for(i=0;i<4;i++){
            CommonMethods.writeChar8(ds,m_sUnused68[i]);
        }
        OffsetOfBytes+=4;
//        fwrite(&m_lDeltaArrayPtr,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lDeltaArrayPtr);
        OffsetOfBytes+=4;
//        fwrite(&m_lNumDeltas,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lNumDeltas);
        OffsetOfBytes+=4;
//        fwrite(&m_lNotebookPtr,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lNotebookPtr);
        OffsetOfBytes+=4;
//        fwrite(&m_lNotebookManEntries,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lNotebookManEntries);
        OffsetOfBytes+=4;
//        fwrite(&m_lNotebookAutoEntries,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lNotebookAutoEntries);
        OffsetOfBytes+=4;
//        fwrite(&m_lSynchArrayPtr,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lSynchArrayPtr);
        OffsetOfBytes+=4;
//        fwrite(&m_lSynchArraySize,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lSynchArraySize);
        OffsetOfBytes+=4;
        nTemp=m_nDataFormat;
        m_nDataFormat=0;
//        fwrite(&m_nDataFormat,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDataFormat);
        m_nDataFormat=nTemp;
        OffsetOfBytes+=2;
//        fwrite(&m_sUnused102,18,1,fpAbfOut);
        for(i=0;i<18;i++){
            CommonMethods.writeChar8(ds,m_sUnused102[i]);
        }
        OffsetOfBytes+=18;

    //Writing Group 3;

//        fwrite(&m_nADCNumChannels,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nADCNumChannels);
        OffsetOfBytes+=2;
//        fwrite(&m_fADCSampleInterval,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fADCSampleInterval);
        OffsetOfBytes+=4;
//        fwrite(&m_fADCSecondSampleInterval,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fADCSecondSampleInterval);
        OffsetOfBytes+=4;
//        fwrite(&m_fSynchTimeUnit,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fSynchTimeUnit);
        OffsetOfBytes+=4;
//        fwrite(&m_fSecondsPerRun,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fSecondsPerRun);
        OffsetOfBytes+=4;
//        fwrite(&m_lNumSamplesPerEpisode,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lNumSamplesPerEpisode);
        OffsetOfBytes+=4;
//        fwrite(&m_lPreTriggerSamples,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lPreTriggerSamples);
        OffsetOfBytes+=4;
//        fwrite(&m_lEpisodesPerRun,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lEpisodesPerRun);
        OffsetOfBytes+=4;
//        fwrite(&m_lRunsPerTrial,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lRunsPerTrial);
        OffsetOfBytes+=4;
//        fwrite(&m_lNumberOfTrials,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lNumberOfTrials);
        OffsetOfBytes+=4;
//        fwrite(&m_nAveragingMode,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAveragingMode);
        OffsetOfBytes+=2;
//        fwrite(&m_nUndoRunCount,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nUndoRunCount);
        OffsetOfBytes+=2;
//        fwrite(&m_nFirstEpisodeInRun,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nFirstEpisodeInRun);
        OffsetOfBytes+=2;
//        fwrite(&m_fTriggerThreshold,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fTriggerThreshold);
        OffsetOfBytes+=4;
//        fwrite(&m_nTriggerSource,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nTriggerSource);
        OffsetOfBytes+=2;
//        fwrite(&m_nTriggerAction,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nTriggerAction);
        OffsetOfBytes+=2;
//        fwrite(&m_nTriggerPolarity,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nTriggerPolarity);
        OffsetOfBytes+=2;
//        fwrite(&m_fScopeOutputInterval,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fScopeOutputInterval);
        OffsetOfBytes+=4;
//        fwrite(&m_fEpisodeStartToStart,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fEpisodeStartToStart);
        OffsetOfBytes+=4;
//        fwrite(&m_fRunStartToStart,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fRunStartToStart);
        OffsetOfBytes+=4;
//        fwrite(&m_fTrialStartToStart,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fTrialStartToStart);
        OffsetOfBytes+=4;
//        fwrite(&m_lAverageCount,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lAverageCount);
        OffsetOfBytes+=4;
//        fwrite(&m_lClockChange,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lClockChange);
        OffsetOfBytes+=4;
//        fwrite(&m_sUnused198,2,1,fpAbfOut);
        for(i=0;i<2;i++){
            CommonMethods.writeChar8(ds,m_sUnused198[i]);
        }
        OffsetOfBytes+=2;

    // Writing Group 4;

//        fwrite(&m_nDrawingStrategy,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDrawingStrategy);
        OffsetOfBytes+=2;
//        fwrite(&m_nTileDisplay,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nTileDisplay);
        OffsetOfBytes+=2;
//        fwrite(&m_nEraseStrategy,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nEraseStrategy);
        OffsetOfBytes+=2;
//        fwrite(&m_nDataDisplayMode,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDataDisplayMode);
        OffsetOfBytes+=2;
//        fwrite(&m_lDisplayAverageUpdate,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lDisplayAverageUpdate);
        OffsetOfBytes+=4;
//        fwrite(&m_nChannelStatsStrategy,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nChannelStatsStrategy);
        OffsetOfBytes+=2;
//        fwrite(&m_lCalculationPeriod,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lCalculationPeriod);
        OffsetOfBytes+=4;
//        fwrite(&m_lSamplesPerTrace,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lSamplesPerTrace);
        OffsetOfBytes+=4;
//        fwrite(&m_lStartDisplayNum,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lStartDisplayNum);
        OffsetOfBytes+=4;
//        fwrite(&m_lFinishDisplayNum,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lFinishDisplayNum);
        OffsetOfBytes+=4;
//        fwrite(&m_nMultiColor,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nMultiColor);
        OffsetOfBytes+=2;
//        fwrite(&m_nShowPNRawData,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nShowPNRawData);
        OffsetOfBytes+=2;
//        fwrite(&m_sUnused234,10,1,fpAbfOut);
        for(i=0;i<10;i++){
            CommonMethods.writeChar8(ds,m_sUnused234[i]);
        }
        OffsetOfBytes+=10;

    //Writing Group 5;

//        fwrite(&m_fADCRange,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fADCRange);
        OffsetOfBytes+=4;
//        fwrite(&m_fDACRange,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fDACRange);
        OffsetOfBytes+=4;
//        fwrite(&m_lADCResolution,4,1,fpAbfOut);//good!
         LittleEndianIO.writeIntLittleEndian(ds,m_lADCResolution);
       OffsetOfBytes+=4;
//        fwrite(&m_lDACResolution,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lDACResolution);
        OffsetOfBytes+=4;

    //Writing Group 6;

//        fwrite(&m_nExperimentType,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nExperimentType);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutosampleEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutosampleEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutosampleADCNum,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutosampleADCNum);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutosampleInstrument,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutosampleInstrument);
        OffsetOfBytes+=2;
//        fwrite(&m_fAutosampleAdditGain,4,1,fpAbfOut);//500 good!
        LittleEndianIO.writeFloatLittleEndian(ds,m_fAutosampleAdditGain);
        OffsetOfBytes+=4;
//        fwrite(&m_fAutosampleFilter,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fAutosampleFilter);
        OffsetOfBytes+=4;
//        fwrite(&m_fAutosampleMembraneCap,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fAutosampleMembraneCap);
        OffsetOfBytes+=4;
//        fwrite(&m_nManualInfoStrategy,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nManualInfoStrategy);
        OffsetOfBytes+=2;
//        fwrite(&m_fCellID1,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fCellID1);
        OffsetOfBytes+=4;
//        fwrite(&m_fCellID2,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fCellID2);
        OffsetOfBytes+=4;
//        fwrite(&m_fCellID3,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fCellID3);
        OffsetOfBytes+=4;
//        fwrite(&m_sCreatorInfo,16,1,fpAbfOut);//"Clampex   "
        for(i=0;i<16;i++){
            CommonMethods.writeChar8(ds,m_sCreatorInfo[i]);
        }
        OffsetOfBytes+=16;
//        fwrite(&m_sFileComment,56,1,fpAbfOut);
        for(i=0;i<56;i++){
            CommonMethods.writeChar8(ds,m_sFileComment[i]);
        }
        OffsetOfBytes+=56;
//        fwrite(&m_sUnused366,12,1,fpAbfOut);
        for(i=0;i<12;i++){
            CommonMethods.writeChar8(ds,m_sUnused366[i]);
        }
        OffsetOfBytes+=12;

    //Writing Group 7;

        for(i=0;i<=15;++i)
        {
//            fwrite(&m_nADCPtoLChannelMap[i],2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nADCPtoLChannelMap[i]);
            OffsetOfBytes+=2;
        }

        for (i=0;i<=15;++i)
        {
//            fwrite(&m_nADCSamplingSeq[i],2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nADCSamplingSeq[i]);
            OffsetOfBytes+=2;
        }

        for (i=0;i<=15;++i)
        {
//            fwrite(&m_sADCChannelName[i],10,1,fpAbfOut);
            for(j=0;j<10;j++){
                CommonMethods.writeChar8(ds,m_sADCChannelName[i][j]);
            }
            OffsetOfBytes+=10;
        }

        for (i=0;i<=15;++i)
        {
//                fwrite(&m_sADCUnits[i],8,1,fpAbfOut);
                for(j=0;j<8;j++){
                    CommonMethods.writeChar8(ds,m_sADCUnits[i][j]);
                }
                OffsetOfBytes+=8;
        }

        for (i=0;i<=15;++i)
        {
//            fwrite(&m_fADCProgrammableGain[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fADCProgrammableGain[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fwrite(&m_fADCDisplayAmplification[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fADCDisplayAmplification[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fwrite(&m_fADCDisplayOffset[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fADCDisplayOffset[i]);
            OffsetOfBytes+=4;
        }


        for (i=0;i<=15;++i)
        {
//        fwrite(&m_fInstrumentScaleFactor[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fInstrumentScaleFactor[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fwrite(&m_fInstrumentOffset[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fInstrumentOffset[i]);
            OffsetOfBytes+=4;
        }


        for (i=0;i<=15;++i)
        {
//            fwrite(&m_fSignalGain[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fSignalGain[i]);
            OffsetOfBytes+=4;
        }

            for (i=0;i<=15;++i)
            {
//                fwrite(&m_fSignalOffset[i],4,1,fpAbfOut);
                LittleEndianIO.writeFloatLittleEndian(ds,m_fSignalOffset[i]);
                OffsetOfBytes+=4;
            }


        for (i=0;i<=15;++i)
        {
//            fwrite(&m_fSignalLowpassFilter[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fSignalLowpassFilter[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=15;++i)
        {
//            fwrite(&m_fSignalHighpassFilter[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fSignalHighpassFilter[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=3;++i)
        {
//            fwrite(&m_sDACChannelName[i],10,1,fpAbfOut);
            for(j=0;j<10;j++){
                CommonMethods.writeChar8(ds,m_sDACChannelName[i][j]);
            }
            OffsetOfBytes+=10;
        }

        for (i=0;i<=3;++i)
        {
//            fwrite(&m_sDACChannelUnits[i],8,1,fpAbfOut);
            for(j=0;j<8;j++){
                CommonMethods.writeChar8(ds,m_sDACChannelUnits[i][j]);
            }
            OffsetOfBytes+=8;
        }

        for (i=0;i<=3;++i)
        {
//            fwrite(&m_fDACScaleFactor[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fDACScaleFactor[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=3;++i)
        {
//            fwrite(&m_fDACHoldingLevel[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fDACHoldingLevel[i]);
            OffsetOfBytes+=4;
        }

//        fwrite(&m_nSignalType,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nSignalType);
        OffsetOfBytes+=2;
//        fwrite(&m_sUnused1412,10,1,fpAbfOut);
        for(j=0;j<10;j++){
            CommonMethods.writeChar8(ds,m_sUnused1412[j]);
        }
        OffsetOfBytes+=10;

    //Writing Group 8;

//        fwrite(&m_nOUTEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nOUTEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_nSampleNumberOUT1,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nSampleNumberOUT1);
        OffsetOfBytes+=2;
//        fwrite(&m_nSampleNumberOUT2,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nSampleNumberOUT2);
        OffsetOfBytes+=2;
//        fwrite(&m_nFirstEpisodeOUT,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nFirstEpisodeOUT);
        OffsetOfBytes+=2;
//        fwrite(&m_nLastEpisodeOUT,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nLastEpisodeOUT);
        OffsetOfBytes+=2;
//        fwrite(&m_nPulseSamplesOUT1,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nPulseSamplesOUT1);
        OffsetOfBytes+=2;
//        fwrite(&m_nPulseSamplesOUT2,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nPulseSamplesOUT2);
        OffsetOfBytes+=2;

    //Writing Group 9;

//        fwrite(&m_nDigitalEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDigitalEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_nWaveformSource,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nWaveformSource);
        OffsetOfBytes+=2;
//        fwrite(&m_nActiveDACChannel,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nActiveDACChannel);
        OffsetOfBytes+=2;
//        fwrite(&m_nInterEpisodeLevel,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nInterEpisodeLevel);
        OffsetOfBytes+=2;

        for (i=0;i<=9;++i)
        {
//            fwrite(&m_nEpochType[i],2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nEpochType[i]);
            OffsetOfBytes+=2;
        }

        for (i=0;i<=9;++i)
        {
//            fwrite(&m_fEpochInitLevel[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fEpochInitLevel[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=9;++i)
        {
//            fwrite(&m_fEpochLevelInc[i],4,1,fpAbfOut);
            LittleEndianIO.writeFloatLittleEndian(ds,m_fEpochLevelInc[i]);
            OffsetOfBytes+=4;
        }

        for (i=0;i<=9;++i)
        {
//            fwrite(&m_nEpochInitDuration[i],2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nEpochInitDuration[i]);
            OffsetOfBytes+=2;
        }

        for (i=0;i<=9;++i)
        {
//            fwrite(&m_nEpochDurationInc[i],2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nEpochDurationInc[i]);
            OffsetOfBytes+=2;
        }


//        fwrite(&m_nDigitalHolding,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDigitalHolding);
        OffsetOfBytes+=2;
//        fwrite(&m_nDigitalInterEpisode,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDigitalInterEpisode);
        OffsetOfBytes+=2;

        for (i=0;i<=9;++i)
        {
//            fwrite(&m_nDigitalValue[i],2,1,fpAbfOut);
            LittleEndianIO.writeShortLittleEndian(ds,m_nDigitalValue[i]);
            OffsetOfBytes+=2;
        }

//        fwrite(&m_fWaveformOffset,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fWaveformOffset);
        OffsetOfBytes+=4;
//        fwrite(&m_sUnused1612,8,1,fpAbfOut);
        for(i=0;i<8;i++){
            CommonMethods.writeChar8(ds,m_sUnused1612[i]);
        }
        OffsetOfBytes+=8;

    //RWriting Group10;

//        fwrite(&m_fDACFileScale,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fDACFileScale);
        OffsetOfBytes+=4;
//        fwrite(&m_fDACFileOffset,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fDACFileOffset);
        OffsetOfBytes+=4;
//        fwrite(&m_sUnused1628,2,1,fpAbfOut);
        for(i=0;i<2;i++){
            CommonMethods.writeChar8(ds,m_sUnused1628[i]);
        }
        OffsetOfBytes+=2;
//        fwrite(&m_nDACFileEpisodeNum,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDACFileEpisodeNum);
        OffsetOfBytes+=2;
//        fwrite(&m_nDACFileADCNum,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nDACFileADCNum);
        OffsetOfBytes+=2;
//        fwrite(&m_sDACFileName,12,1,fpAbfOut);
        for(i=0;i<12;i++){
            CommonMethods.writeChar8(ds,m_sDACFileName[i]);
        }
        OffsetOfBytes+=12;
//        fwrite(&m_sDACFilePath,60,1,fpAbfOut);
        for(i=0;i<60;i++){
            CommonMethods.writeChar8(ds,m_sDACFilePath[i]);
        }
        OffsetOfBytes+=60;
//        fwrite(&m_sUnused1706,12,1,fpAbfOut);
        for(i=0;i<12;i++){
            CommonMethods.writeChar8(ds,m_sUnused1706[i]);
        }
        OffsetOfBytes+=12;

    //Writing Group11;

//        fwrite(&m_nConditEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nConditEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_nConditChannel,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nConditChannel);
        OffsetOfBytes+=2;
//        fwrite(&m_lConditNumPulses,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lConditNumPulses);
        OffsetOfBytes+=4;
//        fwrite(&m_fBaselineDuration,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fBaselineDuration);
        OffsetOfBytes+=4;
//        fwrite(&m_fBaselineLevel,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fBaselineLevel);
        OffsetOfBytes+=4;
//        fwrite(&m_fStepDuration,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fStepDuration);
        OffsetOfBytes+=4;
//        fwrite(&m_fStepLevel,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fStepLevel);
        OffsetOfBytes+=4;
//        fwrite(&m_fPostTrainPeriod,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fPostTrainPeriod);
        OffsetOfBytes+=4;
//        fwrite(&m_fPostTrainLevel,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fPostTrainLevel);
        OffsetOfBytes+=4;
//        fwrite(&m_sUnused1750,12,1,fpAbfOut);
        for(i=0;i<12;i++){
            CommonMethods.writeChar8(ds,m_sUnused1750[i]);
        }
        OffsetOfBytes+=12;

    //Writing Group12;

//        fwrite(&m_nParamToVary,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nParamToVary);
        OffsetOfBytes+=2;
//        fwrite(&m_sParamValueList,80,1,fpAbfOut);
        for(i=0;i<80;i++){
            CommonMethods.writeChar8(ds,m_sParamValueList[i]);
        }
        OffsetOfBytes+=80;//checked till here.

    //Writing Group13;

//        fwrite(&m_nAutopeakEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutopeakEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutopeakPolarity,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutopeakPolarity);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutopeakADCNum,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutopeakADCNum);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutopeakSearchMode,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutopeakSearchMode);
        OffsetOfBytes+=2;
//        fwrite(&m_lAutopeakStart,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lAutopeakStart);
        OffsetOfBytes+=4;
//        fwrite(&m_lAutopeakEnd,4,1,fpAbfOut);
        LittleEndianIO.writeIntLittleEndian(ds,m_lAutopeakEnd);
        OffsetOfBytes+=4;
//        fwrite(&m_nAutopeakSmoothing,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutopeakSmoothing);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutopeakBaseline,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutopeakBaseline);
        OffsetOfBytes+=2;
//        fwrite(&m_nAutopeakAverage,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nAutopeakAverage);
        OffsetOfBytes+=2;
//        fwrite(&m_sUnused1866,14,1,fpAbfOut);
        for(i=0;i<14;i++){
            CommonMethods.writeChar8(ds,m_sUnused1866[i]);
        }
        OffsetOfBytes+=14;

    //Writing Group14;

//        fwrite(&m_nArithmeticEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nArithmeticEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_fArithmeticUpperLimit,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticUpperLimit);
        OffsetOfBytes+=4;
//        fwrite(&m_fArithmeticLowerLimit,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticLowerLimit);
        OffsetOfBytes+=4;
//        fwrite(&m_nArithmeticADCNumA,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nArithmeticADCNumA);
        OffsetOfBytes+=2;
//        fwrite(&m_nArithmeticADCNumB,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nArithmeticADCNumB);
        OffsetOfBytes+=2;
//        fwrite(&m_fArithmeticK1,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticK1);
        OffsetOfBytes+=4;
//        fwrite(&m_fArithmeticK2,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticK2);
        OffsetOfBytes+=4;
//        fwrite(&m_fArithmeticK3,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticK3);
        OffsetOfBytes+=4;
//        fwrite(&m_fArithmeticK4,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticK4);
        OffsetOfBytes+=4;
//        fwrite(&m_sArithmeticOperator,2,1,fpAbfOut);//good!
        for(i=0;i<2;i++){
            CommonMethods.writeChar8(ds,m_sArithmeticOperator[i]);
        }
        OffsetOfBytes+=2;
//        fwrite(&m_sArithmeticUnits,8,1,fpAbfOut);
        for(i=0;i<8;i++){
            CommonMethods.writeChar8(ds,m_sArithmeticUnits[i]);
        }
        OffsetOfBytes+=8;
//        fwrite(&m_fArithmeticK5,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticK5);
        OffsetOfBytes+=4;
//        fwrite(&m_fArithmeticK6,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fArithmeticK6);
        OffsetOfBytes+=4;
//        fwrite(&m_nArithmeticExpression,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nArithmeticExpression);
        OffsetOfBytes+=2;
//        fwrite(&m_sUnused1930,2,1,fpAbfOut);
        for(i=0;i<2;i++){
            CommonMethods.writeChar8(ds,m_sUnused1930[i]);
        }
        OffsetOfBytes+=2;

    //Writing Group 15;

//        fwrite(&m_nPNEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nPNEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_nPNPosition,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nPNPosition);
        OffsetOfBytes+=2;
//        fwrite(&m_nPNPolarity,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nPNPolarity);
        OffsetOfBytes+=2;
//        fwrite(&m_nPNNumPulses,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nPNNumPulses);
        OffsetOfBytes+=2;
//        fwrite(&m_nPNADCNum,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nPNADCNum);
        OffsetOfBytes+=2;
//        fwrite(&m_fPNHoldingLevel,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fPNHoldingLevel);
        OffsetOfBytes+=4;
//        fwrite(&m_fPNSettlingTime,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fPNSettlingTime);
        OffsetOfBytes+=4;
//        fwrite(&m_fPNInterPulse,4,1,fpAbfOut);
        LittleEndianIO.writeFloatLittleEndian(ds,m_fPNInterPulse);
        OffsetOfBytes+=4;
//        fwrite(&m_sUnused1954,12,1,fpAbfOut);
        for(i=0;i<12;i++){
            CommonMethods.writeChar8(ds,m_sUnused1954[i]);
        }
        OffsetOfBytes+=12;

    //Writing Group 16;

//        fwrite(&m_nListEnable,2,1,fpAbfOut);
        LittleEndianIO.writeShortLittleEndian(ds,m_nListEnable);
        OffsetOfBytes+=2;
//        fwrite(&m_sUnused1966,80,1,fpAbfOut);
        for(i=0;i<80;i++){
            CommonMethods.writeChar8(ds,m_sUnused1966[i]);
        }
        OffsetOfBytes+=80;

    //	char ch=' ';

    //	for(i=1;i <=m_nNumPointsIgnored;i++)
    //	{
    //		fwrite(&ch,1,1,fpAbfOut);
    //	}

        byte bTemp;
        int nSize=m_vectcPreDataBuffer.size();
        for(i=0;i<nSize;i++)
        {
            bTemp=m_vectcPreDataBuffer.get(i);
//            fwrite(&cTemp,sizeof(char),1,fpAbfOut);
            ds.writeByte(bTemp);
        }

        float fAmp=0.f;
        short nAmp=0;
        float fScale=0.f;
        int nPosition=0;
        int i0;
        if(m_nDataFormat==0)
        {
            for(i=0;i< m_lActualAcqLength/m_nADCNumChannels;i++)
            {
                for(i0=0;i0<m_nADCNumChannels;i0++)
                {
                    fAmp=m_pfData[nPosition]-m_fInstrumentOffset[i0];//3/23/2010
                    fScale=m_vectfScale.get(i0);
                    nAmp=(short) (fAmp/fScale);
//                    fwrite(&nAmp,sizeof(short),1,fpAbfOut);
                    LittleEndianIO.writeShortLittleEndian(ds,nAmp);
                    nPosition++;
                }
            }
        }
        else
        {
            for(i=0;i< m_lActualAcqLength/m_nADCNumChannels;i++)
            {
                for(i0=0;i0<m_nADCNumChannels;i0++)
                {
                    fAmp=m_pfData[nPosition];
//                    fwrite(&fAmp,sizeof(short),1,fpAbfOut);
                    LittleEndianIO.writeFloatLittleEndian(ds,fAmp);
                    nPosition++;
                }
            }
        }

        nSize=m_vectcPostDataBuffer.size();
        for(i=0;i<nSize;i++)
        {
            bTemp=m_vectcPostDataBuffer.get(i);
//            fwrite(&cTemp,sizeof(char),1,fpAbfOut);
            ds.writeByte(bTemp);
        }
        ds.close();
        fs.close();
            //end of WriteData(FILE fpAbfOut, int nDataFormat)
    }
    public void importData(String path){
        if(!FileAssist.fileExists(path))
            path=FileAssist.getFilePath("import a pClamp data file", "","axon binary file or data file", "", true);
        try {
            ReadData(path);
        }catch (FileNotFoundException e1){
            IJ.error(e1+" --- in importData, Abf");
        }catch (IOException e2){
            IJ.error(e2+" --- in importData, Abf");
        }
    }
    public void exportData(String path){
        try {
            WriteData(path,(short)1);
        }catch (FileNotFoundException e1){
            IJ.error(e1+" --- in exportData, Abf");
        }catch (IOException e2){
            IJ.error(e2+" --- in exportData, Abf");
        }
    }
    public void exportDemoAbf(int nChannels, int nSamplesPerChannel){
        int len=nChannels*nSamplesPerChannel,i,j;
        float[] pfData=new float[len];
        int nPosition=0;
        float fV;
        for(i=0;i<nSamplesPerChannel;i++){
            for(j=0;j<nChannels;j++){
                fV=(float)(10*j+Math.random());
                pfData[nPosition]=fV;
                nPosition++;
            }
        }
        loadHeader(nChannels);
        String path=m_sFilePath;
        path=FileAssist.getExtendedFileName(path, "_demo");
        path=FileAssist.changeExt(path, "dat");
        exportAsAbf(pfData,nChannels,nSamplesPerChannel,path);
    }
    public void exportAsAbf(float[] pfData,int nChannels,int nSamplesPerChannel,String path){
        if(m_nADCNumChannels!=nChannels) loadHeader(nChannels);
        m_pfData=pfData;
        m_lActualAcqLength=nChannels*nSamplesPerChannel;
        ResetSampleInterval(1000/nChannels);
        autoScale();
        exportData(path);
    }
    public void loadHeader(int nNumChannels){
         String dir=FileAssist.getUserDir()+"abf header files"+File.separator;
         String name=PrintAssist.ToString(nNumChannels)+"channelHeader.abf";
         String path=dir+name;
         if(!FileAssist.fileExists(path))
             path=FileAssist.getFilePath("import a pClamp data file for the header.", "","axon binary file", "abf", true);
         importData(path);
   }

   void calRanges(){
        boolean first=true;
        m_pfDataRanges=new float[m_nADCNumChannels][2];
        int i,j,nPosition=0;
        float fV,fT;
        for(i=0;i<m_lActualAcqLength/m_nADCNumChannels;i++)
        {
            for(j=0;j<m_nADCNumChannels;j++)
            {
                fV=m_pfData[nPosition];
                nPosition++;
                if(first){
                    m_pfDataRanges[j][0]=fV;
                    m_pfDataRanges[j][1]=fV;
                }else{
                    fT=m_pfDataRanges[j][0];
                    if(fV<fT) m_pfDataRanges[j][0]=fV;
                    fT=m_pfDataRanges[j][1];
                    if(fV>fT) m_pfDataRanges[j][1]=fV;
                }
            }
            first=false;
        }
        m_fMin=m_pfDataRanges[0][0];
        m_fMax=m_pfDataRanges[0][1];
        for(j=0;j<m_nADCNumChannels;j++)
        {
            if(m_pfDataRanges[j][0]<m_fMin) m_fMin=m_pfDataRanges[j][0];
            if(m_pfDataRanges[j][1]>m_fMax) m_fMax=m_pfDataRanges[j][1];
        }
   }
   void calOffset(){
       int j;
        m_fMin=m_pfDataRanges[0][0];
        m_fMax=m_pfDataRanges[0][1];
        for(j=0;j<m_nADCNumChannels;j++)
        {
            m_fInstrumentOffset[j]=(float) (0.5*(m_pfDataRanges[j][0]+m_pfDataRanges[j][1]));
            m_pfDataRanges[j][0]-=m_fInstrumentOffset[j];
            m_pfDataRanges[j][1]-=m_fInstrumentOffset[j];
            if(m_pfDataRanges[j][0]<m_fMin) m_fMin=m_pfDataRanges[j][0];
            if(m_pfDataRanges[j][1]>m_fMax) m_fMax=m_pfDataRanges[j][1];
        }
   }
   public void autoScale(){
       calRanges();
       calOffset();
       float fMax=Math.abs(m_fMax);
       float ft=Math.abs(m_fMin);
       if(fMax<ft) fMax=ft;
       m_fADCRange=(float)1.2*fMax;
       calScales();
       int nT,factor;
       for(int i=0;i<m_nADCNumChannels;i++){
           fMax=Math.abs(m_pfDataRanges[i][1]);
           ft=Math.abs(m_pfDataRanges[i][0]);
           if(ft>fMax) fMax=ft;
           nT=(int) (fMax/m_vectfScale.get(i));
           if(nT==0) continue;
           factor=m_lADCResolution/nT;
           m_fInstrumentScaleFactor[i]*=factor;
       }
       calScales();
   }
   void calScales(){
        m_vectfScale.clear();
        for(int k=0;k<m_nADCNumChannels;k++)
        {
            float fScale=1.0f/(m_fInstrumentScaleFactor[k]);
            if(k==m_nAutosampleADCNum)
            {
                if(m_nAutosampleEnable!=0)
                {
                    fScale/=m_fAutosampleAdditGain;
                }
            }
            fScale*=m_fADCRange;
            fScale/=m_lADCResolution;
            m_vectfScale.add(fScale);
        }
   }
}
