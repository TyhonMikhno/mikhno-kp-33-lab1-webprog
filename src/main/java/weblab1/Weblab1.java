package weblab1;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Scanner;

class Discipline {
    private String name;

    public Discipline(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Discipline)) return false;
        Discipline that = (Discipline) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
class Student {
    private String name;
    private Map<Discipline, List<Integer>> grades = new HashMap<>();

    public Student(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

    public Map<Discipline, List<Integer>> getGrades() { return grades; }

    public void addGrade(Discipline discipline, int grade) {
        grades.computeIfAbsent(discipline, k -> new ArrayList<>()).add(grade);
    }

    public double calculateAverage() {
        int total = 0, count = 0;
        for (List<Integer> g : grades.values()) {
            for (int mark : g) {
                total += mark;
                count++;
            }
        }
        return count == 0 ? 0 : (double) total / count;
    }

    public void printGrades() {
        for (var entry : grades.entrySet()) {
            System.out.println("  " + entry.getKey().getName() + ": " + entry.getValue());
        }
    }
}

class FileWriterService {
    public void write(String filename, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            writer.write(content);
        }
    }
}

 class FileReaderService {
    public List<String> readLines(String filename) throws IOException {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null)
                lines.add(line);
        }
        return lines;
    }
}
 class School {
    private List<Student> students = new ArrayList<>();
    private List<Discipline> disciplines = new ArrayList<>();
    private final FileWriterService writerService;
    private final FileReaderService readerService;

    public School(FileWriterService writerService, FileReaderService readerService) {
        this.writerService = writerService;
        this.readerService = readerService;
    }

    public List<Student> getStudents() { return students; }

    public List<Discipline> getDisciplines() { return disciplines; }

    public void addStudent(Student student) { students.add(student); }

    public void addDiscipline(Discipline discipline) { disciplines.add(discipline); }

    public void exportData(String filename, Comparator<Student> comparator) throws IOException {
        List<Student> sorted = new ArrayList<>(students);
        sorted.sort(comparator);
        StringBuilder sb = new StringBuilder();

        for (Student s : sorted) {
            sb.append("Student: ").append(s.getName()).append("\n");
            for (var entry : s.getGrades().entrySet()) {
                sb.append("  ").append(entry.getKey().getName()).append(": ")
                        .append(entry.getValue()).append("\n");
            }
        }
        writerService.write(filename, sb.toString());
    }

    public void importData(String filename) throws IOException {
        List<String> lines = readerService.readLines(filename);
        Student current = null;

        for (String line : lines) {
            if (line.startsWith("Student: ")) {
                current = new Student(line.substring(9).trim());
                addStudent(current);
            } else if (line.startsWith("  ") && current != null) {
                String[] parts = line.trim().split(": ");
                Discipline d = new Discipline(parts[0].trim());
                if (!disciplines.contains(d)) addDiscipline(d);
                for (String g : parts[1].replaceAll("[\\[\\]]", "").split(",\\s*")) {
                    if (!g.isBlank())
                        current.addGrade(d, Integer.parseInt(g));
                }
            }
        }
    }

    public double calculateSchoolAverage() {
        return students.stream().mapToDouble(Student::calculateAverage).average().orElse(0);
    }

    public void printAll() {
        for (Student s : students) {
            System.out.println("Student: " + s.getName());
            s.printGrades();
        }
    }
}

public class Weblab1 {

private static final Scanner scanner = new Scanner(System.in);
    private static final School school = new School(new FileWriterService(), new FileReaderService());

    public static void main(String[] args) throws IOException {
        while (true) {
            System.out.println("\nMenu:");
            System.out.println("1. Add Student");
            System.out.println("2. Add Discipline");
            System.out.println("3. Assign Random Grade");
            System.out.println("4. Calculate Student average");
            System.out.println("5. Export Data");
            System.out.println("6. Import Data");
            System.out.println("7. Calculate School Average");
            System.out.println("8. Show All Info");
            System.out.println("9. Remove Student");
            System.out.println("10. Remove Discipline");
            System.out.println("0. Exit");
            System.out.print("Your choice: ");

            int choice = scanner.nextInt(); scanner.nextLine();
            switch (choice) {
                case 1 -> addStudent();
                case 2 -> addDiscipline();
                case 3 -> assignGrade();
                case 4 -> calculateStudentAverage();
                case 5 -> exportData();
                case 6 -> importData();
                case 7 -> System.out.println("Average: " + school.calculateSchoolAverage());
                case 8 -> school.printAll();
                case 9 -> removeStudent();
                case 10 -> removeDiscipline();
                case 0 -> { System.out.println("Goodbye!"); return; }
                default -> System.out.println("Invalid.");
            }
        }
    }

    private static void addStudent() {
        System.out.print("Enter name: ");
        school.addStudent(new Student(scanner.nextLine()));
    }

    private static void addDiscipline() {
        System.out.print("Enter discipline: ");
        school.addDiscipline(new Discipline(scanner.nextLine()));
    }

    private static void assignGrade() {
        if (school.getStudents().isEmpty() || school.getDisciplines().isEmpty()) {
            System.out.println("Add students and disciplines first.");
            return;
        }
        for (int i = 0; i < school.getStudents().size(); i++)
            System.out.println((i + 1) + ". " + school.getStudents().get(i).getName());
        System.out.print("Choose student: ");
        int studentIndex = scanner.nextInt() - 1; scanner.nextLine();

        for (int i = 0; i < school.getDisciplines().size(); i++)
            System.out.println((i + 1) + ". " + school.getDisciplines().get(i).getName());
        System.out.print("Choose discipline: ");
        int discIndex = scanner.nextInt() - 1; scanner.nextLine();

        if (studentIndex < 0 || discIndex < 0) return;

        int grade = new Random().nextInt(11);
        school.getStudents().get(studentIndex).addGrade(
            school.getDisciplines().get(discIndex), grade
        );
        System.out.println("Grade " + grade + " added.");
    }

    private static void calculateStudentAverage() {
        System.out.println("Select a student:");
        List<Student> students = school.getStudents();
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i).getName());
        }
        int studentIndex = scanner.nextInt() - 1;
        scanner.nextLine();

        if (studentIndex >= 0 && studentIndex < students.size()) {
            Student student = students.get(studentIndex);
            System.out.println("Average grade for " + student.getName() + ": " + student.calculateAverage());
        } else {
            System.out.println("Invalid selection.");
        }
    }

    private static void exportData() throws IOException {
        System.out.print("File to export to: ");
        String file = scanner.nextLine();
        school.exportData(file, Comparator.comparing(Student::getName));
        System.out.println("Exported.");
    }

    private static void importData() throws IOException {
        System.out.print("File to import from: ");
        String file = scanner.nextLine();
        school.importData(file);
        System.out.println("Imported.");
    }
     private static void removeStudent() {
        List<Student> students = school.getStudents();
        if (students.isEmpty()) {
            System.out.println("No students to remove.");
            return;
        }
        System.out.println("Select a student to remove:");
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i).getName());
        }
        int index = scanner.nextInt() - 1;
        scanner.nextLine();

        if (index >= 0 && index < students.size()) {
            Student removed = students.remove(index);
            System.out.println("Student " + removed.getName() + " removed.");
        } else {
            System.out.println("Invalid selection.");
        }
    }

    private static void removeDiscipline() {
        List<Discipline> disciplines = school.getDisciplines();
        if (disciplines.isEmpty()) {
            System.out.println("No disciplines to remove.");
            return;
        }
        System.out.println("Select a discipline to remove:");
        for (int i = 0; i < disciplines.size(); i++) {
            System.out.println((i + 1) + ". " + disciplines.get(i).getName());
        }
        int index = scanner.nextInt() - 1;
        scanner.nextLine();

        if (index >= 0 && index < disciplines.size()) {
            Discipline removed = disciplines.remove(index);
            for (Student student : school.getStudents()) {
                student.getGrades().remove(removed);
            }
            System.out.println("Discipline " + removed.getName() + " removed.");
        } else {
            System.out.println("Invalid selection.");
        }
    }
}
   

