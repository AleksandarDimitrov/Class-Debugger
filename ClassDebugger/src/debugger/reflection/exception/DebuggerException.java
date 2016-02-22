package debugger.reflection.exception;


public class DebuggerException extends RuntimeException {


	private static final long serialVersionUID = 1L;

	public DebuggerException(String msg) {
		super(msg);
	}

	public DebuggerException(Exception e) {
		super(e);
	}
	
}
