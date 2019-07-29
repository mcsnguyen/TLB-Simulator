public class MMU {
    private final int VP_POSITION = 0;
    private final int PG_POSITION = 0;
    private final int V_POSITION = 1;
    private final int R_POSITION = 2;
    private final int D_POSITION = 3;
    private final int FR_POSITION = 4;
    private int[][] tlb;
    private OS ram;
    private int tlbSize;
    private int position = 0;
    int[] csvData;

    public MMU(int tlbSize, int entrySize){
        this.tlbSize = tlbSize;
        tlb = new int[tlbSize][entrySize];
    }

    public void updateMemory(OS ram){
        this.ram = ram;
    }

    public int[] executeInstruction(int rwbit, int readAddress, int offset, int writeData){
        csvData = new int[]{0, 0, 0, 0, 0, 0}; // {value, soft, hard, hit, evicted_pg, dirty bit}

        if(!checkTLB(readAddress)) {
            miss(readAddress);
        }
        else{
            csvData[3] = 1; // hit
        }

        if(rwbit == 0) {
            csvData[0] = operate(readAddress, 0, offset, 'r');
        }
        else{
            csvData[0] = operate(readAddress, writeData, offset, 'w');
        }

        return csvData;
    }

    public boolean checkTLB(int virtualPage){
        for(int i = 0; i < tlbSize; i++){
            if(tlb[i][VP_POSITION] == virtualPage){
                return true;
            }
        }
        return false;
    }

    public void miss(int readAddress){
        int pageTableIndex = contains(ram.getPageTable(), readAddress);

        if(pageTableIndex > 0){
            tlbAdd(ram.getPageTable()[pageTableIndex]);
            csvData[1] = 1; // soft miss
        }
        else{
            int diskIndex = contains(ram.getDisk(), readAddress);
            int[] entry = ram.pageTableAdd(readAddress);
            ram.updateMemory(entry[FR_POSITION], -1, -1, ram.getDisk()[diskIndex]);
            tlbAdd(entry);
            csvData[2] = 1; // hard miss
            csvData[4] = ram.getEvictedPage();
        }
    }

    public int contains(int[][] table, int readAddress){
        for(int i = 0; i < table[0].length; i++){
            if(table[i][VP_POSITION] == readAddress){
                return i;
            }
        }
        return -1;
    }

    public int operate(int readAddress, int writeData, int offset, char readWrite){
        int[][] memory = ram.getMemory();
        int data = 0;
        for(int i = 0; i < memory[VP_POSITION].length; i++){
            if(memory[i][PG_POSITION] == readAddress){
                if(readWrite == 'r') {
                    data = memory[i][offset];
                    updateMemory(readAddress, 'r', 0, 0);
                    return data;
                }
                else{
                    updateMemory(readAddress, 'w', offset, writeData);
                    data = writeData;
                    return data;
                }
            }
        }
        return data;
    }

    public void updateMemory(int virtualPage, char readWrite, int offset, int newData){
        if(readWrite == 'w') {
            ram.updatePageTable(virtualPage, 'w');
            int index = setBit(virtualPage, 'd', 1);
            ram.updateMemory(tlb[index][FR_POSITION], offset, newData, null);
            csvData[5] = 1;
            return;
        }
        else {
            ram.updatePageTable(virtualPage, 'r');
            setBit(virtualPage, 'r', 1);
        }
    }

    public int setBit(int virtualPage, char bit, int bitValue){
        for(int i = 0; i < tlb[VP_POSITION].length; i++){
            if(tlb[i][VP_POSITION] == virtualPage){
                if(bit == 'd'){
                    tlb[i][D_POSITION] = bitValue;
                }
                tlb[i][R_POSITION] = bitValue;
                return i;
            }
        }
        return -1;
    }

    public void tlbAdd(int[] entry){
        if(!fullTLB()){
            tlb[position] = entry;
        }
        else{
            replaceFIFO(entry);
        }
        position++;
    }

    private boolean fullTLB(){
        return position >= tlbSize;
    }

    private void replaceFIFO(int[] entry){
        tlb[position % tlbSize] = entry;
    }
}
