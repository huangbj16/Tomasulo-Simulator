public class Instruction {
    InstructionType instructionType;
    int issue, exec, write;
    Instruction(){
        issue = exec = write = -1;
    }
}

class LoadInstruction extends Instruction {
    int registerNo;
    int loadAddr;
    LoadInstruction(String instruction){
        super();
        String[] segments = instruction.split(",");
        registerNo = Integer.parseInt(segments[1].replaceAll("F", ""));
        loadAddr = Integer.parseUnsignedInt(segments[2].replaceAll("0x", "").toLowerCase(), 16);
//        System.out.println(Integer.toHexString(255));
//        System.out.println(Integer.parseUnsignedInt(segments[2].replaceAll("0x", "").toLowerCase(), 16));
        instructionType = InstructionType.LOAD;
    }
}

class CalInstruction extends Instruction {
    int registerD, registerS1, registerS2;
    CalInstruction(String instruction, InstructionType type){
        super();
        String[] segments = instruction.split(",");
        registerD = Integer.parseInt(segments[1].replaceAll("F", ""));;
        registerS1 = Integer.parseInt(segments[2].replaceAll("F", ""));;
        registerS2 = Integer.parseInt(segments[3].replaceAll("F", ""));;
        instructionType = type;
    }
}

class JumpInstruction extends Instruction {
    int compare;
    int registerNo;
    int jumpAddr;
    JumpInstruction(String instruction){
        super();
        String[] segments = instruction.split(",");
        compare = Integer.parseUnsignedInt(segments[1].replaceAll("0x", "").toLowerCase(), 16);
        registerNo = Integer.parseInt(segments[2].replaceAll("F", ""));
        jumpAddr = Integer.parseUnsignedInt(segments[3].replaceAll("0x", "").toLowerCase(), 16);
        instructionType = InstructionType.JUMP;
    }
}