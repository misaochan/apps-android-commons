package fr.free.nrw.commons.category;

import java.util.ArrayList;
import java.util.List;

public interface AsyncResponse {
    void processFinish(List<String> output);
}