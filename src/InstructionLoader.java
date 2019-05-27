import java.util.List;

import static java.lang.System.exit;

public class InstructionLoader {
    Instruction[] LoadInstructionsFromFile(List<String> instructionString, int len){
        Instruction[] instructions = new Instruction[len];
        for (int i = 0; i < len; i++) {
            String line = instructionString.get(i);
            String[] segments = line.split(",");
            switch (segments[0]) {
                case "LD":
                    instructions[i] = new LoadInstruction(line);
                    break;
                case "ADD":
                    instructions[i] = new CalInstruction(line, InstructionType.ADD);
                    break;
                case "SUB":
                    instructions[i] = new CalInstruction(line, InstructionType.SUB);
                    break;
                case "MUL":
                    instructions[i] = new CalInstruction(line, InstructionType.MUL);
                    break;
                case "DIV":
                    instructions[i] = new CalInstruction(line, InstructionType.DIV);
                    break;
                case "JUMP":
                    instructions[i] = new JumpInstruction(line);
                    break;
                default:
                    System.out.println("gg!!");
                    exit(0);
            }
        }
        return instructions;
    }
}

