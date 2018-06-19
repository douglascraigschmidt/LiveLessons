package expressiontree.interpreter.postorder;


import expressiontree.nodes.*;

interface Expr {
    int interpret();
    ComponentNode build();
}
 
class NumberExpr
       implements Expr {
    private int mNumber;

    NumberExpr(int number) {
        mNumber = number;
    }

    NumberExpr(String number) {
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

class NegateExpr
      implements Expr {
    private Expr mRightExpression;

    NegateExpr(Expr rightExpression) {
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

abstract class BinaryExpr
      implements Expr {
    Expr mLeftExpression;
    Expr mRightExpression;

    BinaryExpr(Expr leftExpression,
               Expr rightExpresion) {
        mLeftExpression = leftExpression;
        mRightExpression = rightExpresion;
    }
}

class MultiplyExpr
      extends BinaryExpr {

    MultiplyExpr(Expr leftExpression,
                 Expr rightExpression) {
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

class DivideExpr
      extends BinaryExpr {
    DivideExpr(Expr leftExpression,
               Expr rightExpression) {
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

class AddExpr
      extends BinaryExpr {
    AddExpr(Expr leftExpression,
            Expr rightExpression) {
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

class SubtractExpr
      extends BinaryExpr {
    SubtractExpr(Expr leftExpression,
                 Expr rightExpression) {
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

