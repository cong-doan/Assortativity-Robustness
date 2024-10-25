#define MAXNETWSTATESIZE 150
#define MAXATTSTATESIZE 20
#define MAXBITSIZE 31

inline void calState(__global int *total,__global int *state, short numNodes, short numPart, short leftSize);
inline void converLongToStringArr(__global int *state,__global const int *allStates,
int sI,short numPart, short leftSize,short numNodes);

__kernel void findAllAttractor(__global const int *logicTables, 
			__global const int *allStates,
			__global int *resultATTArr,__global int *resultTransArr,
			__global int *bufArr, int bufSize,
			int numStates,
			short numPart, short leftSize,short numNodes,
			int pass, short logicSize)
{	
	int iState = get_global_id(0);
	if(iState + pass >= numStates)
		return;
		
	__global int *transitionNetworkState = bufArr + iState*bufSize;
	__global int *state = transitionNetworkState + MAXNETWSTATESIZE*numPart;
	__global int *newstate = state + numNodes;
	__global int *nextState = newstate + numNodes;
	__global int *prevState = nextState + numPart;	

	int temp = (iState+pass)*numPart;	
	// add current network state
	for(int i=0;i<numPart;i++)
	{
		transitionNetworkState[i] = allStates[temp+i];
	}
	converLongToStringArr(state,allStates,temp,numPart, leftSize,numNodes);			   	
	calState(nextState,state, numNodes, numPart,leftSize);
		
	__global int *l_resultTransArr = resultTransArr + 2*iState*MAXNETWSTATESIZE*numPart;
	int iTrans = 0;
	resultATTArr[(iState+pass)*MAXATTSTATESIZE*numPart] = -1;

	short indexTransNetw=1;
	int i,j;	
    bool converged=false;
	short startIndexAtt=-1;

    __global const int *logics;
    
	while(true)
	{
		for(i=0;i<numPart;i++)
			prevState[i] = nextState[i];

		// calculate next state
		for(i=0;i<numNodes;i++)
        {
            //newstate[i] = state[i];
            logics = logicTables + i * logicSize;            
            
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

		calState(nextState,newstate, numNodes,numPart,leftSize);
		for(i=0;i<numNodes;i++)
			state[i] = newstate[i];

		// add new transition state
		//short numBytesInStruct = 2*numPart;
		for(i=0;i<numPart;i++)
		{
			l_resultTransArr[iTrans++] = prevState[i];
		}
		for(i=0;i<numPart;i++)
		{
			l_resultTransArr[iTrans++] = nextState[i];
		}		

		// checkConvergence
		converged = false;
		for(i=0;i<indexTransNetw;i++)
		{
			temp = i*numPart;
			for(j=0;j<numPart;j++)
			{
				if(nextState[j] != transitionNetworkState[temp+j])
				{
					break;
				}
			}

			if(j==numPart)
			{
				converged = true;
				startIndexAtt = i;
				break;
			}
		}

		// add nextState
		for(i=0;i<numPart;i++)
		{
			transitionNetworkState[indexTransNetw*numPart+i] = nextState[i];
		}
		++indexTransNetw;
		if(indexTransNetw >= MAXNETWSTATESIZE)
			break;
		//

		if(converged)
		{
			break;
		}
	}//end while

	//Calculate information of attractor
	l_resultTransArr[iTrans] = -1;
	if(converged==true)
	{        
		int NumOfPassedState = indexTransNetw;

		if(NumOfPassedState-startIndexAtt>MAXATTSTATESIZE)
			return;

		int structSize = MAXATTSTATESIZE*numPart;
		int iMin = startIndexAtt;
		int temp2;
		int iAtt = iState+pass;

		for(i=startIndexAtt+1;i<NumOfPassedState-1;i++)
		{
			temp = i*numPart;
			temp2 = iMin*numPart;
			for(j=0;j<numPart;j++)
			{
				if(transitionNetworkState[temp2+j] > transitionNetworkState[temp+j])
				{
					iMin = i;
					break;
				}
				
				if(transitionNetworkState[temp2+j] < transitionNetworkState[temp+j])
					break;				
			}
		}

		temp=0;
		for(i=iMin;i<NumOfPassedState-1;i++)
		{	//Last state is the same as first one.
			// add current network state
			for(j=0;j<numPart;j++)
			{
				resultATTArr[iAtt*structSize+temp] = transitionNetworkState[i*numPart+j];
				++temp;
			}
        }

		for(i=startIndexAtt;i<=iMin;i++)
		{	
			for(j=0;j<numPart;j++)
			{
				resultATTArr[iAtt*structSize+temp] = transitionNetworkState[i*numPart+j];
				++temp;
			}
        }
		if(NumOfPassedState-startIndexAtt < MAXATTSTATESIZE)
		{
			resultATTArr[iAtt*structSize+temp] = -1;
		}
	}	

	return;
}

inline void calState(__global int *total,__global int *state, short numNodes, short numPart, short leftSize)
{
	int m = 1;
	short endI;
	short num;

	for(int t=0;t<numPart;t++)
	{
		endI = (t+1)*MAXBITSIZE-1;
		num = MAXBITSIZE;

		if(t == numPart-1 && leftSize > 0)
		{
			endI = numNodes-1;
			num = leftSize;
		}

		m=1;
		total[t]=0;
		for(int l=0;l<num;l++)
		{
			//if(endI < 0)
			//	break;

			total[t] += m*state[endI--];
			m=m*2;
		}
	}

	return;
}

inline void converLongToStringArr(__global int *state,__global const int *allStates,
int sI,short numPart, short leftSize,short numNodes)
{
		int i;
		//reset state
		for(i=0;i<numNodes;i++)
		{
			state[i] = 0;
		}

		// convert long array to binary string
		//convertLongToBString(sI,allStates,state, numPart, leftSize, numNodes);
		for(i=0;i<numPart;i++)
		{
			short endI = (i+1)*MAXBITSIZE - 1;
			if(i == numPart-1 && leftSize > 0)
			{
				endI = numNodes-1;
			}

			int l = allStates[sI+i];
			while(l>0 && endI >= 0)
			{
				state[endI--] = l%2;
				l = l/2;
			}
		}
		//
}
