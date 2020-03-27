package org.akaza.openclinica.service;

import com.google.common.collect.Streams;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

@Service("excelFileConverterService")
public class ExcelFileConverterServiceImpl implements FileConverterService {

  private final static Logger logger = LoggerFactory.getLogger(ExcelFileConverterServiceImpl.class);

  @Override
  public File convert(File xlsFile) throws IOException {
    File csvFile = File.createTempFile(xlsFile.getName(), FILE_SUFFIX);
    try (PrintStream printStream = new PrintStream(new FileOutputStream(csvFile), true, "UTF-8")) {
      XSSFWorkbook workbook = new XSSFWorkbook(xlsFile);
      DataFormatter formatter = new DataFormatter();
      Streams.stream(workbook.iterator())
              .forEach(sheet -> Streams.stream(sheet.iterator())
                      .forEach(row -> {
                        Streams.stream(row.cellIterator())
                                .forEach(cell -> {
                                  if (cell.getColumnIndex() != 0) {
                                    printStream.print(DELIMITER);
                                  }
                                  String cellValue = formatter.formatCellValue(cell);
                                  // Replace any return characters with blank string
                                  cellValue = cellValue.replaceAll("\n", "");
                                  printStream.print(cellValue);
                                });
                        printStream.println();
                      }));
    } catch (InvalidFormatException e) {
      logger.error("Invalid excel format", e);
      return null;
    }
    return csvFile;
  }
}
