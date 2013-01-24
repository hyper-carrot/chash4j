package chash4j;

import org.apache.log4j.Logger;

public class MyLoggerDefault implements MyLogger {

	public Logger logger = null;

	MyLoggerDefault() {
		this("");
	}

	MyLoggerDefault(String configId) {
		if ((configId == null) || (configId.length() == 0)) {
			logger = Logger.getRootLogger();
		} else {
			logger = Logger.getLogger(configId);
		}
	}

	MyLoggerDefault(Class<?> clazz) {
		if (clazz == null) {
			logger = Logger.getRootLogger();
		} else {
			logger = Logger.getLogger(clazz);
		}
	}
	
	public boolean isDebugEnabled() {
		return logger.isDebugEnabled();
	}
	
	public void debug(Object message) {
		if (logger.isDebugEnabled()) {
			logger.debug(genarateReporterInfo() + message);
		}
	}
	
	public void debug(Object message, Throwable t) {
		if (logger.isDebugEnabled()) {
			logger.debug(genarateReporterInfo() + message, t);
		}
	}
	
	public void info(Object message) {
		if (logger.isInfoEnabled()) {
			logger.info(genarateReporterInfo() + message);
		}
	}
	
	public void info(Object message, Throwable t) {
		if (logger.isInfoEnabled()) {
			logger.info(genarateReporterInfo() + message, t);
		}
	}
	
	public void warn(Object message) {
		logger.warn(genarateReporterInfo() + message);
	}
	
	public void warn(Object message, Throwable t) {
		logger.warn(genarateReporterInfo() + message, t);
	}
	
	public void error(Object message) {
		logger.error(genarateReporterInfo() + message);
	}
	
	public void error(Object message, Throwable t) {
		logger.error(genarateReporterInfo() + message, t);
	}
	
	public void fatal(Object message) {
		logger.fatal(genarateReporterInfo() + message);
	}
	
	public void fatal(Object message, Throwable t) {
		logger.fatal(genarateReporterInfo() + message, t);
	}

	private String genarateReporterInfo() {
		String reporterInfo = "";
		Throwable throwable = new Throwable().fillInStackTrace();
		StackTraceElement[] stackTraceElements = throwable.getStackTrace();
		if ((stackTraceElements != null) && (stackTraceElements.length > 3)) {
			StackTraceElement invokerStackTraceElement = stackTraceElements[2];
			StringBuilder reporterInfoBuf = new StringBuilder();
			reporterInfoBuf.append(invokerStackTraceElement.getClassName());
			reporterInfoBuf.append(".").append(invokerStackTraceElement.getMethodName());
			reporterInfoBuf.append("(").append(invokerStackTraceElement.getFileName());
			reporterInfoBuf.append(":").append(invokerStackTraceElement.getLineNumber()).append(")");
			reporterInfoBuf.append(" -- ");
			reporterInfo = reporterInfoBuf.toString();
		}
		return reporterInfo;
	}

}
