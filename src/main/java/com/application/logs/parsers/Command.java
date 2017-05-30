package com.application.logs.parsers;

import java.util.List;

public interface Command {
    void execute(List<String> brokenLine);
}
