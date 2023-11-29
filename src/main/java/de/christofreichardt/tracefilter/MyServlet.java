package de.christofreichardt.tracefilter;

import de.christofreichardt.diagnosis.AbstractTracer;
import de.christofreichardt.diagnosis.LogLevel;
import de.christofreichardt.diagnosis.Traceable;
import de.christofreichardt.diagnosis.TracerFactory;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Logger;

/**
 *
 * @author Developer
 */
@SuppressWarnings("serial")
@WebServlet(name = "MyServlet", urlPatterns = {"/*"})
public class MyServlet extends HttpServlet implements Traceable {

    final static Logger LOGGER = Logger.getLogger("de.christofreichardt.tracefilter");

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        LOGGER.info("HTTP GET request received ...");
        httpServletResponse.setContentType("text/html;charset=UTF-8");
        try (PrintWriter printWriter = httpServletResponse.getWriter()) {
            printWriter.print("<!DOCTYPE html>");
            printWriter.print("<html>");
            printWriter.print("<body>");
            printWriter.print("<h1>This is the time: %s</h1>".formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            printWriter.print("</body>");
            printWriter.print("</html>");
        }
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        AbstractTracer tracer = getCurrentTracer();
        tracer.entry("void", this, "doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)");
        try {
            int requestCounter = (int) httpServletRequest.getAttribute("de.christofreichardt.tracefilter.requestCounter");
            tracer.logMessage(LogLevel.INFO, "%d. request ...".formatted(requestCounter), 
                    getClass(), "doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)");
            String command;
            try (BufferedReader bufferedReader = httpServletRequest.getReader()) {
                command = bufferedReader.readLine();
            }
            tracer.out().printfIndentln("command = %s", command);
            long pause = ThreadLocalRandom.current().nextLong(100L);
            try {
                Thread.sleep(pause);
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
            if ("REQUEST_LocalDateTime".equals(command)) {
                try (PrintWriter printWriter = httpServletResponse.getWriter()) {
                    printWriter.printf("%3d: %s", requestCounter, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
                }
            }
        } finally {
            tracer.wayout();
        }
    }

    @Override
    public AbstractTracer getCurrentTracer() {
        return TracerFactory.getInstance().getCurrentQueueTracer();
    }
    
}
