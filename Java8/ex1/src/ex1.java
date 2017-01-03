import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

/**
 * This example shows how to use Java 8 lambda expressions and method
 * references to sort elements of a collection.  It also shows how to
 * use the Java 8 forEach() method.
 */
public class ex1 {
    static public void main(String[] argv) {
    	ConcurrentHashMap<String, String> map = new ConcurrentHashMap<>();
    	map.put("foo", "bar");
    	map.put("han", "solo");
    	map.put("r2", "d2");
    	map.put("c3", "p0");
    	
    	String result = map.searchEntries(1, entry -> {
    	    if (entry.getValue().length() > 3) {
    	    	entry.setValue("brinker");
    	        return entry.getKey();
    	    }
    	    return null;
    	});

    	System.out.println("Result: " + result + " result 2: " + map.get("han"));
    	
    	/*
        // The array to sort and print.
        String[] nameArray = {
            "Barbara",
            "James",
            "Mary",
            "John",
            "Robert",
            "Michael",
            "Linda",
            "james",
            "mary"
        };

        showInnerClass(nameArray);
        showLambdaExpression(nameArray);
        showMethodReference(nameArray);
        */
    }

    /**
     * Show how to sort using an anonymous inner class.
     */
    private static void showInnerClass(String[] nameArray) {
        // Make a copy of the array.
        String[] nameArrayCopy = 
            Arrays.copyOf(nameArray, nameArray.length);

        System.out.println(Arrays.asList(nameArrayCopy));

        // Sort using an anonymous inner class.
        Arrays.sort(nameArrayCopy, new Comparator<String>() {
                public int compare(String s,String t) { 
                    return s.toLowerCase().compareTo(t.toLowerCase()); 
                }
            });

        // Print out the sorted contents as an array.
        System.out.println(Arrays.asList(nameArrayCopy));
    }

    /**
     * Show how to sort using a lambda expression.
     */
    private static void showLambdaExpression(String[] nameArray) {
        // Make a copy of the array.
        String[] nameArrayCopy = 
            Arrays.copyOf(nameArray, nameArray.length);

        // Sort using a lambda expression.
        Arrays.sort(nameArrayCopy,
                    // Note type deduction here:
                    (s, t) -> s.compareToIgnoreCase(t));

        // Print out the sorted contents as an array.
        System.out.println(Arrays.asList(nameArrayCopy));
    }

    /**
     * Show how to sort using a method reference.
     */
    private static void showMethodReference(String[] nameArray) {
        // Make a copy of the array.
        String[] nameArrayCopy = 
            Arrays.copyOf(nameArray, nameArray.length);

        // Sort using a method reference.
        Arrays.sort(nameArrayCopy,
                    String::compareToIgnoreCase);

        // Print out the sorted contents using the Java 8 forEach()
        // method.
        Stream.of(nameArrayCopy).forEach(System.out::print);
    }
}

