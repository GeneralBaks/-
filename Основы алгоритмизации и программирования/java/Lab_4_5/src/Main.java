import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);

    final static int MIN_RANK = 6;
    final static int MAX_RANK = 30;

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        ENTER_RANK(1),
        GENERATE_MAGICK_SQUARE(2),
        DISPLAY_MAGICK_SQUARE(3),
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
            MenuOption[] options;
            MenuOption result;
            int len;
            int i;

            options = MenuOption.values();
            result= MenuOption.EXIT;
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
        System.out.println("This program generates a magic square of even-odd degree.");
        showMenuAndProcess();
        scanInput.close();
    }

    private static String colorText(String str, boolean isOptionReady) {
        final String RED = "\u001B[31m";
        final String RESET = "\u001B[0m";
        String newStr;
        newStr = str;

        StringBuilder builder = new StringBuilder();
        if (!isOptionReady)
            newStr = builder.append(RED).append(str).append(RESET).toString();

        return newStr;
    }

    private static void displayMenu(boolean isRankEntered, boolean isSquareExists) {
        System.out.println("=================================================================");
        System.out.println("|1. Enter rank               |");
        System.out.println("|2. " + colorText("Generate magick square", isRankEntered) + "   |");
        System.out.println("|3. " + colorText("Display magick square", isSquareExists) + "    |");
        System.out.println("|4. " + colorText("Save data", isSquareExists) + "                |");
        System.out.println("|5. Load data                |");
        System.out.println("|0. Exit                     |");
        System.out.println("|----------------------------|");
    }

    private static void showMenuAndProcess() {
        boolean isRankEntered = false;
        boolean isSquareExists = false;
        boolean isRunning;
        MenuOption choice;

        int rank = 0;
        int[][] magickSquare = new int[0][0];
        int dataFromFile = 0;

        isRunning = true;
        do {
            isRankEntered = rank != 0;
            isSquareExists = magickSquare.length != 0;
            displayMenu(isRankEntered,isSquareExists);
            choice = getValidMenuChoice(isRankEntered, isSquareExists);

            switch (choice) {
                case ENTER_RANK -> {
                    rank = getRank();
                    magickSquare = new int[0][0];
                }
                case GENERATE_MAGICK_SQUARE -> magickSquare = magicSquareOfEvenOddOrder(rank);
                case  DISPLAY_MAGICK_SQUARE -> displayMagickSquare(magickSquare);
                case SAVE_DATA -> saveToFile(magickSquare);
                case LOAD_DATA -> {
                    dataFromFile = loadFromFile();
                    if (dataFromFile != 0)
                        rank = dataFromFile;
                }
                case EXIT -> isRunning = false;
            }
        } while (isRunning);
    }

    private static void displayMagickSquare(int[][] magickSquare) {
        int size = magickSquare.length;
        int maxNum = size * size;
        int numWidth = String.valueOf(maxNum).length() + 1;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                System.out.printf("%" + numWidth + "d", magickSquare[i][j]);
            }
            System.out.println();
        }
    }

    private static int getRank() {
        int rank = 0;
        boolean isUncorrect;
        do {
            isUncorrect = false;
            rank = enterNum("Enter rank of magick square", MIN_RANK, MAX_RANK);
            if (rank % 4 != 2) {
                isUncorrect = true;
                System.out.println("Rank must be even but not singly even (6, 10, 14, etc.)");
            }
        } while (isUncorrect);
        return rank;
    }

    private static MenuOption getValidMenuChoice(boolean isRankEntered, boolean isSquareExists) {
        final int MAX_NUM = MenuOption.getMaxOrdinal();
        boolean isAvailable;
        MenuOption choice;

        do {
            choice = MenuOption.fromCode(enterNum("Select option: ", 0, MAX_NUM));
            isAvailable = switch (choice) {
                case GENERATE_MAGICK_SQUARE -> isRankEntered;
                case SAVE_DATA,DISPLAY_MAGICK_SQUARE -> isSquareExists;
                default -> true;
            };
            if (!isAvailable)
                System.out.println("You can't use this option.");
        } while (!isAvailable);

        return choice;
    }

    private static int[][] magicSquareOfOddOrder(int n) {
        int[][] matrix = new int[n][n];
        int count = 1;
        int y = 0;
        int x = n / 2;

        for (int i = 0; i < n; i++)
            Arrays.fill(matrix[i], 0);

        while (count <= n * n) {
            matrix[y][x] = count;
            count++;

            if ((y == 0 && x >= n - 1) && (matrix[n - 1][0] != 0))
                y++;
            else {
                y--;
                if (y < 0)
                    y = n - 1;
                x++;
                if (x == n)
                    x = 0;
                if (matrix[y][x] != 0) {
                    y += 2;
                    x--;
                }
            }
        }

        return matrix;
    }

    private static int[][] magicSquareOfEvenOddOrder(int n) {
        int half = n/2;
        int key;
        int move;
        int x;
        int y;

        int[][] matrix = new int[n][n];
        int[][] tempMatrix;
        tempMatrix = magicSquareOfOddOrder(half);


        for (int i = 0; i < half; i++)
            for (int j = 0; j < half; j++)
                matrix[i][j] = tempMatrix[i][j];

        for (int i = 0; i < half; i++)
            for (int j = half; j < n; j++) {
                x = j-half;
                matrix[i][j] = (tempMatrix[i][x]+2*half*half);
            }

        for (int i = half; i < n; i++)
            for (int j = 0; j < half; j++) {
                x = i-half;
                matrix[i][j] = (tempMatrix[x][j]+3*half*half);
            }

        for (int i = half; i < n; i++)
            for (int j = half; j < n; j++) {
                x = i-half;
                y = j-half;
                matrix[i][j] = (tempMatrix[x][y]+half*half);
            }

        move = 0;

        for (int i = MIN_RANK; i < n; i++)
            if((i%4!=0)&&(i%2==0))
                move++;

        for (int j = matrix.length/2-move; j <= matrix.length/2+move-1; j++)
            for (int i = 0; i < tempMatrix.length; i++) {
                key = matrix[i][j];
                matrix[i][j] = matrix[half+i][j];
                matrix[half+i][j] = key;
            }

        for (int j = 0; j <= 1; j++) {
            if (j == 0) {
                key = matrix[0][0];
                matrix[0][0] = matrix[half][0];
                matrix[half][0] = key;
            }
            if (j == 1) {
                key = matrix[half - 1][0];
                matrix[half - 1][0] = matrix[n - 1][0];
                matrix[n - 1][0] = key;
            }
        }

        for (int j = half+1; j < n-1; j++) {
            for (int i = 1; i < half-1; i++) {
                key = matrix[i][1];
                matrix[i][1] = matrix[half+i][1];
                matrix[half+i][1] = key;
            }
        }
        return matrix;
    }

    private static void saveToFile(int[][] mtrx) {
        String filePath;

        filePath = inputPathToFile(FileAccessMode.WRITE);
        if (!filePath.isEmpty())
            writeDataInFile(mtrx, filePath);
    }

    private static void writeDataInFile(int[][] mtrx, String filePath) {
        int len = mtrx.length;
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(len + "\n");

            for (int i = 0; i < len; i++) {
                for (int j = 0; j < len; j++)
                    writer.write(mtrx[i][j] + " ");
                writer.write("\n");
            }

            System.out.println("The matrix was successfully saved.");
        } catch (IOException e) {
            System.out.println("An unexpected error occurred while writing the file.");
        }
    }

    private static int loadFromFile() {
        String filePath;
        int rank = 0;

        filePath = inputPathToFile(FileAccessMode.READ);
        if (!filePath.isEmpty())
            rank = readRankFromFile(filePath);
        return rank;
    }

    private static int readRankFromFile(String filePath) {
        int rank = 0;
        boolean isValid = true;
        try (Scanner fileScan = new Scanner(new FileReader(filePath))) {
            if (fileScan.hasNextInt()) {
                rank = fileScan.nextInt();
            } else {
                isValid = false;
                System.out.println("Error: Invalid file format — missing rank.");
            }
        } catch (IOException e) {
            System.out.println("Error: Failed to read the file.");
        }

        if (isValid && (rank % 4 != 2 || rank < MIN_RANK || rank > MAX_RANK)) {
            System.out.println("Error: Rank must be even and not singly even (6, 10, 14, ...).");
            rank = 0;
        }

        return rank;
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
                System.out.println("Error. Invalid input. Please enter a number.");
            }
            if (!isIncorrect && (userNumber < MIN || userNumber > MAX)) {
                isIncorrect = true;
                System.out.println("Error. You are outside the acceptable range[" + MIN + ", " + MAX + "].");
            }
        } while (isIncorrect);

        return userNumber;
    }

    private static String inputPathToFile(FileAccessMode mode) {
        String userPath;

        System.out.print("Enter the path to the input file: ");
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
            System.out.println("Error. The file at the specified path does not exist.");

        return fileExist;
    }

    private static boolean checkFileExtension(final String USER_PATH) {
        final String FILE_EXTENSION = ".txt";
        boolean isExtValid;

        isExtValid = (USER_PATH.length() >= FILE_EXTENSION.length()
                && FILE_EXTENSION.equals(USER_PATH.substring(USER_PATH.length() - FILE_EXTENSION.length())));
        if (!isExtValid)
            System.out.println("Error. The file must have the extension `" + FILE_EXTENSION + "`.");

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
        boolean isReadable;
        isReadable = true;
        try (FileReader checkRead = new FileReader(file)) {
            checkRead.read();
        } catch (IOException e) {
            isReadable = false;
            System.out.println("Error. The file cannot be read.");
        }
        return isReadable;
    }

    private static boolean checkIsFileWritable(File file) {
        boolean isWritable;
        isWritable = true;
        try (FileWriter checkWrite = new FileWriter(file)) {
            checkWrite.write("");
        } catch (IOException e) {
            isWritable = false;
            System.out.println("Error. Cannot write to file.");
        }
        return isWritable;
    }
}