package com.itool.impl;

import com.itool.Logger;
import com.itool.exception.LoggerException;

public class DefaultLogger extends Logger {

	public DefaultLogger() throws LoggerException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	protected String getProcessId() throws LoggerException {
		// TODO Auto-generated method stub
		return "0";
	}

	@Override
	protected String getThreadId() throws LoggerException {
		// TODO Auto-generated method stub
		return "0";
	}

	@Override
	protected String getFilePath() throws LoggerException {
		// TODO Auto-generated method stub
		return "0";
	}

}
