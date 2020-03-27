package org.akaza.openclinica.service;

import java.io.File;
import java.io.IOException;

public interface FileConverterService {
  String DELIMITER = "|";
  String FILE_SUFFIX = ".txt";
  File convert(File fileToConvert) throws IOException;
}
