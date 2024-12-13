package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

public class Main {
    public static void main(String[] args) {
        // Создаем новый Excel-рабочую книгу
        Workbook workbook = new XSSFWorkbook();

        // Создаем лист в рабочей книге
        Sheet sheet = workbook.createSheet("Test Sheet");

        // Создаем строку на первой строке (индекс 0)
        Row row = sheet.createRow(0);

        // Создаем ячейку в первой колонке (индекс 0)
        Cell cell = row.createCell(0);
        cell.setCellValue("Привет, Apache POI!");

        // Сохраняем файл
        try (FileOutputStream fileOut = new FileOutputStream("test.xlsx")) {
            workbook.write(fileOut);
            System.out.println("Файл test.xlsx успешно создан!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Закрываем рабочую книгу
        try {
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
