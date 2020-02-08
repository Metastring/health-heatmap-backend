package org.metastringfoundation.healthheatmap;

import org.apache.commons.cli.*;

public class CLI {
    //TODO convert the other builder in this project to static class
    final static Option datasetName = Option.builder("n")
            .hasArg()
            .longOpt("name")
            .build();
    final static Option path = Option.builder("p")
            .hasArg()
            .longOpt("path")
            .desc("Path to the file")
            .build();

    final static Option rangeReference = Option.builder("x")
            .hasArg()
            .longOpt("ranges")
            .desc("Specially formatted string that shows range to the data (B0:0,A1:A,B1: )")
            .build();

    final static Option random = new Option("r", "random", false, "Generate random data");
    final static Option profiler = new Option("s", "profiler", true, "Profiler");

    public final static Options options = new Options()
            .addOption(path)
            .addOption(datasetName)
            .addOption(rangeReference)
            .addOption(random)
            .addOption(profiler)
            ;

    public CommandLine parse(String[] args) throws IllegalArgumentException, ParseException {

        final CommandLineParser commandLineParser = new DefaultParser();
        try {
            return commandLineParser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e);
            throw e;
        }
    }

    public static void printHelp() {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("data-reader", options);
    }
}
