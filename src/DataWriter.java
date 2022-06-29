
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class DataWriter {
    private final String outputFilePath;
    private final Map<String, Person> participants; // Участники с пересчитанными (сокращенными) долгами

    public DataWriter(String outputFilePath, Map<String, Person> participants) {
        this.outputFilePath = outputFilePath;
        this.participants = participants;
    }

    public void WriteToFile() throws Exception {
        Map<String, String> strs = getStrsOfStructure(participants);
        PrintWriter writer = new PrintWriter(outputFilePath);
        for (String nickname : strs.keySet()) {
            writer.println(nickname + strs.get(nickname));
        }
        writer.close(); // сохраняем
    }

    /**
     * Составляет для каждого Участника в 'participants' строку с его транзакциями для закрытия долгов
     * @param participants Коллекция участников
     * @return Коллекцию строк
     */
    private Map<String, String> getStrsOfStructure(Map<String, Person> participants) {
        Map<String, String> strs = new HashMap<>();
        String sep = ","; //разделитель
        StringBuilder prevSeps = new StringBuilder(sep);
        for (String nickname : participants.keySet()) {
            // Заголовки
            strs.put("", (strs.containsKey("")) ? strs.get("") + sep + nickname: sep + nickname);
            //Тело
            Map<String, Integer> loans = participants.get(nickname).getLoans();
            for (String debtor : loans.keySet()) {
                putPairWithSeparators(strs, debtor,  loans.get(debtor).toString(), sep, prevSeps.toString());
            }
            prevSeps.append(sep); // это только для новых элементов в мапе
        }
        return strs;
    }

    /**
     * Если ключ уже есть в мапе, то складываем значения.
     * Если нет, просто вставляем новый.
     * Вставляется нужное кол-во разделителей
     * @param strs Коллекция
     * @param key Имя
     * @param value Строка
     * @param separator Разделитель
     * @param prevSeparators набор разделителей начиная с начала строки и до текущей встаки
     */
    private void putPairWithSeparators(Map<String, String> strs,
                                       String key,
                                       String value,
                                       String separator,
                                       String prevSeparators) {
        class Counter {
            // Считает кол-во указанных символов в строке
            int getCountOfChar(String line) {
                int count = 0;
                for (char aChar : line.toCharArray()) {
                    if (aChar == ',')  count++;
                }
                return count;
            }
        }

        if (strs.containsKey(key)) {
            // Проверка на кол-во разделителей
            int prevCount = prevSeparators.length();
            int currentCount = new Counter().getCountOfChar(strs.get(key)); // считаем кол-во разделителей в строке
            if (prevCount > currentCount) {
                value = separator.repeat(Math.max(0, prevCount - currentCount)) + value;
            }
            value = strs.get(key) + value;
        } else {
            value = prevSeparators + value;
        }
        strs.put(key, value);
    }
}
