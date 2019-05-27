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
            Issue();
//            Exec();
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
        instruction.issue = timer;
        LoadInstruction load = (LoadInstruction)instruction;
        registers[load.registerNo].functionUnit = "load"+Integer.toString(loadIndex);
        registers[load.registerNo].isWaiting = true;
        return true;
    }

    boolean IssueReservation(Instruction instruction, Reservation[] reservations){
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

    void PrintState(){
        for (int i = 0; i < adders.length; i++) {

        }
        for (int i = 0; i < multers.length; i++) {

        }
        for (int i = 0; i < loaders.length; i++) {

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
    String reservation;
    Calculator(){
        isBusy = false;
    }
}

class Reservation{
    int issueTime;
    boolean isBusy;
    InstructionType operation;
    String Qj, Qk;
    int Vj, Vk;
    Reservation(){
        isBusy = false;
    }
}

class Loader{
    int runtimeLeft;
    boolean isBusy;
    String reservation;
    Loader(){
        isBusy = false;
    }
}

class LoadBuffer{
    int issueTime;
    boolean isBusy;
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