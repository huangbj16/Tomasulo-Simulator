import static java.lang.System.exit;
import static java.lang.System.in;
import static java.lang.System.load;

public class TomasuloProcessor {
    Calculator[] adders;
    Calculator[] multers;
    Loader[] loaders;
    Reservation[] addRS;
    Reservation[] mulRS;
    LoadBuffer[] loadBuffers;
    RegisterResultStatus[] registers;
    boolean hasJump;
    Instruction[] instructions;
    int nextInstructionIndex;
    int timer;
    final int ADDNUM = 3, MULNUM = 2, LOADNUM = 2, ADDRSNUM = 6, MULRSNUM = 3, LOADBUFNUM = 3;

    TomasuloProcessor(){
        adders = new Calculator[3];
        for (int i = 0; i < adders.length; i++) {
            adders[i] = new Calculator();
        }
        multers = new Calculator[2];
        for (int i = 0; i < multers.length; i++) {
            multers[i] = new Calculator();
        }
        loaders = new Loader[2];
        for (int i = 0; i < loaders.length; i++) {
            loaders[i] = new Loader();
        }
        addRS = new Reservation[6];
        for (int i = 0; i < addRS.length; i++) {
            addRS[i] = new Reservation();
        }
        mulRS = new Reservation[3];
        for (int i = 0; i < mulRS.length; i++) {
            mulRS[i] = new Reservation();
        }
        loadBuffers = new LoadBuffer[3];
        for (int i = 0; i < loadBuffers.length; i++) {
            loadBuffers[i] = new LoadBuffer();
        }
        registers = new RegisterResultStatus[32];
        for (int i = 0; i < registers.length; i++) {
            registers[i] = new RegisterResultStatus();
        }
        hasJump = false;
        instructions = null;
        nextInstructionIndex = 0;
        timer = 0;
    }

    void Process(Instruction[] instructions){
        this.instructions = instructions;
        while(true){
            timer++;
            System.out.println("cycle timer: "+timer);
            Issue();
            Exec();
//            Write();
            if(timer == 10)
                break;
        }
        PrintState();
    }

    void Issue(){
        if(hasJump) return;
        Instruction nextInstruction = instructions[nextInstructionIndex];
        System.out.println(nextInstruction.instructionType);
        switch (nextInstruction.instructionType){
            case LOAD:
                if(IssueLoadBuffer(nextInstruction)){//success
                    nextInstructionIndex++;
                }
                break;
            case ADD:
            case SUB:
                if(IssueReservation(nextInstruction, addRS)){//success
                    nextInstructionIndex++;
                }
                break;
            case MUL:
            case DIV:
                if(IssueReservation(nextInstruction, mulRS)){//success
                    nextInstructionIndex++;
                }
                break;
            case JUMP:
                if(IssueReservation(nextInstruction, addRS)){//success
                    nextInstructionIndex++;//problem??可能不应该加。
                    hasJump = true;
                }
                break;
        }
    }

    boolean IssueLoadBuffer(Instruction instruction){
        System.out.println("issue loadbuffer: "+instruction.instructionType+"timer: "+timer);
        int loadIndex = -1;
        for (int i = 0; i < loadBuffers.length; i++) {
            if(!loadBuffers[i].isBusy){
                loadIndex = i;
                break;
            }
        }
        if(loadIndex == -1) return false;
        loadBuffers[loadIndex].isBusy = true;
        loadBuffers[loadIndex].issueTime = timer;
        loadBuffers[loadIndex].instruction = instruction;
        instruction.issue = timer;
        LoadInstruction load = (LoadInstruction)instruction;
        registers[load.registerNo].functionUnit = "load"+Integer.toString(loadIndex);
        registers[load.registerNo].isWaiting = true;
        return true;
    }

    boolean IssueReservation(Instruction instruction, Reservation[] reservations){
        System.out.println("issue reservation: "+instruction.instructionType+"timer: "+timer);
        int loadIndex = -1;
        for (int i = 0; i < reservations.length; i++) {
            if(!reservations[i].isBusy){
                loadIndex = i;
                break;
            }
        }
        if(loadIndex == -1) return false;
        reservations[loadIndex].isBusy = true;
        reservations[loadIndex].issueTime = timer;
        reservations[loadIndex].operation = instruction.instructionType;
        reservations[loadIndex].instruction = instruction;
        if(instruction.instructionType != InstructionType.JUMP) {
            CalInstruction cal = (CalInstruction) instruction;
            if (registers[cal.registerS1].isWaiting)
                reservations[loadIndex].Qj = registers[cal.registerS1].functionUnit;
            else
                reservations[loadIndex].Vj = registers[cal.registerS1].value;
            if (registers[cal.registerS2].isWaiting)
                reservations[loadIndex].Qk = registers[cal.registerS2].functionUnit;
            else
                reservations[loadIndex].Vk = registers[cal.registerS2].value;
            instruction.issue = timer;
            if (instruction.instructionType == InstructionType.ADD || instruction.instructionType == InstructionType.SUB)
                registers[cal.registerD].functionUnit = "add" + Integer.toString(loadIndex);
            else
                registers[cal.registerD].functionUnit = "mul" + Integer.toString(loadIndex);
            registers[cal.registerD].isWaiting = true;
        }
        else{//jump instruction
            JumpInstruction jump = (JumpInstruction) instruction;
            reservations[loadIndex].Vj = jump.compare;
            if (registers[jump.registerNo].isWaiting)
                reservations[loadIndex].Qk = registers[jump.registerNo].functionUnit;
            else
                reservations[loadIndex].Vk = registers[jump.registerNo].value;
            instruction.issue = timer;
        }
        return true;
    }

    void Exec(){
        //move from res to run, start timeleft, minus already run.
        //adders multers loaders
        int addAvailable = ADDNUM, mulAvailable = MULNUM, loadAvailable = LOADNUM;
        for (int i = 0; i < adders.length; i++) {
            if(adders[i].isBusy) {
                adders[i].runtimeLeft--;
                addAvailable--;
            }
        }
        for (int i = 0; i < multers.length; i++) {
            if(multers[i].isBusy) {
                multers[i].runtimeLeft--;
                mulAvailable--;
            }
        }
        for (int i = 0; i < loaders.length; i++) {
            if(loaders[i].isBusy){
                loaders[i].runtimeLeft--;
                loadAvailable--;
            }
        }
        //check ready in AddRS MulRS LoadBuffer
        while(addAvailable != 0) {
            boolean ready = false;
            int earlyIndex = -1;
            int earlytime = Integer.MAX_VALUE;
            for (int i = 0; i < addRS.length; i++) {
                if (addRS[i].isBusy && addRS[i].isReady && addRS[i].issueTime < earlytime && addRS[i].issueTime != timer){
                    ready = true;
                    earlyIndex = i;
                    earlytime = addRS[i].issueTime;
                }
            }
            if(ready){//someone is ready to exec
                addAvailable--;
                for (int i = 0; i < adders.length; i++) {
                    if(!adders[i].isBusy){
                        startExec(adders[i], addRS[earlyIndex]);
                        break;
                        //problem how to update functionUnit?
                    }
                }
            }
            else
                break;
        }
        while(mulAvailable != 0) {
            boolean ready = false;
            int earlyIndex = -1;
            int earlytime = Integer.MAX_VALUE;
            for (int i = 0; i < mulRS.length; i++) {
                if (mulRS[i].isBusy && mulRS[i].isReady && mulRS[i].issueTime < earlytime && mulRS[i].issueTime != timer){
                    ready = true;
                    earlyIndex = i;
                    earlytime = mulRS[i].issueTime;
                }
            }
            if(ready){//someone is ready to exec
                mulAvailable--;
                for (int i = 0; i < multers.length; i++) {
                    if(!multers[i].isBusy){
                        startExec(multers[i], mulRS[earlyIndex]);
                        break;
                        //problem how to update functionUnit?
                    }
                }
            }
            else
                break;
        }
        while(loadAvailable != 0) {
            boolean ready = false;
            int earlyIndex = -1;
            int earlytime = Integer.MAX_VALUE;
            for (int i = 0; i < loadBuffers.length; i++) {
                if (loadBuffers[i].isBusy && loadBuffers[i].issueTime < earlytime && loadBuffers[i].issueTime != timer){
                    ready = true;
                    earlyIndex = i;
                    earlytime = loadBuffers[i].issueTime;
                }
            }
            if(ready){//someone is ready to exec
                loadAvailable--;
                for (int i = 0; i < loaders.length; i++) {
                    if(!loaders[i].isBusy){
                        System.out.println("start exec load: "+earlyIndex+" "+i);
                        loaders[i].runtimeLeft = 3;
                        loaders[i].isBusy = true;
                        loaders[i].instruction = loadBuffers[earlyIndex].instruction;
                        loaders[i].instruction.exec = timer;
                        LoadInstruction load = (LoadInstruction) loaders[i].instruction;
                        loaders[i].result = load.loadAddr;
                        //problem how to update functionUnit?
                        //restore buffer
                        loadBuffers[earlyIndex].isBusy = false;
                        break;
                    }
                }
            }
            else
                break;
        }
    }

    void startExec(Calculator calculator, Reservation reservation){
        calculator.isBusy = true;
        calculator.instruction = reservation.instruction;
        calculator.instruction.exec = timer;
        switch (reservation.operation){
            case ADD:
                calculator.runtimeLeft = 3;
                calculator.result = reservation.Vj + reservation.Vk;
                break;
            case SUB:
                calculator.runtimeLeft = 3;
                calculator.result = reservation.Vj - reservation.Vk;
                break;
            case JUMP:
                calculator.runtimeLeft = 1;
                calculator.result = reservation.Vj - reservation.Vk;
                break;
            case MUL:
                calculator.runtimeLeft = 12;
                calculator.result = reservation.Vj * reservation.Vk;
                break;
            case DIV:
                calculator.runtimeLeft = 40;
                calculator.result = reservation.Vj / reservation.Vk;
                break;
            default:
                System.out.println("gg");
                exit(0);
        }
        //restore
        reservation.isReady = false;
        reservation.isBusy = false;
        reservation.Qj = null;
        reservation.Qk = null;
    }

    void PrintState(){
        for (int i = 0; i < adders.length; i++) {
            System.out.println("adders: "+adders[i].isBusy+" "+adders[i].runtimeLeft+" "+adders[i].result);
        }
        for (int i = 0; i < multers.length; i++) {
            System.out.println("multers: "+multers[i].isBusy+" "+multers[i].runtimeLeft+" "+multers[i].result);
        }
        for (int i = 0; i < loaders.length; i++) {
            System.out.println("loaders: "+loaders[i].isBusy+" "+loaders[i].runtimeLeft+" "+loaders[i].result);
        }
        for (int i = 0; i < addRS.length; i++) {
            System.out.println("add reservation: "+addRS[i].isBusy+" "+addRS[i].Vj+" "+addRS[i].Vk+" "+addRS[i].Qj+" "+addRS[i].Qk+" "+addRS[i].issueTime+" "+addRS[i].operation);
        }
        for (int i = 0; i < mulRS.length; i++) {
            System.out.println("mul reservation: "+mulRS[i].isBusy+" "+mulRS[i].Vj+" "+mulRS[i].Vk+" "+mulRS[i].Qj+" "+mulRS[i].Qk+" "+mulRS[i].issueTime+" "+mulRS[i].operation);
        }
        for (int i = 0; i < loadBuffers.length; i++) {
            System.out.println("load buffer: "+loadBuffers[i].isBusy+" "+loadBuffers[i].issueTime);
        }
        for (int i = 0; i < registers.length; i++) {
            System.out.println("register: "+registers[i].isWaiting+" "+registers[i].functionUnit+" "+registers[i].value);
        }
        System.out.print("hasJump nextIndex timer: "+hasJump+" "+nextInstructionIndex+" "+timer);
    }
}

class Calculator{
    int runtimeLeft;
    boolean isBusy;
    int result;
    Instruction instruction;
    Calculator(){
        isBusy = false;
    }
}

class Reservation{
    int issueTime;
    boolean isBusy;
    boolean isReady;
    InstructionType operation;
    String Qj, Qk;
    int Vj, Vk;
    Instruction instruction;
    Reservation(){
        isBusy = false;
        isReady = false;
    }
}

class Loader{
    int runtimeLeft;
    boolean isBusy;
    int result;
    Instruction instruction;
    Loader(){
        isBusy = false;
    }
}

class LoadBuffer{
    int issueTime;
    boolean isBusy;
    Instruction instruction;
    LoadBuffer(){
        isBusy = false;
    }
}

class RegisterResultStatus{
    String functionUnit;
    boolean isWaiting;
    int value;
    RegisterResultStatus(){
        functionUnit = null;
        isWaiting = false;
        value = 0;
    }
}