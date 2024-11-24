import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
public class Main {
    public static void main(String[] args) {
        try {

            genaratorAndManualInput();
            NaturalMerge naturalMerge = new NaturalMerge();

            Tape outputTape = new Tape("sorted_records.txt");
            outputTape.openForRead();

            // Wywołanie finalSort w celu zakończenia procesu sortowania
            naturalMerge.finalSort(outputTape);


            // Wyświetlenie posortowanych rekordów po zakończeniu sortowania
            System.out.println("\nZawartość posortowanego pliku 'sorted_records.txt':");
            outputTape.openForRead();
            outputTape.displayFileContents();  // Wyświetl zawartość posortowanej taśmy
            outputTape.closeRead();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void genaratorAndManualInput(){
        try{
            Tape tape = new Tape("records.txt");
            Scanner scanner = new Scanner(System.in);

            // Generowanie 10 losowych rekordów
            System.out.println("Generowanie losowych rekordów...");
            tape.openForWrite(false);
            Record.generateRandomData(tape, 10);  // Generowanie danych losowych
            tape.closeWrite();

            // Wprowadzenie danych ręcznych
            System.out.println("Wprowadź imię i nazwisko (oddzielone spacją), lub wpisz '0', aby zakończyć:");
            List<Record> manualRecordsBlock = new ArrayList<>();

            while (true) {
                String input = scanner.nextLine();
                if (input.equals("0")) {
                    break;  // Zakończenie wprowadzania danych
                }

                String[] parts = input.split(" ");
                if (parts.length < 2) {
                    System.out.println("Niepoprawny format. Wprowadź imię i nazwisko oddzielone spacją.");
                    continue;
                }

                String firstName = parts[0];
                String lastName = parts[1];
                manualRecordsBlock.add(new Record(firstName, lastName));

                // Zapis blokowy, gdy blok jest pełny
                if (manualRecordsBlock.size() == Tape.BLOCK_RECORDS) {
                    tape.openForWrite(true);
                    tape.writeRecordsBlock(manualRecordsBlock);
                    tape.closeWrite();
                    manualRecordsBlock.clear();
                }
            }

            // Zapisanie pozostałych rekordów, jeśli blok nie jest pełny
            if (!manualRecordsBlock.isEmpty()) {
                tape.openForWrite(true);
                tape.writeRecordsBlock(manualRecordsBlock);
                tape.closeWrite();
            }

            // Wyświetlenie zawartości pliku przed sortowaniem
            System.out.println("\nZawartość pliku 'records.txt' przed sortowaniem:");
            tape.displayFileContents();

            // Zamknięcie zasobów
            scanner.close();
            System.out.println("Operacje zakończone.");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
