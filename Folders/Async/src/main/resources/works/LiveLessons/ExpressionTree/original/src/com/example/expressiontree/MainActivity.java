package com.example.expressiontree;

import java.util.Map.Entry;
import java.util.Vector;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

/** 
 * @class MainActivity
 * 
 * @brief Initial start up screen for the android GUI.  Presents the
 *        user with an alert dialog (options menU) and have the choice
 *        between succinct and verbose mode.
 */
public class MainActivity extends Activity  {
    /** onCreate. */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** onStart. */
    public void onStart() {
        super.onStart();
        
        /** Alert dialog. */
        AlertDialog dialog =
            new AlertDialog.Builder(MainActivity.this).create();
        dialog.setTitle("Options");
		
        /** Set back pressed. */
        dialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel (DialogInterface dialog) {
                    dialog.dismiss();
                    finish();	
                }
            });
        
        /** Formats the succinct button. */
        dialog.setButton(AlertDialog.BUTTON_NEGATIVE,
                         "Succinct",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog,
                                                 int which) {
                                 /**
                                  * Sets an intent for launching the
                                  * succinct activity.
                                  */
                                 Intent intent =
                                     new Intent(getApplicationContext(),
                                                CalculatorGUISuccinct.class);
                                 intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                 startActivity(intent);
                             }
                         });

        /** Formats the verbose button. */ 
        dialog.setButton(AlertDialog.BUTTON_POSITIVE,
                         "Verbose",
                         new DialogInterface.OnClickListener() {
                             public void onClick(DialogInterface dialog, int which) {
                                 /**
                                  *  Sets an intent for launching the 
                                  *  verbose activity.
                                  */
                                 Intent intent = new Intent(getApplicationContext(),
                                                            CalculatorGUIVerbose.class);
                                 intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                                 startActivity(intent);
                             }
                         });
	
        /** Displays dialog to screen. */
        dialog.show();
    }
}	
	
	
		

