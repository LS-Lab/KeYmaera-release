package de.uka.ilkd.key.dl.transitionmodel;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import de.uka.ilkd.key.dl.model.DLProgram;
import de.uka.ilkd.key.dl.transitionmodel.TransitionSystemGenerator.SpecialSymbols;
import de.uka.ilkd.key.java.PrettyPrinter;

/**
 * TODO jdq documentation since Nov 9, 2007
 * 
 * @author jdq
 * @since Nov 9, 2007
 * 
 */
public class DottyStateGenerator implements
        StateGenerator<DottyStateGenerator.NumberedState, List<String>> {

    public static final class NumberedState {
        private static long maxNumber = 0;

        private long number;

        /**
         * 
         */
        public NumberedState() {
            this.number = maxNumber++;
        }

        /**
         * @return the number
         */
        public long getNumber() {
            return number;
        }

        public HashSet<String> transitions = new HashSet<String>();

    }

    public static void generateDottyFile(Writer writer, DLProgram program)
            throws IOException {

        writer.write("digraph program\n");
        writer.write("{\n");
        TransitionSystem<NumberedState, List<String>> transitionModel = TransitionSystemGenerator
                .getTransitionModel(program, new DottyStateGenerator(),
                        new NumberedState());
        for (String trans : transitionModel.getFinalState().transitions) {
            writer.write(trans + "\n");
        }
        writer.write("}\n");
        writer.flush();
    }

    @Override
    public List<String> generateAction(DLProgram program) {
        StringWriter writer = new StringWriter();
        try {
            program.prettyPrint(new PrettyPrinter(writer));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return Collections.singletonList(writer.toString());
    }

    @Override
    public List<String> generateBranch(DLProgram program, int pos) {
        return Collections.singletonList(SpecialSymbols.CHOICE.toString());
    }

    @Override
    public List<String> generateMerge(DLProgram program, int pos) {
        return Collections.singletonList(SpecialSymbols.NOOP.toString());
    }

    @Override
    public DottyStateGenerator.NumberedState generateMergeState(
            DLProgram program, List<DottyStateGenerator.NumberedState> states) {
        NumberedState numberedState = new NumberedState();
        for (NumberedState s : states) {
            numberedState.transitions.addAll(s.transitions);
            String string = s.getNumber() + " -> " + numberedState.getNumber()
                    + " [ label=\" NOOP \" ];";
            System.out.println("Generating state: " + string);// XXX
            numberedState.transitions.add(string);
        }
        return numberedState;
    }

    @Override
    public DottyStateGenerator.NumberedState getPostState(
            DottyStateGenerator.NumberedState pre, List<String> action) {
        NumberedState numberedState = new NumberedState();
        numberedState.transitions.addAll(pre.transitions);
        String string = pre.getNumber() + " -> " + numberedState.getNumber()
                + " [ label=\"" + action + "\" ];";
        numberedState.transitions.add(string);
        System.out.println("Generating state: " + string);// XXX
        return numberedState;
    }

    @Override
    public List<String> getSpecialSymbolNoop(NumberedState pre,
            NumberedState post) {
        String string = pre.getNumber() + " -> " + post.getNumber()
                + " [ label=\"NOOP\" ];";
        System.out.println("Generating state: " + string);// XXX
        post.transitions.add(string);
        post.transitions.addAll(pre.transitions);
        return Collections.singletonList(SpecialSymbols.NOOP.toString());
    }

    @Override
    public List<String> getSpecialSymbolStar(NumberedState pre,
            NumberedState post) {
        String string = pre.getNumber() + " -> " + post.getNumber()
                + " [ label=\"*\" ];";
        System.out.println("Generating state: " + string);// XXX
        post.transitions.add(string);
        post.transitions.addAll(pre.transitions);
        return Collections.singletonList(SpecialSymbols.STAR.toString());
    }
}