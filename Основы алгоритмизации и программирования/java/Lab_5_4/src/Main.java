import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);
    private static final int MAX_NUM = 1000;
    private static final int MIN_NUM = -1000;

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        ADD_TO_STACK(1),
        CLEAR_STACK(2),
        OUTPUT_STACK(3),
        TRANSFORM_TO_DEQUE(4),
        OUTPUT_DEQUE(5),
        SAVE_DATA(6),
        LOAD_DATA(7);

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

    private static class StackNode {
        int num;
        StackNode next;

        StackNode(int num) {
            this.num = num;
        }
    }

    private static class DequeNode {
        int num;
        DequeNode next;
        DequeNode prev;

        DequeNode(int num) {
            this.num = num;
        }
    }

    private static StackNode stackTop = null;
    private static DequeNode dequeHead = null;
    private static DequeNode dequeTail = null;

    public static void main(String[] args) {
        System.out.println("Программа преобразования стека в двунаправленную очередь");
        showMenuAndProcess();
        scanInput.close();
    }

    private static void showMenuAndProcess() {
        boolean isStackEmpty = true;
        boolean isDequeCreated = false;
        boolean isRunning = true;

        while (isRunning) {
            displayMenu(isStackEmpty, isDequeCreated);
            MenuOption choice = getValidMenuChoice(isStackEmpty, isDequeCreated);
            switch (choice) {
                case ADD_TO_STACK -> {
                    pushToStack();
                    isStackEmpty = false;
                    isDequeCreated = false;
                }
                case CLEAR_STACK -> {
                    clearStack();
                    isStackEmpty = true;
                    isDequeCreated = false;
                }
                case OUTPUT_STACK -> outputStack();
                case TRANSFORM_TO_DEQUE -> {
                    transformToDeque();
                    isDequeCreated = true;
                }
                case OUTPUT_DEQUE -> outputDeque();
                case SAVE_DATA -> saveDeque();
                case LOAD_DATA -> {
                    if (loadData()) {
                        isStackEmpty = false;
                        isDequeCreated = true;
                    }
                }
                case EXIT -> isRunning = false;
            }
        }
    }

    private static void pushToStack() {
        int num = enterNum("Введите число ("+ MIN_NUM +"-" + MAX_NUM + "): ", MIN_NUM, MAX_NUM);
        StackNode newNode = new StackNode(num);
        newNode.next = stackTop;
        stackTop = newNode;
        System.out.println("Элемент " + num + " добавлен в стек");
    }

    private static void clearStack() {
        while (stackTop != null) {
            StackNode temp = stackTop;
            stackTop = stackTop.next;
            temp.next = null;
        }
        System.out.println("Стек очищен");
    }

    private static void outputStack() {
        if (stackTop == null) {
            System.out.println("Стек пуст");
            return;
        }

        System.out.println("Содержимое стека (вершина сверху):");
        System.out.println("┌───────┐");
        StackNode current = stackTop;
        while (current != null) {
            System.out.printf("│ %-5d │%s\n", current.num, current == stackTop ? " ← вершина" : "");
            System.out.println("├───────┤");
            current = current.next;
        }
        System.out.println("└───────┘");
    }

    private static void transformToDeque() {
        clearDeque();

        StackNode current = stackTop;
        while (current != null) {
            addToDeque(current.num);
            current = current.next;
        }

        System.out.println("Стек преобразован в двунаправленную очередь");
    }

    private static void addToDeque(int num) {
        DequeNode newNode = new DequeNode(num);
        if (dequeHead == null) {
            dequeHead = dequeTail = newNode;
        } else {
            newNode.next = dequeHead;
            dequeHead.prev = newNode;
            dequeHead = newNode;
        }
    }

    private static void clearDeque() {
        dequeHead = dequeTail = null;
    }

    private static void outputDeque() {
        if (dequeHead == null) {
            System.out.println("Очередь пуста");
            return;
        }

        System.out.println("Двунаправленная очередь (слева направо):");
        DequeNode current = dequeHead;
        System.out.print("HEAD ↔ ");
        while (current != null) {
            System.out.print("[" + current.num + "]");
            if (current.next != null) {
                System.out.print(" ↔ ");
            }
            current = current.next;
        }
        System.out.println(" ↔ TAIL");

        System.out.println("Двунаправленная очередь (справа налево):");
        current = dequeTail;
        System.out.print("TAIL ↔ ");
        while (current != null) {
            System.out.print("[" + current.num + "]");
            if (current.prev != null) {
                System.out.print(" ↔ ");
            }
            current = current.prev;
        }
        System.out.println(" ↔ HEAD");
    }

    private static void saveDeque() {
        String path = inputPathToFile(FileAccessMode.WRITE);
        if (path.isEmpty()) return;

        try (FileWriter fw = new FileWriter(path)) {
            DequeNode current = dequeHead;
            while (current != null) {
                fw.write(current.num + " ");
                current = current.next;
            }
            System.out.println("Очередь сохранена в файл");
        } catch (IOException e) {
            System.out.println("Ошибка записи файла");
        }
    }

    private static boolean loadData() {
        String path = inputPathToFile(FileAccessMode.READ);
        if (path.isEmpty()) return false;

        clearStack();
        clearDeque();
        try (Scanner sc = new Scanner(new FileReader(path))) {
            while (sc.hasNextInt()) {
                int num = sc.nextInt();
                if (num < MIN_NUM || num > MAX_NUM) {
                    System.out.println("Найдено некорректное число: " + num);
                    clearStack();
                    clearDeque();
                    return false;
                }

                StackNode newNode = new StackNode(num);
                newNode.next = stackTop;
                stackTop = newNode;
            }

            transformToDeque();
            System.out.println("Данные загружены из файла");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
            return false;
        }
    }

    private static String hasAccess(boolean isAccessible) {
        return isAccessible ? "" : "*";
    }

    private static void displayMenu(boolean isStackEmpty, boolean isDequeCreated) {
        System.out.println("----------------------------------------------------");
        System.out.println("1. Добавить элемент к стеку");
        System.out.println("2. " + hasAccess(!isStackEmpty) + "Очистить стек");
        System.out.println("3. " + hasAccess(!isStackEmpty) + "Отобразить стек");
        System.out.println("4. " + hasAccess(!isStackEmpty) + "Преобразовать в очередь");
        System.out.println("5. " + hasAccess(isDequeCreated) + "Отобразить очередь");
        System.out.println("6. " + hasAccess(isDequeCreated) + "Сохранить порядок");
        System.out.println("7. Загрузить данные");
        System.out.println("0. Выход");
        System.out.println("----------------------------------------------------");
    }

    private static MenuOption getValidMenuChoice(boolean isStackEmpty, boolean isDequeCreated) {
        int max = MenuOption.getMaxOrdinal();
        MenuOption choice;
        boolean valid;
        do {
            choice = MenuOption.fromCode(enterNum("Выберите пункт: ", 0, max));
            valid = switch (choice) {
                case CLEAR_STACK, OUTPUT_STACK, TRANSFORM_TO_DEQUE -> !isStackEmpty;
                case OUTPUT_DEQUE, SAVE_DATA -> isDequeCreated;
                default -> true;
            };
            if (!valid) System.out.println("Пункт недоступен");
        } while (!valid);
        return choice;
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