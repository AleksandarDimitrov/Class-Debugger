package debugger.reflection;

public class DebuggerMain {
	
	public static final Pet cat ;
	
	static {
		cat = new Pet(7, "Cat");
		Pet[] kittens = new Pet[]{
		   new Pet(1, "little kitty"),
		   new Pet(2, "bigger kitty")
		};
		cat.setChildren(kittens);
	}

	public static void main(String[] args) {
		Debugger debugger = new SimpleDebugger();
		debugger.printInstanceInfo(cat);
	}
	
}
