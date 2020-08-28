package com.colasoft.tip.network.recon.enums;

import java.util.Arrays;
import java.util.List;

public enum TaskOperationEnums {

    start, stop, delete, cancel;

    public static List<String> getAll() {
        return Arrays.asList(start.name(), stop.name(), delete.name(), cancel.name());
    }

}
