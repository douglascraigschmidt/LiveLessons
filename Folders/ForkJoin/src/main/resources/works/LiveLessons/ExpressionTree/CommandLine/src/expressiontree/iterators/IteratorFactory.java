package expressiontree.iterators;

import expressiontree.tree.ExpressionTree;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Implementation of the Factory Method pattern that dynamically
 * allocates the appropriate {@code Iterator} strategy requested by a
 * caller.  This variant of the pattern doesn't use inheritance, so it
 * plays the role of the ConcreteCreator in the Factory Method
 * pattern.
 */
public class IteratorFactory {
    /*
     * Constants that determine the traversal order.
     */
    public final static String IN_ORDER = "in-order";
    public final static String PRE_ORDER = "pre-order";
    public final static String POST_ORDER = "post-order";
    public final static String LEVEL_ORDER = "level-order";

    /**
     * This interface uses the Command pattern to create @a Iterator
     * implementations at runtime.
     */
    @FunctionalInterface
    public interface IteratorFactoryCommand {
        Iterator<ExpressionTree> execute(ExpressionTree tree);
    }

    /**
     * Map used to validate mInput requests for @a Iterator
     * implementations and dispatch the execute() method of the
     * requested iterator.
     */
    private Map<String, IteratorFactoryCommand> mIteratorMap =
        new HashMap<>();
	
    /** 
     * Constructor.
     */
    public IteratorFactory() {
        /*
         * The IteratorFactory maps strings to an interface capable of
         * building the appropriate {@code Iterator} implementation at
         * runtime.
         */

    	// An "in-order" string maps to a command object that creates
        // an @a InOrderIterator implementation.
        mIteratorMap.put(IN_ORDER, InOrderIterator::new);
            
    	// A "pre-order" string maps to a command object that creates
        // a @a PreOrderIterator implementation.
        mIteratorMap.put(PRE_ORDER, PreOrderIterator::new);
            
    	// A "post-order" string maps to a command object that creates
        // a @a PostOrderIterator implementation.
        mIteratorMap.put(POST_ORDER, PostOrderIterator::new);
            
    	// A "level-order" string maps to a command object that
        // creates a @a LevelOrderIterator implementation.
        mIteratorMap.put(LEVEL_ORDER, LevelOrderIterator::new);
    }
	
    /** 
     * Create a new @a Iterator implementation based on the caller's
     * designated @a traversalOrderRequest.
     */
    public Iterator<ExpressionTree> iterator(ExpressionTree tree,
                                             String traversalOrderRequest) {
        if (traversalOrderRequest.equals(""))
            // Default to in-order if user doesn't explicitly request
            // a traversal order.
            traversalOrderRequest = "in-order";

        //* Try to find the pre-allocated factory command.
        IteratorFactoryCommand command =
            mIteratorMap.get(traversalOrderRequest);

        if (command != null)
            // If we find it then execute it.
            return command.execute(tree);
        else
            // Otherwise, the user gave an unknown request, so throw
            // an exception.
            throw new IllegalArgumentException
                (traversalOrderRequest 
                 + " is not a supported traversal order");
    }

    /**
     * Iterates through an {@code Tree} in level-order.  Plays the role of
     * the "ConcreteStrategy" in the Strategy pattern that defines the
     * pre-order iteration algorithm.
     */
    public static class LevelOrderIterator
        implements Iterator<ExpressionTree> {
        /**
         * Queue of expression trees.
         */
        private Queue<ExpressionTree> queue =
            new LinkedList<>();

        /**
         * Constructor.
         */
        public LevelOrderIterator(ExpressionTree tree) {
            if(!tree.isNull())
                queue.add(tree);
        }

        /**
         * Moves iterator to the next expression tree in the stack.
         */
        public ExpressionTree next() {
            // Store the current front element in the queue.
            ExpressionTree result = queue.peek();

            if (!queue.isEmpty()) {
                // We need to pop the node off the stack before
                // pushing the children, or else we'll revisit this
                // node later.
                ExpressionTree temp = queue.remove();

                // Note the order here: mRight first, then mLeft. Since this
                // is LIFO, this results in the mLeft child being the first
                // evaluated, which fits into the Pre-order traversal
                // strategy.
                if (!temp.getRightChild().isNull())
                    queue.add (temp.getRightChild());
                if (!temp.getLeftChild().isNull())
                    queue.add (temp.getLeftChild());
            }

            return result;
        }

        /**
         * Checks if the queue is empty.
         */
        public boolean hasNext() {
            return !queue.isEmpty();
        }

        /**
         * Removes an expression tree from the front of the queue.
         */
        public void remove() {
            queue.remove();
        }
    }
}
	
	
	
