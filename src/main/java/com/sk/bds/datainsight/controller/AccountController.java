package com.sk.bds.datainsight.controller;

import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.AccountInitInfo;
import com.sk.bds.datainsight.database.model.User;
import com.sk.bds.datainsight.exception.BadException;
import com.sk.bds.datainsight.response.ResponseBase;
import com.sk.bds.datainsight.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {

    @Autowired
    AccountService service;

    @Autowired
    UserDao userDao;

    @RequestMapping(value = "/account/init", method = RequestMethod.PUT, consumes = "application/json")
    public Object firstInit(@RequestBody AccountInitInfo info) throws Exception {
        String ssoId = info.getUserId();
        if (ssoId == null) {
            throw new BadException("mandatory-field(userId) is missing");
        }

        int userId = -1;
        User user = userDao.getUserBySsoId(ssoId);
        if (user != null) {
            userId = user.getId();
        }

        service.firstInit(userId, ssoId);
        ResponseBase res = new ResponseBase();
        return res.getRes();
    }
}
