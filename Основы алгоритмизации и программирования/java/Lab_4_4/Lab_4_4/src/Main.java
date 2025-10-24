import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        ENTER_MATRIX(1),
        FIND_WAY(2),
        SAVE_DATA(3),
        LOAD_DATA(4);

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
        System.out.println("This program will check if there is a route between city №1 and city №n");
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

    private static void displayMenu(boolean isMatrixExists, boolean isWayFound) {
        System.out.println("=================================================================");
        System.out.println("|1. Enter matrix     |");
        System.out.println("|2. " + colorText("Find way", isMatrixExists) + "         |");
        System.out.println("|3. " + colorText("Save data", isWayFound) + "        |");
        System.out.println("|4. Load data        |");
        System.out.println("|0. Exit             |");
        System.out.println("|--------------------|");
    }

    private static void showMenuAndProcess() {
        boolean isMatrixExists = false;
        boolean isWayFound = false;
        boolean isReachable = false;
        boolean isRunning;
        MenuOption choice;

        int[][] mtrx = new int[0][0];
        int[][] dataFromFile;

        isRunning = true;
        do {
            isMatrixExists = mtrx.length != 0;
            displayMenu(isMatrixExists, isWayFound);
            choice = getValidMenuChoice(isMatrixExists, isWayFound);

            switch (choice) {
                case ENTER_MATRIX -> {
                    mtrx = createMatrix();
                    isWayFound = false;
                }
                case FIND_WAY -> {
                    isReachable = findWay(mtrx);
                    isWayFound = true;
                }
                case SAVE_DATA -> saveToFile(mtrx, isReachable);
                case LOAD_DATA -> {
                    dataFromFile = loadFromFile();
                    if ((dataFromFile != null) && validateAdjacencyMatrix(dataFromFile)) {
                        mtrx = Arrays.copyOf(dataFromFile, dataFromFile[0].length);
                        isWayFound = false;
                    }
                }
                case EXIT -> isRunning = false;
            }
        } while (isRunning);
    }

    private static MenuOption getValidMenuChoice(boolean isMatrixExists, boolean isWayFound) {
        final int MAX_NUM = MenuOption.getMaxOrdinal();
        boolean isAvailable;
        MenuOption choice;

        do {
            choice = MenuOption.fromCode(enterNum("Select option: ", 0, MAX_NUM));
            isAvailable = switch (choice) {
                case FIND_WAY -> isMatrixExists;
                case SAVE_DATA -> isWayFound;
                default -> true;
            };
            if (!isAvailable)
                System.out.println("You can't use this option.");
        } while (!isAvailable);

        return choice;
    }

    private static int readPoint(String prompt, int max) {
        return enterNum(prompt + " (1-" + max + "): ", 1, max) - 1;
    }

    private static int[][] createMatrix() {
        final int MAX = 100;
        int[][] mtrx;
        int rank;
        int roads;
        int from = 0, to = 0;

        rank = enterNum("Enter number of cities: ", 2, MAX);
        mtrx = new int[rank][rank];

        roads = enterNum("Enter amount of roads: ", 0, rank * (rank - 1) / 2);

        for (int i = 0; i < roads; i++) {
            System.out.println("Enter road #" + (i + 1) + ":");
            boolean isValid = false;
            while (!isValid) {
                from = readPoint("From point", rank);
                to = readPoint("To point", rank);

                if (from == to)
                    System.out.println("Error: A city cannot be connected to itself.");
                else if (mtrx[from][to] == 1)
                    System.out.println("Error: This road already exists.");
                else
                    isValid = true;
            }
            mtrx[from][to] = 1;
            mtrx[to][from] = 1;
        }

        return mtrx;
    }

    private static boolean findWay(int[][] mtrx) {
        int len = mtrx.length;
        boolean[] visited = new boolean[len];

        boolean result = isReachable(mtrx, 0, len - 1, visited);
        if (result)
            System.out.println("You can travel from city 1 to point " + len + ".");
        else
            System.out.println("You cannot travel from city 1 to point " + len + ".");
        return result;
    }

    private static boolean isReachable(int[][] graph, int current, int target, boolean[] visited) {
        if (current == target)
            return true;

        visited[current] = true;

        for (int i = 0; i < graph.length; i++)
            if (graph[current][i] == 1 && !visited[i])
                if (isReachable(graph, i, target, visited))
                    return true;
        return false;
    }

    private static void saveToFile(int[][] mtrx, boolean isReachable) {
        String filePath;

        filePath = inputPathToFile(FileAccessMode.WRITE);
        if (!filePath.isEmpty())
            writeDataInFile(mtrx, filePath, isReachable);
    }

    private static void writeDataInFile(int[][] mtrx, String filePath, boolean isReachable) {
        int len = mtrx.length;
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(len + "\n");

            for (int i = 0; i < len; i++) {
                for (int j = 0; j < len; j++)
                    writer.write(mtrx[i][j] + " ");
                writer.write("\n");
            }

            writer.write("Reachable: " + isReachable + "\n");
            System.out.println("The matrix and result were successfully saved.");
        } catch (IOException e) {
            System.out.println("An unexpected error occurred while writing the file.");
        }
    }

    private static int[][] loadFromFile() {
        String filePath;
        int[][] mtrx = null;

        filePath = inputPathToFile(FileAccessMode.READ);
        if (!filePath.isEmpty())
            mtrx = readMatrixFromFile(filePath);
        return mtrx;
    }

    private static int[][] readMatrixFromFile(String filePath) {
        int[][] matrix = null;
        boolean isDataValid = true;

        try (Scanner fileScan = new Scanner(new FileReader(filePath))) {
            if (!fileScan.hasNextInt()) {
                System.out.println("Error: Invalid file format - missing matrix size.");
                isDataValid = false;
            }

            int size = fileScan.nextInt();

            if (isDataValid) {
                matrix = new int[size][size];

                for (int i = 0; i < size && isDataValid; i++)
                    for (int j = 0; j < size && isDataValid; j++) {
                        if (!fileScan.hasNextInt()) {
                            System.out.println("Error: File is missing matrix data or invalid value.");
                            isDataValid = false;
                        } else
                            matrix[i][j] = fileScan.nextInt();
                    }
            }

            if (fileScan.hasNext()) {
                System.out.println("Error: extra data in file.");
                isDataValid = false;
            }
        } catch (IOException e) {
            System.out.println("An unexpected error occurred while reading the file.");
            isDataValid = false;
        }

        if (!isDataValid)
            matrix = null;
        return matrix;
    }

    public static boolean validateAdjacencyMatrix(int[][] matrix) {
        boolean isValid = true;
        int size = matrix.length;
        int j, i = 0;
        int val;

        while (i < size && isValid) {
            if (matrix[i][i] != 0) {
                System.out.println("Error: Cycle at top " + (i + 1));
                isValid = false;
            } else {
                j = 0;
                while (j < size && isValid) {
                    val = matrix[i][j];
                    if (val != 0 && val != 1) {
                        System.out.println("Error: invalid value " + val +
                                " in cell [" + (i + 1) + "," + (j + 1) + "]");
                        isValid = false;
                    } else if (matrix[i][j] != matrix[j][i]) {
                        System.out.println("Error: the matrix is not symmetrical in [" +
                                (i + 1) + "," + (j + 1) + "] и [" +
                                (j + 1) + "," + (i + 1) + "]");
                        isValid = false;
                    } else
                        j++;
                }
            }
            i++;
        }

        return isValid;
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

        System.out.print("Enter the path to file: ");
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
        boolean isReadable = true;
        try (FileReader checkRead = new FileReader(file)) {
            checkRead.read();
        } catch (IOException e) {
            isReadable = false;
            System.out.println("Error. The file cannot be read.");
        }
        return isReadable;
    }

    private static boolean checkIsFileWritable(File file) {
        boolean isWritable = true;
        try (FileWriter checkWrite = new FileWriter(file)) {
            checkWrite.write("");
        } catch (IOException e) {
            isWritable = false;
            System.out.println("Error. Cannot write to file.");
        }
        return isWritable;
    }
}