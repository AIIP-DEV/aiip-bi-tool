package com.sk.bds.datainsight.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sk.bds.datainsight.database.dao.AnalysisDao;
import com.sk.bds.datainsight.database.dao.DashboardDao;
import com.sk.bds.datainsight.database.dao.SettingDao;
import com.sk.bds.datainsight.database.model.*;
import com.sk.bds.datainsight.echart.OptionHelper;
import com.sk.bds.datainsight.echart.PolarTwoValueAxes;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.exception.InternalException;
import com.sk.bds.datainsight.util.Constants;
import com.sk.bds.datainsight.util.SQLParser;
import com.sk.bds.datainsight.util.Util;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.*;
import net.sf.jsqlparser.util.deparser.StatementDeParser;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ChartService {
    private SimpleDateFormat format = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss");
    private Option defaultOption = new Option();
    private ObjectMapper objectMapper = new ObjectMapper();
    private EchartOption echartOption = new EchartOption();

    @Autowired
    SettingDao settingDao;

    @Getter
    enum ChartShape {
        SHAPE1("Column", "1", "none"),
        SHAPE2("Column", "2", "regular"),
        SHAPE3("Column", "3", "100%"),
        SHAPE4("Bar", "4", "none"),
        SHAPE5("Bar", "5", "regular"),
        SHAPE6("Bar", "6", "100%"),
        SHAPE7("Line", "7", "none"),
        SHAPE8("Line", "8", "regular"),
        SHAPE9("Line", "9", "100%"),
        SHAPE10("Area", "10", "none"),
        SHAPE11("Area", "11", "regular"),
        SHAPE12("Area", "12", "100%"),
        SHAPE13("Pie", "13", "0"),
        SHAPE14("Pie", "14", "60"),
        SHAPE15("Funnel", "15", "0"),
        SHAPE16("Funnel", "16", "60"),
        SHAPE17("Radar", "17", "polygons"),
        SHAPE18("Radar", "18", "circles"),
        UNKNOWN("unknown", "99", "none");

        private String type;
        private String shape;
        private String data;

        ChartShape(String type, String shape, String data) {
            this.type = type;
            this.shape = shape;
            this.data = data;
        }

        public static ChartShape from(String type, String shape) {
            for (ChartShape chartShape : ChartShape.values()) {
                if (chartShape.getType().equals(type) && chartShape.getShape().equals(shape)) {
                    return chartShape;
                }
            }
            return UNKNOWN;
        }
    }

    @Getter
    enum ChartType {
        PIE("PIE", "PIE", "13", false),
        PIE_3D("PIE-3D", "Pie", "13", true),
        LINE("LINE", "LINE", "7", false),
        LINE_STACKED("LINE-STACKED", "Line", "8", false),
        AREA("AREA", "Area", "11", false),
        AREA_STACKED("AREA-STACKED", "Area", "11", false),
        BAR("BAR", "Bar", "5", false),
        BAR_STACKED("BAR-STACKED", "Bar", "5", false),
        COLUMN_STACKED("COLUMN-STACKED", "Column", "2", false),
        FUNNEL("FUNNEL", "FUNNEL", "15", false),
        FUNNEL_3D("FUNNEL-3D", "Funnel", "15", true),
        RADAR("RADAR", "Radar", "17", false),
        POLAR("POLAR", "Radar", "18", false),
        AREA_CLUSTERED("AREA-CLUSTERED", "Area", "10", false),
        BAR_CLUSTERED("BAR-CLUSTERED", "Bar", "4", false),
        COLUMN_CLUSTERED("COLUMN-CLUSTERED", "Column", "1", false),
        LINE_STACKED_100("LINE-STACKED-100", "Line", "9", false),
        AREA_STACKED_100("AREA-STACKED-100", "Area", "12", false),
        BAR_STACKED_100("BAR-STACKED-100", "Bar", "6", false),
        COLUMN_STACKED_100("COLUMN-STACKED-100", "Column", "3", false),
        BAR_CLUSTERED_LINE("BAR-STACKED-LINE", "Bar", "4", false),
        COLUMN_CLUSTERED_LINE("COLUMN-CLUSTERED-LINE", "Column", "1", false),
        COLUMN("COLUMN-CLUSTERED-LINE", "Column", "1", false),
        DONUT("DOUNT", "Pie", "14", false),
        DONUT_3D("DOUNT-3D", "Pie", "14", true),
        TABLE("TABLE", "Table", "", false),
        TABLE_GROUPED("TABLE-GROUPED", "Table", "group", false),
        WORD_CLOUD("WORD-CLOUD", "Word", "", false),
        UNKNOWN("UNKNOWN", "", "", false);

        private String name;
        private String type;
        private String shape;
        private boolean enable3d;

        ChartType(String name, String type, String shape, boolean enable3d) {
            this.name = name;
            this.type = type;
            this.shape = shape;
            this.enable3d = enable3d;
        }

        public static ChartType from(String name) {
            for (ChartType chartType : ChartType.values()) {
//                if (chartType.getType().equals(name)) {
                if (chartType.getName().equals(name)) {
                    return chartType;
                }
            }
            return UNKNOWN;
        }
    }

    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManager", rollbackFor = Exception.class)
    public Object getExportData(DataService ds, AnalysisDao dao, int chartId, int exportId, int userId, boolean isJsonString, Map<String, String> variableMap, boolean isChart, List<String> headerList) throws Exception {
        Object result;
        try {
            List<ChartInfo> chartInfoList = dao.getChartInfo();
            AnalysisChart chart = dao.getAnalysisChartId(String.valueOf(chartId));
            String analysisId = String.valueOf(chart.getAnalysisId());
            List<AnalysisChart> chartList = new ArrayList<>();
            chartList.add(chart);
            ArrayList<String> columnList = new ArrayList<>();
            Analysis analysis = dao.getAnalysisId(analysisId);
            List<Map<String, Object>> schemaData = null;
            ArrayList<String[]> columnData = new ArrayList<>();
            if (analysis.getDataTable() != null) {
                schemaData = dao.getColumnInfo(analysis.getDataTable());
            } else {
                setAnalysisColumnData(dao, analysisId, analysis, columnData, variableMap);
            }
            setColumnInfo(ds, schemaData, columnData, columnList, chartList);

            String column = String.join(",", columnList);
            if (headerList != null) {
                String[] headers = column.replaceAll("`", "").split(",");
                headerList.addAll(Arrays.asList(headers));
            }

            List<Map<String, Object>> analysisData;
            if (isChart) {
                analysisData = getAnalysisData(ds, dao, analysisId, column, variableMap);
                result = getChartData(null, chartInfoList, chart, columnData, analysisData, null, null);
            } else {
                analysisData = getExportCsvData(ds, dao, analysisId, column, variableMap);
                result = analysisData;
            }
        } catch (BadException e) {
            throw e;
        } catch (EmptyResultDataAccessException e) {
            throw new EmptyResultDataAccessException(0);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("getExportChartData error: {}", e.getMessage());
            InternalException ie = new InternalException(e);
            ie.setMessage("분석작업 차트 데이터 가져오기에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
        }
        if (isJsonString) {
            return objectMapper.writeValueAsString(result);
        } else {
            return result;
        }
    }

    private List<Map<String, Object>> getAnalysisData(DataService ds, AnalysisDao dao, String analysisId, String column, Map<String, String> variableMap) throws Exception {
        Analysis analysis = dao.getAnalysisId(analysisId);
        List<Filter> analysisFilterList = dao.getAnalysisFilter(analysisId);
        List<AnalysisFilterGroup> filterGroup = dao.getAnalysisFilterGroup(analysisId);
        for (AnalysisFilterGroup fg : filterGroup) {
            fg.addFilter(analysisFilterList);
        }
        String analysisWhere = ds.getWhereStrFromDB(filterGroup);
        String query = null;
        if (analysis.getQueryId() != null) {
            String queryText = dao.getQueryTextFromAnalysisId(analysisId);
            List<Map<String, Object>> tableList = dao.selectDataSetTables(Util.getDataSetList(queryText));
            List<Map<String, Object>> variableList = null;
            List<Object> variableInfo = analysis.getVariableInfo();
            if (variableInfo != null) {
                variableList = new ArrayList<>();
                for (Object obj : variableInfo) {
                    variableList.add((Map<String, Object>) obj);
                }
            }
            if ("".equals(column)) column = "*";
            queryText = Util.getQueryText(queryText, tableList, variableList, variableMap);
            //query = String.format("SELECT %s FROM (%s) A", column, queryText);
            query = queryText;
            if (analysisWhere != null) {
                //analysisWhere = "('a' = 'a')";
                Expression whereExp = CCJSqlParserUtil.parseCondExpression(analysisWhere);
                Statement stmt = CCJSqlParserUtil.parse(query);
                Select selectStatement = (Select) stmt;
                SQLParser parser = new SQLParser(whereExp);
                selectStatement.getSelectBody().accept(parser);
                // deparse
                StatementDeParser deParser = new StatementDeParser(new StringBuilder());
                stmt.accept(deParser);
                query = deParser.getBuffer().toString();
            }
        } else {
            query = String.format("SELECT %s FROM %s", column, analysis.getDataTable());
        }

        if (column != null && !"".equals(column.trim())) {
            List<String> columnList = Arrays.stream(column.split(","))
                    .map(value -> value.replace("`", ""))
                    .collect(Collectors.toList());
            List<Map<String, Object>> dataList = dao.getDataSetTable(query);
            List<Map<String, Object>> newDataList = new ArrayList<>();
            for (Map<String, Object> map : dataList) {
                Map<String, Object> temp = new HashMap<>();
                for (String key : columnList) {
                    if (map.containsKey(key)) {
                        temp.put(key, map.get(key));
                    }
                }
                newDataList.add(temp);
            }
            return newDataList;
        }
        return new ArrayList<>();
    }

    private List<Map<String, Object>> getExportCsvData(DataService ds, AnalysisDao dao, String analysisId, String column, Map<String, String> variableMap) throws Exception {
        Analysis analysis = dao.getAnalysisId(analysisId);
        List<Filter> analysisFilterList = dao.getAnalysisFilter(analysisId);
        List<AnalysisFilterGroup> filterGroup = dao.getAnalysisFilterGroup(analysisId);
        for (AnalysisFilterGroup fg : filterGroup) {
            fg.addFilter(analysisFilterList);
        }
        String query = null;
        if (analysis.getQueryId() != null) {
            String queryText = dao.getQueryTextFromAnalysisId(analysisId);
            List<Map<String, Object>> tableList = dao.selectDataSetTables(Util.getDataSetList(queryText));
            List<Map<String, Object>> variableList = null;
            List<Object> variableInfo = analysis.getVariableInfo();
            if (variableInfo != null) {
                variableList = new ArrayList<>();
                for (Object obj : variableInfo) {
                    variableList.add((Map<String, Object>) obj);
                }
            }
            if ("".equals(column)) column = "*";
            queryText = Util.getQueryText(queryText, tableList, variableList, variableMap);
            //query = String.format("SELECT %s FROM (%s) A", column, queryText);
            query = String.format(queryText);
        } else {
            query = String.format("SELECT %s FROM %s", column, analysis.getDataTable());
        }

        if (column != null && !"".equals(column.trim())) {
            return dao.getDataSetTable(query);
        }
        return new ArrayList<>();
    }

    public List<AnalysisChart> getAnalysisChartDataMap(DataService ds, AnalysisDao dao, String analysisId, Map<String, String> variableMap, String userId) throws Exception {
        long time = System.currentTimeMillis();
        List<AnalysisChart> chartList = dao.getAnalysisChart(analysisId);
        ArrayList<String> columnList = new ArrayList<>();
        Analysis analysis = dao.getAnalysisId(analysisId);
        List<Map<String, Object>> schemaData = null;
        ArrayList<String[]> columnData = new ArrayList<>();
        if (analysis.getDataTable() != null) {
            schemaData = dao.getColumnInfo(analysis.getDataTable());
        } else {
            setAnalysisColumnData(dao, analysisId, analysis, columnData, variableMap);
        }
        setColumnInfo(ds, schemaData, columnData, columnList, chartList);
        String column = String.join(",", columnList);
        List<Map<String, Object>> analysisData = getAnalysisData(ds, dao, analysisId, column, variableMap);
        List<ChartInfo> chartInfoList = dao.getChartInfo();
        for (AnalysisChart chart : chartList) {
            Object option = getChartData(dao, chartInfoList, chart, columnData, analysisData, analysisId, userId);
            chart.setOption((Map<String, Object>) option);
//            chart.setType((String) ((Map) option).get("type"));
        }
        time = System.currentTimeMillis() - time;
        log.info("getAnalysisChartDataMap duration: {} ms", time);
        return chartList;
    }

    public Object getAnalysisChartData(DataService ds, AnalysisDao dao, String analysisId, AnalysisChart chart, Map<String, String> variableMap, String userId) throws Exception {
        long time = System.currentTimeMillis();
        Analysis analysis = dao.getAnalysisId(analysisId);
        List<Map<String, Object>> schemaData = null;
        ArrayList<String[]> columnData = new ArrayList<>();
        ArrayList<String> columnList = new ArrayList<>();
        if (analysis.getDataTable() != null) {
            schemaData = dao.getColumnInfo(analysis.getDataTable());
        } else {
            setAnalysisColumnData(dao, analysisId, analysis, columnData, variableMap);
        }
        List<Chart> chartList = new ArrayList<>();
        chartList.add(chart);
        setColumnInfo(ds, schemaData, columnData, columnList, chartList);
        String column = String.join(",", columnList);
        List<Map<String, Object>> analysisData = new ArrayList<>();
        if (!"".equals(column.trim())) {
            analysisData = getAnalysisData(ds, dao, analysisId, column, variableMap);
        }
        List<ChartInfo> chartInfoList = dao.getChartInfo();
        Object data = getChartData(dao, chartInfoList, chart, columnData, analysisData, analysisId, userId);
        time = System.currentTimeMillis() - time;
        log.info("getAnalysisChartData duration: {} ms", time);
        return data;
    }

    private void setAnalysisColumnData(AnalysisDao dao, String analysisId, Analysis analysis, ArrayList<String[]> columnData, Map<String, String> variableMap) throws Exception {
        String queryText = dao.getQueryTextFromAnalysisId(analysisId);
        List<Map<String, Object>> tableList = dao.selectDataSetTables(Util.getDataSetList(queryText));
        List<Map<String, Object>> variableList = null;
        List<Object> variableInfo = analysis.getVariableInfo();
        if (variableInfo != null) {
            variableList = new ArrayList<>();
            for (Object obj : variableInfo) {
                variableList.add((Map<String, Object>) obj);
            }
        }
        queryText = Util.getQueryText(queryText, tableList, variableList, variableMap);
        List<Map<String, Object>> dataSetTableList = dao.getDataSetTable(queryText);
        columnData.addAll(Util.getColumnInfo(dataSetTableList));
    }

    public void setColumnInfo(DataService ds, List<Map<String, Object>> schemaData, ArrayList<String[]> columnData, ArrayList<String> columnList, List chartList) {
        if (schemaData != null) {
            for (Map<String, Object> schema : schemaData) {
                columnData.add(new String[]{schema.get("Field").toString(), ds.getType(schema.get("Type"))});
            }
        }

        // schemaData
//        [
//            "Field": "CATEGORY_NM",
        //        "Type":"text",
        //        "Null":"YES",
        //        "Key":"",
        //        "Default":null,
        //        "Extra":""
//        ]


        // column Data
//        [
//            [
//                "MODEL_NM", "STRING"
//                ]
//            [
//                "MODEL_NM", "STRING"
//                ]
//            [
//                "MODEL_NM", "STRING"
//            ]
//        ]

        // column list
        // `MODEL_NM`

        for (int i = 0; i < chartList.size(); ++i) {
            Chart chart = (Chart) chartList.get(i);
            if (chart.getValue() != null) {
                String[] columns = chart.getValue().split(",");
                for (String column : columns) {
                    column = String.format("`%s`", column);
                    if (!columnList.contains(column)) {
                        columnList.add(column);
                    }
                }
            }
            String[] columnInfo = new String[]{chart.getAxis(), chart.getGroup(), chart.getLine()};
            for (String info : columnInfo) {
                if (info != null) {
                    if (info.indexOf(",") != -1) {
                        String column = String.format("CONCAT(`%s`) AS `%s`", info.replaceAll(",", "`,'-',`"), info);
                        if (!columnList.contains(column)) {
                            columnList.add(column);
                            columnData.add(new String[]{info, "STRING"});
                        }
                    } else {
                        String column = String.format("`%s`", info);
                        if (!columnList.contains(column)) {
                            columnList.add(column);
                        }
                    }
                }
            }
        }
    }

    public List<DashboardChart> getDashboardChartDataMap(DataService ds, DashboardDao dao, String dashboardId) throws Exception {
        long time = System.currentTimeMillis();
        List<ChartInfo> chartInfoList = dao.getChartInfo();
        List<DashboardChart> chartList = dao.getDashboardChartList(dashboardId);
        List<DashboardDataSet> dataSets = dao.getDashboardDataSetList(dashboardId);
        Map<String, List<DashboardChart>> chartMap = new HashMap<>();
        Map<Integer, List<DashboardChart>> queryChartMap = new HashMap<>();
        Map<Integer, ArrayList<String[]>> queryChartColumnDataMap = new HashMap<>();
        Map<Integer, String> queryTextMap = new HashMap<>();
        Map<Integer, String> dataSetWhere = new HashMap<>();
        for (DashboardChart chart : chartList) {
            for (DashboardDataSet dataSet : dataSets) {
                if (dataSet.getId().equals(chart.getDashboardDataId())) {
                    List<DashboardChart> list = null;
                    String table = dataSet.getDataTable();
                    if (table == null) {
                        Integer queryId = dataSet.getQueryId();
                        list = queryChartMap.get(queryId);
                        if (list == null) {
                            list = new ArrayList<>();
                            queryChartMap.put(queryId, list);
                        }
                        if (!queryChartColumnDataMap.containsKey(queryId)) {
                            ArrayList<String[]> columnData = new ArrayList<>();
                            String queryText = dao.getQueryTextFromQueryId(String.valueOf(queryId));
                            List<Map<String, Object>> tableList = dao.selectDataSetTables(Util.getDataSetList(queryText));
                            List<Map<String, Object>> variableList = null;
                            List<Object> variableInfo = dataSet.getVariableInfo();
                            if (variableInfo != null) {
                                variableList = new ArrayList<>();
                                for (Object obj : variableInfo) {
                                    variableList.add((Map<String, Object>) obj);
                                }
                            }
                            queryText = Util.getQueryText(queryText, tableList, variableList, null);
                            List<Map<String, Object>> dataSetTableList = dao.getDataSetTable(queryText);
                            columnData.addAll(Util.getColumnInfo(dataSetTableList));
                            queryTextMap.put(queryId, queryText);
                            queryChartColumnDataMap.put(queryId, columnData);
                        }
                        List<Filter> analysisFilterList = dao.getDashboardFilter(dataSet.getId(), queryId);
                        List<DashboardFilterGroup> filterGroup = dao.getDashboardFilterGroup(dataSet.getId(), queryId);
                        for (DashboardFilterGroup fg : filterGroup) {
                            fg.addFilter(analysisFilterList);
                        }
                        dataSetWhere.put(dataSet.getId(), ds.getWhereStrFromDB(filterGroup));
                    } else {
                        list = chartMap.get(table);
                        if (list == null) {
                            list = new ArrayList<>();
                            chartMap.put(table, list);
                        }
                    }
                    list.add(chart);
                    break;
                }
            }
        }

        for (String table : chartMap.keySet()) {
            List<Map<String, Object>> schemaData = dao.getColumnInfo(table);
            ArrayList<String[]> columnData = new ArrayList<>();
            ArrayList<String> columnList = new ArrayList<>();
            List<DashboardChart> cList = chartMap.get(table);
            setColumnInfo(ds, schemaData, columnData, columnList, cList);
            String column = String.join(",", columnList);
            List<Map<String, Object>> dashboardData = dao.getDataTableData(table, column);
            for (DashboardChart chart : cList) {
                Object option = getChartData(null, chartInfoList, chart, columnData, dashboardData, null, null);
                chart.setOption((Map<String, Object>) option);
                chart.setType((String) ((Map) option).get("type"));
            }
        }
        for (Integer queryId : queryChartMap.keySet()) {
            ArrayList<String[]> columnData = queryChartColumnDataMap.get(queryId);
            ArrayList<String> columnList = new ArrayList<>();
            List<DashboardChart> cList = queryChartMap.get(queryId);
            setColumnInfo(ds, null, columnData, columnList, cList);
            String column = String.join(",", columnList);
            for (DashboardChart chart : cList) {
                String queryText = queryTextMap.get(queryId);
                String where = dataSetWhere.get(chart.getDashboardDataId());
                if (where == null) {
                    where = "";
                } else {
                    where = String.format("WHERE %s", where);
                }
                queryText = String.format("SELECT %s FROM (%s) A %s", column, queryText, where);
                List<Map<String, Object>> dashboardData = dao.getDataSetTable(queryText);
                Object option = getChartData(null, chartInfoList, chart, columnData, dashboardData, null, null);
                chart.setOption((Map<String, Object>) option);
                chart.setType((String) ((Map) option).get("type"));
            }
        }
        time = System.currentTimeMillis() - time;
        log.info("getDashboardChartDataMap duration: {} ms", time);
        return chartList;
    }

    private Object getChartData(AnalysisDao dao, List<ChartInfo> chartInfoList, Chart chart, ArrayList<String[]> columnData, List<Map<String, Object>> analysisData, String analysisId, String userId) {
        chart.setData(new HashMap<>());
        ChartType chartType = null;

        for (ChartInfo info : chartInfoList) {
            if (info.getType().equals(chart.getType())) {
                chartType = ChartType.from(info.getType());
                break;
            }
        }

        Map<String, Object> fieldColumn = new HashMap<>();
        List<Map<String, Object>> fieldValueData = new ArrayList<>();
        fieldColumn.put("value", fieldValueData);
        String[] fieldName = new String[]{"axis", "group", "line", "value"};
        String[] fieldValue = new String[]{chart.getValueInfo().getAxis(), chart.getValueInfo().getGroup(), chart.getValueInfo().getLine(),
                chart.getValueInfo().getValue()};
        for (int i = 0; i < fieldName.length; ++i) {
            if (fieldValue[i] != null) {
                if ("value".equals(fieldName[i])) {
                    String[] fieldValues = fieldValue[i].split(",");
                    for (int j = 0; j < fieldValues.length; ++j) {
                        fieldValueData.add(getFieldColumnData(fieldValues[j], getColumnType(columnData, fieldValues[j]), true));
                    }
                } else {
                    fieldColumn.put(fieldName[i], getFieldColumnData(fieldValue[i], getColumnType(columnData, fieldValue[i]), true));
                }
            }
        }

        switch (chartType) {
            case PIE:
                return createPieChartData(dao, chart, chartType, fieldColumn, analysisData, analysisId, userId);
            case DONUT:
            case PIE_3D:
            case DONUT_3D:
            case FUNNEL:
            case FUNNEL_3D:
            case WORD_CLOUD:
                return createGroupOneValueChartData(dao, chart, chartType, fieldColumn, analysisData, analysisId, userId);
            case LINE:
            case LINE_STACKED:
            case LINE_STACKED_100:
            case AREA:
            case AREA_STACKED:
            case AREA_CLUSTERED:
            case AREA_STACKED_100:
            case BAR:
            case BAR_STACKED:
            case BAR_CLUSTERED:
            case BAR_STACKED_100:
            case BAR_CLUSTERED_LINE:
            case COLUMN_STACKED:
            case COLUMN_CLUSTERED:
            case COLUMN_STACKED_100:
            case COLUMN_CLUSTERED_LINE:
            case COLUMN:
                return createAxisValueChartDataV2(dao, chart, chartType, fieldColumn, analysisData, analysisId, userId);
            case RADAR:
                return createGroupValueChartData(dao, chart, chartType, fieldColumn, analysisData, analysisId, userId); // amchart
            case POLAR:
                return createTwoValueAxesInPolarChartData(chart, chartType, fieldColumn, analysisData); // for echart
            case TABLE:
                return createTableValuesChartData(chart, chartType, fieldColumn, analysisData);
            case TABLE_GROUPED:
                return createTableValueChartData(chart, chartType, fieldColumn, analysisData);
        }

        return null;
    }

    private Map<String, Object> getFieldColumnData(String name, String type, boolean check) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("type", type);
        data.put("check", check);
        return data;
    }

    private String getColumnType(ArrayList<String[]> column, String name) {
        String type = "STRING";
        for (int i = 0; i < column.size(); ++i) {
            if (column.get(i)[0].equals(name)) {
                type = column.get(i)[1];
                break;
            }
        }
        return type;
    }

    private Object createPieChartData(AnalysisDao dao, Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData, String analysisId, String userId) {
        Map<String, Object> chartData = chart.getData();
        List<String> graphs = new ArrayList<>();
        graphs.add("count");
        chartData.put("graphs", graphs);
        chartData.put("category", "category");
        List<Map<String, Object>> dataProvider = new ArrayList<>();
        Map<String, Object> fieldColumnGroup = (Map<String, Object>) fieldColumn.get("group");
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
        Map<String, Object> xAxis = new HashMap<>();
        Map<String, Object> yAxis = new HashMap<>();
        List<Map<String, Object>> series = new ArrayList<>();
        String eChartType = chart.getType().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> getQueryList = new ArrayList<>();
        Map<String, Object> type = new HashMap<>();

        if (fieldColumnGroup != null) {
            Map<String, Object> data = new HashMap<>();
            for (Map<String, Object> columnData : analysisData) {
                String name = null;
                String group = null;
                Object value = null;
                Map<String, Object> firstValue = null;
                if (valueList.size() > 0) {
                    name = String.valueOf(valueList.get(0).get("name"));
                    group = String.valueOf(columnData.get(fieldColumnGroup.get("name")));
                    value = columnData.get(name);
                    firstValue = valueList.get(0);
                } else {
                    name = String.valueOf(columnData.get(fieldColumnGroup.get("name")));
                }
                String flag = chart.getValueInfo().getFlag();
                setData(data, firstValue, name, group, value, chartType, flag);
            }

            try {
                getQueryList = dao.getColumnGroupBy(chart.getValueInfo().getGroup(), dao.getDataTable(userId, "ANALYSIS", analysisId));
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<String> tempList = new ArrayList<>();
            for (Map<String, Object> temp : getQueryList) {
//                tempList.add(temp.get(axis.get("name")).toString());
            }
            xAxis.put("type", "category");
            xAxis.put("data", tempList);
            if (chartType.getType().equals("area")) {
                eChartType = "line";
                xAxis.put("boundaryGap", false);
                type.put("areaStyle", "{}");
            }
            yAxis.put("type", "value");

            List<Long> seriesData = new ArrayList<>();
            List<Object> seriesName = new ArrayList<>();
            for (Map<String, Object> temp : getQueryList) {
                seriesData.add((Long) temp.get("cnt"));
                seriesName.add(temp.get(chart.getValueInfo().getGroup()));
            }

            type.put("type", eChartType);
            type.put("value", seriesData);
            type.put("name", seriesName);
            series.add(type);
        }
        makeChart(chartData, chart, series);
        return chartData.get("option");
    }

    private Object createTableValueChartData(Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData) {
        Map<String, Object> chartData = chart.getData();
        Map<String, Object> fieldColumnGroup = (Map<String, Object>) fieldColumn.get("group");
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
        List<Map<String, Object>> dataProvider = new ArrayList<>();

        if ("group".equals(chartType.getShape())) {
            if (fieldColumnGroup != null) {
                Map<String, Object> data = new HashMap<>();
                for (Map<String, Object> columnData : analysisData) {
                    String dataName = String.valueOf(columnData.get(fieldColumnGroup.get("name")));
                    String type = null;
                    Object value = null;
                    if (valueList.size() > 0) {
                        type = String.valueOf(valueList.get(0).get("type"));
                        value = columnData.get(valueList.get(0).get("name"));
                    } else {
                        type = "INTEGER";
                        value = 1;
                    }
                    String flag = chart.getValueInfo().getFlag() == null ? "Count" : chart.getValueInfo().getFlag();
                    setDataNameType(data, dataName, type, value, flag);
                }
            }
        } else {
            if (valueList.size() > 0) {
                Map<String, Object> data = new HashMap<>();
                for (Map<String, Object> columnData : analysisData) {
                    data.put(String.valueOf(columnData.get(valueList.get(0).get("name"))), columnData);
                }
                List<String> keySet = new ArrayList<>();
                keySet.addAll(data.keySet());
                int limitCnt = 100;
                for (int i = 0; i < keySet.size() && i < limitCnt; ++i) {
                    dataProvider.add((Map<String, Object>) data.get(keySet.get(i)));
                }
            }
        }
        makeChart(chartData, chart, null);
        Map<String, Object> option = (Map<String, Object>) chartData.get("option");
        option.put("type", chartType.name);
        return option;
    }

    private Object createTableValuesChartData(Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData) {
        Map<String, Object> chartData = chart.getData();
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
        List<Map<String, Object>> dataProvider = new ArrayList<>();
        if (valueList.size() > 0) {
            List<String> keySet = new ArrayList<>();
            int limitCnt = 100;
            for (int i = 0; i < analysisData.size() && i < limitCnt; ++i) {
                dataProvider.add(analysisData.get(i));
            }
        }
        makeChart(chartData, chart, null);
        Map<String, Object> option = (Map<String, Object>) chartData.get("option");
        option.put("type", chartType.name);
        return option;
    }

    private Object createGroupValueChartData(AnalysisDao dao, Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData, String analysisId, String userId) {
        Map<String, Object> chartData = chart.getData();

        List<String> graphs = new ArrayList<>();
        chartData.put("graphs", graphs);
        chartData.put("category", "category");
        List<Map<String, Object>> dataProvider = new ArrayList<>();
        Map<String, Object> fieldColumnGroup = (Map<String, Object>) fieldColumn.get("group");
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
        Map<String, Object> options = (Map<String, Object>) chart.getOption();
        Map<String, Object> radar = (Map<String, Object>) options.get("radar");
//        List<Object> indicator = (List<Object>)radar.get("indicator");
        List<Map<String, Object>> indicator = new ArrayList<>();
        Map<String, Object> data = new HashMap<>();
        String eChartType = chart.getType().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> series = new ArrayList<>();

        if (fieldColumnGroup != null) {
            String categoryField = String.valueOf(fieldColumnGroup.get("name"));
            String categoryType = String.valueOf(fieldColumnGroup.get("type"));
            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> category = new HashMap<>();
            for (Map<String, Object> columnData : analysisData) {
                String categoryName = String.valueOf(columnData.get(categoryField));
                if (!category.containsKey(categoryName)) {
                    category.put(categoryName, 0);
                }
                category.put(categoryName, 1 + getLong(category.get(categoryName)));
                for (Map<String, Object> value : valueList) {
                    String flag = chart.getValueInfo().getFlag() == null ? "Count" : chart.getValueInfo().getFlag();
                    setDataMap(dataMap, categoryName, null, value, columnData, null, flag);
                }

            }
            List<Map<String, Object>> getQueryList = new ArrayList<>();

            try {
                getQueryList = dao.getColumnGroupBy(chart.getGroup(), dao.getDataTable(userId, "ANALYSIS", analysisId));
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<Map<String, Object>> indicatorList = new ArrayList<>();
            List<Map<String, Object>> seriesList = new ArrayList<>();
            List<Long> list = new ArrayList<>();

            for (Map<String, Object> temp : getQueryList) {
                Map<String, Object> indicatorData = new HashMap<>();
//                Map<String, Object> seriesData = new HashMap<>();
                indicatorData.put("name", temp.get(
                        Optional.ofNullable(fieldColumnGroup.get("name")).orElse(""))
                );
                list.add((Long) temp.get("cnt"));
                indicatorList.add(indicatorData);
//                seriesList.add(seriesData);
            }

            Map<String, Object> map = new HashMap<>();
            map.put("value", list);
            seriesList.add(0, map);
            indicator.addAll(indicatorList);
            series.addAll(Collections.singletonList(map));

            options.remove("series");
            radar.remove("indicator");
            radar.put("indicator", indicator);
            radar.remove("data");           // ??
            data.put("type", eChartType);

            series.addAll(Collections.singletonList(data));
            Map<String, Object> obj = new HashMap<>();
            obj.put("data", seriesList);
            obj.put("type", chart.getType().toLowerCase(Locale.ROOT));

            options.put("series", Collections.singletonList(obj));
        }
        makeChart(chartData, chart, null);
        return chartData.get("option");
    }

    private Object createGroupOneValueChartData(AnalysisDao dao, Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData, String analysisId, String userId) {
        Map<String, Object> chartData = chart.getData();
        List<String> graphs = new ArrayList<>();
        graphs.add("count");
        chartData.put("graphs", graphs);
        chartData.put("category", "category");
        Map<String, Object> fieldColumnGroup = (Map<String, Object>) fieldColumn.get("group");
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
        List<Map<String, Object>> series = new ArrayList<>();
        String eChartType = chart.getType().toLowerCase(Locale.ROOT);
        List<Map<String, Object>> getQueryList = new ArrayList<>();
//        Map options = null;
//        try {
//            if(chart.getOption() instanceof String){
//                options = objectMapper.readValue((String) chart.getOption(), Map.class);
//            } else {
        Map<String, Object> options = (Map<String, Object>) chart.getOption();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Map<String, Object> options = (Map<String, Object>) chart.getOption();

        if (fieldColumnGroup != null) {
            Map<String, Object> data = new HashMap<>();
            for (Map<String, Object> columnData : analysisData) {
                String name;
                String group = null;
                Object value = null;
                Map<String, Object> firstValue = null;
                if (valueList.size() > 0) {
                    name = String.valueOf(valueList.get(0).get("name"));
                    group = String.valueOf(columnData.get(fieldColumnGroup.get("name")));
                    value = columnData.get(name);
                    firstValue = valueList.get(0);
                } else {
                    name = String.valueOf(columnData.get(fieldColumnGroup.get("name")));
                }
                String flag = chart.getValueInfo().getFlag();
                setData(data, firstValue, name, group, value, chartType, flag);
            }

            try {
                getQueryList = dao.getColumnGroupBy(chart.getGroup(), dao.getDataTable(userId, "ANALYSIS", analysisId));
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<Map<String, Object>> mapList = new ArrayList<>();

            for (Map<String, Object> temp : getQueryList) {
                Map<String, Object> tempData = new HashMap<>();
                tempData.put("value", temp.get("cnt"));
                tempData.put("name", temp.get(
                        Optional.ofNullable(fieldColumnGroup.get("name")).orElse(""))

                );
                mapList.add(tempData);
            }

            options.remove("series");
            data.put("data", mapList);
            data.put("type", eChartType);

            series.addAll(Collections.singletonList(data));
            options.put("series", series);
        }
        makeChart(chartData, chart, null);
        return chartData.get("option");
    }

    private void setData(Map<String, Object> data, Map<String, Object> value, String name, String group, Object dataValue, ChartType chartType, String flag) {
        String dataName = name;
        String type = null;
        if (value != null) {
            type = String.valueOf(value.get("type"));
        }
        if (flag == null) {
            if ("INTEGER".equals(type) || "FLOAT".equals(type)) {
                flag = "SUM";
            } else {
                flag = "COUNT";
            }
        }
        if (group != null) {
            if (chartType != ChartType.WORD_CLOUD) {
                /*if ("SUM".equals(flag.toUpperCase())) {
                    dataName = String.format("%s, sum(%s)", group, name);
                } else if ("COUNT".equals(flag.toUpperCase())) {
                    dataName = String.format("%s, count(%s)", group, name);
                } else if ("MIN".equals(flag.toUpperCase())) {
                    dataName = String.format("%s, min(%s)", group, name);
                } else if ("MAX".equals(flag.toUpperCase())) {
                    dataName = String.format("%s, max(%s)", group, name);
                } else if ("DISTINCT".equals(flag.toUpperCase())) {
                    dataName = String.format("%s, distinct(%s)", group, name);
                }*/
                dataName = group;
            } else {
                dataName = group;
            }
        }
        setDataNameType(data, dataName, type, dataValue, flag);
    }

    private List<Map<String, Object>> getCategorySubData(List<Map<String, Object>> dataList, String field) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> data : dataList) {
            String key = String.valueOf(data.get("category"));
            Map<String, Object> item = new HashMap<>();
            item.put("category", key);
            item.put(field, data.get(key));
            if (data.get("subData") != null) {
                item.put("subData", getCategorySubData((List<Map<String, Object>>) data.get("subData"), field));
            }
            result.add(item);
        }
        return result;
    }

    private Map<String, Object> getSubData(Map<String, Object> drillDataMap, Map<String, Object> drillCategoryMap, Map<String, Object> drillKeyMap, String[] formats) {
        for (int i = formats.length - 1; i > 0; --i) {
            List<String> drillMainKey = (List<String>) drillKeyMap.get(formats[i - 1]);
            List<String> drillSubKey = (List<String>) drillKeyMap.get(formats[i]);
            if (drillMainKey != null && drillSubKey != null) {
                for (String subKey : drillSubKey) {
                    ((Map<String, Object>) drillDataMap.get(subKey)).put("category", subKey);
                    for (String mainKey : drillMainKey) {
                        ((Map<String, Object>) drillDataMap.get(mainKey)).put("category", mainKey);
                        List subData = (List) ((Map<String, Object>) drillDataMap.get(mainKey)).get("subData");
                        if (subData == null) {
                            subData = new ArrayList();
                            ((Map<String, Object>) drillDataMap.get(mainKey)).put("subData", subData);
                        }
                        List categorySubData = (List) ((Map<String, Object>) drillCategoryMap.get(mainKey)).get("subData");
                        if (categorySubData == null) {
                            categorySubData = new ArrayList();
                            ((Map<String, Object>) drillCategoryMap.get(mainKey)).put("subData", categorySubData);
                        }
                        if (subKey.substring(0, mainKey.length()).equals(mainKey)) {
                            subData.add(drillDataMap.get(subKey));
                            categorySubData.add(drillCategoryMap.get(subKey));
                        }
                    }
                }
            }
        }
        Map<String, Object> result = new HashMap<>();
        List<String> keys = (List<String>) drillKeyMap.get(formats[0]);
        if (keys != null) {
            Map<String, Object> data = new HashMap<>();
            Map<String, Object> category = new HashMap<>();
            result.put("data", data);
            result.put("category", category);
            for (String key : keys) {
                data.put(key, drillDataMap.get(key));
                category.put(key, drillCategoryMap.get(key));
            }
        }
        return result;
    }

    private List<String> getDrillCategoryName(Date value, String[] formats, Map<String, SimpleDateFormat> formatterMap) {
        List<String> result = new ArrayList<>();
        for (String format : formats) {
            SimpleDateFormat formatter = formatterMap.get(format);
            result.add(formatter.format(value));
        }
        return result;
    }

    private Object createAxisValueChartData(AnalysisDao dao, Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData, String analysisId, String userId) {
        Map<String, Object> chartData = chart.getData();
        chartData.put("graphs", new ArrayList<String>());
        chartData.put("lines", new ArrayList<String>());
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> axis = (Map<String, Object>) fieldColumn.get("axis");
        Map<String, Object> group = (Map<String, Object>) fieldColumn.get("group");
        Map<String, Object> line = (Map<String, Object>) fieldColumn.get("line");
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
        Map<String, Object> xAxis = new HashMap<>();
        Map<String, Object> yAxis = new HashMap<>();
        Map<String, Object> options = (Map<String, Object>) chart.getOption();
        Map<String, Object> data = new HashMap<>();

        if (chart.getType().equals("AREA")) {
            xAxis.put("type", "category");
            xAxis.put("boundaryGap", false);
            yAxis.put("type", "value");
            options.put("xAxis", xAxis);
            options.put("yAxis", yAxis);
            data.put("areaStyle", "{}");
        } else {
            xAxis = (Map<String, Object>) options.get("xAxis");
            yAxis = (Map<String, Object>) options.get("yAxis");
        }

//        List<Map<String, Object>> originSeries = (List<Map<String, Object>>) options.get("series");
//
//        for (Map.Entry entry : originSeries.get(0).entrySet()) {
//            data.put(entry.getKey().toString(), entry.getValue());
//        }

        List<Map<String, Object>> series = new ArrayList<>();

        if (axis != null) {
            List<Map<String, Object>> getQueryList = new ArrayList<>();

            try {
                getQueryList = dao.getColumnGroupBy(chart.getValueInfo().getAxis(), dao.getDataTable(userId, "ANALYSIS", analysisId));
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<String> tempList = new ArrayList<>();
            for (Map<String, Object> temp : getQueryList) {
                tempList.add(temp.get(axis.get("name")).toString());
            }

            if (chart.getType().equals("COLUMN")) {
                yAxis.put("data", tempList);
                data.put("type", "bar");
            } else if (chart.getType().equals("AREA")) {
                data.put("type", "line");
            } else {
                xAxis.put("data", tempList);
                data.put("type", chart.getType().toLowerCase(Locale.ROOT));
            }

            List<Long> seriesData = new ArrayList<>();
            for (Map<String, Object> temp : getQueryList) {
                seriesData.add((Long)temp.get("cnt"));
            }
            options.remove("series");
            data.put("data", seriesData);

            series.addAll(Collections.singletonList(data));
            options.put("series", series);
        }
        makeChart(chartData, chart, null);
        return chartData.get("option");
    }

    private Object createAxisValueChartDataV2(AnalysisDao dao, Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData, String analysisId, String userId) {
        OptionHelper optionHelper = OptionHelper.of((Map<String, Object>) chart.getOption(), chart.getType());

        Map<String, Object> chartData = chart.getData();
        chartData.put("graphs", new ArrayList<String>());
        chartData.put("lines", new ArrayList<String>());
        Map<String, Object> dataMap = new HashMap<>();
        Map<String, Object> axis = (Map<String, Object>) fieldColumn.get("axis");
        Map<String, Object> group = (Map<String, Object>) fieldColumn.get("group");
        Map<String, Object> line = (Map<String, Object>) fieldColumn.get("line");
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
//        Map<String, Object> xAxis = new HashMap<>();
//        Map<String, Object> yAxis = new HashMap<>();
        Map<String, Object> options = (Map<String, Object>) chart.getOption();
        Map<String, Object> data = new HashMap<>();

        Map<String, Object> xAxis = optionHelper.getXAxis();
        Map<String, Object> yAxis = optionHelper.getYAxis();
        if (chart.getType().equals("AREA")) {
            optionHelper.putWhereThen(Constants.ECHART_XAXIS, "type", "category")
                    .putWhereThen(Constants.ECHART_XAXIS, "boundaryGap", false)
                    .putWhereThen(Constants.ECHART_XAXIS, "type", "value");


            optionHelper.putThen("xAxis", xAxis)
                    .putThen("yAxis", yAxis);
            optionHelper.putWhereThen("option", Constants.ECHART_XAXIS, xAxis)
                    .putWhereThen(Constants.ECHART_OPTION, Constants.ECHART_YAXIS, yAxis);
            data.put("areaStyle", "{}");
        } else {
            xAxis = (Map<String, Object>) options.get("xAxis");
            yAxis = (Map<String, Object>) options.get("yAxis");
        }

//        List<Map<String, Object>> originSeries = (List<Map<String, Object>>) options.get("series");
//
//        for (Map.Entry entry : originSeries.get(0).entrySet()) {
//            data.put(entry.getKey().toString(), entry.getValue());
//        }

        List<Map<String, Object>> series = new ArrayList<>();

        if (axis != null) {
            List<Map<String, Object>> getQueryList = new ArrayList<>();

            try {
                getQueryList = dao.getColumnGroupBy(chart.getValueInfo().getAxis(), dao.getDataTable(userId, "ANALYSIS", analysisId));
            } catch (Exception e) {
                e.printStackTrace();
            }

            List<String> tempList = new ArrayList<>();
            for (Map<String, Object> temp : getQueryList) {
                String nameValue = "";
                if (Objects.nonNull(temp.get(axis.get("name")))) {
                    nameValue = temp.get(axis.get("name")).toString();
                }
                tempList.add(nameValue);
            }

            if (chart.getType().equals("COLUMN")) {
                yAxis.put("data", tempList);
                data.put("type", "bar");
            } else if (chart.getType().equals("AREA")) {
                data.put("type", "line");
            } else {
                xAxis.put("data", tempList);
                data.put("type", chart.getType().toLowerCase(Locale.ROOT));
            }

            List<Long> seriesData = new ArrayList<>();
            for (Map<String, Object> temp : getQueryList) {
                seriesData.add((Long)temp.get("cnt"));
            }
            options.remove("series");
            data.put("data", seriesData);

            series.addAll(Collections.singletonList(data));
            options.put("series", series);
        }
        makeChart(chartData, chart, null);
        return chartData.get("option");
    }

    private String getCategoryValue(Object value, String type) {
        if ("DATE".equals(type)) {
            try {
                return format.format((Date) value);
            } catch (Exception e) {
            }
        }
        return value.toString();
    }

    private void setDataMap(Map<String, Object> dataMap, String categoryName, Map<String, Object> group, Map<String, Object> value, Map<String, Object> columnData, List<String> lines, String flag) {
        Map<String, Object> data = (Map<String, Object>) dataMap.get(categoryName);
        if (data == null) {
            data = new HashMap<>();
            dataMap.put(categoryName, data);
        }
        String dataName = null;
        if (value != null) {
            dataName = String.valueOf(value.get("name"));
            if (group != null) {
                dataName = String.format("%s_%s", columnData.get(group.get("name")), value.get("name"));
            }
        } else {
            if (group != null) {
                dataName = String.valueOf(columnData.get(group.get("name")));
            } else {
                dataName = categoryName;
            }
        }

        if (lines != null && !lines.contains(dataName)) {
            lines.add(dataName);
        }
        String type = null;
        Object dataValue = null;
        if (value == null) {
            type = "INTEGER";
            dataValue = 1;
        } else {
            type = String.valueOf(value.get("type"));
            dataValue = columnData.get(value.get("name"));
        }
        if (flag == null) {
            if ("INTEGER".equals(type) || "FLOAT".equals(type)) {
                flag = "SUM";
            } else {
                flag = "COUNT";
            }
        }
        setDataNameType(data, dataName, type, dataValue, flag);
    }

    private void setDataNameType(Map<String, Object> data, String dataName, String type, Object value, String flag) {
        if (!data.containsKey(dataName)) {
            data.put(dataName, 0);
        }
        if ("SUM".equals(flag.toUpperCase())) {
            if ("INTEGER".equals(type) || "FLOAT".equals(type)) {
                data.put(dataName, getDouble(value) + getDouble(data.get(dataName).toString()));
            } else {
                data.put(dataName, 1 + getLong(data.get(dataName)));
            }
        } else if ("COUNT".equals(flag.toUpperCase())) {
            data.put(dataName, 1 + getLong(data.get(dataName)));
        } else if ("MIN".equals(flag.toUpperCase())) {
            if ("INTEGER".equals(type) || "FLOAT".equals(type)) {
                Double init = getDouble(data.get(dataName));
                Double temp = getDouble(value);
                if (init == 0) {
                    data.put(dataName, temp);
                }
                if (temp < init) {
                    data.put(dataName, temp);
                }
            } else {
                data.put(dataName, 1 + getLong(data.get(dataName)));
            }
        } else if ("MAX".equals(flag.toUpperCase())) {
            if ("INTEGER".equals(type) || "FLOAT".equals(type)) {
                Double init = getDouble(data.get(dataName));
                Double temp = getDouble(value);
                if (temp > init) {
                    data.put(dataName, temp);
                }
            } else {
                data.put(dataName, 1 + getLong(data.get(dataName)));
            }
        } else if ("DISTINCT".equals(flag.toUpperCase())) {
            data.put(dataName, 1);
        }
    }

    private void makeChart(Map<String, Object> chartData, Chart chart, List<Map<String, Object>> series) {
        switch (chart.getType()) {
            case "COLUMN":
            case "BAR":
                makeColumnChart(chartData, chart);
                break;
            case "LINE":
            case "AREA":
                makeLineChart(chartData, chart);
                break;
            case "PIE":
                makePieChart(chartData, chart, series);
                break;
            case "FUNNEL":
                makeFunnelChart(chartData, chart);
                break;
            case "RADAR":
                makeRadarChart(chartData, chart);
                break;
            case "Table":
                makeTableChart(chartData, null);
                break;
        }
    }

    // TODO
    private void makeTableChart(Map<String, Object> chartData, List<Map<String, Object>> dataProvider) {
//        Map<String, Object> option = objectMapper.convertValue(defaultOption.getOption(Option.TABLE), Map.class);
//        option.put("dataProvider", dataProvider);
//        chartData.put("option", option);

        Map<String, Object> option = objectMapper.convertValue(null, Map.class);
        option.put("dataProvider", dataProvider);
        chartData.put("option", option);
    }

    private void makeFunnelChart(Map<String, Object> chartData, Chart chart) {
        chartData.put("option", chart.getOption());
    }

    private void makeRadarChart(Map<String, Object> chartData, Chart chart) {
        chartData.put("option", chart.getOption());
    }

    private void makeLineChart(Map<String, Object> chartData, Chart chart) {
        chartData.put("option", chart.getOption());
    }

    private void makePieChart(Map<String, Object> chartData, Chart chart, List<Map<String, Object>> series) {
        Map<String, Object> option = (Map<String, Object>) chart.getOption();
//        JSONObject object = new JSONObject(chart.getOption());
//        Map<String, Object> option = objectMapper.convertValue(object,Map.class);

//        Map<String, Object> chartOption = (Map<String, Object>) chart.getOption(); // for echart
//        JSONObject object = new JSONObject(chart.getOption());
//        Map option = null;
//        try {
//            if(chart.getOption() instanceof String){
//                option = objectMapper.readValue((String) chart.getOption(), Map.class);
//            } else {
//                option = (Map<String, Object>) chart.getOption();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }


//        Map<String, Object> optionSeries = (Map<String, Object>) option.get("series");
        Map<String, Object> optionSeries = new HashMap<>();
        Map<String, Object> optionLegend = (Map<String, Object>) option.get("legend");
//        Map<String, Object> seriesItemStyle = (Map<String, Object>) optionSeries.get("itemStyle");
//        Map<String, Object> seriesLabel= (Map<String, Object>) optionSeries.get("label");
//        Map<String, Object> seriesLabelLine = (Map<String, Object>) optionSeries.get("labelLine");

        Map<String, Object> seriesInfo = series.get(0);
        List<Object> seriesData = (List<Object>) seriesInfo.get("value");
        List<Object> seriesName = (List<Object>) seriesInfo.get("name");
        List<Map<String, Object>> data = new ArrayList<>();
        for (int i = 0; i < seriesData.size(); i++) {
            Map<String, Object> pieData = new HashMap<>();
            pieData.put("value", seriesData.get(i));
            pieData.put("name", seriesName.get(i));
            data.add(pieData);
        }
        List<Object> legendData = new ArrayList<>();
        for (int i = 0; i < seriesName.size(); i++) {
            legendData.add(seriesName.get(i));
        }
        optionLegend.put("data", legendData);
        option.put("legend", optionLegend);
        option.remove("series");
        optionSeries.put("data", data);
        optionSeries.put("type", "pie");
//        seriesItemStyle.put("opacity", 1);
//        seriesLabel.remove("length");

        option.put("series", optionSeries);
        chartData.put("option", option);
    }

    private void makeColumnChart(Map<String, Object> chartData, Chart chart) {
        chartData.put("option", chart.getOption());
    }

    class AscendingObject implements Comparator<Object> {
        Map<String, Object> dataMap;

        AscendingObject(Map<String, Object> dataMap) {
            this.dataMap = dataMap;
        }

        @Override
        public int compare(Object a, Object b) {
            String dataA = dataMap.get(a) == null ? "" : dataMap.get(a).toString();
            String dataB = dataMap.get(b) == null ? "" : dataMap.get(b).toString();
            try {
                Integer intA = Integer.parseInt(dataA);
                Integer intB = Integer.parseInt(dataB);
                return intB.compareTo(intA);
            } catch (Exception e) {
                try {
                    Double doubleA = getDouble(dataA);
                    Double doubleB = getDouble(dataB);
                    return doubleB.compareTo(doubleA);
                } catch (Exception ee) {
                }
            }
            return dataB.compareTo(dataA);
        }
    }

    private long getLong(Object data) {
        try {
            return Long.parseLong(data.toString());
        } catch (Exception e) {
        }
        return 0;
    }

    private double getDouble(Object data) {
        try {
            return Double.parseDouble(data.toString());
        } catch (Exception e) {
        }
        return 0;
    }

    private String getTitle(String name) {
        if (name.length() > 1) {
            return String.format("%c%s", name.toUpperCase().charAt(0), name.substring(1));
        }
        return name;
    }

    // for echart
    private void makeEchartPolarChart(Map<String, Object> chartData, List<Map<String, Object>> dataProvider, ChartType chartType) {

        Map<String, Object> reqOption = (Map<String, Object>) chartData.get(Constants.ECHART_OPTION);
        List<Map<String, Object>> rawData = (List<Map<String, Object>>) chartData.get(Constants.CHART_DATA);

        PolarTwoValueAxes echartOption = new PolarTwoValueAxes(reqOption, dataProvider);
        Map<String, Object> option = objectMapper.convertValue(echartOption.getChartOption(), Map.class);

        // update legend
        Map<String, Object> legend = (Map<String, Object>) reqOption.get("legend");
        Map<String, Object> resLegend = (Map<String, Object>) option.get("legend");
        legend.put("data", resLegend.get("data"));

        // add polar
        reqOption.put("polar", option.get("polar"));

        // add series
        reqOption.put("series", option.get("series"));

        // update tooltip
        /*"tooltip": {
            "trigger": "axis",
            "axisPointer": {
                "type": "cross"
            }
        }*/
        Map<String, Object> tooltip = (Map<String, Object>) reqOption.get("tooltip");
        tooltip.compute("trigger", (k, v) -> v = "axis");
        tooltip.compute("show", (k, v) -> v = true);
        Map<String, Object> resTooltip = (Map<String, Object>) option.get("tooltip");
        tooltip.put("axisPointer", resTooltip.get("axisPointer"));

        // update angleAxis
        /*"angleAxis": {
            "type": "value",
            "startAngle": 0
        }*/
        Map<String, Object> angleAxis = (Map<String, Object>) reqOption.get("angleAxis");
        Map<String, Object> resAngleAxis = (Map<String, Object>) option.get("angleAxis");
        angleAxis.compute("type", (k, v) -> v = "value");
        angleAxis.put("startAngle", resAngleAxis.get("startAngle"));

        // updata radiusAxis
        // 현재 EchartOption의 값과 같으므로 업데이트 할 필요 없음
        /*"radiusAxis": {
            "type": "category",
            "data": "",
            "axisLine": {
                "show": "true"
            },
            "axisLabel": {
                "rotate": ""
            }
        }*/

        // add animationDuration
        Map<String, Object> resAnimationDuration = (Map<String, Object>) option.get("animationDuration");
        reqOption.put("animationDuration", resAnimationDuration.get("animationDuration"));

        chartData.put("option", reqOption);
    }


    private Object createTwoValueAxesInPolarChartData(Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData) {
        Map<String, Object> chartData = chart.getData();
        Map<String, Object> chartOption = (Map<String, Object>) chart.getOption(); // for echart
//        JSONObject object = new JSONObject(chart.getOption());

//        Map chartOption = null;
//        try {
//            if(chart.getOption() instanceof String){
//                chartOption = objectMapper.readValue((String) chart.getOption(), Map.class);
//            } else {
//                chartOption = (Map<String, Object>) chart.getOption();
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        chartData.put(Constants.ECHART_OPTION, chartOption); // for echart
        chartData.put(Constants.CHART_DATA, analysisData);


        List<Map<String, Object>> dataProvider = new ArrayList<>();
        dataProvider = makeTwoValueAxesInPolarChartData(chart, chartType, fieldColumn, analysisData);

        makeEchartPolarChart(chartData, dataProvider, chartType);
        return chartData.get("option");
    }


    // for polar chart data
    private List<Map<String, Object>> makeTwoValueAxesInPolarChartData(Chart chart, ChartType chartType, Map<String, Object> fieldColumn, List<Map<String, Object>> analysisData) {
        // set data to series.data
        // [[0,0], [0.03, 1], [0.06, 2]

        Map<String, Object> chartData = chart.getData();
//        Map<String, Object> chartOption = chart.getEChartOption(); // for echart

        List<String> graphs = new ArrayList<>();
        chartData.put("graphs", graphs);
        chartData.put("category", "category");

        List<Map<String, Object>> dataProvider = new ArrayList<>();
        Map<String, Object> fieldColumnGroup = (Map<String, Object>) fieldColumn.get("group");
        List<Map<String, Object>> valueList = (List<Map<String, Object>>) fieldColumn.get("value");
        if (fieldColumnGroup != null) {
            String categoryField = String.valueOf(fieldColumnGroup.get("name"));
            String categoryType = String.valueOf(fieldColumnGroup.get("type"));
            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> category = new HashMap<>();
            for (Map<String, Object> columnData : analysisData) {
                String categoryName = String.valueOf(columnData.get(categoryField));
                if (!category.containsKey(categoryName)) {
                    category.put(categoryName, 0);
                }
                category.put(categoryName, 1 + getLong(category.get(categoryName)));
                for (Map<String, Object> value : valueList) {
                    String flag = chart.getValueInfo().getFlag() == null ? "Count" : chart.getValueInfo().getFlag();
                    setDataMap(dataMap, categoryName, null, value, columnData, null, flag);
                }

            }


            List<String> keySet = new ArrayList<>();
            keySet.addAll(category.keySet());
            for (String key : keySet) {
                Map<String, Object> item = new HashMap<>();
                item.put("category", getCategoryValue(key, categoryType));
                dataProvider.add(item);
                if (valueList.size() > 0) {
                    Map<String, Object> data = (Map<String, Object>) dataMap.get(key);
                    for (String dataKey : data.keySet()) {
                        item.put(dataKey, data.get(dataKey));
                        if (!graphs.contains(dataKey)) {
                            graphs.add(dataKey);
                        }
                    }
                } else {
                    if (!graphs.contains("graphs")) {
                        graphs.add("count");
                    }
                    item.put("count", category.get(key));
                }
            }
        }

        return dataProvider;
    }
}
