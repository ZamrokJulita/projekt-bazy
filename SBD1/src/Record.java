import lombok.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Record {
    private String firstName;
    private String lastName;

    @Override
    public String toString() {
        return lastName + " " + firstName;
    }

    // Metoda generująca losowe rekordy i zapisująca je na taśmie
    public static void generateRandomData(Tape tape, int numberOfRecords) throws IOException {
        List<String> firstNames = loadNamesFromFile("C:\\Users\\julit\\Documents\\Studia\\5_semestr\\SBD\\SBD1\\src\\imiona.txt");
        List<String> lastNames = loadNamesFromFile("C:\\Users\\julit\\Documents\\Studia\\5_semestr\\SBD\\SBD1\\src\\nazwiska.txt");

        Random random = new Random();
        List<Record> recordsBlock = new ArrayList<>();

        // Tworzenie i zapisywanie losowych rekordów
        for (int i = 0; i < numberOfRecords; i++) {
            String firstName = firstNames.get(random.nextInt(firstNames.size()));
            String lastName = lastNames.get(random.nextInt(lastNames.size()));
            recordsBlock.add(new Record(firstName, lastName));

            if (recordsBlock.size() == Tape.BLOCK_RECORDS) {
                tape.writeRecordsBlock(recordsBlock);  // Zapis całego bloku
                recordsBlock.clear();
            }
        }
        // Zapisanie ostatnich rekordów, jeśli blok nie jest pełny
        if (!recordsBlock.isEmpty()) {
            tape.writeRecordsBlock(recordsBlock);
        }

    }

    // Metoda wczytująca linie z pliku do listy
    private static List<String> loadNamesFromFile(String fileName) throws IOException {
        List<String> names = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                names.add(line.trim());
            }
        }
        return names;
    }

    public int compareTo(Record other) {
        // Najpierw porównujemy nazwiska
        int lastNameComparison = this.lastName.compareTo(other.lastName);

        if (lastNameComparison != 0) {
            return lastNameComparison;  // Jeśli nazwiska są różne, zwracamy wynik porównania nazwisk
        }

        // Jeśli nazwiska są takie same, porównujemy imiona
        return this.firstName.compareTo(other.firstName);
    }

}
