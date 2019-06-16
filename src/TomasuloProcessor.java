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
    Tomasulo father;

    TomasuloProcessor(Tomasulo tomasulo, Instruction[] instructions){
        father = tomasulo;
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
        this.instructions = instructions;
        nextInstructionIndex = 0;
        timer = 0;
    }

    boolean Process(){//return value indicate whether or not the process finishes
        if(nextInstructionIndex >= instructions.length && Finished()) {//whether the process finishes or not
//            System.out.println("finished");
            return false;
        }
        timer++;
//        System.out.println("\ncycle timer: "+timer);
        Write();
        Exec();
        Issue();
        return true;
//        PrintState();
    }

    boolean Finished(){
        for (int i = 0; i < adders.length; i++) {
            if (adders[i].isBusy) return false;
        }
        for (int i = 0; i < multers.length; i++) {
            if (multers[i].isBusy) return false;
        }
        for (int i = 0; i < loaders.length; i++) {
            if (loaders[i].isBusy) return false;
        }
        for (int i = 0; i < addRS.length; i++) {
            if (addRS[i].isBusy) return false;
        }
        for (int i = 0; i < mulRS.length; i++) {
            if (mulRS[i].isBusy) return false;
        }
        for (int i = 0; i < loadBuffers.length; i++) {
            if (loadBuffers[i].isBusy) return false;
        }
        return true;
    }

    void Issue(){
        if(hasJump || nextInstructionIndex >= instructions.length) return;
        Instruction nextInstruction = instructions[nextInstructionIndex];
//        System.out.println("Issue new instruction: pc = "+nextInstructionIndex+" type = "+nextInstruction.instructionType);
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
//        System.out.println("issue loadbuffer: type = "+instruction.instructionType+" timer = "+timer);
        int loadIndex = -1;
        for (int i = 0; i < loadBuffers.length; i++) {
            if(!loadBuffers[i].isBusy && loadBuffers[i].writeTime != timer){
                loadIndex = i;
                break;
            }
        }
        if(loadIndex == -1) return false;
        loadBuffers[loadIndex].isBusy = true;
        loadBuffers[loadIndex].issueTime = timer;
        loadBuffers[loadIndex].writeTime = -1;
        loadBuffers[loadIndex].instruction = instruction;
        if(instruction.issue == -1)
            instruction.issue = timer;
        LoadInstruction load = (LoadInstruction)instruction;
        registers[load.registerNo].functionUnit = "loadBuffer"+Integer.toString(loadIndex);
        registers[load.registerNo].isWaiting = true;
        return true;
    }

    boolean IssueReservation(Instruction instruction, Reservation[] reservations){
//        System.out.print("issue reservation: type = "+instruction.instructionType+" timer = "+timer);
        int loadIndex = -1;
        for (int i = 0; i < reservations.length; i++) {
            if(!reservations[i].isBusy && reservations[i].writeTime != timer){
                loadIndex = i;
                break;
            }
        }
        if(loadIndex == -1) return false;
        reservations[loadIndex].isBusy = true;
        reservations[loadIndex].issueTime = timer;
        reservations[loadIndex].operation = instruction.instructionType;
        reservations[loadIndex].instruction = instruction;
        reservations[loadIndex].writeTime = -1;
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
            if(reservations[loadIndex].Qj == null && reservations[loadIndex].Qk == null){
                reservations[loadIndex].isReady = true;
                reservations[loadIndex].readyTime = timer;
            }
//            System.out.println(" loadIndex = "+loadIndex+" Vj Vk Qj Qk = "+reservations[loadIndex].Vj+" "+reservations[loadIndex].Vk+" "+reservations[loadIndex].Qj+" "+reservations[loadIndex].Qk+" ready = "+reservations[loadIndex].isReady);
            if(instruction.issue == -1)
                instruction.issue = timer;
            if (instruction.instructionType == InstructionType.ADD || instruction.instructionType == InstructionType.SUB)
                registers[cal.registerD].functionUnit = "addRS" + Integer.toString(loadIndex);
            else
                registers[cal.registerD].functionUnit = "mulRS" + Integer.toString(loadIndex);
            registers[cal.registerD].isWaiting = true;
        }
        else{//jump instruction
            JumpInstruction jump = (JumpInstruction) instruction;
            reservations[loadIndex].Vj = jump.compare;
            if (registers[jump.registerNo].isWaiting)
                reservations[loadIndex].Qk = registers[jump.registerNo].functionUnit;
            else
                reservations[loadIndex].Vk = registers[jump.registerNo].value;
            if(reservations[loadIndex].Qk == null){
                reservations[loadIndex].isReady = true;
                reservations[loadIndex].readyTime = timer;
            }
            if(instruction.issue == -1)
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
                if(adders[i].runtimeLeft == 1 && adders[i].instruction.exec == -1)
                    adders[i].instruction.exec = timer;
                addAvailable--;
            }
        }
        for (int i = 0; i < multers.length; i++) {
            if(multers[i].isBusy) {
                multers[i].runtimeLeft--;
                if(multers[i].runtimeLeft == 1 && multers[i].instruction.exec == -1)
                    multers[i].instruction.exec = timer;
                mulAvailable--;
            }
        }
        for (int i = 0; i < loaders.length; i++) {
            if(loaders[i].isBusy){
                loaders[i].runtimeLeft--;
                if(loaders[i].runtimeLeft == 1 && loaders[i].instruction.exec == -1)
                    loaders[i].instruction.exec = timer;
                loadAvailable--;
            }
        }
        //check ready in AddRS MulRS LoadBuffer
        while(addAvailable != 0) {
            boolean ready = false;
            int earlyIndex = -1;
            int earlytime = Integer.MAX_VALUE;
            for (int i = 0; i < addRS.length; i++) {
//                System.out.println("exec: "+addRS[i].isBusy+addRS[i].isReady+(addRS[i].issueTime < earlytime)+addRS[i].readyTime+timer);
                if (addRS[i].isBusy && addRS[i].isReady && !addRS[i].isExec && addRS[i].issueTime < earlytime && addRS[i].readyTime != timer){
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
//                        System.out.println("start exec "+adders[i].instruction.instructionType+": addRS"+earlyIndex+" to adder"+i+" Vj = "+addRS[earlyIndex].Vj+" Vk = "+addRS[earlyIndex].Vk+" result = "+adders[i].result);
                        String oldFU = "addRS" + Integer.toString(earlyIndex);
                        String newFU = "adder" + Integer.toString(i);
                        updateFU(oldFU, newFU);
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
                if (mulRS[i].isBusy && mulRS[i].isReady && !mulRS[i].isExec && mulRS[i].issueTime < earlytime && mulRS[i].readyTime != timer){
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
//                        System.out.println("start exec "+multers[i].instruction.instructionType+": mulRS"+earlyIndex+" to multer"+i+" Vj = "+mulRS[earlyIndex].Vj+" Vk = "+mulRS[earlyIndex].Vk+" result = "+multers[i].result);
                        String oldFU = "mulRS" + Integer.toString(earlyIndex);
                        String newFU = "multer" + Integer.toString(i);
                        updateFU(oldFU, newFU);
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
                if (loadBuffers[i].isBusy && !loadBuffers[i].isExec && loadBuffers[i].issueTime < earlytime && loadBuffers[i].issueTime != timer){
                    ready = true;
                    earlyIndex = i;
                    earlytime = loadBuffers[i].issueTime;
                }
            }
            if(ready){//someone is ready to exec
                loadAvailable--;
                for (int i = 0; i < loaders.length; i++) {
                    if(!loaders[i].isBusy){
//                        System.out.print("start exec load: loadBuffer"+earlyIndex+" to loader"+i+" instant = ");
                        loaders[i].runtimeLeft = 3;
                        loaders[i].isBusy = true;
                        loaders[i].instruction = loadBuffers[earlyIndex].instruction;
//                        loaders[i].instruction.exec = timer;
                        LoadInstruction load = (LoadInstruction) loaders[i].instruction;
                        loaders[i].result = load.loadAddr;
//                        System.out.println(load.loadAddr);
                        //problem how to update functionUnit?
                        //restore buffer no longer needed
//                        loadBuffers[earlyIndex].isBusy = false;
                        loadBuffers[earlyIndex].isExec = true;
                        loaders[i].loadBuffer = loadBuffers[earlyIndex];
                        //update FU
                        String oldFU = "loadBuffer" + Integer.toString(earlyIndex);
                        String newFU = "loader" + Integer.toString(i);
                        updateFU(oldFU, newFU);
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
        calculator.reservation = reservation;
        reservation.isExec = true;
//        calculator.instruction.exec = timer;
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
                if(calculator.instruction.exec == -1)
                    calculator.instruction.exec = timer;
                calculator.result = reservation.Vj == reservation.Vk ? ((JumpInstruction)reservation.instruction).jumpAddr : 1;
                break;
            case MUL:
                calculator.runtimeLeft = 12;
                calculator.result = reservation.Vj * reservation.Vk;
                break;
            case DIV:
                if(reservation.Vk == 0){
                    calculator.result = reservation.Vj;
                    calculator.runtimeLeft = 1;
                    if(calculator.instruction.exec == -1)
                        calculator.instruction.exec = timer;
                }
                else {
                    calculator.runtimeLeft = 40;
                    calculator.result = reservation.Vj / reservation.Vk;
                }
                break;
            default:
//                System.out.println("gg");
                exit(0);
        }
//        //restore is no longer needed
//        reservation.isReady = false;
//        reservation.readyTime = 0;
//        reservation.isBusy = false;
//        reservation.Qj = null;
//        reservation.Qk = null;
    }

    void updateFU(String oldFU, String newFU){
//        System.out.println("updateFU "+oldFU+" "+newFU);
        for (int i = 0; i < registers.length; i++) {
            if(registers[i].isWaiting && oldFU.equals(registers[i].functionUnit)){
                registers[i].functionUnit = newFU;
            }
        }
        for (int i = 0; i < addRS.length; i++) {
            if(oldFU.equals(addRS[i].Qj))
                addRS[i].Qj = newFU;
            if(oldFU.equals(addRS[i].Qk))
                addRS[i].Qk = newFU;
        }
        for (int i = 0; i < mulRS.length; i++) {
            if(oldFU.equals(mulRS[i].Qj))
                mulRS[i].Qj = newFU;
            if(oldFU.equals(mulRS[i].Qk))
                mulRS[i].Qk = newFU;
        }
    }

    void Write(){
        for (int i = 0; i < adders.length; i++) {
            if(adders[i].isBusy && adders[i].runtimeLeft == 1){
//                System.out.println("write adders: type = "+adders[i].instruction.instructionType+" result = "+adders[i].result);
                startWrite(adders[i]);
                if(adders[i].instruction.instructionType != InstructionType.JUMP) {
                    String old = "adder" + Integer.toString(i);
                    updateValue(old, adders[i].result);
                }
                else{//no fu should be waiting for jump instruction.
                    nextInstructionIndex += adders[i].result-1;
                    hasJump = false;
                }
            }
        }
        for (int i = 0; i < multers.length; i++) {
            if(multers[i].isBusy && multers[i].runtimeLeft == 1){
//                System.out.println("write multers type = "+multers[i].instruction.instructionType+" result = "+multers[i].result);
                startWrite(multers[i]);
                String old = "multer"+Integer.toString(i);
                updateValue(old, multers[i].result);
            }
        }
        for (int i = 0; i < loaders.length; i++) {
            if(loaders[i].isBusy && loaders[i].runtimeLeft == 1){
//                System.out.println("write loaders type = "+loaders[i].instruction.instructionType+" result = "+loaders[i].result);
                loaders[i].isBusy = false;
                loaders[i].loadBuffer.isBusy = false;
                loaders[i].loadBuffer.writeTime = timer;
                loaders[i].loadBuffer.isExec = false;
                loaders[i].loadBuffer = null;
                loaders[i].runtimeLeft = 0;
                if(loaders[i].instruction.write == -1)
                    loaders[i].instruction.write = timer;
                String old = "loader"+Integer.toString(i);
                updateValue(old, loaders[i].result);
            }
        }
    }

    void updateValue(String oldFU, int value){
//        System.out.println("updateValue "+oldFU+" "+value);
        for (int i = 0; i < registers.length; i++) {
            if(registers[i].isWaiting && oldFU.equals(registers[i].functionUnit)){
                registers[i].isWaiting = false;
                registers[i].functionUnit = null;
                registers[i].value = value;
            }
        }
        for (int i = 0; i < addRS.length; i++) {
            if(addRS[i].isBusy && (addRS[i].Qj != null || addRS[i].Qk != null)) {
                if (oldFU.equals(addRS[i].Qj)) {
                    addRS[i].Qj = null;
                    addRS[i].Vj = value;
                }
                if (oldFU.equals(addRS[i].Qk)) {
                    addRS[i].Qk = null;
                    addRS[i].Vk = value;
                }
                if (addRS[i].Qj == null && addRS[i].Qk == null) {
                    addRS[i].isReady = true;
                    addRS[i].readyTime = timer;
                }
            }
        }
        for (int i = 0; i < mulRS.length; i++) {
            if(mulRS[i].isBusy && (mulRS[i].Qj != null || mulRS[i].Qk != null)) {//is waiting
                if (oldFU.equals(mulRS[i].Qj)) {
                    mulRS[i].Qj = null;
                    mulRS[i].Vj = value;
                }
                if (oldFU.equals(mulRS[i].Qk)) {
                    mulRS[i].Qk = null;
                    mulRS[i].Vk = value;
                }
                if (mulRS[i].Qj == null && mulRS[i].Qk == null) {
                    mulRS[i].isReady = true;
                    mulRS[i].readyTime = timer;
                }
            }
        }
    }

    void startWrite(Calculator calculator){
        calculator.isBusy = false;
        calculator.runtimeLeft = 0;
        //restore
        calculator.reservation.isReady = false;
        calculator.reservation.readyTime = 0;
        calculator.reservation.isBusy = false;
        calculator.reservation.Qj = null;
        calculator.reservation.Qk = null;
        calculator.reservation.writeTime = timer;
        calculator.reservation.isExec = false;
        calculator.reservation = null;
        if(calculator.instruction.write == -1)
            calculator.instruction.write = timer;
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
        //print instruction timer
        for (int i = 0; i < instructions.length; i++) {
            System.out.println(instructions[i].instructionType+" "+instructions[i].issue+" "+instructions[i].exec+" "+instructions[i].write);
        }
        System.out.print("hasJump nextIndex timer: "+hasJump+" "+nextInstructionIndex+" "+timer);
    }
}

class Calculator{
    int runtimeLeft;
    boolean isBusy;
    int result;
    Instruction instruction;
    Reservation reservation;
    Calculator(){
        isBusy = false;
        reservation = null;
    }
}

class Reservation{
    int issueTime;
    boolean isBusy;
    boolean isReady;
    boolean isExec;
    int readyTime;
    int writeTime;
    InstructionType operation;
    String Qj, Qk;
    int Vj, Vk;
    Instruction instruction;
    Reservation(){
        isExec = false;
        isBusy = false;
        isReady = false;
        Qj = Qk = null;
        readyTime = 0;
        writeTime = -1;
    }
}

class Loader{
    int runtimeLeft;
    boolean isBusy;
    int result;
    Instruction instruction;
    LoadBuffer loadBuffer;
    Loader(){
        isBusy = false;
        loadBuffer = null;
    }
}

class LoadBuffer{
    int issueTime;
    boolean isBusy;
    Instruction instruction;
    int writeTime;
    boolean isExec;
    LoadBuffer(){
        isBusy = false;
        isExec = false;
        writeTime = -1;
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