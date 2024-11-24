import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class NaturalMerge {

    private static final String SORTED_FILE_NAME = "sorted_records.txt";
    private static boolean isNaturalSequence(Record previous, Record current) {
        int lastNameComparison = previous.getLastName().compareTo(current.getLastName());
        return lastNameComparison < 0 || (lastNameComparison == 0 && previous.getFirstName().compareTo(current.getFirstName()) <= 0);
    }

    // Funkcja odczytująca rosnącą sekwencję z taśmy
    private List<Record> readRun(Tape tape) throws IOException {
        List<Record> run = new ArrayList<>();

        Record current = tape.readRecord(); // Odczyt pierwszego rekordu
        if (current == null) return run;    // Jeśli taśma jest pusta, zwróć pustą listę

        run.add(current); // Dodaj pierwszy rekord do sekwencji
        Record previous = current;

        while ((current = tape.readRecord()) != null) {
            if (isNaturalSequence(previous, current)) {
                run.add(current); // Dodaj rekord do sekwencji
                previous = current;
            } else {
                tape.unreadRecord(current); // Cofnij ostatni rekord (należy do następnego runu)
                break; // Koniec bieżącej sekwencji
            }
        }

        return run;
    }
    // Funkcja scalająca dwa runy
    private List<Record> mergeRuns(List<Record> run1, List<Record> run2) {
        List<Record> mergedRun = new ArrayList<>();
        int i = 0, j = 0;

        // Scalanie dwóch posortowanych runów
        while (i < run1.size() && j < run2.size()) {
            Record record1 = run1.get(i);
            Record record2 = run2.get(j);

            // Porównujemy dwa rekordy
            if (isNaturalSequence(record1, record2)) {
                mergedRun.add(record1);
                i++; // Przechodzimy do następnego rekordu w pierwszym runie
            } else {
                mergedRun.add(record2);
                j++; // Przechodzimy do następnego rekordu w drugim runie
            }
        }

        // Dodanie pozostałych rekordów z runu1 (jeśli jakieś zostały)
        while (i < run1.size()) {
            mergedRun.add(run1.get(i));
            i++;
        }

        // Dodanie pozostałych rekordów z runu2 (jeśli jakieś zostały)
        while (j < run2.size()) {
            mergedRun.add(run2.get(j));
            j++;
        }

        return mergedRun;
    }

    private void writeToFileAndDisplay(List<Record> mergedRun) throws IOException {
        // Otwieramy plik do zapisu
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(SORTED_FILE_NAME, true), "UTF-8"))) {
            for (Record record : mergedRun) {
                String formattedRecord = String.format("%-16s%-16s", record.getLastName(), record.getFirstName());
                writer.write(formattedRecord + "\n");
                System.out.println(formattedRecord);  // Wypisanie na konsolę
            }
            writer.flush();  // Zapewniamy, że dane są zapisane
        }
    }

    // Główna funkcja, która przetwarza dane z dwóch taśm
    public void sortTapeRecords(Tape tapeA, Tape tapeB) throws IOException {
        // Otwórz taśmy do odczytu
        tapeA.openForRead();
        tapeB.openForRead();

        List<Record> runA;
        List<Record> runB;

        // Pętla główna - przetwarzamy rekordy z taśm dopóki są dostępne
        while (true) {
            // Odczyt runów z obu taśm
            runA = readRun(tapeA);
            runB = readRun(tapeB);

            // Jeśli oba runy są puste, kończymy
            if (runA.isEmpty() && runB.isEmpty()) {
                break;
            }

            // Scalamy oba runy
            List<Record> mergedRun = mergeRuns(runA, runB);

            // Zapisujemy i wyświetlamy wyniki
            writeToFileAndDisplay(mergedRun);
        }

        // Zamknięcie taśm po zakończeniu
        tapeA.setReadOperationsCounter(tapeA.getReadOperationsCounter()+1);
        tapeB.setReadOperationsCounter(tapeB.getReadOperationsCounter()+1);
        tapeA.closeRead();
        tapeB.closeRead();
    }

    public void finalSort(Tape outputTape) throws IOException{
        boolean isSorted = false;
        TapeSorter.distributeToTapes("records.txt", "tapeA.txt", "tapeB.txt");
        System.out.println("Sekwencje zostały podzielone na tapeA.txt i tapeB.txt.");

        // Rozpoczęcie sortowania z użyciem klasy NaturalMerge
        Tape tapeA = new Tape("tapeA.txt");
        Tape tapeB = new Tape("tapeB.txt");

        sortTapeRecords(tapeA, tapeB);  // Sortowanie danych na tapeA i tapeB
        while(!isSorted) {
            TapeSorter.distributeToTapes("sorted_records.txt", "tapeA.txt", "tapeB.txt");
            outputTape.clear();
            sortTapeRecords(tapeA, tapeB);
            tapeA.clear();
            tapeB.clear();
            isSorted = checkIfSorted(outputTape);  // Sprawdzanie, czy taśma jest już posortowana
        }
        // Po zakończeniu sortowania
        //System.out.println("Liczba faz sortowania: " + phaseCount);
        System.out.println("Liczba odczytów stron z dysku: " + (tapeA.getReadOperationsCounter() + tapeB.getReadOperationsCounter()));
        System.out.println("Liczba zapisów stron na dysk: " + (tapeA.getWriteOperationsCounter() + tapeB.getWriteOperationsCounter()));

    }


    private static boolean checkIfSorted(Tape tape) throws IOException {
        Record previousRecord = null;
        Record currentRecord;
        while ((currentRecord = tape.readRecord()) != null) {
            if (previousRecord != null && !isNaturalSequence(previousRecord, currentRecord)) {
                return false;  // Jeśli dane nie są w porządku, zwróć false
            }
            previousRecord = currentRecord;
        }
        return true;  // Jeśli dane są posortowane
    }
}
