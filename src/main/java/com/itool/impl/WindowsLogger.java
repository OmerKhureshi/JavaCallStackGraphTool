package com.itool.impl;

import com.itool.Logger;
import com.itool.exception.LoggerException;

import java.lang.management.ManagementFactory;

public class WindowsLogger extends Logger {

	public WindowsLogger() throws LoggerException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getProcessId() throws LoggerException {
		return ManagementFactory.getRuntimeMXBean().getName().split("@")[0];
	}

	@Override
	protected String getThreadId() throws LoggerException {
		// TODO Auto-generated method stub
		return String.valueOf(Thread.currentThread().getId());
	}

	@Override
	protected String getFilePath() throws LoggerException {
		// TODO Auto-generated method stub
		return System.getProperty("user.home");
	}

}
