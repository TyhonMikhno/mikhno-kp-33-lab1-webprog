package weblab1;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class test {
    private School school;
    private FileWriterService writerMock;
    private FileReaderService readerMock;
    
    @BeforeEach
    void setUp() {
        writerMock = mock(FileWriterService.class);
        readerMock = mock(FileReaderService.class);
        school = new School(writerMock, readerMock);
    }

    @Test
    void testAddStudent() {
        Student s = new Student("Alice");
        school.addStudent(s);
        assertEquals(1, school.getStudents().size());
        assertEquals("Alice", school.getStudents().get(0).getName());
    }

    @Test
    void testAddDiscipline() {
        Discipline d = new Discipline("Math");
        school.addDiscipline(d);
        assertTrue(school.getDisciplines().contains(d));
    }

    @Test
    void testAssignGradeAndAverage() {
        Student s = new Student("Bob");
        Discipline d = new Discipline("Physics");
        s.addGrade(d, 8);
        s.addGrade(d, 10);
        assertEquals(9.0, s.calculateAverage(), 0.01);
    }

    @Test
    void testEmptyAverage() {
        Student s = new Student("NoGrades");
        assertEquals(0.0, s.calculateAverage(), 0.01);
    }

    @Test
    void testSchoolAverage() {
        Student s1 = new Student("S1");
        Student s2 = new Student("S2");
        Discipline d = new Discipline("History");
        s1.addGrade(d, 10);
        s2.addGrade(d, 6);
        school.addStudent(s1);
        school.addStudent(s2);
        assertEquals(8.0, school.calculateSchoolAverage(), 0.01);
    }

    @Test
    void testRemoveDisciplineAlsoRemovesFromStudents() {
        Discipline d = new Discipline("Biology");
        Student s = new Student("Student");
        s.addGrade(d, 7);
        school.addDiscipline(d);
        school.addStudent(s);
        assertTrue(s.getGrades().containsKey(d));
        school.getDisciplines().remove(d);
        s.getGrades().remove(d); 
        assertFalse(s.getGrades().containsKey(d));
    }

    @Test
    void testDisciplineEqualsAndHashCode() {
        Discipline d1 = new Discipline("Chemistry");
        Discipline d2 = new Discipline("Chemistry");
        assertEquals(d1, d2);
        assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    void testInvalidGradeParsingIsSkipped() {
        Student s = new Student("Parser");
        Discipline d = new Discipline("Art");
        school.addStudent(s);
        school.addDiscipline(d);
        String input = "Art: [10, , notANumber, 5]";
        String[] grades = input.replaceAll("[\\[\\]]", "").split(",\\s*");

        for (String g : grades) {
            try {
                if (!g.isBlank())
                    s.addGrade(d, Integer.parseInt(g));
            } catch (NumberFormatException ignored) {}
        }

        List<Integer> studentGrades = s.getGrades().get(d);
        assertEquals(List.of(10, 5), studentGrades);
    }

    @Test
void testAddGradeCreatesNewDisciplineEntry() {
    Student s = new Student("Tom");
    Discipline d = new Discipline("Geography");
    
    assertFalse(s.getGrades().containsKey(d));
    
    s.addGrade(d, 6);
    
    assertTrue(s.getGrades().containsKey(d));
    assertEquals(List.of(6), s.getGrades().get(d));
}
@Test
    void testExportData() throws IOException {
        Student student = new Student("Alice");
        Discipline math = new Discipline("Math");
        student.addGrade(math, 9);
        student.addGrade(math, 8);
        school.addStudent(student);
        school.addDiscipline(math);

        school.exportData("output.txt", (s1, s2) -> s1.getName().compareTo(s2.getName()));

        ArgumentCaptor<String> contentCaptor = ArgumentCaptor.forClass(String.class);
        verify(writerMock).write(eq("output.txt"), contentCaptor.capture());

        String exported = contentCaptor.getValue();
        assertTrue(exported.contains("Student: Alice"));
        assertTrue(exported.contains("Math: [9, 8]"));
    }

    @Test
    void testImportData() throws IOException {
        List<String> fileContent = List.of(
            "Student: Bob",
            "  Math: [10, 7]",
            "  Science: [8]"
        );
        when(readerMock.readLines("input.txt")).thenReturn(fileContent);

        school.importData("input.txt");

        List<Student> students = school.getStudents();
        assertEquals(1, students.size());
        Student bob = students.get(0);
        assertEquals("Bob", bob.getName());
        assertEquals(2, bob.getGrades().size());
        assertEquals(List.of(10, 7), bob.getGrades().get(new Discipline("Math")));
        assertEquals(List.of(8), bob.getGrades().get(new Discipline("Science")));
    }
}