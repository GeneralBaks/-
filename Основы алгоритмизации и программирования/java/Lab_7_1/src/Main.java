import java.io.*;
import java.util.*;

public class Main {
    private static final Scanner scan = new Scanner(System.in);
    private static final String END_MARKER = "стоп";
    private static final String RESET = "\u001B[0m";
    private static final String RED = "\033[91;1m";
    private static final String CYAN_BOLD = "\033[38;2;0;255;255m";
    private static final String GREEN = "\033[38;2;173;255;47m";

    private static class EdgeNode {
        int vertex;
        int count;
        EdgeNode next;

        public EdgeNode(int vertex, EdgeNode next) {
            this.vertex = vertex;
            this.count = 1;
            this.next = next;
        }
    }

    private static class FileProcessingResult {
        private final boolean success;
        private final String errorMessage;
        private final GraphData graphData;

        public FileProcessingResult(GraphData graphData) {
            this.success = true;
            this.errorMessage = null;
            this.graphData = graphData;
        }

        public FileProcessingResult(boolean success, String errorMessage) {
            this.success = success;
            this.errorMessage = errorMessage;
            this.graphData = null;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public GraphData getGraphData() { return graphData; }
    }

    private static class VertexCountProcessingResult {
        private final boolean success;
        private final String errorMessage;
        private final int vertexCount;

        public VertexCountProcessingResult(int vertexCount) {
            this.success = true;
            this.errorMessage = null;
            this.vertexCount = vertexCount;
        }

        public VertexCountProcessingResult(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
            this.vertexCount = -1;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public int getVertexCount() { return vertexCount; }
    }

    private static class IncidenceListsProcessingResult {
        private final boolean success;
        private final String errorMessage;
        private final EdgeNode[] incidenceLists;
        private final boolean hasLoops;

        public IncidenceListsProcessingResult(EdgeNode[] incidenceLists, boolean hasLoops) {
            this.success = true;
            this.errorMessage = null;
            this.incidenceLists = incidenceLists;
            this.hasLoops = hasLoops;
        }

        public IncidenceListsProcessingResult(String errorMessage) {
            this.success = false;
            this.errorMessage = errorMessage;
            this.incidenceLists = null;
            this.hasLoops = false;
        }

        public boolean isSuccess() { return success; }
        public String getErrorMessage() { return errorMessage; }
        public EdgeNode[] getIncidenceLists() { return incidenceLists; }
        public boolean hasLoops() { return hasLoops; }
    }

    private static void addEdge(EdgeNode[] lists, int from, int to) {
        EdgeNode current = lists[from];
        EdgeNode prev = null;
        boolean edgeExists = false;

        while (current != null && !edgeExists) {
            if (current.vertex == to) {
                current.count++;
                edgeExists = true;
            }
            prev = current;
            current = current.next;
        }

        if (!edgeExists) {
            EdgeNode newNode = new EdgeNode(to, null);

            current = lists[from];
            prev = null;
            while (current != null && current.vertex < to) {
                prev = current;
                current = current.next;
            }

            if (prev == null) {
                newNode.next = lists[from];
                lists[from] = newNode;
            } else {
                newNode.next = prev.next;
                prev.next = newNode;
            }
        }
    }

    static class GraphData {
        EdgeNode[] incidenceLists;
        int vertexCount;
        boolean hasLoops;

        public GraphData(EdgeNode[] incidenceLists, int vertexCount, boolean hasLoops) {
            this.incidenceLists = incidenceLists;
            this.vertexCount = vertexCount;
            this.hasLoops = hasLoops;
        }
    }

    public static void main(String[] args) {
        initializeProgram();
        String choice = chooseOption("Выберите способ ввода:");
        GraphData graphData = processInputBasedOnChoice(choice);
        processOutput(graphData);
        scan.close();
    }

    private static void initializeProgram() {
        System.out.println("Эта программа работает с направленными графами.");
        System.out.println("Поддерживаются петли (ребра из вершины в саму себя).");
    }

    private static String chooseOption(String message) {
        System.out.println("\n" + message);
        System.out.println("1. Для ввода через консоль введите '" + GREEN + "консоль" + RESET + "'");
        System.out.println("2. Для ввода из файла введите '" + GREEN + "файл" + RESET + "'");
        String choice;
        boolean isValid;
        do {
            choice = scan.nextLine().toLowerCase();
            isValid = choice.equals("консоль") || choice.equals("файл");
            if (!isValid) {
                System.out.println("Слово '" + RED + choice + RESET + "' не соответствует доступным вариантам.");
                System.out.println("Пожалуйста, введите 'консоль' или 'файл':");
            }
        } while (!isValid);
        return choice;
    }

    private static GraphData processInputBasedOnChoice(String choice) {
        if (choice.equals("консоль"))
            return processConsoleInput();
        else {
            System.out.println("Файл должен содержать:");
            System.out.println("1. Первая строка: количество вершин (1-99)");
            System.out.println("2. Для каждой вершины: список связанных вершин через пробел");
            System.out.println("Примечание: Петли (ребра в саму себя) поддерживаются");
            return processFileInput();
        }
    }

    private static GraphData processConsoleInput() {
        int vertexCount = getVertexCountFromConsole();
        EdgeNode[] incidenceLists = new EdgeNode[vertexCount];
        boolean hasLoops = false;
        for (int i = 0; i < vertexCount; i++) {
            System.out.println("Введите вершины, соединенные с вершиной " + CYAN_BOLD + (i + 1) + RESET + ", введите '" + CYAN_BOLD + END_MARKER + RESET + "' для завершения:");
            List<Integer> connections = inputListFromConsole(vertexCount);
            for (int vertex : connections) {
                if (vertex - 1 == i)
                    hasLoops = true;
                addEdge(incidenceLists, i, vertex - 1);
            }
        }
        return new GraphData(incidenceLists, vertexCount, hasLoops);
    }

    private static GraphData processFileInput() {
        GraphData graphData = null;
        boolean fileIsValid = false;
        String filePath;
        do {
            filePath = getInputFilePath();
            FileProcessingResult result = processGraphFile(filePath);
            if (result.isSuccess()) {
                graphData = result.getGraphData();
                fileIsValid = true;
            } else
                System.out.println("Ошибка: " + result.getErrorMessage());
        } while (!fileIsValid);
        return graphData;
    }

    private static FileProcessingResult processGraphFile(String filePath) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            VertexCountProcessingResult vertexCountResult = processVertexCount(reader);
            if (!vertexCountResult.isSuccess())
                return new FileProcessingResult(false, vertexCountResult.getErrorMessage());
            int vertexCount = vertexCountResult.getVertexCount();
            IncidenceListsProcessingResult listsResult = processIncidenceLists(reader, vertexCount);
            if (!listsResult.isSuccess())
                return new FileProcessingResult(false, listsResult.getErrorMessage());
            if (reader.readLine() != null)
                return new FileProcessingResult(false,
                        "Файл содержит" + RED + " лишние" + RESET + " строки после обработки всех вершин. " +
                                "Ожидалось не более " + CYAN_BOLD + (vertexCount + 1) + RESET + " строк.");
            return new FileProcessingResult(
                    new GraphData(
                            listsResult.getIncidenceLists(),
                            vertexCount,
                            listsResult.hasLoops()
                    )
            );
        } catch (IOException e) {
            return new FileProcessingResult(false, RED + "Ошибка файла: " + RESET + e.getMessage());
        }
    }

    private static VertexCountProcessingResult processVertexCount(BufferedReader reader) throws IOException {
        String line = reader.readLine();
        if (line == null || line.trim().isEmpty())
            return new VertexCountProcessingResult(RED + "Отсутствует первая строка (количество вершин)." + RESET);
        if (line.trim().length() > 1 && line.trim().startsWith("0"))
            return new VertexCountProcessingResult(RED + "Ведущие нули" + RESET + " не допускаются в количестве вершин.");
        try {
            int vertexCount = Integer.parseInt(line.trim());
            if (vertexCount < 1 || vertexCount > 99)
                return new VertexCountProcessingResult("Количество вершин должно быть от" + CYAN_BOLD + " 1" + RESET + " до" + CYAN_BOLD + " 99" + RESET + ".");
            return new VertexCountProcessingResult(vertexCount);
        } catch (NumberFormatException e) {
            return new VertexCountProcessingResult(RED + "Неверный формат количества вершин." + RESET);
        }
    }

    private static IncidenceListsProcessingResult processIncidenceLists(BufferedReader reader, int vertexCount) throws IOException {
        EdgeNode[] incidenceLists = new EdgeNode[vertexCount];
        boolean hasLoops = false;
        for (int i = 0; i < vertexCount; i++) {
            String line = reader.readLine();
            if (line != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    String[] parts = line.split("\\s+");
                    List<Integer> vertices = new ArrayList<>();
                    for (String part : parts) {
                        String displayPart = part.length() > 10 ? part.substring(0, 7) + "..." : part;
                        if (part.length() > 1 && part.startsWith("0"))
                            return new IncidenceListsProcessingResult(
                                    "Номер вершины '" + displayPart + "' содержит" + RED + " ведущие нули. " + RESET);
                        try {
                            int vertex = Integer.parseInt(part);
                            String displayVertex = String.valueOf(vertex);
                            displayVertex = displayVertex.length() > 10 ? displayVertex.substring(0, 7) + "..." : displayVertex;
                            if (vertex < 1 || vertex > vertexCount)
                                return new IncidenceListsProcessingResult(
                                        "Вершина " + displayVertex + RED + " вне диапазона (1-" + vertexCount + ") для вершины " + (i+1) + RESET);
                            vertices.add(vertex);
                        } catch (NumberFormatException e) {
                            return new IncidenceListsProcessingResult(RED +
                                    "Неверный номер вершины '" + displayPart +
                                    "' для вершины " + (i+1) + ". Должно быть целым числом." + RESET);
                        }
                    }
                    Collections.sort(vertices);
                    for (int vertex : vertices) {
                        if (vertex - 1 == i)
                            hasLoops = true;
                        addEdge(incidenceLists, i, vertex - 1);
                    }
                }
            }
        }
        return new IncidenceListsProcessingResult(incidenceLists, hasLoops);
    }

    private static int getVertexCountFromConsole() {
        System.out.println("Введите количество вершин графа (от" + CYAN_BOLD + " 1" + RESET + " до " + CYAN_BOLD + "99" + RESET + "):");
        return Integer.parseInt(readNum(1, 99));
    }

    private static List<Integer> inputListFromConsole(int maxVertex) {
        List<Integer> vertices = new ArrayList<>();
        String num = readNum(1, maxVertex);
        while (!num.equalsIgnoreCase(END_MARKER)) {
            int n = Integer.parseInt(num);
            vertices.add(n);
            num = readNum(1, maxVertex);
        }
        return vertices;
    }

    private static void processOutput(GraphData graphData) {
        String outputChoice = chooseOption("Выберите способ вывода:");
        if (outputChoice.equals("консоль"))
            displayResultsOnConsole(graphData);
        else
            saveResultsToFile(graphData);
    }

    private static void displayResultsOnConsole(GraphData graphData) {
        System.out.println("\n=== Информация о графе ===");
        System.out.println("Вершины: " + graphData.vertexCount);
        System.out.println("Есть петли: " + (graphData.hasLoops ? "Да" : "Нет"));
        printIncidenceLists(graphData.incidenceLists, graphData.vertexCount);
        printMatrix(convertToAdjacencyMatrix(graphData));
    }

    private static void printIncidenceLists(EdgeNode[] lists, int n) {
        System.out.println("\nСписки смежности:");
        for (int i = 0; i < n; i++) {
            System.out.printf("%2d: ", i + 1);
            EdgeNode current = lists[i];
            while (current != null) {
                System.out.print((current.vertex + 1) + "(" + current.count + ") ");
                current = current.next;
            }
            System.out.println();
        }
    }

    private static int[][] convertToAdjacencyMatrix(GraphData graphData) {
        int[][] matrix = new int[graphData.vertexCount][graphData.vertexCount];
        for (int i = 0; i < graphData.vertexCount; i++) {
            EdgeNode current = graphData.incidenceLists[i];
            while (current != null) {
                matrix[i][current.vertex] = current.count;
                current = current.next;
            }
        }
        return matrix;
    }

    private static void printMatrix(int[][] matrix) {
        System.out.println("\nМатрица смежности:");
        writeMatrix(new PrintWriter(System.out, true), matrix);
    }

    private static void saveResultsToFile(GraphData graphData) {
        String path;
        boolean isSaved;
        System.out.println("\nДля вывода введите путь к файлу.");
        System.out.println("Если файл не существует, он будет создан автоматически");
        do {
            System.out.print("Введите путь к файлу с расширением .txt: ");
            path = scan.nextLine();
            isSaved = path.toLowerCase().endsWith(".txt") && trySaveGraphToFile(graphData, path);
            if (!path.toLowerCase().endsWith(".txt"))
                System.out.println("Ошибка: Файл должен иметь расширение .txt. Пожалуйста, попробуйте снова.");
        } while (!isSaved);
    }

    private static boolean trySaveGraphToFile(GraphData graphData, String path) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write("=== Информация о графе ===\n");
            writer.write("Вершины: " + graphData.vertexCount + "\n");
            writer.write("Есть петли: " + (graphData.hasLoops ? "Да" : "Нет") + "\n\n");
            writer.write("Списки смежности (вершина: связанные вершины с количеством):\n");
            for (int i = 0; i < graphData.vertexCount; i++) {
                writer.write(String.format("%2d: ", i + 1));
                EdgeNode current = graphData.incidenceLists[i];
                while (current != null) {
                    writer.write((current.vertex + 1) + "(" + current.count + ") ");
                    current = current.next;
                }
                writer.newLine();
            }
            writer.write("\nМатрица смежности:\n");
            writeMatrix(new PrintWriter(writer), convertToAdjacencyMatrix(graphData));
            System.out.println(GREEN + "Данные успешно сохранены в " + CYAN_BOLD + path + RESET);
            return true;
        } catch (IOException e) {
            System.out.println(RED + "Ошибка записи в файл: " + CYAN_BOLD + e.getMessage() + RESET);
            return false;
        }
    }

    private static String readNum(int min, int max) {
        String num;
        boolean isValid;
        do {
            num = scan.nextLine();
            isValid = num.equalsIgnoreCase(END_MARKER) || !tryConvertStringToInteger(num, min, max);
            if (!isValid)
                System.out.println(num.length() > 1 && num.startsWith("0") ?
                        RED + "Числа с ведущими нулями не допускаются!" + RESET + " Пожалуйста, попробуйте снова:" :
                        RED + "Вы ввели неверные данные!" + RESET + " Пожалуйста, попробуйте снова:");
        } while (!isValid);
        return num;
    }

    private static boolean tryConvertStringToInteger(String num, int min, int max) {
        if (num.length() > 1 && num.startsWith("0"))
            return true;
        try {
            int n = Integer.parseInt(num);
            return n < min || n > max;
        } catch (Exception e) {
            return !num.equalsIgnoreCase(END_MARKER);
        }
    }

    private static void writeMatrix(PrintWriter writer, int[][] matrix) {
        writer.print("   ");
        for (int i = 0; i < matrix.length; i++)
            writer.printf("%2d ", i + 1);
        writer.println();
        for (int i = 0; i < matrix.length; i++) {
            writer.printf("%2d ", i + 1);
            for (int j = 0; j < matrix[i].length; j++)
                writer.printf("%2d ", matrix[i][j]);
            writer.println();
        }
    }

    private static String getInputFilePath() {
        String path;
        boolean isValidPath;
        do {
            System.out.print("\nВведите путь к файлу с расширением .txt (например, C:/graphs/graph1.txt): ");
            path = scan.nextLine().trim();
            isValidPath = path.endsWith(".txt") && new File(path).exists();
            if (!path.endsWith(".txt"))
                System.out.println(RED + "Ошибка" + RESET + ": Файл должен иметь расширение .txt. Пожалуйста, попробуйте снова.");
            else if (!new File(path).exists())
                System.out.println(RED + "Файл не найден." + RESET + " Попробуйте снова:");
        } while (!isValidPath);
        return path;
    }
}