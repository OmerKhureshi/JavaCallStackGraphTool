package com.csgt.db.DTO;

import java.sql.Date;

public class CallTraceDTO extends BaseDTO{
    private int processId;
    private int threadId;
    private int methodId;
    private String message;
    private String params;
    private String lockObjId;
    private Date timeStamp;

    public CallTraceDTO() {
    }

    public CallTraceDTO(int processId, int threadId, int methodId, String message, String params, String lockObjId, Date timeStamp) {
        this.processId = processId;
        this.threadId = threadId;
        this.methodId = methodId;
        this.message = message;
        this.params = params;
        this.lockObjId = lockObjId;
        this.timeStamp = timeStamp;
    }

    public int getProcessId() {
        return processId;
    }

    public void setProcessId(int processId) {
        this.processId = processId;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public int getMethodId() {
        return methodId;
    }

    public void setMethodId(int methodId) {
        this.methodId = methodId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public String getLockObjId() {
        return lockObjId;
    }

    public void setLockObjId(String lockObjId) {
        this.lockObjId = lockObjId;
    }

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }
}
