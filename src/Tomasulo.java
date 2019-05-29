import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tomasulo {
    static InstructionLoader loader;
    static TomasuloProcessor processor;

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
//        int a = 3/2, b = (-3)/2;
//        System.out.println(a);
//        System.out.println(b);
        String pathname = "../test2.nel";
        List<String> instructionString = readFile(pathname);
        System.out.println(instructionString.size());
        loader = new InstructionLoader();
        Instruction[] instructions = loader.LoadInstructionsFromFile(instructionString, instructionString.size());
        processor = new TomasuloProcessor();
        processor.Process(instructions);
//        System.out.println("finished");
    }
}
