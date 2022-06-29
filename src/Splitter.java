import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class Splitter {
    private final Map<String, Person> participants; // Участники
    private final Map<String, Person> updParticipants; // Участники с переписанными долгами
    private final Map<String, Integer> transactions; // Для консоли транзакции необходимые для взаиморасчетов
    public Splitter(Map<String, Person> participants) {
        this.participants = participants;
        transactions = new HashMap<>();
        updParticipants = new LinkedHashMap<>(); //Новая коллекция с теми же участниками как в пришедшей 'participants'
        for (String key : participants.keySet()) {
            updParticipants.put(key, new Person(key));
        }
    }

    /**
     * Вычисляет самые короткие транзакции и записывает в 'transactions' для консоли
     * и переписанные долги в 'updParticipants'
     */
    private void setTransactions() {
        Map<String, Integer> balances = new HashMap<>(); // Соберем в коллекцию с итогом по долгам
        for (Person person : participants.values()) {
            // Узнаем кому должен 'person' и кто ему должен
            Map<String, Integer> personLoans =  person.getLoans(); // одалживания
            Map<String, Integer> personCredits =  getPersonCredits(person); // займы
            // Получаем итоговый баланс Участника путем вычитания из одалживаний кредитов.
            // Собираем актуальные итоги - у кого дебет и кредит равны - не рассматриваем, выбывает из взаиморасчетов
            int loansTotal = getValuesTotal(personLoans);
            int creditsTotal = getValuesTotal(personCredits);
            if (loansTotal - creditsTotal != 0) {
                balances.put(person.getNickname(), loansTotal - creditsTotal); // пишем в коллекцию итоговых балансов
            }
        }
        //--Печать
        System.out.println("\n-----Итоговый баланс");
        balances.forEach((nickname, balance) -> System.out.println(nickname + ": " + balance));
        //--

        // Заполняем 'transactions' и 'updParticipants'
        fillTransactions(balances);

        //--Печать
        System.out.println("\n-----Итоговый баланс после взаиморасчетов");
        balances.forEach((nickname, balance) -> System.out.println(nickname + ": " + balance));
        //--
    }

    /**
     * Ищет и складывает сколько занял участник 'person'
     * @param person Участник
     * @return Сумма займов участника
     */
    private int getPersonCreditsTotal(Person person) {
        int creditsTotal = 0;
        // Найдем все займы для person
        for (Person otherPerson: participants.values()) {
            if (otherPerson.getNickname().equals(person.getNickname())) continue; // себя пропускаем
            for (Map.Entry<String, Integer> entry : otherPerson.getLoans().entrySet()) {
                String nickname = entry.getKey(); // кому одолжил
                int loan = entry.getValue();    // сколько одолжил
                // Если у другого текущий 'person' обозначен как должник, запишем в мапу 'personCredits'
                if (nickname.equals(person.getNickname())) {
                    creditsTotal += loan;
                }
            }
        }
        return creditsTotal;
    }

    /**
     * Складывает значения перечисленные в коллекции движения денег 'sums'
     * @param sums Коллекция займов
     * @return Сумма займов
     */
    private int getValuesTotal(Map<String, Integer> sums) {
        int sumsTotal = 0;
        for (int sum : sums.values()) {
            sumsTotal += sum;
        }
        return sumsTotal;
    }

    /**
     * Перебирает всех участников и ищет кому участник 'person' должен
     * @param person Участник
     * @return Коллекцию долгов участника (имя = долг)
     */
    private Map<String, Integer> getPersonCredits(Person person) {
        Map<String, Integer> personCredits = new HashMap<>(); // Займы
        // Найдем все займы для person
        for (Person otherPerson: participants.values()) {
            if (otherPerson.getNickname().equals(person.getNickname())) continue; // себя пропускаем
            for (Map.Entry<String, Integer> entry : otherPerson.getLoans().entrySet()) {
                String nickname = entry.getKey(); // кому одолжил
                int loan = entry.getValue();    // сколько одолжил
                // Если у другого текущий 'person' обозначен как должник, запишем в мапу 'personCredits'
                if (nickname.equals(person.getNickname())) {
                    personCredits.put(otherPerson.getNickname(),loan);
                }
            }
        }
        return personCredits;
    }

    public Map<String, Integer> getTransactions() {
        if (transactions.isEmpty()) {
            setTransactions();
        }
        return transactions;
    }

    public Map<String, Person> getRewritedDebts() {
        if (transactions.isEmpty()) {
            updParticipants.forEach((nickname, person) -> person.clearLoans()); // обнулим задолженности
            setTransactions();
        }
        return updParticipants;
    }

    private void fillTransactions(Map<String, Integer> balances) {
        boolean isDone = true;
        // Писк по прямому совпадению суммы
        for (String nickname:balances.keySet()) {
            int balance = balances.get(nickname);
            if (balance == 0) continue;
            int contraBalance = balance * -1;
            String otherNickname = getKeyOfEqualSum(contraBalance, balances);
            if (otherNickname != null) {
                // Была найдена прямая контр-сумма
                String direct = balance > 0 ? otherNickname + " -> " + nickname : nickname + " -> " + otherNickname;
                transactions.put(direct, Math.abs(balance)); // записываем транзакцию как текст

                String debtor = balance > 0 ? otherNickname : nickname;
                String creditor = balance < 0 ? otherNickname : nickname;
                updParticipants.get(creditor).addLoan(debtor, Math.abs(balance));

                balances.put(nickname, 0); // обнуляем баланс у текущего участника
                balances.put(otherNickname, 0); // обнуляем баланс у контрагента
            } else {
                isDone = false;
            }
        }
        if (isDone) return;
        // Оставшиеся долги идем по порядку и набираем нужную сумму для расчета
        for (String nickname:balances.keySet()) {
            int balance = balances.get(nickname);
            if (balance == 0) continue;
            String counterparty = getKeysOfCoveredSums(balance, nickname, balances);
            if (counterparty == null) break; // Никого не нашли значит все
            int diff = balances.get(counterparty) + balance; // С разными знаками получится вычитание, а здесь разные
            String direct = balance > 0 ? counterparty + " -> " + nickname : nickname + " -> " + counterparty;
            String debtor = balance > 0 ? counterparty : nickname;
            String creditor = balance < 0 ? counterparty : nickname;
            if (balance > 0) {
                if (Math.abs(balances.get(counterparty)) >= Math.abs(balance)) {
                    updParticipants.get(creditor).addLoan(debtor, Math.abs(balance));
                    transactions.put(direct, Math.abs(balance)); // записываем транзакцию
                    balances.put(nickname, 0);
                    balances.put(counterparty, diff); // Ноль здесь может быть тоже
                } else {
                    updParticipants.get(creditor).addLoan(debtor, Math.abs(balances.get(counterparty)));
                    transactions.put(direct, Math.abs(balances.get(counterparty))); // записываем транзакцию
                    balances.put(nickname, diff);
                    balances.put(counterparty, 0);
                }
            } else {
                if (Math.abs(balances.get(counterparty)) > Math.abs(balance)) {
                    updParticipants.get(creditor).addLoan(debtor, Math.abs(balance));
                    transactions.put(direct, Math.abs(balance)); // записываем транзакцию
                    balances.put(nickname, 0);
                    balances.put(counterparty, diff); // Ноль здесь может быть тоже
                } else {
                    updParticipants.get(creditor).addLoan(debtor, Math.abs(balances.get(counterparty)));
                    transactions.put(direct, Math.abs(balances.get(counterparty))); // записываем транзакцию
                    balances.put(nickname, diff);
                    balances.put(counterparty, 0);
                }
            }
        }
    }

    /**
     * Ищет есть ли в коллекции сумма равная sum.
     * @param sum Сумма которую ищем
     * @param balances Коллекция в которой ищем, ключ=Имя, значение=Сумма
     * @return Имя, которое содержит искомую сумму. Если совпадений нет вернет null
     */
    private String getKeyOfEqualSum(int sum, Map<String, Integer> balances) {
        for (Map.Entry<String, Integer> entry: balances.entrySet()) {
            if (entry.getValue() == sum) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Ищет сумму противоположную по знаку и не равную нулю и возвращает владельца (ключ)
     * @param sum Сумма для поиска
     * @param ExceptKey Не искать по ключу
     * @param balances Коллекция сумм
     * @return Ключ строкового типа у которого значение противоположное по знаку искомой суммы
     */
    private String getKeysOfCoveredSums(int sum, String ExceptKey, Map<String, Integer> balances) {
        if (sum == 0) return null;
        String key = null; // Для ключи-имя кто может покрыть долг
        for (Map.Entry<String, Integer> entry: balances.entrySet()) {
            if (entry.getKey().equals(ExceptKey)) continue; // Пропускаем запись с ключом-исключением
            if (entry.getValue() == 0) continue;
            if ((sum > 0 && entry.getValue() > 0)
                    || (sum < 0 && entry.getValue() < 0)) continue; // нужно противоположное по знаку
            key = entry.getKey(); // Нашли кто частично или полностью или с запасом покроет
            break;
        }
        return key;
    }



}
