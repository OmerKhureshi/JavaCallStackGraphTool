package com.application.db.model;

/**
 * Model for a log statement in the Call Trace File.
 */
public class TraceStatement {
    int processId;
    int threadId;
    int methodId;
    String eventType;
    String parameters;

    public TraceStatement(int processId, int threadId, int methodId, String eventType, String parameters) {
        this.processId = processId;
        this.threadId = threadId;
        this.methodId = methodId;
        this.eventType = eventType;
        this.parameters = parameters;
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }
}
