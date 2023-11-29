package de.christofreichardt.tracefilter;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.TracerFactory;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.annotation.WebInitParam;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;

/**
 *
 * @author Developer
 */
@WebFilter(filterName = "MyTraceFilter", servletNames = {"MyServlet"}, initParams = {
    @WebInitParam(name = "tracing", value = "on")
})
public class MyTraceFilter implements Filter {

    final static Logger LOGGER = Logger.getLogger("de.christofreichardt.tracefilter");

    FilterConfig filterConfig;
    final AtomicInteger atomicInteger = new AtomicInteger();

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        this.filterConfig = filterConfig;
        LOGGER.info(() -> "tracing = %s".formatted(this.filterConfig.getInitParameter("tracing")));
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        int requestCounter = this.atomicInteger.getAndIncrement();
        servletRequest.setAttribute("de.christofreichardt.tracefilter.requestCounter", requestCounter);
        if ("on".equalsIgnoreCase(this.filterConfig.getInitParameter("tracing"))) {
            AbstractTracer tracer = TracerFactory.getInstance().takeTracer();
            tracer.initCurrentTracingContext();
            tracer.entry("void", this, "doFilter(ServletRequest request, ServletResponse response, FilterChain chain)");
            try {
                tracer.out().printfIndentln("method = %s", httpServletRequest.getMethod());
                tracer.out().printfIndentln("contextPath = %s", httpServletRequest.getContextPath());
                tracer.out().printfIndentln("requestCounter = %d", requestCounter);
                filterChain.doFilter(servletRequest, servletResponse);
            } finally {
                tracer.wayout();
            }
        } else {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

}
