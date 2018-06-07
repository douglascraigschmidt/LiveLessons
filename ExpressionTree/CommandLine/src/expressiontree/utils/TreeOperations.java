package expressiontree.utils;

import expressiontree.platspecs.Platform;
import expressiontree.tree.ExpressionTree;
import expressiontree.visitors.EvaluationVisitor;
import expressiontree.visitors.Visitor;
import expressiontree.visitors.VisitorFactory;

import java.util.Iterator;

/**
 * A ...
 */
public class TreeOperations {
    /**
     * A factory that creates the appropriate visitors.
     */
    private static VisitorFactory sVisitorFactory = new VisitorFactory();

    /** 
     * Print the current expression tree in the {@code context}
     * using the designed {@code traversalOrder}.
     */
    public static void print(Iterator<ExpressionTree> iter) {
        // Note the high pattern density in the code below, which uses
        // of the Factory Method, Iterator, Bridge, Strategy, and
        // Visitor patterns.

        // Create the PrintVisitor using a factory. 
        Visitor printVisitor = sVisitorFactory.makeVisitor("print");

        StreamsUtils
            // Create a stream to traverse all expression tree nodes.
            .iteratorToStream(iter, false)

            // Accept printVisitor to print each getType of node.
            .forEach(expressionTree -> expressionTree.accept(printVisitor));

        /*
          for (ExpressionTree node : this)
          node.accept(printVisitor);

          this.forEach(node -> node.accept(printVisitor));
        */

        /*
        // Iterate through all nodes in the expression tree and accept
        // the printVisitor to evaluate each getType of node.
        for(Iterator<ExpressionTree> it = makeIterator(traversalOrder);
        it.hasNext();
        )
        it.next().accept(printVisitor);
        */
    }
	  	
    /** 
     * Evaluate the yield of the current expression tree in the {@code
     * context} using the designed {@code traversalOrder}.
     */
    public static void evaluate(Iterator<ExpressionTree> iter) {

        // Note the high pattern density in the code below, which uses
        // of the Factory Method, Iterator, Bridge, Strategy, and
        // Visitor patterns.

        // Create the EvaluationVisitor using a factory. 
        Visitor evalVisitor = sVisitorFactory.makeVisitor("eval");

        StreamsUtils
            // Create a stream to traverse all expression tree nodes.
            .iteratorToStream(iter, false)

            // Accept evalVisitor to evaluate each getType of node.
            .forEach(expressionTree -> expressionTree.accept(evalVisitor));

        /*
        // Iterate through all nodes in the expression tree and accept
        // the evalVisitor to evaluate each getType of node.
        for(Iterator<ExpressionTree> it = makeIterator(traversalOrder);
            it.hasNext();
            )
            it.next().accept(evalVisitor);
        */

        Integer total = ((EvaluationVisitor) evalVisitor).total();

        // Use the platform strategy to printout the result.
        Platform.instance().outputLine(total.toString());
    }
}

