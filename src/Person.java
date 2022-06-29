import java.util.HashMap;
import java.util.Map;

public class Person {
    private final String nickname;
    private final Map<String, Integer> loans; // Одалживания

    public Person(String nickname) {
        this.nickname = nickname;
        loans = new HashMap<>();
    }

    public String getNickname() {
        return nickname;
    }

    public Map<String, Integer> getLoans() {
        return loans;
    }

    public void addLoan(String personNickname, Integer loan) {
        if (loans.containsKey(personNickname)) {
            loans.put(personNickname, loans.get(personNickname) + loan);
        } else {
            loans.put(personNickname, loan);
        }
    }

    public void removeLoan(String nickname) {
        loans.remove(nickname);
    }

    public void clearLoans() {
        loans.clear();
    }

    public int getLoansTotal() {
        int loansTotal = 0;
        for (int loan : loans.values()) {
            loansTotal += loan;
        }
        return loansTotal;
    }

    @Override
    public String toString() {
        final String[] loansStr = {""};
        loans.forEach((personName, loan) -> loansStr[0] += "\n\t\t" + personName + " = " + loan);
        return "Person" +
                "\n\tnickname = '" + nickname + '\'' +
                "\n\tloans =" + loansStr[0];
    }
}
