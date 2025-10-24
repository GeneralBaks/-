
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);
    static final int BOARD_SIZE = 8;

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        CLEA_BOARD(1),
        ENTER_START_END(2),
        ADD_OBSTACLES(3),
        FIND_PATH(4),
        SAVE_DATA(5),
        LOAD_DATA(6);

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
                } else {
                    i++;
                }
            }
            return result;
        }

        public static int getMaxOrdinal() {
            return values().length - 1;
        }
    }

    private static class Point {
        int x, y;
        Point(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    public static void main(String[] args) {
        System.out.println("Knight's shortest path on a chessboard with obstacles.");
        showMenuAndProcess();
        scanInput.close();
    }

    private static void showMenuAndProcess() {
        boolean isBegEndEntered = false;
        boolean isWayFound = false;
        boolean isRunning = true;

        int[][] board = new int[BOARD_SIZE][BOARD_SIZE];
        Point start = null, end = null;

        while (isRunning) {
            displayMenu(isBegEndEntered, isWayFound);
            MenuOption choice = getValidMenuChoice(isBegEndEntered, isWayFound);
            switch (choice) {
                case CLEA_BOARD -> {
                    clearBoard(board);
                    isBegEndEntered = false;
                    isWayFound = false;
                    System.out.println("Board cleared.");
                }
                case ENTER_START_END -> {
                    start = enterPoint("Start");
                    end = enterPoint("End");
                    clearBoard(board);
                    board[start.x][start.y] = 2;
                    board[end.x][end.y] = 1;
                    isBegEndEntered = true;
                    isWayFound = false;
                    printBoard(board);
                }
                case ADD_OBSTACLES -> {
                    addObstacles(board, start, end);
                    printBoard(board);
                    isWayFound = false;
                }
                case FIND_PATH -> {
                    int result = findKnightPath(board, start.x, start.y, end.x, end.y);
                    if (result == -1) {
                        System.out.println("The knight cannot reach the destination.");
                    } else {
                        System.out.println("Minimum number of moves: " + result);
                    }
                    isWayFound = true;
                }
                case SAVE_DATA -> saveToFile(board);
                case LOAD_DATA -> {
                    int[][] loaded = loadFromFile();
                    if (loaded != null) {
                        board = loaded;
                        Point[] points = findStartAndEnd(board);
                        start = points[0];
                        end = points[1];
                        isBegEndEntered = start != null && end != null;
                        isWayFound = false;
                        printBoard(board);
                    }
                }
                case EXIT -> isRunning = false;
            }
        }
    }

    private static String hasAccess(boolean isAccessible) {
        if (isAccessible)
            return "";
        else
            return "*";
    }

    private static void displayMenu(boolean startSet, boolean pathFound) {
        System.out.println("----------------------------------------------------");
        System.out.println("1. Clear board");
        System.out.println("2. Enter start and end points");
        System.out.println("3. " + hasAccess(startSet) + "Add obstacles");
        System.out.println("4. " + hasAccess(startSet)  + "Find path");
        System.out.println("5. " + hasAccess(pathFound)  + "Save board to file");
        System.out.println("6. Load board from file");
        System.out.println("0. Exit");
        System.out.println("----------------------------------------------------");
    }

    private static MenuOption getValidMenuChoice(boolean startSet, boolean pathFound) {
        int max = MenuOption.getMaxOrdinal();
        MenuOption choice;
        boolean valid;
        do {
            choice = MenuOption.fromCode(enterNum("Select option: ", 0, max));
            valid = switch (choice) {
                case FIND_PATH -> startSet;
                case SAVE_DATA -> pathFound;
                default -> true;
            };
            if (!valid) System.out.println("Option not available.");
        } while (!valid);
        return choice;
    }

    private static void clearBoard(int[][] board) {
        for (int[] row : board) Arrays.fill(row, 0);
    }

    private static Point enterPoint(String label) {
        int x = enterNum(label + " X (1-8): ", 1, BOARD_SIZE) - 1;
        int y = enterNum(label + " Y (1-8): ", 1, BOARD_SIZE) - 1;
        return new Point(x, y);
    }

    private static void addObstacles(int[][] board, Point start, Point end) {
        int count = enterNum("Number of obstacles: ", 0, BOARD_SIZE - 2);
        for (int i = 0; i < count; i++) {
            int x = enterNum("Obstacle Y (1-8): ", 1, BOARD_SIZE) - 1;
            int y = enterNum("Obstacle X (1-8): ", 1, BOARD_SIZE) - 1;
            if ((x == start.x && y == start.y) || (x == end.x && y == end.y) || board[x][y] == -1) {
                System.out.println("Invalid obstacle position. Try again.");
                i--;
            } else {
                board[x][y] = -1;
            }
        }
    }

    private static int findKnightPath(int[][] board, int startX, int startY, int endX, int endY) {
        final int[][] moves = {{-2,-1},{-2,1},{-1,-2},{-1,2},{1,-2},{1,2},{2,-1},{2,1}};
        int[][] visited = new int[BOARD_SIZE][BOARD_SIZE];
        int[][] queue = new int[BOARD_SIZE*BOARD_SIZE][2];
        int qStart = 0;
        int qEnd = 0;
        int[] curPos;
        int newX;
        int newY;
        int i;
        int[] curMov;


        visited[startX][startY] = 1;
        queue[qEnd][0] = startX;
        queue[qEnd][1] = startY;
        qEnd++;

        while (qStart < qEnd) {
            curPos = queue[qStart];
            qStart++;

            i = 0;
            while (i < BOARD_SIZE) {
                curMov = moves[i];
                newX = curPos[0] + curMov[0];
                newY = curPos[1] + curMov[1];

                if (newX >= 0 && newY >= 0 && newX < BOARD_SIZE && newY < BOARD_SIZE
                        && board[newX][newY] != -1 && visited[newX][newY] == 0) {
                    visited[newX][newY] = visited[curPos[0]][curPos[1]] + 1;

                    queue[qEnd][0] = newX;
                    queue[qEnd][1] = newY;
                    qEnd++;

                    if (newX == endX && newY == endY) {
                        return visited[newX][newY] - 1;
                    }
                }
                i++;
            }
        }
        return -1;
    }

    private static void printBoard(int[][] board) {
        System.out.println("   1 2 3 4 5 6 7 8");
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.out.print((i + 1) + " ");
            for (int j = 0; j < BOARD_SIZE; j++) {
                char c = switch (board[i][j]) {
                    case -1 -> '#';
                    case 1 -> 'E';
                    case 2 -> 'S';
                    default -> '.';
                };
                System.out.print(" " + c);
            }
            System.out.println();
        }
    }

    private static void saveToFile(int[][] board) {
        String path = inputPathToFile(FileAccessMode.WRITE);
        if (!path.isEmpty()) {
            try (FileWriter fw = new FileWriter(path)) {
                fw.write(BOARD_SIZE + "\n");
                for (int i = 0; i < BOARD_SIZE; i++) {
                    for (int j = 0; j < BOARD_SIZE; j++)
                        fw.write(board[i][j] + " ");
                    fw.write("\n");
                }
                System.out.println("Board saved.");
            } catch (IOException e) {
                System.out.println("Error writing file.");
            }
        }
    }

    private static int[][] loadFromFile() {
        int[][] board = null;
        boolean isValid = true;
        int size = 0;
        String path = inputPathToFile(FileAccessMode.READ);

        if (!path.isEmpty()) {
            try (Scanner sc = new Scanner(new FileReader(path))) {
                if (sc.hasNextInt())
                    size = sc.nextInt();
                else
                    isValid = false;

                if (isValid) {
                    board = new int[size][size];
                    for (int i = 0; i < size; i++)
                        for (int j = 0; j < size; j++)
                            if (sc.hasNextInt())
                                board[i][j] = sc.nextInt();
                            else
                                isValid = false;
                }
            } catch (IOException e) {
                System.out.println("Error reading file.");
            }
        }
        if (!isValid)
            board = null;    
        return board;
    }

    private static Point[] findStartAndEnd(int[][] board) {
        Point start = null, end = null;
        Point[] res;
        for (int i = 0; i < BOARD_SIZE; i++)
            for (int j = 0; j < BOARD_SIZE; j++) {
                if (board[i][j] == 2)
                    start = new Point(i, j);
                if (board[i][j] == 1)
                    end = new Point(i, j);
            }
        res = new Point[]{start, end};
        return res;
    }

    private static int enterNum(String msg, int min, int max) {
        int num = 0;
        boolean valid = false;
        while (!valid) {
            System.out.print(msg);
            try {
                num = Integer.parseInt(scanInput.nextLine());
                if (num >= min && num <= max) valid = true;
                else System.out.println("Enter between " + min + " and " + max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number.");
            }
        }
        return num;
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
