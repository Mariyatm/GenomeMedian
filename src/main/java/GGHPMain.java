import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import algo.graph.DuplicatedGenome;
import algo.graph.Neighbours;
import algo.graph.OrdinaryGenome;
import algo.graph.TwoRegularNeighbours;
import algo.guided_problems.GGHPGraph;
import algo.guided_problems.gghp.Solver;
import algo.solver.Solution;
import genome.Genome;
import graph.BreakpointGraph;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import static io.Grimm.Reader.readFile;

public class GGHPMain {

    public static void solveProblem(String dirPath, int timeLimit, boolean isRestricted) throws Exception {
        String resultedName = "GGHPResult";
        if (isRestricted) {
            resultedName = "C" + resultedName;
        }
        solveProblem(dirPath, dirPath + "/" + resultedName + ".gen", timeLimit, isRestricted);
    }

    public static void solveProblem(String dirPath, String resultedPath, int timeLimit, boolean isRestricted) throws Exception {
        String pathOrd = dirPath + "/ord.gen";
        String pathWGD = dirPath + "/wgd.gen";
        solveProblem(pathOrd, pathWGD, resultedPath, timeLimit, isRestricted);
    }

    public static void solveProblem(String pathOrd, String pathWGD, String resultedPath, int timeLimit, boolean isRestricted) throws Exception {

        Genome ordGenome = readFile(pathOrd).get(0);
        Genome wgdGenome = readFile(pathWGD).get(0);
        String nameTest = "ord, wgd: " + ordGenome.getName() + ", " + wgdGenome.getName();

        BreakpointGraph graph = new BreakpointGraph();
        Neighbours ordNeigbours = graph.addGenome(ordGenome, "ord");
        Neighbours wgdNeigbours = graph.addGenome(wgdGenome, "wgd");


        Solver solver = tryToSolve(ordNeigbours, wgdNeigbours, timeLimit, isRestricted);

        Solution solution = solver.getCurrentSolution();
        Common.writeResult(graph, solution, solver.getDistance(), resultedPath, isRestricted, "GGHP", nameTest);
    }

    public static algo.guided_problems.gghp.Solver tryToSolve(Neighbours ordNbrs, Neighbours wgdNbrs, int timeLimit, boolean isRestricted) throws Exception {
        DuplicatedGenome baseGenome = new DuplicatedGenome(new TwoRegularNeighbours(wgdNbrs.neighbours));
        OrdinaryGenome guidedGenome = new OrdinaryGenome(new Neighbours(ordNbrs.neighbours, 1));
        GGHPGraph graph = new GGHPGraph(baseGenome, guidedGenome);

        algo.guided_problems.gghp.Solver solver = new Solver(graph);
        if (timeLimit != -1) {
            solver.solveWithLimit(timeLimit, isRestricted);
        }
        solver.solve();
        return solver;
    }

    public static void solveAllFromDir(String dirProblems, int timeLimit, boolean isRetricted) throws IOException {
        Files.list(new File(dirProblems).toPath()).sorted()
                .forEach(problemPath -> {
                    try {
                        solveProblem(String.valueOf(problemPath), timeLimit, isRetricted);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
    }

    public static void main(String[] args) throws Exception {
        int timeLimit = 60 * 60 * 2; // 2 hours
        boolean isRestricted = false;

        Options options = new Options();

        options
                .addOption("o", "ord", true, "Path to ordinary genome B")
                .addOption("w", "wgd", true, "Path to duplicated genome A")
                .addOption("r", "result", true, "Resulted path")
                .addOption("t", "time", true, "Time limit for solving problem (in seconds), default 60*60*2")
                .addOption("p", "restricted",false, "Flag for using restricted model, default = false")
                .addOption("z", "dir", true,"Solve all problems from dir (use instead o, w, r)");

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();

        try {
            CommandLine cmd = parser.parse(options, args);

            String ordPath = cmd.getOptionValue("ord");
            String wgdPath = cmd.getOptionValue("wgd");
            String resultedPath = cmd.getOptionValue("result");
            timeLimit = (int) Long.parseLong(cmd.getOptionValue("time", String.valueOf(timeLimit))) * 1000;
            isRestricted = cmd.hasOption("restricted");
            String dir = cmd.getOptionValue("dir");
            if (ordPath != null && wgdPath != null && resultedPath != null) {
                solveProblem(ordPath, wgdPath, resultedPath, timeLimit, isRestricted);
            } else if (dir != null) {
                solveAllFromDir(dir, timeLimit, isRestricted);
            } else {
                throw new ParseException("Cannot parse paths");
            }

        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("GGHPMain", options);

            System.exit(1);
        }
    }

}
