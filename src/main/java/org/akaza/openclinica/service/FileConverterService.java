package org.akaza.openclinica.service;

import java.io.File;
import java.io.IOException;

public interface FileConverterService {
  File convert(File fileToConvert) throws IOException;
}
