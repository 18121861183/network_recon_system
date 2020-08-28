package com.colasoft.tip.network.recon;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class Test {

    public static void main(String[] args) throws IOException {
        CSVPrinter printer = CSVFormat.EXCEL.withDelimiter(',').withQuote('\"').print(new File("/root/test111.csv"), StandardCharsets.UTF_8);

        printer.printRecord("1","2","3,5",4,5);
        printer.printRecord("1","2",3,4,5);
        printer.printRecord("1","2",3,4,5);
        printer.printRecord("1","2",3,4,5);
        printer.printRecord("1","2",3,4,5);
        printer.printRecord("1","2",3,4,5);

        printer.flush();
        printer.close();

    }

}
