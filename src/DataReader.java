import util.StrsFileReader;

import java.util.*;


public class DataReader {
    private final String inputFilePath;
    private final Map<String, Person> participants;

    public DataReader(String inputFilePath) {
        this.inputFilePath = inputFilePath;
        participants = new LinkedHashMap<>();
        fillparticipants();
    }

    public Map<String, Person> getParticipants() {
        return participants;
    }

    /**
     * Fills the structure
     */
    private void fillparticipants() {
        String[] strs = StrsFileReader.getStrings(inputFilePath);
        if (strs == null) return;
        List<String> orderedNames = null;
        for (int i = 0; i < strs.length; i++) {
            String[] parts = strs[i].split(",");
            // В первой строке собираем имена и заполняем HashMap participants с ключом - nickname
            if (i == 0) {
                orderedNames = Arrays.asList(parts); // собираем имена (nickname) по порядку как в строке
                for (int j = 2; j < parts.length; j++) {
                    participants.put(parts[j], new Person(parts[j])); // ключ - nickname
                }
                continue;
            }
            // Все остальные строки
            String nickName = parts[0];
            for (int j = 2; j < parts.length; j++) {
                if (!isInt(parts[j])) continue; // не число пропускаем
                int expense = Integer.parseInt(parts[j]); // получили трату за кого-то
                if (!participants.containsKey(nickName) || j >= orderedNames.size()) continue; // существование
                // Сами себя не пишем
                if (!nickName.equals(orderedNames.get(j))) {
                    participants.get(nickName).addLoan(orderedNames.get(j), expense); // записываем кому одолжили
                }
            }
        }
    }

    /**
     * Проверка строки на int
     */
    private boolean isInt(String str) {
        if (str.matches("-?\\d+")) {
            long testNumber = Long.parseLong(str);
            return testNumber >= Integer.MIN_VALUE && testNumber <= Integer.MAX_VALUE;
        }
        return false;
    }

}
