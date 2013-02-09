package org.psics.util;

public class ClassUtil {


	public static String getUnqualifiedName(Object obj) {
		Class<?> cls = (obj instanceof Class ? (Class)obj : obj.getClass());
		String s = cls.getSimpleName();
		return s;
	}



}
