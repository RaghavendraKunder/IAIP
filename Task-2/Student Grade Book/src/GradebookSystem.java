import java.util.*;

class Student {
    int id;
    String name;
    HashMap<String, Integer> subjects;

    Student(int id, String name) {
        this.id = id;
        this.name = name;
        subjects = new HashMap<>();
    }

    void addSubject(String subject, int marks) {
        subjects.put(subject, marks);
    }

    double calculateAverage() {
        if (subjects.size() == 0) return 0;
        int sum = 0;
        for (int marks : subjects.values()) {
            sum += marks;
        }
        return (double) sum / subjects.size();
    }

    void displayReport() {
        System.out.println("\nID: " + id);
        System.out.println("Name: " + name);
        System.out.println("Subjects & Marks:");
        for (Map.Entry<String, Integer> entry : subjects.entrySet()) {
            System.out.println(entry.getKey() + ": " + entry.getValue());
        }
        System.out.println("Average: " + calculateAverage());
    }
}

public class GradebookSystem {

    static Scanner sc = new Scanner(System.in);
    static ArrayList<Student> students = new ArrayList<>();

    public static void main(String[] args) {

        int choice;

        do {
            System.out.println("\n===== STUDENT GRADEBOOK =====");
            System.out.println("1. Add Student");
            System.out.println("2. Add/Edit Grades");
            System.out.println("3. Display Student Report");
            System.out.println("4. Delete Student");
            System.out.println("5. Display All Students");
            System.out.println("6. Exit");

            System.out.print("Enter choice: ");
            choice = sc.nextInt();

            switch (choice) {
                case 1:
                    addStudent();
                    break;
                case 2:
                    addGrades();
                    break;
                case 3:
                    displayStudent();
                    break;
                case 4:
                    deleteStudent();
                    break;
                case 5:
                    displayAll();
                    break;
                case 6:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid choice!");
            }

        } while (choice != 6);
    }

    static void addStudent() {
        System.out.print("Enter ID: ");
        int id = sc.nextInt();
        sc.nextLine();

        System.out.print("Enter Name: ");
        String name = sc.nextLine();

        students.add(new Student(id, name));
        System.out.println("Student added!");
    }

    static Student findStudent(int id) {
        for (Student s : students) {
            if (s.id == id) return s;
        }
        return null;
    }

    static void addGrades() {
        System.out.print("Enter Student ID: ");
        int id = sc.nextInt();

        Student s = findStudent(id);

        if (s == null) {
            System.out.println("Student not found!");
            return;
        }

        System.out.print("Enter number of subjects: ");
        int n = sc.nextInt();
        sc.nextLine();

        for (int i = 0; i < n; i++) {
            System.out.print("Subject name: ");
            String subject = sc.nextLine();

            System.out.print("Marks: ");
            int marks = sc.nextInt();
            sc.nextLine();

            s.addSubject(subject, marks);
        }

        System.out.println("Grades updated!");
    }

    static void displayStudent() {
        System.out.print("Enter Student ID: ");
        int id = sc.nextInt();

        Student s = findStudent(id);

        if (s != null) {
            s.displayReport();
        } else {
            System.out.println("Student not found!");
        }
    }

    static void deleteStudent() {
        System.out.print("Enter Student ID: ");
        int id = sc.nextInt();

        Student s = findStudent(id);

        if (s != null) {
            students.remove(s);
            System.out.println("Student deleted!");
        } else {
            System.out.println("Student not found!");
        }
    }

    static void displayAll() {
        if (students.isEmpty()) {
            System.out.println("No students available!");
            return;
        }

        for (Student s : students) {
            s.displayReport();
        }
    }
}