package utils;

/**
 * This class avoids the copying overhead of String.substring() that
 * was introduced in Java 7.  It's based on the SubbableString class
 * described at http://www.javaspecialists.eu/archive/Issue230.html.
 */
public class SharedString 
       implements CharSequence {
    /**
     * This is the char array shared by the CharSequences returned
     * from subSequence().
     */
    private final char[] mValue;

    /**
     * The offset into the char array.
     */
    private final int mOffset;

    /**
     * The number of characters in the SharedString.
     */
    private final int mCount;

    /**
     * This constructor initializes the SharedString from the char
     * array.
     */
    public SharedString(char[] value) {
        this(value, 0, value.length);
    }

    /**
     * This constructor initializes the SharedString from the char
     * array for @a count bytes starting at @a offset.
     */
    private SharedString(char[] value, int offset, int count) {
        mValue = value;
        mOffset = offset;
        mCount = count;
    }

    /**
     * Return the length of this SharedString.
     */
    @Override
    public int length() {
        return mCount;
    }

    /**
     * Return a String representation of this SharedString.
     */
    @Override
    public String toString() {
        return new String(mValue, mOffset, mCount);
    }

    /**
     * Return the character at @a index.
     */
    @Override
    public char charAt(int index) {
        if (index < 0 || index >= mCount)
            throw new StringIndexOutOfBoundsException(index);
        return mValue[index + mOffset];
    }

    /**
     * Return a new subsequence beginning at @a start and continuing
     * to @a end.  This subsequence shares the underlying char array
     * without copying it.
     */
    @Override
    public CharSequence subSequence(int start, int end) {
        if (start < 0) 
            throw new StringIndexOutOfBoundsException(start);

        if (end > mCount) 
            throw new StringIndexOutOfBoundsException(end);

        if (start > end) 
            throw new StringIndexOutOfBoundsException(end - start);

        return start == 0 && end == mCount
            // Simply return "this" if the start and end match exactly.
            ? this 
            // Otherwise, create a new SharedString that points into
            // the subsequence without copying it.
            : new SharedString(mValue, mOffset + start, end - start);
    }
}
