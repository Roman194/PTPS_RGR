import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.*;

public class Main {
    public static void main(String[] args) {

        Student [] students = new Student[]{
                new Student("Olga", 120, 200, ""),
                new Student("Anna", 25, 850, ""),
                new Student("Igor", 275, 470, ""),
                new Student("Roman", 800, 150, ""),
                new Student("Elena", 575, 100, ""),
                new Student("Rita", 250, 300, ""),
                new Student("Alexander", 640, 350, ""),
                new Student("Matvei", 700, 650, "")
        };
        List<String> studentNames = new ArrayList<>();

        Scanner sc = new Scanner(System.in);

        System.out.println("Please write connections for our students! You can't write connection for student himself, previous ones or with unknown student");
        System.out.print("There are list of known students: ");
        for(Student student:students){
            System.out.print(student.name() + ", ");
            studentNames.add(student.name());
        }


        System.out.println("");

        for(int i = 0; i < students.length - 1; i++){  //ввод связей между студентами
            System.out.println(students[i].name()+" connections : ");
            boolean isProperConnection = false;
            while(!isProperConnection) {
                isProperConnection = true;
                students[i] = new Student(
                        students[i].name(),
                        students[i].x(),
                        students[i].y(),
                        sc.nextLine()
                );

                if(!Objects.equals(students[i].connections(), "-")) { //проверка на то, что все введённые студенты соответствуют требованиям
                    String[] studentConnections = students[i].connections().split(" ");
                    for (String connection : studentConnections) {
                        if (!(studentNames.contains(connection))) {
                            System.out.println("Wrong connection: unknown student was found");
                            isProperConnection = false;
                            break;
                        } else {
                            for (int j = 0; j < studentNames.size(); j++) {
                                if (studentNames.get(j).equals(connection) && j <= i) {
                                    System.out.println("You can't make connections with previous or current students");
                                    isProperConnection = false;
                                    break;
                                }
                            }
                            if (!isProperConnection)
                                break;
                        }

                    }
                }

            }
        }

        students = optimization(students); //оптимизация расположения узлов

        DrawNetwork drawNetwork = new DrawNetwork(students); //рисование самих линий с помощью JPanel (Swing)
        drawNetwork.setLayout(null);

        for(Student student: students){ //рисование текстовых лэйблов в узлах
            JLabel label = new JLabel(student.name());
            drawNetwork.add(label);
            Dimension size = label.getPreferredSize();
            label.setBounds(student.x(), student.y(), size.width, size.height);
            label.setForeground(Color.WHITE);

        }

        JFrame frame = new JFrame("students network"); //создание окна, содержащего панель с линиями и лэйблами
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(drawNetwork);
        frame.pack();
        frame.setSize(1000, 1000);
        frame.setVisible(true);

    }

    public static Student[] optimization(Student[] students){ // оптимизация значений, основанная на алгоритме случайного поиска
        Random rand = new Random();
        List<Line> lines = findAllLines(students);
        CrossFeedback cross = findAllCrosses(lines, 0);
        CrossFeedback bestCross = cross;
        Student[] bestStudentCoords = students;

        for(int i = 0; i < 1000; i++){ //рассматривается выборка из 1000 различных занчений координат узлов (не считая первоначальную)
            Student [] currentStudentCoords = new Student[students.length];
            int j = 0;
            for(Student student: students){
                currentStudentCoords[j] = new Student(
                        student.name(),
                        rand.nextInt(980),
                        rand.nextInt(980),
                        student.connections()
                );
                j++;
            }
            float allFines = findAllFines(currentStudentCoords); //если узлы соответсвуют штрафуемым условиям, то получают за это доп. баллы
            lines = findAllLines(currentStudentCoords);
            cross = findAllCrosses(lines, allFines); //основные баллы формируются как суммарное количество пересечений линий друг другом

            if(cross.crossCount() < bestCross.crossCount()){
                bestCross = cross;
                bestStudentCoords = currentStudentCoords;
            }

        }
        System.out.println(bestCross.crossCount());
        return bestStudentCoords; //возвращаем студентов с лучшими координатами узлов (мин. по суммарным баллам)
    }

    public static float findAllFines(Student [] students){
        float finePoints = 0;
        for(int i = 0; i < students.length ; i++){ //Если узлы находятся ближе друг к другу, чем на 50 пикселей, то штраф соразмерно близости
            for(int j = i + 1; j < students.length; j++){
                double dist = Math.sqrt(Math.pow(students[i].x() - students[j].x(), 2) + Math.pow(students[i].y() - students[j].y(), 2));
                if (dist < 50){
                    finePoints += (1.0 - (dist / 50.0));
                }
            }
        }
        return finePoints;
    }

    public static CrossFeedback findAllCrosses(List<Line> lines, float allFines){
        float crossCount = 0;

        for(int i = 0; i < lines.size(); i++){ //перебираем все линии между студентами
            for(int j = i + 1; j < lines.size(); j++){
                Line firstLine = lines.get(i);
                Line secondLine = lines.get(j);

                int den = ((secondLine.getY2() - secondLine.getY1()) * (firstLine.getX2() - firstLine.getX1())) -
                        ((secondLine.getX2() - secondLine.getX1()) * (firstLine.getY2() - firstLine.getY1()));

                if(den != 0){ //если они не параллельны друг другу то высчитываем угол пересечения
                    float ua = (float) (((secondLine.getX2() - secondLine.getX1()) * (firstLine.getY1() - secondLine.getY1())) -
                            ((secondLine.getY2() - secondLine.getY1()) * (firstLine.getX1() - secondLine.getX1()))) / den;
                    float ub = (float) (((firstLine.getX2() - firstLine.getX1()) * (firstLine.getY1() - secondLine.getY1())) -
                            ((firstLine.getY2() - firstLine.getY1()) * (firstLine.getX1() - secondLine.getX1()))) / den;

                    if(ua > 0 && ua < 1 && ub > 0 && ub < 1){ //если угол в пределах видимой части обоих линий, то прибавляем балл за пересечение
                        crossCount++;
                        lines.get(i).setCrossCount(firstLine.getCrossCount() + 1);
                        lines.get(j).setCrossCount(secondLine.getCrossCount() + 1);
                    }
                }
            }
        }
        crossCount += allFines;
        return new CrossFeedback(crossCount, lines);
    }

    public static  List<Line> findAllLines(Student[] students){
        List<Line> allLines = new ArrayList<>();

        int currStudNum = 0;
        for(Student student: students){ //для каждого из студентов ищем тех, которые с ним связаны

            if(!Objects.equals(student.connections(), "-")) {
                String[] connections = student.connections().split(" ");
                for (int currConnStudNum = currStudNum + 1; currConnStudNum < students.length; currConnStudNum++) {
                    Student connectedStudent = students[currConnStudNum];
                    if (Arrays.asList(connections).contains(connectedStudent.name())) {
                        //Добавим узлы текущего и связанного с ним студентов. В результате формируется новая линия-связь
                        allLines.add(new Line(student.x(), student.y(), connectedStudent.x(), connectedStudent.y(), 0));
                    }

                }
            }
            currStudNum++;
        }
        return allLines; //Возвращаем все найденные линии-связи
    }
}