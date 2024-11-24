import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TapeSorter {

    public static void distributeToTapes(String inputFile, String tapeAFile, String tapeBFile) throws IOException {
        Tape inputTape = new Tape(inputFile);
        Tape tapeA = new Tape(tapeAFile);
        Tape tapeB = new Tape(tapeBFile);

        // Otwieranie pliku do odczytu
        inputTape.openForRead();

        // Otwieranie plików tapeA i tapeB do zapisu
        tapeA.openForWrite(false);
        tapeB.openForWrite(false);

        List<Record> sequence = new ArrayList<>();
        BufferedWriter currentTapeWriter = tapeA.writer;  // Zaczynamy od tapeA
        Record previousRecord = null;

        String line;
        while ((line = inputTape.reader.readLine()) != null) {
            // Zakładając, że linia zawiera nazwisko i imię, oddzielone spacją (ale mogą być dodatkowe spacje)
            String[] parts = line.split("\\s+", 2); // Dzielimy na maksymalnie 2 części: nazwisko i imię

            // Sprawdzamy, czy linia zawiera dokładnie dwie części: nazwisko i imię
            if (parts.length == 2) {
                String lastName = parts[0].trim();  // Nazwisko
                String firstName = parts[1].trim(); // Imię

                Record currentRecord = new Record(firstName, lastName); // Tworzymy rekord na podstawie odczytanej linii

                // Dodajemy rekord do bieżącej sekwencji lub kończymy ją, jeśli sekwencja się zmienia
                if (previousRecord == null || isNaturalSequence(previousRecord, currentRecord)) {
                    sequence.add(currentRecord);
                } else {
                    // Zapisujemy bieżącą sekwencję do aktualnej taśmy i przełączamy taśmy
                    writeSequenceToTape(currentTapeWriter, sequence);
                    sequence.clear();
                    sequence.add(currentRecord);
                    currentTapeWriter = (currentTapeWriter == tapeA.writer) ? tapeB.writer : tapeA.writer;
                }
                previousRecord = currentRecord;
            } else {
                // Jeśli linia nie zawiera dwóch części (nazwisko, imię), możemy ją zignorować lub obsłużyć błąd
                System.out.println("Zignorowano błędny rekord: " + line);
            }
        }

        // Zapisujemy ostatnią sekwencję, jeśli jest niepusta
        if (!sequence.isEmpty()) {
            writeSequenceToTape(currentTapeWriter, sequence);
        }

        // Zamykanie plików
        inputTape.closeRead();
        tapeA.closeWrite();
        tapeB.closeWrite();
    }

    // Metoda do sprawdzania, czy rekord należy do tej samej sekwencji naturalnej
    private static boolean isNaturalSequence(Record previous, Record current) {
        int lastNameComparison = previous.getLastName().compareTo(current.getLastName());
        return lastNameComparison < 0 || (lastNameComparison == 0 && previous.getFirstName().compareTo(current.getFirstName()) <= 0);
    }

    private static void writeSequenceToTape(BufferedWriter writer, List<Record> sequence) throws IOException {
        for (Record record : sequence) {
            writer.write(record.toString() + "\n");
        }
        writer.flush();  // Wypisanie zawartości bloku do pliku
    }

}
