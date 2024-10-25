package myrbn;

public class Interaction {
        public String Index;
	public String NodeSrc;
	public String NodeDst;
	public int InteractionType;
        public int NoSelected;
        public int isBetweenModule;
        public int isInsideModule;
        public int NoSelectedInsideModule;
        public int isBiggestModule;
        public int isSmallModule;
        public int NoBiggestModule;
        public int NoSmallModule;
        public int fbl;
        public int totaldegree;
        public Double edgebetweenness;
        
        // colin edit for OpenCL
        public int [] nInfos = new int[3];
                        //0: index of NodeSrc
                        //1: index of NodeDst
                        //2: InteractionType
        /**/

	public Interaction(){
            this.Index="";
            this.InteractionType=0;
            this.NodeSrc="";
            this.NodeDst="";
            this.NoSelected=0;
            this.isBetweenModule=0;
            this.NoSelectedInsideModule=0;
            this.isInsideModule=0;
            this.isBiggestModule=0;
            this.isSmallModule=0;
            this.NoBiggestModule=0;
            this.NoSmallModule=0;
            this.fbl=0;
            this.totaldegree=0;
            this.edgebetweenness=0.0;          
	}
        // colin edit for OpenCL
	/*public Interaction(int interactionid, String nodesrc, String nodedst, int interactiontype){
            this.Index="";
            this.NodeSrc=nodesrc;
            this.NodeDst=nodedst;
            this.InteractionType=interactiontype;
	}

        public Interaction(String nodesrc, String nodedst, int interactiontype){
            this.Index="";
            this.NodeSrc=nodesrc;
            this.NodeDst=nodedst;
            this.InteractionType=interactiontype;
	}*/
        /**/
	
        public Interaction Copy(){
            Interaction ina = new Interaction();
            ina.Index=this.Index;
            ina.InteractionType=this.InteractionType;
            ina.NodeSrc=this.NodeSrc;
            ina.NodeDst=this.NodeDst;
            ina.NoSelected=this.NoSelected;
            ina.isBetweenModule=this.isBetweenModule;      
            ina.isInsideModule=this.isInsideModule;
            ina.NoSelectedInsideModule=this.NoSelectedInsideModule;
            ina.isBiggestModule=this.isBiggestModule;
            ina.isSmallModule=this.isSmallModule;
            ina.NoBiggestModule=this.NoBiggestModule;
            ina.NoSmallModule=this.NoSmallModule;
            ina.fbl=this.fbl;
            ina.totaldegree=this.totaldegree;
            ina.edgebetweenness=this.edgebetweenness;           
            for(int i=0;i<3;i++)
                ina.nInfos[i]=this.nInfos[i];
            return ina;
        }
	
}
