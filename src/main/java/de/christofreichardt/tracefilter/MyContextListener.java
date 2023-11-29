package de.christofreichardt.tracefilter;

import de.christofreichardt.diagnosis.TracerFactory;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Developer
 */
public class MyContextListener implements ServletContextListener {

    final static Logger LOGGER = Logger.getLogger("de.christofreichardt.tracefilter");

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        LOGGER.info("Context initialized ...");
        LOGGER.info("Context path = %s".formatted(servletContextEvent.getServletContext().getContextPath()));
        InputStream resourceAsStream = MyContextListener.class.getClassLoader().getResourceAsStream("de/christofreichardt/tracefilter/trace-config.xml");
        if (resourceAsStream != null) {
            try {
                TracerFactory.getInstance().reset();
                TracerFactory.getInstance().readConfiguration(resourceAsStream);
                TracerFactory.getInstance().openQueueTracer();
            } catch (TracerFactory.Exception ex) {
                LOGGER.log(Level.WARNING, "Problems occured when reading the tracer configuration.", ex);
            } finally {
                try {
                    resourceAsStream.close();
                } catch (IOException ex) {
                }
            }
        } else {
            LOGGER.warning("Missing tracer configuration.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        TracerFactory.getInstance().closeQueueTracer();
        LOGGER.info("Context destroyed ...");
    }

}
