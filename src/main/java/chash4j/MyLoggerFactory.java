package chash4j;

public class MyLoggerFactory {

	public static MyLogger getRootLogger() {
		return new MyLoggerDefault();
	}

	public static MyLogger getLogger(String configId) {
		return new MyLoggerDefault(configId);
	}

	public static MyLogger getLogger(Class<?> clazz) {
		return new MyLoggerDefault(clazz);
	}

}
