/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package myrbn;

import cytoscape.MyRBN.Common;
import cytoscape.MyRBN.Config;
import cytoscape.MyRBN.Main;
import cytoscape.MyRBN.SettingDialog;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.jocl.CL.*;
import org.jocl.*;
import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import javax.swing.JList;

/**
 * cytoscape.CyMain
 * @author colin
 */
public class MyOpenCL {
    public static final int CPU_PLATFORM = 0;
    public static final int GPU_PLATFORM = 1;
    public static int OPENCL_PLATFORM = CPU_PLATFORM;
    
    public static boolean USE_OPENCL = !true;
    public static final boolean USE_DEBUG = true;    
    private final boolean ENABLE_EXCEPTIONS = true;

    //public static final int MAXNUMDISPLAYEDFBLs =    1000000;//2000;
    
    public static final int MAXNUMFBLs =    2500000;//2000;
    public static final int MAXNUMCPFBLs =  1500000;//700000;//2000;
    //public static final int MAXPATHLEN = 15;//Path.MAXPATHLEN;
    //public static final int NUMFBLEXAMINING = 10000;

    // Attractor constants
    public static final int MAXBITSIZE = 31;//63;//2
    //public static final int MAXNUMPART = 60;    //30;//20;//colin: no limit # of nodes in Robustness computation
    public static final int MAXNODES = 65536;   //3000;//colin: no limit # of nodes in FBL/FFL search
    //public static final int MAXATTSIZE = 250000;
    public static final int MAXATTSTATESIZE = 20;//10;   //10;//20;
    public static final int MAXNETWSTATESIZE = 150;//75;  //100;//colin: no limit # of nodes in Robustness computation
    //public static final int MAXALLPASSEDSTATESIZE = 1500000;
    public static final int MAXTRANSITIONSIZE = 90000;

    public static final int MAXATTSTATESIZE_ONLY = 120;
    
    // The platform, device type and device number that will be used
    private int platformIndex = 0;
    private long deviceType = CL_DEVICE_TYPE_ALL;
    private int deviceIndex = 0;

    // Properties of OPENCL platform
    private int numPlatforms = 0;
    private cl_platform_id platforms[];
    private cl_platform_id platform;
    private int numDevices;
    private cl_device_id devices[];
    private cl_device_id device;

    public static int numPart;
    public static int leftSize;
    public static int numATT;

    public static boolean init_error = false;

    //colin: add for GPU
    public int numFBLs;
    //public static final int WORKGROUPSIZE =    64;
    public static final int WORKGROUPSIZE_ROBUST =    64;//64;
    public static final int CL_DELAYTIME =    300;

    /**/
    
    public MyOpenCL()
    {
        /*if(MyOpenCL.USE_OPENCL)
        {
            reInit();
        }*/
        /*if(MyOpenCL.USE_DEBUG)
        {
            MyOpenCL.USE_OPENCL = true;
            MyOpenCL.OPENCL_PLATFORM = MyOpenCL.GPU_PLATFORM;
            setPlatform(1);
            setDevice(0);
            reInit();
        }*/
    }

    public void setPlatform(int index)
    {
        this.platformIndex = index;
    }

    public void setDevice(int index)
    {
        this.deviceIndex = index;
    }
    
    public void adaptListPlatforms(SettingDialog dlg)
    {
        if(numPlatforms == 0)
        {
            try {
                initPlatforms();
            } catch (Exception ex) {                
            }
        }

        dlg.clearPlatformItem();

        for(int i=0;i<numPlatforms;i++)
        {
            String platformName = getString(platforms[i], CL_PLATFORM_NAME);
            dlg.addPlatformItem(platformName);
        }
    }

    public void adaptListDevices(SettingDialog dlg)
    {     
        try {
            initDevices(platformIndex);
        } catch (Exception ex) {
            numDevices = 0;
        }

        dlg.clearDeviceItem();

        for(int i=0;i<numDevices;i++)
        {
            String deviceName = getString(devices[i], CL_DEVICE_NAME);
            dlg.addDeviceItem(deviceName);
        }
    }

    public void adaptListDeviceInfo(SettingDialog dlg, int index)
    {        
        String info = "";

        String deviceName = getString(devices[index], CL_DEVICE_NAME);
        info += "DEVICE_NAME = " + deviceName + "\n";

        String deviceVendor = getString(devices[index], CL_DEVICE_VENDOR);
        info += "DEVICE_VENDOR = " + deviceVendor + "\n";

        String deviceVer = getString(devices[index], CL_DEVICE_VERSION);
        info += "DEVICE_VERSION = " + deviceVer + "\n";

        int maxComputeUnits = getInt(devices[index], CL_DEVICE_MAX_COMPUTE_UNITS);
        info += "DEVICE_MAX_COMPUTE_UNITS = " + maxComputeUnits + "\n";

        long maxWorkItemDimensions = getLong(devices[index], CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
        info += "DEVICE_MAX_WORK_ITEM_DIMENSIONS = " + maxWorkItemDimensions + "\n";

        long maxWorkItemSizes[] = getLongs(devices[index], CL_DEVICE_MAX_WORK_ITEM_SIZES, 3);
        info += "DEVICE_MAX_WORK_ITEM_SIZES = " + maxWorkItemSizes[0] + "/" + maxWorkItemSizes[1] + "/" + maxWorkItemSizes[2] + "\n";

        long maxWorkGroupSize = getLong(devices[index], CL_DEVICE_MAX_WORK_GROUP_SIZE);
        info += "DEVICE_MAX_WORK_GROUP_SIZE = " + maxWorkGroupSize + "\n";

        dlg.setDeviceInfo(info);
    }
    
    private boolean initPlatforms() throws Exception
    {
        // Obtain the number of platforms
        int numPlatformsArray[] = new int[1];
        clGetPlatformIDs(0, null, numPlatformsArray);
        numPlatforms = numPlatformsArray[0];
        //log("numPlatforms =" + numPlatforms);

        // Obtain a platform ID
        platforms = new cl_platform_id[numPlatforms];
        clGetPlatformIDs(platforms.length, platforms, null);
        //platform = platforms[platformIndex];

        if(USE_DEBUG)
        {
            // Print information of a found platform
            System.out.printf("=== %d OpenCL platform(s) found: ===\n", numPlatforms);
            for (int i=0; i < numPlatforms; i++)
            {
                    System.out.printf("  -- %d --\n", i);

                    String platformProfile = getString(platforms[i], CL_PLATFORM_PROFILE);
                    System.out.println("  PROFILE = " + platformProfile);
                    String platformVer = getString(platforms[i], CL_PLATFORM_VERSION);
                    System.out.printf("  VERSION = %s\n", platformVer);

                    String platformName = getString(platforms[i], CL_PLATFORM_NAME);
                    System.out.printf("  NAME = %s\n", platformName);
                    String platformVendor = getString(platforms[i], CL_PLATFORM_VENDOR);
                    System.out.printf("  VENDOR = %s\n", platformVendor);

                    String platformExt = getString(platforms[i], CL_PLATFORM_EXTENSIONS);
                    System.out.printf("  EXTENSIONS = %s\n", platformExt);
            }
        }

	if (numPlatforms == 0)
		return false;

        return true;
    }

    private boolean initDevices(int index) throws Exception
    {
        platform = platforms[index];
        // Obtain the number of devices for the platform
        if(OPENCL_PLATFORM == CPU_PLATFORM)
        {
            deviceType = CL_DEVICE_TYPE_CPU;
        }
        else
        {
            //System.out.printf(" go tohere CL_DEVICE_TYPE_GPU\n");
            deviceType = CL_DEVICE_TYPE_GPU;
        }

        int numDevicesArray[] = new int[1];
        clGetDeviceIDs(platform, deviceType, 0, null, numDevicesArray);        
        numDevices = numDevicesArray[0];
        
        // Obtain a device ID
        devices = new cl_device_id[numDevices];
        clGetDeviceIDs(platform, deviceType, numDevices, devices, null);
        device = devices[deviceIndex];

        if(USE_DEBUG)
        {
            //System.out.printf("=== %d OpenCL device(s) found on platform:\n", numDevices);
            for (int i=0; i<numDevices; i++)
            {
                //System.out.printf("  -- %d --\n", i);
                String deviceName = getString(devices[i], CL_DEVICE_NAME);
                System.out.printf("  DEVICE_NAME = %s\n", deviceName);

                /*String deviceVendor = getString(devices[i], CL_DEVICE_VENDOR);
                System.out.printf("  DEVICE_VENDOR = %s\n", deviceVendor);
                String deviceVer = getString(devices[i], CL_DEVICE_VERSION);
                System.out.printf("  DEVICE_VERSION = %s\n", deviceVer);

                // CL_DEVICE_MAX_COMPUTE_UNITS
                int maxComputeUnits = getInt(devices[i], CL_DEVICE_MAX_COMPUTE_UNITS);
                System.out.printf("  CL_DEVICE_MAX_COMPUTE_UNITS:\t\t%d\n", maxComputeUnits);

                // CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS
                long maxWorkItemDimensions = getLong(devices[i], CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS);
                System.out.printf("  CL_DEVICE_MAX_WORK_ITEM_DIMENSIONS:\t%d\n", maxWorkItemDimensions);

                // CL_DEVICE_MAX_WORK_ITEM_SIZES
                int maxWorkItemSizes[] = getInts(devices[i], CL_DEVICE_MAX_WORK_ITEM_SIZES, 3);
                System.out.printf("  CL_DEVICE_MAX_WORK_ITEM_SIZES:\t\t%d / %d / %d \n",
                        maxWorkItemSizes[0], maxWorkItemSizes[1], maxWorkItemSizes[2]);

                // CL_DEVICE_MAX_WORK_GROUP_SIZE
                long maxWorkGroupSize = getLong(devices[i], CL_DEVICE_MAX_WORK_GROUP_SIZE);
                System.out.printf("  CL_DEVICE_MAX_WORK_GROUP_SIZE:\t\t%d\n", maxWorkGroupSize);*/
            }
        }

        if (numDevices == 0)
		return false;

        return true;
    }

    private boolean initOpenCL() throws Exception
    {
        // Enable exceptions and subsequently omit error checks in this sample
        CL.setExceptionsEnabled(ENABLE_EXCEPTIONS);

        if(numPlatforms == 0)
        {
            if(initPlatforms() == false)
                return false;
        }

        if(initDevices(platformIndex) == false)
            return false;

        return true;
    }

    public void reInit()
    {
        try
        {
            MyOpenCL.init_error = false;
            initOpenCL();
        }
        catch(Exception ex)
        {
            MyOpenCL.init_error = true;
            MyOpenCL.USE_OPENCL = false;
            numDevices = 0;
        }        
    }

    private void log(String str)
    {
        System.out.println(str);
    }

    public int getNumDevices()
    {
        return numDevices;
    }
    
    public int [] convertInterationArrToIntArr(ArrayList<Interaction> rndina)
    {
        int length = rndina.size();
        int [] intArr = new int[length*3+1];

        for(int i=0; i<length; i++)
        //for(int j=0; j<3; j++)
        {            
            Interaction intA = rndina.get(i);
            intArr[i*3 + 0] = Common.nodeIDsArr.indexOf(intA.nInfos[0]);// intA.nInfos[0];//source
            intArr[i*3 + 1] = Common.nodeIDsArr.indexOf(intA.nInfos[1]);// intA.nInfos[1];//dest
            intArr[i*3 + 2] = intA.nInfos[2];// type
        }

        intArr[length*3] = -1;//0;// end of array

        /*for(int i=0; i<length-1; i++)
        {
         * 
            if(intArr[i*3 + 0] == intArr[(i+1)*3 + 0] && intArr[i*3 + 1] == intArr[(i+1)*3 + 1])
            {
                System.out.printf("%s %s %d %d <> ",Common.indexIDs.get(intArr[i*3 + 0]),Common.indexIDs.get(intArr[i*3 + 1]),
                        intArr[i*3 + 2],intArr[(i+1)*3 + 2]);
            }
        }
        System.out.println("");*/

        return intArr;
    }

    /*public int [] convertInterationArrToIntArr4GPU(ArrayList<Interaction> rndina, ArrayList<Integer> indexSelectedNodes, long [] size)
    {
        int length = rndina.size();
        int realLength = (int)(size[0]*size[1]*4);
        int [] intArr = new int[realLength];
        int temp;

        for(int i=0; i<realLength; i++)
        {
            intArr[i] = 0;
        }
        
        for(int i=0; i<length; i++)        
        {
            Interaction intA = rndina.get(i);
            temp=i*4;
            intArr[temp + 0] = Common.nodeIDsArr.indexOf(intA.nInfos[0]);// intA.nInfos[0];//source
            intArr[temp + 1] = Common.nodeIDsArr.indexOf(intA.nInfos[1]);// intA.nInfos[1];//dest
            intArr[temp + 2] = intA.nInfos[2];// type
            intArr[temp + 3] = indexSelectedNodes.indexOf(intA.nInfos[1]);// index destnode in selectednodes
            if(intArr[temp + 3]==-1)
                intArr[temp + 3] = MAXNODES;
        }
        intArr[length*4] = -1;//0;// end of array
        return intArr;
    }*/
    
    private int [] createIndexNodeInSelectedNodesArr(ArrayList<Integer> indexSelectedNodes)
    {        
        int numNode = Common.nodeIDsArr.size();

        int [] indexNodeInSelectedNodesArr = new int[numNode];
        int i;

        for(i=0;i<numNode;i++)
        {            
            indexNodeInSelectedNodesArr[i] = indexSelectedNodes.indexOf(Common.nodeIDsArr.get(i));
            if(indexNodeInSelectedNodesArr[i] == -1)
                indexNodeInSelectedNodesArr[i] = MAXNODES;
        }

        return indexNodeInSelectedNodesArr;
    }

    private int [][] createFirstIndexSrcNodeInEdgesArr(ArrayList<Interaction> rndina, int coff)
    {
        int numNode = Common.nodeIDsArr.size();
        int numEdge = rndina.size();
        
        int [][] firstIndexSrcNodeInEdgesArr = new int[2][numNode];
        int i, j;
        int indexSrcNode;
        
        for(i=0;i<numNode;i++)
        {
            firstIndexSrcNodeInEdgesArr[0][i] = -1;
            firstIndexSrcNodeInEdgesArr[1][i] = 0;
        }
        
        for(i=0;i<numEdge;i++)
        {
            Interaction intA = rndina.get(i);
            indexSrcNode = Common.nodeIDsArr.indexOf(intA.nInfos[0]);// src node

            if(firstIndexSrcNodeInEdgesArr[0][indexSrcNode] == -1)
                firstIndexSrcNodeInEdgesArr[0][indexSrcNode] = i*coff;

            j=i+1;
            while(j<numEdge)
            {
                intA = rndina.get(j);
                int tempI = Common.nodeIDsArr.indexOf(intA.nInfos[0]);// src node
                if(tempI != indexSrcNode)
                    break;

                j++;
            }

            firstIndexSrcNodeInEdgesArr[1][indexSrcNode] = (j-i);
            i = j-1;
        }

        return firstIndexSrcNodeInEdgesArr;
    }
    public int [] convertNodeClusterIDToIntArr(ArrayList<Node> nodes)
    {
        int length = nodes.size();
       // int [] intArr = new int[length*1 + 1];
        int [] intArr = new int[length];
        for(int i=0; i<length; i++)
        {
            Node node = nodes.get(i);
            //intArr[i*2 + 0] = node.NodeState;
            //????
            intArr[i] = node.ClusterID;// cluster id of each node
        }

        //intArr[length*1] = -1;// end of array?? tại sao

        return intArr;
    }
    public int [] convertIntegerArrToIntArr(ArrayList<Integer> posarr)
    {
        int length = posarr.size();
        int [] intArr = new int[length];

        for(int i=0; i<length; i++)
        {
            Integer intA = posarr.get(i);
            intArr[i] = intA.intValue();
        }

        return intArr;
    }

    public byte [] convertToByteArr(ArrayList<Byte> arrByte)
    {
        int length = arrByte.size();
        byte [] arr = new byte[length];

        for(int i=0; i<length; i++)
        {
            Byte b = arrByte.get(i);
            arr[i] = b.byteValue();
        }

        return arr;
    }

    public byte [] createStates(int numWorkItems, int numState, int numNode)
    {
        int numBytes = numWorkItems*numState*numNode;
        byte [] resultArr = new byte[numBytes];

        return resultArr;
    }

    public int [] createStates4CPU(int numWorkItem, int numNode, int numPart)
    {
        int num = numWorkItem*(MAXNETWSTATESIZE*numPart + 2*MAXATTSTATESIZE*numPart + 2*numNode + numPart);
        System.out.println("createStates4CPU=" + (num*4/1000));
        int [] resultArr = new int[num];

        return resultArr;
    }
    public int [] createStatesOutModule4CPU(int numWorkItem, int numNode, int numPart)
    {
        //???
        int num = numWorkItem*(MAXNETWSTATESIZE*numPart + 2*MAXATTSTATESIZE*numPart  + 3*numNode +numNode + numPart);
       // System.out.println("createStates4CPU=" + (num*4/1000));
        int [] resultArr = new int[num];
        return resultArr;
    }
    public int [] createStatesInModule4CPU(int numWorkItem, int numNode, int numPart)
    {
        //???
        int num = numWorkItem*(MAXNETWSTATESIZE*numPart + 2*MAXATTSTATESIZE*numPart  + 3*numNode +numNode + numPart);
       // System.out.println("createStates4CPU=" + (num*4/1000));
        int [] resultArr = new int[num];
        return resultArr;
    }
    public int [][] splitIntegerArrToIntArr(ArrayList<Integer> posarr, ArrayList<Integer> parts)
    {        
        int [][] intArr = new int[parts.size()][];
        int index=0;

        int size;

        for(int i=0;i<parts.size();i++)
        {
            size = parts.get(i);
            /*int leftSize = size - size/WORKGROUPSIZE*WORKGROUPSIZE;
            int newSize = size;
            if(leftSize > 0)
                newSize += WORKGROUPSIZE - leftSize;*/
            
            intArr[i] = new int[size];
            
            for(int j=0; j<size; j++)
            {
                Integer intA = posarr.get(index++);
                intArr[i][j] = intA.intValue();
            }

            /*for(int j=size; j<newSize; j++)
                intArr[i][j] = -1;*/
        }
        return intArr;
    }
    
    public int [] createResultArr(int maxPathLen)
    {
//            public int IncomingTypeOfStartNode;
//            public int type;
//            public int length;//one less than number of nodes
//            public ArrayList<String> nodes; //length+1
//            public ArrayList<Integer> types; //length
        int numBytesInStruct = (1+maxPathLen+1);  //(3+2*MAXPATHLEN+1);
        int num = MAXNUMFBLs*numBytesInStruct + 1;
        int [] resultArr = new int[num];

        // if start number in a struct = 2, result array should end ????        
        for(int j=0;j<num;j++)
        {
            resultArr[j] = -1;
        }          
        
        return resultArr;
    }

    /*public short [][] create2DResultArr(int numParts)
    {
//            public int IncomingTypeOfStartNode;
//            public int type;
//            public int length;//one less than number of nodes
//            public ArrayList<String> nodes; //length+1
//            public ArrayList<Integer> types; //length
        int numBytesInStruct = (1+MAXPATHLEN+1);  //(3+2*MAXPATHLEN+1);
            // remove length, ArrayList<Integer> types
        int num = MAXNUMFBLs*numBytesInStruct + 1;
        short [][] resultArr = new short[numParts][num];

        // if start number in a struct = 2, result array should end ????  
        for(int i=0; i<numParts; i++)        
            for(int j=0;j<num;j++)
            {
                resultArr[i][j] = -1;
            }        

        return resultArr;
    }*/
    
    public int [] createResultArr4CPFBL(int maxPathLen)
    {
//            public int i;//index of first FBL
//            public int j;//index of second FBL
//            public ArrayList<String> nodes; //length+1
        int numBytesInStruct = (2+maxPathLen+1);
        int [] resultArr = new int[MAXNUMCPFBLs*numBytesInStruct + 1];
        
        return resultArr;
    }
    
    private int [] createFBLArr(ArrayList<FBL> AllDistinctFBLs, boolean coupled, int maxPathLen)
    {
        int numBytesInStruct = (1 + maxPathLen+1);
                //first int is length
        
        int numFBLs = AllDistinctFBLs.size();
        int [] fblArr = new int[numFBLs*numBytesInStruct + 1];
        int numNodes;
        int i,j;
        
        for(i=0;i<numFBLs;i++)
        {
            FBL fbl = AllDistinctFBLs.get(i);
            numNodes = fbl.nodes.size();
            if(coupled)
                fblArr[i*numBytesInStruct] = numNodes;
            else
                fblArr[i*numBytesInStruct] = fbl.type;
                    
            for(j=0;j<numNodes-1;j++)
            {
                String nodeID = fbl.nodes.get(j);
                fblArr[i*numBytesInStruct + 1 + j] = Common.nodeIDsArr.indexOf(Common.stringIDs.get(nodeID));
            }
            
            fblArr[i*numBytesInStruct + 1 + (numNodes-1)] = -1;
        }
        fblArr[numFBLs*numBytesInStruct] = 0;   //end array
                
        return fblArr;
    }

    // Attractors create arr section
    public int [] convertInterationArrToIntArr4ATT(ArrayList<Interaction> rndina, ArrayList<Node> nodes)
    {
        int length = rndina.size();
        int [] intArr = new int[length*3 + 3];
        int indexNodeSrcInNodeArr;
        int indexNodeDstInNodeArr;
        
        for(int i=0; i<length; i++)
        {
            Interaction intA = rndina.get(i);
            //intArr[i*4 + 0] = intA.nInfos[0];
            //intArr[i*3 + 0] = intA.nInfos[1];
            intArr[i*3 + 0] = intA.nInfos[2];//type of interation

            indexNodeSrcInNodeArr = Common.searchUsingBinaryGENE(intA.NodeSrc, nodes);
            indexNodeDstInNodeArr = Common.searchUsingBinaryGENE(intA.NodeDst, nodes);
            intArr[i*3 + 1] = indexNodeSrcInNodeArr;
            intArr[i*3 + 2] = indexNodeDstInNodeArr;

            //System.out.println(intA.NodeSrc + " " + intA.InteractionType + " " + intA.NodeDst);
        }

        intArr[length*3] = 2;// end of array

        return intArr;
    }

    private ArrayList<Interaction> reduceEdgeArr(ArrayList<Interaction> rndina)
    {
        ArrayList<Interaction> reduceArr = new ArrayList<Interaction>();
        int length = rndina.size();

        for(int i=0; i<length; i++)
        {
            Interaction intA = rndina.get(i);
            if(intA.nInfos[2] != 0)
                reduceArr.add(intA);
        }

        return reduceArr;
    }
    
    public int [] convertInterationArrToIntArr4ATT_GPU(ArrayList<Interaction> rndina, ArrayList<Node> nodes, long [] size)
    {
        int length = rndina.size();
        int realLength = (int)(size[0]*size[1]*4);
        int [] intArr = new int[realLength];        
        int temp;
        
        for(int i=0; i<realLength; i++)
        {
            intArr[i] = 0;
        }
        
        int indexNodeSrcInNodeArr;
        int indexNodeDstInNodeArr;

        for(int i=0; i<length; i++)
        {
            temp = i*4;
            Interaction intA = rndina.get(i);            
            intArr[temp] = intA.nInfos[2];//type of interation

            indexNodeSrcInNodeArr = Common.searchUsingBinaryGENE(intA.NodeSrc, nodes);
            indexNodeDstInNodeArr = Common.searchUsingBinaryGENE(intA.NodeDst, nodes);
            intArr[temp + 1] = indexNodeSrcInNodeArr;
            intArr[temp + 2] = indexNodeDstInNodeArr;

            //System.out.println(intA.NodeSrc + " " + intA.InteractionType + " " + intA.NodeDst);
        }

        intArr[length*4] = 2;// end of array

        return intArr;
    }

    private int [][] createFirstIndexDstNodeInEdgesArr(ArrayList<Interaction> rndina, ArrayList<Node> nodes)
    {
        int size = nodes.size();
        int left = size - ((int)(size/WORKGROUPSIZE_ROBUST))*WORKGROUPSIZE_ROBUST;
        if(left > 0)
            left = MyOpenCL.WORKGROUPSIZE_ROBUST - left;
        int newsize = size+left;
        System.out.printf("colin GPU: OpenCL number of new nodes size = %d\n", newsize);
        
        int numEdge = rndina.size();

        int [][] firstIndexSrcNodeInEdgesArr = new int[2][newsize];
            //0: index
            //1: number of edge
        int i,j;
        int indexDstNode;

        for(i=0;i<newsize;i++)
        {
            firstIndexSrcNodeInEdgesArr[0][i] = -1;
            firstIndexSrcNodeInEdgesArr[1][i] = 0;
        }

        for(i=0;i<numEdge;i++)
        {
            Interaction intA = rndina.get(i);
            indexDstNode = Common.searchUsingBinaryGENE(intA.NodeDst, nodes);
            
            firstIndexSrcNodeInEdgesArr[0][indexDstNode] = (i*4);

            j=i+1;
            while(j<numEdge)
            {
                intA = rndina.get(j);
                int tempI = Common.searchUsingBinaryGENE(intA.NodeDst, nodes);
                if(tempI != indexDstNode)
                    break;
                
                j++;
            }

            firstIndexSrcNodeInEdgesArr[1][indexDstNode] = (j-i);
            i = j-1;
        }

        return firstIndexSrcNodeInEdgesArr;
    }
    
    public byte [] convertNodeArrToIntArr(ArrayList<Node> nodes)
    {
        int length = nodes.size();
        byte [] intArr = new byte[length*1 + 1];

        for(int i=0; i<length; i++)
        {
            Node node = nodes.get(i);
            //intArr[i*2 + 0] = node.NodeState;
            intArr[i] = 0;//(byte)node.NodeFunc;//colin: add Nested canalyzing function
        }

        intArr[length*1] = 0;// end of array

        return intArr;
    }

    public int [] createATTResultArr(int numStates)
    {
        int numBytesInStruct = MAXATTSTATESIZE*numPart;
        int [] resultArr = new int[numStates*numBytesInStruct + 1];

        //System.out.println("colin: resultArr.length=" + resultArr.length);
        for(int i=0;i<resultArr.length;i++)
            resultArr[i] = -1;
        
        return resultArr;
    }

    public int [] createATTResultArr_only(int numStates)
    {
        int numBytesInStruct = MAXATTSTATESIZE_ONLY*numPart;
        int [] resultArr = new int[numStates*numBytesInStruct + 1];

        //System.out.println("colin: resultArr.length=" + resultArr.length);
        for(int i=0;i<resultArr.length;i++)
            resultArr[i] = -1;
        
        return resultArr;
    }
    
    /*public long [] createAllPassedResultArr()
    {
        int numBytesInStruct = numPart;
        long [] resultArr = new long[MAXALLPASSEDSTATESIZE*numBytesInStruct + 1];

        return resultArr;
    }*/

    public int [] createTransResultArr(int numStates)
    {
        int numBytesInStruct = 2*MAXNETWSTATESIZE*numPart;
        int [] resultArr = new int[numStates*numBytesInStruct + 1];

        for(int i=0;i<resultArr.length;i++)
            resultArr[i] = -1;
        
        return resultArr;
    }

    public long [] convertLongArrTolongArr(ArrayList<Long> posarr)
    {
        int length = posarr.size();
        long [] intArr = new long[length];

        for(int i=0; i<length; i++)
        {
            Long intA = posarr.get(i);
            intArr[i] = intA.longValue();
        }

        return intArr;
    }

    public int [] createIndexNodes4Robustness(ArrayList<Integer> posSelectedNodes)
    {
        int size = posSelectedNodes.size();
        int left = size - ((int)(size/WORKGROUPSIZE_ROBUST))*WORKGROUPSIZE_ROBUST;
        if(left > 0)
            left = MyOpenCL.WORKGROUPSIZE_ROBUST - left;
        int newsize = size+left;
        
        int [] intArr = new int[newsize];

        for(int i=0; i<size; i++)
        {
            Integer intA = posSelectedNodes.get(i);
            intArr[i] = intA.intValue();
        }

        for(int i=size; i<newsize; i++)
        {
            intArr[i] = -1;
        }
        return intArr;
    }
    /**/
    
    public void runExample()
    {
        // Create input- and output data
        int n = 100;//(int)Math.pow(2, 10);
        float srcArrayA[] = new float[n];
        float srcArrayB[] = new float[n];
        float dstArray[] = new float[n];
        
        for (int i=0; i<n; i++)
        {
            srcArrayA[i] = i;
            srcArrayB[i] = i;
        }
        Pointer srcA = Pointer.to(srcArrayA);
        Pointer srcB = Pointer.to(srcArrayB);
        Pointer dst = Pointer.to(dstArray);        

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);

        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[3];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR,
            Sizeof.cl_float * n, srcA, null);
        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_USE_HOST_PTR,
            Sizeof.cl_float * n, srcB, null);
        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_float * n, null, null);

        // Create the program from the source code
        String path = "res/kernels/unitTest.cl";
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "sampleKernel", null);

        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0,
            Sizeof.cl_mem, Pointer.to(memObjects[0]));
        clSetKernelArg(kernel, 1,
            Sizeof.cl_mem, Pointer.to(memObjects[1]));
        clSetKernelArg(kernel, 2,
            Sizeof.cl_mem, Pointer.to(memObjects[2]));

        // Set the work-item dimensions
        //long l = (long)Math.pow(2, 10) + 1;
        long global_work_size[] = new long[]{n};
        long local_work_size[] = new long[]{1};//1024

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, null, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
            n * Sizeof.cl_float, dst, 0, null, null);

        // Release kernel, program, and memory objects
        clReleaseMemObject(memObjects[0]);
        clReleaseMemObject(memObjects[1]);
        clReleaseMemObject(memObjects[2]);
        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        
        System.out.println("Result: "+java.util.Arrays.toString(dstArray));
    }

    public void findAllFBLsOf1NodeWithSpecifiedLength(ArrayList<Integer> indexSelectedNodes, ArrayList<Interaction> rndina,
            ArrayList<Integer> posDstArr, /*ArrayList<Integer> posNodesInDstArr,*/ int length, int maximal){
        //remove some reduandant paths
        int [] indexNodeInSelectedNodeArr = createIndexNodeInSelectedNodesArr(indexSelectedNodes);
        long [] size = null;
        int coeff = 3;
        int [] rndinaIntArr = null;
        /*if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
            coeff = 4;
            size = calculateSizeOfImage2D(rndina.size());
            System.out.printf("colin: size log2 = %d %d\n", size[0],size[1]);
            rndinaIntArr = convertInterationArrToIntArr4GPU(rndina, indexSelectedNodes, size);
        }
        else*/
            rndinaIntArr = convertInterationArrToIntArr(rndina);
        for(int i=0; i<posDstArr.size(); i++)
        {
            int pos = posDstArr.get(i)*coeff;
            int indexExamineNode = rndinaIntArr[pos];
            int indexNodeDst = rndinaIntArr[pos+1];
            int indexArr = indexNodeInSelectedNodeArr[indexExamineNode];
            // colin: remove redundant paths from begin
            if(indexNodeDst == indexExamineNode || indexNodeInSelectedNodeArr[indexNodeDst] < indexArr){
                posDstArr.remove(i);
                i--;
            }
        }
        int [] posarrInt = convertIntegerArrToIntArr(posDstArr);
        int [][] firstIndexSrcNodeInEdgesArr = createFirstIndexSrcNodeInEdgesArr(rndina, coeff);
        int maxOutNeighbors = 0;
        for(int i=0; i<Common.nodeIDsArr.size(); i++)
        {
            if(maxOutNeighbors < firstIndexSrcNodeInEdgesArr[1][i])
                maxOutNeighbors = firstIndexSrcNodeInEdgesArr[1][i];
        }

        int numWorkItems = posarrInt.length;
        int [] indexExamineNodes = new int[numWorkItems];
        for(int i=0; i<numWorkItems; i++)
        {
            int pos = posarrInt[i]*coeff;
            int indexExamineNode = rndinaIntArr[pos];
            indexExamineNodes[i] = indexExamineNode;
        }                

        // create global buffer memory
        int maxPathLen = length + 1;
        int stackSize = maxOutNeighbors*maxPathLen;
        int bufSize = 1 + 2*stackSize + 3*maxPathLen;
        int [] g_buff = new int[2 + numWorkItems*bufSize];
            //g_buff[0] for the index of saved FBL
            //g_buff[1] for STOP condition, default is STOP
        g_buff[0] = -1;
        g_buff[1] = 1;
        for(int i=0; i< numWorkItems; i++)
        {
            g_buff[2+i*bufSize] = 0;//stack index
            g_buff[2+i*bufSize + 1] = posarrInt[i]*coeff;   //stack value
            g_buff[2+i*bufSize + 1 + stackSize] = 0;    //depth value
        }        
        System.out.println("colin: OpenCL number of items = " + numWorkItems + "/MaxOutDegree=" + maxOutNeighbors);
        System.out.println("colin: OpenCL number of g_buff size = " + g_buff.length*4/1000000);  
        // create result array
        int [] resultArr = createResultArr(maxPathLen);
        System.out.println("colin: OpenCL resultArr size = " + resultArr.length*4/1000000);
        
        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        //colin: add for GPU
        long flagMem = CL_MEM_USE_HOST_PTR;
        if(OPENCL_PLATFORM == GPU_PLATFORM)
            flagMem = CL_MEM_COPY_HOST_PTR;
        /**/
        
         // Create the program from the source code
        String path = "res/kernels/fbl.cl";
        /*if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
            path = "res/kernels/fblG.cl";
        }*/
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);       

        // create image2d_t for edges arr
        /*cl_image_format imageFormat = new cl_image_format();
        imageFormat.image_channel_order = CL_RGBA;
        imageFormat.image_channel_data_type = CL_SIGNED_INT32;*/
                
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
        
        // Set the work-item dimensions
        int left = numWorkItems - ((int)(numWorkItems/WORKGROUPSIZE_ROBUST))*WORKGROUPSIZE_ROBUST;
        if(left > 0) left = MyOpenCL.WORKGROUPSIZE_ROBUST - left;
        int newNumWorkItems = numWorkItems+left;
        long global_work_size[] = new long[]{newNumWorkItems};
        long local_work_size[] = new long[]{WORKGROUPSIZE_ROBUST};
        System.out.println("colin: OpenCL newNumWorkItems=" + newNumWorkItems);

        boolean stop = false;
        int countExecution = 0;
        cl_mem memResults = null, mem_gbuff = null;
        /*if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        mem_gbuff = clCreateBuffer(context,
            CL_MEM_READ_WRITE|flagMem,
            Sizeof.cl_int * g_buff.length, Pointer.to(g_buff), null);
        memResults = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * resultArr.length, null, null);
        }
        else
        {
        memResults = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * resultArr.length, Pointer.to(resultArr), null);
        }*/
        
        while(!stop)
        {
        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[4];
        //cl_mem mem_gbuffCPU = null;
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * firstIndexSrcNodeInEdgesArr[0].length, Pointer.to(firstIndexSrcNodeInEdgesArr[0]), null);

        /*if(OPENCL_PLATFORM == GPU_PLATFORM)
        {        
            memObjects[1] = clCreateImage2D(
                context, CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
                new cl_image_format[]{imageFormat}, size[0], size[1],
                0, Pointer.to(rndinaIntArr), null);
        }
        else*/
        {
            memObjects[1] = clCreateBuffer(context,
                CL_MEM_READ_ONLY | flagMem,
                Sizeof.cl_int * rndinaIntArr.length, Pointer.to(rndinaIntArr), null);
        }

        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * indexExamineNodes.length, Pointer.to(indexExamineNodes), null);

        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * indexNodeInSelectedNodeArr.length, Pointer.to(indexNodeInSelectedNodeArr), null);

        /*if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|flagMem,
            Sizeof.cl_int * g_buff.length, Pointer.to(g_buff), null);
        }
        else
        {
        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * g_buff.length, Pointer.to(g_buff), null);
        }*/

        g_buff[1] = 1;
        if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
            if(mem_gbuff == null)
                mem_gbuff = clCreateBuffer(context,
                    CL_MEM_READ_WRITE|flagMem,
                    Sizeof.cl_int * g_buff.length, Pointer.to(g_buff), null);
            else
                clEnqueueWriteBuffer(commandQueue, mem_gbuff, CL_TRUE, 0,
                    2 * Sizeof.cl_int, Pointer.to(g_buff), 0, null, null);
            if(memResults == null)
                memResults = clCreateBuffer(context,
                    CL_MEM_READ_WRITE,
                    Sizeof.cl_int * resultArr.length, null, null);
        }
        else
        {
        mem_gbuff = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * g_buff.length, Pointer.to(g_buff), null);
        memResults = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * resultArr.length, Pointer.to(resultArr), null);
        }
        /*if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        clEnqueueWriteBuffer(commandQueue, mem_gbuff, CL_TRUE, 0,
            2 * Sizeof.cl_int, Pointer.to(g_buff), 0, null, null);
        }
        else
        {
        mem_gbuffCPU = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * g_buff.length, Pointer.to(g_buff), null);
        }*/
        
        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "findAllFBLsOf1NodeWithSpecifiedLength", null);

        // Set the arguments for the kernel
        for(int j=0; j<memObjects.length;j++)
            clSetKernelArg(kernel, j, Sizeof.cl_mem, Pointer.to(memObjects[j]));
        int iPara = memObjects.length;
        //if(OPENCL_PLATFORM == GPU_PLATFORM)
        clSetKernelArg(kernel, iPara++, Sizeof.cl_mem, Pointer.to(mem_gbuff));
        //else
        //    clSetKernelArg(kernel, iPara++, Sizeof.cl_mem, Pointer.to(mem_gbuffCPU));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_mem, Pointer.to(memResults));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{length}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{maximal}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{numWorkItems}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{stackSize}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        
        /*if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
            clSetKernelArg(kernel, iPara++,
                Sizeof.cl_int, Pointer.to(new int[]{(int)size[2]}));
            clSetKernelArg(kernel, iPara++,
                Sizeof.cl_int, Pointer.to(new int[]{(int)size[0]-1}));
        }*/
                                    
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);        

        // Read the output data
        int [] temp = new int[2];
        if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        clEnqueueReadBuffer(commandQueue, mem_gbuff, CL_TRUE, 0,
            2 * Sizeof.cl_int, Pointer.to(temp), 0, null, null);
        }
        else
        {
        clEnqueueReadBuffer(commandQueue, mem_gbuff, CL_TRUE, 0,
            g_buff.length * Sizeof.cl_int, Pointer.to(g_buff), 0, null, null);
        }
        // Release kernel, program, and memory objects
        for(int j=0; j<memObjects.length; j++)
            clReleaseMemObject(memObjects[j]);
        clReleaseKernel(kernel);
        if(OPENCL_PLATFORM == CPU_PLATFORM){
            temp[0] = g_buff[0];
            temp[1] = g_buff[1];
        }
        
        //colin: add for GPU
        this.numFBLs = temp[0] + 1;
        g_buff[0] = temp[0];
        if(this.numFBLs > MAXNUMFBLs)
        {
            this.numFBLs = MAXNUMFBLs;
            clEnqueueReadBuffer(commandQueue, memResults, CL_TRUE, 0,
                resultArr.length * Sizeof.cl_int, Pointer.to(resultArr), 0, null, null);        
            MyRBN.adaptResultArrToPaths(resultArr, maxPathLen, true);
            g_buff[0] = -1;//reset index of saved FBLs to continue the search
        }        
        else
        {
            if(temp[1] == 1)//stop
            {
                clEnqueueReadBuffer(commandQueue, memResults, CL_TRUE, 0,
                    resultArr.length * Sizeof.cl_int, Pointer.to(resultArr), 0, null, null);        
                MyRBN.adaptResultArrToPaths(resultArr, maxPathLen, true);
            }
        }
        
        if(OPENCL_PLATFORM == CPU_PLATFORM)
        {
            clReleaseMemObject(mem_gbuff);
            clReleaseMemObject(memResults);
        }
        
        ++countExecution;
        if(temp[1] == 1) stop = true;
        }

        //clEnqueueReadBuffer(commandQueue, memResults, CL_TRUE, 0,
        //    resultArr.length * Sizeof.cl_int, Pointer.to(resultArr), 0, null, null);        
        //clReleaseMemObject(memResults);
        this.numFBLs = MyRBN.Paths.size();
        System.out.println("colin: Total found FBLs/countExecution = " + this.numFBLs + "/" + countExecution);
        //MyRBN.adaptResultArrToPaths(resultArr, maxPathLen);
        
        if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
            clReleaseMemObject(mem_gbuff);
            clReleaseMemObject(memResults);
        }
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        // release memory
        for(int i=0; i<firstIndexSrcNodeInEdgesArr.length; i++)
            firstIndexSrcNodeInEdgesArr[i] = null;
        firstIndexSrcNodeInEdgesArr = null;
        rndinaIntArr = null;
        indexNodeInSelectedNodeArr = null;
        indexExamineNodes = null;
        posarrInt = null;        
        g_buff = null;
        resultArr = null;
        System.gc();
        // end release
        
        return;
    }

    public int [] findAllFFL(ArrayList<Integer> indexNodes, ArrayList<Integer> destNodes,
            ArrayList<Interaction> rndina, int length, int maximal){
        int [] rndinaIntArr = convertInterationArrToIntArr(rndina);
        int lengthIntA = rndinaIntArr.length;
        int numCoupledNodes = indexNodes.size()*destNodes.size();
        int [][] firstIndexSrcNodeInEdgesArr = createFirstIndexSrcNodeInEdgesArr(rndina, 3);
        System.out.printf("colin: OpenCL number of items = %d x %d\n", indexNodes.size(), destNodes.size());

        int maxOutNeighbors = 0;
        for(int i=0; i<Common.nodeIDsArr.size(); i++)
        {
            if(maxOutNeighbors < firstIndexSrcNodeInEdgesArr[1][i])
                maxOutNeighbors = firstIndexSrcNodeInEdgesArr[1][i];
        }

        final int MAX_PROCESS_PATHS = 2048;
        int numWorkItems = numCoupledNodes;
        if(numWorkItems > MAX_PROCESS_PATHS) numWorkItems = MAX_PROCESS_PATHS;
        // create global buffer memory
        int maxPathLen = length + 1;
        int stackSize = maxOutNeighbors*maxPathLen;
        int bufSize = 1 + 2*stackSize + 3*maxPathLen;
        int [] g_buff = new int[2 + numWorkItems*bufSize];
            //g_buff[0] for the index of saved FBL
            //g_buff[1] for STOP condition, default is STOP
        g_buff[0] = -1;
        System.out.println("colin: OpenCL number of items = " + numWorkItems + "/MaxOutDegree=" + maxOutNeighbors);
        System.out.println("colin: OpenCL number of g_buff size = " + g_buff.length*4/1000000);

        int [] indexNodesInt = new int[numCoupledNodes];//convertIntegerArrToIntArr(indexNodes);
        int [] destNodesInt = new int[numCoupledNodes];//convertIntegerArrToIntArr(destNodes);
        System.out.println("colin: OpenCL indexNodes size = " + 2*indexNodesInt.length*4/1000000);
        int temp = 0;
        for(int i=0; i<indexNodes.size(); i++)
        for(int j=0; j<destNodes.size(); j++)
        {
            indexNodesInt[temp] = indexNodes.get(i);
            destNodesInt[temp++] = destNodes.get(j);
        }
        
        // create result array
        int [] resultArr = createResultArr(maxPathLen);
        int lengthResult = resultArr.length;
        Pointer pResult = Pointer.to(resultArr);
        System.out.println("colin: OpenCL resultArr size = " + resultArr.length*4/1000000);

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Create the program from the source code
        String path = "res/kernels/ffl.cl";
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        //colin: add for GPU
        long flagMem = CL_MEM_USE_HOST_PTR;
        if(OPENCL_PLATFORM == GPU_PLATFORM)
            flagMem = CL_MEM_COPY_HOST_PTR;
        /**/
        cl_mem memResults = null;
        if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        memResults = clCreateBuffer(context, CL_MEM_READ_WRITE,
            Sizeof.cl_int * lengthResult, null, null);
        }
        else
        {
        memResults = clCreateBuffer(context, CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * lengthResult, pResult, null);
        }
        // Set the work-item dimensions
        long global_work_size[] = new long[]{MAX_PROCESS_PATHS};
        long local_work_size[] = new long[]{WORKGROUPSIZE_ROBUST};//1024
        
        int numIteration = numCoupledNodes / MAX_PROCESS_PATHS;
        int left = numCoupledNodes - numIteration*MAX_PROCESS_PATHS;
        if (left > 0)
            ++numIteration;
        int pass = 0;
        int countExecution = 0;
        for(int n=0; n<numIteration; n++)
        {
        boolean stop = false;
        for(int i=0; i< numWorkItems; i++)
        {
            g_buff[2+i*bufSize] = 0;//stack index
            g_buff[2+i*bufSize + 1] = -1;   //stack value for the examined node = -1
            g_buff[2+i*bufSize + 1 + stackSize] = 0;    //depth value
        }
        
        while(!stop)
        {
        g_buff[1] = 1;
        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[5];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * indexNodesInt.length, Pointer.to(indexNodesInt), null);

        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * destNodesInt.length, Pointer.to(destNodesInt), null);

        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * firstIndexSrcNodeInEdgesArr[0].length, Pointer.to(firstIndexSrcNodeInEdgesArr[0]), null);
        
        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * lengthIntA, Pointer.to(rndinaIntArr), null);                
       
        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|flagMem,
            Sizeof.cl_int * g_buff.length, Pointer.to(g_buff), null);                                            

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "findAllFFLs", null);
        // Set the arguments for the kernel
        for(int i=0; i<memObjects.length; i++)
            clSetKernelArg(kernel, i,Sizeof.cl_mem, Pointer.to(memObjects[i]));                    
        int iPara = memObjects.length;        
        clSetKernelArg(kernel, iPara++, Sizeof.cl_mem, Pointer.to(memResults));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{length}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{maximal}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{numCoupledNodes}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{stackSize}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        clSetKernelArg(kernel, iPara++, Sizeof.cl_int, Pointer.to(new int[]{pass}));        

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);
        clEnqueueReadBuffer(commandQueue, memObjects[4], CL_TRUE, 0,
            g_buff.length * Sizeof.cl_int, Pointer.to(g_buff), 0, null, null);        
        // Release kernel, program, and memory objects
        for(int j=0; j<memObjects.length; j++)
            clReleaseMemObject(memObjects[j]);
        clReleaseKernel(kernel);
        
        ++countExecution;
        if(g_buff[1] == 1) stop = true;
        }//end While stop
        
        if(g_buff[0] >= MAXNUMFBLs) break;
        pass += MAX_PROCESS_PATHS;
        }//end For loop

        clEnqueueReadBuffer(commandQueue, memResults, CL_TRUE, 0,
            lengthResult * Sizeof.cl_int, pResult, 0, null, null);
        clReleaseMemObject(memResults);
        //colin: add for GPU
        this.numFBLs = g_buff[0]+1;
        if(this.numFBLs > MAXNUMFBLs)
            this.numFBLs = MAXNUMFBLs;
        System.out.println("colin: Total found FFLs/numIteration/countExecution/pass = " + this.numFBLs + "/" + numIteration + "/" + countExecution + "/" + pass);
        /**/                
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        // release memory
        indexNodesInt = null;
        rndinaIntArr = null;
        destNodesInt = null;
        g_buff = null;
        firstIndexSrcNodeInEdgesArr = null;       
        System.gc();
        // end release
                
        return resultArr;
    }

    public int [] findAllAttractor(ArrayList<Node> nodes, 
            ArrayList<Integer> allStates){                
        int numStates = allStates.size()/numPart;
        final int MAX_PROCESS_STATES = 2048;
        System.out.printf("colin: OpenCL number of items = %d\n", numStates);

        int [] logicTables = Node.getLogicTables(nodes, false);
        int logicSize = logicTables.length / nodes.size();
        System.out.printf("colin: findAllAttractor: logicSize = %d\n", logicSize);
        
        //byte [] nodeArrInt = convertNodeArrToIntArr(nodes);
        int [] allStatesArrLong = convertIntegerArrToIntArr(allStates);                        

        int numWorkItem = numStates;
        if(numStates > MAX_PROCESS_STATES) numWorkItem = MAX_PROCESS_STATES;
        int bufSize = (MAXNETWSTATESIZE*numPart + 2*nodes.size() + 2*numPart);
        int num_bufArr = numWorkItem*bufSize;
        int [] bufArr = new int[num_bufArr];
        System.out.println("findAllAttractor-BuffMem=" + (num_bufArr*4/1000));        

        // create result array
        int [] resultATTArr = createATTResultArr(numStates);
        int [] resultTransArr = createTransResultArr(numWorkItem);
        
        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        //colin: add for GPU
        long flagMem = CL_MEM_USE_HOST_PTR;
        if(OPENCL_PLATFORM == GPU_PLATFORM)
            flagMem = CL_MEM_COPY_HOST_PTR;        
        /**/                        
        // Create the program from the source code
        String path = "res/kernels/attr.cl";
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        int numIteration = numStates / MAX_PROCESS_STATES;
        int left = numStates - numIteration*MAX_PROCESS_STATES;
        if (left > 0)
            ++numIteration;
        int pass = 0;
        // Set the work-item dimensions
        long global_work_size[] = new long[]{MAX_PROCESS_STATES};
        long local_work_size[] = new long[]{WORKGROUPSIZE_ROBUST};
        
        for(int ki=0; ki<numIteration; ki++) {
        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[5];
        int iPara = 0;
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * logicTables.length, Pointer.to(logicTables), null);        

        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|flagMem,
            Sizeof.cl_int * allStatesArrLong.length, Pointer.to(allStatesArrLong), null);

        if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * resultATTArr.length, null, null);
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * resultTransArr.length, null, null);
        }
        else
        {
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * resultATTArr.length, Pointer.to(resultATTArr), null);
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * resultTransArr.length, Pointer.to(resultTransArr), null);
        }

        if(OPENCL_PLATFORM == GPU_PLATFORM)
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * num_bufArr, null, null);
        else
            memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * bufArr.length, Pointer.to(bufArr), null);
        
        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "findAllAttractor", null);
        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernel, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        //int iPara = memObjects.length;
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_int, Pointer.to(new int[]{numStates}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_short, Pointer.to(new int[]{numPart}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_short, Pointer.to(new int[]{leftSize}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_short, Pointer.to(new int[]{nodes.size()}));
                
        System.out.println("pass=" + pass);            
        clSetKernelArg(kernel, iPara ++, Sizeof.cl_int, Pointer.to(new int[]{pass}));
        clSetKernelArg(kernel, iPara ++, Sizeof.cl_short, Pointer.to(new int[]{logicSize}));
        
        pass += global_work_size[0];
        // Execute the kernel
        //cl_event eRange = new cl_event();
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);
        //clWaitForEvents(1, new cl_event[]{eRange});
        //System.out.println("pass ddd");
        
        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
            resultTransArr.length * Sizeof.cl_int, Pointer.to(resultTransArr), 0, null, null);
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
            resultATTArr.length * Sizeof.cl_int, Pointer.to(resultATTArr), 0, null, null);
        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);
        clReleaseKernel(kernel);

        if(MyRBN.Transitions.size() >= MAXTRANSITIONSIZE) continue;
        int structSize = 2*numPart;
        int [] l = new int[numPart];
        int sI;
        numWorkItem = (int)global_work_size[0];
        if(left > 0 && ki == numIteration-1)
            numWorkItem = left;
        for(int i=0;i<numWorkItem;i++)
        {
            sI = i*MAXNETWSTATESIZE*structSize;
            for(int nTrans=0; nTrans<MAXNETWSTATESIZE; nTrans++)
            {
            if(resultTransArr[sI] < 0)
            {
                break;
            }
            
            Transition trans = new Transition();
            for(int m=0;m<2;m++)
            {
                for(int j=0;j<numPart;j++)
                {
                    if(m==0)
                        l[j] = resultTransArr[sI+j];
                    else
                        l[j] = resultTransArr[sI+j + numPart];
                }

                //cal state
                String state = "";
                int NumOfBitPerState = MAXBITSIZE;
                for(int k=0;k<numPart;k++)
                {
                    if(k==numPart-1 && leftSize > 0)
                    {
                        NumOfBitPerState = leftSize;
                    }

                    String s0=Integer.toBinaryString(l[k]);
                    StringBuilder s1= new StringBuilder("");
                    for(int p=0;p<NumOfBitPerState-s0.length();p++){
                        s1.append("0");
                    }
                    String stemp=s1.toString().concat(s0);

                    state = state.concat(stemp);
                }

                if(m==0)
                    trans.NodeSrc = state;
                else
                    trans.NodeDst = state;
            }

            MyRBN.Transitions.add(trans);
            sI += structSize;
            }
            if(MyRBN.Transitions.size() >= MAXTRANSITIONSIZE) break;
        }
        }
        /**/
        numATT = numStates;
                
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        
        // release memory
        //nodeArrInt = null;
        logicTables = null;
        allStatesArrLong = null;        
        resultTransArr = null;
        bufArr = null;
        System.gc();
        // end release
        
        return resultATTArr;
    }
    
    public int [] findAllAttractor_only(ArrayList<Node> nodes, 
            ArrayList<Integer> allStates){                
        int numStates = allStates.size()/numPart;
        final int MAX_PROCESS_STATES = 2048;
        System.out.printf("colin: OpenCL number of items = %d\n", numStates);

        int [] logicTables = Node.getLogicTables(nodes, false);
        int logicSize = logicTables.length / nodes.size();
        //System.out.printf("colin: findAllAttractor: logicSize = %d\n", logicSize);
        
        //byte [] nodeArrInt = convertNodeArrToIntArr(nodes);
        int [] allStatesArrLong = convertIntegerArrToIntArr(allStates);                        

        int numWorkItem = numStates;
        if(numStates > MAX_PROCESS_STATES) numWorkItem = MAX_PROCESS_STATES;
        int bufSize = (MAXNETWSTATESIZE*numPart + 2*nodes.size() + 2*numPart);
        int num_bufArr = numWorkItem*bufSize;
        int [] bufArr = new int[num_bufArr];
        //System.out.println("findAllAttractor-BuffMem=" + (num_bufArr*4/1000));        

        // create result array
        int [] resultATTArr = createATTResultArr_only(numStates);
        //int [] resultTransArr = createTransResultArr(numWorkItem);
        
        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        //colin: add for GPU
        long flagMem = CL_MEM_USE_HOST_PTR;
        if(OPENCL_PLATFORM == GPU_PLATFORM)
            flagMem = CL_MEM_COPY_HOST_PTR;        
        /**/                        
        // Create the program from the source code
        String path = "res/kernels/attr_only.cl";
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        int numIteration = numStates / MAX_PROCESS_STATES;
        int left = numStates - numIteration*MAX_PROCESS_STATES;
        if (left > 0)
            ++numIteration;
        int pass = 0;
        // Set the work-item dimensions
        long global_work_size[] = new long[]{MAX_PROCESS_STATES};
        long local_work_size[] = new long[]{WORKGROUPSIZE_ROBUST};
        
        for(int ki=0; ki<numIteration; ki++) {
        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[4];
        int iPara = 0;
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * logicTables.length, Pointer.to(logicTables), null);        

        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|flagMem,
            Sizeof.cl_int * allStatesArrLong.length, Pointer.to(allStatesArrLong), null);

        if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * resultATTArr.length, null, null);        
        }
        else
        {
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * resultATTArr.length, Pointer.to(resultATTArr), null);        
        }

        if(OPENCL_PLATFORM == GPU_PLATFORM)
        memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * num_bufArr, null, null);
        else
            memObjects[iPara ++] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * bufArr.length, Pointer.to(bufArr), null);
        
        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "findAllAttractor", null);
        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernel, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        //int iPara = memObjects.length;
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_int, Pointer.to(new int[]{numStates}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_short, Pointer.to(new int[]{numPart}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_short, Pointer.to(new int[]{leftSize}));
        clSetKernelArg(kernel, iPara++,
            Sizeof.cl_short, Pointer.to(new int[]{nodes.size()}));
                
        //System.out.println("pass=" + pass);            
        clSetKernelArg(kernel, iPara ++, Sizeof.cl_int, Pointer.to(new int[]{pass}));
        clSetKernelArg(kernel, iPara ++, Sizeof.cl_short, Pointer.to(new int[]{logicSize}));
        
        pass += global_work_size[0];
        // Execute the kernel
        //cl_event eRange = new cl_event();
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);
        //clWaitForEvents(1, new cl_event[]{eRange});
        //System.out.println("pass ddd");
        
        // Read the output data
        //clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
        //    resultTransArr.length * Sizeof.cl_int, Pointer.to(resultTransArr), 0, null, null);
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
            resultATTArr.length * Sizeof.cl_int, Pointer.to(resultATTArr), 0, null, null);
        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);
        clReleaseKernel(kernel);                        
        }
        /**/
        numATT = numStates;
                
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);
        
        // release memory
        //nodeArrInt = null;
        logicTables = null;
        allStatesArrLong = null;                
        bufArr = null;
        System.gc();
        // end release
        
        return resultATTArr;
    }
    
    public void findAllCoupledFBLs(ArrayList<FBL> AllDistinctFBLs, int maxPathLen, ArrayList<CoupleFBL> CoupleFBLs){
        int [] fblArr = createFBLArr(AllDistinctFBLs, true, maxPathLen);
        int totalFBL = AllDistinctFBLs.size();// - 1;
        //System.out.println("posarrInt: "+java.util.Arrays.toString(posarrInt));
        //System.out.println("indexNodesInt: "+java.util.Arrays.toString(indexNodesInt));       

        // create result array
        int [] resultArr = createResultArr4CPFBL(maxPathLen);
        System.out.println("colin: OpenCL resultArr size = " + resultArr.length*4/1000000);
        int lengthResult = resultArr.length;
        Pointer pResult = Pointer.to(resultArr);
        int [] indexResult = new int[2];
        indexResult[0] = -1;
        indexResult[1] = maxPathLen;

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Create the program from the source code
        String path = "res/kernels/fbl.cl";
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);
        
        //colin: add for GPU
        long flagMem = CL_MEM_USE_HOST_PTR;
        if(OPENCL_PLATFORM == GPU_PLATFORM)
            flagMem = CL_MEM_COPY_HOST_PTR | CL_MEM_ALLOC_HOST_PTR;
        /**/
        cl_mem memResult = null;
        /*cl_mem mem_fblArr = null;
        mem_fblArr = clCreateBuffer(context,
            CL_MEM_READ_ONLY | flagMem,
            Sizeof.cl_int * fblArr.length, Pointer.to(fblArr), null);
        if(OPENCL_PLATFORM == GPU_PLATFORM)
        {
        memResult = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * lengthResult, null, null);
        }
        else
        {
        memResult = clCreateBuffer(context,
            CL_MEM_READ_WRITE | flagMem,
            Sizeof.cl_int * lengthResult, pResult, null);
        }*/
        
        int [] done = new int[totalFBL];
        for(int i=0; i<done.length; i++) done[i] = 0;
        
        for(int n=0; n<totalFBL - 1; n++)
        {
            cl_mem mem_fblArr = clCreateBuffer(context,
                CL_MEM_READ_ONLY | flagMem, Sizeof.cl_int * fblArr.length, Pointer.to(fblArr), null);
            if (OPENCL_PLATFORM == GPU_PLATFORM) {
                if(memResult == null)
                memResult = clCreateBuffer(context,
                        CL_MEM_READ_WRITE | CL_MEM_ALLOC_HOST_PTR,
                        Sizeof.cl_int * lengthResult, null, null);
            } else {
                memResult = clCreateBuffer(context,
                        CL_MEM_READ_WRITE | flagMem,
                        Sizeof.cl_int * lengthResult, pResult, null);
            }                        
        // Allocate the memory objects for the input- and output data
        cl_mem memIndexResult = clCreateBuffer(context,
            CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * 2, Pointer.to(indexResult), null);
        cl_mem memDoneArray = clCreateBuffer(context,
            CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * done.length, Pointer.to(done), null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "findAllCoupledFBLs", null);
        // Set the arguments for the kernel
        clSetKernelArg(kernel, 0,
            Sizeof.cl_mem, Pointer.to(mem_fblArr));
        clSetKernelArg(kernel, 1,
            Sizeof.cl_mem, Pointer.to(memResult));
        clSetKernelArg(kernel, 2,
            Sizeof.cl_mem, Pointer.to(memIndexResult));
        clSetKernelArg(kernel, 3,
            Sizeof.cl_mem, Pointer.to(memDoneArray));
        clSetKernelArg(kernel, 4, Sizeof.cl_int, Pointer.to(new int[]{n}));
        
        // Set the work-item dimensions
        int numWorkItems = totalFBL - 1 - n;
        long global_work_size[] = new long[]{numWorkItems};
        long local_work_size[] = new long[]{1};//1024

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);
        
        clEnqueueReadBuffer(commandQueue, memIndexResult, CL_TRUE, 0,
            2 * Sizeof.cl_int, Pointer.to(indexResult), 0, null, null);
        clEnqueueReadBuffer(commandQueue, memDoneArray, CL_TRUE, 0,
            done.length * Sizeof.cl_int, Pointer.to(done), 0, null, null);
        // Release kernel, program, and memory objects        
        clReleaseMemObject(memIndexResult);
        clReleaseMemObject(memDoneArray);
        clReleaseMemObject(mem_fblArr);
        clReleaseKernel(kernel);

        if(indexResult[0] >= MAXNUMCPFBLs || (indexResult[0] < MAXNUMCPFBLs && n == totalFBL - 2)){
            // Read the output data       
            System.out.println("colin: Iteration order = " + n);
            clEnqueueReadBuffer(commandQueue, memResult, CL_TRUE, 0,
                lengthResult * Sizeof.cl_int, pResult, 0, null, null);            
            addCoupledFBLs(AllDistinctFBLs, maxPathLen, indexResult[0]+1, resultArr, CoupleFBLs);
            //break;
        }
        if (OPENCL_PLATFORM == CPU_PLATFORM) {
            clReleaseMemObject(memResult);
        }
        if(indexResult[0] >= MAXNUMCPFBLs){            
            indexResult[0] = -1;
            --n;//recheck again for failed adding coupled FBLs
        }
        else
            for(int i=0; i<done.length; i++) done[i] = 0;
        }

        // Read the output data       
        //clEnqueueReadBuffer(commandQueue, memResult, CL_TRUE, 0,
        //    lengthResult * Sizeof.cl_int, pResult, 0, null, null);
        if(OPENCL_PLATFORM == GPU_PLATFORM)
            clReleaseMemObject(memResult);
        //clReleaseMemObject(mem_fblArr);
        
        this.numFBLs = CoupleFBLs.size();//indexResult[0]+1;
        //if(this.numFBLs > MAXNUMCPFBLs)
        //    this.numFBLs = MAXNUMCPFBLs;
        //System.out.println("colin: Number of found Coupled FBLs = " + this.numFBLs);
                
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        // release memory
        fblArr = null;
        indexResult = null;
        resultArr = null;
        System.gc();
        // end release
        
        // result
        return;
    }

    /*public int [][] setAttributeForNodes(ArrayList<FBL> AllDistinctFBLs, int maxPathLen){
        int [] fblArr = createFBLArr(AllDistinctFBLs, false, maxPathLen);
        int n = AllDistinctFBLs.size()/NUMFBLEXAMINING;
        int nLeft = AllDistinctFBLs.size() - n*NUMFBLEXAMINING;
        if(nLeft > 0)
            n++;
        
        //System.out.println("posarrInt: "+java.util.Arrays.toString(posarrInt));
        //System.out.println("indexNodesInt: "+java.util.Arrays.toString(indexNodesInt));

        // create result array
        int numNodes = Common.nodeIDsArr.size();
        int [][] numFBLs = new int[3][numNodes];
        for(int i=0;i<3;i++)
            for(int j=0;j<numNodes;j++)
                numFBLs[i][j] = 0;
            //0: numFBLs of node
            //1: num FBLs + of node       

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);

        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);

        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[4];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * fblArr.length, Pointer.to(fblArr), null);

        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * numFBLs[0].length, Pointer.to(numFBLs[0]), null);
        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * numFBLs[1].length, Pointer.to(numFBLs[1]), null);
        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_WRITE | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * numFBLs[2].length, Pointer.to(numFBLs[2]), null);
        // Create the program from the source code
        String path = "res/kernels/fbl_cpu.cl";
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);

        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel = clCreateKernel(program, "setAttributeForNodes", null);

        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernel, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }               
        clSetKernelArg(kernel, 4,
            Sizeof.cl_int, Pointer.to(new int[]{AllDistinctFBLs.size()}));
        clSetKernelArg(kernel, 5,
            Sizeof.cl_int, Pointer.to(new int[]{Common.nodeIDsArr.size()}));

        // Set the work-item dimensions
        //long l = (long)Math.pow(2, 10) + 1;
        long global_work_size[] = new long[]{n};
        long local_work_size[] = new long[]{1};//1024

        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[1], CL_TRUE, 0,
            numFBLs[0].length * Sizeof.cl_int, Pointer.to(numFBLs[0]), 0, null, null);
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
            numFBLs[1].length * Sizeof.cl_int, Pointer.to(numFBLs[1]), 0, null, null);
        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
            numFBLs[2].length * Sizeof.cl_int, Pointer.to(numFBLs[2]), 0, null, null);

        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);

        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        // release memory
        fblArr = null;                
        
        System.gc();
        // end release

        // result
        return numFBLs;
    }*/
    
    public int [] calRobustness(ArrayList<Node> nodes, ArrayList<Interaction> rndina,
            ArrayList<Integer> posSelectedNodes, ArrayList<Integer> allStates, int PerturbationType, int NumPossibleFunc){
        int [] rndinaIntArr = convertInterationArrToIntArr4ATT(rndina,nodes);
        int lengthIntA = rndinaIntArr.length;
        System.out.printf("colin: OpenCL number of items = %d\n", posSelectedNodes.size());
        System.out.printf("colin: OpenCL number of part = %d\n", numPart);
        System.out.printf("colin: OpenCL number of states = %d\n", allStates.size()/numPart);
        /*if(allStates.size() == 1)
        {
             System.out.printf("colin: OpenCL value of states = %d\n", allStates.get(0));
        }*/

        byte [] nodeArrInt = convertNodeArrToIntArr(nodes);
        int [] posSelectedNodesInt = convertIntegerArrToIntArr(posSelectedNodes);
        int [] allStatesArrLong = convertIntegerArrToIntArr(allStates);

        // create result array
        int [] resultArr = new int[NumPossibleFunc*posSelectedNodes.size()];
        for(int i=0; i</*posSelectedNodes.size()*/resultArr.length;i++)
        {
            resultArr[i] = 0;
        }

        int [] bufArr = createStates4CPU(posSelectedNodes.size(), nodes.size(), numPart);//colin: no limit # of nodes in Robustness computation
        int bufSize = (MAXNETWSTATESIZE*numPart + 2*MAXATTSTATESIZE*numPart + 3*nodes.size() + numPart);

        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[6];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_char * nodeArrInt.length, Pointer.to(nodeArrInt), null);

        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * lengthIntA, Pointer.to(rndinaIntArr), null);

        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * posSelectedNodesInt.length, Pointer.to(posSelectedNodesInt), null);

        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * allStatesArrLong.length, Pointer.to(allStatesArrLong), null);

        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * resultArr.length, Pointer.to(resultArr), null);

        memObjects[5] = clCreateBuffer(context,
            CL_MEM_WRITE_ONLY|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * bufArr.length, Pointer.to(bufArr), null);
        
        // Create the program from the source code
        String path = "res/kernels/robust.cl";
        if(Main.AllPossibleFunc)
        {
            path = "res/kernels/robustAllFunc.cl";
        }
        
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel;
        if(!Main.AllPossibleFunc)
        {
            kernel = clCreateKernel(program, "calRobustness", null);
        }
        else
        {
            kernel = clCreateKernel(program, "calRobustnessAllFunc", null);
        }

        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernel, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        int nextPara = memObjects.length;
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{numPart}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{leftSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{nodes.size()}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{allStatesArrLong.length/numPart}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{PerturbationType}));
        if(Main.AllPossibleFunc)
        {
            clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_long, Pointer.to(new long[]{NumPossibleFunc}));
        }

        // Set the work-item dimensions        
        long global_work_size[] = new long[]{posSelectedNodesInt.length};
        long local_work_size[] = new long[]{1};//1024
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[4], CL_TRUE, 0,
            resultArr.length * Sizeof.cl_int, Pointer.to(resultArr), 0, null, null);

        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);

        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);        

        nodeArrInt = null;
        rndinaIntArr = null;
        posSelectedNodesInt = null;
        allStatesArrLong = null;
        bufArr = null;
        System.gc();
        
        return resultArr;
    }
public void calRobustnessGPU(ArrayList<Node> nodes, 
        ArrayList<Integer> posSelectedNodes, ArrayList<Byte> allStates, int PerturbationType, int NumPossibleFunc, int mutationTime, RobustnessValues robs, String ruleIndex){        
        int numStates = allStates.size()/nodes.size();        
        System.out.printf("colin GPU: OpenCL number of items = %d\n", posSelectedNodes.size());        
        System.out.printf("colin GPU: OpenCL number of states = %d\n", numStates);

        int [] logicTables = Node.getLogicTables(nodes, false);
        int [] inv_logicTables = Node.getLogicTables(nodes, true);
        int logicSize = logicTables.length / nodes.size();
        System.out.printf("colin: logicSize = %d\n", logicSize);
                
        int [] posSelectedNodesInt = createIndexNodes4Robustness(posSelectedNodes);
        byte [] allStates_byte = convertToByteArr(allStates);        
        
        // create result array
        int [] resultArr = null;
        float [] resultArr_PINF = null;
        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
            resultArr_PINF = new float[posSelectedNodes.size()*posSelectedNodes.size()];
            for (int i = 0; i </*posSelectedNodes.size()*/ resultArr_PINF.length; i++) {
                resultArr_PINF[i] = 0;
            }
        } else {
            resultArr = new int[NumPossibleFunc*posSelectedNodes.size()];
            for (int i = 0; i </*posSelectedNodes.size()*/ resultArr.length; i++) {
                resultArr[i] = 0;
            }
        }        
        
        //init paras for Original attractors
        int num_originalAtts = numStates;        
        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
                 || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
            num_originalAtts = posSelectedNodes.size();            
        }
        int [] resultSizeArr = new int[num_originalAtts];
        int numByteBufForOriginalAtts = num_originalAtts*(MAXATTSTATESIZE+2)*nodes.size();
        byte [] originalAtts = createStates(num_originalAtts, MAXATTSTATESIZE, nodes.size());
        
        //init paras for New attractors
        int [] resultSizeArrForNewAtts = new int[posSelectedNodes.size()];
        // create transitionNetworkState and att1, att2                
        int numByteBufForNewAtts = posSelectedNodes.size()*(MAXATTSTATESIZE+2)*nodes.size();
        byte [] newAtts = createStates(posSelectedNodes.size(), MAXATTSTATESIZE, nodes.size());
        
        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);

        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem mem_PosSelectedNodes = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * posSelectedNodesInt.length, Pointer.to(posSelectedNodesInt), null);
        
        cl_mem memObjects[] = new cl_mem[5];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * logicTables.length, Pointer.to(logicTables), null);        

        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_char * allStates_byte.length, Pointer.to(allStates_byte), null);        
        
        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * resultSizeArr.length, null, null);        
        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_char * originalAtts.length, null, null);
        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_char * numByteBufForOriginalAtts, null, null);        
        
        // Create the program from the source code
        String path = "res/kernels/robustGPU_canal.cl";
        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
                ) {
            path = "res/kernels/robustGPU_canal_ko.cl";
        } else if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
            path = "res/kernels/robustGPU_canal_ko_pinf.cl";
        }
        
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel "findOriginalAtts"
        cl_kernel kernelFindOriginalAtts = clCreateKernel(program, "findOriginalAtts", null);
        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernelFindOriginalAtts, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        int indexPara = memObjects.length;
        clSetKernelArg(kernelFindOriginalAtts, indexPara++,
            Sizeof.cl_int, Pointer.to(new int[]{nodes.size()}));
        clSetKernelArg(kernelFindOriginalAtts, indexPara++,
            Sizeof.cl_int, Pointer.to(new int[]{num_originalAtts}));                
        clSetKernelArg(kernelFindOriginalAtts, indexPara++,
                Sizeof.cl_short, Pointer.to(new int[]{logicSize}));        

        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
                || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
            clSetKernelArg(kernelFindOriginalAtts, indexPara + 1, Sizeof.cl_mem, Pointer.to(mem_PosSelectedNodes));
        }
        
        // Set the work-item dimensions        
        int left = numStates - ((int)(numStates/WORKGROUPSIZE_ROBUST))*WORKGROUPSIZE_ROBUST;
        if(left > 0)
            left = MyOpenCL.WORKGROUPSIZE_ROBUST - left;
        int newNumStates = numStates+left;        
        long global_work_size[] = new long[]{newNumStates};
        long local_work_sizeGPU[] = new long[]{WORKGROUPSIZE_ROBUST};//1024

        if (PerturbationType == Config.MUTATION_INITIAL_STATE || PerturbationType == Config.MUTATION_UPDATE_RULE) {        
        clEnqueueNDRangeKernel(commandQueue, kernelFindOriginalAtts, 1, null,
            global_work_size, local_work_sizeGPU, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
            resultSizeArr.length * Sizeof.cl_int, Pointer.to(resultSizeArr), 0, null, null);
        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
            originalAtts.length * Sizeof.cl_char, Pointer.to(originalAtts), 0, null, null);

        System.out.println("For original attractors:");
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        for(int i=0; i<numStates; i++)
        {
            if(min > resultSizeArr[i]) min = resultSizeArr[i];
            if(max < resultSizeArr[i]) max = resultSizeArr[i];
            System.out.print(resultSizeArr[i] + " ");
        }
        System.out.println();
        System.out.println("Min/Max size of original attractors: " + min + "/" + max);
        for(int i=0; i<numStates; i++)
        {
            //if(resultSizeArr[i] == 3)
            //    System.out.println("Exist attractors with size 3");
            if(resultSizeArr[i] == 4)
            {
                int off = i*MAXATTSTATESIZE*nodes.size();
                for(int k=0;k<3;k++)
                {
                    for(int n=0;n<nodes.size();n++)
                        System.out.print(originalAtts[off++]==0?"0":"1");
                    System.out.println();
                }
                break;
            }
        }
        }
        
        // Create the kernel "findNewAtts"
        cl_kernel kernelFindNewAtts = clCreateKernel(program, "findNewAtts", null);
        cl_kernel kernelCompareAtts = clCreateKernel(program, "compareAtts", null);
        if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
            kernelCompareAtts = clCreateKernel(program, "compareAtts_PINF", null);
        }
        
        cl_mem mem_inv_logicTables = clCreateBuffer(context,
            CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * inv_logicTables.length, Pointer.to(inv_logicTables), null);
        cl_mem mem_resultSizeArrForNewAtts = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_int * resultSizeArrForNewAtts.length, null, null);
        cl_mem mem_newAtts = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_char * newAtts.length, null, null);
        cl_mem mem_bufForNewAtts = clCreateBuffer(context,
            CL_MEM_READ_WRITE,
            Sizeof.cl_char * numByteBufForNewAtts, null, null);
        
        cl_mem mem_resultArr = null;
        cl_mem mem_resultArr_PINF = null;
        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
            mem_resultArr_PINF = clCreateBuffer(context,
                CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_float * resultArr_PINF.length, Pointer.to(resultArr_PINF), null);
        } else {
            mem_resultArr = clCreateBuffer(context,
                CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
                Sizeof.cl_int * resultArr.length, Pointer.to(resultArr), null);
        }
        
        // Execute the kernel
        System.out.println("For NEW attractors:");
        int iPara = 0;
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[0]));   
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_inv_logicTables));   
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_PosSelectedNodes));
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[1]));   //allStates_byte
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultSizeArrForNewAtts));
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_newAtts));
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_bufForNewAtts));
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{nodes.size()}));
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{numStates}));
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{PerturbationType}));
        iPara++;        
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{posSelectedNodes.size()}));
        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_short, Pointer.to(new int[]{logicSize}));
        
        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
                || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
            int fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT;
            if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
                fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT_PINF;
            }
            clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_short, Pointer.to(new int[]{fixedValue}));
            clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_short, Pointer.to(new int[]{mutationTime}));
        }
        
        //if(!Main.AllPossibleFunc) NumPossibleFunc = 1;
        //for(int kF=0; kF<NumPossibleFunc; kF++)                                                       
        global_work_size[0] = posSelectedNodesInt.length;
        for(int i=0; i<numStates; i++) {            
            if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
                    || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
                //Find original attractors of a state: paralled by nodes                 
                clSetKernelArg(kernelFindOriginalAtts, indexPara, Sizeof.cl_int, Pointer.to(new int[]{(i*nodes.size())}));
                
                clEnqueueNDRangeKernel(commandQueue, kernelFindOriginalAtts, 1, null, global_work_size, local_work_sizeGPU, 0, null, null);
                // Read the output data
                clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, resultSizeArr.length * Sizeof.cl_int, Pointer.to(resultSizeArr), 0, null, null);
                clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0, originalAtts.length * Sizeof.cl_char, Pointer.to(originalAtts), 0, null, null);
            }
            
            clSetKernelArg(kernelFindNewAtts, 10, Sizeof.cl_int, Pointer.to(new int[]{(i*nodes.size())}));
            //cl_event evt_ndrange = clCreateUserEvent(context, null);
            //global_work_size[0] = posSelectedNodesInt.length;
            clEnqueueNDRangeKernel(commandQueue, kernelFindNewAtts, 1, null, global_work_size, local_work_sizeGPU, 0, null, null);
            //clWaitForEvents(1, new cl_event[]{evt_ndrange});
            //clReleaseEvent(evt_ndrange);
            // Read the output data
            clEnqueueReadBuffer(commandQueue, mem_resultSizeArrForNewAtts, CL_TRUE, 0, resultSizeArrForNewAtts.length * Sizeof.cl_int, Pointer.to(resultSizeArrForNewAtts), 0, null, null);
            clEnqueueReadBuffer(commandQueue, mem_newAtts, CL_TRUE, 0, newAtts.length * Sizeof.cl_char, Pointer.to(newAtts), 0, null, null);
            
            /*min = Integer.MAX_VALUE;
            max = Integer.MIN_VALUE;
            for(int k=0; k<posSelectedNodes.size(); k++)
            {
                if(min > resultSizeArrForNewAtts[k]) min = resultSizeArrForNewAtts[k];
                if(max < resultSizeArrForNewAtts[k]) max = resultSizeArrForNewAtts[k];
                //System.out.print(resultSizeArrForNewAtts[k] + " ");
            }
            //System.out.println();
            System.out.println("Min/Max size of NEW attractors: " + min + "/" + max);*/
            iPara = 0;            
            if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
                clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_PosSelectedNodes));
            }
            
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[2]));
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultSizeArrForNewAtts));
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[3]));
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_newAtts));
            
            if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
                clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultArr_PINF));
            } else {
                clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultArr));
            }
            
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{nodes.size()}));
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{i}));
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{0*posSelectedNodes.size()}));
            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{posSelectedNodes.size()}));

            clEnqueueNDRangeKernel(commandQueue, kernelCompareAtts, 1, null, global_work_size, local_work_sizeGPU, 0, null, null);            
        }
                
        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
            clEnqueueReadBuffer(commandQueue, mem_resultArr_PINF, CL_TRUE, 0, resultArr_PINF.length * Sizeof.cl_float, Pointer.to(resultArr_PINF), 0, null, null);
        } else {
            clEnqueueReadBuffer(commandQueue, mem_resultArr, CL_TRUE, 0, resultArr.length * Sizeof.cl_int, Pointer.to(resultArr), 0, null, null);
        }
        
        clReleaseKernel(kernelFindOriginalAtts);
        clReleaseKernel(kernelFindNewAtts);
        clReleaseKernel(kernelCompareAtts);
        //clFinish(commandQueue);
        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);
        clReleaseMemObject(mem_PosSelectedNodes);
        clReleaseMemObject(mem_bufForNewAtts);
        clReleaseMemObject(mem_newAtts);
        clReleaseMemObject(mem_resultSizeArrForNewAtts);
        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
            clReleaseMemObject(mem_resultArr_PINF);
        } else {
            clReleaseMemObject(mem_resultArr);
        }        
        
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);

        logicTables = null;
        inv_logicTables = null;
        posSelectedNodesInt = null;
        allStates_byte = null;
        resultSizeArr = null;
        resultSizeArrForNewAtts = null;
        
        originalAtts = null;
        newAtts = null;
        System.gc();

        //RobustnessValues robs = new RobustnessValues();
        robs.set_noRobustStates(resultArr);
        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
            robs.set_pInfValues(ruleIndex, resultArr_PINF);
        }
        
        return;
    }
//    public void calInModuleRobustnessGPU(ArrayList<Node> nodes, 
//        ArrayList<Integer> posSelectedNodes, ArrayList<Byte> allStates, int PerturbationType, int NumPossibleFunc, int mutationTime, RobustnessValues robs, String ruleIndex,int  NumModule){        
//        int numStates = allStates.size()/nodes.size();        
//        System.out.printf("colin GPU: OpenCL number of items = %d\n", posSelectedNodes.size());        
//        System.out.printf("colin GPU: OpenCL number of states = %d\n", numStates);
//
//        int [] logicTables = Node.getLogicTables(nodes, false);
//        int [] inv_logicTables = Node.getLogicTables(nodes, true);
//        int logicSize = logicTables.length / nodes.size();
//        System.out.printf("colin: logicSize = %d\n", logicSize);
//                
//        int [] posSelectedNodesInt = createIndexNodes4Robustness(posSelectedNodes);
//        byte [] allStates_byte = convertToByteArr(allStates);        
//        
//        // create result array
//        double [] resultArr = null;
//        float [] resultArr_PINF = null;
//        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//            resultArr_PINF = new float[posSelectedNodes.size()*posSelectedNodes.size()];
//            for (int i = 0; i </*posSelectedNodes.size()*/ resultArr_PINF.length; i++) {
//                resultArr_PINF[i] = 0;
//            }
//        } else {
//            resultArr = new double[NumPossibleFunc*posSelectedNodes.size()];
//            for (int i = 0; i </*posSelectedNodes.size()*/ resultArr.length; i++) {
//                resultArr[i] = 0.0;
//            }
//        }        
//        
//        //init paras for Original attractors
//        int num_originalAtts = numStates;        
//        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
//                 || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
//            num_originalAtts = posSelectedNodes.size();            
//        }
//        int [] resultSizeArr = new int[num_originalAtts];
//        int numByteBufForOriginalAtts = num_originalAtts*(MAXATTSTATESIZE+2)*nodes.size();
//        byte [] originalAtts = createStates(num_originalAtts, MAXATTSTATESIZE, nodes.size());
//        
//        //init paras for New attractors
//        int [] resultSizeArrForNewAtts = new int[posSelectedNodes.size()];
//        // create transitionNetworkState and att1, att2                
//        int numByteBufForNewAtts = posSelectedNodes.size()*(MAXATTSTATESIZE+2)*nodes.size();
//        byte [] newAtts = createStates(posSelectedNodes.size(), MAXATTSTATESIZE, nodes.size());
//        
//        // Initialize the context properties
//        cl_context_properties contextProperties = new cl_context_properties();
//        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
//        // Create a context for the selected device
//        cl_context context = clCreateContext(
//            contextProperties, 1, new cl_device_id[]{device},
//            null, null, null);
//
//        // Create a command-queue for the selected device
//        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);
//
//        // Allocate the memory objects for the input- and output data
//        cl_mem mem_PosSelectedNodes = clCreateBuffer(context,
//            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
//            Sizeof.cl_int * posSelectedNodesInt.length, Pointer.to(posSelectedNodesInt), null);
//        
//        cl_mem memObjects[] = new cl_mem[5];
//        memObjects[0] = clCreateBuffer(context,
//            CL_MEM_COPY_HOST_PTR,
//            Sizeof.cl_int * logicTables.length, Pointer.to(logicTables), null);        
//
//        memObjects[1] = clCreateBuffer(context,
//            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
//            Sizeof.cl_char * allStates_byte.length, Pointer.to(allStates_byte), null);        
//        
//        memObjects[2] = clCreateBuffer(context,
//            CL_MEM_READ_WRITE,
//            Sizeof.cl_int * resultSizeArr.length, null, null);        
//        memObjects[3] = clCreateBuffer(context,
//            CL_MEM_READ_WRITE,
//            Sizeof.cl_char * originalAtts.length, null, null);
//        memObjects[4] = clCreateBuffer(context,
//            CL_MEM_READ_WRITE,
//            Sizeof.cl_char * numByteBufForOriginalAtts, null, null);        
//        
//        // Create the program from the source code
//        String path = "res/kernels/robustGPU_canal.cl";
//        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
//                ) {
//            path = "res/kernels/robustGPU_canal_ko.cl";
//        } else if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
//            path = "res/kernels/robustGPU_canal_ko_pinf.cl";
//        }
//        
//        URL url = getClass().getResource(path);
//        String programSource = MyOpenCL.readFile(url);
//        cl_program program = clCreateProgramWithSource(context,
//            1, new String[]{ programSource }, null, null);
//        // Build the program
//        clBuildProgram(program, 0, null, null, null, null);
//
//        // Create the kernel "findOriginalAtts"
//        cl_kernel kernelFindOriginalAtts = clCreateKernel(program, "findOriginalAtts", null);
//        // Set the arguments for the kernel
//        for(int i=0;i<memObjects.length;i++)
//        {
//            clSetKernelArg(kernelFindOriginalAtts, i,
//                Sizeof.cl_mem, Pointer.to(memObjects[i]));
//        }
//        int indexPara = memObjects.length;
//        clSetKernelArg(kernelFindOriginalAtts, indexPara++,
//            Sizeof.cl_int, Pointer.to(new int[]{nodes.size()}));
//        clSetKernelArg(kernelFindOriginalAtts, indexPara++,
//            Sizeof.cl_int, Pointer.to(new int[]{num_originalAtts}));                
//        clSetKernelArg(kernelFindOriginalAtts, indexPara++,
//                Sizeof.cl_short, Pointer.to(new int[]{logicSize}));        
//
//        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
//                || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
//            clSetKernelArg(kernelFindOriginalAtts, indexPara + 1, Sizeof.cl_mem, Pointer.to(mem_PosSelectedNodes));
//        }
//        
//        // Set the work-item dimensions        
//        int left = numStates - ((int)(numStates/WORKGROUPSIZE_ROBUST))*WORKGROUPSIZE_ROBUST;
//        if(left > 0)
//            left = MyOpenCL.WORKGROUPSIZE_ROBUST - left;
//        int newNumStates = numStates+left;        
//        long global_work_size[] = new long[]{newNumStates};
//        long local_work_sizeGPU[] = new long[]{WORKGROUPSIZE_ROBUST};//1024
//
//        if (PerturbationType == Config.MUTATION_INITIAL_STATE || PerturbationType == Config.MUTATION_UPDATE_RULE) {        
//        clEnqueueNDRangeKernel(commandQueue, kernelFindOriginalAtts, 1, null,
//            global_work_size, local_work_sizeGPU, 0, null, null);
//
//        // Read the output data
//        clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0,
//            resultSizeArr.length * Sizeof.cl_int, Pointer.to(resultSizeArr), 0, null, null);
//        clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0,
//            originalAtts.length * Sizeof.cl_char, Pointer.to(originalAtts), 0, null, null);
//
//        System.out.println("For original attractors:");
//        int min = Integer.MAX_VALUE;
//        int max = Integer.MIN_VALUE;
//        for(int i=0; i<numStates; i++)
//        {
//            if(min > resultSizeArr[i]) min = resultSizeArr[i];
//            if(max < resultSizeArr[i]) max = resultSizeArr[i];
//            System.out.print(resultSizeArr[i] + " ");
//        }
//        System.out.println();
//        System.out.println("Min/Max size of original attractors: " + min + "/" + max);
//        for(int i=0; i<numStates; i++)
//        {
//            //if(resultSizeArr[i] == 3)
//            //    System.out.println("Exist attractors with size 3");
//            if(resultSizeArr[i] == 4)
//            {
//                int off = i*MAXATTSTATESIZE*nodes.size();
//                for(int k=0;k<3;k++)
//                {
//                    for(int n=0;n<nodes.size();n++)
//                        System.out.print(originalAtts[off++]==0?"0":"1");
//                    System.out.println();
//                }
//                break;
//            }
//        }
//        }
//        
//        // Create the kernel "findNewAtts"
//        cl_kernel kernelFindNewAtts = clCreateKernel(program, "findNewAtts", null);
//        cl_kernel kernelCompareAtts = clCreateKernel(program, "compareAtts", null);
//        if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//            kernelCompareAtts = clCreateKernel(program, "compareAtts_PINF", null);
//        }
//        
//        cl_mem mem_inv_logicTables = clCreateBuffer(context,
//            CL_MEM_COPY_HOST_PTR,
//            Sizeof.cl_int * inv_logicTables.length, Pointer.to(inv_logicTables), null);
//        cl_mem mem_resultSizeArrForNewAtts = clCreateBuffer(context,
//            CL_MEM_READ_WRITE,
//            Sizeof.cl_int * resultSizeArrForNewAtts.length, null, null);
//        cl_mem mem_newAtts = clCreateBuffer(context,
//            CL_MEM_READ_WRITE,
//            Sizeof.cl_char * newAtts.length, null, null);
//        cl_mem mem_bufForNewAtts = clCreateBuffer(context,
//            CL_MEM_READ_WRITE,
//            Sizeof.cl_char * numByteBufForNewAtts, null, null);
//        
//        cl_mem mem_resultArr = null;
//        cl_mem mem_resultArr_PINF = null;
//        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//            mem_resultArr_PINF = clCreateBuffer(context,
//                CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
//                Sizeof.cl_float * resultArr_PINF.length, Pointer.to(resultArr_PINF), null);
//        } else {
//            mem_resultArr = clCreateBuffer(context,
//                CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
//                Sizeof.cl_int * resultArr.length, Pointer.to(resultArr), null);
//        }
//        
//        // Execute the kernel
//        System.out.println("For NEW attractors:");
//        int iPara = 0;
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[0]));   
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_inv_logicTables));   
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_PosSelectedNodes));
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[1]));   //allStates_byte
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultSizeArrForNewAtts));
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_newAtts));
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_bufForNewAtts));
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{nodes.size()}));
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{numStates}));
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{PerturbationType}));
//        iPara++;        
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{posSelectedNodes.size()}));
//        clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_short, Pointer.to(new int[]{logicSize}));
//        
//        if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
//                || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
//            int fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT;
//            if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//                fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT_PINF;
//            }
//            clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_short, Pointer.to(new int[]{fixedValue}));
//            clSetKernelArg(kernelFindNewAtts, iPara++, Sizeof.cl_short, Pointer.to(new int[]{mutationTime}));
//        }
//        
//        //if(!Main.AllPossibleFunc) NumPossibleFunc = 1;
//        //for(int kF=0; kF<NumPossibleFunc; kF++)                                                       
//        global_work_size[0] = posSelectedNodesInt.length;
//        for(int i=0; i<numStates; i++) {            
//            if (PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION
//                    || PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
//                //Find original attractors of a state: paralled by nodes                 
//                clSetKernelArg(kernelFindOriginalAtts, indexPara, Sizeof.cl_int, Pointer.to(new int[]{(i*nodes.size())}));
//                
//                clEnqueueNDRangeKernel(commandQueue, kernelFindOriginalAtts, 1, null, global_work_size, local_work_sizeGPU, 0, null, null);
//                // Read the output data
//                clEnqueueReadBuffer(commandQueue, memObjects[2], CL_TRUE, 0, resultSizeArr.length * Sizeof.cl_int, Pointer.to(resultSizeArr), 0, null, null);
//                clEnqueueReadBuffer(commandQueue, memObjects[3], CL_TRUE, 0, originalAtts.length * Sizeof.cl_char, Pointer.to(originalAtts), 0, null, null);
//            }
//            
//            clSetKernelArg(kernelFindNewAtts, 10, Sizeof.cl_int, Pointer.to(new int[]{(i*nodes.size())}));
//            //cl_event evt_ndrange = clCreateUserEvent(context, null);
//            //global_work_size[0] = posSelectedNodesInt.length;
//            clEnqueueNDRangeKernel(commandQueue, kernelFindNewAtts, 1, null, global_work_size, local_work_sizeGPU, 0, null, null);
//            //clWaitForEvents(1, new cl_event[]{evt_ndrange});
//            //clReleaseEvent(evt_ndrange);
//            // Read the output data
//            clEnqueueReadBuffer(commandQueue, mem_resultSizeArrForNewAtts, CL_TRUE, 0, resultSizeArrForNewAtts.length * Sizeof.cl_int, Pointer.to(resultSizeArrForNewAtts), 0, null, null);
//            clEnqueueReadBuffer(commandQueue, mem_newAtts, CL_TRUE, 0, newAtts.length * Sizeof.cl_char, Pointer.to(newAtts), 0, null, null);
//            
//            /*min = Integer.MAX_VALUE;
//            max = Integer.MIN_VALUE;
//            for(int k=0; k<posSelectedNodes.size(); k++)
//            {
//                if(min > resultSizeArrForNewAtts[k]) min = resultSizeArrForNewAtts[k];
//                if(max < resultSizeArrForNewAtts[k]) max = resultSizeArrForNewAtts[k];
//                //System.out.print(resultSizeArrForNewAtts[k] + " ");
//            }
//            //System.out.println();
//            System.out.println("Min/Max size of NEW attractors: " + min + "/" + max);*/
//            iPara = 0;            
//            if(PerturbationType >= Config.MUTATION_KNOCKOUT_PINF) {
//                clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_PosSelectedNodes));
//            }
//            
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[2]));
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultSizeArrForNewAtts));
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(memObjects[3]));
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_newAtts));
//            
//            if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//                clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultArr_PINF));
//            } else {
//                clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_mem, Pointer.to(mem_resultArr));
//            }
//            
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{nodes.size()}));
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{i}));
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{0*posSelectedNodes.size()}));
//            clSetKernelArg(kernelCompareAtts, iPara++, Sizeof.cl_int, Pointer.to(new int[]{posSelectedNodes.size()}));
//
//            clEnqueueNDRangeKernel(commandQueue, kernelCompareAtts, 1, null, global_work_size, local_work_sizeGPU, 0, null, null);            
//        }
//                
//        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//            clEnqueueReadBuffer(commandQueue, mem_resultArr_PINF, CL_TRUE, 0, resultArr_PINF.length * Sizeof.cl_float, Pointer.to(resultArr_PINF), 0, null, null);
//        } else {
//            clEnqueueReadBuffer(commandQueue, mem_resultArr, CL_TRUE, 0, resultArr.length * Sizeof.cl_int, Pointer.to(resultArr), 0, null, null);
//        }
//        
//        clReleaseKernel(kernelFindOriginalAtts);
//        clReleaseKernel(kernelFindNewAtts);
//        clReleaseKernel(kernelCompareAtts);
//        //clFinish(commandQueue);
//        // Release kernel, program, and memory objects
//        for(int i=0; i<memObjects.length; i++)
//            clReleaseMemObject(memObjects[i]);
//        clReleaseMemObject(mem_PosSelectedNodes);
//        clReleaseMemObject(mem_bufForNewAtts);
//        clReleaseMemObject(mem_newAtts);
//        clReleaseMemObject(mem_resultSizeArrForNewAtts);
//        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//            clReleaseMemObject(mem_resultArr_PINF);
//        } else {
//            clReleaseMemObject(mem_resultArr);
//        }        
//        
//        clReleaseProgram(program);
//        clReleaseCommandQueue(commandQueue);
//        clReleaseContext(context);
//
//        logicTables = null;
//        inv_logicTables = null;
//        posSelectedNodesInt = null;
//        allStates_byte = null;
//        resultSizeArr = null;
//        resultSizeArrForNewAtts = null;
//        
//        originalAtts = null;
//        newAtts = null;
//        System.gc();
//
//        //RobustnessValues robs = new RobustnessValues();
//        robs.set_noRobustStates(resultArr);
//        if (PerturbationType >= Config.MUTATION_KNOCKOUT_PINF){
//            robs.set_pInfValues(ruleIndex, resultArr_PINF);
//        }
//        
//        return;
//    }
//    
    public int [] calRobustness_NestedCanalyzing(ArrayList<Node> nodes, 
            ArrayList<Integer> posSelectedNodes, ArrayList<Integer> allStates, int PerturbationType, int NumPossibleFunc, int mutationTime){
                
        System.out.printf("colin: OpenCL number of items = %d\n", posSelectedNodes.size());
        System.out.printf("colin: OpenCL number of part = %d\n", numPart);
        System.out.printf("colin: OpenCL number of states = %d\n", allStates.size()/numPart);
        /*if(allStates.size() == 1)
        {
             System.out.printf("colin: OpenCL value of states = %d\n", allStates.get(0));
        }*/

        int [] logicTables = Node.getLogicTables(nodes, false);
        int [] inv_logicTables = Node.getLogicTables(nodes, true);
        int [] posSelectedNodesInt = convertIntegerArrToIntArr(posSelectedNodes);
        int [] allStatesArrLong = convertIntegerArrToIntArr(allStates);

        // create result array
        int [] resultArr = new int[NumPossibleFunc*posSelectedNodes.size()];
        for(int i=0; i</*posSelectedNodes.size()*/resultArr.length;i++)
        {
            resultArr[i] = 0;
        }

        int [] bufArr = createStates4CPU(posSelectedNodes.size(), nodes.size(), numPart);//colin: no limit # of nodes in Robustness computation
        int bufSize = (MAXNETWSTATESIZE*numPart + 2*MAXATTSTATESIZE*numPart + 2*nodes.size() + numPart);

        int logicSize = logicTables.length / nodes.size();
        System.out.printf("colin: logicSize = %d\n", logicSize);
        //System.out.println("colin: logicTables     = " + java.util.Arrays.toString(logicTables));
        //System.out.println("colin: inv_logicTables = " + java.util.Arrays.toString(inv_logicTables));
        
        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[6];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * logicTables.length, Pointer.to(logicTables), null);

        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * inv_logicTables.length, Pointer.to(inv_logicTables), null);

        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * posSelectedNodesInt.length, Pointer.to(posSelectedNodesInt), null);

        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * allStatesArrLong.length, Pointer.to(allStatesArrLong), null);

        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * resultArr.length, Pointer.to(resultArr), null);

        memObjects[5] = clCreateBuffer(context,
            CL_MEM_WRITE_ONLY|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * bufArr.length, Pointer.to(bufArr), null);
        
        // Create the program from the source code
        String path = "res/kernels/robust_canal.cl";                
        if(PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION) {
            path = "res/kernels/robust_canal_ko.cl";
        }
        
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel;        
        kernel = clCreateKernel(program, "calRobustness", null);
        
        
        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernel, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        int nextPara = memObjects.length;
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{numPart}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{leftSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{nodes.size()}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{allStatesArrLong.length/numPart}));
        
        if(PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION) {
            int fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT;
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{fixedValue}));
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{mutationTime}));
        } else {
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{PerturbationType}));
        }
        
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{logicSize}));
        

        // Set the work-item dimensions        
        long global_work_size[] = new long[]{posSelectedNodesInt.length};
        long local_work_size[] = new long[]{1};//1024
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[4], CL_TRUE, 0,
            resultArr.length * Sizeof.cl_int, Pointer.to(resultArr), 0, null, null);

        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);

        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);        

        logicTables = null;
        inv_logicTables = null;
        posSelectedNodesInt = null;
        allStatesArrLong = null;
        bufArr = null;
        System.gc();
        
        return resultArr;
    }
    
    public double [] calOutModuleRobustness_NestedCanalyzing(ArrayList<Node> nodes, 
            ArrayList<Integer> posSelectedNodes, ArrayList<Integer> allStates, int PerturbationType, int NumPossibleFunc, int mutationTime,int NumModule){
                
        System.out.printf("colin: OpenCL number of items = %d\n", posSelectedNodes.size());
        System.out.printf("colin: OpenCL number of part = %d\n", numPart);
        System.out.printf("colin: OpenCL number of states = %d\n", allStates.size()/numPart);
        /*if(allStates.size() == 1)
        {
             System.out.printf("colin: OpenCL value of states = %d\n", allStates.get(0));
        }*/
        int [] nodeclusterid=convertNodeClusterIDToIntArr(nodes);
        int [] logicTables = Node.getLogicTables(nodes, false);
        int [] inv_logicTables = Node.getLogicTables(nodes, true);
        int [] posSelectedNodesInt = convertIntegerArrToIntArr(posSelectedNodes);
        int [] allStatesArrLong = convertIntegerArrToIntArr(allStates);

        // create result array
        double [] resultArr = new double[NumPossibleFunc*posSelectedNodes.size()];
        for(int i=0; i</*posSelectedNodes.size()*/resultArr.length;i++)
        {
            resultArr[i] = 0;
        }

        int [] bufArr = createStatesOutModule4CPU(posSelectedNodes.size(), nodes.size(), numPart);//colin: no limit # of nodes in Robustness computation
        int bufSize = (MAXNETWSTATESIZE*numPart + 2*MAXATTSTATESIZE*numPart + 3*nodes.size() + numPart);

        int logicSize = logicTables.length / nodes.size();
        System.out.printf("colin: logicSize = %d\n", logicSize);
        //System.out.println("colin: logicTables     = " + java.util.Arrays.toString(logicTables));
        //System.out.println("colin: inv_logicTables = " + java.util.Arrays.toString(inv_logicTables));
        
        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[7];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * logicTables.length, Pointer.to(logicTables), null);

        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * inv_logicTables.length, Pointer.to(inv_logicTables), null);
        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * nodeclusterid.length, Pointer.to(nodeclusterid), null);

        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * posSelectedNodesInt.length, Pointer.to(posSelectedNodesInt), null);

        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * allStatesArrLong.length, Pointer.to(allStatesArrLong), null);

        memObjects[5] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_double * resultArr.length, Pointer.to(resultArr), null);

        memObjects[6] = clCreateBuffer(context,
            CL_MEM_WRITE_ONLY|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * bufArr.length, Pointer.to(bufArr), null);
        
        // Create the program from the source code
        String path = "res/kernels/robust_canal_outmodule.cl";                
        if(PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION) {
            path = "res/kernels/robust_canal_ko.cl";
        }
        
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel;        
        kernel = clCreateKernel(program, "calOutModuleRobustness", null);
        
        
        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernel, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        int nextPara = memObjects.length;
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{numPart}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{leftSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{nodes.size()}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{allStatesArrLong.length/numPart}));
        
        if(PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION) {
            int fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT;
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{fixedValue}));
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{mutationTime}));
        } else {
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{PerturbationType}));
        }
        
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{logicSize}));
        
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{NumModule}));
        
        // Set the work-item dimensions        
        long global_work_size[] = new long[]{posSelectedNodesInt.length};
        long local_work_size[] = new long[]{1};//1024
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[5], CL_TRUE, 0,
            resultArr.length * Sizeof.cl_double, Pointer.to(resultArr), 0, null, null);

        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);

        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);        

        logicTables = null;
        inv_logicTables = null;
        posSelectedNodesInt = null;
        allStatesArrLong = null;
        bufArr = null;
        nodeclusterid=null;
        System.gc();
        
        return resultArr;
    }
    
    public double [] calInModuleRobustness_NestedCanalyzing(ArrayList<Node> nodes, 
            ArrayList<Integer> posSelectedNodes, ArrayList<Integer> allStates, int PerturbationType, int NumPossibleFunc, int mutationTime,int NumModule){
                
        System.out.printf("colin: OpenCL number of items = %d\n", posSelectedNodes.size());
        System.out.printf("colin: OpenCL number of part = %d\n", numPart);
        System.out.printf("colin: OpenCL number of states = %d\n", allStates.size()/numPart);
        /*if(allStates.size() == 1)
        {
             System.out.printf("colin: OpenCL value of states = %d\n", allStates.get(0));
        }*/
        int [] nodeclusterid=convertNodeClusterIDToIntArr(nodes);
        int [] logicTables = Node.getLogicTables(nodes, false);
        int [] inv_logicTables = Node.getLogicTables(nodes, true);
        int [] posSelectedNodesInt = convertIntegerArrToIntArr(posSelectedNodes);
        int [] allStatesArrLong = convertIntegerArrToIntArr(allStates);

        // create result array
        double [] resultArr = new double[NumPossibleFunc*posSelectedNodes.size()];
        for(int i=0; i</*posSelectedNodes.size()*/resultArr.length;i++)
        {
            resultArr[i] = 0;
        }

        int [] bufArr = createStatesInModule4CPU(posSelectedNodes.size(), nodes.size(), numPart);//colin: no limit # of nodes in Robustness computation
        int bufSize = (MAXNETWSTATESIZE*numPart + 2*MAXATTSTATESIZE*numPart + 3*nodes.size() + numPart);

        int logicSize = logicTables.length / nodes.size();
        System.out.printf("colin: logicSize = %d\n", logicSize);
        //System.out.println("colin: logicTables     = " + java.util.Arrays.toString(logicTables));
        //System.out.println("colin: inv_logicTables = " + java.util.Arrays.toString(inv_logicTables));
        
        // Initialize the context properties
        cl_context_properties contextProperties = new cl_context_properties();
        contextProperties.addProperty(CL_CONTEXT_PLATFORM, platform);
        // Create a context for the selected device
        cl_context context = clCreateContext(
            contextProperties, 1, new cl_device_id[]{device},
            null, null, null);
        // Create a command-queue for the selected device
        cl_command_queue commandQueue = clCreateCommandQueue(context, device, 0, null);

        // Allocate the memory objects for the input- and output data
        cl_mem memObjects[] = new cl_mem[7];
        memObjects[0] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * logicTables.length, Pointer.to(logicTables), null);

        memObjects[1] = clCreateBuffer(context,
            CL_MEM_READ_ONLY | CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * inv_logicTables.length, Pointer.to(inv_logicTables), null);
        
        memObjects[2] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * nodeclusterid.length, Pointer.to(nodeclusterid), null);

        memObjects[3] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * posSelectedNodesInt.length, Pointer.to(posSelectedNodesInt), null);

        memObjects[4] = clCreateBuffer(context,
            CL_MEM_READ_ONLY|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_int * allStatesArrLong.length, Pointer.to(allStatesArrLong), null);

        memObjects[5] = clCreateBuffer(context,
            CL_MEM_READ_WRITE|CL_MEM_COPY_HOST_PTR,
            Sizeof.cl_double * resultArr.length, Pointer.to(resultArr), null);

        memObjects[6] = clCreateBuffer(context,
            CL_MEM_WRITE_ONLY|CL_MEM_USE_HOST_PTR,
            Sizeof.cl_int * bufArr.length, Pointer.to(bufArr), null);
        
        // Create the program from the source code
        String path = "res/kernels/robust_canal_inmodule.cl";                
        if(PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION) {
            path = "res/kernels/robust_canal_ko.cl";
        }
        
        URL url = getClass().getResource(path);
        String programSource = MyOpenCL.readFile(url);
        cl_program program = clCreateProgramWithSource(context,
            1, new String[]{ programSource }, null, null);
        // Build the program
        clBuildProgram(program, 0, null, null, null, null);

        // Create the kernel
        cl_kernel kernel;        
        kernel = clCreateKernel(program, "calInModuleRobustness", null);
        
        
        // Set the arguments for the kernel
        for(int i=0;i<memObjects.length;i++)
        {
            clSetKernelArg(kernel, i,
                Sizeof.cl_mem, Pointer.to(memObjects[i]));
        }
        int nextPara = memObjects.length;
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{bufSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{numPart}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{leftSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{nodes.size()}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{allStatesArrLong.length/numPart}));
        
        
        if(PerturbationType == Config.MUTATION_KNOCKOUT || PerturbationType == Config.MUTATION_OVER_EXPRESSION) {
            int fixedValue = PerturbationType - Config.MUTATION_KNOCKOUT;
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{fixedValue}));
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{mutationTime}));
        } else {
            clSetKernelArg(kernel, nextPara++,
                Sizeof.cl_short, Pointer.to(new int[]{PerturbationType}));
        }
        
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_short, Pointer.to(new int[]{logicSize}));
        clSetKernelArg(kernel, nextPara++,
            Sizeof.cl_int, Pointer.to(new int[]{NumModule}));

        // Set the work-item dimensions        
        long global_work_size[] = new long[]{posSelectedNodesInt.length};
        long local_work_size[] = new long[]{1};//1024
        // Execute the kernel
        clEnqueueNDRangeKernel(commandQueue, kernel, 1, null,
            global_work_size, local_work_size, 0, null, null);

        // Read the output data
        clEnqueueReadBuffer(commandQueue, memObjects[5], CL_TRUE, 0,
            resultArr.length * Sizeof.cl_double, Pointer.to(resultArr), 0, null, null);

        // Release kernel, program, and memory objects
        for(int i=0; i<memObjects.length; i++)
            clReleaseMemObject(memObjects[i]);

        clReleaseKernel(kernel);
        clReleaseProgram(program);
        clReleaseCommandQueue(commandQueue);
        clReleaseContext(context);        

        logicTables = null;
        inv_logicTables = null;
        posSelectedNodesInt = null;
        allStatesArrLong = null;
        bufArr = null;
        nodeclusterid=null;
        System.gc();
        
        return resultArr;
    }
    /**
     * Returns the value of the platform info parameter with the given name
     *
     * @param platform The platform
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_platform_id platform, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetPlatformInfo(platform, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetPlatformInfo(platform, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static String getString(cl_device_id device, int paramName)
    {
        // Obtain the length of the string that will be queried
        long size[] = new long[1];
        clGetDeviceInfo(device, paramName, 0, null, size);

        // Create a buffer of the appropriate size and fill it with the info
        byte buffer[] = new byte[(int)size[0]];
        clGetDeviceInfo(device, paramName, buffer.length, Pointer.to(buffer), null);

        // Create a string from the buffer (excluding the trailing \0 byte)
        return new String(buffer, 0, buffer.length-1);
    }

     /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static int getInt(cl_device_id device, int paramName)
    {
        return getInts(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static int[] getInts(cl_device_id device, int paramName, int numValues)
    {
        int values[] = new int[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_int * numValues, Pointer.to(values), null);
        return values;
    }

    /**
     * Returns the value of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @return The value
     */
    private static long getLong(cl_device_id device, int paramName)
    {
        return getLongs(device, paramName, 1)[0];
    }

    /**
     * Returns the values of the device info parameter with the given name
     *
     * @param device The device
     * @param paramName The parameter name
     * @param numValues The number of values
     * @return The value
     */
    private static long[] getLongs(cl_device_id device, int paramName, int numValues)
    {
        long values[] = new long[numValues];
        clGetDeviceInfo(device, paramName, Sizeof.cl_long * numValues, Pointer.to(values), null);
        return values;
    }
    /**/
    private static String readFile(URL url)
    {
        try
        {
            //BufferedReader br = new BufferedReader(new FileReader(fileName));
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(url.openStream()));

            StringBuilder sb = new StringBuilder();
            String line = null;
            while (true)
            {
                line = br.readLine();
                if (line == null)
                {
                    break;
                }
                sb.append(line+"\n");
            }
            return sb.toString();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            return "";
        }
    }
    
    public static void main(String[] args)
    {
        MyOpenCL mycl = new MyOpenCL();
        mycl.runExample();
        //mycl.runExample_Class();
    }

    public static void showMemory(String label) {
        Runtime rt = Runtime.getRuntime();
        long free = rt.freeMemory();
        long total = rt.totalMemory();
        long used = total - free;
        System.out.println(label + "\t" + free + "\t" + total + "\t" + used);
    }

    private long [] calculateSizeOfImage2D(int sizeData)
    {
        long [] size = new long[3];

        // find log 2 of sizeData
        int v = sizeData;  // 32-bit value to find the log2 of
        int b[] = {0x2, 0xC, 0xF0, 0xFF00, 0xFFFF0000};
        int S[] = {1, 2, 4, 8, 16};
        int i;

        int r = 0; // result of log2(v) will go here
        for (i = 4; i >= 0; i--) // unroll for speed...
        {
            if ((v & b[i]) != 0) {
                v >>= S[i];
                r |= S[i];
            }
        }

        if(Math.pow(2, r) < sizeData)
        {
            r++;
        }
        
        //System.out.printf("colin: log2 = %d\n", r);
        int max = r/2;        
        if(max < r-max)
            max = r-max;

        size[0] = (long)Math.pow(2,max);
        size[1] =  sizeData/size[0];
        if(size[1]*size[0] <= sizeData)
            size[1]++;
        
        size[2] = max;
        //System.out.printf("colin: size log2 = %d %d\n", size[0],size[1]);
        return size;
    }

    public static String convertToBinaryString(String state, boolean skip)
    {
        if(skip)
            return state;

        String [] childStates = state.split(" ");
        if(childStates.length == MyOpenCL.numPart)
        {
            String s = "";
            int NumOfBitPerState = MAXBITSIZE;
            for (int k = 0; k < numPart; k++) {
                if (k == numPart - 1 && leftSize > 0) {
                    NumOfBitPerState = leftSize;
                }

                int l = Integer.parseInt(childStates[k]);
                String s0 = Integer.toBinaryString(l);
                StringBuilder s1 = new StringBuilder("");
                for (int p = 0; p < NumOfBitPerState - s0.length(); p++) {
                    s1.append("0");
                }
                String stemp = s1.toString().concat(s0);

                s = s.concat(stemp);
            }

            return s;
        }
        else
            return state;
    }

    public Attractor getATT(int[] resultATTArr, int index, int att_size) {
        int structSize = att_size * numPart;
        int temp;
        int[] l = new int[numPart];

        if (resultATTArr[index * structSize] < 0) {
            return null;
        }

        Attractor attractor = new Attractor();
        temp = 0;
        for (int j = 0; j < att_size; j++) {
            if (resultATTArr[index * structSize + temp] < 0) {
                break;
            }

            for (int k = 0; k < numPart; k++) {
                l[k] = resultATTArr[index * structSize + temp];
                ++temp;
            }

            //cal state
            String state = "";
            int NumOfBitPerState = MAXBITSIZE;
            for (int k = 0; k < numPart; k++) {
                if (k == numPart - 1 && leftSize > 0) {
                    NumOfBitPerState = leftSize;
                }

                String s0 = Integer.toBinaryString(l[k]);
                StringBuilder s1 = new StringBuilder("");
                for (int p = 0; p < NumOfBitPerState - s0.length(); p++) {
                    s1.append("0");
                }
                String stemp = s1.toString().concat(s0);
                state = state.concat(stemp);
            }
            attractor.States.add(state);
        }

        attractor.Length = attractor.States.size() - 1;
        return attractor;
    }

    private void addCoupledFBLs(ArrayList<FBL> AllDistinctFBLs, int MaxLength, int noFoundCoupledFBLs, int [] resultArr, ArrayList<CoupleFBL> CoupleFBLs){
        int numBytesInStruct = (2 + MaxLength + 1);
        int iFBL1;
        int iFBL2;        
        if(noFoundCoupledFBLs > MAXNUMCPFBLs)
            noFoundCoupledFBLs = MAXNUMCPFBLs;
            
        for (int i = 0; i < noFoundCoupledFBLs; i++) {
            //if(resultArr[i*numBytesInStruct + 2] == 0)
            //    break;

            //System.out.printf("%d %d - ", resultArr[i*numBytesInStruct],resultArr[i*numBytesInStruct+1]);
            ArrayList<String> SharedNodes = new ArrayList<String>();
            for (int j = 0; j <= MaxLength; j++) {
                if (resultArr[i * numBytesInStruct + 2 + j] == -1) {
                    break;
                }
                /*if (resultArr[i * numBytesInStruct + 2 + j] < 0) {
                    System.out.println("i/j = " + i + "/" + j + "/" + resultArr[i * numBytesInStruct + 2 + j]);
                    
                }*/
                SharedNodes.add(Common.indexIDs.get(Common.nodeIDsArr.get(resultArr[i * numBytesInStruct + 2 + j])));
                //System.out.printf("%d ", resultArr[i*numBytesInStruct + 2 + j]);
            }
            //System.out.println("");

            if (SharedNodes.size() > 0) {
                //System.out.println("Intersection Length " + (SharedNodes.size()-1) + "\t" + SharedNodes.toString());
                iFBL1 = resultArr[i * numBytesInStruct];
                iFBL2 = resultArr[i * numBytesInStruct + 1];
                CoupleFBL cf = new CoupleFBL();
                cf.IntersectionLength = SharedNodes.size() - 1;
                cf.coherent = (AllDistinctFBLs.get(iFBL1).type == AllDistinctFBLs.get(iFBL2).type) ? true : false;
                cf.fbl1 = AllDistinctFBLs.get(iFBL1);
                cf.fbl2 = AllDistinctFBLs.get(iFBL2);
                cf.SharedNodes = SharedNodes;

                CoupleFBLs.add(cf);
            }
        }
    }    
    /*private int convertBytesToInt(ByteBuffer buff, int index)
    {
        index = index*4;
        byte b1 = buff.get(index);
        byte b2 = buff.get(index+1);
        byte b3 = buff.get(index+2);
        byte b4 = buff.get(index+3);
        int i = ((0xFF & b4) << 24) | ((0xFF & b3) << 16) |
                ((0xFF & b2) << 8) | (0xFF & b1);
        return i;
    }*/
}
