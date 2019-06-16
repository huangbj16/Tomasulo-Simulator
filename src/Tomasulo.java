import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
    ControlPanel controlPanel;
    InstructionPanel instructionPanel;
    JScrollPane scrollPane;
    List<String> instructionString;

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
        String pathname = "../Archive/test0.nel";
        List<String> instructionString = readFile(pathname);
        System.out.println(instructionString.size());

        JFrame.setDefaultLookAndFeelDecorated(true);
        try{
            UIManager.setLookAndFeel("ch.randelshofer.quaqua.QuaquaLookAndFeel");
        } catch(Exception e){
            e.printStackTrace();
        }
        Tomasulo tomasulo = new Tomasulo();
        tomasulo.instructionString = instructionString;
        tomasulo.resPanel = new ResPanel();
        tomasulo.bufferPanel = new BufferPanel();
        tomasulo.registerPanel = new RegisterPanel();
        tomasulo.calPanel = new CalPanel();
        tomasulo.controlPanel = new ControlPanel();
        tomasulo.instructionPanel = new InstructionPanel(instructionString.toArray());
        tomasulo.instructionPanel.setPreferredSize(new Dimension(200, 25*(tomasulo.instructionPanel.lineNum+1)));
        tomasulo.scrollPane = new JScrollPane(tomasulo.instructionPanel);
        tomasulo.frame = new JFrame();

        tomasulo.frame.setLayout(null);
        tomasulo.frame.setSize(1000, 1000);
        tomasulo.controlPanel.setBounds(100, 50, 400, 50);
        tomasulo.resPanel.setBounds(100, 100, 800, 200);
        tomasulo.bufferPanel.setBounds(100, 350, 200, 300);
        tomasulo.registerPanel.setBounds(100, 700, 800, 200);
        tomasulo.calPanel.setBounds(350, 350, 200, 300);
        tomasulo.scrollPane.setBounds(600, 350, 300, 300);
        tomasulo.frame.add(tomasulo.controlPanel);
        tomasulo.frame.add(tomasulo.resPanel);
        tomasulo.frame.add(tomasulo.bufferPanel);
        tomasulo.frame.add(tomasulo.registerPanel);
        tomasulo.frame.add(tomasulo.calPanel);
        tomasulo.frame.add(tomasulo.scrollPane);
        tomasulo.frame.setVisible(true);
        tomasulo.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//        int a = 3/2, b = (-3)/2;
//        System.out.println(a);
//        System.out.println(b);

        tomasulo.loader = new InstructionLoader();
        Instruction[] instructions = tomasulo.loader.LoadInstructionsFromFile(instructionString, instructionString.size());
        tomasulo.processor = new TomasuloProcessor(tomasulo, instructions);
        tomasulo.updateDisplay();
        tomasulo.controlPanel.button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tomasulo.processor.Process();
                tomasulo.updateDisplay();
                tomasulo.controlPanel.timer.setText(Integer.toString(tomasulo.processor.timer));
            }
        });
        tomasulo.controlPanel.endButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                long startTime =  System.currentTimeMillis();
                while(tomasulo.processor.Process() == true);
                tomasulo.instructionPanel.clear();
                tomasulo.updateDisplay();
                long endTime =  System.currentTimeMillis();
                long elaspTime = (endTime-startTime);
                System.out.println("time: "+elaspTime+"ms");
                tomasulo.controlPanel.timer.setText(Integer.toString(tomasulo.processor.timer));
            }
        });
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

        for (int i = 0; i < processor.registers.length/2; i++) {
            registerPanel.labels[17+i+1].setText(processor.registers[i].functionUnit);
            registerPanel.labels[34+i+1].setText(Integer.toString(processor.registers[i].value));
        }
        for (int i = processor.registers.length/2; i < processor.registers.length; i++) {
            registerPanel.labels[68+i+1-16].setText(processor.registers[i].functionUnit);
            registerPanel.labels[85+i+1-16].setText(Integer.toString(processor.registers[i].value));
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
        int start = processor.nextInstructionIndex > 500 ? processor.nextInstructionIndex-500 : 0;
        for (int i = 0; i+start < processor.instructions.length && i < 1000; i++) {
            instructionPanel.instructionLabels[4+4*i].setText((String) instructionString.get(start+i));
            if(processor.instructions[i].issue != -1)
                instructionPanel.instructionLabels[4+4*i+1].setText(Integer.toString(processor.instructions[start+i].issue));
            if(processor.instructions[i].exec != -1)
                instructionPanel.instructionLabels[4+4*i+2].setText(Integer.toString(processor.instructions[start+i].exec));
            if(processor.instructions[i].write != -1)
                instructionPanel.instructionLabels[4+4*i+3].setText(Integer.toString(processor.instructions[start+i].write));
        }
    }

    String instructionToString(InstructionType instructionType){
        if(instructionType == null) return "";
        else return instructionType.toString();
    }
}
