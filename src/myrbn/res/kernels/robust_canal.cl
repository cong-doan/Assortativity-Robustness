#define MAXNETWSTATESIZE 150
#define MAXATTSTATESIZE 20
#define MAXBITSIZE 31

inline void calState(__global int *total,__global int *state, short numNodes, short numPart, short leftSize);

inline int findAtt(__global const int *logicTables, __global const int *inv_logicTables,
__global int *state,__global int *newstate,
__global int *nextState,
__global int *transitionNetworkState,
short numPart, short leftSize,short numNodes,
short logicSize, int pos, 
__global int *att);

inline void converLongToStringArr(__global int *state,__global const int *allStates,
int sI,short numPart, short leftSize,short numNodes);

__kernel void calRobustness(__global const int *logicTables, __global const int *inv_logicTables, 
			__global const int *posSelectedNodesInt,
			__global const int *allStates,
			__global int *resultArr,
			__global int *bufArr, int bufSize,			
			short numPart, short leftSize,short numNodes,
			int numStates,short typePert, short logicSize)
{
	int pos = posSelectedNodesInt[get_global_id(0)];
	int iState,i,j,temp;
	int sI;

	__global int *transitionNetworkState = bufArr + get_global_id(0)*bufSize;	//[MAXNETWSTATESIZE*MAXNUMPART];
	__global int *att1 = transitionNetworkState + MAXNETWSTATESIZE*numPart;	//[MAXATTSTATESIZE*MAXNUMPART];
	__global int *att2 = att1 + MAXATTSTATESIZE*numPart;	//[MAXATTSTATESIZE*MAXNUMPART];

	int NumOfRobustStates=0;
	
	__global int *state = att2 + MAXATTSTATESIZE*numPart;	//[MAXNODES];
	__global int *newstate = state + numNodes;			//[MAXNODES];	
	//__global int *funcNodes = newstate + numNodes;		//[MAXNODES];
	__global int *nextState = newstate + numNodes;		//[MAXNUMPART];

	for(iState = 0;iState<numStates;iState++)
	{
		sI = iState*numPart;
		// add current network state
		for(i=0;i<numPart;i++)
		{
			transitionNetworkState[i] = allStates[sI+i];
		}

		converLongToStringArr(state,allStates,sI,numPart, leftSize,numNodes);		
		int size1 = findAtt(logicTables, inv_logicTables, state,newstate,nextState,transitionNetworkState,
							numPart,leftSize,numNodes, logicSize, -1, att1);

		// make pertubation
		converLongToStringArr(state,allStates,sI,numPart, leftSize,numNodes);
        temp = pos;
		if(typePert == 1)//Node state perturb
		{
			state[pos] = (state[pos]==1)?0:1;
            temp = -1;
		}		

		// add current network state
		calState(nextState,state, numNodes,numPart,leftSize);
		for(i=0;i<numPart;i++)
		{
			transitionNetworkState[i] = nextState[i];
		}
		int size2 = findAtt(logicTables, inv_logicTables, state,newstate,nextState,transitionNetworkState,
							numPart,leftSize,numNodes, logicSize, temp, att2);

		// compare 2 attractors
		sI = -1;
		if(size1 == size2)
		{
			// compare att1[0] with all elements of att2 arr
			for(i=0;i<size2;i++)
			{
				temp = i*numPart;
				for(j=0;j<numPart;j++)
				{
					if(att1[j] != att2[temp+j])
					{
						break;
					}
				}

				if(j==numPart)
				{
					sI = i;
					break;
				}
			}

			if(sI == -1)
				continue;

			i=0;
			j=sI;
			while(i<size1)
			{
				for(temp=0;temp<numPart;temp++)
				{
					if(att1[i*numPart+temp] != att2[j*numPart+temp])
					{
						break;
					}
				}

				if(temp == numPart)
				{
					i++;
					j++;
					if(j == size2)
						j = 0;
				}
				else
				{
					break;
				}
			}//end while

			if(i==size1)
			{
				// all state in two attractors att1 and att2 is same
				++NumOfRobustStates;
			}
		}

		//NumOfScannedStates++;
	}//end for

	resultArr[get_global_id(0)] = NumOfRobustStates;
}

inline int findAtt(__global const int *logicTables, __global const int *inv_logicTables,
__global int *state,__global int *newstate,
__global int *nextState,
__global int *transitionNetworkState,
short numPart, short leftSize,short numNodes,
short logicSize, int pos, 
__global int *att)
{
	short indexTransNetw=1;
	int i,j;
	int temp;

    bool converged=false;
	short startIndexAtt=-1;

    __global const int *logics;
    
	while(true)
	{
        // calculate next state
		for(i=0;i<numNodes;i++)
        {
            //newstate[i] = state[i];             
            if(i != pos) 
                logics = logicTables + i * logicSize;
            else 
                logics = inv_logicTables + i * logicSize;   //perturbed node
            
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
			return 0;
		//

		if(converged)
		{
			break;
		}
	}//end while

	//Calculate information of attractor
	if(converged==true)
	{
		//NumOfPassedState = indexTransNetw;
		if(indexTransNetw - 1 -startIndexAtt>MAXATTSTATESIZE)
			return 0;

		temp = 0;
		for(i=startIndexAtt;i<indexTransNetw-1;i++)
		{
			//remove end state
			for(j=0;j<numPart;j++)
			{
				att[temp*numPart+j] = transitionNetworkState[i*numPart+j];
			}
			++temp;
		}

	}

	if(converged==true)
		return indexTransNetw - 1 - startIndexAtt;
	else
		return 0;
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
