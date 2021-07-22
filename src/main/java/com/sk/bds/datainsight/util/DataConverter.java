package com.sk.bds.datainsight.util;

import com.sk.bds.datainsight.exception.BadException;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.format.CellDateFormatter;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.usermodel.*;
import org.mozilla.universalchardet.UniversalDetector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;

public class DataConverter {

    private static final Logger log = LoggerFactory.getLogger(DataConverter.class);

    HashMap<String, String> header;         // <colName, colType>
    HashMap<Integer, String> headerIndex;   // <index, colName>
    List<Map<String, Object>> data;         // List< Map<colName, value> >
    HashMap<String, String> typeMap;
    String filename;
    byte[] byteData;

    public DataConverter(String filename, InputStream inputStream, String delimiter, Boolean hasHeader) throws Exception {
        header = new HashMap<>();
        headerIndex = new HashMap<>();
        data = new ArrayList<>();
        typeMap = new HashMap<>();
        this.filename = filename;
        setByteData(inputStream);
        this.convert(delimiter, hasHeader);
    }

    public HashMap<String, String> getHeader() {
       return header;
    }

    public HashMap<Integer, String> getIndex() {
        return headerIndex;
    }

    public List<Map<String, Object>> getData() {
        return data;
    }

    private void setByteData(InputStream inputStream) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int read = 0;
        while((read=inputStream.read(buffer))!=-1){
            bos.write(buffer, 0, read);
        }
        byteData = bos.toByteArray();
    }

    private void convert(String delimiter, Boolean hasHeader) throws Exception {
        String ext = filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
        log.info("filename: {}, ext: {}",filename, ext);
        switch (ext) {
            case "xls":
                excelConvert(true);
                break;
            case "xlsx":
                excelConvert(false);
                break;
            case "tsv":
                delimiterFileConvert(delimiter == null ? '\t' : delimiter.charAt(0), hasHeader);
                break;
            default:
                delimiterFileConvert(delimiter == null ? ',' : delimiter.charAt(0), hasHeader);
                break;
        }
        log.info("header: {}", header);
    }

    private void excelConvert(boolean isXls) throws Exception {
        Sheet sheet = new Sheet(isXls, new ByteArrayInputStream(byteData));
        int maxRow = sheet.getPhysicalNumberOfRows();
        int headerCount = -1;
        int headerRow = -1;
        if (maxRow > 0) {
            for (int i = 0; i < maxRow; ++i) {
                Row row = sheet.getRow(i);
                if (row != null) {
                    HashMap<String, Object> dataMap = null;
                    if (headerCount == -1) {
                        headerCount = row.getPhysicalNumberOfCells();
                        headerRow = i;
                    } else {
                        dataMap = new HashMap<>();
                        data.add(dataMap);
                    }
                    for (int j = 0; j < headerCount; ++j) {
                        Cell cell = row.getCell(j);
                        String cellValue = getCellValue(cell);
                        if (i == headerRow) {
                            cellValue = cellValue.replaceAll(" ", "_");
                            cellValue = replaceHeader(cellValue);
                            if ("".equals(cellValue)) {
                                cellValue = "EMPTY_" + j;
                            }
                            header.put(cellValue, null);
                            headerIndex.put(j, cellValue);
                        } else {
                            if (cellValue.length() < 1 || "NULL".equalsIgnoreCase(cellValue)) {
                                cellValue = null;
                            }
                            dataMap.put(headerIndex.get(j).replaceAll("-", "dash"), cellValue);
                            String type = Util.getType(cellValue);
                            String headerType = header.get(headerIndex.get(j));
                            if (headerType == null || !"STRING".equals(headerType)) {
                                header.put(headerIndex.get(j), type);
                            }
                        }
                    }
                }
            }
        }
        if (headerCount < 1) {
            log.warn("excelConvert error: file has no header info");
            throw new BadException("파일은 헤더 정보를 포함하여야 합니다.");
        } else if (this.data.size() < 1) {
            log.warn("excelConvert error: file has no record");
            throw new BadException("파일은 최소 1개의 데이터 레코드를 포함하여야 합니다.");
        }
    }

    private String getCellValue(Cell cell) {
        Object value = "";
        if (cell != null) {
            switch (cell.getCellType()) {
                case XSSFCell.CELL_TYPE_NUMERIC:
                    if (cell.isCellDateFormatted()) {
                        if(cell.getCellStyle().getDataFormat()==14) {
                            String dateFmt = "dd/mm/yyyy";
                            value = new CellDateFormatter(dateFmt).format(cell.getJavaDate());
                        } else {
                            DataFormatter fmt = new DataFormatter();
                            value = fmt.formatCellValue(cell.getCell());
                        }
                    } else {
                        String tmp = "" + cell.getNumericCellValue();
                        if ("INTEGER".equals(Util.getType(tmp))) {
                            int index = tmp.indexOf(".");
                            if (index != -1) {
                                tmp = tmp.substring(0, index);
                            }
                        }
                        value = tmp;
                    }
                    break;
                case XSSFCell.CELL_TYPE_FORMULA:
                    value = cell.getCellFormula();
                    break;
                case XSSFCell.CELL_TYPE_BOOLEAN:
                    value = cell.getBooleanCellValue();
                    break;
                case XSSFCell.CELL_TYPE_ERROR:
                    value = cell.getErrorCellString();
                    break;
                case XSSFCell.CELL_TYPE_STRING:
                    value = cell.getStringCellValue();
                    break;
            }
        }
        return value.toString();
    }

    private void delimiterFileConvert(final char delimiter, boolean hasHeader) throws Exception {
        String encoding = getFileEncoding();
        DataReader reader = new DataReader(new InputStreamReader(new ByteArrayInputStream(byteData), encoding), delimiter);

        try {
            String[] headers = reader.readNext();
            if (headers == null || headers.length < 1) {
                log.warn("delimiterFileConvert error: file has no header info");
                throw new BadException("파일은 헤더 정보를 포함하여야 합니다.");
            }

            if(hasHeader) {
                setHeader(headers);
            } else { // 첫번째 행이 Header가 아니기 때문에 첫번째 행을 데이터로 만들고, 해더는 임의 생성
                //더미 해더 생성
                ArrayList<String> dummyHeader = new ArrayList<>();
                for(int i = 0 ; i < headers.length ; i++) {
                    dummyHeader.add("Column" + i);
                }

                setHeader(dummyHeader.stream().toArray(String[]::new));

                //첫번째 행을 데이터로 만듬
                HashMap<String, Object> dataMap = new HashMap<>();
                this.data.add(dataMap);
                for (int i = 0; i < headers.length; ++i) {
                    setDataMap(dataMap, headers, i);
                }
            }

            String[] data;
            while ((data = reader.readNext()) != null) {
                HashMap<String, Object> dataMap = new HashMap<>();
                this.data.add(dataMap);
                for (int i = 0; i < headers.length; ++i) {
                    setDataMap(dataMap, data, i);
                }
            }
            if (this.data.size() < 1) {
                log.warn("delimiterFileConvert error: file has no record");
                throw new BadException("파일은 최소 1개의 데이터 레코드를 포함하여야 합니다.");
            }
        } catch (IOException ioe) { //catch IOException from DataReader
            log.warn(String.format("delimiterFileConvert error: file has invalid data format, line:%d", reader.getLinesRead()));
            throw new BadException(String.format("파일 파싱오류 : 파일이 잘못된 형식의 데이터를 포함하고 있습니다 (line:%d)", reader.getLinesRead()));
        }
    }

    private void setDataMap(HashMap<String, Object> dataMap, String[] data, int index) {
        String value = null;
        if (data.length > index) {
            value = data[index].replaceAll("[\'\"]", "");
            if (value.length() < 1 || "NULL".equalsIgnoreCase(value)) {
                value = null;
            }
        }
        dataMap.put(headerIndex.get(index).replaceAll("-", "dash"), value);
        String type = Util.getType(value);
        String headerType = header.get(headerIndex.get(index));
        if (headerType == null || !"STRING".equals(headerType)) {
            header.put(headerIndex.get(index), type);
        }
    }

    private void setHeader(String[] headers) {
        for (int i = 0; i < headers.length; ++i) {
            String headerStr = replaceHeader(headers[i]);
            if ("".equals(headerStr) || headerStr.matches("^[_]*$")) {
                headerStr = "EMPTY_" + i;
            }
            if (header.keySet().contains(headerStr)) {
                headerStr += "_" + i;
            }
            header.put(headerStr, null);
            headerIndex.put(i, headerStr);
        }
    }

    private String replaceHeader(String text) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0 ; i < text.length(); i++) {
            if (text.charAt(i) == '\'' || text.charAt(i) == '\"') {
                continue;
            }
            if (Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '-' || text.charAt(i) == '_') {
                sb.append(text.charAt(i));
            } else {
                sb.append('_');
            }
        }
        return sb.toString();
    }

    private String getFileEncoding() throws Exception {
        byte[] buffer = new byte[1024];
        int read = 0;
        ByteArrayInputStream bis = new ByteArrayInputStream(byteData);
        UniversalDetector detector = new UniversalDetector(null);
        while((read=bis.read(buffer))!=-1 && !detector.isDone()){
            detector.handleData(buffer, 0, read);
        }
        detector.dataEnd();
        String encoding = detector.getDetectedCharset();
        if (encoding == null) {
            encoding = "UTF-8";
        }
        return encoding;
    }

    class Sheet {
        XSSFSheet xssfSheet;
        HSSFSheet hssfSheet;
        boolean isXls;

        Sheet(boolean isXls, InputStream stream) throws Exception {
            this.isXls = isXls;
            if (isXls) {
                hssfSheet = new HSSFWorkbook(stream).getSheetAt(0);
            } else {
                xssfSheet = new XSSFWorkbook(stream).getSheetAt(0);
            }
        }

        Row getRow(int index) {
            XSSFRow xssfRow = xssfSheet != null ? xssfSheet.getRow(index) :  null;
            HSSFRow hssfRow = hssfSheet != null ? hssfSheet.getRow(index) :  null;
            if (xssfRow != null || hssfRow != null) {
                return new Row(isXls, xssfRow, hssfRow);
            }
            return null;
        }

        int getPhysicalNumberOfRows() {
            int index = 0;
            int rowCount = 0;
            int physicalCount = 0;
            if (isXls) {
                physicalCount = hssfSheet.getPhysicalNumberOfRows();
            } else {
                physicalCount = xssfSheet.getPhysicalNumberOfRows();
            }
            while (rowCount != physicalCount) {
                if (isXls) {
                    if (hssfSheet.getRow(index++) != null) {
                        rowCount++;
                    }
                } else {
                    if (xssfSheet.getRow(index++) != null) {
                        rowCount++;
                    }
                }
            }
            return index;
        }
    }

    class Row {
        boolean isXls;
        XSSFRow xssfRow;
        HSSFRow hssfRow;

        Row(boolean isXls, XSSFRow xssfRow, HSSFRow hssfRow) {
            this.isXls = isXls;
            this.xssfRow = xssfRow;
            this.hssfRow = hssfRow;
        }

        int getPhysicalNumberOfCells() {
            if (isXls) {
                return hssfRow.getPhysicalNumberOfCells();
            } else {
                return xssfRow.getPhysicalNumberOfCells();
            }
        }

        Cell getCell(int index) {
            XSSFCell xssfCell = xssfRow != null ? xssfRow.getCell(index) : null;
            HSSFCell hssfCell = hssfRow != null ? hssfRow.getCell(index) : null;
            if (xssfCell != null || hssfCell != null) {
                return new Cell(isXls, xssfCell, hssfCell);
            }
            return null;
        }
    }

    class Cell {
        boolean isXls;
        XSSFCell xssfCell;
        HSSFCell hssfCell;

        Cell(boolean isXls, XSSFCell xssfCell, HSSFCell hssfCell) {
            this.isXls = isXls;
            this.xssfCell = xssfCell;
            this.hssfCell = hssfCell;
        }

        int getCellType() {
            if (isXls) {
                return hssfCell.getCellType();
            } else {
                return xssfCell.getCellType();
            }
        }

        org.apache.poi.ss.usermodel.Cell getCell() {
            if (isXls) {
                return hssfCell;
            } else {
                return xssfCell;
            }
        }

        Date getJavaDate() {
            if (isXls) {
                return HSSFDateUtil.getJavaDate(hssfCell.getNumericCellValue());
            } else {
                return DateUtil.getJavaDate(xssfCell.getNumericCellValue());
            }
        }

        boolean isCellDateFormatted() {
            if (isXls) {
                return HSSFDateUtil.isCellDateFormatted(hssfCell);
            } else {
               return DateUtil.isCellDateFormatted(xssfCell);
            }
        }

        CellStyle getCellStyle() {
            if (isXls) {
                return hssfCell.getCellStyle();
            } else {
                return xssfCell.getCellStyle();
            }
        }

        double getNumericCellValue() {
            if (isXls) {
                return hssfCell.getNumericCellValue();
            } else {
                return xssfCell.getNumericCellValue();
            }
        }

        String getCellFormula() {
            if (isXls) {
                return hssfCell.getCellFormula();
            } else {
                return xssfCell.getCellFormula();
            }
        }

        boolean getBooleanCellValue() {
            if (isXls) {
                return hssfCell.getBooleanCellValue();
            } else {
                return xssfCell.getBooleanCellValue();
            }
        }

        String getErrorCellString() {
            if (isXls) {
                return "" + hssfCell.getErrorCellValue();
            } else {
                return xssfCell.getErrorCellString();
            }
        }

        String getStringCellValue() {
            if (isXls) {
                return hssfCell.getStringCellValue();
            } else {
                return xssfCell.getStringCellValue();
            }
        }

        public String toString() {
            if (isXls) {
                return hssfCell.toString();
            } else {
                return xssfCell.toString();
            }
        }
    }
}
