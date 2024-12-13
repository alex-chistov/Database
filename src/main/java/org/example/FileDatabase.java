package org.example;

import javax.swing.*;
import java.io.*;
import java.util.*;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;

import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileDatabase {
    private final String filePath;
    private final Map<Integer, Long> indexMap = new HashMap<>(); // Индексы для быстрого поиска

    public FileDatabase(String filePath) throws IOException {
        this.filePath = filePath;
        loadIndex();
    }

    // Загрузка индекса
    private void loadIndex() throws IOException {
        indexMap.clear();
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            long position = 0;
            while (file.getFilePointer() < file.length()) {
                int id = file.readInt();
                indexMap.put(id, position);
                file.readUTF(); // name
                file.readDouble(); // salary
                file.readBoolean(); // active
                position = file.getFilePointer();
            }
        }
    }

    // Добавление записи
    public void addRecord(Record record) throws IOException {
        if (indexMap.containsKey(record.getId())) {
            throw new IllegalArgumentException("Duplicate key detected");
        }

        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek(file.length());
            indexMap.put(record.getId(), file.getFilePointer());
            writeRecord(file, record);
        }
    }

    // Удаление записи
    public void deleteRecord(int id) throws IOException {
        deleteRecords("id", String.valueOf(id));
    }

    // Удаление записей по любому полю
    public void deleteRecords(String fieldName, String value) throws IOException {
        File originalFile = new File(filePath);
        File tempFile = new File(filePath + ".tmp");

        try (RandomAccessFile file = new RandomAccessFile(originalFile, "r");
             RandomAccessFile temp = new RandomAccessFile(tempFile, "rw")) {

            boolean recordFound = false;

            while (file.getFilePointer() < file.length()) {
                Record record = readRecord(file);
                if (!matches(record, fieldName, value)) {
                    writeRecord(temp, record);
                } else {
                    recordFound = true;
                }
            }

            if (!recordFound) {
                throw new IOException("No records found matching " + fieldName + " = " + value);
            }
        }

        if (!originalFile.delete()) {
            throw new IOException("Failed to delete original file.");
        }
        if (!tempFile.renameTo(originalFile)) {
            throw new IOException("Failed to replace original file.");
        }

        loadIndex();
    }

    // Поиск записей по любому полю
    public List<Record> findRecords(String fieldName, String value) throws IOException {
        List<Record> results = new ArrayList<>();

        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            while (file.getFilePointer() < file.length()) {
                Record record = readRecord(file);
                if (matches(record, fieldName, value)) {
                    results.add(record);
                }
            }
        }

        return results;
    }

    // Поиск по ID
    public Record findById(int id) throws IOException {
        Long position = indexMap.get(id);
        if (position == null) return null;

        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(position);
            return readRecord(file);
        }
    }

    // Создание резервной копии
    public void createBackup(String backupFilePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
             RandomAccessFile backupFile = new RandomAccessFile(backupFilePath, "rw")) {

            backupFile.setLength(0); // Очистить файл перед записью
            while (file.getFilePointer() < file.length()) {
                Record record = readRecord(file);
                writeRecord(backupFile, record);
            }
        }
    }

    // Восстановление из резервной копии
    public void restoreFromBackup(String backupFilePath) throws IOException {
        try (RandomAccessFile backupFile = new RandomAccessFile(backupFilePath, "r");
             RandomAccessFile mainFile = new RandomAccessFile(filePath, "rw")) {

            mainFile.setLength(0); // Очищаем основную базу данных
            while (backupFile.getFilePointer() < backupFile.length()) {
                Record record = readRecord(backupFile);
                writeRecord(mainFile, record);
            }
        }
        loadIndex();
    }

    // Экспорт данных в файл xlsx
    public void exportToExcel(String excelFilePath) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r");
             Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Database Records");

            // Создание заголовков столбцов
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Name", "Salary", "Active"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
            }

            // Чтение записей из базы данных
            int rowNum = 1;
            while (file.getFilePointer() < file.length()) {
                Record record = readRecord(file);
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(record.getId());
                row.createCell(1).setCellValue(record.getName());
                row.createCell(2).setCellValue(record.getSalary());
                row.createCell(3).setCellValue(record.isActive());
            }

            // Запись в Excel-файл
            try (FileOutputStream outputStream = new FileOutputStream(excelFilePath)) {
                workbook.write(outputStream);
            }
        }
    }

    // Вспомогательные методы
    private boolean matches(Record record, String fieldName, String value) {
        return switch (fieldName.toLowerCase()) {
            case "id" -> String.valueOf(record.getId()).equals(value);
            case "name" -> record.getName().equalsIgnoreCase(value);
            case "salary" -> String.valueOf(record.getSalary()).equals(value);
            case "active" -> String.valueOf(record.isActive()).equalsIgnoreCase(value);
            default -> false;
        };
    }

    // Чтение записи из файла
    private Record readRecord(RandomAccessFile file) throws IOException {
        int id = file.readInt();
        String name = file.readUTF();
        double salary = file.readDouble();
        boolean active = file.readBoolean();
        return new Record(id, name, salary, active);
    }

    // Запись записи) в файл
    private void writeRecord(RandomAccessFile file, Record record) throws IOException {
        file.writeInt(record.getId());
        file.writeUTF(record.getName());
        file.writeDouble(record.getSalary());
        file.writeBoolean(record.isActive());
    }

    // Отображение всех записей
    public void displayAllRecords(JTextArea outputArea) throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            outputArea.setText("");
            while (file.getFilePointer() < file.length()) {
                Record record = readRecord(file);
                outputArea.append(record + "\n");
            }
        }
    }

    // Очистка базы данных
    public void clearDatabase() throws IOException {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.setLength(0);
        }
        indexMap.clear();
    }

    // Редактирование записи
    public void editRecord(int id, String newName, double newSalary, boolean newActive) throws IOException {
        Long position = indexMap.get(id);
        if (position == null) {
            throw new IOException("Record with ID " + id + " not found.");
        }

        try (RandomAccessFile file = new RandomAccessFile(filePath, "rw")) {
            file.seek(position);
            Record record = new Record(id, newName, newSalary, newActive);
            file.seek(position);
            writeRecord(file, record);
            indexMap.put(id, position);
        }
    }
}
