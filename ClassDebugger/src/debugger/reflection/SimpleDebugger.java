package debugger.reflection;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;

import debugger.reflection.exception.DebuggerException;

public class SimpleDebugger implements Debugger {

	private static final String INFO_MSG_TEMPLATE = 
			"Class : %s\nObject: %s\nImplements: %s\nInherits from: %s\nFields:\n\t%s\nConstructors:\n\t%s\nMethods:\n\t%s ";
	private static final String CONSTRUCTOR_INFO_TEMPLATE = "[%s] %s (%s) %s";
	private static final String METHOD_INFO_TEMPLATE = "[%s] %s %s(%s) %s";
	private static final String FIELD_PRIMITIVE_INFO_TEMPLATE = "[%s] %s: %s = %s";
	private static final String FIELD_ARRAY_INFO_TEMPLATE = "[%s] %s: array[%s] of %s {\n\t%s\n}";
	
	@Override
	public void printInstanceInfo(Object instance) {
		String debugMessage = getInstanceInfo(instance);
		System.out.println(debugMessage);
	}
	
	@Override
	public String getInstanceInfo(Object instance) {
		if (instance == null){
			return null;
		}
		
		String className = getClassName(instance);
		String hashCode = getHasCode(instance);
		String implementers = getImplementers(instance);
		String superClass = getSuperClass(instance);
		String fields = getFields(instance);
		String constructors = getConstructors(instance);
		String methods = getMethods(instance);
		String msg = 
				String.format(INFO_MSG_TEMPLATE, 
						className, hashCode, implementers, superClass,
						fields, constructors, methods);
		return msg;
	}


	private String getMethods(Object instance) {
		Class<? extends Object> clazz = instance.getClass();
		StringBuilder sb = new StringBuilder();
		Method[] methods = clazz.getDeclaredMethods();
		for(int i=0;i < methods.length; i++){
			Method method = methods[i];
			String modifier = getModifier(method.getModifiers());
			String returnType = method.getReturnType().getName();
			String methodName = method.getName();
			String params = getParameters(method.getParameters());
			String exceptions = getExceptionTypes(method.getExceptionTypes());
			sb.append(String.format(
					METHOD_INFO_TEMPLATE, 
					modifier, returnType, methodName,
					params, exceptions ));
			if ( i < methods.length - 1)
				sb.append("\n\t");
		}
		
		return sb.toString();
	}


	private String getConstructors(Object instance) {
		Class<? extends Object> clazz = instance.getClass();
		Constructor<?>[] constructors = clazz.getConstructors();
		StringBuilder sb = new StringBuilder();
		for (int i = 0;i < constructors.length;i++){
			Constructor<?> constructor = constructors[i];
			int modifierMode = constructor.getModifiers();
			String modifier = getModifier(modifierMode);
			String paramStr = getParameters(constructor.getParameters());
			String exceptions = getExceptionTypes(constructor.getExceptionTypes());
			String constructorInfo = 
					String.format(CONSTRUCTOR_INFO_TEMPLATE, 
							modifier, constructor.getName(), paramStr, exceptions);
			sb.append(constructorInfo);
			if ( i < constructors.length - 1)
				sb.append("\n\t");
		}
		
		return sb.toString();
	}

	private String getExceptionTypes(Class<?>[] exceptionTypes){
		StringBuilder sb = new StringBuilder();
		if (exceptionTypes != null && exceptionTypes.length > 0){
			sb.append("throws ");
			for (int i = 0; i < exceptionTypes.length; i++){
				sb.append(exceptionTypes[i].getName());
				if (i < exceptionTypes.length - 1)
					sb.append(", ");
			}
		}
		return sb.toString();
	}
	
	private String getParameters(Parameter[] parameters) {
		StringBuilder paramStr = new StringBuilder();
		for (int i = 0; i < parameters.length; i++) {
			Parameter param = parameters[i];
			paramStr.append(String.format("%s:%s", param.getName(), param.getType().getName()));
			if (i < parameters.length - 1)
				paramStr.append(", ");
		}
		return paramStr.toString();
	}
	
	private String getModifier(int modiferMode){
		if (Modifier.isPrivate(modiferMode))
			return "-";
		if (Modifier.isProtected(modiferMode))
			return "#";
		if (Modifier.isPublic(modiferMode))
			return "+";
		if (Modifier.isFinal(modiferMode))
			return "F";
		if (Modifier.isStatic(modiferMode))
			return "S";
		
		throw new DebuggerException("Unsupported modifer mode " + modiferMode );
	}

	private String getFields(Object instance) {
		Class<? extends Object> clazz = instance.getClass();
		Field[] fields = clazz.getDeclaredFields();
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < fields.length ;i++){
			Field field = fields[i];
			Class<?> type = field.getType();
			if (type.isPrimitive()){
				sb.append(buildPrimitiveField(field, instance));
			}else if (type.isArray()){
				sb.append(buildArrayField(field, instance));
			}else  if (Object.class.isAssignableFrom(type)){ // is a reference type
				sb.append(buildReferenceField(field, instance));
			}
			if (i < fields.length - 1)
				sb.append("\n\t");
		} 
		
		return sb.toString();
	}

	private String buildReferenceField(Field field, Object instance) {
		try {
			String modifier = getModifier(field.getModifiers());
			String name = field.getName();
			String type = field.getType().getName();
			field.setAccessible(true);
			String value = String.valueOf( field.get(instance) );
			return String.format("[%s] %s: %s = %s", modifier, name, type, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new DebuggerException(e);
		}
	}

	private String buildArrayField(Field field, Object instance) {
		try {
			String modifier = getModifier(field.getModifiers());
			String fieldName = field.getName();
			field.setAccessible(true);
			Object array = field.get(instance);
			int length = Array.getLength(array);
			String elements = buildArrayElements(length, array, field, instance);
			return String.format(
					FIELD_ARRAY_INFO_TEMPLATE, 
					modifier, fieldName, length, "class name", elements);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new DebuggerException(e);
		}
	}


	private String buildArrayElements(int arrayLegth, Object array, Field field, Object instance) {
		StringBuilder sb = new StringBuilder("\t");
		for (int i = 0; i < arrayLegth ;i++){
			Object o = Array.get(array, i);
			sb.append(String.format("[%s] = %s", i, o));
			if (i <  arrayLegth  - 1){
				sb.append("\n\t\t");
			}
		}
		return sb.toString();
	}


	private String buildPrimitiveField(Field field, Object instance){
		try {
			String modifier = getModifier(field.getModifiers());
			String fieldName = field.getName();
			String type = field.getType().getName();
			field.setAccessible(true);
			String value = String.valueOf(field.get(instance));
			return String.format(
					FIELD_PRIMITIVE_INFO_TEMPLATE, 
					modifier, fieldName, type, value);
		} catch (IllegalArgumentException | IllegalAccessException e) {
			throw new DebuggerException(e);
		}
	}

	private String getSuperClass(Object instance) {
		Class<? extends Object> clazz = instance.getClass();
		return clazz.getSuperclass().getName();
	}

	private String getImplementers(Object instance) {
		Class<? extends Object> clazz = instance.getClass();
		Class<?>[] interfaces = clazz.getInterfaces();
		if (interfaces == null || interfaces.length == 0)
			return "";
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i< interfaces.length ;i++){
			Class<?> implementedInterface = interfaces[i]; 
			sb.append(implementedInterface.getName());
			if (i < interfaces.length - 1){
				sb.append(", ");
			}
		}
		return sb.toString();
	}

	private String getHasCode(Object instance) {
		return String.valueOf(instance.hashCode());
	}

	private String getClassName(Object instance) {
		Class<? extends Object> clazz = instance.getClass();
		return clazz.getName();
	}

}
