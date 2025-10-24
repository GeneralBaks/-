import java.io.*;
import java.util.Scanner;

public class Main {
    private static Scanner scanInput = new Scanner(System.in);

    private static final int MAX_STR_LEN = 255;

    private enum FileAccessMode {
        READ,
        WRITE
    }

    private enum MenuOption {
        EXIT(0),
        ENTER_STRING(1),
        PROCESS_STRING(2),
        SAVE_STRING(3),
        LOAD_STRING(4);

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
    }

    public static void main(String[] args) {
        System.out.println("This program removes the specified sequence from a string.");
        showMenuAndProcess();
        scanInput.close();
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

    private static char enterChar(final String prompt) {
        char userChar;
        String inputStr;
        boolean isIncorrect;
        int strLen;

        userChar = ' ';
        inputStr = "";
        do{
            isIncorrect = false;
            System.out.print(prompt);
            try {
                inputStr = scanInput.nextLine();
            }
            catch (Exception e) {
                System.out.println("Error. Invalid input. Please enter a correct character.");
                isIncorrect = true;
            }
            strLen = inputStr.length();
            if (strLen > 1) {
                System.out.println("Error. Invalid input. Please enter a correct character.");
                isIncorrect = true;
            }
            else if (strLen == 0)
                userChar = ' ';
            else
                userChar = inputStr.charAt(0);
            if (IsInvalidChar(inputStr.toLowerCase().charAt(0))) {
                System.out.println("Error. Invalid character.");
                isIncorrect = true;
            }
        } while(isIncorrect);

        return userChar;
    }

    private static String enterString(final String prompt, final int maxLen) {
        boolean isIncorrect;
        String userStr;

        userStr = "";

        do {
            isIncorrect = false;
            System.out.print(prompt);

            try {
                userStr = scanInput.nextLine();
            } catch (NumberFormatException e) {
                isIncorrect = true;
                System.out.println("Error. Invalid input. Please enter a number.");
            }

            if (!isIncorrect)
                isIncorrect = !isStringValid(userStr.toLowerCase(), maxLen);
        } while (isIncorrect);

        return userStr;
    }

    private static boolean isStringValid(final String str, final int MAX) {
        boolean isCorrect;
        int len;
        int i;

        i = 0;
        len = str.length();
        isCorrect = true;

        if (str.isEmpty() || str.length() > MAX) {
            isCorrect = false;
            System.out.println("Error. You are outside the acceptable range" +
                    "[0, " + MAX + "].");
        }

        while (isCorrect && i < len) {
            if (IsInvalidChar(str.charAt(i))) {
                isCorrect = false;
                System.out.println("Error. Incorrect character (" + str.charAt(i) + ").");
            }
            i++;
        }

        return isCorrect;
    }

    private static boolean IsInvalidChar(char chr) {
        boolean isCorrect;

        isCorrect = !(chr >= 'a' && chr <= 'z') &&
                !(chr >= 'а' && chr <= 'я') &&
                chr != '-' && chr != 'ё';

        return isCorrect;
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

    private static void displayMenu(boolean isUnprocSrtExist, boolean isStrProcessed,
                                              String originalStr,String processedStr) {
        System.out.println("=================================================================");
        System.out.println("|1. Enter string               |");
        System.out.println("|2. " + colorText("Process string", isUnprocSrtExist) + "             |");
        System.out.println("|3. " + colorText("Save string", isStrProcessed) + "                |");
        System.out.println("|4. Load string                |");
        System.out.println("|0. Exit                       |");
        System.out.println("|------------------------------|");
        System.out.println("|Original string: " + originalStr);
        System.out.println("|Processed string: " + processedStr);
    }

    private static void showMenuAndProcess() {
        boolean hasUnprocStr;
        boolean isStrProcessed;
        boolean isRunning;
        MenuOption choice;
        String originalStr;
        String processedStr;
        String tempStr;

        originalStr = "";
        processedStr = "";
        isRunning = true;
        do {
            isStrProcessed = !processedStr.isEmpty();
            hasUnprocStr = !originalStr.isEmpty();

            displayMenu(hasUnprocStr, isStrProcessed, originalStr, processedStr);
            choice = getValidMenuChoice(hasUnprocStr, isStrProcessed);

            switch (choice) {
                case ENTER_STRING -> {
                    originalStr = enterString("Enter string: ", MAX_STR_LEN);
                    processedStr = "";
                }
                case PROCESS_STRING -> processedStr = processString(originalStr);
                case SAVE_STRING -> saveToFile(processedStr);
                case LOAD_STRING -> {
                    tempStr = loadFromFile();
                    if (!tempStr.isEmpty()) {
                        originalStr = tempStr;
                        processedStr = "";
                    }
                }
                case EXIT -> isRunning = false;
            }
        } while (isRunning);
    }

    private static MenuOption getValidMenuChoice(boolean hasUnprocStr, boolean isStrProcessed) {
        final int MAX_NUM = 4;
        boolean isAvailable;
        MenuOption choice;

        do {
            choice = MenuOption.fromCode(enterNum("Select option: ", 0, MAX_NUM));
            isAvailable = switch (choice) {
                case PROCESS_STRING -> hasUnprocStr;
                case SAVE_STRING -> isStrProcessed;
                default -> true;
            };
            if (!isAvailable)
                System.out.println("You can't use this option.");
        } while (!isAvailable);

        return choice;
    }
    
    private static String processString(String str) {
        String seq;
        char chr;
        int num;

        chr = enterChar("Enter character you want to delete: ");
        num = enterNum("Enter length of sequence: ",1,str.length());
        seq = generateSequence(chr,num);
        str  = cutString(str,0,seq);

        return str;
    }

    private static String cutString(String str, int pos,final String SEQ) {
        if (str.length() + 1 - pos < SEQ.length())
            return str;

        if (str.startsWith(SEQ, pos)) {
            str = str.substring(0, pos) + str.substring(pos + SEQ.length());
            return cutString(str, pos,SEQ);
        } else
            return cutString(str, ++pos,SEQ);
    }

    private static String generateSequence(char chr, int len) {
        String seq;
        StringBuilder builder = new StringBuilder();
        builder.append(String.valueOf(chr).repeat(Math.max(0, len)));
        seq = builder.toString();
        return seq;
    }

    private static void saveToFile(String str) {
        String filePath;

        filePath = inputPathToFile(FileAccessMode.WRITE);
        if (!filePath.isEmpty())
            writeStringInFile(str, filePath);
    }

    private static void writeStringInFile(String str, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(str);
            System.out.println("The result was successfully recorded.");
        } catch (IOException e) {
            System.out.println("An unexpected error occurred while writing the file.");
        }
    }

    private static String loadFromFile() {
        String filePath;
        String str;
        str = "";
        filePath = inputPathToFile(FileAccessMode.READ);
        if (!filePath.isEmpty()) {
            str = readStringFromFile(filePath);
            if(!isStringValid(str.toLowerCase(),MAX_STR_LEN))
                str = "";
        }
        return str;
    }

    private static String readStringFromFile(String filePath) {
        boolean hasNextLine;
        String str;

        str = "";
        hasNextLine = false;
        File userFile = new File(filePath);

        if (userFile.length() == 0) {
            System.out.println("File is empty.");
        } else {
            try (Scanner fileScan = new Scanner(new FileReader(filePath))){
                str = fileScan.nextLine();
                hasNextLine = fileScan.hasNext();
            } catch (IOException e) {
                System.out.println("An unexpected error occurred while reading the file.");
            }

            if (hasNextLine) {
                str = "";
                System.out.println("Error. File contains extra lines.");
            }
        }
        return str;
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