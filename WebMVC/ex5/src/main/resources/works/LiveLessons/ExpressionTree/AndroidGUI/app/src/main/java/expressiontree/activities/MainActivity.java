package expressiontree.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;

/** 
 * Initial start up screen for the android GUI.  Presents the
 * user with an alert dialog (options menU) and have the choice
 * between succinct and verbose mode.
 */
public class MainActivity 
       extends Activity {
    /**
     * onCreate.
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * onStart.
     */
    public void onStart() {
        super.onStart();
        
        // Alert dialog.
        AlertDialog alertDialog =
            new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Options");
		
        // Set back pressed.
        alertDialog.setOnCancelListener(dialog -> {
                dialog.dismiss();
                finish();
            });
        
        // Formats the succinct button.
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                              "Succinct",
                              (dialog12, which) -> {
                                  // Sets an intent for launching the
                                  // succinct activity.
                                  Intent intent =
                                      new Intent(getApplicationContext(),
                                                 CalculatorGUISuccinct.class);
                                  intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                  startActivity(intent);
                              });

        // Formats the verbose button.
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE,
                              "Verbose",
                              (dialog1, which) -> {
                                  // Sets an intent for launching the
                                  // verbose activity.
                                  Intent intent =
                                      new Intent(getApplicationContext(),
                                                 CalculatorGUIVerbose.class);
                                  intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                  startActivity(intent);
                              });
	
        // Displays dialog to screen.
        alertDialog.show();
    }
}	
	
	
		

