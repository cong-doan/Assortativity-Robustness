#define NUMNETWSTATESIZE 150
#define MAXATTSTATESIZE 20
#define BUFSIZE_FOR_ORIGINAL_ATTS 22
#define BUFSIZE_FOR_NEW_ATTS 23

inline int findAtt(__read_only image2d_t rndinaIntArr,
__global char *state, __global char *newstate, __global char *funcNodes,
const int numNodes,
__global char *transitionNetworkState,
__global char *att,
const int log_vector_width, const int width_mask,
const sampler_t smp,
			__global const int *firstIndexDstNodeInEdgesArr,
			__global const int *numIndexDstNodeInEdgesArr);

inline int4 unaligned_read_from_image(
                                //OpenCL Image
                                __read_only image2d_t vector, 
                                //how many bits to shift for division 
                                //(log_2 of the image width)
                                const int log_vector_width,
                                //Boolean mask for modulus
                                //(the image width - 1)
                               const int width_mask,
							   const sampler_t smp,
                               //The unaligned index of the first element we need
                               int col);

__kernel void findOriginalAtts(__global char *nodes, __read_only image2d_t rndinaIntArr,			
			__global const char *allStates,			
			__global int *resultSizeArr,
			__global char *resultAtts,
			__global char *bufArr,
			__global const int *firstIndexDstNodeInEdgesArr,
			__global const int *numIndexDstNodeInEdgesArr,
			const int numNodes,
			const int numStates,
			const int log_vector_width, const int width_mask)
{	
	int iState = get_global_id(0);
	if(iState >= numStates)
		return;
	int sI = iState*numNodes;	

	const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | //Natural coordinates
		CLK_ADDRESS_CLAMP | //Clamp to zeros
		CLK_FILTER_NEAREST; //Don't interpolate	

	__global char *transitionNetworkState = bufArr + sI*BUFSIZE_FOR_ORIGINAL_ATTS;	
	__global char *state = transitionNetworkState + MAXATTSTATESIZE*numNodes;
	__global char *newstate = state + numNodes;	

	__global char *att1 = resultAtts + sI*MAXATTSTATESIZE;		
	
	// add current network state		
	for(int i=0;i<numNodes;i++)
	{
		transitionNetworkState[i] = allStates[sI];
		state[i] = allStates[sI];			
		++sI;
	}		

	int size1 = findAtt(rndinaIntArr,state,newstate,nodes,
						numNodes, transitionNetworkState, att1, log_vector_width, width_mask, smp,
						firstIndexDstNodeInEdgesArr, numIndexDstNodeInEdgesArr);
	resultSizeArr[iState] = size1;
}
//-----------------------------------------------------------------------------------------------------------------------------------
__kernel void findNewAtts(__global char *nodes, __read_only image2d_t rndinaIntArr,			
			__global const int *posSelectedNodesInt,
			__global const char *allStates,			
			__global int *resultSizeArr,
			__global char *resultAtts,
			__global char *bufArr,
			__global const int *firstIndexDstNodeInEdgesArr,
			__global const int *numIndexDstNodeInEdgesArr,
			const int numNodes,
			const int numStates,
			const int typePert,
			const int pass,
			const int log_vector_width, const int width_mask,
            const int numExaminedNodes)
{	
	int iNode = get_global_id(0);
	if(iNode >= numExaminedNodes)
		return;
	int sI = iNode*numNodes;	

	const sampler_t smp = CLK_NORMALIZED_COORDS_FALSE | //Natural coordinates
		CLK_ADDRESS_CLAMP | //Clamp to zeros
		CLK_FILTER_NEAREST; //Don't interpolate	

	__global char *transitionNetworkState = bufArr + sI*BUFSIZE_FOR_NEW_ATTS;	
	__global char *state = transitionNetworkState + MAXATTSTATESIZE*numNodes;
	__global char *newstate = state + numNodes;	
	__global char *funcNodes = newstate + numNodes;

	for(int i=0;i<numNodes;i++)
	{
		funcNodes[i] = nodes[i];		
	}

	__global char *att1 = resultAtts + sI*MAXATTSTATESIZE;		
	int pos = posSelectedNodesInt[iNode];
	sI = pass;//*numNodes;
	
	// add current network state		
	for(int i=0;i<numNodes;i++)
	{		
		state[i] = allStates[sI];			
		++sI;
	}		

	if(typePert == 1)//Node state perturb
	{
		state[pos] = (state[pos]==1)?0:1;
	}
	else
	{
		funcNodes[pos] = (funcNodes[pos]==1)?0:1;
	}	
	for(int i=0;i<numNodes;i++)
	{
		transitionNetworkState[i] = state[i];
	}

	int size1 = findAtt(rndinaIntArr,state,newstate,funcNodes,
						numNodes, transitionNetworkState, att1, log_vector_width, width_mask, smp,
						firstIndexDstNodeInEdgesArr, numIndexDstNodeInEdgesArr);
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
	
	__global char *att1 = originalAtts + pass*MAXATTSTATESIZE*numNodes;
	__global char *att2 = newAtts + iNode*MAXATTSTATESIZE*numNodes;
	int size1 = sizeOriginalAtts[pass];
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

inline int findAtt(__read_only image2d_t rndinaIntArr,
__global char *state, __global char *newstate, __global char *funcNodes,
const int numNodes,
__global char *transitionNetworkState,
__global char *att,
const int log_vector_width, const int width_mask,
const sampler_t smp,
			__global const int *firstIndexDstNodeInEdgesArr,
			__global const int *numIndexDstNodeInEdgesArr)
{	
	short indexTransNetw=1;
	int i,j;
	int temp;

    bool converged=false;

	short startIndexAtt=-1;
	short indexTrans = 0;	//can replace at index 0	

	int4 edge;	
	while(true)
	{
			// calculate next state			
			for(i=0;i<numNodes;i++)
			{
				newstate[i] = state[i];
				int pos = firstIndexDstNodeInEdgesArr[i];
				int num = numIndexDstNodeInEdgesArr[i];

				if(num > 0)
				{
					edge = unaligned_read_from_image(rndinaIntArr, log_vector_width, width_mask, smp, pos);
					if(edge.x==1)
					{
						temp = state[edge.y];
					}
					else
					{
						temp = (state[edge.y] == 0)?1:0;
					}

					newstate[i] = temp;
					for(j=1; j<num; j++)
					{
						pos += 4;
						edge = unaligned_read_from_image(rndinaIntArr, log_vector_width, width_mask, smp, pos);
						if(edge.x==1)
						{
							temp = state[edge.y];
						}
						else
						{
							temp = (state[edge.y] == 0)?1:0;
						}

						if(funcNodes[i] == 1)
						{
							if(temp == 1)
							{
								newstate[i] = 1;
								break;
							}				
							//newstate[i] = newstate[i] | temp;
						}
						else
						{
							if(temp == 0)
							{
								newstate[i] = 0;
								break;
							}
							//newstate[i] = newstate[i] & temp;
						}
					}
				}
			}			
		
			/*// calculate next state
			for(i=0;i<numNodes;i++)
				newstate[i] = 2;//state[i];

			int pos = 0;
			edge = unaligned_read_from_image(rndinaIntArr, log_vector_width, width_mask, smp, pos);
			while(edge.x != 2)
			{				
				j=edge.z;//rndinaIntArr[i+2];//dest
				if(edge.x==1)
				{
					temp = state[edge.y];
				}
				else// if(edge.x == -1)
				{
					temp = (state[edge.y] == 0)?1:0;
				}				

				if(newstate[j] == 2)
					newstate[j] = temp;
				else
				{
					if(funcNodes[j] == 1)
						newstate[j] = newstate[j] | temp;
					else
						newstate[j] = newstate[j] & temp;
				}
				
				pos += 4;
				edge = unaligned_read_from_image(rndinaIntArr, log_vector_width, width_mask, smp, pos);
			}

		for(i=0;i<numNodes;i++)
			if(newstate[i]==2)
				newstate[i] = state[i];*/

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

inline int4 unaligned_read_from_image(
                                //OpenCL Image
                                __read_only image2d_t vector, 
                                //how many bits to shift for division 
                                //(log_2 of the image width)
                                const int log_vector_width,
                                //Boolean mask for modulus
                                //(the image width - 1)
                               const int width_mask,
							   const sampler_t smp,
                               //The unaligned index of the first element we need
                               int col) 
{
	//int alignment = col % 4;
	//if (alignment < 0) alignment += 4; //col may be negative
	int rgbcol = col >> 2;
	int4 v;

	//if (alignment == 0) 
	{ 	//Fully aligned to pixels in image
		int2 coord;
		coord.x = rgbcol & width_mask;
		coord.y = rgbcol >> log_vector_width;
		v = read_imagei(vector, smp, coord);
	}

	return v;
}
