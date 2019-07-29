public class OS {
    private final int VP_POSITION = 0;
    private final int PG_POSITION = 0;
    private final int V_POSITION = 1;
    private final int R_POSITION = 2;
    private final int D_POSITION = 3;
    private final int FR_POSITION = 4;
    private int[][] memory;
    private int[][] disk;
    private int[][] pageTable;
    private int entrySize, pageTableSize, evictedPage;
    private int position = 0;
    private int loadAt = 0;

    public OS(int memorySize, int memoryDataSize, int pageDataSize){
        entrySize = pageDataSize;
        pageTableSize = memorySize;
        memory = new int[memorySize][memoryDataSize];
        disk = new int[2*memorySize][memoryDataSize];
        pageTable = new int[memorySize][pageDataSize];
    }

    public int[] createEntry(int virtualPage, int pageFrame){
        int[] entry = new int[entrySize];
        entry[VP_POSITION] = virtualPage;
        entry[V_POSITION] = 1;
        entry[R_POSITION] = 0;
        entry[D_POSITION] = 0;
        entry[FR_POSITION] = pageFrame;
        return entry;
    }

    public int[] pageTableAdd(int virtualPage){
        int[] entry;
        if(!fullPageTable()){
            entry = createEntry(virtualPage, position);
            pageTable[position] = entry;
        }
        else{
            entry = createEntry(virtualPage, clockAlgorithm());
            pageTable[position] = entry;
        }
        position++;
        return entry;
    }

    public boolean fullPageTable(){
        return position >= pageTableSize;
    }

    public int clockAlgorithm(){
        int pageIndex = 0;

        while(true){
            if(pageTable[pageIndex][R_POSITION] == 0){
                if(pageTable[pageIndex][D_POSITION] == 1){
                    writeToDisk(pageIndex);
                }
                evictedPage = pageIndex % pageTable[VP_POSITION].length;
                return evict(evictedPage);
            }
            else{
                pageTable[pageIndex][R_POSITION] = 0;
            }
            pageIndex++;
        }
    }

    public int evict(int pageIndex){
        return pageIndex;
    }

    public int getEvictedPage(){
        return evictedPage;
    }

    public void addToDisk(int[] data){
        disk[loadAt] = data;
        loadAt++;
    }

    public void writeToDisk(int pageIndex){
        int pageFrame = pageTable[pageIndex][FR_POSITION];
        int virtualPage = pageTable[pageIndex][VP_POSITION];

        for(int i = 0; i < disk[VP_POSITION].length; i++){
            if(disk[i][VP_POSITION] == virtualPage){
                disk[i] = findPage(pageFrame);
            }
        }
    }

    public int[] findPage(int pageFrame){
        for(int i = 0; i < memory[VP_POSITION].length; i++){
            if(memory[i][FR_POSITION] == pageFrame){
                return memory[position];
            }
        }
        return null;
    }

    public void resetRBit(){
        for(int i = 0; i < pageTable[VP_POSITION].length; i++){
            pageTable[i][R_POSITION] = 0;
        }
    }

    public void updatePageTable(int virtualPage, char readWrite){
        for(int i = 0; i < pageTable[VP_POSITION].length; i++) {
            if(pageTable[i][VP_POSITION] == virtualPage) {
                if(readWrite == 'w'){
                    pageTable[i][D_POSITION] = 1;
                }
                pageTable[i][R_POSITION] = 1;
                return;
            }
        }
    }

    public void updateMemory(int pageFrame, int offset, int newData, int[] newDataArray){
        for(int i = 0; i < memory[VP_POSITION].length; i++){
            if(memory[i][PG_POSITION] == pageFrame){
                if(offset > 0) {
                    memory[i][offset] = newData;
                }
                else{
                    memory[i] = newDataArray;
                }
                return;
            }
        }
    }

    public int[][] getDisk(){
        return disk;
    }

    public int[][] getMemory(){
        return memory;
    }

    public int[][] getPageTable(){
        return pageTable;
    }
}
