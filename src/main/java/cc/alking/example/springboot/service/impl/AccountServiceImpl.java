package cc.alking.example.springboot.service.impl;

import cc.alking.example.springboot.service.AccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AccountServiceImpl implements AccountService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountServiceImpl.class);

    @Override
    public boolean register(String name, String pwd) {
        LOGGER.info("register name={},pwd={}", name, pwd);
        return true;
    }
}
