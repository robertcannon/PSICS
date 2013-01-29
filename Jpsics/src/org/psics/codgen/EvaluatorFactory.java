package org.psics.codgen;

import java.io.StringReader;

import org.codehaus.janino.SimpleCompiler;
import org.psics.be.E;

public class EvaluatorFactory {

    static EvaluatorFactory instance;


    public static EvaluatorFactory get() {
        if (instance == null) {
			instance = new EvaluatorFactory();
		}
		return instance;
    }



	public EvaluatorFactory() {
		  // EnumeratorSet dbginf = DebuggingInformation.DEFAULT_DEBUGGING_INFORMATION;

	     //   classLoader = new CachingJavaSourceClassLoader(scl, searchPath, null, cacheDir, dbginf);
	      // classLoader = new JavaSourceClassLoader(scl, searchPath, null, dbginf);
	}



	public Object instantiateFromString(String cname, String srcCode) {
		SimpleCompiler sc = new SimpleCompiler();
		sc.setParentClassLoader(getClass().getClassLoader());
		Object ret = null;
		try {
			sc.cook(new StringReader(srcCode));

			Class<?> c = sc.getClassLoader().loadClass(cname);
			ret = c.newInstance();

		} catch (Exception ex) {
			E.error("cant build evaluator: " + ex + ExceptionReporter.getReadableCause(ex));
			E.info("While compiling " + cname + "\n");
			E.info("The source code is " + srcCode);
		}

		return ret;

	}








}
