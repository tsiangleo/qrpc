package com.qrpc.util;

import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
	private Class clazz;

	private Object object;

	private ReflectionUtils() {

	}

	/**
	 * construct method
	 * 
	 * @param obj
	 */
	public ReflectionUtils(Object obj) {
		this.object = obj;
		clazz = obj.getClass();
	}

	/**
	 * construct method
	 * 
	 * @param className
	 * @throws Exception
	 */
	public ReflectionUtils(String className) throws Exception {
		if (className == null)
			clazz = null;
		else
			clazz = Class.forName(className);
		this.object = clazz.newInstance();
	}

	/**
	 * 执行对象方法 当所执行的函数的参数包含基本类型的包装类是，必须为所有参数定义其参数类型
	 * 
	 * @param methodName
	 *            方法名
	 * @param args
	 *            参数
	 * @param types
	 *            当函数的参数包含基本类型的包装类时，此参数包含所以参数的类型信息
	 * @return 方法返回值
	 * @throws Exception
	 */
	public Object invoke(String methodName, Object[] args, Class[] types)
			throws Exception {
		Class[] parameterTypes = null;
		if (types == null)
			parameterTypes = getParameterTypes(args);
		else
			parameterTypes = types;
		Method method = clazz.getDeclaredMethod(methodName, parameterTypes);
		method.setAccessible(true);
		return method.invoke(object, args);
	}

	/**
	 * 
	 * 返回参数数组中各个元素的class对象，注意：当args参数中的元素是基本类型时，返回的是“基本类型.class”，而不是“包装类型.class”。
	 * @param args 参数数组
	 * @return
	 * @throws Exception
	 */
	public static Class[] getParameterTypes(Object[] args) {
		if (args == null) {
			return null;
		}
		Class[] parameterTypes = new Class[args.length];
		for (int i = 0, j = args.length; i < j; i++) {
			if (args[i] instanceof Integer) {
				parameterTypes[i] = Integer.TYPE;
			} else if (args[i] instanceof Byte) {
				parameterTypes[i] = Byte.TYPE;
			} else if (args[i] instanceof Short) {
				parameterTypes[i] = Short.TYPE;
			} else if (args[i] instanceof Float) {
				parameterTypes[i] = Float.TYPE;
			} else if (args[i] instanceof Double) {
				parameterTypes[i] = Double.TYPE;
			} else if (args[i] instanceof Character) {
				parameterTypes[i] = Character.TYPE;
			} else if (args[i] instanceof Long) {
				parameterTypes[i] = Long.TYPE;
			} else if (args[i] instanceof Boolean) {
				parameterTypes[i] = Boolean.TYPE;
			} else {
				parameterTypes[i] = args[i].getClass();
			}
		}
		return parameterTypes;
	}

	public void print(Integer i, int j) {
		System.out.println("Integer: " + i.intValue());
		System.out.println("int: " + j);
	}

	public void print(int i) {
		System.out.println("int: " + i);
	}

	public static boolean isWrapClass(Class clz) {
		try {
			return ((Class) clz.getField("TYPE").get(null)).isPrimitive();
		} catch (Exception e) {
			return false;
		}
	}

	public static void main(String[] args) throws Exception {
		ReflectionUtils r = new ReflectionUtils(new ReflectionUtils());
		Object[] obj = new Object[2];
		Integer I = new Integer(333);
		obj[0] = I;
		obj[1] = 444;
		Class[] clazz = new Class[2];
		clazz[0] = Integer.class;
		clazz[1] = int.class;
		r.invoke("print", obj, clazz);
	}

	public static void main2(String[] args) throws Exception {

		int a = 1;
		int b = 2;
		Integer c = 3;
		Integer d = 3;
		int[] objects = { a, b, c, d, 2 };

		for (int i = 0; i < objects.length; i++) {
			System.out.println();
			// System.out.println(objects[i].getClass());
			// if( objects[i].getClass() == int.class){
			// System.out.println("int.class");
			// }else if(objects[i].getClass() == Integer.class){
			// System.out.println("int.class");
			// }else if(objects[i] instanceof Integer){
			// System.out.println("Integer");
			// }
		}

		// System.out.println((char)65);
		// System.out.println(isWrapClass(Long.class));
		// System.out.println(isWrapClass(Integer.class));
		// System.out.println(isWrapClass(String.class));
		// System.out.println(isWrapClass(ReflectionUtils.class));
		// System.out.println(isWrapClass(int.class));
	}
}
