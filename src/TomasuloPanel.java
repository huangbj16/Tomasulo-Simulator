import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class ResPanel extends JPanel {
    GridLayout gridLayout;
    JLabel[] labels;
    int row = 10, col = 7;

    public ResPanel() {
        super();
        gridLayout = new GridLayout(row, col);
        this.setLayout(gridLayout);
        labels = new JLabel[row*col];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("", JLabel.CENTER);
//            labels[i].setPreferredSize(new Dimension(10, 10));
            labels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.add(labels[i]);
        }
        labels[0].setText("保留站状态");
        labels[1].setText("Busy");
        labels[2].setText("Op");
        labels[3].setText("Vj");
        labels[4].setText("Vk");
        labels[5].setText("Qj");
        labels[6].setText("Qk");
        String prefix = "Ars ";
        for (int i = 0; i < 6; i++) {
            labels[7+i*7].setText(prefix+(i+1));
        }
        prefix = "Mrs ";
        for (int i = 0; i < 3; i++) {
            labels[49+i*7].setText(prefix+(i+1));
        }
    }
}

class BufferPanel extends JPanel {
    GridLayout gridLayout;
    JLabel[] labels;
    int row = 4, col = 3;

    public BufferPanel() {
        super();
        gridLayout = new GridLayout(row, col);
        this.setLayout(gridLayout);
        labels = new JLabel[row*col];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("", JLabel.CENTER);
            labels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.add(labels[i]);
        }
        labels[0].setText("LoadBuffer状态");
        labels[1].setText("Busy");
        labels[2].setText("Address");
        String prefix = "LB ";
        for (int i = 0; i < 3; i++) {
            labels[3+i*3].setText(prefix+(i+1));
        }
    }
}

class RegisterPanel extends JPanel {
    GridLayout gridLayout;
    JLabel[] labels;
    int row = 6, col = 17;

    public RegisterPanel() {
        super();
        gridLayout = new GridLayout(row, col);
        this.setLayout(gridLayout);
        labels = new JLabel[row*col];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("", JLabel.CENTER);
            labels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.add(labels[i]);
        }
        labels[0].setText("寄存器状态");
        labels[17].setText("State");
        labels[34].setText("Value");
        labels[51].setText("寄存器状态");
        labels[68].setText("State");
        labels[85].setText("Value");
        String prefix = "F";
        for (int i = 0; i < 16; i++) {
            labels[1+i].setText(prefix+i);
        }
        for (int i = 16; i < 32; i++) {
            labels[52+i-16].setText(prefix+i);
        }
    }
}

class CalPanel extends JPanel {
    GridLayout gridLayout;
    JLabel[] labels;
    int row = 8, col = 3;

    public CalPanel() {
        super();
        gridLayout = new GridLayout(row, col);
        this.setLayout(gridLayout);
        labels = new JLabel[row*col];
        for (int i = 0; i < labels.length; i++) {
            labels[i] = new JLabel("", JLabel.CENTER);
            labels[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
            this.add(labels[i]);
        }
        labels[0].setText("运算部件状态");
        labels[1].setText("当前执行指令");
        labels[2].setText("剩余周期");
        labels[3].setText("Add 1");
        labels[6].setText("Add 2");
        labels[9].setText("Add 3");
        labels[12].setText("Mult 1");
        labels[15].setText("Mult 2");
        labels[18].setText("Load 1");
        labels[21].setText("Load 2");
    }
}

class ControlPanel extends JPanel{
    FlowLayout flowLayout;
    JButton button;
    JLabel timerText, timer;
    public ControlPanel() {
        super();
        flowLayout = new FlowLayout();
        this.setLayout(flowLayout);
        button = new JButton("next");
        this.add(button);
        timerText = new JLabel("cycle: ");
        this.add(timerText);
        timer = new JLabel("0");
        this.add(timer);
    }
}