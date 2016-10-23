import java.util.*;

/**
 * ...
 */
public class ex4 {
    static public void main(String[] argv) {
    	Map<String, Integer> iqMap = new HashMap<String, Integer>() {
    		   { put("Larry", 100); put("Curly", 100); put("Moe", 100); }
    	};

    	System.out.println(iqMap);
    		 
    	// Replace all values of all keys.
    	iqMap.replaceAll((k, v) -> v - 50);

    	System.out.println(iqMap);

    	// Replace all values of all keys.
    	for (Map.Entry<String, Integer> entry : iqMap.entrySet())
    		 entry.setValue(entry.getValue() - 50);
    	
    	System.out.println(iqMap);


    }
}

