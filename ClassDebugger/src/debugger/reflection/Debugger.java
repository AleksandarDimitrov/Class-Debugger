package debugger.reflection;

public interface Debugger {

	/**
	 * @param instance
	 */
	void printInstanceInfo(Object instance);
	
	/**
	 * @param instance
	 * @return
	 */
	String getInstanceInfo (Object instance);
	
}
