import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tomasulo {
    static InstructionLoader loader;
    static TomasuloProcessor processor;
    JFrame frame;
    ResPanel resPanel;
    BufferPanel bufferPanel;
    RegisterPanel registerPanel;
    CalPanel calPanel;

    public static List<String> readFile(String pathname) {
        List<String> instructions = new ArrayList<>();
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            while ((line = br.readLine()) != null) {
                System.out.println(line);
                instructions.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return instructions;
    }
    //blog.csdn.net/nickwong_/article/details/51502969

    public static void main(String []args){
        JFrame.setDefaultLookAndFeelDecorated(true);
        Tomasulo tomasulo = new Tomasulo();

        tomasulo.resPanel = new ResPanel();
        tomasulo.bufferPanel = new BufferPanel();
        tomasulo.registerPanel = new RegisterPanel();
        tomasulo.calPanel = new CalPanel();

        tomasulo.frame = new JFrame();

        tomasulo.frame.setLayout(new GridLayout(5, 1));
        tomasulo.frame.add(tomasulo.resPanel);
        tomasulo.frame.add(tomasulo.bufferPanel);
        tomasulo.frame.add(tomasulo.registerPanel);
        tomasulo.frame.add(tomasulo.calPanel);
        tomasulo.frame.setSize(810, 840);
        tomasulo.frame.setVisible(true);
        tomasulo.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        int a = 3/2, b = (-3)/2;
//        System.out.println(a);
//        System.out.println(b);
        String pathname = "../test2.nel";
        List<String> instructionString = readFile(pathname);
        System.out.println(instructionString.size());

        tomasulo.loader = new InstructionLoader();
        Instruction[] instructions = tomasulo.loader.LoadInstructionsFromFile(instructionString, instructionString.size());
        tomasulo.processor = new TomasuloProcessor(tomasulo);
        tomasulo.processor.Process(instructions);
//        System.out.println("finished");
    }

    public void updateDisplay() {
        for (int i = 0; i < processor.addRS.length; i++) {
            String text = processor.addRS[i].isBusy == true ? "Yes" : "No";
            resPanel.labels[7*(i+1)+1].setText(text);
            if(processor.addRS[i].isBusy) {
                resPanel.labels[7 * (i + 1) + 2].setText(instructionToString(processor.addRS[i].operation));
                resPanel.labels[7 * (i + 1) + 3].setText(Integer.toString(processor.addRS[i].Vj));
                resPanel.labels[7 * (i + 1) + 4].setText(Integer.toString(processor.addRS[i].Vk));
                resPanel.labels[7 * (i + 1) + 5].setText(processor.addRS[i].Qj);
                resPanel.labels[7 * (i + 1) + 6].setText(processor.addRS[i].Qk);
            }
            else{
                resPanel.labels[7 * (i + 1) + 2].setText(null);
                resPanel.labels[7 * (i + 1) + 3].setText(null);
                resPanel.labels[7 * (i + 1) + 4].setText(null);
                resPanel.labels[7 * (i + 1) + 5].setText(null);
                resPanel.labels[7 * (i + 1) + 6].setText(null);
            }
        }

        for (int i = 0; i < processor.mulRS.length; i++) {
            String text = processor.mulRS[i].isBusy == true ? "Yes" : "No";
            resPanel.labels[7*(i+7)+1].setText(text);
            if(processor.mulRS[i].isBusy) {
                resPanel.labels[7 * (i + 7) + 2].setText(instructionToString(processor.mulRS[i].operation));
                resPanel.labels[7 * (i + 7) + 3].setText(Integer.toString(processor.mulRS[i].Vj));
                resPanel.labels[7 * (i + 7) + 4].setText(Integer.toString(processor.mulRS[i].Vk));
                resPanel.labels[7 * (i + 7) + 5].setText(processor.mulRS[i].Qj);
                resPanel.labels[7 * (i + 7) + 6].setText(processor.mulRS[i].Qk);
            }
            else {
                resPanel.labels[7 * (i + 7) + 2].setText(null);
                resPanel.labels[7 * (i + 7) + 3].setText(null);
                resPanel.labels[7 * (i + 7) + 4].setText(null);
                resPanel.labels[7 * (i + 7) + 5].setText(null);
                resPanel.labels[7 * (i + 7) + 6].setText(null);
            }
        }

        for (int i = 0; i < processor.loadBuffers.length; i++) {
            String text = processor.loadBuffers[i].isBusy == true ? "Yes" : "No";
            bufferPanel.labels[3*(i+1)+1].setText(text);
            if(processor.loadBuffers[i].isBusy) {
                text = Integer.toString(((LoadInstruction) processor.loadBuffers[i].instruction).loadAddr);
                bufferPanel.labels[3 * (i + 1) + 2].setText(text);
            }
            else{
                bufferPanel.labels[3 * (i + 1) + 2].setText(null);
            }
        }

        for (int i = 0; i < processor.registers.length; i++) {
            registerPanel.labels[33+i+1].setText(processor.registers[i].functionUnit);
            registerPanel.labels[66+i+1].setText(Integer.toString(processor.registers[i].value));
        }

        for (int i = 0; i < processor.adders.length; i++) {
            if(processor.adders[i].isBusy) {
                calPanel.labels[3 * (i + 1) + 1].setText(processor.adders[i].instruction.instructionType.toString());
                calPanel.labels[3 * (i + 1) + 2].setText(Integer.toString(processor.adders[i].runtimeLeft));
            }
            else{
                calPanel.labels[3*(i+1)+1].setText(null);
                calPanel.labels[3*(i+1)+2].setText(null);
            }
        }
        for (int i = 0; i < processor.multers.length; i++) {
            if(processor.multers[i].isBusy) {
                calPanel.labels[3 * (i + 4) + 1].setText(processor.multers[i].instruction.instructionType.toString());
                calPanel.labels[3 * (i + 4) + 2].setText(Integer.toString(processor.multers[i].runtimeLeft));
            }
            else{
                calPanel.labels[3*(i+4)+1].setText(null);
                calPanel.labels[3*(i+4)+2].setText(null);
            }
        }
        for (int i = 0; i < processor.loaders.length; i++) {
            if (processor.loaders[i].isBusy) {
                calPanel.labels[3 * (i + 6) + 1].setText(processor.loaders[i].instruction.instructionType.toString());
                calPanel.labels[3 * (i + 6) + 2].setText(Integer.toString(processor.loaders[i].runtimeLeft));
            }
            else{
                calPanel.labels[3 * (i + 6) + 1].setText(null);
                calPanel.labels[3 * (i + 6) + 2].setText(null);
            }
        }
    }

    String instructionToString(InstructionType instructionType){
        if(instructionType == null) return "";
        else return instructionType.toString();
    }
}
