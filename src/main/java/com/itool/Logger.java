package com.itool;

import com.itool.exception.LoggerException;

import java.io.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Defines a common behavior for the loggers.  
 * The class also provides useful functions to write 
 */
public abstract class Logger {

	int methodCount = 1;
	Map<Method, Integer> methodMap = new HashMap<Method, Integer>();

	private PrintWriter printWriterDefinition=null;
	private PrintWriter printWriterCallTrace=null;

	public Logger() throws LoggerException {
		// String timestamp = getCurrentTimestamp();
        String timestamp = String.valueOf(System.currentTimeMillis());
		String fileName =null;
		try {
			fileName = getProcessId() + timestamp;
		} catch (Exception ec) {
			throw new LoggerException("Exception in Logger Initialization:  " + ec.getMessage());
		}
		// create file
		File traceFile = new File(getFilePath(),Constants.I_TOOL + fileName + "_call_trace.txt");
		File methodFile = new File(getFilePath(),Constants.I_TOOL + fileName + "_method_definitions.txt");

		try {
			printWriterDefinition = new PrintWriter(new BufferedWriter(new FileWriter(methodFile, false)));
			// out.print("Method Definitions: \n");
			// out.close();
			printWriterCallTrace = new PrintWriter(new BufferedWriter(new FileWriter(traceFile, false)));
			// out.print("Call Trace: \n");
			// out.close();
		} catch (IOException io) {
			if(printWriterCallTrace != null){
				printWriterCallTrace.close();
			}
			if(printWriterDefinition != null){
				printWriterDefinition.close();
			}
			throw new LoggerException("Exception in Logger Initialization: " + io.getMessage());
		}
	}

	protected abstract String getProcessId() throws LoggerException;

	protected abstract String getThreadId() throws LoggerException;

	protected abstract String getFilePath() throws LoggerException;

	synchronized public void logExit(Method method) throws LoggerException {
		addToMethodDefinition(method);
        StringBuilder builder = new StringBuilder();
        builder.append(getCurrentTimestamp());
        builder.append(Constants.separator);
        builder.append(getProcessId());
        builder.append(Constants.separator);
        builder.append(getThreadId());
        builder.append(Constants.separator);
		builder.append(Constants.EVENT_EXIT);
		System.out.println(builder.toString());
		printWriterCallTrace.println(builder.toString());
		printWriterCallTrace.flush();
	}

	// wait-enter Method
	synchronized public void logCustom(Method method, String message) throws LoggerException {
		addToMethodDefinition(method);
		StringBuilder builder = new StringBuilder();
        builder.append(getCurrentTimestamp());
        builder.append(Constants.separator);
        builder.append(getProcessId());
        builder.append(Constants.separator);
        builder.append(getThreadId());
        builder.append(Constants.separator);
		builder.append(message);
        printWriterCallTrace.println(builder.toString());
        printWriterCallTrace.flush();
	}

	// Method m = Local.class.getEnclosingMethod();
	synchronized public void logEnter(Method method, String[] actParamters) throws LoggerException {

		// methodId | packageName | methodName | return type and arguments
		// 1 |android.app.Activity|findViewById|(I)Landroid/view/View; - method
		// definition
		addToMethodDefinition(method);

		StringBuilder builder = new StringBuilder();
        builder.append(getCurrentTimestamp());
        builder.append(Constants.separator);
        builder.append(getProcessId());
        builder.append(Constants.separator);
        builder.append(getThreadId());
        builder.append(Constants.separator);
        builder.append(Constants.EVENT_ENTER);
        builder.append(Constants.separator);
        builder.append(Constants.OPEN_BRACKET);

		for (int i = 0; i < actParamters.length; i++) {
			builder.append(actParamters[i]);
			if (i != actParamters.length - 1) {
				builder.append(Constants.COMMA);
			}
		}
		builder.append(Constants.CLOSE_BRACKET);
		System.out.println(builder.toString());
		printWriterCallTrace.println(builder.toString());
		printWriterCallTrace.flush();
	}

	private String getCurrentTimestamp() {
	    return String.valueOf(System.currentTimeMillis());
	}

	private synchronized void addToMethodDefinition(Method method) {
		if (!methodMap.containsKey(method)) {
			String methodDef = getMethodDefinition(method);
			System.out.println(methodDef);
			printWriterDefinition.println(methodDef);
			printWriterDefinition.flush();
			methodMap.put(method, methodCount);
			methodCount++;
		}

	}

	private String getMethodDefinition(Method method) {
		StringBuilder builder = new StringBuilder();
		builder.append(methodCount);
		builder.append(Constants.separator);
		builder.append(method.getDeclaringClass().getName());
		builder.append(Constants.separator);
		builder.append(method.getName());
		builder.append(Constants.separator);
		builder.append(Constants.OPEN_CIR_BRACKET);
		for (Class<?> param : method.getParameterTypes()) {
			builder.append(getType(param));
		}
		builder.append(Constants.CLOSE_CIR_BRACKET);
		builder.append(getType(method.getReturnType()));
		return builder.toString();
	}

	private String getType(Class<?> param) {
		if (param.isArray()) {
			return param.getName().replaceAll("\\.", "/");
		} else if (param.isPrimitive()) {
			return getPrimitiveType(param.getName());
		} else {
			return "L" + param.getName().replaceAll("\\.", "/") + ";";
		}
	}

	private String getPrimitiveType(String name) {
		
		if (Constants.INT.equals(name))
			return Constants.JVM_INT;
		else if (Constants.LONG.equals(name))
			return Constants.JVM_LONG;
		else if (Constants.BOOLEAN.equals(name))
			return Constants.JVM_BOOLEAN;
		else if (Constants.DOUBLE.equals(name))
			return Constants.JVM_DOUBLE;
		else if (Constants.SHORT.equals(name))
			return Constants.JVM_SHORT;
		else if (Constants.BYTE.equals(name))
			return Constants.JVM_BYTE;
		else if (Constants.FLOAT.equals(name))
			return Constants.JVM_FLOAT;
		else if (Constants.CHAR.equals(name))
			return Constants.JVM_CHAR;
		else if (Constants.VOID.equals(name))
			return Constants.JVM_VOID;
		else
			return name;
	}
	
	public void close(){
		printWriterDefinition.flush();
		printWriterDefinition.close();
		printWriterCallTrace.flush();
		printWriterCallTrace.close();
	}
}