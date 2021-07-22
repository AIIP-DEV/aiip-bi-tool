package com.sk.bds.datainsight.util;

public class SqlMap {
    public class Dashboard {
        public static final String DROP_TABLE = "DROP TABLE `%s`";

        public static final String SELECT_DASHBOARD = "SELECT * FROM " +
                "(SELECT * FROM " +
                "(SELECT * FROM DASHBOARD) S1 " +
                "LEFT JOIN (SELECT DASHBOARD_ID, COUNT(DISTINCT DATA_SET_ID) AS CNT_DATA_SET, COUNT(DISTINCT QUERY_ID) AS CNT_QUERY FROM DASHBOARD_DATA_SET GROUP BY DASHBOARD_ID) S2 ON S1.ID = S2.DASHBOARD_ID " +
                "LEFT JOIN (SELECT DASHBOARD_ID AS D_ID, COUNT(1) AS CNT_CHART FROM DASHBOARD_CHART GROUP BY DASHBOARD_ID) S3 ON S1.ID = S3.D_ID) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 1) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID " +
                "%s %s %s";
        public static final String SELECT_DASHBOARD_COUNT = "SELECT COUNT(1) FROM " +
                "(SELECT * FROM " +
                "(SELECT * FROM DASHBOARD) S1 " +
                "LEFT JOIN (SELECT DASHBOARD_ID, COUNT(DISTINCT DATA_SET_ID) AS CNT_DATA_SET, COUNT(DISTINCT QUERY_ID) AS CNT_QUERY FROM DASHBOARD_DATA_SET GROUP BY DASHBOARD_ID) S2 ON S1.ID = S2.DASHBOARD_ID " +
                "LEFT JOIN (SELECT DASHBOARD_ID AS D_ID, COUNT(1) AS CNT_CHART FROM DASHBOARD_CHART GROUP BY DASHBOARD_ID) S3 ON S1.ID = S3.D_ID) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 1) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID " +
                "%s";
        public static final String SELECT_DASHBOARD_ID = "SELECT * FROM " +
                "(SELECT * FROM DASHBOARD WHERE ID = %s) S1 " +
                "LEFT JOIN (SELECT DASHBOARD_ID, COUNT(DISTINCT DATA_SET_ID) AS CNT_DATA_SET, COUNT(DISTINCT QUERY_ID) AS CNT_QUERY FROM DASHBOARD_DATA_SET GROUP BY DASHBOARD_ID) S2 ON S1.ID = S2.DASHBOARD_ID " +
                "LEFT JOIN (SELECT DASHBOARD_ID, COUNT(*) AS CNT_CHART FROM DASHBOARD_CHART GROUP BY DASHBOARD_ID) S3 ON S1.ID = S3.DASHBOARD_ID " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 1) S4 ON S1.ID = S4.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) S5 ON S4.GROUP_ID = S5.G_ID ";
        public static final String SELECT_DATA_TABLE_INFO = "SELECT ID, DATA_TABLE FROM DATA_SET WHERE ID = (SELECT DATA_SET_ID FROM ANALYSIS WHERE ID = %s)";
        public static final String SELECT_DASHBOARD_CHART = "SELECT * FROM DASHBOARD_CHART WHERE DASHBOARD_ID = %s";
        public static final String SELECT_DASHBOARD_CHART_ID = "SELECT * FROM DASHBOARD_CHART WHERE ID = %s";
        public static final String SELECT_DASHBOARD_CHART_DATA_ID = "SELECT * FROM DASHBOARD_CHART WHERE DASHBOARD_DATA_ID = %s";
        public static final String SELECT_DASHBOARD_DATA_SET = "SELECT * FROM DASHBOARD_DATA_SET WHERE DASHBOARD_ID = %s";
        public static final String SELECT_DASHBOARD_DATA_SET_ID = "SELECT * FROM DASHBOARD_DATA_SET WHERE ID = %s";
        public static final String SELECT_QUERY_TEXT = "SELECT QUERY_TEXT FROM QUERY WHERE ID = %s";
        public static final String SELECT_DASHBOARD_FILTER = "SELECT * FROM DASHBOARD_FILTER_INFO " +
                "WHERE GROUP_ID IN (SELECT ID FROM DASHBOARD_FILTER_GROUP_INFO WHERE DASHBOARD_DATA_SET_ID = %d AND QUERY_ID = %d)";
        public static final String SELECT_DASHBOARD_FILTER_GROUP = "SELECT * FROM DASHBOARD_FILTER_GROUP_INFO WHERE DASHBOARD_DATA_SET_ID = %d AND QUERY_ID = %d";

        public static final String DELETE_DASHBOARD_DATA_SET = "DELETE FROM DASHBOARD_DATA_SET WHERE ID = %s";
        public static final String DELETE_DASHBOARD = "DELETE FROM DASHBOARD WHERE ID = %s";
        public static final String DELETE_DASHBOARD_GROUP_MAPPING = "DELETE FROM GROUP_MAPPING WHERE ID = %s AND `TYPE` = 1";

        public static final String UPDATE_DASHBOARD = "UPDATE DASHBOARD SET %s WHERE ID = %s";

        public static final String INSERT_DASHBOARD_CHART = "INSERT INTO DASHBOARD_CHART (`DASHBOARD_ID`, `DASHBOARD_DATA_ID`, `NAME`, `SUB_NAME`, `CHART_ID`, " +
                "`AXIS`, `VALUE`, `GROUP`, `LINE`, `DRAW_INFO`) " +
                "VALUES (:DASHBOARD_ID, :DASHBOARD_DATA_ID, :NAME, :SUB_NAME, :CHART_ID, :AXIS, :VALUE, :GROUP, :LINE, :DRAW_INFO)";
        public static final String INSERT_DASHBOARD_FILTER_INFO = "INSERT INTO DASHBOARD_FILTER_INFO (GROUP_ID, `COLUMN`, " +
                "`CONDITION`, VALUE1, VALUE2) VALUES (:groupId, :column, :condition, :value1, :value2)";
        public static final String INSERT_DASHBOARD_COLUMN_INFO = "INSERT INTO DASHBOARD_COLUMN_INFO (DASHBOARD_DATA_SET_ID, USE_COLUMNS, ORIGINAL_COLUMN, CHANGE_COLUMN, ADD_COLUMN) " +
                "SELECT %d, USE_COLUMNS, ORIGINAL_COLUMN, CHANGE_COLUMN, ADD_COLUMN FROM ANALYSIS_COLUMN_INFO WHERE ANALYSIS_ID = %d";

        public static final String UPDATE_DASHBOARD_CHART = "UPDATE DASHBOARD_CHART SET `DASHBOARD_ID` = :DASHBOARD_ID, `DASHBOARD_DATA_ID` = :DASHBOARD_DATA_ID, `NAME` = :NAME, " +
                "`SUB_NAME` = :SUB_NAME, `CHART_ID` = :CHART_ID, `AXIS` = :AXIS, `VALUE` = :VALUE, `GROUP` = :GROUP, `LINE` = :LINE, " +
                "`DRAW_INFO` = :DRAW_INFO " +
                "WHERE `ID` = :ID";
        public static final String DELETE_DASHBOARD_CHART = "DELETE FROM DASHBOARD_CHART WHERE ID = %s";

        public static final String CREATE_DATA_SET = "CREATE TABLE `DATA_SET` (" +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`SRC_TYPE` VARCHAR(10) NOT NULL, " +
                "`SRC_CONNECTION` VARCHAR(1024) NOT NULL, " +
                "`DATA_TABLE` VARCHAR(64) NOT NULL, " +
                "`USE_COLUMNS` TEXT NOT NULL, " +
                "`ORIGINAL_COLUMN` TEXT NOT NULL, " +
                "`CHANGE_COLUMN` TEXT NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "`STATUS` INT(11) NOT NULL DEFAULT '0', " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DATA_SET_FILTER = "CREATE TABLE `DATA_SET_FILTER` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`GROUP_ID` INT(11) NOT NULL, " +
                "`COLUMN` VARCHAR(64) NOT NULL, " +
                "`CONDITION` VARCHAR(10) NOT NULL, " +
                "`VALUE1` VARCHAR(32) NULL DEFAULT NULL, " +
                "`VALUE2` VARCHAR(32) NULL DEFAULT NULL, " +
                "`ENABLE` VARCHAR(5) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_DATA_SET_FILTER` (`GROUP_ID`), " +
                "CONSTRAINT `FK1_DATA_SET_FILTER` FOREIGN KEY (`GROUP_ID`) REFERENCES `DATA_SET_FILTER_GROUP` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DATA_SET_FILTER_GROUP = "CREATE TABLE `DATA_SET_FILTER_GROUP` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`DATA_SET_ID` INT(11) NOT NULL, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`ENABLE` VARCHAR(5) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_DATA_SET_FILTER_GROUP` (`DATA_SET_ID`), " +
                "CONSTRAINT `FK1_DATA_SET_FILTER_GROUP` FOREIGN KEY (`DATA_SET_ID`) REFERENCES `DATA_SET` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DATA_SET_ADD_COLUMN = "CREATE TABLE `DATA_SET_ADD_COLUMN` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`DATA_SET_ID` INT(11) NOT NULL, " +
                "`NAME` VARCHAR(64) NOT NULL, " +
                "`ORIGINAL_COLUMN` VARCHAR(64) NOT NULL, " +
                "`FORMULA` VARCHAR(1024) NOT NULL, " +
                "`TYPE` VARCHAR(10) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_ADD_COLUMN` (`DATA_SET_ID`), " +
                "CONSTRAINT `FK1_ADD_COLUMN` FOREIGN KEY (`DATA_SET_ID`) REFERENCES `DATA_SET` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_CHART_INFO = "CREATE TABLE `CHART_INFO` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`NAME` VARCHAR(50) NOT NULL, " +
                "`TYPE` VARCHAR(50) NOT NULL, " +
                "`VALUE` VARCHAR(255) NOT NULL, " +
                "`USE_YN` VARCHAR(1) NOT NULL DEFAULT 'Y', " +
                "`SORT` INT(11) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_ECHART_INFO = "CREATE TABLE `ECHART_INFO` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`NAME` VARCHAR(50) NULL, " +
                "`TYPE` VARCHAR(50) NULL, " +
                "`VALUE` VARCHAR(255) NULL, " +
                "`USE_YN` VARCHAR(1) NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "`SORT` INT(11) NOT NULL, " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_CHART_AGGREGATE = "CREATE TABLE `CHART_AGGREGATE` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`TYPE` VARCHAR(10) NOT NULL, " +
                "`NAME` VARCHAR(50) NOT NULL, " +
                "`FUNC` VARCHAR(50) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_CHART_FORMAT = "CREATE TABLE `CHART_FORMAT` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`TYPE` VARCHAR(10) NOT NULL, " +
                "`NAME` VARCHAR(50) NOT NULL, " +
                "`FUNC` VARCHAR(50) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DASHBOARD = "CREATE TABLE `DASHBOARD` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`SUB_NAME` VARCHAR(255) NULL DEFAULT NULL, " +
                "`OBJECT` BLOB NULL DEFAULT NULL, " +
                "`THUMB_IMG` LONGTEXT NULL, " +
                "`CREATE_USER` VARCHAR(20) NULL DEFAULT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DASHBOARD_CHART = "CREATE TABLE `DASHBOARD_CHART` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`DASHBOARD_ID` INT(11) NOT NULL, " +
                "`DASHBOARD_DATA_ID` INT(11) NOT NULL, " +
                "`NAME` VARCHAR(255) NULL DEFAULT NULL, " +
                "`SUB_NAME` VARCHAR(255) NULL DEFAULT NULL, " +
                "`CHART_ID` INT(11) NOT NULL, " +
                "`AXIS` VARCHAR(64) NULL DEFAULT NULL, " +
                "`VALUE` VARCHAR(1024) NULL DEFAULT NULL, " +
                "`GROUP` VARCHAR(64) NULL DEFAULT NULL, " +
                "`LINE` VARCHAR(64) NULL DEFAULT NULL, " +
                "`FLAG` VARCHAR(50) NULL DEFAULT NULL, " +
                "`DRAW_INFO` TEXT NULL DEFAULT NULL, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_DASHBOARD_CHART` (`DASHBOARD_ID`), " +
                "INDEX `FK2_DASHBOARD_CHART` (`CHART_ID`), " +
                "INDEX `FK3_DASHBOARD_CHART` (`DASHBOARD_DATA_ID`), " +
                "CONSTRAINT `FK1_DASHBOARD_CHART` FOREIGN KEY (`DASHBOARD_ID`) REFERENCES `DASHBOARD` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT `FK2_DASHBOARD_CHART` FOREIGN KEY (`CHART_ID`) REFERENCES `CHART_INFO` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT `FK3_DASHBOARD_CHART` FOREIGN KEY (`DASHBOARD_DATA_ID`) REFERENCES `DASHBOARD_DATA_SET` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DASHBOARD_DATA_SET = "CREATE TABLE `DASHBOARD_DATA_SET` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`DASHBOARD_ID` INT(11) NOT NULL, " +
                "`DATA_SET_ID` INT(11) NULL DEFAULT NULL, " +
                "`DATA_TABLE` VARCHAR(64) NULL DEFAULT NULL, " +
                "`QUERY_ID` INT(11) NULL DEFAULT NULL, " +
                "`VARIABLE_INFO` TEXT NULL, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_DASHBOARD_DATA_SET` (`DASHBOARD_ID`), " +
                "INDEX `FK2_DASHBOARD_DATA_SET` (`DATA_SET_ID`), " +
                "INDEX `FK_DASHBOARD_DATA_SET_QUERY` (`QUERY_ID`), " +
                "CONSTRAINT `FK1_DASHBOARD_DATA_SET` FOREIGN KEY (`DASHBOARD_ID`) REFERENCES `DASHBOARD` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT `FK2_DASHBOARD_DATA_SET` FOREIGN KEY (`DATA_SET_ID`) REFERENCES `DATA_SET` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT `FK3_DASHBOARD_DATA_SET_QUERY` FOREIGN KEY (`QUERY_ID`) REFERENCES `QUERY` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_ANALYSIS = "CREATE TABLE `ANALYSIS` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`QUERY_ID` INT(11) NULL DEFAULT NULL, " +
                "`DATA_SET_ID` INT(11) NULL DEFAULT NULL, " +
                "`NAME` VARCHAR(255) NOT NULL DEFAULT '이름 없음', " +
                "`SUB_NAME` VARCHAR(255) NULL DEFAULT NULL, " +
                "`THUMB_IMG` LONGTEXT NULL, " +
                "`DATA_TABLE` VARCHAR(64) NULL DEFAULT NULL, " +
                "`VARIABLE_INFO` TEXT NULL DEFAULT NULL, " +
                "`CREATE_USER` VARCHAR(20) NULL DEFAULT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "`STATUS` INT(1) NOT NULL, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_ANALYSIS` (`DATA_SET_ID`), " +
                "INDEX `FK_ANALYSIS_QUERY` (`QUERY_ID`), " +
                "CONSTRAINT `FK1_ANALYSIS` FOREIGN KEY (`DATA_SET_ID`) REFERENCES `DATA_SET` (`ID`) ON UPDATE CASCADE ON DELETE SET NULL, " +
                "CONSTRAINT `FK2_ANALYSIS_QUERY` FOREIGN KEY (`QUERY_ID`) REFERENCES `QUERY` (`ID`) ON UPDATE CASCADE ON DELETE SET NULL " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_ANALYSIS_CHART = "CREATE TABLE `ANALYSIS_CHART` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`ANALYSIS_ID` INT(11) NULL DEFAULT NULL, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`CHART_ID` INT(11) NULL DEFAULT NULL, " +
                "`AXIS` VARCHAR(64) NULL DEFAULT NULL, " +
                "`VALUE` VARCHAR(64) NULL DEFAULT NULL, " +
                "`GROUP` VARCHAR(64) NULL DEFAULT NULL, " +
                "`LINE` VARCHAR(64) NULL DEFAULT NULL, " +
                "`FLAG` VARCHAR(50) NULL DEFAULT NULL, " +
                "`TYPE` VARCHAR(50) NOT NULL, " +
                "`OPTION` TEXT NOT NULL, " +
                "`THUMBNAIL` LONGTEXT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_ANALYSIS_CHART` (`ANALYSIS_ID`), " +
                "INDEX `FK2_ANALYSIS_CHART` (`CHART_ID`), " +
                "CONSTRAINT `FK1_ANALYSIS_CHART` FOREIGN KEY (`ANALYSIS_ID`) REFERENCES `ANALYSIS` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT `FK2_ANALYSIS_CHART` FOREIGN KEY (`CHART_ID`) REFERENCES `CHART_INFO` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_ANALYSIS_FILTER = "CREATE TABLE `ANALYSIS_FILTER` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`GROUP_ID` INT(11) NOT NULL, " +
                "`COLUMN` VARCHAR(64) NOT NULL, " +
                "`CONDITION` VARCHAR(10) NOT NULL, " +
                "`VALUE1` VARCHAR(32) NULL DEFAULT NULL, " +
                "`VALUE2` VARCHAR(32) NULL DEFAULT NULL, " +
                "`ENABLE` VARCHAR(5) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_ANALYSIS_FILTER` (`GROUP_ID`), " +
                "CONSTRAINT `FK1_ANALYSIS_FILTER` FOREIGN KEY (`GROUP_ID`) REFERENCES `ANALYSIS_FILTER_GROUP` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_ANALYSIS_FILTER_GROUP = "CREATE TABLE `ANALYSIS_FILTER_GROUP` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`ANALYSIS_ID` INT(11) NOT NULL, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`ENABLE` VARCHAR(5) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_ANALYSIS_FILTER_GROUP` (`ANALYSIS_ID`), " +
                "CONSTRAINT `FK1_ANALYSIS_FILTER_GROUP` FOREIGN KEY (`ANALYSIS_ID`) REFERENCES `ANALYSIS` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_ANALYSIS_COLUMN_INFO = "CREATE TABLE `ANALYSIS_COLUMN_INFO` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`ANALYSIS_ID` INT(11) NOT NULL, " +
                "`USE_COLUMNS` TEXT NOT NULL, " +
                "`ORIGINAL_COLUMN` TEXT NOT NULL, " +
                "`CHANGE_COLUMN` TEXT NOT NULL, " +
                "`ADD_COLUMN` TEXT NULL DEFAULT NULL COMMENT '[{\"NAME\":\"\", \"ORIGINAL_COLUMN\":\"\", \"FORMULA\":\"\", \"TYPE\":\"\"}]', " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_ANALYSIS_COLUMN_INFO` (`ANALYSIS_ID`), " +
                "CONSTRAINT `FK1_ANALYSIS_COLUMN_INFO` FOREIGN KEY (`ANALYSIS_ID`) REFERENCES `ANALYSIS` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DASHBOARD_FILTER_GROUP_INFO = "CREATE TABLE `DASHBOARD_FILTER_GROUP_INFO` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`DASHBOARD_DATA_SET_ID` INT(11) NOT NULL, " +
                "`DATA_SET_ID` INT(11) NULL DEFAULT NULL, " +
                "`QUERY_ID` INT(11) NULL DEFAULT NULL, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_DASHBOARD_FILTER_GROUP_INFO` (`DASHBOARD_DATA_SET_ID`), " +
                "CONSTRAINT `FK1_DASHBOARD_FILTER_GROUP_INFO` FOREIGN KEY (`DASHBOARD_DATA_SET_ID`) REFERENCES `DASHBOARD_DATA_SET` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DASHBOARD_FILTER_INFO = "CREATE TABLE `DASHBOARD_FILTER_INFO` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`GROUP_ID` INT(11) NOT NULL, " +
                "`COLUMN` VARCHAR(64) NOT NULL, " +
                "`CONDITION` VARCHAR(10) NOT NULL, " +
                "`VALUE1` VARCHAR(32) NULL DEFAULT NULL, " +
                "`VALUE2` VARCHAR(32) NULL DEFAULT NULL, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_DASHBOARD_FILTER_INFO` (`GROUP_ID`), " +
                "CONSTRAINT `FK1_DASHBOARD_FILTER_INFO` FOREIGN KEY (`GROUP_ID`) REFERENCES `DASHBOARD_FILTER_GROUP_INFO` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_DASHBOARD_COLUMN_INFO = "CREATE TABLE `DASHBOARD_COLUMN_INFO` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`DASHBOARD_DATA_SET_ID` INT(11) NOT NULL, " +
                "`USE_COLUMNS` TEXT NOT NULL, " +
                "`ORIGINAL_COLUMN` TEXT NOT NULL, " +
                "`CHANGE_COLUMN` TEXT NOT NULL, " +
                "`ADD_COLUMN` TEXT NULL DEFAULT NULL COMMENT '[{\"NAME\":\"\", \"ORIGINAL_COLUMN\":\"\", \"FORMULA\":\"\", \"TYPE\":\"\"}]', " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_DASHBOARD_COLUMN_INFO` (`DASHBOARD_DATA_SET_ID`), " +
                "CONSTRAINT `FK1_DASHBOARD_COLUMN_INFO` FOREIGN KEY (`DASHBOARD_DATA_SET_ID`) REFERENCES `DASHBOARD_DATA_SET` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_ORIGINAL = "CREATE TABLE `ORIGINAL` ( " +
                "`SCHEDULE_ID` INT(11) NOT NULL, " +
                "`ORIGINAL_TABLE` VARCHAR(64) NOT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`SCHEDULE_ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_GROUP = "CREATE TABLE `GROUP` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`NAME` VARCHAR(128) NOT NULL, " +
                "`DESCRIPTION` VARCHAR(1024) NOT NULL, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "UNIQUE INDEX `NAME` (`NAME`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_GROUP_MAPPING = "CREATE TABLE `GROUP_MAPPING` ( " +
                "`ID` INT(11) NOT NULL, " +
                "`TYPE` INT(1) NOT NULL, " +
                "`GROUP_ID` INT(11) NOT NULL, " +
                "PRIMARY KEY (`ID`, `TYPE`), " +
                "INDEX `FK_GROUP_MAPPING_GROUP` (`GROUP_ID`), " +
                "CONSTRAINT `FK_GROUP_MAPPING_GROUP` FOREIGN KEY (`GROUP_ID`) REFERENCES `GROUP` (`ID`) " +
                ")" +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_QUERY = "CREATE TABLE `QUERY` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`DESCRIPTION` VARCHAR(1024) NULL DEFAULT NULL, " +
                "`QUERY_TEXT` TEXT NOT NULL, " +
                "`DATA_SET_INFO` VARCHAR(128) NOT NULL, " +
                "`VARIABLE_INFO` TEXT NULL, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_REPORT = "CREATE TABLE `REPORT` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`SUB_NAME` VARCHAR(255) NULL DEFAULT NULL, " +
                "`OBJECT` BLOB NULL DEFAULT NULL, " +
                "`THUMB_IMG` LONGTEXT NULL, " +
                "`CREATE_USER` VARCHAR(20) NULL DEFAULT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`) " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
        public static final String CREATE_REPORT_CHART = "CREATE TABLE `REPORT_CHART` ( " +
                "`ID` INT(11) NOT NULL AUTO_INCREMENT, " +
                "`REPORT_ID` INT(11) NULL DEFAULT NULL, " +
                "`ANALYSIS_CHART_ID` INT(11) NULL DEFAULT NULL, " +
                "`NAME` VARCHAR(255) NOT NULL, " +
                "`CHART_ID` INT(11) NULL DEFAULT NULL, " +
                "`AXIS` VARCHAR(64) NULL DEFAULT NULL, " +
                "`VALUE` VARCHAR(64) NULL DEFAULT NULL, " +
                "`GROUP` VARCHAR(64) NULL DEFAULT NULL, " +
                "`LINE` VARCHAR(64) NULL DEFAULT NULL, " +
                "`FLAG` VARCHAR(50) NULL DEFAULT NULL, " +
                "`TYPE` VARCHAR(50) NOT NULL, " +
                "`OPTION` TEXT NOT NULL, " +
                "`THUMBNAIL` LONGTEXT NULL, " +
                "`UPDATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP, " +
                "`CREATE_DATE` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "PRIMARY KEY (`ID`), " +
                "INDEX `FK1_REPORT_CHART` (`REPORT_ID`), " +
                "INDEX `FK2_REPORT_CHART` (`CHART_ID`), " +
                "INDEX `FK3_REPORT_CHART` (`ANALYSIS_CHART_ID`), " +
                "CONSTRAINT `FK1_REPORT_CHART` FOREIGN KEY (`REPORT_ID`) REFERENCES `REPORT` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT `FK2_REPORT_CHART` FOREIGN KEY (`CHART_ID`) REFERENCES `CHART_INFO` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE, " +
                "CONSTRAINT `FK3_REPORT_CHART` FOREIGN KEY (`ANALYSIS_CHART_ID`) REFERENCES `ANALYSIS_CHART` (`ID`) ON UPDATE CASCADE ON DELETE CASCADE " +
                ") " +
                "COLLATE='utf8_general_ci' " +
                "ENGINE=InnoDB";
    }

    public class DataSet {
        public static final String CREATE_DATA_SET_TABLE = "CREATE TABLE %s (%s) COLLATE='utf8_general_ci' ENGINE=InnoDB";

        public static final String DROP_DATA_SET_TABLE = "DROP TABLE IF EXISTS %s";

        public static final String INSERT_DATA_SET_TABLE = "INSERT IGNORE INTO %s (%s) VALUES (%s)";
        public static final String INSERT_DATA_SET_FILTER = "INSERT INTO DATA_SET_FILTER (`GROUP_ID`, `COLUMN`, `CONDITION`, `VALUE1`, `VALUE2`, `ENABLE`) " +
                "VALUES (:GROUP_ID, :COLUMN, :CONDITION, :VALUE1, :VALUE2, :ENABLE)";
        public static final String INSERT_DATA_SET_ADD_COLUMN = "INSERT INTO DATA_SET_ADD_COLUMN (`DATA_SET_ID`, `NAME`, `ORIGINAL_COLUMN`, `FORMULA`, `TYPE`) " +
                "VALUES (:DATA_SET_ID, :NAME, :ORIGINAL_COLUMN, :FORMULA, :TYPE)";

        public static final String SELECT_DATA_SET = "SELECT * FROM " +
                "(SELECT * FROM DATA_SET) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 2) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID " +
                "%s %s %s";
        public static final String SELECT_DATA_SET_COUNT = "SELECT COUNT(1) FROM " +
                "(SELECT * FROM DATA_SET) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 2) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID " +
                "%s";
        public static final String SELECT_DATA_SET_UPDATE_COUNT = "SELECT COUNT(1) FROM DATA_SET WHERE `STATUS` = 1";
        public static final String SELECT_DATA_SET_ID = "SELECT * FROM " +
                "(SELECT * FROM DATA_SET WHERE ID = %s) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 2) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID";
        public static final String SELECT_DATA_SET_FILTER = "SELECT * FROM DATA_SET_FILTER " +
                "WHERE GROUP_ID IN (SELECT ID FROM DATA_SET_FILTER_GROUP WHERE DATA_SET_ID = %s)";
        public static final String SELECT_DATA_SET_FILTER_GROUP = "SELECT * FROM DATA_SET_FILTER_GROUP WHERE DATA_SET_ID = %s";
        public static final String SELECT_DATA_SET_ADD_COLUMN = "SELECT * FROM DATA_SET_ADD_COLUMN WHERE DATA_SET_ID = %s";

        public static final String DELETE_DATA_SET = "DELETE FROM DATA_SET WHERE ID = %s";
        public static final String DELETE_DATA_SET_FILTER_GROUP = "DELETE FROM DATA_SET_FILTER_GROUP WHERE DATA_SET_ID = %s";
        public static final String DELETE_DATA_SET_ADD_COLUMN = "DELETE FROM DATA_SET_ADD_COLUMN WHERE DATA_SET_ID = %s";
        public static final String DELETE_DATA_SET_GROUP_MAPPING = "DELETE FROM GROUP_MAPPING WHERE ID = %s AND `TYPE` = 2";

        public static final String UPDATE_DATA_SET = "UPDATE DATA_SET SET `NAME` = :NAME, `USE_COLUMNS` = :USE_COLUMNS, `DATA_TABLE` = :DATA_TABLE, " +
                "CHANGE_COLUMN = :CHANGE_COLUMN WHERE ID = :ID";
        public static final String UPDATE_DATA_SET_INFO = "UPDATE DATA_SET SET `USE_COLUMNS` = :USE_COLUMNS, `ORIGINAL_COLUMN` = :ORIGINAL_COLUMN, " +
                "`CHANGE_COLUMN` = :CHANGE_COLUMN, `STATUS` = :STATUS WHERE `ID` = :ID";
        public static final String UPDATE_DATA_SET_IS_UPDATE = "UPDATE DATA_SET SET `STATUS` = :STATUS WHERE `ID` = :ID";
    }

    public class Analysis {
        public static final String CREATE_ANALYSIS_TABLE = "CREATE TABLE %s (%s) COLLATE='utf8_general_ci' ENGINE=InnoDB";
        public static final String SELECT_ANALYSIS = "SELECT * FROM " +
                "(SELECT * FROM " +
                "(SELECT * FROM ANALYSIS) S1 " +
                "LEFT JOIN (SELECT ID AS D_ID, SRC_TYPE, NAME AS SRC_NAME FROM DATA_SET) S2 ON S1.DATA_SET_ID = S2.D_ID " +
                "LEFT JOIN (SELECT ANALYSIS_ID, COUNT(*) AS CNT_CHART FROM ANALYSIS_CHART GROUP BY ANALYSIS_ID) S3 ON S1.ID = S3.ANALYSIS_ID " +
                "LEFT JOIN (SELECT ID AS Q_ID, NAME AS QUERY_NAME FROM QUERY) S4 ON S1.QUERY_ID = S4.Q_ID) A " +
                "LEFT JOIN (SELECT ID AS A_ID, GROUP_ID AS M_GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 0) B ON A.ID = B.A_ID " +
                "LEFT JOIN (SELECT ID AS GROUP_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.M_GROUP_ID = C.GROUP_ID " +
                "%s %s %s";
        public static final String SELECT_ANALYSIS_COUNT = "SELECT COUNT(1) FROM " +
                "(SELECT * FROM " +
                "(SELECT * FROM ANALYSIS) S1 " +
                "LEFT JOIN (SELECT ID AS D_ID, SRC_TYPE, NAME AS SRC_NAME FROM DATA_SET) S2 ON S1.DATA_SET_ID = S2.D_ID " +
                "LEFT JOIN (SELECT ANALYSIS_ID, COUNT(*) AS CNT_CHART FROM ANALYSIS_CHART GROUP BY ANALYSIS_ID) S3 ON S1.ID = S3.ANALYSIS_ID) A " +
                "LEFT JOIN (SELECT ID AS A_ID, GROUP_ID AS M_GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 0) B ON A.ID = B.A_ID " +
                "LEFT JOIN (SELECT ID AS GROUP_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.M_GROUP_ID = C.GROUP_ID " +
                "%s";
        public static final String SELECT_ANALYSIS_CHART_BY_ID = "SELECT * FROM " +
                "ANALYSIS_CHART AA " +
                "WHERE AA.ANALYSIS_ID IN(SELECT BB.ID FROM " +
                "(SELECT * FROM " +
                "(SELECT * FROM " +
                "(SELECT * FROM ANALYSIS) S1 " +
                "LEFT JOIN (SELECT ANALYSIS_ID, COUNT(*) AS CNT_CHART FROM ANALYSIS_CHART GROUP BY ANALYSIS_ID) S3 ON S1.ID = S3.ANALYSIS_ID " +
                "LEFT JOIN (SELECT ID AS Q_ID, NAME AS QUERY_NAME FROM QUERY) S4 ON S1.QUERY_ID = S4.Q_ID) A " +
                "LEFT JOIN (SELECT ID AS A_ID, GROUP_ID AS M_GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 0) B ON A.ID = B.A_ID " +
                "LEFT JOIN (SELECT ID AS GROUP_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.M_GROUP_ID = C.GROUP_ID " +
                "%s %s %s) BB)";
        public static final String SELECT_ANALYSIS_ID = "SELECT * FROM " +
                "(SELECT * FROM ANALYSIS WHERE ID = %s) S1 " +
                "LEFT JOIN (SELECT ID, SRC_TYPE, NAME AS SRC_NAME FROM DATA_SET) S2 ON S1.DATA_SET_ID = S2.ID " +
                "LEFT JOIN (SELECT ANALYSIS_ID, COUNT(*) AS CNT_CHART FROM ANALYSIS_CHART GROUP BY ANALYSIS_ID) S3 ON S1.ID = S3.ANALYSIS_ID " +
                "LEFT JOIN (SELECT ID AS A_ID, GROUP_ID AS M_GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 0) S4 ON S1.ID = S4.A_ID " +
                "LEFT JOIN (SELECT ID AS GROUP_ID, NAME AS GROUP_NAME FROM `GROUP`) S5 ON S4.M_GROUP_ID = S5.GROUP_ID " +
                "LEFT JOIN (SELECT ID AS Q_ID, NAME AS QUERY_NAME FROM QUERY) S6 ON S1.QUERY_ID = S6.Q_ID";
        public static final String SELECT_ANALYSIS_ID_BY_UUID = "SELECT ID FROM ANALYSIS WHERE SUB_NAME = '%s'";
        public static final String SELECT_COUNT_ANALYSIS_ID_BY_UUID = "SELECT COUNT(*) FROM ANALYSIS WHERE SUB_NAME = '%s'";
        public static final String SELECT_CHART_INFO = "SELECT * FROM CHART_INFO ORDER BY `SORT`";
        public static final String SELECT_ANALYSIS_FILTER = "SELECT * FROM ANALYSIS_FILTER " +
                "WHERE GROUP_ID IN (SELECT ID FROM ANALYSIS_FILTER_GROUP WHERE ANALYSIS_ID = %s)";
        public static final String SELECT_ANALYTICS_FILTER = "SELECT * FROM ANALYSIS_FILTER " +
                "WHERE GROUP_ID IN (SELECT ID FROM ANALYSIS_FILTER_GROUP WHERE ANALYSIS_ID = %s)";
        public static final String SELECT_ANALYSIS_FILTER_GROUP = "SELECT * FROM ANALYSIS_FILTER_GROUP WHERE ANALYSIS_ID = %s";
        public static final String SELECT_ANALYSIS_CHART = "SELECT * FROM ANALYSIS_CHART WHERE ANALYSIS_ID = %s";
        public static final String SELECT_ANALYSIS_CHART_TITLE = "SELECT A.ID AS ID, CONCAT(A.NAME, ' > ', B.NAME) AS TITLE FROM " +
                "(SELECT ID, ANALYSIS_ID, NAME FROM ANALYSIS_CHART WHERE ID IN (%s)) A " +
                "LEFT JOIN (SELECT ID, NAME FROM ANALYSIS) B ON A.ANALYSIS_ID = B.ID";
        public static final String SELECT_ANALYSIS_CHART_ID = "SELECT * FROM ANALYSIS_CHART WHERE ID = %s";
        public static final String SELECT_DATA_SET_ADD_COLUMN = "SELECT NAME, ORIGINAL_COLUMN, FORMULA, `TYPE` FROM DATA_SET_ADD_COLUMN WHERE DATA_SET_ID = %d";
        public static final String SELECT_ANALYSIS_GROUP = "SELECT * FROM " +
                "(SELECT * FROM `GROUP`) A " +
                "LEFT JOIN ( " +
                "SELECT A.GROUP_ID, GROUP_CONCAT(A.TYPES, A.CNT) AS TYPE_CNT FROM " +
                "(SELECT COUNT(1) AS CNT, GROUP_ID, IF(`TYPE` = 0, '분석작업: ', IF(`TYPE` = 1, '대시보드: ', IF(`TYPE` = 2, '데이터 셋: ', '쿼리: '))) AS TYPES FROM GROUP_MAPPING GROUP BY GROUP_ID, `TYPE`) A GROUP BY A.GROUP_ID" +
                ") B ON A.ID = B.GROUP_ID " +
                "%s %s %s";
        public static final String SELECT_ANALYSIS_GROUP_COUNT = "SELECT COUNT(1) FROM " +
                "(SELECT * FROM `GROUP`) A " +
                "LEFT JOIN ( " +
                "SELECT A.GROUP_ID, GROUP_CONCAT(A.TYPES, A.CNT) AS TYPE_CNT FROM " +
                "(SELECT COUNT(1) AS CNT, GROUP_ID, IF(`TYPE` = 0, '분석작업: ', IF(`TYPE` = 1, '대시보드: ', IF(`TYPE` = 2, '데이터 셋: ', '쿼리: '))) AS TYPES FROM GROUP_MAPPING GROUP BY GROUP_ID, `TYPE`) A GROUP BY A.GROUP_ID" +
                ") B ON A.ID = B.GROUP_ID " +
                "%s";
        public static final String SELECT_ANALYSIS_QUERY_TEXT = "SELECT B.QUERY_TEXT FROM " +
                "(SELECT QUERY_ID FROM ANALYSIS WHERE ID = %s) A " +
                "LEFT JOIN (SELECT QUERY_TEXT, ID FROM QUERY) B ON A.QUERY_ID = B.ID";
        public static final String SELECT_ANALYSIS_SRC_INFO = "SELECT IF(B.SRC_TYPE IS NULL, C.SRC_TYPE, B.SRC_TYPE) AS srcType, IF(B.NAME IS NULL, C.NAME, B.NAME) AS name FROM " +
                "(SELECT DATA_SET_ID, QUERY_ID FROM ANALYSIS WHERE ID = %s) A " +
                "LEFT JOIN (SELECT ID, SRC_TYPE, NAME FROM DATA_SET) B ON A.DATA_SET_ID = B.ID " +
                "LEFT JOIN (SELECT ID, 'QUERY' AS SRC_TYPE, NAME FROM QUERY) C ON A.QUERY_ID = C.ID";
        public static final String SELECT_GROUP_BY_NAME = "SELECT COUNT(*) FROM `GROUP` WHERE NAME = %s";
        public static final String SELECT_GROUP_BY_ID = "SELECT ID FROM `GROUP` WHERE NAME = %s";

        public static final String DELETE_ANALYSIS = "DELETE FROM ANALYSIS WHERE ID = %s";
        public static final String DELETE_ANALYSIS_FILTER = "DELETE FROM ANALYSIS_FILTER WHERE ID = %s";
        public static final String DELETE_ANALYSIS_FILTER_GROUP = "DELETE FROM ANALYSIS_FILTER_GROUP WHERE ID = %s";
        public static final String DELETE_ANALYSIS_CHART = "DELETE FROM ANALYSIS_CHART WHERE ID = %s";
        public static final String DELETE_ANALYSIS_GROUP = "DELETE FROM `GROUP` WHERE ID = %s";
        public static final String DELETE_ANALYSIS_GROUP_MAPPING = "DELETE FROM GROUP_MAPPING WHERE ID = %s AND `TYPE` = 0";

        public static final String INSERT_ANALYSIS_TABLE = "INSERT IGNORE INTO %s (%s) VALUES (%s)";
        public static final String INSERT_ANALYSIS_FILTER = "INSERT INTO ANALYSIS_FILTER (`GROUP_ID`, `COLUMN`, `CONDITION`, `VALUE1`, " +
                "`VALUE2`, `ENABLE`) VALUES (?, ?, ?, ?, ?, ?)";
        public static final String INSERT_ANALYSIS_CHART = "INSERT INTO ANALYSIS_CHART (`ANALYSIS_ID`, `NAME`, `AXIS`, `OPTION`, `TYPE`, " +
                "`VALUE`, `GROUP`, `LINE`, `FLAG`, `THUMBNAIL`) " +
                "VALUES (:ANALYSIS_ID, :NAME, :AXIS, :OPTION, :TYPE, :VALUE, :GROUP, :LINE, :FLAG, :THUMBNAIL)";
        public static final String INSERT_ANALYSIS_COLUMN_INFO = "INSERT INTO ANALYSIS_COLUMN_INFO (ANALYSIS_ID, USE_COLUMNS, ORIGINAL_COLUMN, " +
                "CHANGE_COLUMN, ADD_COLUMN) VALUES (?, ?, ?, ?, ?)";
        public static final String INSERT_ANALYSIS_GROUP_MAPPING = "INSERT INTO GROUP_MAPPING (`ID`, `GROUP_ID`, `TYPE`) " +
                "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE `GROUP_ID` = VALUES(`GROUP_ID`)";
        public static final String INSERT_GROUP_TABLE = "INSERT INTO `GROUP` (`NAME`, `DESCRIPTION`)" +
                "VALUES (:NAME, :DESCRIPTION) ON DUPLICATE KEY UPDATE `NAME` = VALUES(`NAME`)";

        public static final String UPDATE_ANALYSIS_FILTER = "UPDATE ANALYSIS_FILTER SET `COLUMN` = ?, `CONDITION` = ?, `VALUE1` = ?, " +
                "`VALUE2` = ?, `ENABLE` = ? WHERE ID = ?";
        public static final String UPDATE_ANALYSIS_FILTER_GROUP = "UPDATE ANALYSIS_FILTER_GROUP SET `NAME` = ?, `ENABLE` = ? WHERE ID = ?";
        public static final String UPDATE_ANALYSIS_CHART = "UPDATE ANALYSIS_CHART SET `ANALYSIS_ID` = :ANALYSIS_ID, `NAME` = :NAME, " +
                "`AXIS` = :AXIS, `VALUE` = :VALUE, `GROUP` = :GROUP, `LINE` = :LINE, `FLAG` = :FLAG, `TYPE` = :TYPE, `OPTION` = :OPTION, `THUMBNAIL` = :THUMBNAIL " +
                "WHERE `ID` = :ID";
        public static final String UPDATE_ANALYSIS = "UPDATE ANALYSIS SET `NAME` = ?, `SUB_NAME` = ?, `THUMB_IMG` = ?, VARIABLE_INFO = ? WHERE `ID` = ?";
        public static final String UPDATE_ANALYSIS_GROUP = "UPDATE `GROUP` SET `NAME` = ?, `DESCRIPTION` = ? WHERE `ID` = ?";
        public static final String UPDATE_ANALYSIS_COLUMN_INFO_IS_UPDATE = "UPDATE ANALYSIS_COLUMN_INFO SET `STATUS` = :STATUS WHERE `ANALYSIS_ID` = :ANALYSIS_ID";
        public static final String UPDATE_ANALYSIS_COLUMN_INFO = "UPDATE ANALYSIS_COLUMN_INFO SET `USE_COLUMNS` = :USE_COLUMNS, `ORIGINAL_COLUMN` = :ORIGINAL_COLUMN, " +
                "`CHANGE_COLUMN` = :CHANGE_COLUMN WHERE `ANALYSIS_ID` = :ANALYSIS_ID";
        public static final String SELECT_COLUMN_GROUP_BY = "SELECT %s, COUNT(%s) AS cnt FROM %s GROUP BY %s";
        public static final String SELECT_DATA_SET = "SELECT DATA_TABLE FROM BI_%s.%s WHERE ID = %s";
    }

    public class Query {
        public static final String SELECT_DATA_SET_TABLES = "SELECT ID, DATA_TABLE FROM DATA_SET WHERE ID IN (%s)";
        public static final String SELECT_DATA_SET_TABLE = "SELECT DATA_TABLE FROM DATA_SET WHERE ID = %s";
        public static final String SELECT_QUERY = "SELECT * FROM " +
                "(SELECT * FROM QUERY) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 3) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID " +
                "%s %s %s";
        public static final String SELECT_QUERY_COUNT = "SELECT COUNT(1) FROM " +
                "(SELECT * FROM QUERY) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 3) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID " +
                "%s";
        public static final String SELECT_QUERY_ID = "SELECT * FROM " +
                "(SELECT * FROM QUERY) A " +
                "LEFT JOIN (SELECT ID AS M_ID, GROUP_ID FROM GROUP_MAPPING WHERE `TYPE` = 3) B ON A.ID = B.M_ID " +
                "LEFT JOIN (SELECT ID AS G_ID, NAME AS GROUP_NAME FROM `GROUP`) C ON B.GROUP_ID = C.G_ID " +
                "WHERE ID = %s";

        public static final String UPDATE_QUERY = "UPDATE QUERY SET `NAME` = ?, `DESCRIPTION` = ?, `QUERY_TEXT` = ?, `DATA_SET_INFO` = ?, " +
                "VARIABLE_INFO = ? WHERE `ID` = ?";

        public static final String DELETE_QUERY = "DELETE FROM QUERY WHERE ID = ?";
        public static final String DELETE_QUERY_GROUP_MAPPING = "DELETE FROM GROUP_MAPPING WHERE ID = %s AND `TYPE` = 3";
    }
}
