package cc.alking.example.springboot.controller;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.ZoneId;
import java.util.*;

@RestController
public class IndexController {

    private static final Logger LOGGER = LoggerFactory.getLogger(IndexController.class);

    @GetMapping(path = "/index", consumes = "*/*")
    public Object index(ServletRequest request, ServletResponse response, HttpSession session, HttpMethod httpMethod, Locale locale,
                        TimeZone timeZone, ZoneId zoneId){
        LOGGER.info("method: {}",httpMethod);
        LOGGER.info("locale: {}",locale);
        LOGGER.info("timeZone: {},zoneId: {}",timeZone,zoneId);
        LOGGER.info("request: {}",request);
        LOGGER.info("response: {}",response);
        logSession(session);
        return Map.of("status","OK");
    }

    private void logSession(HttpSession session){
        ToStringBuilder builder = new ToStringBuilder(session)
                .append("id", session.getId())
                .append("createTime", session.getCreationTime())
                .append("lastAccessTime", session.getLastAccessedTime())
                .append("maxInactiveInterval", session.getMaxInactiveInterval())
                .append("isNew", session.isNew());
        Map<String,Object> attributes = new HashMap<>();
        Enumeration<String> enumeration = session.getAttributeNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            Object value = session.getAttribute(name);
            attributes.put(name, value);
        }
        builder.append("attributes",attributes);
        LOGGER.info("session detail: {}", builder);
    }

    @GetMapping(path = "/error", consumes = "*/*")
    public Map<String, Object> handle(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", request.getAttribute("jakarta.servlet.error.status_code"));
        map.put("reason", request.getAttribute("jakarta.servlet.error.message"));
        return map;
    }
}
