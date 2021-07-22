package com.sk.bds.datainsight.service;

import com.sk.bds.datainsight.database.dao.DashboardDao;
import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.User;
import com.sk.bds.datainsight.exception.InternalException;
import com.sk.bds.datainsight.util.DataSourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;

@Service
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    @Autowired
    UserDao userDao;

    @Value("${summary.db.url}")
    String url;
    @Value("${summary.db.port}")
    String port;
    @Value("${summary.db.username}")
    String username;
    @Value("${summary.db.password}")
    String password;

    private final String[] INIT_TABLE = {"CHART_INFO"};

    @Transactional(propagation = Propagation.REQUIRED, transactionManager = "transactionManager", rollbackFor = Exception.class)
    public User firstInit(int userId, String ssoId) throws Exception {
        if (userId != -1) {
            //throw new BadException("Already init user");
            log.warn("firstInit warn: Already init user. ssoId={}, userId={}", ssoId, userId);
            return null;
        }

        String dbName = String.format("BI_%s", ssoId);
        JdbcTemplate jdbcTemplate = new JdbcTemplate(DataSourceManager.getDataSource(url, port, "", username, password));
        jdbcTemplate.execute(String.format("CREATE DATABASE %s", dbName));

        HashMap<String, Object> param = new HashMap<>();
        param.put("ssoId", ssoId);
        param.put("limitSize", 10);
        param.put("dbUrl", url);
        param.put("dbPort", port);
        param.put("dbId", username);
        param.put("dbPwd", password);
        param.put("dbName", dbName);
        userDao.insertUser(param);
        User user = userDao.getUserBySsoId(ssoId);
        DashboardDao dao = new DashboardDao(DataSourceManager.getDataSource(url, port, dbName, username, password));
        try {
            dao.createInitTable();
            for (String tableName : INIT_TABLE) {
                dao.insertInitData(tableName, userDao.getTableData(tableName));
            }
        } catch (Exception e) {
            log.error("firstInit error: {}", e.getMessage());
            jdbcTemplate.execute(String.format("DROP DATABASE %s", dbName));
            InternalException ie = new InternalException(e);
            ie.setMessage("서비스 시작에 실패 하였습니다.\n자세한 내용은 관리자에게 문의 바랍니다.");
            throw ie;
        } finally {
            dao.close();
            jdbcTemplate.getDataSource().getConnection().close();
        }

        return user;
    }
}
