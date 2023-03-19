package com.example.expressiontree;

import com.example.expressiontree.R;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 * @class AndroidPlatform
 * 
 * @brief Accounts for the user input and the ExpressionTree textual
 *        output.  It organizes the data so an Android platform can
 *        easily recognize the data and create the GUI.  It plays the
 *        role of the "Concrete Strategy" in the Strategy pattern.
 */
public class AndroidPlatform extends Platform {
    /** EditTextVariable. */
    EditText editTextInput;
	
    /** TextViewVariable. */
    TextView textViewOutput;
	
    /** A hash map of the activated commands. */
    HashMap<String, CommandMenu> menu =
        new HashMap<String, CommandMenu>();
	
    /** Activity variable finds gui widgets by view. */
    Activity activity;

    /** True if the current GUI is verbose. */
    private boolean verboseField;
	
    /**
     * The commandMenu interface precludes the use of an excessive
     * if-statement to activate various GUI operations.
     */ 
    private interface CommandMenu {
        public void select(boolean enable);
    }
    
    /** Tracks the error messages. */
    static int errorNumber = 0;

    public AndroidPlatform(Object input,
                           Object output,
                           final Object activityParam) {
        /** The editText box designated for expr input. */
        editTextInput = (EditText) input;
		
        /** 
         * A textview output which displays calculations and
         * expression trees. 
         */
        textViewOutput = (TextView) output;
		
        /** The current activity window (succinct or verbose). */
        this.activity = (Activity) activityParam;
		
        /** Activates the format radiogroup. */
        menu.put("format", new CommandMenu() {
                public void select(boolean enable) {
                    enableRadioGroup(R.id.rad1,enable);
                }
            });
		
        /** Activates the print RadioGroup. */
        menu.put("print", new CommandMenu() {
                public void select(boolean enable) {
                    enableRadioGroup(R.id.rad2,enable);
                }
            });
		
        /** Activates the eval radiogroup. */
        menu.put("eval", new CommandMenu() {
                public void select(boolean enable) {
                    enableRadioGroup(R.id.rad3,enable);
                }
            });	
		
        /** Activates the set checkbox. */
        menu.put("set", new CommandMenu() {
                public void select(boolean enable) {
                    enableMenu(R.id.cb,enable);
                }
            });
		
        /** Activates the expr editText box. */
        menu.put("expr", new CommandMenu() {
                public void select(boolean enable) {
                    enableMenu(R.id.et,enable); 
                }
            });	
		
        /** Enables the quit button. */
        menu.put("quit", new CommandMenu() {
                public void select(boolean enable) {
                    enableMenu(R.id.buttonquit,enable);   
                }
            });
		
        /** Does nothing in android. */
        menu.put("", new CommandMenu() {
                public void select(boolean enable) {
                    /**Nothing to do here */
                }
            });
    }
    
    /** Enables the menu item. */
    private void enableMenu(int id,boolean enable) {
    	View t =  activity.findViewById(id);
    	t.setEnabled(enable);
    }
    
    /** Enables the active RadioButtons. */
    private void enableRadioGroup(int id,boolean enable) {
        RadioGroup rg = (RadioGroup) activity.findViewById(id);
        for (int i = 0; i < rg.getChildCount(); i++)
            (rg.getChildAt(i)).setEnabled(enable);
    }
	
    /** Displays a line followed by a new line character. */
    public String retrieveInput(boolean verbose) {
        String words;
        verboseField = verbose;
		
        if(verbose) 
            words = buttonToggled() + " " + editTextInput.getText().toString();
        else 
            words = editTextInput.getText().toString();

        words = words.trim();
        
        if(verbose)
            Log.e("AndroidPlatform", "retrieveInputVerbose " + words);
        else
            Log.e("AndroidPlatform", "retrieveInputSuccinct " + words);
        
        return words;
    }

    /**
     * Retrieves textual input of the toggled button determined by the
     * user.
     */
    private String buttonToggled() {
    	Log.e("AndroidPlatform", "buttonToggled");
        /** Checks the format radio group for checked buttons. */
        RadioGroup rg = (RadioGroup) activity.findViewById(R.id.rad1);
        for(int i = 0; i < rg.getChildCount(); i++)
            if(( (RadioButton) rg.getChildAt(i)).isChecked()
               && ((RadioButton) rg.getChildAt(i)).isEnabled()) 
                return  ("format"
                         + " " 
                         + (String) ((TextView) rg.getChildAt(i)).getText()).toLowerCase();
		
        /** Checks the print radio group for checked buttons. */
        rg = (RadioGroup) activity.findViewById(R.id.rad2);
        for (int i = 0; i < rg.getChildCount(); i++)
            if(((RadioButton) rg.getChildAt(i)).isChecked()
               && ((RadioButton) rg.getChildAt(i)).isEnabled()) 
                {
                    ((TextView) activity.findViewById(R.id.tv)).setText("");
                    return ("print" 
                            + " " 
                            + (String) ((TextView) rg.getChildAt(i)).getText()).toLowerCase();
                } 
		
        /** Checks the eval radio group for checked buttons. */
        rg = (RadioGroup) activity.findViewById(R.id.rad3);
        for(int i = 0; i < rg.getChildCount(); i++)
            if (((RadioButton) rg.getChildAt(i)).isChecked()
                && ((RadioButton) rg.getChildAt(i)).isEnabled()) 
                return ("eval" 
                        + " " 
                        + (String) ((TextView) rg.getChildAt(i)).getText()).toLowerCase();
		
        /** Checks the set checkbox. */
        CheckBox cb = (CheckBox) activity.findViewById(R.id.cb);
        if(cb.isChecked()&&cb.isEnabled()) 
            return ("set"+ (String) ((TextView) cb).getText()).toLowerCase();
		
        return "expr";
    }

    /** Changes the TextView to the input string. */
    public String outputLine(final String line) {
    	Log.e("AndroidPlatform", "outputLine");
        textViewOutput.setText(line);
        return (String) textViewOutput.getText();
    }

    /** Displays a the inputed code without a new line character. */
    public String outputString(final String string) {
    	Log.e("AndroidPlatform", "outputChar");
    	if(string.equals("> "))
            return "";
        textViewOutput.setText(string);
        return (String) textViewOutput.getText();
    }

    /** Returns the platform name in a String. */
    public String platformName() {
    	Log.e("AndroidPlatform", "platform name");
        return System.getProperty("java.specification.vendor");
    }

    /** Returns true if we're running on a command-line platform. */
    public boolean isCommandLinePlatform() {
        return false;
    }

    /** Activates the designated menu item. */
    public void outputMenu(String numeral,
                           String option,
                           String selection) {
    	boolean enable = numeral.startsWith("1") 
            || option.equals("quit");
        menu.get(option).select(enable);
    }

    /** Disables the RadioButtons and unchecks them. */
    public void disableAll(boolean verbose) {
        if(verbose) {
            errorLog("AndroidPlatform","disableAll true");
            disable((RadioGroup) activity.findViewById(R.id.rad1));
            disable((RadioGroup) activity.findViewById(R.id.rad2));
            disable((RadioGroup) activity.findViewById(R.id.rad3));
            EditText et = (EditText) activity.findViewById(R.id.et);
            et.setEnabled(false);
        }
    }

    /** Disables all radio buttons of a radio group */
    private void disable(RadioGroup rg) {
    	for (int i = 0; i < rg.getChildCount(); i++)
            rg.getChildAt(i).setEnabled(false);
    }
    
    /** Enables the option specified. */
    public void enableOption(String option) {
    	Log.e("AndroidPlatform", "enableOption");
    	menu.get(option).select(true);
    }

    /**
     * Specific for the printvisitor functionality.  Allows for the
     * addition of multiple lines to a textView display.
     */
    public String addString(String output) {
        errorLog("AndroidPlatform", "addString");
        textViewOutput.setText(textViewOutput.getText() + output);
        return output;
    }

    /** 
     * Error log formats the message and displays it for the
     * debugging purposes.
     */
    public void errorLog(String javaFile, 
                         String errorMessage) {
        Log.e(javaFile,errorMessage);
    }
}

