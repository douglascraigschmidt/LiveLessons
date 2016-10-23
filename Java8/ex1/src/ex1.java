import java.util.*;

/**
 * ...
 */
public class ex1 {
    static public void main(String[] argv) {
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

        String[] nameArrayCopy = Arrays.copyOf(nameArray, nameArray.length);
        System.out.println(Arrays.asList(nameArrayCopy));
        // Sort using an anonymous inner class.
        Arrays.sort(nameArrayCopy, new Comparator<String>() {
                public int compare(String s,String t) { 
                    return s.toLowerCase().compareTo(t.toLowerCase()); 
                }
            });
        System.out.println(Arrays.asList(nameArrayCopy));

        nameArrayCopy = Arrays.copyOf(nameArray, nameArray.length);
        // Sort using a lambda expression.
        Arrays.sort(nameArrayCopy, (s, t) -> s.compareToIgnoreCase(t));
        System.out.println(Arrays.asList(nameArrayCopy));

        nameArrayCopy = Arrays.copyOf(nameArray, nameArray.length);
        // Sort using a method reference.
        Arrays.sort(nameArrayCopy, String::compareToIgnoreCase);
        System.out.println(Arrays.asList(nameArrayCopy));
    }
}

