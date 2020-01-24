package org.metastringfoundation.healthheatmap;

public class DatasetIntegrityError extends Exception {
    DatasetIntegrityError(Exception e) {
        super("DatasetIntegrityError", e);
    }
    DatasetIntegrityError(String msg) {
        super(msg);
    }
}