package expressiontree.platspecs;

import expressiontree.R;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import java.util.HashMap;

/**
 * Accounts for the user input and the ExpressionTree textual output.
 * It organizes the data so an Android platform can easily recognize
 * the data and create the GUI.  It plays the role of the "Concrete
 * Strategy" in the Strategy pattern.
 */
public class AndroidPlatform
       extends Platform {
    /** 
     * EditTextVariable. 
     */
    private EditText mEditTextInput;
	
    /**
     * TextViewVariable. 
     */
    private TextView mTextViewOutput;
	
    /**
     * A hash map of the activated commands. 
     */
    private HashMap<String, CommandMenu> mMenu =
        new HashMap<>();
	
    /** 
     * Activity variable finds gui widgets by view. 
     */
    private Activity mActivity;

    /**
     * True if the current GUI is verbose.
     */
    private boolean mVerboseField;

    /**
     * The commandMenu interface avoids the need for if-statements to
     * run various GUI operations.
     */ 
    @FunctionalInterface
    private interface CommandMenu {
        public void select(boolean enable);
    }
    
    /** Tracks the error messages. */
    static int errorNumber = 0;

    /**
     * Constructor.
     */
    AndroidPlatform(Object input,
                    Object output,
                    final Object activityParam) {
        // The editText box designated for expr input. 
        mEditTextInput = (EditText) input;
		
        // A textview output which displays calculations and
        // expression trees.
        mTextViewOutput = (TextView) output;
		
        // The current mActivity window (succinct or verbose).
        mActivity = (Activity) activityParam;
		
        // Activates the format radiogroup.
        mMenu.put("format", enable -> enableRadioGroup(R.id.rad1, enable));
		
        // Activates the print RadioGroup. 
        mMenu.put("print", enable -> enableRadioGroup(R.id.rad2, enable));
		
        // Activates the eval radiogroup.
        mMenu.put("eval", enable -> enableRadioGroup(R.id.rad3, enable));
		
        // Activates the set checkbox.
        mMenu.put("set", enable -> enableMenu(R.id.cb, enable));
		
        // Activates the expr editText box.
        mMenu.put("expr", enable -> enableMenu(R.id.et, enable));
		
        // Enables the quit button. 
        mMenu.put("quit", enable -> enableMenu(R.id.buttonquit, enable));
		
        // Does nothing in android.
        mMenu.put("", enable -> {
            // Nothing to do here.
        });
    }
    
    /** 
     * Enables the mMenu item.
     */
    private void enableMenu(int id,boolean enable) {
    	View t =  mActivity.findViewById(id);
    	t.setEnabled(enable);
    }
    
    /**
     * Enables the active RadioButtons. 
     */
    private void enableRadioGroup(int id,boolean enable) {
        RadioGroup rg = (RadioGroup) mActivity.findViewById(id);
        for (int i = 0; i < rg.getChildCount(); i++)
            (rg.getChildAt(i)).setEnabled(enable);
    }
	
    /**
     * Displays a line followed by a new line character. 
     */
    public String retrieveInput(boolean verbose) {
        String words;
        mVerboseField = verbose;
		
        if (verbose) 
            words = buttonToggled() + " " + mEditTextInput.getText().toString();
        else 
            words = mEditTextInput.getText().toString();

        words = words.trim();
        
        if (verbose)
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
        // Checks the format radio group for checked buttons.
        RadioGroup rg = (RadioGroup) mActivity.findViewById(R.id.rad1);
        for (int i = 0; i < rg.getChildCount(); i++)
            if (( (RadioButton) rg.getChildAt(i)).isChecked()
                && ((RadioButton) rg.getChildAt(i)).isEnabled()) 
                return  ("format"
                         + " " 
                         + (String) ((TextView) rg.getChildAt(i)).getText()).toLowerCase();
		
        // Checks the print radio group for checked buttons.
        rg = (RadioGroup) mActivity.findViewById(R.id.rad2);
        for (int i = 0; i < rg.getChildCount(); i++)
            if (((RadioButton) rg.getChildAt(i)).isChecked()
                && ((RadioButton) rg.getChildAt(i)).isEnabled()) {
                ((TextView) mActivity.findViewById(R.id.tv)).setText("");
                return ("print" 
                        + " " 
                        + (String) ((TextView) rg.getChildAt(i)).getText()).toLowerCase();
            } 
		
        // Checks the eval radio group for checked buttons. 
        rg = (RadioGroup) mActivity.findViewById(R.id.rad3);
        for (int i = 0; i < rg.getChildCount(); i++)
            if (((RadioButton) rg.getChildAt(i)).isChecked()
                && ((RadioButton) rg.getChildAt(i)).isEnabled()) 
                return ("eval" 
                        + " " 
                        + (String) ((TextView) rg.getChildAt(i)).getText()).toLowerCase();
		
        // Checks the set checkbox.
        CheckBox cb = (CheckBox) mActivity.findViewById(R.id.cb);
        if(cb.isChecked()&&cb.isEnabled()) 
            return ("set"+ (String) ((TextView) cb).getText()).toLowerCase();
		
        return "expr";
    }

    /** 
     * Changes the TextView to the input string. 
     */
    public String outputLine(final String line) {
    	Log.e("AndroidPlatform", "outputLine");
        mTextViewOutput.setText(line);
        return (String) mTextViewOutput.getText();
    }

    /** 
     * Displays a the inputed code without a new line character. 
     */
    public String outputString(final String string) {
    	Log.e("AndroidPlatform", "outputChar");
    	if(string.equals("> "))
            return "";
        mTextViewOutput.setText(string);
        return (String) mTextViewOutput.getText();
    }

    /** 
     * Returns the platform name in a String. 
     */
    public String platformName() {
    	Log.e("AndroidPlatform", "platform name");
        return System.getProperty("java.specification.vendor");
    }

    /** 
     * Activates the designated mMenu item.
     */
    public void outputMenu(String numeral,
                           String option,
                           String selection) {
    	boolean enable = numeral.startsWith("1") 
            || option.equals("quit");
        mMenu.get(option).select(enable);
    }

    /** 
     * Disables the RadioButtons and unchecks them. 
     */
    public void disableAll(boolean verbose) {
        if (verbose) {
            errorLog("AndroidPlatform","disableAll true");
            disable((RadioGroup) mActivity.findViewById(R.id.rad1));
            disable((RadioGroup) mActivity.findViewById(R.id.rad2));
            disable((RadioGroup) mActivity.findViewById(R.id.rad3));
            EditText et = (EditText) mActivity.findViewById(R.id.et);
            et.setEnabled(false);
        }
    }

    /** 
     * Disables all radio buttons of a radio group.
     */
    private void disable(RadioGroup rg) {
    	for (int i = 0; i < rg.getChildCount(); i++)
            rg.getChildAt(i).setEnabled(false);
    }
    
    /** 
     * Enables the option specified. 
     */
    public void enableOption(String option) {
    	Log.e("AndroidPlatform", "enableOption");
    	mMenu.get(option).select(true);
    }

    /**
     * Specific for the printvisitor functionality.  Allows for the
     * addition of multiple lines to a textView display.
     */
    public String addString(String output) {
        errorLog("AndroidPlatform", "addString");
        mTextViewOutput.setText(mTextViewOutput.getText() + output);
        return output;
    }

    /** 
     * Error log formats the message and displays it for the debugging
     * purposes.
     */
    public void errorLog(String javaFile, 
                         String errorMessage) {
        Log.e(javaFile,errorMessage);
    }
}

