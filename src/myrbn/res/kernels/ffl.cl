#define MAXNUMFBLs 2500000
#define MAXNUMCPFBLs 700000
#define MAXPATHLEN 1000
#define MAXEXECUTION 10240

#pragma OPENCL EXTENSION cl_khr_global_int32_base_atomics : enable

__kernel void findAllFFLs(
			__global const int *indexNodes,
			__global const int *destNodes,
			__global const int *firstIndexSrcNodeInEdgesArr,
            __global const int *rndinaIntArr,			
			__global int *g_buff,
			__global int *resultArr, int length, int findMaximal,
			int numCoupledNodes, int stackSize, int bufSize,
			int pass)
{
	int id = get_global_id(0);
	if(id + pass >= numCoupledNodes) return;    

	__global int *buff = g_buff + 2 + id*bufSize;//g_buff[0] for the index of saved FBL
	int stackIndex = buff[0];
	if(stackIndex < 0) return;

	//int indexExamineNode = indexExamineNodes[id];
	//int indexArr = indexNodeInSelectedNodeArr[indexExamineNode];    
	int indexExamineNode = indexNodes[id+pass];
	int indexNodeDst = destNodes[id+pass];
	if(indexNodeDst == indexExamineNode) return;
		
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
	if(visitedcount < 0)
		continue;
	if(pos == -1)
	{
		indexNode = indexExamineNode;
		pathNodes[visitedcount] = indexNode;
		pathTypes[visitedcount] = 0;
		iEdges[visitedcount] = 0;
	}
	else
	{
	indexNode = rndinaIntArr[pos+1];	
	pathNodes[visitedcount] = indexNode;
	pathTypes[visitedcount] = rndinaIntArr[pos+2];
	iEdges[visitedcount] = pos/3;
	}

	// check indexNode OKE or leaf node
	if(indexNode == indexNodeDst)
	{
		if(findMaximal == 1 || (visitedcount == length))
		{
			int index = atom_inc(&g_buff[0]) + 1;			
			// check max index
			if(index >= MAXNUMFBLs)
			{
				stackIndex = -1;
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

			// remove all Node with same depth in stack
			//if(!maximal || (visitedcount == length))
			{
				int i=stackIndex;
				while(i >=0 && depth[i] == visitedcount)
				{
					pos = stack[i];					
					if(pos == -1)
					{
						indexNode = indexExamineNode;						
					}
					else
					{
						indexNode = rndinaIntArr[pos+1];
					}
				
					if(indexNode == indexNodeDst)
					{
						depth[i] = -1;
					}

					--i;
				}
			}
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
		if(!visited)
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