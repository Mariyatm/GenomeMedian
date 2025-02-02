package algo.ugap.solver.gghp;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import algo.graph.DuplicatedGenome;
import algo.graph.GenomeException;
import algo.graph.OrdinaryGenome;
import algo.guided_problems.GGHPGraph;
import algo.solver.BaseDetector;
import algo.ugap.solver.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class GGHPDetectorTest {

    GGHPGraph getTestGraph() throws GenomeException {
        ArrayList<ArrayList<Integer>> neighbours = new ArrayList<>();
        neighbours.add(new ArrayList<>(Arrays.asList(5, 3)));
        neighbours.add(new ArrayList<>(Arrays.asList(1, 1)));
        neighbours.add(new ArrayList<>(Arrays.asList(2, 2)));
        neighbours.add(new ArrayList<>(Arrays.asList(0, 4)));
        neighbours.add(new ArrayList<>(Arrays.asList(3, 5)));
        neighbours.add(new ArrayList<>(Arrays.asList(0, 4)));

        neighbours.add(new ArrayList<>(Arrays.asList(7, 7)));
        neighbours.add(new ArrayList<>(Arrays.asList(6, 6)));

        neighbours.add(new ArrayList<>(Arrays.asList(9, 13)));
        neighbours.add(new ArrayList<>(Arrays.asList(8, 10)));
        neighbours.add(new ArrayList<>(Arrays.asList(9, 11)));
        neighbours.add(new ArrayList<>(Arrays.asList(10, 12)));
        neighbours.add(new ArrayList<>(Arrays.asList(11, 13)));
        neighbours.add(new ArrayList<>(Arrays.asList(12, 8)));
        DuplicatedGenome baseGenome = new DuplicatedGenome(neighbours);


        OrdinaryGenome guidedGenome = new OrdinaryGenome(Arrays.asList(4, 2, 1, 5, 0, 3, 7, 6, 9, 8, 11, 10, 13, 12));
        GGHPGraph graph = new GGHPGraph(baseGenome, guidedGenome);
        return graph;
    }

    GGHPGraph getSircleTestGraph() throws GenomeException {
        ArrayList<ArrayList<Integer>> neighbours = new ArrayList<>();
        neighbours.add(new ArrayList<>(Arrays.asList(1, 5)));
        neighbours.add(new ArrayList<>(Arrays.asList(0, 2)));
        neighbours.add(new ArrayList<>(Arrays.asList(1, 3)));
        neighbours.add(new ArrayList<>(Arrays.asList(2, 4)));
        neighbours.add(new ArrayList<>(Arrays.asList(3, 5)));
        neighbours.add(new ArrayList<>(Arrays.asList(0, 4)));
        DuplicatedGenome baseGenome = new DuplicatedGenome(neighbours);
        OrdinaryGenome guidedGenome = new OrdinaryGenome(Arrays.asList(1, 0, 3, 2, 5, 4));
        GGHPGraph graph = new GGHPGraph(baseGenome, guidedGenome);
        return graph;
    }

    GGHPGraph getSymTestGraph() throws GenomeException {
        ArrayList<ArrayList<Integer>> neighbours = new ArrayList<>();
        neighbours.add(new ArrayList<>(Arrays.asList(1, 3)));
        neighbours.add(new ArrayList<>(Arrays.asList(0, 2)));
        neighbours.add(new ArrayList<>(Arrays.asList(1, 3)));
        neighbours.add(new ArrayList<>(Arrays.asList(0, 2)));
        neighbours.add(new ArrayList<>(Arrays.asList(7, 5)));
        neighbours.add(new ArrayList<>(Arrays.asList(6, 4)));
        neighbours.add(new ArrayList<>(Arrays.asList(5, 7)));
        neighbours.add(new ArrayList<>(Arrays.asList(6, 4)));
        DuplicatedGenome baseGenome = new DuplicatedGenome(neighbours);
        OrdinaryGenome guidedGenome = new OrdinaryGenome(Arrays.asList(5, 4, 7, 6, 1, 0, 3, 2));
        GGHPGraph graph = new GGHPGraph(baseGenome, guidedGenome);
        return graph;
    }


    @Test
    public void testWithParallelSolver() throws Exception {

        GGHPGraph graph = getTestGraph();
        GGHPState firstState = new GGHPState(graph);
        BaseSolver solver = new ParallelSolver(firstState);
        solver.solve();

        int cyclesCount = solver.getCurrentSolution().getCyclesCount();
        assertEquals(cyclesCount, 16);
    }

    @Test
    public void testWithSeqSolver() throws Exception {
        GGHPGraph graph = getTestGraph();
        GGHPState firstState = new GGHPState(graph);
        SeqSolver solver = new SeqSolver(firstState);
        solver.solve();
        int cyclesCount = solver.getCurrentSolution().getCyclesCount();
        assertEquals(cyclesCount, 16);
    }

    @Test
    public void search2SimpleTest() throws Exception {
        GGHPGraph graph = getTestGraph();
        GGHPDetector detector = new GGHPDetector(graph, false);
        BaseDetector.Branch result = detector.search2();
        int cyclesCount = result.getCyclesCount();
        assertEquals(cyclesCount, 7);
    }

    @Test
    public void search2EmptyTest() throws Exception {
        GGHPGraph graph = getSymTestGraph();
        GGHPDetector detector = new GGHPDetector(graph, false);
        BaseDetector.Branch result = detector.search2();
        int cyclesCount = result.getCyclesCount();
        assertEquals(cyclesCount, 0);
    }

    @Test
    public void search2ComplexTest() throws Exception {
        GGHPGraph graph = getSircleTestGraph();
        GGHPDetector detector;
        BaseDetector.Branch branch ;
        int operation_number = 0;
        while (graph.size() != 0) {
            detector = new GGHPDetector(graph, false);
            branch = detector.search2();
            graph.lazyReconstruct(branch);
            graph.pushReconstruction();
            operation_number++;
        }
        assertEquals(operation_number, 3);
    }

    @Test
    public void updateWithExplicit4SimpleTest() throws Exception {
        GGHPGraph graph = getTestGraph();
        GGHPDetector detector = new GGHPDetector(graph, false);
        BaseDetector.Branch result = new BaseDetector.Branch();
        BaseDetector.Branch branch = detector.search2();
        detector.updateWithExplicit4(branch);
        assertEquals(branch.getCyclesCount(), 11);
    }

    @Test
    public void updateWithExplicit4Test() throws Exception {
        GGHPGraph graph = getSymTestGraph();
        GGHPDetector detector = new GGHPDetector(graph, false);
        BaseDetector.Branch branch = detector.search2();
        detector.updateWithExplicit4(branch);
        assertEquals(branch.getCyclesCount(), 0);
    }

    @Test
    public void stateSizeTest() throws Exception {
        GGHPGraph graph = getSircleTestGraph();
        GGHPState state = new GGHPState(graph);
        int size = state.getSize();
        assertEquals(size, 6);
        List<State> states = state.computeNextStates(true);
        assertEquals(states.size(), 1);
        assertEquals(states.get(0).cyclesCount, 7);
    }

    @Test
    public void searchNotExplicit4Test() throws Exception {
        GGHPGraph graph = getSymTestGraph();
        GGHPDetector detector = new GGHPDetector(graph, false);
        List<BaseDetector.Branch> results = detector.searchNotExplicit4();
        assertEquals(results.size(), 2);
//        results.get(0).getRemovedVertices();
//        graph.lazyReconstruct(results.get(0));
//        graph.pushReconstruction();
//        assertEquals(graph.size(), 4);
    }

}