package com.fileretrieval.engine;

import java.util.List;

public interface Engine {

    void index(String filesPath);
    List<String> search(List<String> params);
}
