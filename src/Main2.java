import java.io.*;
import java.util.*;

class MyException extends Exception {
    public MyException(String s){
        super(s);
    }
}

class PCB{
    static String jobId;
    static int TTL; // Total Time Limit
    static int TLL; // Total Line Limit
    static int TTC; // Total Time Count
    static int LLC; // Line Limit Count
}

public class Main2 {
    BufferedReader br;
    FileWriter writer;
    boolean visited[] = new boolean[30];
    int allo[] = new int[10];
    //char[][] mainMemory = new char[100][4];
    char[] registerR = new char[4];
    char[] registerIR;
    int registerICCounter;
    boolean registerC;
    int memoryUsed;
    int SI=3,TI=0,PI=0;

    int PTR;
    String EM;
    char[][] realMemory = new char[300][4];

    public Main2(String input, String output){
        try{
            File fd = new File(input);
            br = new BufferedReader(new FileReader(fd));
            writer = new FileWriter(output);
        }catch(Exception e ){System.out.println(e);}
    }

    public void LOAD(){
        String strLoad;
        char[] arr = new char[20];
        try{
            while((strLoad = br.readLine())!=null) {
                arr = strLoad.toCharArray();
                if (arr[0] == '$' && arr[1] == 'A' && arr[2] == 'M' && arr[3] == 'J') {
                    System.out.println("Program card detected.");
                    init(strLoad);
                }
                else if (arr[0] == '$' && arr[1] == 'D' && arr[2] == 'T' && arr[3] == 'A') {
                    System.out.println("Data card detected.");
                    startExecution();
                }
                else if (arr[0] == '$' && arr[1] == 'E' && arr[2] == 'N' && arr[3] == 'D') {
                    //print_memory();
                    System.out.println("\nJobID: "+PCB.jobId+" Ended.");
                }
                else {
                    if ((memoryUsed == 100)) {
                        System.out.println("No more memory.");
                    }
                    for (int i = 0; i < strLoad.length(); i++) {
                        if (i % 4 == 0 && i != 0) {
                            memoryUsed++;
                        }
                        //mainMemory[memoryUsed][i % 4] = arr[i];
                        int realA = addMap(memoryUsed);
                        realMemory[realA][i % 4] = arr[i];
                    }
                }
            }
        }catch(Exception e){
            print_memory();
            System.out.println("jai ho\n"+e);
        }
    }
    public void init(String str){
        char arr[] = str.toCharArray();
        String jobID = String.valueOf(arr[4])+String.valueOf(arr[5])+String.valueOf(arr[6])+String.valueOf(arr[7]);
        int TTL = Integer.parseInt(String.valueOf(arr[8])+String.valueOf(arr[9])+String.valueOf(arr[10])+String.valueOf(arr[11]));
        int TLL = Integer.parseInt(String.valueOf(arr[12])+String.valueOf(arr[13])+String.valueOf(arr[14])+String.valueOf(arr[15]));

        for(int i=0;i<allo.length; i++)
            allo[i] = -1;
        //mainMemory = new char[100][4];
        realMemory = new char[300][4];
        PTR = allocate()*10;
        registerR = new char[4];
        registerIR = new char[4];
        registerC = false;
        registerICCounter = 0;
        memoryUsed = 0;
        SI = 0;
        //initPageTable();
        initPCB(jobID,TTL,TLL);
        System.out.println("Memory Initialised");
    }
    public void startExecution(){
//        IC initialized to [00]
        int registerIC = 0;
        registerICCounter = 0;
        while (1<2) {

            registerIC = addMap(registerICCounter);
            if(registerIC==300){
                System.out.println("instruction counter overflow");
                break;
            }

//        here instruction register 4 bytes will be loaded with instruction GD10 from memory location;

            for (int i = 0; i < 4; i++) {
                registerIR[i] = realMemory[registerIC][i];
            }
            registerICCounter++;

            if(registerIR[0] == 'L' && registerIR[1] == 'R'){
                PCB.TTC++;
                int loc = Integer.parseInt(String.valueOf(registerIR[2])+String.valueOf(registerIR[3]));
                if(allo[loc/10] == -1){ // this if block handles page fault, if address get as -1 then PI set to 3 which is valid page fault
                    PI = 3;
                    masterMode();
                }
                loc = addMap(loc);
                registerR[0] =realMemory[loc][0];
                registerR[1] =realMemory[loc][1];
                registerR[2] =realMemory[loc][2];
                registerR[3] =realMemory[loc][3];
            }
            else if(registerIR[0] == 'S' && registerIR[1] == 'R'){
                PCB.TTC++;
                int loc = Integer.parseInt(String.valueOf(registerIR[2])+String.valueOf(registerIR[3]));
                loc = addMap(loc);
                realMemory[loc][0] =registerR[0];
                realMemory[loc][1] =registerR[1];
                realMemory[loc][2] =registerR[2];
                realMemory[loc][3] =registerR[3];
            }
            else if(registerIR[0] == 'B' && registerIR[1] == 'T'){
                PCB.TTC++;
                int loc = Integer.parseInt(String.valueOf(registerIR[2]) + String.valueOf(registerIR[3]));
                if(registerC == true){
                    registerICCounter=loc;
                    registerC=false;
                }
            }
            else if (registerIR[0] == 'C' && registerIR[1] == 'R') {
                PCB.TTC++;
                int loc = Integer.parseInt(String.valueOf(registerIR[2]) + String.valueOf(registerIR[3]));
                loc = addMap(loc);
                if (realMemory[loc][0] == registerR[0] && realMemory[loc][1] == registerR[1] && realMemory[loc][2] == registerR[2] && realMemory[loc][3] == registerR[3]) {
                    registerC = true;
                } else {
                    registerC = false;
                }
            }
            else if (registerIR[0] == 'G' && registerIR[1] == 'D') {
                PCB.TTC+=2;
                SI = 1;
                masterMode();
            }
            else if (registerIR[0] == 'P' && registerIR[1] == 'D') {
                PCB.TTC+=2;
                SI = 2;
                masterMode();
            }
            else if (registerIR[0] == 'H') {
                PCB.TTC++;
                SI = 3;
                masterMode();
                break;
            }
            else{
                PI = 1;
                masterMode();
            }
            if(PCB.TTC >= PCB.TTL){
                TI=2;
                masterMode();
            }
        }
    }

    public void masterMode(){
        //Case of TI and PI
        if(TI == 0 && PI == 1){terminate(4);}
        if(TI == 0 && PI == 2){terminate(5);}
        if(TI == 0 && PI == 3){
            terminate(6);
            System.exit(0);
        }
        if(TI == 2 && PI == 1){terminate(3);terminate(4);System.exit(0);}
        if(TI == 2 && PI == 2){terminate(3);terminate(5);System.exit(0);}
        if(TI == 2 && PI == 3){terminate(3);System.exit(0);}

        // Case of TI and SI
        if(TI == 0 && SI == 1){
            read();
        }
        if(TI == 0 && SI == 2){
            try {
                writeData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        if(TI == 0 && SI == 3){terminate(0);return;/*System.exit(0);*/}
        if(TI == 2 && SI == 1){terminate(3);System.exit(0);}
        if(TI == 2 && SI == 2){
            try {
                writeData();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            terminate(3);
            System.exit(0);
        }
        if(TI == 2 || SI == 3){
            if(TI==2){
                terminate(3);
            }
            if(SI==3){
                terminate(0);
                return;
            }
        }
    }

    void read(){
        registerIR[3]='0';
        int loc = Integer.parseInt(String.valueOf(registerIR[2])+String.valueOf(registerIR[3]));
        int realA = addMap(loc);
        int temp = realA;
        char[] charArr;
        String str;
        try {
            str = br.readLine();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        int len = str.length();
        charArr = str.toCharArray();
        for(int i=0;i<len;i++){
            if(i%4==0 && i!=0){
                realA++;
            }
            realMemory[realA][i%4]=charArr[i];
        }
        if (realMemory[temp][0] == '$' && realMemory[temp][1] == 'E' && realMemory[temp][2] == 'N' && realMemory[temp][3] == 'D') {
            terminate(1);
        }
    }
    void writeData() throws IOException {
        PCB.LLC++;
        if(PCB.LLC>PCB.TLL)
            terminate(2);
        registerIR[3]='0';
        String str = new String();
        int loc = Integer.parseInt(String.valueOf(registerIR[2])+String.valueOf(registerIR[3]));
        if(allo[loc/10] == -1){ // this if block handles page fault, if address get as -1 then PI set to 3 which is valid page fault
            PI = 3;
            masterMode();
        }
        int realA = addMap(loc);
        String total="";
        for(int i=0;i<10;i++){
            str += new String(realMemory[realA+i]);
            str = str.trim();
            if(!str.isEmpty()){
                total =  total.concat(str);
            }
        }
        writer.write(str+"\n");
        if(str == "" || str.equals("")){
            PI = 3;
        }
        System.out.println(""+str);
    }
    void halt(){
        return;
    }
    public void print_memory(){
        for(int i=0;i<300;i++) {
            System.out.println("RealMemory["+i+"] "+new String(realMemory[i]));
        }
    }
    int addMap(int IC){

        if(allo[IC/10] == -1){
            int num = allocate();
            int a = num%10;
            num = num/10;
            int b = num%10;
            num = num/10;
            int c = num%10;
            num = num/10;
            int d = num%10;
            allo[IC/10] = num;
            //System.out.println(d+" "+c+" "+b+" "+a);
            realMemory[PTR+IC/10][0] = (char)d;
            realMemory[PTR+IC/10][1] = (char)c;
            realMemory[PTR+IC/10][2] = (char)b;
            realMemory[PTR+IC/10][3] = (char)a;
        }

        int I = (int)Math.floor(IC/10);
        if(I<0 || I>99)
            PI = 2; // Operand error
        int PTE = PTR + I;
        int a = (int)(realMemory[PTE][0]);
        int b = (int)(realMemory[PTE][1]);
        int c = (int)(realMemory[PTE][2]);
        int d = (int)(realMemory[PTE][3]);
        int realAddr = Integer.parseInt(a+""+b+""+c+""+d);
        return (realAddr*10+(IC%10));
    }
    void initPCB(String jobID,int TTL, int TLL){
        PCB.LLC = 0;
        PCB.TLL = TLL;
        PCB.TTC = 0;
        PCB.TTL = TTL;
        PCB.jobId = jobID;
    }
    void terminate(int em){
        String strTer="\n\n";

        try{
            writer.write(strTer+"\n");
            writer.flush();
            switch (em) {
                case 0:{
                    strTer = ("No Error");
                    break;
                }
                case 1:{
                    strTer = ("Out of Data");
                    break;
                }
                case 2:{
                    strTer = ("Line Limit Exceeded");
                    break;
                }
                case 3:{
                    strTer = ("Time Limit Exceeded");
                    break;
                }
                case 4:{
                    strTer = ("Operation Code Error");
                    break;
                }
                case 5:{
                    strTer = ("Operand Error");
                    break;
                }
                case 6:{
                    strTer = ("Invalid Page Fault");
                    break;
                }
                default:{
                    strTer = ("No Exception Mentioned");
                    break;
                }
            }
            writer.write(strTer+"\n");
            writer.flush();
            throw new MyException(strTer);
        }catch(MyException e){
            System.out.println("Error:- "+e.getMessage());
        }catch(IOException ie){
            System.out.println(ie);
        }
    }
    int getRandomNumber() {
        Random random = new Random();
        return random.nextInt(30);
    }
    int allocate(){
        int a = getRandomNumber();
        while(visited[a])
            a = getRandomNumber();
        visited[a] = true;
        return a;
    }

}