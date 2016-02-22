package debugger.reflection;

import java.io.Serializable;

public class Pet implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private int age;
	private String name;
	private Pet[] children;
	
	public Pet() {
	}
	
	public Pet(int age, String name) throws IllegalArgumentException {
		this.age = age;
		this.name = name;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) throws IllegalArgumentException {
		if (age < 0)
			throw new IllegalArgumentException("Age can't be negative");
		this.age = age;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Pet[] getChildren() {
		return children;
	}

	public void setChildren(Pet[] children) {
		this.children = children;
	}

	public String toString() {
		return String.format("[Name = %s, Age = %s]", name, age);
	}
		
	private void doSomething(String theSomething){
		// 
	}
	
}
