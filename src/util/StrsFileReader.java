package util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class StrsFileReader {

    private StrsFileReader() {
    }

    private static String read(String path) {
        try {
            return Files.readString(Path.of(path));
        } catch (IOException e) {
            System.out.println("Could not open the file = " + path);
            return null;
        }
    }
    private static String[] split(String str) {
        return str.split("\\r?\\n");
//        return str != null ? str.split("\\n?\\r") : null;
    }

    /**
     * Получает массив строк файла.
     * @param path - путь к файлу.
     * @return - вернет содержимое файла разбитое на строки. Если файл не считали - вернет null.
     */
    public static String[] getStrings(String path) {
        String str = read(path);
        return str == null ? null : split(str);
    }

    /**
     * Возвращает путь до файла с верными слэшами для операционной системы.
     * Исправляет только одиночные неверные слэши. Не проверяет на наличие расширения.
     * @param directoryPath - путь к директории
     * @param fileName - имя файла с расширением
     * @return Возвращает путь
     */
    public static String getRightPath(String directoryPath, String fileName) {
        // Путь к директории
        String os = System.getProperty("os.name").toLowerCase();
        String slash = os.contains("win") ? "\\" : "/";
        directoryPath = directoryPath.trim();
        directoryPath = directoryPath.replace("\\", slash);
        directoryPath = directoryPath.replace("/", slash);
        if (!directoryPath.endsWith(slash)) {
            directoryPath = directoryPath + slash;
        }
        // Имя файла с расширением
        fileName = fileName.trim();
        fileName = fileName.replace("\\", "");
        fileName = fileName.replace("/", "");

        return directoryPath + fileName;
    }

}
