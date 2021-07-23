package com.sk.bds.datainsight.util;

import com.sk.bds.datainsight.exception.BadException;
import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Util {

    private static final Logger log = LoggerFactory.getLogger(Util.class);
    static RSAPublicKey rsaPublicKey;

    private static final char[] CHO_INDEX = {
            0x1100, 0x1101, 0x1102, 0x1103, 0x1104, 0x1105, 0x1106, 0x1107, 0x1108, 0x1109,
            0x110a, 0x110b, 0x110c, 0x110d, 0x110e, 0x110f, 0x1110, 0x1111, 0x1112
    };
    private static final char[] JUN_INDEX = {
            0x1161, 0x1162, 0x1163, 0x1164, 0x1165, 0x1166, 0x1167, 0x1168, 0x1169, 0x116a,
            0x116b, 0x116c, 0x116d, 0x116e, 0x116f, 0x1170, 0x1171, 0x1172, 0x1173, 0x1174,
            0x1175
    };
    private static final char[] JON_INDEX = {
            0x0000, 0x11a8, 0x11a9, 0x11aa, 0x11ab, 0x11ac, 0x11ad, 0x11ae, 0x11af, 0x11b0,
            0x11b1, 0x11b2, 0x11b3, 0x11b4, 0x11b5, 0x11b6, 0x11b7, 0x11b8, 0x11b9, 0x11ba,
            0x11bb, 0x11bc, 0x11bd, 0x11be, 0x11bf, 0x11c0, 0x11c1, 0x11c2
    };

    public static final int DATA_SET_LIST = 0;
    public static final int ANALYSIS_LIST = 1;
    public static final int DASHBOARD_LIST = 2;
    public static final int GROUP_LIST = 3;
    public static final int QUERY_LIST = 4;
    public static final int REPORT_LIST = 5;


    enum DATA_SET {
        ID("id", "ID"),
        NAME("name", "NAME"),
        SRC_TYPE("srcType", "SRC_TYPE"),
        DATA_TABLE("dataTable", "DATA_TABLE"),
        USE_COLUMNS("useColumns", "USE_COLUMNS"),
        ORIGINAL_COLUMN("originalColumn", "ORIGINAL_COLUMN"),
        CHANGE_COLUMN("changeColumn", "CHANGE_COLUMN"),
        UPDATE_DATE("updateDate", "UPDATE_DATE"),
        CREATE_DATE("createDate", "CREATE_DATE"),
        GROUP_NAME("groupName", "GROUP_NAME"),
        GROUP_ID("groupId", "GROUP_ID");

        private String name;
        private String value;

        DATA_SET(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static String getValue(String name) {
            for (DATA_SET dataSet : DATA_SET.values()) {
                if (dataSet.name.equals(name)) {
                    return dataSet.value;
                }
            }
            return null;
        }
    }

    enum ANALYSIS {
        ID("id", "ID"),
        NAME("name", "NAME"),
        SUB_NAME("subName", "SUB_NAME"),
        DATA_SET_ID("dataSetId", "DATA_SET_ID"),
        SRC_TYPE("srcType", "SRC_TYPE"),
        SRC_NAME("srcName", "SRC_NAME"),
        CNT_CHART("cntChart", "CNT_CHART"),
        DATA_TABLE("dataTable", "DATA_TABLE"),
        CREATE_USER("createUser", "CREATE_USER"),
        UPDATE_DATE("updateDate", "UPDATE_DATE"),
        CREATE_DATE("createDate", "CREATE_DATE"),
        GROUP_NAME("groupName", "GROUP_NAME"),
        GROUP_ID("groupId", "GROUP_ID");

        private String name;
        private String value;

        ANALYSIS(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static String getValue(String name) {
            for (ANALYSIS analysis : ANALYSIS.values()) {
                if (analysis.name.equals(name)) {
                    return analysis.value;
                }
            }
            return null;
        }
    }

    enum DASHBOARD {
        ID("id", "ID"),
        NAME("name", "NAME"),
        SUB_NAME("subName", "SUB_NAME"),
        CNT_DATA_SET("cntDataSet", "CNT_DATA_SET"),
        CNT_CHART("cntChart", "CNT_CHART"),
        CREATE_USER("createUser", "CREATE_USER"),
        UPDATE_DATE("updateDate", "UPDATE_DATE"),
        CREATE_DATE("createDate", "CREATE_DATE"),
        GROUP_NAME("groupName", "GROUP_NAME"),
        GROUP_ID("groupId", "GROUP_ID");

        private String name;
        private String value;

        DASHBOARD(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static String getValue(String name) {
            for (DASHBOARD dashboard : DASHBOARD.values()) {
                if (dashboard.name.equals(name)) {
                    return dashboard.value;
                }
            }
            return null;
        }
    }

    enum REPORT {
        ID("id", "ID"),
        NAME("name", "NAME"),
        CREATE_USER("createUser", "CREATE_USER"),
        UPDATE_DATE("updateDate", "UPDATE_DATE"),
        CREATE_DATE("createDate", "CREATE_DATE"),
        GROUP_NAME("groupName", "GROUP_NAME"),
        GROUP_ID("groupId", "GROUP_ID");

        private String name;
        private String value;

        REPORT(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static String getValue(String name) {
            for (REPORT report : REPORT.values()) {
                if (report.name.equals(name)) {
                    return report.value;
                }
            }
            return null;
        }
    }

    enum GROUP {
        ID("id", "ID"),
        NAME("name", "NAME"),
        SUB_NAME("description", "DESCRIPTION"),
        UPDATE_DATE("updateDate", "UPDATE_DATE"),
        CREATE_DATE("createDate", "CREATE_DATE"),
        GROUP_ID("groupId", "GROUP_ID");

        private String name;
        private String value;

        GROUP(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static String getValue(String name) {
            for (GROUP group : GROUP.values()) {
                if (group.name.equals(name)) {
                    return group.value;
                }
            }
            return null;
        }
    }

    enum QUERY {
        ID("id", "ID"),
        NAME("name", "NAME"),
        GROUP_NAME("groupName", "GROUP_NAME"),
        SUB_NAME("description", "DESCRIPTION"),
        UPDATE_DATE("updateDate", "UPDATE_DATE"),
        CREATE_DATE("createDate", "CREATE_DATE"),
        GROUP_ID("groupId", "GROUP_ID");

        private String name;
        private String value;

        QUERY(String name, String value) {
            this.name = name;
            this.value = value;
        }

        public static String getValue(String name) {
            for (QUERY query : QUERY.values()) {
                if (query.name.equals(name)) {
                    return query.value;
                }
            }
            return null;
        }
    }

    enum REG_CHAR {
        circumflex('^'),
        dollarSign('$'),
        period('.'),
        asterisk('*'),
        plusSign('+'),
        questionMark('?'),
        leftBracket('['),
        rightBracket(']'),
        leftBrace('{'),
        rightBrace('{'),
        leftParenthesis('('),
        rightParenthesis(')'),
        verticalBar('|'),
        exclamationPoint('!'),
        atSign('@'),
        crosshatch('#'),
        ampersand('&'),
        percent('%'),
        backSlash('\\');
        private char name;
        private String value;

        REG_CHAR(char name) {
            this.name = name;
            if (name == '\\') {
                this.value = "\\\\\\\\";
            } else {
                this.value = "\\\\" + name;
            }
        }

        public static String getValue(char name) {
            for (REG_CHAR regChar : REG_CHAR.values()) {
                if (regChar.name == name) {
                    return regChar.value;
                }
            }
            return String.valueOf(name);
        }
    }

    public static void checkParameter(String[] verifyKeys, String[] intParserKeys, Map<String, Object> body) throws Exception {
        Util.verifyRequireParameters(verifyKeys, body);
        if (body.containsKey("error")) {
            throw new BadException(String.format("Not found body: %s", body));
        }
        if (intParserKeys != null) {
            for (String key : intParserKeys) {
                try {
                    int tmp = Integer.parseInt(body.get(key).toString());
                } catch (NumberFormatException e) {
                    throw new BadException(String.format("Invalid body (%s): %s", key, body.get(key)));
                }
            }
        }
    }

    private static void verifyRequireParameters(String[] keys, Map<String, Object> body) {
        ArrayList<String> errList = new ArrayList<>();
        for (String key : keys) {
            String[] tmp = key.split("[.]");
            if (tmp.length > 1) {
                Object sub = body.get(tmp[0]);
                if (sub == null || !(sub instanceof Map) || ((Map<String, Object>) sub).get(tmp[1]) == null) {
                    errList.add(key);
                }
            } else {
                if (body.get(key) == null) {
                    errList.add(key);
                }
            }
        }
        if (errList.size() > 0) {
            body.put("error", errList);
        }
    }

    public static boolean verifyToken(String token, String ssoId, String key) {
        boolean result = true;
        try {
            if (rsaPublicKey == null) {
                rsaPublicKey = publicKeyFromString(key);
            }
            Algorithm algorithm = Algorithm.RSA256(rsaPublicKey, null);
            JWTVerifier verifier = JWT.require(algorithm).withClaim("client_id", ssoId).build();
            verifier.verify(token);
        } catch (Exception e) {
            log.error("verifyToken Error ssoId: {}, token: {}", ssoId, token, e);
            result = false;
        }
        return result;
    }

    public static RSAPublicKey publicKeyFromString(String key) {
        try {
            byte[] pubKeyBytes = java.util.Base64.getDecoder().decode(key);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(pubKeyBytes);
            return (RSAPublicKey) keyFactory.generatePublic(publicKeySpec);
        } catch (Exception e) {
            log.error("Error loading public key", e);
        }
        return null;
    }

    public static Object sendHttp(String api, String method, HashMap<String, String> header, String body, boolean getStream) throws Exception {
        log.info("sendHttp method: {},  api: {}, header: {}, body: {}", method, api, header, body);
        URL url = new URL(api);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestProperty("Content-Type", "application/json");
        if ("PATCH".equals(method)) {
            con.setRequestProperty("X-HTTP-Method-Override", "PATCH");
            method = "POST";
        }
        con.setRequestMethod(method);
        if (header != null) {
            for (String key : header.keySet()) {
                con.setRequestProperty(key, header.get(key));
            }
        }
        con.setDoInput(true);
        if (body != null) {
            byte[] bytes = body.getBytes("utf-8");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.write(bytes, 0, bytes.length);
            wr.flush();
            wr.close();
        }
        if (getStream) {
            int responseCode = con.getResponseCode();
            if (responseCode >= HttpURLConnection.HTTP_OK && responseCode <= HttpURLConnection.HTTP_USE_PROXY) {
                return con.getInputStream();
            } else {
                return getResponse(con);
            }
        } else {
            return getResponse(con);
        }
    }

    private static Object getResponse(HttpURLConnection con) throws Exception {
        boolean error = false;
        Object result = null;
        int responseCode = con.getResponseCode();
        String contentType = con.getContentType();

        InputStream is = null;
        if (responseCode >= HttpURLConnection.HTTP_OK && responseCode <= HttpURLConnection.HTTP_USE_PROXY) {
            is = con.getInputStream();
        } else {
            error = true;
            is = con.getErrorStream();
        }
        if (is != null) {
            BufferedReader in = null;
            if (contentType != null) {
                if (contentType.toLowerCase().indexOf("euc-kr") != -1) {
                    in = new BufferedReader(new InputStreamReader(is, "euc-kr"));
                } else if (contentType.toLowerCase().indexOf("utf-8") != -1) {
                    in = new BufferedReader(new InputStreamReader(is, "utf-8"));
                } else {
                    in = new BufferedReader(new InputStreamReader(is));
                }
            } else {
                in = new BufferedReader(new InputStreamReader(is));
            }

            String inputLine = null;
            StringBuffer sb = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                sb.append(inputLine);
            }
            in.close();

            String response = sb.toString();
            log.info(response);
            if (response != null && !"null".equals(response)) {
                if (response.startsWith("{")) {
                    result = new JSONObject(response);
                } else if (response.startsWith("[")) {
                    result = new JSONArray(response);
                } else {
                    result = response;
                }
            }
            if (error) {
                throw new Exception(response);
            }
        }
        return result;
    }

    public static String getEncoding(List<Map<String, Object>> dataList) {
        String encoding = "UTF-8";
        for (Map<String, Object> data : dataList) {
            for (String key : data.keySet()) {
                String value = String.valueOf(data.get(key));
                if (value != null && value.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*")) {
                    encoding = "EUC-KR";
                    break;
                }
            }
        }
        return encoding;
    }

    public static String getEncodedFilename(String filename, String browser) {
        String encodedFilename = filename;
        try {
            if (browser.equals("MSIE")) {
                encodedFilename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
            } else if (browser.equals("Firefox")) {
                encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
            } else if (browser.equals("Opera")) {
                encodedFilename = "\"" + new String(filename.getBytes("UTF-8"), "8859_1") + "\"";
            } else if (browser.equals("Chrome")) {
                StringBuffer sb = new StringBuffer();
                for (int i = 0; i < filename.length(); i++) {
                    char c = filename.charAt(i);
                    if (c > '~') {
                        sb.append(URLEncoder.encode("" + c, "UTF-8"));
                    } else {
                        sb.append(c);
                    }
                }
                encodedFilename = sb.toString();
            } else {
                throw new RuntimeException("Not supported browser");
            }
        } catch (Exception e) {
        }
        return encodedFilename;
    }

    public static String getBrowser(String userAgent) {
        //String header = request.getHeader("User-Agent");
        if (userAgent.indexOf("MSIE") > -1) {
            return "MSIE";
        } else if (userAgent.indexOf("Chrome") > -1) {
            return "Chrome";
        } else if (userAgent.indexOf("Opera") > -1) {
            return "Opera";
        } else if (userAgent.indexOf("Trident/7.0") > -1) {
            //IE 11 이상 //IE 버전 별 체크 >> Trident/6.0(IE 10) , Trident/5.0(IE 9) , Trident/4.0(IE 8)
            return "MSIE";
        }
        return "Firefox";
    }

    public static void setJsonFromMap(JSONObject json, Map<String, Object> data) throws Exception {
        for (String key : data.keySet()) {
            Object obj = data.get(key);
            if (obj instanceof Map) {
                JSONObject jsonObj = new JSONObject();
                json.put(key, jsonObj);
                setJsonFromMap(jsonObj, (Map) obj);
            } else if (obj instanceof List) {
                JSONArray array = new JSONArray();
                json.put(key, array);
                setJsonFromList(array, (List) obj);
            } else {
                json.put(key, obj);
            }
        }
    }

    public static void setJsonFromList(JSONArray json, List data) throws Exception {
        for (Object obj : data) {
            if (obj instanceof Map) {
                JSONObject jsonObj = new JSONObject();
                json.put(jsonObj);
                setJsonFromMap(jsonObj, (Map) obj);
            } else if (obj instanceof List) {
                JSONArray array = new JSONArray();
                json.put(array);
                setJsonFromList(array, (List) obj);
            } else {
                json.put(obj);
            }
        }
    }

    public static void setMapFromJson(Map<String, Object> map, JSONObject obj) throws Exception {
        Iterator keys = obj.keys();
        while (keys.hasNext()) {
            String key = keys.next().toString();
            Object data = obj.get(key);
            if (data instanceof JSONObject) {
                HashMap<String, Object> subMap = new HashMap<>();
                map.put(key, subMap);
                setMapFromJson(subMap, (JSONObject) data);
            } else if (data instanceof JSONArray) {
                ArrayList<Object> list = new ArrayList<>();
                map.put(key, list);
                setListFromJson(list, (JSONArray) data);
            } else if (data != null && !"null".equals(data.toString())) {
                map.put(key, data);
            }
        }
    }

    public static void setListFromJson(List<Object> list, JSONArray array) throws Exception {
        for (int i = 0; i < array.length(); ++i) {
            Object obj = array.get(i);
            if (obj instanceof JSONObject) {
                HashMap<String, Object> subMap = new HashMap<>();
                list.add(subMap);
                setMapFromJson(subMap, (JSONObject) obj);
            } else if (obj instanceof JSONArray) {
                ArrayList<Object> arrayList = new ArrayList<>();
                list.add(arrayList);
                setListFromJson(arrayList, (JSONArray) obj);
            } else if (obj != null && !"null".equals(obj.toString())) {
                list.add(obj);
            }
        }
    }

    public static Map<String, String> getListParam(JSONObject param, int who) {
        String limit = null;
        if (param.optInt("limit") != 0) {
            limit = String.format("LIMIT %d, %d", param.optInt("offset"), param.optInt("limit"));
        }
        String orderBy = null;
        JSONObject sort = param.optJSONObject("sort");
        if (sort != null) {
            String key = sort.optString("key");
            switch (who) {
                case DATA_SET_LIST:
                    orderBy = String.format("ORDER BY %s %s", DATA_SET.getValue(key), sort.optString(key));
                    break;
                case ANALYSIS_LIST:
                    orderBy = String.format("ORDER BY `%s` %s", ANALYSIS.getValue(key), sort.optString("value"));
                    break;
                case DASHBOARD_LIST:
                    orderBy = String.format("ORDER BY %s %s", DASHBOARD.getValue(key), sort.optString(key));
                    break;
                case REPORT_LIST:
                    orderBy = String.format("ORDER BY %s %s", REPORT.getValue(key), sort.optString(key));
                    break;
                case GROUP_LIST:
                    orderBy = String.format("ORDER BY %s %s", GROUP.getValue(key), sort.optString(key));
                    break;
                case QUERY_LIST:
                    orderBy = String.format("ORDER BY %s %s", QUERY.getValue(key), sort.optString(key));
                    break;
            }
        }
        String where = null;
//        JSONArray filters = null;
//        try {
//            filters = param.getJSONArray("filter");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        JSONArray filters = param.optJSONArray("filter");
        if (filters != null) {
            StringBuffer sb = new StringBuffer();
            sb.append("WHERE ");
            for (int i = 0; i < filters.length(); ++i) {
                JSONObject filter = filters.optJSONObject(i);
                String value = filter.optString("value");
                String key = filter.optString("key");
                if ("".equals(value)) {
                    value = "";
                    key = null;
                }
                if (value != null) {
                    StringBuffer buff = new StringBuffer();
                    for (int j = 0; j < value.length(); ++j) {
                        buff.append(REG_CHAR.getValue(value.charAt(j)));
                    }
                    value = buff.toString();
                }
                if ("groupId".equals(key)) {
                    switch (who) {
                        case DATA_SET_LIST:
                            sb.append(String.format("`%s` = '%s'", DATA_SET.getValue(filter.optString("key")), value));
                            break;
                        case ANALYSIS_LIST:
                            sb.append(String.format("`%s` = '%s'", ANALYSIS.getValue(filter.optString("key")), value));
                            break;
                        case DASHBOARD_LIST:
                            sb.append(String.format("`%s` = '%s'", DASHBOARD.getValue(filter.optString("key")), value));
                            break;
                        case REPORT_LIST:
                            sb.append(String.format("`%s` = '%s'", REPORT.getValue(filter.optString("key")), value));
                            break;
                        case GROUP_LIST:
                            sb.append(String.format("`%s` = '%s'", GROUP.getValue(filter.optString("key")), value));
                            break;
                        case QUERY_LIST:
                            sb.append(String.format("`%s` = '%s'", QUERY.getValue(filter.optString("key")), value));
                            break;
                    }
                } else {
                    switch (who) {
                        case DATA_SET_LIST:
                            sb.append(String.format("`%s` REGEXP '%s'", DATA_SET.getValue(filter.optString("key")), value));
                            break;
                        case ANALYSIS_LIST:
                            sb.append(String.format("`%s` REGEXP '%s'", ANALYSIS.getValue(filter.optString("key")), value));
                            break;
                        case DASHBOARD_LIST:
                            sb.append(String.format("`%s` REGEXP '%s'", DASHBOARD.getValue(filter.optString("key")), value));
                            break;
                        case REPORT_LIST:
                            sb.append(String.format("`%s` REGEXP '%s'", REPORT.getValue(filter.optString("key")), value));
                            break;
                        case GROUP_LIST:
                            sb.append(String.format("`%s` REGEXP '%s'", GROUP.getValue(filter.optString("key")), value));
                            break;
                        case QUERY_LIST:
                            sb.append(String.format("`%s` REGEXP '%s'", QUERY.getValue(filter.optString("key")), value));
                            break;
                    }
                }
                sb.append(" AND ");
            }
            where = sb.substring(0, sb.length() - 5);
        }
        Map<String, String> result = new HashMap<>();
        result.put("limit", limit == null ? "" : limit);
        result.put("orderBy", orderBy == null ? "" : orderBy);
        result.put("where", where == null ? "" : where);
        return result;
    }

    public static String getType(String value) {
        String type = "STRING";
        if (value != null && !"".equals(value.trim())) {
            try {
                double dValue = Double.parseDouble(value);
                if (value.indexOf(".") != -1 && dValue % 1 != 0) {
                    type = "FLOAT";
                } else {
                    type = "INTEGER";
                }
            } catch (NumberFormatException e) {
            }
        }
        return type;
    }

    public static List<String> getDataSetList(String query) {
        List<String> list = new ArrayList<>();
        Pattern ptn = Pattern.compile("\\[\\[([a-z-A-Z-\\s0-9]*)\\]\\]");
        Matcher matcher = ptn.matcher(query);
        while (matcher.find()) {
            list.add(matcher.group(1));
        }
        return list;
    }

    public static String getQueryText(String queryText, List<Map<String, Object>> tableList, List<Map<String, Object>> variableList, Map<String, String> variableMap) throws BadException {
        for (Map<String, Object> table : tableList) {
            queryText = queryText.replaceAll(String.format("\\[\\[%d\\]\\]", table.get("ID")), table.get("DATA_TABLE").toString());
        }
        List<String> list = getDataSetList(queryText);
        if (list.size() > 0) {
            throw new BadException(String.format("쿼리에 사용한 데이터 셋 테이블을 찾을 수 없습니다. ID: %s", list.toString()));
        }
        Pattern p = Pattern.compile("\\{\\{(.*?)\\}\\}");
        Matcher m = p.matcher(queryText);
        List<String> paramList = new ArrayList<>();
        while (m.find()) {
            String key = m.group().replace("{{", "").replace("}}", "");
            if (!paramList.contains(key)) {
                paramList.add(key);
            }
        }
        log.info("getQueryText: query - {}, param - {}", queryText, paramList);
        if (variableMap != null) {
            for (String key : variableMap.keySet()) {
                queryText = queryText.replaceAll(String.format("\\{\\{%s\\}\\}", key), variableMap.get(key));
                paramList.remove(key);
            }
            if (paramList.size() > 0) {
                throw new BadException(String.format("쿼리에 사용한 변수가 부족 합니다. %s", paramList.toString()));
            }
        }
        if (variableList != null) {
            for (Map<String, Object> variable : variableList) {
                String value = String.valueOf(variable.get("value"));
                queryText = queryText.replaceAll(String.format("\\{\\{%s\\}\\}", variable.get("name")), value);
            }
        }
        return queryText;
    }

    public static ArrayList<String[]> getColumnInfo(List<Map<String, Object>> dataList) {
        Map<String, String> columnInfo = new TreeMap<>();
        for (Map<String, Object> data : dataList) {
            for (String key : data.keySet()) {
                String type = Util.getType(String.valueOf(data.get(key)));
                String oldType = columnInfo.get(key);
                if (!type.equals(oldType) && !"STRING".equals(oldType)) {
                    if ("STRING".equals(type)) {
                        columnInfo.put(key, type);
                    } else if (!"INTEGER".equals(oldType)) {
                        columnInfo.put(key, type);
                    }
                }
            }
        }
        ArrayList<String[]> column = new ArrayList<>();
        for (String key : columnInfo.keySet()) {
            column.add(new String[]{key, columnInfo.get(key)});
        }
        return column;
    }

    public static String getHangulText(String text) {
        List<Character> charList = new ArrayList<Character>();
        for (int i = 0; i < text.length(); ++i) {
            charList.add(text.charAt(i));
        }
        StringBuffer sb = new StringBuffer();
        int index = 0;
        while (index < text.length()) {
            index = setHangulText(charList, sb, index);
        }
        return sb.toString();
    }

    private static int setHangulText(List<Character> list, StringBuffer sb, int index) {
        int[] indexArray = {-1, -1, 0};
        String cho = new String(CHO_INDEX);
        String jun = new String(JUN_INDEX);
        String jon = new String(JON_INDEX);
        StringBuffer tmp = new StringBuffer();
        if (cho.indexOf(list.get(index)) != -1) {
            tmp.append(list.get(index));
            indexArray[0] = cho.indexOf(list.get(index++));
        } else if (jun.indexOf(list.get(index)) != -1) {
            tmp.append(list.get(index));
            indexArray[1] = jun.indexOf(list.get(index++));
        } else if (jon.indexOf(list.get(index)) != -1) {
            tmp.append(list.get(index));
            indexArray[2] = jon.indexOf(list.get(index++));
        } else {
            sb.append(list.get(index++));
            return index;
        }

        if (jun.indexOf(list.get(index)) != -1) {
            tmp.append(list.get(index));
            indexArray[1] = jun.indexOf(list.get(index++));
        } else if (jon.indexOf(list.get(index)) != -1) {
            tmp.append(list.get(index));
            indexArray[2] = jon.indexOf(list.get(index++));
        }

        if (jon.indexOf(list.get(index)) != -1) {
            tmp.append(list.get(index));
            indexArray[2] = jon.indexOf(list.get(index++));
        }
        if (indexArray[0] == -1 || indexArray[1] == -1) {
            sb.append(tmp.toString());
        } else {
            sb.append((char) (((indexArray[0] * 21) + indexArray[1]) * 28 + indexArray[2] + 0xAC00));
        }
        return index;
    }
}
