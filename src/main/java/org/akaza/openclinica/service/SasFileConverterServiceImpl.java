package org.akaza.openclinica.service;

import com.epam.parso.CSVDataWriter;
import com.epam.parso.SasFileReader;
import com.epam.parso.impl.CSVDataWriterImpl;
import com.epam.parso.impl.SasFileReaderImpl;
import org.springframework.stereotype.Service;

import java.io.*;

@Service("sasFileConverterService")
public class SasFileConverterServiceImpl implements FileConverterService {
  @Override
  public File convert(File sasFile) throws IOException {
    InputStream inputStream = new FileInputStream(sasFile);
    SasFileReader sasFileReader = new SasFileReaderImpl(inputStream);
    File convertedFile = File.createTempFile(sasFile.getName(), ".txt");
    Writer writer = new FileWriter(convertedFile);
    CSVDataWriter csvDataWriter = new CSVDataWriterImpl(writer, "|");
    csvDataWriter.writeColumnNames(sasFileReader.getColumns());
    csvDataWriter.writeRowsArray(sasFileReader.getColumns(), sasFileReader.readAll());
    writer.flush();
    return convertedFile;
  }
}
