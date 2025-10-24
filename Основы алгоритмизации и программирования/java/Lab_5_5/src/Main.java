import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);
    private static final int MAX_NUM = 1000;
    private static final int MIN_NUM = -1000;

    private static class QueueNode {
        int num;
        QueueNode next;

        QueueNode(int num) {
            this.num = num;
        }
    }

    private static QueueNode queueHead = null;
    private static QueueNode queueTail = null;

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        ADD_TO_QUEUE(1),
        DEL_FROM_QUEUE(2),
        OUTPUT_QUEUE(3),
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
            for (MenuOption option : values()) {
                if (option.code == inputCode) {
                    return option;
                }
            }
            return EXIT;
        }

        public static int getMaxOrdinal() {
            return values().length - 1;
        }
    }

    public static void main(String[] args) {
        System.out.println("Программа для работы с однонаправленной очередью.");
        showMenuAndProcess();
        scanInput.close();
    }

    private static void showMenuAndProcess() {
        boolean isQueueEmpty = true;
        boolean isRunning = true;

        while (isRunning) {
            displayMenu(isQueueEmpty);
            MenuOption choice = getValidMenuChoice(isQueueEmpty);
            switch (choice) {
                case ADD_TO_QUEUE -> {
                    addToQueue();
                    isQueueEmpty = false;
                }
                case DEL_FROM_QUEUE -> {
                    removeFromQueue();
                    isQueueEmpty = (queueHead == null);
                }
                case OUTPUT_QUEUE -> outputQueue();
                case SAVE_DATA -> saveQueue();
                case LOAD_DATA -> {
                    if (loadData()) {
                        isQueueEmpty = false;
                    }
                }
                case EXIT -> isRunning = false;
            }
        }
    }

    private static void addToQueue() {
        int num = enterNum("Введите число (" + MIN_NUM + "-" + MAX_NUM + "): ", MIN_NUM, MAX_NUM);
        QueueNode newNode = new QueueNode(num);
        if (queueTail == null) {
            queueHead = queueTail = newNode;
        } else {
            queueTail.next = newNode;
            queueTail = newNode;
        }
        System.out.println("Элемент " + num + " добавлен в очередь");
    }

    private static void removeFromQueue() {
        if (queueHead == null) {
            System.out.println("Очередь пуста");
            return;
        }
        int num = queueHead.num;
        queueHead = queueHead.next;
        if (queueHead == null) {
            queueTail = null;
        }
        System.out.println("Элемент " + num + " удален из очереди");
    }

    private static void outputQueue() {
        if (queueHead == null) {
            System.out.println("Очередь пуста");
            return;
        }
        System.out.println("Содержимое очереди (HEAD -> TAIL):");
        QueueNode current = queueHead;
        while (current != null) {
            System.out.print("[" + current.num + "]");
            if (current.next != null) {
                System.out.print(" -> ");
            }
            current = current.next;
        }
        System.out.println();
    }

    private static void saveQueue() {
        String path = inputPathToFile(FileAccessMode.WRITE);
        if (path.isEmpty()) return;

        try (FileWriter fw = new FileWriter(path)) {
            QueueNode current = queueHead;
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

        clearQueue();
        try (Scanner fileScanner = new Scanner(new FileReader(path))) {
            while (fileScanner.hasNextInt()) {
                int num = fileScanner.nextInt();
                if (num < MIN_NUM || num > MAX_NUM) {
                    System.out.println("Найдено некорректное число: " + num);
                    clearQueue();
                    return false;
                }
                QueueNode newNode = new QueueNode(num);
                if (queueTail == null) {
                    queueHead = queueTail = newNode;
                } else {
                    queueTail.next = newNode;
                    queueTail = newNode;
                }
            }
            System.out.println("Данные загружены из файла");
            return true;
        } catch (Exception e) {
            System.out.println("Ошибка чтения файла: " + e.getMessage());
            return false;
        }
    }

    private static void clearQueue() {
        queueHead = queueTail = null;
    }

    private static void displayMenu(boolean isQueueEmpty) {
        System.out.println("----------------------------------------------------");
        System.out.println("1. Добавить элемент в очередь");
        System.out.println("2. " + (isQueueEmpty ? "*" : "") + "Удалить элемент из очереди");
        System.out.println("3. " + (isQueueEmpty ? "*" : "") + "Отобразить очередь");
        System.out.println("4. " + (isQueueEmpty ? "*" : "") + "Сохранить очередь");
        System.out.println("5. Загрузить данные");
        System.out.println("0. Выход");
        System.out.println("----------------------------------------------------");
    }

    private static MenuOption getValidMenuChoice(boolean isQueueEmpty) {
        int max = MenuOption.getMaxOrdinal();
        MenuOption choice;
        boolean valid;
        do {
            choice = MenuOption.fromCode(enterNum("Выберите пункт: ", 0, max));
            valid = switch (choice) {
                case DEL_FROM_QUEUE, OUTPUT_QUEUE, SAVE_DATA -> !isQueueEmpty;
                default -> true;
            };
            if (!valid) System.out.println("Пункт недоступен");
        } while (!valid);
        return choice;
    }

    private static int enterNum(String msg, int min, int max) {
        while (true) {
            System.out.print(msg);
            try {
                int num = Integer.parseInt(scanInput.nextLine());
                if (num >= min && num <= max) return num;
                System.out.println("Число должно быть от " + min + " до " + max);
            } catch (NumberFormatException e) {
                System.out.println("Некорректный ввод!");
            }
        }
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