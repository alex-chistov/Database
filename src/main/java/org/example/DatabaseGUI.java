package org.example;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.util.List;

public class DatabaseGUI extends JFrame {
    private final FileDatabase database;

    // Текстовые поля для ввода данных
    private JTextField idField;
    private JTextField nameField;
    private JTextField salaryField;
    private JCheckBox activeCheckBox;

    public DatabaseGUI(FileDatabase database) {
        this.database = database;
        setTitle("File Database GUI");
        setSize(1200, 400);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Панель для ввода данных
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new GridLayout(5, 2));

        // Добавление полей для ввода данных
        inputPanel.add(new JLabel("ID:"));
        idField = new JTextField();
        inputPanel.add(idField);

        inputPanel.add(new JLabel("Name:"));
        nameField = new JTextField();
        inputPanel.add(nameField);

        inputPanel.add(new JLabel("Salary:"));
        salaryField = new JTextField();
        inputPanel.add(salaryField);

        inputPanel.add(new JLabel("Active:"));
        activeCheckBox = new JCheckBox();
        inputPanel.add(activeCheckBox);

        // Вывод области
        JTextArea outputArea = new JTextArea();
        outputArea.setEditable(false);

        // Кнопки
        JButton addButton = new JButton("Add Record");
        JButton deleteButton = new JButton("Delete Record");
        JButton findButton = new JButton("Find Record");
        JButton editButton = new JButton("Edit Record");
        JButton clearButton = new JButton("Clear Database");
        JButton displayButton = new JButton("Display All Records");
        JButton backupButton = new JButton("Create Backup");
        JButton restoreButton = new JButton("Restore From Backup");
        JButton exportButton = new JButton("Export to XLSX");

        // Панель для кнопок
        JPanel buttonPanel = new JPanel();

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(findButton);
        buttonPanel.add(editButton);
        buttonPanel.add(clearButton);
        buttonPanel.add(displayButton);
        buttonPanel.add(backupButton);
        buttonPanel.add(restoreButton);
        buttonPanel.add(exportButton);

        // Панель для размещения
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(new JScrollPane(outputArea), BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // Добавление основной панели в окно
        add(mainPanel);

        // Обработчики событий
        addButton.addActionListener(e -> addRecord(outputArea));
        deleteButton.addActionListener(e -> deleteRecord(outputArea));
        findButton.addActionListener(e -> findRecord(outputArea));
        editButton.addActionListener(e -> editRecord(outputArea));
        clearButton.addActionListener(e -> clearDatabase(outputArea));
        displayButton.addActionListener(e -> displayAllRecords(outputArea));
        backupButton.addActionListener(e -> createBackup(outputArea));
        restoreButton.addActionListener(e -> restoreFromBackup(outputArea));
        exportButton.addActionListener(e -> exportToXLSX(outputArea));

        setVisible(true);
    }

    // Метод для добавления записи
    private void addRecord(JTextArea outputArea) {
        try {
            int id = Integer.parseInt(idField.getText());
            String name = nameField.getText();
            double salary = Double.parseDouble(salaryField.getText());
            boolean active = activeCheckBox.isSelected();

            Record record = new Record(id, name, salary, active);
            database.addRecord(record);
            outputArea.setText("Record added: " + record);
        } catch (Exception e) {
            outputArea.setText("Error adding record: " + e.getMessage());
        }
    }

    // Метод для удаления записи
    private void deleteRecord(JTextArea outputArea) {
        try {
            String fieldName = "id";
            String value = idField.getText();
            if (value.isEmpty()) {
                fieldName = "name";
                value = nameField.getText();
                if (value.isEmpty()) {
                    fieldName = "salary";
                    value = salaryField.getText();
                    if (value.isEmpty()) {
                        fieldName = "active";
                        value = String.valueOf(activeCheckBox.isSelected());
                    }
                }
            }

            if (fieldName.equals("salary")) {
                double salary = Double.parseDouble(value);
                value = String.valueOf(salary);
            }

            database.deleteRecords(fieldName, value);
            outputArea.setText("Records deleted where " + fieldName + " = " + value);
        } catch (Exception e) {
            outputArea.setText("Error deleting record: " + e.getMessage());
        }
    }

    // Метод для поиска записи
    private void findRecord(JTextArea outputArea) {
        try {
            String fieldName = "id";
            String value = idField.getText();
            if (value.isEmpty()) {
                fieldName = "name";
                value = nameField.getText();
                if (value.isEmpty()) {
                    fieldName = "salary";
                    value = salaryField.getText();
                    if (value.isEmpty()) {
                        fieldName = "active";
                        value = String.valueOf(activeCheckBox.isSelected());
                    }
                }
            }

            if (fieldName.equals("salary")) {
                double salary = Double.parseDouble(value);
                value = String.valueOf(salary);
            }

            List<Record> records = database.findRecords(fieldName, value);
            if (!records.isEmpty()) {
                outputArea.setText("Found records:\n");
                for (Record record : records) {
                    outputArea.append(record + "\n");
                }
            } else {
                outputArea.setText("No records found with " + fieldName + " = " + value);
            }
        } catch (Exception e) {
            outputArea.setText("Error finding record: " + e.getMessage());
        }
    }

    // Метод для редактирования записи
    private void editRecord(JTextArea outputArea) {
        try {
            int id = Integer.parseInt(idField.getText());
            String newName = nameField.getText();
            double newSalary = Double.parseDouble(salaryField.getText());
            boolean newActive = activeCheckBox.isSelected();

            database.editRecord(id, newName, newSalary, newActive);
            outputArea.setText("Record updated: ID = " + id + ", New Name = " + newName + ", New Salary = " + newSalary + ", Active = " + newActive);
        } catch (Exception e) {
            outputArea.setText("Error editing record: " + e.getMessage());
        }
    }

    // Метод для очистки базы данных
    private void clearDatabase(JTextArea outputArea) {
        try {
            database.clearDatabase();
            outputArea.setText("Database cleared.");
        } catch (IOException e) {
            outputArea.setText("Error clearing database: " + e.getMessage());
        }
    }

    // Метод для отображения всех записей
    private void displayAllRecords(JTextArea outputArea) {
        try {
            database.displayAllRecords(outputArea);
        } catch (IOException e) {
            outputArea.setText("Error displaying records: " + e.getMessage());
        }
    }

    // Метод для создания резервной копии
    private void createBackup(JTextArea outputArea) {
        try {
            String backupPath = "database_backup.db";
            database.createBackup(backupPath);
            outputArea.setText("Backup created at: " + backupPath);
        } catch (IOException e) {
            outputArea.setText("Error creating backup: " + e.getMessage());
        }
    }

    // Метод для восстановления из резервной копии
    private void restoreFromBackup(JTextArea outputArea) {
        try {
            String backupPath = "database_backup.db";
            database.restoreFromBackup(backupPath);
            outputArea.setText("Database restored from backup: " + backupPath);
        } catch (IOException e) {
            outputArea.setText("Error restoring from backup: " + e.getMessage());
        }
    }

    // Метод для экспорта базы данных в XLSX
    private void exportToXLSX(JTextArea outputArea) {
        try {
            String exportPath = "database_export.xlsx";
            database.exportToExcel(exportPath);
            outputArea.setText("Database exported to: " + exportPath);
        } catch (IOException e) {
            outputArea.setText("Error exporting to XLSX: " + e.getMessage());
        }
    }

    public static void main(String[] args) throws IOException {
        String filePath = "database.db";
        FileDatabase database = new FileDatabase(filePath);
        new DatabaseGUI(database);
    }
}
