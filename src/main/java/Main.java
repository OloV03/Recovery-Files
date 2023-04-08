import java.io.*;
import java.util.HashMap;
import java.util.Objects;

/**
 * <h1>Практика 5</h1>
 * Данная программа позволяет восстановить файлы различных типов
 * из предоставленных дампов
 * <p>
 * @author  Ilya Jukov
 */
public class Main {

    public static void main(String[] args) {

        AnalyseDump("dumps/dump_doc");
        AnalyseDump("dumps/dump_arc");
        AnalyseDump("dumps/dump_audio");
        AnalyseDump("dumps/dump_grap");
        AnalyseDump("dumps/dump_video");

    }

    /**
     * <b>Анализ дампа</b>
     * <p>
     * Для анализа необходим сам дамп и таблица распределения сектров
     *
     * @param dumpFolder путь к папке
     */
    public static void AnalyseDump(String dumpFolder) {
        HashMap<String, FileOutputStream> files = new HashMap<>();
        String[] filesFromFolder = getFilesFromFolder(dumpFolder);

        if (filesFromFolder == null)
            return;

        String nameDumpFile = filesFromFolder[0];
        String nameTable = filesFromFolder[1];
        String resultFolder = filesFromFolder[2];

        try (FileInputStream dump = new FileInputStream(nameDumpFile);
             BufferedReader tableInfo = new BufferedReader(new FileReader(nameTable))) {

            while (tableInfo.ready()) {
                String line = tableInfo.readLine();
                String[] info = parseLineFromTable(line);

                String key = info[0];
                int sector = Integer.parseInt(info[1]);

                if (!key.contains(".")) {
                    fillPart(dump, sector);
                    continue;
                }
                else if (key.contains("(")) {
                    key = key.split(" ")[0];
                }

                if (!files.containsKey(key))
                    files.put(key, new FileOutputStream(resultFolder + "/" + key, true));

                readInFile(dump, files.get(key), sector);

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * <b>Чтение сектора <i>"заполнения"</i> (пустых значений)</b>
     *
     * @param fileIn поток чтения файла
     * @param size размер сектора (в байтах)
     */
    public static void fillPart(FileInputStream fileIn, int size) throws IOException {
        byte[] buffer = new byte[512];

        while (size > 0) {
            fileIn.read(buffer);
            size--;
        }
    }

    /**
     * <b>Чтение сектора входного потока в необходимый файл</b>
     *
     * @param fileIn входной поток данных
     * @param fileOut выходной поток данных
     * @param size размер считываемого сектора
     */
    public static void readInFile(FileInputStream fileIn, FileOutputStream fileOut, int size) throws IOException {
        byte[] buffer = new byte[512];

        while (size > 0) {

            int x = fileIn.read(buffer);
            fileOut.write(buffer);
            size--;

        }

    }

    /**
     * <b>Разбор строки таблицы .csv</b>
     * @param line строка для разбора
     * @return String[] массив с названием выходного файла <p> и кол-вом секторов, которые необходимо прочитать
     */
    public static String[] parseLineFromTable(String line) {
        String[] result = new String[2];
        String[] info = line.split(";");

        result[0] = info[0];
        result[1] = String.valueOf(Integer.parseInt(info[3]) - Integer.parseInt(info[2]) + 1);

        return result;
    }

    /**
     *  <b>Разбор репозитория дампа<b/>
     *
     * @param folder папка с дампом и таблицей
     * @return String[] массив с именем файла дампа, <p> именем таблицы и папкой, куда будут помещены результаты
     */
    public static String[] getFilesFromFolder(String folder) {
        String[] files = new String[3];

        File parentDir = new File(folder);
        File resultDir = new File(parentDir.getPath() + "/Result");

        if (!resultDir.exists() && !resultDir.mkdir())
            return null;

        files[2] = resultDir.getPath();

        for (var file : Objects.requireNonNull(parentDir.listFiles())) {
            if (file.getName().contains(".dd"))
                files[0] = file.getPath();
            else
                files[1] = file.getPath();
        }

        return files;
    }
}