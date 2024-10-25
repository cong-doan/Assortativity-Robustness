#define MAXNETWSTATESIZE 150
#define MAXATTSTATESIZE 20
#define MAXBITSIZE 31

inline void calState(__global int *total,__global int *state, short numNodes, short numPart, short leftSize);
int compare2state(__global int *state1,__global int *state2,__global const int *nodesclusteridArr,short numNodes,short pos);

inline int findAtt(__global const int *logicTables, __global const int *inv_logicTables,
__global int *state,__global int *newstate,
__global int *nextState,
__global int *transitionNetworkState,
short numPart, short leftSize,short numNodes,
short logicSize, int pos, 
__global int *att);

inline void converAttractorToStringArr(__global int *state,__global int *Attstring,__global const int *AttArr,short numPart, short leftSize,short numNodes,short AttSize);
inline double HammingDistance(__global int *transitionNetState,__global int *state,__global int *newstate,__global int *att1,__global int *att2,__global const int *nodesclusteridArr,short numPart, short leftSize,short numNodes,short size1,short size2,short pos);
inline void converLongToStringArr(__global int *state,__global const int *allStates,int sI,short numPart, short leftSize,short numNodes);

__kernel void calInModuleRobustness(__global const int *logicTables, __global const int *inv_logicTables, __global const int *nodesclusteridArr,  
			__global const int *posSelectedNodesInt,
			__global const int *allStates,
			__global double *resultArr,
			__global int *bufArr, int bufSize,			
			short numPart, short leftSize,short numNodes,
			int numStates,short typePert, short logicSize, int NumCluster)
{
	int pos = posSelectedNodesInt[get_global_id(0)];
	int iState,i,temp;
	int sI;

	__global int *transitionNetworkState = bufArr + get_global_id(0)*bufSize;	//[MAXNETWSTATESIZE*MAXNUMPART];
	__global int *att1 = transitionNetworkState + MAXNETWSTATESIZE*numPart;	//[MAXATTSTATESIZE*MAXNUMPART];
	__global int *att2 = att1 + MAXATTSTATESIZE*numPart;	//[MAXATTSTATESIZE*MAXNUMPART];

	__global int *state = att2 + MAXATTSTATESIZE*numPart;	//[MAXNODES];
	__global int *newstate = state + numNodes;			//[MAXNODES];		
	__global int *nextState = newstate + numNodes;		//[MAXNUMPART];

    double sum=0;
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
		double resultinmodulerobustness=HammingDistance(transitionNetworkState,state,newstate,att1,att2,nodesclusteridArr,numPart,leftSize,numNodes,size1,size2,pos);
        sum=sum+resultinmodulerobustness;
	}//end for

	resultArr[get_global_id(0)] = sum/numStates;
}

int compare2state(__global int *state1,__global int *state2,__global const int *nodesclusteridArr,short numNodes,short pos)
{
    int i;
    int result=0;
    int clusterid;
    clusterid=nodesclusteridArr[pos];
    
    for(i=0;i<numNodes;i++)
    {
        if (nodesclusteridArr[i]==clusterid)
        {
            if(state1[i]>state2[i])
            {
                result=1;
                break;
            }
            if(state1[i]<state2[i])
            {
                result=2;
                break;
            }
        }
    }
    return result;
        
}

inline double HammingDistance(__global int *transitionNetState,__global int *state,__global int *newstate,__global int *att1,__global int *att2,__global const int *nodesclusteridArr,short numPart, short leftSize,short numNodes,short size1,short size2,short pos)
{
    int i,sizecluster,j;
    int h,k;
    int result=0,clusterid;
    int min,start;
    
    clusterid=nodesclusteridArr[pos];
    //size of cluster
    sizecluster=0;
    for(i=0;i<numNodes;i++)
        if (nodesclusteridArr[i]==clusterid)
            sizecluster++;
        
    //reset transition network state
    for(i=0;i<size1*numPart;i++)
        transitionNetState[i]=0;
    //finding minimum state in att1
    min=0;
    j=min*numPart;
    converLongToStringArr(state,att1,j,numPart, leftSize,numNodes);
    for(i=1;i<size1;i++)
    {
        j=i*numPart;
        converLongToStringArr(newstate,att1,j,numPart, leftSize,numNodes);
        if(compare2state(state,newstate,nodesclusteridArr,numNodes,pos)==1)
        {
            min=i;
            j=min*numPart;
            converLongToStringArr(state,att1,j,numPart, leftSize,numNodes);
        }
    }
    
    //copy from min to size 1
    start=0;
    for(i=min;i<size1;i++)
    {
        j=i*numPart;
        h=start*numPart;
        for(k=j;k<j+numPart;k++)
            transitionNetState[h+k-j]=att1[k];
        start++;
    }
    //copy from 0 to min-1
    for(i=0;i<min;i++)
    {
        j=i*numPart;
        h=start*numPart;
        for(k=j;k<j+numPart;k++)
            transitionNetState[h+k-j]=att1[k];
        start++;
    }
    //copy from transitionNetState to att1
    for(i=0;i<size1*numPart;i++)
    att1[i]=transitionNetState[i];
    
    //reset transition network state
    for(i=0;i<size2*numPart;i++)
        transitionNetState[i]=0;
    //finding minimum state in att1
    min=0;
    j=min*numPart;
    converLongToStringArr(state,att2,j,numPart, leftSize,numNodes);
    for(i=1;i<size2;i++)
    {
        j=i*numPart;
        converLongToStringArr(newstate,att2,j,numPart, leftSize,numNodes);
        if(compare2state(state,newstate,nodesclusteridArr,numNodes,pos)==1)
        {
            min=i;
            j=min*numPart;
            converLongToStringArr(state,att2,j,numPart, leftSize,numNodes);
        }
    }
    
    //copy from min to size 1
    start=0;
    for(i=min;i<size2;i++)
    {
        j=i*numPart;
        h=start*numPart;
        for(k=j;k<j+numPart;k++)
            transitionNetState[h+k-j]=att2[k];
        start++;
    }
    //copy from 0 to min-1
    for(i=0;i<min;i++)
    {
        j=i*numPart;
        h=start*numPart;
        for(k=j;k<j+numPart;k++)
            transitionNetState[h+k-j]=att2[k];
        start++;
    }
    //copy from transitionNetState to att1
    for(i=0;i<size2*numPart;i++)
    att2[i]=transitionNetState[i];
    
        
    //compare 2 attractors
    if (size1>size2)
    {
        result=0;
        for(i=0;i<size2;i++)
        {
            j=i*numPart;
            converLongToStringArr(state,att1,j,numPart, leftSize,numNodes);
            converLongToStringArr(newstate,att2,j,numPart, leftSize,numNodes);
            //compare two states
            h=0;
            for(k=0;k<numNodes;k++)
                if(nodesclusteridArr[k]==clusterid)
                    if(state[k]!=newstate[k])
                        h++;
            result=result+(1-(double)h/sizecluster);
        }
        result=result/size1;
    }
    else
    {
        result=0;
        for(i=0;i<size1;i++)
        {
            j=i*numPart;
            converLongToStringArr(state,att1,j,numPart, leftSize,numNodes);
            converLongToStringArr(state,att2,j,numPart, leftSize,numNodes);
            //compare 2 states
            h=0;
            for(k=0;k<numNodes;k++)
                if(nodesclusteridArr[k]==clusterid)
                    if(state[k]!=newstate[k])
                        h++;
            result=result+(1-(double)h/sizecluster);
        }
        result=result/size2;
    }
    return result;
        
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
