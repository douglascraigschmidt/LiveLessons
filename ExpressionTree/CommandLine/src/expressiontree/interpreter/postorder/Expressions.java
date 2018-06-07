package expressiontree.interpreter.postorder;


import expressiontree.nodes.*;

interface Expression {
    int interpret();
    ComponentNode build();
}
 
class NumberExpression
       implements Expression {
    private int mNumber;

    NumberExpression(int number) {
        mNumber = number;
    }

    NumberExpression(String number) {
        mNumber = Integer.parseInt(number);
    }

    @Override
    public int interpret() {
        return mNumber;
    }

    /**
     * Hook method for building a {@code ComponentNode} a la the Builder
     * pattern.
     */
    @Override
    public ComponentNode build() { return new LeafNode(mNumber); }
}

class NegateExpression
      implements Expression {
    private Expression mRightExpression;

    NegateExpression(Expression rightExpression) {
        mRightExpression = rightExpression;
    }

    @Override
    public int interpret() {
        return -mRightExpression.interpret();
    }

    /**
     * Hook method for building a {@code ComponentNode} a la the Builder
     * pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeNegateNode(mRightExpression.build());
    }
}

abstract class BinaryExpression
      implements Expression {
    Expression mLeftExpression;
    Expression mRightExpression;

    BinaryExpression(Expression leftExpression,
                     Expression rightExpresion) {
        mLeftExpression = leftExpression;
        mRightExpression = rightExpresion;
    }
}

class MultiplyExpression
      extends BinaryExpression {

    MultiplyExpression(Expression leftExpression,
                       Expression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public int interpret() {
        return mLeftExpression.interpret() 
            * mRightExpression.interpret();
    }

    /**
     * Hook method for building a {@code ComponentNode} a la the Builder
     * pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeMultiplyNode(mLeftExpression.build(),
                                         mRightExpression.build());
    }
}

class DivideExpression
      extends BinaryExpression {
    DivideExpression(Expression leftExpression,
                     Expression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public int interpret() {
        return mLeftExpression.interpret() 
            / mRightExpression.interpret();
    }

    /**
     * Hook method for building a {@code ComponentNode} a la the Builder
     * pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeDivideNode(mLeftExpression.build(),
                                       mRightExpression.build());
    }
}

class AddExpression
      extends BinaryExpression {
    AddExpression(Expression leftExpression,
                  Expression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public int interpret() {
        return mLeftExpression.interpret()
            + mRightExpression.interpret();
    }

    /**
     * Hook method for building a {@code ComponentNode} a la the Builder
     * pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeAddNode(mLeftExpression.build(),
                                    mRightExpression.build());
    }
}

class SubtractExpression
      extends BinaryExpression {
    SubtractExpression(Expression leftExpression,
                       Expression rightExpression) {
        super(leftExpression, rightExpression);
    }

    @Override
    public int interpret() {
        return mLeftExpression.interpret() 
            - mRightExpression.interpret();
    }

    /**
     * Hook method for building a {@code ComponentNode} a la the Builder
     * pattern.
     */
    @Override
    public ComponentNode build() {
        return new CompositeSubtractNode(mLeftExpression.build(),
                                         mRightExpression.build());
    }
}

