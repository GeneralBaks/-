import java.io.*;
import java.util.*;

public class Main {
    private static final Scanner scan = new Scanner(System.in);

    private static void printCondition() {
        System.out.println("Эта программа преобразует списки инцидентности графа в матрицу смежности и находит кратчайший путь между городами.");
    }

    private static boolean hasLeadingZero(String num) {
        return num.length() > 1 && num.charAt(0) == '0';
    }

    private static boolean isValidInteger(String num, int min, int max) {
        boolean result = false;
        if (!hasLeadingZero(num)) {
            try {
                int n = Integer.parseInt(num);
                result = (n >= min && n <= max);
            } catch (Exception err) {
                result = false;
            }
        }
        return result;
    }

    private static boolean tryConvertStringToInteger(String num, int min, int max) {
        boolean result = false;
        if (num.equals("-")) {
            result = true;
        } else {
            result = isValidInteger(num, min, max);
        }
        return result;
    }

    private static String readNum(int min, int max) {
        boolean isCorrect = false;
        String num = "";
        while (!isCorrect) {
            num = scan.nextLine();
            isCorrect = tryConvertStringToInteger(num, min, max);
            if (!isCorrect && !num.equals("-")) {
                System.out.println("Вы ввели некорректные данные! Попробуйте снова:");
            }
        }
        return num;
    }

    private static int readChoice() {
        final int MIN = 1, MAX = 2;
        String num;
        System.out.println("Введите значение от " + MIN + " до " + MAX + ":");
        num = readNum(MIN, MAX);
        return Integer.parseInt(num);
    }

    private static int chooseInputListMethod() {
        int choice;
        System.out.println("Выберите способ ввода:");
        System.out.println("1. Ввод с консоли");
        System.out.println("2. Ввод из файла");
        choice = readChoice();
        return choice;
    }

    private static boolean isValidVertex(int vertex, int currentVertex, ArrayList<Integer> list) {
        boolean result = true;
        if (vertex == currentVertex) {
            System.out.println("Ошибка: Вершина не может ссылаться на саму себя (петля).");
            result = false;
        } else if (list.contains(vertex)) {
            System.out.println("Ошибка: дублирующаяся вершина " + vertex + ". Пропускаем эту вершину.");
            result = false;
        }
        return result;
    }

    private static ArrayList<Integer> inputListFromConsole(int topCount, int currentVertex) {
        ArrayList<Integer> list = new ArrayList<>();
        String num;
        int n;
        final int MIN = 1;
        System.out.println("Введите вершины графа, связанные с вершиной " + currentVertex + " (нельзя вводить саму вершину или дубликаты), для завершения введите '-':");

        boolean finished = false;
        while (!finished) {
            num = readNum(MIN, topCount);
            if (num.equals("-")) {
                finished = true;
            } else {
                n = Integer.parseInt(num);
                if (isValidVertex(n, currentVertex, list)) {
                    list.add(n);
                }
            }
        }
        Collections.sort(list);
        return list;
    }

    private static void printFileFormat() {
        System.out.println("Файл должен содержать:");
        System.out.println("- Количество вершин (1 до 99) в первой строке.");
        System.out.println("- Затем для каждой вершины пары связанных вершин и весов, разделенных пробелами.");
        System.out.println("Пример:");
        System.out.println("3\n2 10 3 5\n1 10\n1 5\n");
    }

    private static String inputListFromFile() {
        String pathFile;
        boolean isInputFromFileSuccessfully = false;
        printFileFormat();

        while (!isInputFromFileSuccessfully) {
            System.out.print("Введите путь к файлу: ");
            pathFile = scan.nextLine();
            isInputFromFileSuccessfully = checkFile(pathFile);
            if (isInputFromFileSuccessfully) {
                return pathFile;
            }
        }
        return "";
    }

    private static boolean isValidFileType(String path) {
        return path.toLowerCase().endsWith(".txt");
    }

    private static boolean isFileExists(File file) {
        return file.exists() && file.isFile();
    }

    private static boolean validateFirstLine(String firstLine, int minVertex, int maxVertex) {
        boolean result = false;
        if (!hasLeadingZero(firstLine)) {
            try {
                int vertexCount = Integer.parseInt(firstLine);
                result = (vertexCount >= minVertex && vertexCount <= maxVertex);
                if (!result) {
                    System.out.println("Количество вершин должно быть от 1 до 99.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Первая строка должна быть числом.");
            }
        } else {
            System.out.println("Количество вершин не должно иметь ведущих нулей.");
        }
        return result;
    }

    private static boolean isValidLineParts(String[] parts, int lineNumber) {
        boolean result = true;
        if (parts.length % 2 != 0) {
            System.out.println("Строка " + (lineNumber + 1) + ": Ребра должны быть в парах (вершина вес).");
            result = false;
        }
        return result;
    }

    private static boolean hasLeadingZeros(String vertexStr, String weightStr) {
        return (vertexStr.length() > 1 && vertexStr.charAt(0) == '0') ||
                (weightStr.length() > 1 && weightStr.charAt(0) == '0');
    }

    private static boolean isValidEdgeData(String vertexStr, String weightStr, int lineNumber,
                                           int vertexCount, int minWeight, int maxWeight) {
        boolean result = false;
        if (!hasLeadingZeros(vertexStr, weightStr)) {
            try {
                int neighbor = Integer.parseInt(vertexStr);
                int weight = Integer.parseInt(weightStr);

                if (neighbor >= 1 && neighbor <= vertexCount &&
                        weight >= minWeight && weight <= maxWeight) {
                    result = true;
                } else if (neighbor < 1 || neighbor > vertexCount) {
                    System.out.println("Строка " + (lineNumber + 1) + ": Вершина " + neighbor + " вне диапазона.");
                } else {
                    System.out.println("Строка " + (lineNumber + 1) + ": Вес должен быть от 1 до 100.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Строка " + (lineNumber + 1) + ": Неверный формат числа.");
            }
        } else {
            System.out.println("Строка " + (lineNumber + 1) + ": Ведущие нули не разрешены.");
        }
        return result;
    }

    private static boolean checkLineContent(String line, int lineNumber, int vertexCount,
                                            int minWeight, int maxWeight) {
        boolean result = true;
        if (!line.isEmpty()) {
            String[] parts = line.split("\\s+");
            if (isValidLineParts(parts, lineNumber)) {
                Set<Integer> neighbors = new HashSet<>();

                for (int i = 0; i < parts.length && result; i += 2) {
                    String vertexStr = parts[i];
                    String weightStr = parts[i + 1];

                    if (isValidEdgeData(vertexStr, weightStr, lineNumber, vertexCount, minWeight, maxWeight)) {
                        int neighbor = Integer.parseInt(vertexStr);

                        if (neighbor == lineNumber) {
                            System.out.println("Строка " + (lineNumber + 1) + ": Обнаружена петля (вершина ссылается на себя).");
                            result = false;
                        } else if (!neighbors.add(neighbor)) {
                            System.out.println("Строка " + (lineNumber + 1) + ": Дублирующееся ребро к вершине " + neighbor + ".");
                            result = false;
                        }
                    } else {
                        result = false;
                    }
                }
            } else {
                result = false;
            }
        }
        return result;
    }

    private static boolean validateFileContent(Scanner scanner, int vertexCount, int minWeight, int maxWeight) {
        boolean result = true;
        int lineCounter = 0;

        while (scanner.hasNextLine() && result) {
            String line = scanner.nextLine().trim();
            lineCounter++;

            if (lineCounter > vertexCount) {
                System.out.println("Слишком много строк в файле. Ожидается " + vertexCount + " определений вершин.");
                result = false;
            } else {
                result = checkLineContent(line, lineCounter, vertexCount, minWeight, maxWeight);
            }
        }

        return result;
    }

    private static boolean checkFile(String path) {
        final int MIN_VERTEX = 1, MAX_VERTEX = 99;
        final int MIN_WEIGHT = 1, MAX_WEIGHT = 100;
        boolean result = false;

        File file = new File(path);

        if (!isFileExists(file)) {
            System.out.println("Файл не найден или не является обычным файлом!");
        } else if (!isValidFileType(path)) {
            System.out.println("Неверный тип файла. Разрешены только .txt файлы.");
        } else {
            try (Scanner scanner = new Scanner(file)) {
                if (!scanner.hasNextLine()) {
                    System.out.println("Файл пуст!");
                } else {
                    String firstLine = scanner.nextLine().trim();
                    if (validateFirstLine(firstLine, MIN_VERTEX, MAX_VERTEX)) {
                        int vertexCount = Integer.parseInt(firstLine);
                        result = validateFileContent(scanner, vertexCount, MIN_WEIGHT, MAX_WEIGHT);
                    }
                }
            } catch (IOException e) {
                System.out.println("Ошибка чтения файла: " + e.getMessage());
            }
        }

        return result;
    }

    private static void initializeIncidentLists(ArrayList<Integer>[] incidentLists) {
        for (int i = 0; i < incidentLists.length; i++) {
            incidentLists[i] = new ArrayList<>();
        }
    }

    private static void processLineFromFile(String line, int currentVertex,
                                            ArrayList<Integer>[] incidentLists,
                                            ArrayList<Edge> edgeList) {
        if (!line.isEmpty()) {
            String[] parts = line.split("\\s+");
            for (int i = 0; i < parts.length; i += 2) {
                int neighbor = Integer.parseInt(parts[i]);
                int weight = Integer.parseInt(parts[i + 1]);

                if (!incidentLists[currentVertex - 1].contains(neighbor)) {
                    incidentLists[currentVertex - 1].add(neighbor);
                    edgeList.add(new Edge(currentVertex, neighbor, weight));
                }
            }
            Collections.sort(incidentLists[currentVertex - 1]);
        }
    }

    private static ArrayList<Integer>[] readFile(String path, int vertexCount, ArrayList<Edge> edgeList) {
        ArrayList<Integer>[] incidentLists = new ArrayList[vertexCount];
        initializeIncidentLists(incidentLists);

        try (Scanner fileScan = new Scanner(new File(path))) {
            fileScan.nextLine();
            int currentVertex = 1;

            while (fileScan.hasNextLine() && currentVertex <= vertexCount) {
                String line = fileScan.nextLine().trim();
                processLineFromFile(line, currentVertex, incidentLists, edgeList);
                currentVertex++;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Файл не найден!");
        }

        return incidentLists;
    }

    private static int chooseOutputListMethod() {
        int choice;
        System.out.println("Выберите способ вывода:");
        System.out.println("1. Вывод в консоль");
        System.out.println("2. Вывод в файл");
        choice = readChoice();
        return choice;
    }

    private static void outputListToConsole(ArrayList<Integer> list) {
        if (!list.isEmpty()) {
            for (Integer num : list) {
                System.out.print(num + " ");
            }
        }
    }

    private static void outputIncidentLists(ArrayList<Integer>[] incedentLists, int n) {
        for (int i = 0; i < n; i++) {
            System.out.print((i + 1) + ": ");
            outputListToConsole(incedentLists[i]);
            System.out.println();
        }
    }

    private static void outputAnswerToConsole(int minWay) {
        if (minWay == -1) {
            System.out.println("Между городами нет пути!");
        } else {
            System.out.println("Длина кратчайшего пути между городами: " + minWay);
        }
    }

    private static void writeResultToFile(FileWriter writer, int minWay) throws IOException {
        if (minWay == -1) {
            writer.write("Между городами нет пути!");
        } else {
            writer.write("Длина кратчайшего пути между городами: " + minWay);
        }
    }

    private static boolean processOutputFile(File outputFile, int minWay) {
        boolean result = false;
        try {
            if (outputFile.isFile()) {
                if (outputFile.canWrite()) {
                    try (FileWriter writer = new FileWriter(outputFile)) {
                        writeResultToFile(writer, minWay);
                        result = true;
                    }
                } else {
                    System.out.println("Файл доступен только для чтения!");
                }
            } else {
                if (outputFile.createNewFile()) {
                    try (FileWriter writer = new FileWriter(outputFile)) {
                        writeResultToFile(writer, minWay);
                        result = true;
                    }
                } else {
                    System.out.println("Не удалось создать файл!");
                }
            }
        } catch (IOException e) {
            System.out.println("Не удалось вывести в файл!");
        }
        return result;
    }

    private static void outputMinWayToFile(int minWay) {
        String path;
        boolean isFileCorrect = false;
        System.out.println("Введите путь к файлу для вывода.");
        System.out.println("Если файл не существует, он будет создан автоматически по указанному пути или в корневой папке программы (по умолчанию)");

        while (!isFileCorrect) {
            System.out.print("Введите путь к файлу и его имя с расширением: ");
            path = scan.nextLine();
            if (!isValidFileType(path)) {
                System.out.println("Неверный тип файла. Разрешены только .txt файлы.");
            } else {
                File outputFile = new File(path);
                isFileCorrect = processOutputFile(outputFile, minWay);
            }
        }
        System.out.println("Вывод данных... успешно!");
    }

    private static int getEdgeCount(ArrayList<Integer>[] incidentLists) {
        int n = incidentLists.length;
        int counter = 0;
        for (int i = 0; i < n; i++) {
            counter += incidentLists[i].size();
        }
        return counter;
    }

    private static Edge[] createEdgesArr(ArrayList<Integer>[] incidentLists) {
        int n = incidentLists.length;
        int weight, edgesCount, j = 0;
        Edge[] edges;
        final int MIN = 1, MAX = 99;
        edgesCount = getEdgeCount(incidentLists);
        edges = new Edge[edgesCount];
        for (int i = 0; i < n; i++) {
            for (Integer dest : incidentLists[i]) {
                System.out.println("Введите длину пути от города " + (i + 1) + " до города " + dest + ": ");
                weight = Integer.parseInt(readNum(MIN, MAX));
                edges[j] = new Edge(i + 1, dest, weight);
                j++;
            }
        }
        return edges;
    }

    private static void initializeDistances(int[] dist, int src) {
        for (int i = 0; i < dist.length; i++) {
            dist[i] = Integer.MAX_VALUE;
        }
        dist[src - 1] = 0;
    }

    private static void relaxEdges(int[] dist, Edge[] edges, int n) {
        for (int i = 0; i < n - 1; i++) {
            for (int j = 0; j < edges.length; j++) {
                if ((dist[edges[j].src - 1] != Integer.MAX_VALUE) &&
                        (dist[edges[j].src - 1] + edges[j].weight < dist[edges[j].dest - 1])) {
                    dist[edges[j].dest - 1] = dist[edges[j].src - 1] + edges[j].weight;
                }
            }
        }
    }

    private static int findMinWay(ArrayList<Integer>[] incidentLists, Edge[] edges, int src, int dest) {
        int n = incidentLists.length;
        int[] dist = new int[n];
        int result;

        initializeDistances(dist, src);
        relaxEdges(dist, edges, n);

        if (dist[dest - 1] == Integer.MAX_VALUE) {
            result = -1;
        } else {
            result = dist[dest - 1];
        }
        return result;
    }

    public static class Edge {
        int src;
        int dest;
        int weight;

        public Edge(int src, int dest, int weight) {
            this.src = src;
            this.dest = dest;
            this.weight = weight;
        }
    }

    public static void main(String[] args) throws FileNotFoundException {
        Integer i1 = 1000;
        Integer i2 = 1000;
        System.out.println(i1 == i2);
        System.out.println(i1 == 1000);
        printCondition();
        int choice = chooseInputListMethod();
        GraphData graphData;
        if (choice == 1) {
            graphData = readGraphFromConsole();
        } else {
            graphData = readGraphFromFile();
        }
        int minWay = findMinWay(graphData.incidentLists, graphData.edges, graphData.src, graphData.dest);
        choice = chooseOutputListMethod();
        if (choice == 1) {
            outputAnswerToConsole(minWay);
        } else {
            outputMinWayToFile(minWay);
        }
        scan.close();
    }

    private static class GraphData {
        ArrayList<Integer>[] incidentLists;
        Edge[] edges;
        int src;
        int dest;

        GraphData(ArrayList<Integer>[] incidentLists, Edge[] edges, int src, int dest) {
            this.incidentLists = incidentLists;
            this.edges = edges;
            this.src = src;
            this.dest = dest;
        }
    }

    private static GraphData readGraphFromConsole() {
        final int MIN = 1, MAX = 99;
        System.out.println("Введите количество вершин графа (числа от 1 до 99):");
        int n = Integer.parseInt(readNum(MIN, MAX));
        ArrayList<Integer>[] incidentLists = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            incidentLists[i] = inputListFromConsole(n, i + 1);
        }
        System.out.println("Список инцидентности:");
        outputIncidentLists(incidentLists, n);
        Edge[] edges = createEdgesArr(incidentLists);
        System.out.println("Введите точку отправления:");
        int src = Integer.parseInt(readNum(MIN, n));
        System.out.println("Введите точку прибытия:");
        int dest = Integer.parseInt(readNum(MIN, n));
        return new GraphData(incidentLists, edges, src, dest);
    }

    private static int readVertexCountFromFile(String path) {
        final int MIN = 1, MAX = 99;
        int result = 0;
        try (Scanner fileScan = new Scanner(new File(path))) {
            if (!fileScan.hasNextLine()) {
                System.out.println("Ошибка: Файл пуст.");
                System.exit(0);
            }
            String firstLine = fileScan.nextLine();
            if (!tryConvertStringToInteger(firstLine, MIN, MAX)) {
                System.out.println("Неверное количество вершин в файле.");
                System.exit(0);
            }
            result = Integer.parseInt(firstLine);
        } catch (Exception e) {
            System.out.println("Не удалось прочитать файл.");
            System.exit(0);
        }
        return result;
    }

    private static GraphData readGraphFromFile() {
        String path = inputListFromFile();
        int n = readVertexCountFromFile(path);

        ArrayList<Edge> edgeList = new ArrayList<>();
        ArrayList<Integer>[] incidentLists = readFile(path, n, edgeList);
        Edge[] edges = edgeList.toArray(new Edge[0]);
        System.out.println("Списки инцидентности из файла:");
        outputIncidentLists(incidentLists, n);
        System.out.println("Введите точку отправления:");
        int src = Integer.parseInt(readNum(1, n));
        System.out.println("Введите точку прибытия:");
        int dest = Integer.parseInt(readNum(1, n));

        return new GraphData(incidentLists, edges, src, dest);
    }
}