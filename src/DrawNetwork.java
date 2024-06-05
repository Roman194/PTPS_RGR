import javax.swing.*;
import java.awt.*;
import java.util.Arrays;
import java.util.Objects;

public class DrawNetwork extends JPanel {
    Student [] students;

    @Override
    protected void paintComponent(Graphics g) {

        Graphics2D g2d = (Graphics2D) g.create();

        g2d.setColor(Color.BLACK); //задний план покрашен в чёрный
        g2d.fillRect(0, 0, 1000, 1000);

        g2d.setColor(Color.WHITE);
        int currStudNum = 0;
        for(Student student: students){ //перебор студентов с целью рисования всех линий

            if(!Objects.equals(student.connections(), "-")) {
                String[] connections = student.connections().split(" ");
                for (int currConnStudNum = currStudNum + 1; currConnStudNum < students.length; currConnStudNum++) {
                    Student connectedStudent = students[currConnStudNum];
                    if (Arrays.asList(connections).contains(connectedStudent.name())) {
                        g2d.drawLine(student.x(), student.y(), connectedStudent.x(), connectedStudent.y());
                    }
                }
            }

            currStudNum++;
        }
    }
    public DrawNetwork(Student [] students){
        this.students = students;
    }
}
