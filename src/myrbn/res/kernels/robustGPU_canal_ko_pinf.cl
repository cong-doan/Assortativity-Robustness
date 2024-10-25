#pragma OPENCL EXTENSION cl_khr_byte_addressable_store : enable

#define NUMNETWSTATESIZE 150
#define MAXATTSTATESIZE 20
#define BUFSIZE_FOR_ORIGINAL_ATTS 22
#define BUFSIZE_FOR_NEW_ATTS 22

inline int findAtt(__global const int *logicTables, __global const int *inv_logicTables,
__global char *state, __global char *newstate, 
const int numNodes,
short logicSize, int perturbed_pos, short fixedValue, short mutationTime, 
__global char *transitionNetworkState,
__global char *att);

inline int findGCD(int a, int b);
inline int findLCM(int a, int b);

__kernel void findOriginalAtts(__global const int *logicTables, 
			__global const char *allStates,			
			__global int *resultSizeArr,
			__global char *resultAtts,
			__global char *bufArr,			
			const int numNodes,
			const int numExaminedNodes,
			short logicSize, 
            const int pass, __global const int *posSelectedNodesInt)
{	
	int iNode = get_global_id(0);
	if(iNode >= numExaminedNodes)
		return;
	int sI = iNode*numNodes;	

	__global char *transitionNetworkState = bufArr + sI*BUFSIZE_FOR_ORIGINAL_ATTS;	
	__global char *state = transitionNetworkState + MAXATTSTATESIZE*numNodes;
	__global char *newstate = state + numNodes;	

	__global char *att1 = resultAtts + sI*MAXATTSTATESIZE;		
	
    int pos = posSelectedNodesInt[iNode];
	sI = pass;
	// add current network state		
	for(int i=0;i<numNodes;i++)
	{
		//transitionNetworkState[i] = allStates[sI];
		state[i] = allStates[sI];			
		++sI;
	}		
    //swap state[0] & state[pos]: to ensure state[pos] = fixedValue
    /*sI = state[pos];
    state[pos] = state[0];
    state[0] = sI;*/
    // update current network state	
    for(int i=0;i<numNodes;i++)
	{
		transitionNetworkState[i] = state[i];
	}
        
	int size1 = findAtt(logicTables, logicTables, state,newstate,
						numNodes, logicSize, -1, -1, -1, transitionNetworkState, att1);
	resultSizeArr[iNode] = size1;
}
//-----------------------------------------------------------------------------------------------------------------------------------
__kernel void findNewAtts(__global const int *logicTables, __global const int *inv_logicTables, 
			__global const int *posSelectedNodesInt,
			__global const char *allStates,			
			__global int *resultSizeArr,
			__global char *resultAtts,
			__global char *bufArr,			
			const int numNodes,
			const int numStates,
			const int typePert,
			const int pass,			
            const int numExaminedNodes,
            short logicSize, short fixedValue, short mutationTime)
{	
	int iNode = get_global_id(0);
	if(iNode >= numExaminedNodes)
		return;
	int sI = iNode*numNodes;		

	__global char *transitionNetworkState = bufArr + sI*BUFSIZE_FOR_NEW_ATTS;	
	__global char *state = transitionNetworkState + MAXATTSTATESIZE*numNodes;
	__global char *newstate = state + numNodes;	
	//__global char *funcNodes = newstate + numNodes;	

	__global char *att1 = resultAtts + sI*MAXATTSTATESIZE;		
	int pos = posSelectedNodesInt[iNode];
	sI = pass;//*numNodes;
	
	// add current network state		
	for(int i=0;i<numNodes;i++)
	{		
		state[i] = allStates[sI];			
		++sI;
	}		

	//swap state[0] & state[pos]: to ensure state[pos] = fixedValue
    /*sI = state[pos];
    state[pos] = state[0];
    state[0] = sI;*/
    // update current network state	
    for(int i=0;i<numNodes;i++)
	{
		transitionNetworkState[i] = state[i];
	}

	int size1 = findAtt(logicTables, inv_logicTables, state,newstate,
						numNodes, logicSize, pos, fixedValue, mutationTime, transitionNetworkState, att1);
	resultSizeArr[iNode] = size1;
}
//-----------------------------------------------------------------------------------------------------------------------------------
__kernel void compareAtts(
			__global int *sizeOriginalAtts,
			__global int *sizeNewAtts,
			__global char *originalAtts,			
			__global char *newAtts,			
			__global int *resultArr,
			const int numNodes,			
			const int pass,
            const int indexUpdateRule,
            const int numExaminedNodes)
{	
	int iNode = get_global_id(0);
	if(iNode >= numExaminedNodes)
		return;	
	
	__global char *att1 = originalAtts + iNode*MAXATTSTATESIZE*numNodes;
	__global char *att2 = newAtts + iNode*MAXATTSTATESIZE*numNodes;
	int size1 = sizeOriginalAtts[iNode];
	int size2 = sizeNewAtts[iNode];

		// compare 2 attractors
		int iC = -1;
		int i,j,temp;
		if(size1 == size2)
		{
			// compare att1[0] with all elements of att2 arr
			temp = 0;
			for(i=0;i<size2;i++)
			{				
				for(j=0;j<numNodes;j++)
				{
					if(att1[j] != att2[temp+j])
					{
						break;
					}
				}

				if(j==numNodes)
				{
					iC = i;
					break;
				}

				temp += numNodes;
			}

			if(iC == -1)
				return;//continue;

			i=0;
			j=iC;
			int temp1 = i*numNodes;
			int temp2 = j*numNodes;
			while(i<size1)
			{				
				for(temp=0;temp<numNodes;temp++)
				{
					if(att1[temp1+temp] != att2[temp2+temp])
					{
						break;
					}
				}

				if(temp == numNodes)
				{
					i++;
					j++;
					temp1 += numNodes;
					temp2 += numNodes;
					if(j == size2)
					{
						j = 0;
						temp2 = 0;
					}
				}
				else
				{
					break;
				}
			}//end while

			if(i==size1)
			{
				// all state in two attractors att1 and att2 is same
				//++NumOfRobustStates;
				resultArr[iNode + indexUpdateRule]++;
			}
		}			

	//resultArr[get_global_id(0)] = NumOfRobustStates;
}
//-----------------------------------------------------------------------------------------------------------------------------------
__kernel void compareAtts_PINF(
            __global const int *posSelectedNodesInt,
			__global int *sizeOriginalAtts,
			__global int *sizeNewAtts,
			__global char *originalAtts,			
			__global char *newAtts,			
			__global float *resultArr,
			const int numNodes,			
			const int pass,
            const int indexUpdateRule,
            const int numExaminedNodes)
{	
	int iNode = get_global_id(0);
	if(iNode >= numExaminedNodes)
		return;	
	
	__global char *att1 = originalAtts + iNode*MAXATTSTATESIZE*numNodes;
	__global char *att2 = newAtts + iNode*MAXATTSTATESIZE*numNodes;
	int size1 = sizeOriginalAtts[iNode];
	int size2 = sizeNewAtts[iNode];

    if(size1 <= 0 || size2 <= 0) {
        return;
    }
    
    int mergedSize = findLCM(size1, size2);
    int pos = posSelectedNodesInt[iNode];
    
	// compare 2 attractors	
    int distance;
    int pointer1, pointer2;
    
    for(int n = 0; n < numNodes; n ++)
    {
        distance = 0;
        pointer1 = 0;
        pointer2 = 0;
        
		for(int s = 0; s < mergedSize; s ++)
		{
			// compare values of the node "n" in att1 and att2
			distance += abs(att1[pointer1*numNodes + n] - att2[pointer2*numNodes + n]);
						
            pointer1 ++;
            pointer2 ++;
            if(pointer1 == size1) {
                pointer1 = 0;
            }
            if(pointer2 == size2) {
                pointer2 = 0;
            }            												
		}
        
        resultArr[pos*numNodes + n] += convert_float(distance) / mergedSize;
    }//end: For (n)	
}

    inline int findLCM(int a, int b) {
        // Tim Boi Chung Nho Nhat cua hai so nguyen duong
        int GCD = findGCD(a, b);
        return (a*b)/GCD;
    }
    
    inline int findGCD(int a, int b) {
        // Tim Uoc Chung Lon Nhat cua hai so nguyen duong
        if(a < b) {
            int temp = a;
            a = b;
            b = temp;
        }
        
        int r;
        while (b!=0){
            r = a % b;
            a = b;
            b = r;
        }

        return a;
    }
    
inline int findAtt(__global const int *logicTables, __global const int *inv_logicTables,
__global char *state, __global char *newstate, 
const int numNodes,
short logicSize, int perturbed_pos, short fixedValue, short mutationTime, 
__global char *transitionNetworkState,
__global char *att)
{	
	short indexTransNetw=1;
	int i,j;
	int temp;

    bool converged=false;

	short startIndexAtt=-1;
	short indexTrans = 0;	//can replace at index 0	

	__global const int *logics;
	
	while(true)
	{
		// calculate next state			
		for(i=0;i<numNodes;i++)
        {
            logics = logicTables + i * logicSize;
            if(i == perturbed_pos && mutationTime > 0)
            {
                //logics = inv_logicTables + i * logicSize;   //perturbed node
                newstate[i] = fixedValue;
                -- mutationTime;
                continue;
            }            
            
            j = 0;
            temp = state[i];  //is output
            while(logics[j] != -1)
            {
                temp = logics[j + 2];
                if(state[logics[j]] == logics[j + 1])
                {                    
                    break;
                }
                j += 3;
            }
            
            if(j > 0 && logics[j] == -1)    //is not an input node; output = O_default = 1 - Om
                temp = 1 - temp;
            newstate[i] = temp;
		}        

		for(i=0;i<numNodes;i++)
			state[i] = newstate[i];

		// checkConvergence
		converged = false;
		int numExamined = MAXATTSTATESIZE;
		if(indexTransNetw < MAXATTSTATESIZE)
			numExamined = indexTransNetw;

		temp = 0;
		for(i=0;i<numExamined;i++)
		{			
			for(j=0;j<numNodes;j++)
			{
				if(state[j] != transitionNetworkState[temp+j])
				{
					break;
				}
			}

			if(j==numNodes)
			{
				converged = true;
				startIndexAtt = i;
				break;
			}

			temp += numNodes;
		}

		// add nextState
		if(!converged)
		{
			temp = indexTransNetw*numNodes;
			if(indexTransNetw >= MAXATTSTATESIZE)
			{
				temp = indexTrans*numNodes;
				++indexTrans;
				if(indexTrans == MAXATTSTATESIZE)
					indexTrans = 0;
			}

			for(i=0;i<numNodes;i++)
			{
				transitionNetworkState[temp+i] = state[i];
			}
			++indexTransNetw;
			if(indexTransNetw >= NUMNETWSTATESIZE)
				return 0;
		}
		else
		{
			break;
		}
	}//end while

	//Calculate information of attractor
	if(converged==true)
	{		
		//remove end state
		int size = 0;
		int end1 = 0;
		int end2 = 0;		
		
		if(startIndexAtt < indexTrans)
		{
			end1 = indexTrans;			
			size = indexTrans - startIndexAtt;
		}
		else
		{
			int numExamined = MAXATTSTATESIZE;
			if(indexTransNetw < MAXATTSTATESIZE)
				numExamined = indexTransNetw;

			end1 = numExamined;
			end2 = indexTrans;			
			size = numExamined - startIndexAtt + indexTrans;
		}

		temp = 0;
		for(i=startIndexAtt;i<end1;i++)
		{
			int tempTrans = i*numNodes;
			for(j=0;j<numNodes;j++)
			{
				att[temp+j] = transitionNetworkState[tempTrans+j];
			}
			temp+=numNodes;			
		}

		for(i=0;i<end2;i++)
		{
			int tempTrans = i*numNodes;
			for(j=0;j<numNodes;j++)
			{
				att[temp+j] = transitionNetworkState[tempTrans+j];
			}
			temp+=numNodes;		
		}

		return size;
	}
	
	return 0;
}


