import util.StrsFileReader;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws Exception {
        String directoryPath = "resources";
        String inputFilePath = "input.csv";
        String outputFilePath = "output.csv";
        inputFilePath = StrsFileReader.getRightPath(directoryPath, inputFilePath);
        outputFilePath = StrsFileReader.getRightPath(directoryPath, outputFilePath);

        // Считываем данные из файла
        DataReader dataReader = new DataReader(inputFilePath);
        System.out.println("\n-----Входные данные -> список объектов. В полях объекта: ник, кому одалживал и сколько."
                + " Это структура данных. Все остальное рассчитывается.");
        dataReader.getParticipants().forEach((nickname, person) -> System.out.println(person));

        // Расчет транзакций и перезапись задолженностей
        Splitter splitter = new Splitter(dataReader.getParticipants());
        Map<String, Integer> transactions = splitter.getTransactions(); // Для консоли
        Map<String, Person> updParticipants = splitter.getRewritedDebts(); // Участники с переписанными долгами
        System.out.println("\n-----Схема взаиморасчетов");
        transactions.forEach((nickname, balance) -> System.out.println(nickname + ": " + balance));
        System.out.println("\n-----Переписанные долги участников");
        updParticipants.forEach((nickname, person) -> System.out.println(person));

        // Запись данных в файл
        DataWriter dataWriter = new DataWriter(outputFilePath, updParticipants);
        dataWriter.WriteToFile();
    }


}
