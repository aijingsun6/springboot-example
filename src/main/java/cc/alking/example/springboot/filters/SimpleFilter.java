package cc.alking.example.springboot.filters;

import jakarta.servlet.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;

//@Component
public class SimpleFilter implements Filter{

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleFilter.class);


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        LOGGER.info("{}", filterConfig);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        LOGGER.info("{}", request);
        LOGGER.info("{}", response);
        LOGGER.info("{}", chain);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
        LOGGER.info("destroy");
    }
}
