package org.metastringfoundation.healthheatmap;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

public class Main {

    public static void main(String[] args) throws IllegalArgumentException {
        try {
            CommandLine commandLine = new CLI().parse(args);

            DataStorer dataStorer = new MultiplexDataStorer();

            String path = commandLine.getOptionValue("path");
            String datasetName = commandLine.getOptionValue("name");
            String rangeReference = commandLine.getOptionValue("ranges");
            boolean random = commandLine.hasOption("random");
            boolean profiler = commandLine.hasOption("profiler");
            String profilerAction = commandLine.getOptionValue("profiler");

            if (random) {
                Dataset dataset;
                int GENERATE_THIS_MANY_DATASETS = 20;
                for (int datasetNumber = 0; datasetNumber < GENERATE_THIS_MANY_DATASETS; datasetNumber++) {
                    dataset = new RandomDataset.builder()
                            .entities(500)
                            .indicators(500)
                            .name("Random Dataset " + Integer.toString(datasetNumber))
                            .build();

                    System.out.println("adding dataset " + datasetNumber);
                    dataStorer.addDataset(dataset);
                }
            } else if (profiler) {
                System.out.println("Starting profiler...");
                new DatabaseProfiler(profilerAction).run();
                System.out.println("Ended profiling.");
            } else {
                Dataset dataset = new CSVDataset
                        .builder()
                        .metadata(new DatasetMetadata(datasetName))
                        .path(path)
                        .ranges(rangeReference)
                        .build();

                dataStorer.addDataset(dataset);
            }

        } catch (ParseException e) {
            CLI.printHelp();
            System.exit(1);
        } catch (DatasetIntegrityError e) {
            System.out.println("The data has some problem");
            e.printStackTrace();
        }
    }
}
