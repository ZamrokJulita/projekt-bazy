import lombok.Getter;
import lombok.Setter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Tape {
    public static final int BLOCK_RECORDS = 10;
    private static final int RECORD_SIZE = 32;
    protected BufferedWriter writer;
    protected BufferedReader reader;
    private String fileName;
    private List<Record> currentBlock;
    @Getter
    private List<Record> readBuffer; // Bufor przechowujący odczytane rekordy
    private int readIndex; // Indeks aktualnego rekordu w buforze
    @Getter
    @Setter
    private int readOperationsCounter; // Licznik operacji odczytu
    @Getter
    @Setter
    private int writeOperationsCounter; // Licznik operacji zapisu


    public Tape(String fileName) {
        this.fileName = fileName;
        this.readOperationsCounter = 0;
        this.writeOperationsCounter = 0;
    }

    public void openForWrite(boolean append) throws IOException {
        this.writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, append), "UTF-8"));
        currentBlock = new ArrayList<>();
    }

    public void clear() throws IOException {
        writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(fileName, false), "UTF-8"));
    }

    public void openForRead() throws IOException {
        this.reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName), "UTF-8"));
        this.readBuffer = new ArrayList<>(); // Inicjalizacja bufora do przechowywania odczytanych rekordów
        this.readIndex = 0; // Ustawienie indeksu na początek
    }

    public void closeRead() throws IOException {
        if (reader != null) reader.close();
        readBuffer.clear(); // Możemy wyczyścić bufor po zakończeniu odczytu
    }

    public void closeWrite() throws IOException {
        if (writer != null) writer.close();
    }

    private String formatRecord(Record record) {
        String formattedRecord = String.format("%-16s%-16s", record.getLastName(), record.getFirstName());
        return formattedRecord.substring(0, RECORD_SIZE);
    }

    // Zapis pojedynczego rekordu
    public void writeRecord(Record record) throws IOException {
        currentBlock.add(record);  // Dodaj rekord do buforu
        if (currentBlock.size() == BLOCK_RECORDS) {  // Jeżeli osiągnął maksymalny rozmiar
            writeRecordsBlock(currentBlock);  // Zapisz blok
            writeOperationsCounter++;
            currentBlock.clear();  // Wyczyść blok
        }
    }

    // Odczyt pojedynczego rekordu
    public Record readRecord() throws IOException {
        if (readBuffer.isEmpty() || readIndex >= readBuffer.size()) {
            // Jeśli bufor jest pusty lub indeks jest poza zakresem, odczytujemy nowe rekordy z pliku
            String line = reader.readLine();
            if (line == null) return null; // Koniec pliku
            // Tworzymy nowy rekord i dodajemy go do bufora
            if (line.length() < 32) {
                // Jeśli linia jest za krótka, wypełnij ją spacjami do 32 znaków
                line = String.format("%-32s", line);  // Uzupełnia linię do 32 znaków spacjami
            }
            String lastName = line.substring(0, 16).trim();
            String firstName = line.substring(16,32).trim();
            readBuffer.add(new Record(firstName, lastName));
        }
        return readBuffer.get(readIndex++);
    }

    // Zapis listy rekordów w jednym bloku
    public void writeRecordsBlock(List<Record> records) throws IOException {
        StringBuilder block = new StringBuilder();
        for (Record record : records) {
            block.append(formatRecord(record)).append("\n");
        }
        writer.write(block.toString()); // Zapis całego bloku jednocześnie
        writer.flush();  // Natychmiastowy zapis
        writeOperationsCounter++;
    }

    public void unreadRecord(Record record) {
        if (readIndex > 0) {
            // Zmniejszamy indeks, żeby cofnąć się do poprzedniego rekordu
            readIndex--;
        }
    }

    public void displayFileContents() throws IOException {
        openForRead();
        String record;
        while ((record = reader.readLine()) != null) {
            System.out.println(record);
        }
        closeRead();
    }


}
