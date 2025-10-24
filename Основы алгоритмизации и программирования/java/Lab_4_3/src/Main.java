import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class Main {
    final static int MIN = -1_000_000;
    final static int MAX = 1_000_000;

    private static Scanner scanInput = new Scanner(System.in);

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        ENTER_ARRAY(1),
        ENTER_NUMBER(2),
        SORT_ARRAY(3),
        FIND_NUMBER(4),
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
        System.out.println("This program performs a recursive binary search for an element in a sorted array");
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

    private static void displayMenu(boolean isArrayExists, boolean isArrSorted, boolean isNumWasFound, boolean isTargetNumExists,
                                    int[] originalArray, int[] sortedArray) {
        System.out.println("=================================================================");
        System.out.println("|1. Enter array              |");
        System.out.println("|2. Enter number             |");
        System.out.println("|3. " + colorText("Sort array", isArrayExists) + "               |");
        System.out.println("|4. " + colorText("Find number", isArrSorted && isTargetNumExists) + "              |");
        System.out.println("|5. " + colorText("Save data", isNumWasFound) + "                |");
        System.out.println("|6. Load data                |");
        System.out.println("|0. Exit                     |");
        System.out.println("|----------------------------|");
        System.out.print("|Original array: ");
        System.out.println(arrToStr(originalArray));
        System.out.print("|Sorted array: ");
        System.out.println(arrToStr(sortedArray));
    }

    private static void showMenuAndProcess() {
        boolean isArrExists = false;
        boolean isTargetNumExists = false;
        boolean isArrSorted = false;
        boolean isNumWasFound = false;
        boolean isRunning;
        MenuOption choice;

        int[][] dataFromFile;
        int[] origArr = new int[0];
        int[] sortedArr = new int[0];
        int targetNum = 0;
        int targetIdx = 0;

        isRunning = true;
        do {
            isArrExists = origArr.length != 0;
            isArrSorted = sortedArr.length != 0;
            displayMenu(isArrExists, isArrSorted, isNumWasFound, isTargetNumExists, origArr, sortedArr);
            choice = getValidMenuChoice(isArrExists, isArrSorted, isNumWasFound);

            switch (choice) {
                case ENTER_ARRAY -> origArr = createArray();
                case ENTER_NUMBER -> {
                    targetNum = enterNum("Enter number to search: ", MIN, MAX);
                    isTargetNumExists = true;
                }
                case SORT_ARRAY ->  {
                    sortedArr = Arrays.copyOf(origArr, origArr.length);
                    simpleInsertSort(sortedArr);
                }
                case FIND_NUMBER -> {
                    targetIdx = findElement(sortedArr, targetNum);
                    isNumWasFound = targetIdx != -1;
                    if (targetIdx == -1)
                        System.out.println("Number was not found.");
                    else
                        System.out.println("Number position: " + (targetIdx + 1));
                }
                case SAVE_DATA -> saveToFile(origArr,sortedArr,targetNum,targetIdx + 1);
                case LOAD_DATA -> {
                    dataFromFile = loadFromFile();
                    if (dataFromFile != null) {
                        origArr = Arrays.copyOf(dataFromFile[0], dataFromFile[0].length);
                        targetNum = dataFromFile[1][0];
                    }
                }
                case EXIT -> isRunning = false;
            }
        } while (isRunning);
    }

    private static void saveToFile(int[] origArr, int[] sortedArr, int targetNum, int numPos) {
        String filePath = inputPathToFile(FileAccessMode.WRITE);
        if (!filePath.isEmpty())
            writeDataInFile(filePath, arrToStr(origArr), arrToStr(sortedArr), targetNum, numPos);
    }

    private static void writeDataInFile(String filePath ,String origArr, String sortedArr, int targetNum, int numPos) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write("Original array = " + origArr + '\n');
            writer.write("Sorted array = " + sortedArr + '\n');
            writer.write("Value " + targetNum + " found at position: " + numPos);
        } catch (IOException e) {
            System.out.println("An unexpected error occurred while writing the file.");
        }
    }

    private static int[][] loadFromFile() {
        int[][] data = null;
        String filePath = inputPathToFile(FileAccessMode.READ);
        if (!filePath.isEmpty()) {
            data = readDataFromFile(filePath);
        }
        return data;
    }

    private static int[][] readDataFromFile (String filePath) {
        int[][] data = new int[2][0];
        boolean hasExtraData;
        int lenOfArr;
        int i = 0;

        hasExtraData = false;
        File userFile = new File(filePath);

        if (userFile.length() == 0) {
            System.out.println("File is empty.");
        } else {
            try (Scanner fileScan = new Scanner(new FileReader(filePath))){
                lenOfArr = fileScan.nextInt();
                if (fileScan.hasNext())
                    hasExtraData = true;
                else
                    data[0] = new int[lenOfArr];

                if (!hasExtraData) {
                    while (i < lenOfArr && fileScan.hasNext()) {
                        data[0][i] = fileScan.nextInt();
                        ++i;
                    }
                    hasExtraData = fileScan.hasNext();
                }

                if (!hasExtraData) {
                    if (fileScan.hasNextLine()) {
                        data[1] = new int[1];
                        data[1][0] = fileScan.nextInt();
                    }
                    hasExtraData = fileScan.hasNext();
                }
            } catch (IOException e) {
                System.out.println("An unexpected error occurred while reading the file.");
            }

            if (hasExtraData) {
                data = null;
                System.out.println("Error. File contains extra lines.");
            }
        }
        return data;
    }

    private static MenuOption getValidMenuChoice(boolean isArrayExists, boolean isArraySorted, boolean isNumWasFound) {
        final int MAX_NUM = MenuOption.getMaxOrdinal();
        boolean isAvailable;
        MenuOption choice;

        do {
            choice = MenuOption.fromCode(enterNum("Select option: ", 0, MAX_NUM));
            isAvailable = switch (choice) {
                case SORT_ARRAY -> isArrayExists;
                case FIND_NUMBER -> isArraySorted;
                case SAVE_DATA -> isNumWasFound;
                default -> true;
            };
            if (!isAvailable)
                System.out.println("You can't use this option.");
        } while (!isAvailable);

        return choice;
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

    private static int[] createArray() {
        String prompt;
        int len;
        StringBuilder builder = new StringBuilder();

        len = enterNum("Enter array length: ", 1, 100);
        int [] arr = new int[len];

        for(int i = 0; i < len; i++) {
            prompt = builder.append("Enter element №").append(i+1).append(": ").toString();
            arr[i] = enterNum(prompt, MIN, MAX);
            builder.delete(0,builder.length());
        }

        return arr;
    }

    private static String arrToStr(int[] arr) {
        int len = arr.length;
        StringBuilder builder = new StringBuilder();

        builder.append('[');
        for (int i = 0; i < len; i++) {
            builder.append(arr[i]);
            if (i < len-1)
                builder.append(", ");
        }
        builder.append(']');

        return builder.toString();
    }

    private static void simpleInsertSort(int[] arr) {
        int j;
        int len;
        len = arr.length;

        for (int i = 1; i < len; i++) {
            int current = arr[i];
            j = i - 1;
            while (j >= 0 && arr[j] > current) {
                arr[j + 1] = arr[j];
                j--;
            }
            arr[j + 1] = current;
        }
    }

    private static int findElement(int[] arr, int target) {
        int targetIdx;
        targetIdx = binarySearch(arr, target, 0,arr.length-1);
        return targetIdx;
    }

    private static int binarySearch(int[] arr, int target, int left, int right) {
        if (left > right)
            return -1;

        int mid = left + (right - left) / 2;

        if (arr[mid] == target)
            return mid;
        else if (arr[mid] < target)
            return binarySearch(arr, target, mid + 1, right);
        else
            return binarySearch(arr, target, left, mid - 1);
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