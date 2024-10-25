#define MAXNUMFBLs 2500000
#define MAXNUMCPFBLs 1500000
#define MAXPATHLEN 1000
#define MAXEXECUTION 10240

#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable

__kernel void findAllFBLsOf1NodeWithSpecifiedLength(
			__global const int *firstIndexSrcNodeInEdgesArr,
            __global const int *rndinaIntArr, __global const int *indexExamineNodes,
			__global const int *indexNodeInSelectedNodeArr,
			__global int *g_buff,
			__global int *resultArr, int length, int findMaximal,
			int numWorkItems, int stackSize, int bufSize)
{
	int id = get_global_id(0);
	if(id >= numWorkItems) return;    

	__global int *buff = g_buff + 2 + id*bufSize;//g_buff[0] for the index of saved FBL
	int stackIndex = buff[0];
	if(stackIndex < 0) return;

	int indexExamineNode = indexExamineNodes[id];
	int indexArr = indexNodeInSelectedNodeArr[indexExamineNode];    
		
	__global int *stack = buff + 1;
	__global int *depth = stack + stackSize;	
		//index 0: pos of node in rndinaIntArr
		//index 1: depth of node (=vissitedcount)
	
	// get all adjacent nodes of nodeID
	int indexNode;// = paths[visitedcount][0];
	int maxPathLen = length+1;
	int structsize = maxPathLen + 2;

	__global int *pathNodes = depth + stackSize;
	__global int *pathTypes = pathNodes + maxPathLen;
	__global int *iEdges = pathTypes + maxPathLen;

	// init stack
	int visitedcount = 0, pos;
	int numExec = 0;

	while(stackIndex >= 0)
	{
	if(numExec > MAXEXECUTION)
	{
		break;
	}

	pos = stack[stackIndex];
	visitedcount = depth[stackIndex];
	-- stackIndex;

	indexNode = rndinaIntArr[pos+1];	
	pathNodes[visitedcount] = indexNode;
	pathTypes[visitedcount] = rndinaIntArr[pos+2];
	iEdges[visitedcount] = pos/3;

	// check indexNode OKE or leaf node
	if(indexNode == indexExamineNode)
	{
		if(findMaximal == 1 || (visitedcount == length))
		{
			int index = atom_inc(&g_buff[0]) + 1;			
			// check max index
			if(index >= MAXNUMFBLs)
			{
				//stackIndex = -1;
                ++ stackIndex;
				break;
			}

			int si = index*structsize;
			int lengthPath = visitedcount;
			//int NumOfNeg = 0;
			//int NumOfNeu = 0;

			// add path to resultArr
			resultArr[si + 0] = pathTypes[0];	//IncomingTypeOfStartNode
			//resultArr[si + 2] = lengthPath;		//length

			si += 2;
			for(int i=0;i<lengthPath;i++){
				resultArr[si + i] = iEdges[i+1];	//paths[i][0];
				//resultArr[si + MAXPATHLEN + i] = paths[i+1][1];	//OutcomingTypeOfNode

				/*if(pathTypes[i+1] == -1)
				{
					NumOfNeg++;
				}
				else if(pathTypes[i+1] == 0)
				{
					NumOfNeu++;
				}*/
			}
			//colin: add for GPU
			for(int i=lengthPath;i<maxPathLen;i++){
				resultArr[si + i] = -1;
			}
			//

			/*if(NumOfNeu == lengthPath)
			{
				resultArr[si - 1] = 0;
			}
			else
			{
				if(NumOfNeg%2 == 0)
				{
					resultArr[si - 1] = 1;
				}
				else
				{
					resultArr[si - 1] = -1;
				}
			}*/
		}

		// continue pop stack, no find neighbor more
		++numExec;
		continue;
	}

	// check depth with max length
	if((visitedcount + 1) > length)
	{
		continue;
	}

	int k = firstIndexSrcNodeInEdgesArr[indexNode];
	if(k<0)
	{
		continue;	
	}
	
	while(indexNode == rndinaIntArr[k])
	{
		int destNode = rndinaIntArr[k+1];			

		bool visited = false;
		for(int i=0;i<=visitedcount;i++)
		{
			if(destNode == pathNodes[i])
			{
				visited = true;
				break;
			}
		}

		// colin: remove redundant paths
		if(!visited && indexNodeInSelectedNodeArr[destNode] >= indexArr)
		{
			// destNode OKE, push to stack
			++stackIndex;
			stack[stackIndex] = k;					//pos
			depth[stackIndex] = visitedcount + 1;	//depth
		}
		k+=3;
		++numExec;
	}
	}//stack While

	buff[0] = stackIndex;
	if(stackIndex >= 0)
		g_buff[1] = 0;
}

__kernel void findAllCoupledFBLs(__global const int * fblArr,
			__global int *resultArr, __global int *indexResult, __global int *done,
			int pass)
{
	int id = get_global_id(0);
    if(done[id] == 1) return;
	int maxPathLen = indexResult[1];
	int numBytes = maxPathLen+2;

	int start = pass*numBytes;
	int start2 = (id+pass+1)*numBytes;//start;

	int i,j,k;
	int pos;
	int size1, size2;
	__local int SharedNodes[MAXPATHLEN+1];
	__local int f1nodes[MAXPATHLEN+1];
	__local int f2nodes[MAXPATHLEN+1];
	__local int temp1[MAXPATHLEN+1];
	__local int *fnodes;

	int sizeShared;
	int FBLSIZE = 2 + maxPathLen + 1;
	size1 = fblArr[start];

	//while(true)
	{
		//start2 += numBytes;
		if(fblArr[start2] == 0){
            done[id] = 1;
			return;//break;
        }

		size2 = fblArr[start2];

		//find shared node
		sizeShared = 0;
		for(i=0;i<size1-1;i++)
		{
			for(j=0;j<size2-1;j++)
			{
				if(fblArr[start+1+i] == fblArr[start2+1+j])
				{
					SharedNodes[sizeShared] = fblArr[start+1+i];
					sizeShared++;
					break;
				}
			}
		}

		if(sizeShared<2){
            done[id] = 1;
            return;//continue;
        }

		for(i=0;i<size1-1;i++)
			f1nodes[i] = fblArr[start+1+i];
		for(i=0;i<size2-1;i++)
			f2nodes[i] = fblArr[start2+1+i];

		int element = SharedNodes[0];
		//reorder two array
		fnodes = f1nodes;
		int size = size1;

		for(k=0;k<2;k++)
		{
			if(k==1)
			{
				fnodes = f2nodes;
				size = size2;
			}

			pos = -1;
			for(i=0;i<size-1;i++)
			{
				if(fnodes[i] == element)
				{
					pos = i;
					break;
				}
			}

			j=0;
			for(i=pos;i<size-1;i++)
			{
				temp1[j] = fnodes[i];
				j++;
			}
			for(i=0;i<pos;i++)
			{
				temp1[j] = fnodes[i];
				j++;
			}

			for(i=0;i<size-1;i++)
				fnodes[i] = temp1[i];
		}
		//

		//Step 3
        //From begining
        int Min = (size1<size2)?size1-1:size2-1;
        int M1=0;
        i=0;
        j=0;
        while(true){
            if(i>=Min||j>=Min) break;
            if(f1nodes[i] == f2nodes[j]){
                M1++;
                i++;
                j++;
            }else{
                break;
            }
        }

        //From ending
        int M2=0;
        i=size1-2;
        j=size2-2;
        while(true){
            if(i<0||j<0) break;
            if(f1nodes[i] == f2nodes[j]){
                M2++;
                i--;
                j--;
            }else{
                break;
            }
        }

        int M=M1+M2;
        if(M>=sizeShared)
		{
            int index = atom_inc(&indexResult[0]) + 1;
			if(index >= MAXNUMCPFBLs)
				return;

			int si = index*FBLSIZE;
			resultArr[si] = pass;//id;
			resultArr[si+1] = id+pass+1;//start2/numBytes;

			for(i=0;i<sizeShared;i++)
			{
				resultArr[si+2+i] = SharedNodes[i];

			}
			for(i=sizeShared; i<=maxPathLen; i++)
			{
				resultArr[si+2+i] = -1;
			}
        }
		//end
	}
    done[id] = 1;
}
