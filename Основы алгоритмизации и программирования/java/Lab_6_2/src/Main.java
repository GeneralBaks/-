import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);
    private static final int MIN_RANK = 3;
    private static final int MAX_RANK = 19;

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        GENERATE_MAGICK_SQUARE(1),
        SAVE_DATA(2),
        LOAD_DATA(3);

        final int code;

        MenuOption(int code) {
            this.code = code;
        }

        public int getCode() {
            return code;
        }

        public static MenuOption fromCode(int inputCode) {
            MenuOption[] options;
            MenuOption result;
            int len;
            int i;

            options = MenuOption.values();
            result = MenuOption.EXIT;
            len = options.length;
            i = 0;
            while (i < len) {
                if (options[i].getCode() != inputCode) {
                    i++;
                } else {
                    result = options[i];
                    i = len;
                }
            }
            return result;
        }

        public static int getMaxOrdinal() {
            return values().length - 1;
        }
    }

    public static void main(String[] args) {
        System.out.println("Данная программа генерирует магический квадрат методом террасы.");
        showMenuAndProcess();
        scanInput.close();
    }

    private static String colorText(String str, boolean isOptionReady) {
        final String RED = "\u001B[31m";
        final String RESET = "\u001B[0m";
        String newStr = str;

        StringBuilder builder = new StringBuilder();
        if (!isOptionReady)
            newStr = builder.append(RED).append(str).append(RESET).toString();

        return newStr;
    }

    private static void displayMenu(boolean isSquareExists) {
        System.out.println("=================================================================");
        System.out.println("|1. Сгенерировать магический квадрат   |");
        System.out.println("|2. " + colorText("Сохранить данные", isSquareExists) + "                   |");
        System.out.println("|3. Загрузить данные                   |");
        System.out.println("|0. Выход                              |");
        System.out.println("|--------------------------------------|");
    }

    private static void showMenuAndProcess() {
        boolean isSquareExists = false;
        boolean isRunning;
        MenuOption choice;

        int[][] square = new int[0][0];
        int rank = 0;

        isRunning = true;
        do {
            isSquareExists = square.length != 0;
            displayMenu(isSquareExists);
            choice = getValidMenuChoice(isSquareExists);

            switch (choice) {
                case GENERATE_MAGICK_SQUARE -> {
                    rank = enterNum("Введите нечётный ранг матрицы: ", MIN_RANK, MAX_RANK);
                    if (rank % 2 == 0)
                        System.out.println("Ошибка! Ранг должен быть нечётным.");
                    else {
                        square = generateMagickSquare(rank);
                        displaySquare(square);
                    }
                }
                case SAVE_DATA -> saveSquareToFile(rank,square);
                case LOAD_DATA -> {
                    rank = loadSquareFromFile();
                    if (rank > 0) {
                        square = generateMagickSquare(rank);
                        displaySquare(square);
                    }
                }
                case EXIT -> isRunning = false;
            }
        } while (isRunning);
    }

    private static MenuOption getValidMenuChoice(boolean isSquareExists) {
        final int MAX_NUM = MenuOption.getMaxOrdinal();
        boolean isAvailable;
        MenuOption choice;

        do {
            choice = MenuOption.fromCode(enterNum("Выберите опцию: ", 0, MAX_NUM));
            isAvailable = switch (choice) {
                case SAVE_DATA -> isSquareExists;
                default -> true;
            };
            if (!isAvailable)
                System.out.println("Эта опция недоступна.");
        } while (!isAvailable);

        return choice;
    }

    private static int[][] generateMagickSquare(int rank) {
        int[][] result = new int[rank][rank];
        int[][] temp = buildTerrace(rank);
        shiftTerraceEdges(temp, rank);
        extractCentralSquare(result, temp, rank);
        return result;
    }

    private static int[][] buildTerrace(int rank) {
        int size = 2 * rank - 1;
        int[][] temp = new int[size][size];
        int i = size / 2;
        int j = 0;

        for (int num = 1; num <= rank * rank; num++) {
            temp[i][j] = num;

            if (num % rank == 0) {
                i = size/2 + (num / rank);
                j = num / rank;
            } else {
                i--;
                j++;
            }
        }

        return temp;
    }

    private static void shiftTerraceEdges(int[][] tempMtrx, int rank) {
        int size = tempMtrx.length;
        int delta = rank / 2;

        for (int col = 0; col < size; col++) {
            for (int row = 0; row < delta; row++) {
                if (tempMtrx[row][col] != 0) {
                    tempMtrx[row + rank][col] = tempMtrx[row][col];
                    tempMtrx[row][col] = 0;
                }

                int bottomRow = size - 1 - row;
                if (tempMtrx[bottomRow][col] != 0) {
                    tempMtrx[bottomRow - rank][col] = tempMtrx[bottomRow][col];
                    tempMtrx[bottomRow][col] = 0;
                }
            }
        }

        for (int row = 0; row < size; row++) {
            for (int col = 0; col < delta; col++) {
                if (tempMtrx[row][col] != 0) {
                    tempMtrx[row][col + rank] = tempMtrx[row][col];
                    tempMtrx[row][col] = 0;
                }

                int rightCol = size - 1 - col;
                if (tempMtrx[row][rightCol] != 0) {
                    tempMtrx[row][rightCol - rank] = tempMtrx[row][rightCol];
                    tempMtrx[row][rightCol] = 0;
                }
            }
        }
    }

    private static void extractCentralSquare(int[][] target, int[][] temp, int rank) {
        int offset = rank / 2;
        for (int row = 0; row < rank; row++) {
            for (int col = 0; col < rank; col++) {
                target[row][col] = temp[row + offset][col + offset];
            }
        }
    }

    private static String formatSquareToString(int[][] square) {
        StringBuilder sb = new StringBuilder();
        int rank = square.length;
        int maxLen = String.valueOf(rank * rank).length();

        for (int[] row : square) {
            for (int num : row) {
                sb.append(String.format("%" + (maxLen + 1) + "d", num));
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private static void displaySquare( int[][] square) {
        System.out.println(formatSquareToString(square));
    }

    private static void saveSquareToFile(int rank, int[][] square) {
        String filePath = inputPathToFile(FileAccessMode.WRITE);
        if (filePath.isEmpty()) return;

        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(rank + "\n");
            writer.write(formatSquareToString(square));
            System.out.println("Данные успешно сохранены в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static int loadSquareFromFile() {
        String filePath = inputPathToFile(FileAccessMode.READ);
        if (filePath.isEmpty()){
            System.out.println("Ошибка: файл пуст.");
            return -1;
        }

        try (Scanner scanner = new Scanner(new File(filePath))) {
            if (!scanner.hasNextInt()) {
                System.out.println("Ошибка: файл не содержит данных о ранге матрицы.");
                return -1;
            }

            int rank = scanner.nextInt();
            if (rank < MIN_RANK || rank > MAX_RANK) {
                System.out.printf("Ошибка: ранг должен быть в диапазоне [%d, %d].\n", MIN_RANK, MAX_RANK);
                return -1;
            }
            if (rank % 2 == 0) {
                System.out.println("Ошибка: ранг должен быть нечётным.");
                return -1;
            }

            System.out.println("Ранг успешно загружен. Магический квадрат будет сгенерирован.");
            return rank;
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла.");
            return -1;
        }
    }

    private static int enterNum(final String PROMPT, final int MIN, final int MAX) {
        boolean isIncorrect;
        int userNumber;

        userNumber = 0;

        do {
            isIncorrect = false;
            System.out.print(PROMPT);
            try {
                userNumber = Integer.parseInt(scanInput.nextLine());
            } catch (NumberFormatException e) {
                isIncorrect = true;
                System.out.println("Ошибка. Неверный ввод. Пожалуйста, введите число.");
            }
            if (!isIncorrect && (userNumber < MIN || userNumber > MAX)) {
                isIncorrect = true;
                System.out.println("Ошибка. Введенное значение вне допустимого диапазона [" + MIN + ", " + MAX + "].");
            }
        } while (isIncorrect);

        return userNumber;
    }

    private static String inputPathToFile(FileAccessMode mode) {
        String userPath;

        System.out.print("Введите путь к файлу: ");
        userPath = scanInput.nextLine();

        if (!isValidFile(userPath, mode))
            userPath = "";

        return userPath;
    }

    private static boolean isValidFile(String path, FileAccessMode mode) {
        boolean isValid;
        File file = new File(path);
        isValid = checkExistentOfFile(file)
                && checkFileExtension(path)
                && checkAccessToFile(file, mode);
        return isValid;
    }

    private static boolean checkExistentOfFile(final File USER_FILE) {
        boolean fileExist;

        fileExist = USER_FILE.exists();
        if (!fileExist)
            System.out.println("Ошибка. Файл по указанному пути не существует.");

        return fileExist;
    }

    private static boolean checkFileExtension(final String USER_PATH) {
        final String FILE_EXTENSION = ".txt";
        boolean isExtValid;

        isExtValid = (USER_PATH.length() >= FILE_EXTENSION.length()
                && FILE_EXTENSION.equals(USER_PATH.substring(USER_PATH.length() - FILE_EXTENSION.length())));
        if (!isExtValid)
            System.out.println("Ошибка. Файл должен иметь расширение `" + FILE_EXTENSION + "`.");

        return isExtValid;
    }

    private static boolean checkAccessToFile(final File file, FileAccessMode mode) {
        boolean isAccessible;
        isAccessible = true;
        switch (mode) {
            case READ -> isAccessible = checkIsFileReadable(file);
            case WRITE -> isAccessible = checkIsFileWritable(file);
        }
        return isAccessible;
    }

    private static boolean checkIsFileReadable(File file) {
        boolean isReadable = true;
        try (FileReader checkRead = new FileReader(file)) {
            checkRead.read();
        } catch (IOException e) {
            isReadable = false;
            System.out.println("Ошибка. Невозможно прочитать файл.");
        }
        return isReadable;
    }

    private static boolean checkIsFileWritable(File file) {
        boolean isWritable = true;
        try (FileWriter checkWrite = new FileWriter(file)) {
            checkWrite.write("");
        } catch (IOException e) {
            isWritable = false;
            System.out.println("Ошибка. Невозможно записать в файл.");
        }
        return isWritable;
    }
}