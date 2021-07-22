package com.sk.bds.datainsight.interceptor;

import com.sk.bds.datainsight.database.dao.UserDao;
import com.sk.bds.datainsight.database.model.User;
import com.sk.bds.datainsight.exception.AuthException;
import com.sk.bds.datainsight.exception.ForbiddenException;
import com.sk.bds.datainsight.service.AccountService;
import com.sk.bds.datainsight.util.AnonymousCallable;
import com.sk.bds.datainsight.util.Util;
import org.apache.commons.codec.binary.Base64;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    @Autowired
    UserDao userDao;

    @Autowired
    Environment environment;

    @Autowired
    AccountService service;

    @Value("${rsa.public.key}")
    String rsaPublicKey;

    private static final Logger log = LoggerFactory.getLogger(AuthInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }
        AnonymousCallable anonymousCallable = ((HandlerMethod)handler).getMethodAnnotation(AnonymousCallable.class);
        if (anonymousCallable != null) {
            return true;
        }
        String ssoId = "ssoId";
        AuthException authException = null;
        try {
            String jwtToken = request.getHeader("Authorization");
            int index = jwtToken.indexOf("Bearer ");
            if (index != -1) {
                String tokenStr = jwtToken.substring("Bearer ".length());
                String[] tmp = tokenStr.split("\\.");
                String base64EncodedBody = tmp[1];
                Base64 base64Url = new Base64(true);
                JSONObject body = new JSONObject(new String(base64Url.decode(base64EncodedBody)));

                //if token not expired OR skip-expire-check API
                if (body.getLong("exp") * 1000 > System.currentTimeMillis() || request.getRequestURI().contains("/account/init")) {
                    ssoId = body.getString("client_id");
                    if (ssoId == null) {
                        authException =  new AuthException("client_id not found");
                    }
                    else if (!Util.verifyToken(tokenStr, ssoId, rsaPublicKey)) {
                        authException = new AuthException("Invalid token");
                    }
                } else {
                    authException = new AuthException("Expired token");
                }
            } else {
                authException = new AuthException("Invalid token");
            }
        } catch (Exception e) {
            authException =  new AuthException("Token parsing error");
            authException.setExceptionMessage(e.getMessage());
        }
        if(authException != null) {
            setResponseHeader(response);
            throw authException;
        }

        if (request.getRequestURI().contains("/account/init")) {
            return true;
        } else {
            request.setAttribute("ssoId", ssoId);
            request.setAttribute("Authorization", request.getHeader("Authorization"));
            User user = userDao.getUserBySsoId(ssoId);
            if (user == null) {
                creatInitTableForUser(request, ssoId);
            } else {
                request.setAttribute("userId", user.getId());
            }
        }
       return true;
    }

    private void creatInitTableForUser(HttpServletRequest request, String ssoId) throws Exception {
        int initUserId = -1;
        User user = service.firstInit(initUserId, ssoId);
        request.setAttribute("userId", user.getId());

        /*if (request.getRequestURI().contains("/dashboard/init")) {
            request.setAttribute("userId", -1);
        } else {
            ForbiddenException exception = new ForbiddenException("user not found");
            setResponseHeader(response);
            throw exception;
        }*/
    }

    private void setResponseHeader(HttpServletResponse response) {
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // TODO Auto-generated method stub
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // TODO Auto-generated method stub
    }

}
