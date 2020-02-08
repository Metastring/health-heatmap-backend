package org.metastringfoundation.healthheatmap;

public class MultiplexDataStorer implements DataStorer {
    private Database mongodb;
    private Database postgres;

    public MultiplexDataStorer() {
        this.mongodb = new MongoDB();
        this.postgres = new PostgreSQL();
    }

    @Override
    public void addDataset(Dataset dataset) {
        this.mongodb.addDataset(dataset);
        this.postgres.addDataset(dataset);
    }
}
