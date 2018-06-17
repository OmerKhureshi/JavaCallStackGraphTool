package com.csgt.dataaccess.DTO;

import java.sql.Date;

public class CallTraceDTO extends BaseDTO{
    private int processId;
    private int threadId;
    private int methodId;
    private String eventType;
    private String params;
    private String lockObjId;
    private Date timeStamp;

    public CallTraceDTO() {
    }

    public CallTraceDTO(int processId, int threadId, int methodId, String eventType, String params, String lockObjId, Date timeStamp) {
        this.processId = processId;
        this.threadId = threadId;
        this.methodId = methodId;
        this.eventType = eventType;
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

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
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
