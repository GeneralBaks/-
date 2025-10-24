import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);
    private static final int MAX_NUM = 1000;

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        ENTER_N(1),
        ENTER_M(2),
        FIND_SEQ(3),
        SAVE_DATA(4),
        LOAD_DATA(5);

        final int code;

        MenuOption(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static MenuOption fromCode(int inputCode) {
            MenuOption[] options = MenuOption.values();
            MenuOption result = MenuOption.EXIT;
            int i = 0;
            while (i < options.length) {
                if (options[i].getCode() == inputCode) {
                    result = options[i];
                    i = options.length;
                } else
                    i++;
            }
            return result;
        }

        public static int getMaxOrdinal() {
            return values().length - 1;
        }
    }

    private static class Node {
        int num;
        Node next;
        Node(int num) {
            this.num = num;
        }
    }

    public static void main(String[] args) {
        System.out.println("Поиск кратчайшего пути коня на шахматной доске с препятствиями");
        showMenuAndProcess();
        scanInput.close();
    }

    private static void showMenuAndProcess() {
        boolean isNEntered = false;
        boolean isMEntered = false;
        boolean isSegFound = false;
        boolean isRunning = true;
        int n = 0, m = 0;
        String result = "";

        while (isRunning) {
            displayMenu(isNEntered, isMEntered, isSegFound);
            MenuOption choice = getValidMenuChoice(isNEntered, isMEntered, isSegFound);
            switch (choice) {
                case ENTER_N -> {
                    n = enterNum("Введите количество участников: ", 2, MAX_NUM);
                    isNEntered = true;
                    isSegFound = false;
                }
                case ENTER_M -> {
                    m = enterNum("Введите порядок выбывания: ", 1, n);
                    isMEntered = true;
                    isSegFound = false;
                }
                case FIND_SEQ -> {
                    Node head = createCircularList(n);
                    result = solveJosephus(n, m, head);
                    System.out.println(result);
                    isSegFound = true;
                }
                case SAVE_DATA -> {
                    saveResult(result);
                }
                case LOAD_DATA -> {
                    int[] loaded = loadData();
                    if (loaded != null) {
                        n = loaded[0];
                        m = loaded[1];
                        isNEntered = true;
                        isMEntered = true;
                        System.out.println("Загружены значения: N = " + n + ", M = " + m);
                    }
                }
                case EXIT -> isRunning = false;
            }
        }
    }

    private static String hasAccess(boolean isAccessible) {
        return isAccessible ? "" : "*";
    }

    private static void displayMenu(boolean isNEntered, boolean isMEntered, boolean IsSegFound) {
        System.out.println("----------------------------------------------------");
        System.out.println("1. Ввести количество участников(N)");
        System.out.println("2. " + hasAccess(isNEntered) + "Ввести порядок выбывания(M)");
        System.out.println("3. " + hasAccess(isNEntered && isMEntered) + "Вывести порядок выбывания");
        System.out.println("4. " + hasAccess(IsSegFound) + "Сохранить порядок");
        System.out.println("5. Загрузить данные");
        System.out.println("0. Выход");
        System.out.println("----------------------------------------------------");
    }

    private static MenuOption getValidMenuChoice(boolean isNEntered, boolean isMEntered, boolean IsSegFound) {
        int max = MenuOption.getMaxOrdinal();
        MenuOption choice;
        boolean valid;
        do {
            choice = MenuOption.fromCode(enterNum("Выберите пункт: ", 0, max));
            valid = switch (choice) {
                case ENTER_M -> isNEntered;
                case FIND_SEQ -> isNEntered && isMEntered;
                case SAVE_DATA -> IsSegFound;
                default -> true;
            };
            if (!valid) System.out.println("Пункт недоступен");
        } while (!valid);
        return choice;
    }

    private static Node createCircularList(int n) {
        Node head = new Node(1);
        Node current = head;

        for (int i = 2; i <= n; i++) {
            current.next = new Node(i);
            current = current.next;
        }
        current.next = head;
        return head;
    }

    private static String solveJosephus(int n, int m, Node head) {
        StringBuilder result = new StringBuilder();
        result.append("Порядок выбывания: ");

        int remaining = n;
        Node first = head;

        while (remaining > 1) {
            Node prev = first;
            Node current = first;
            for (int i = 1; i < m; i++) {
                prev = current;
                current = current.next;
            }

            result.append(current.num).append(" ");

            if (current == first)
                first = current.next;

            prev.next = current.next;

            remaining--;
        }

        result.append("\nПоследний оставшийся: ").append(first.num);
        return result.toString();
    }


    private static void saveResult(String result) {
        String path = inputPathToFile(FileAccessMode.WRITE);
        if (!path.isEmpty()) {
            try (FileWriter fw = new FileWriter(path)) {
                fw.write(result);
                System.out.println("Порядок сохранён");
            } catch (IOException e) {
                System.out.println("Ошибка записи файла");
            }
        }
    }

    private static int[] loadData() {
        String path = inputPathToFile(FileAccessMode.READ);
        if (path.isEmpty()) return null;

        try (Scanner sc = new Scanner(new FileReader(path))) {
            int n = sc.nextInt();
            if (n < 2 || n > MAX_NUM) {
                System.out.println("Некорректное значение N в файле");
                return null;
            }

            int m = sc.nextInt();
            if (m < 1 || m > n) {
                System.out.println("Некорректное значение M в файле");
                return null;
            }

            if (sc.hasNextLine()) {
                System.out.println("Лишние данные в файле.");
                return null;
            }

            return new int[]{n, m};
        } catch (Exception e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
            return null;
        }
    }

    private static int enterNum(String msg, int min, int max) {
        int num = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print(msg);
            try {
                num = Integer.parseInt(scanInput.nextLine());
                if (num >= min && num <= max) valid = true;
                else System.out.println("Введите число от " + min + " до " + max);
            } catch (NumberFormatException e) {
                System.out.println("Некорректное число");
            }
        }
        return num;
    }

    private static String inputPathToFile(FileAccessMode mode) {
        System.out.print("Введите путь к файлу: ");
        String userPath = scanInput.nextLine();

        if (!isValidFile(userPath, mode))
            userPath = "";

        return userPath;
    }

    private static boolean isValidFile(String path, FileAccessMode mode) {
        File file = new File(path);
        return checkFileExists(file)
                && checkFileExtension(path)
                && checkFileAccess(file, mode);
    }

    private static boolean checkFileExists(File file) {
        if (!file.exists()) {
            System.out.println("Ошибка: файл не существует");
            return false;
        }
        return true;
    }

    private static boolean checkFileExtension(String path) {
        if (!path.endsWith(".txt")) {
            System.out.println("Ошибка: требуется файл с расширением .txt");
            return false;
        }
        return true;
    }

    private static boolean checkFileAccess(File file, FileAccessMode mode) {
        try {
            if (mode == FileAccessMode.READ) {
                new FileReader(file).close();
            } else {
                new FileWriter(file, true).close();
            }
            return true;
        } catch (IOException e) {
            System.out.println("Ошибка доступа к файлу");
            return false;
        }
    }
}