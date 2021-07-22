package com.sk.bds.datainsight.util;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;

import java.io.IOException;
import java.io.Reader;
import java.util.ResourceBundle;


public class DataReader extends CSVReader {
    public DataReader(Reader reader) {
        super(reader);
    }
    public DataReader(Reader reader, char separator) {
        super(reader);
        this.parser = (new CSVParserBuilder()).withSeparator(separator).withIgnoreQuotations(false).build();
    }

    @Override
    public String[] readNext() throws IOException {
        String[] result = null;
        int linesInThisRecord = 0;

        do {
            String nextLine = this.getNextLine();
            ++linesInThisRecord;
            if (!this.hasNext) {
                return this.validateResult(result);
            }

            if (this.multilineLimit > 0 && linesInThisRecord > this.multilineLimit) {
                throw new IOException(String.format(this.errorLocale, ResourceBundle.getBundle("opencsv", this.errorLocale).getString("multiline.limit.broken"), this.multilineLimit));
            }
            //기존 CSVReader 에서 MultiLine parsing 대신 singleLine 만 parsing 하도록 변경
            //String[] r = this.parser.parseLineMulti(nextLine);
            String[] r = this.parser.parseLine(nextLine);
            if (r.length > 0) {
                if (result == null) {
                    result = r;
                } else {
                    result = this.combineResultsFromMultipleReads(result, r);
                }
            }
        } while(this.parser.isPending());

        return this.validateResult(result);
    }
}