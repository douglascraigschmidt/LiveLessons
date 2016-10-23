import java.util.*;

/**
 * ...
 */
public class ex6 {
    static public void main(String[] argv) {
        Map<String, String> personMap = new HashMap<String, String>() 
            {
                { put("Demon", "Naughty");
                  put("Angel", "Nice"); 
                } 
            };

        String person = "Demigod";

        Optional<String> disposition = 
            Optional.ofNullable(personMap.get(person));

        System.out.println("disposition of "
                           + person + " = "
                           + disposition.orElseGet(() -> "unknown"));
    }
}

