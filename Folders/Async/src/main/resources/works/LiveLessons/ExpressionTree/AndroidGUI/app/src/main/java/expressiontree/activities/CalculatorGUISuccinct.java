package expressiontree.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import expressiontree.R;
import expressiontree.input.InputDispatcher;
import expressiontree.input.InputHandler;
import expressiontree.platspecs.Platform;
import expressiontree.platspecs.PlatformFactory;
import expressiontree.utils.Options;

/**
 * This class formats the succinct GUI for the Android version of the
 * expression tree application.
 */
public class CalculatorGUISuccinct 
       extends Activity {
    /** 
     * TextView object displays the output of the expression Tree
     * calculate button.
     */
    private TextView mTextView;
		
    /** 
     * EditText object intakes user input for the interpreter. 
     */
    private EditText mEditText;
		
    /** 
     * Button 'Enter' triggers the evaluation of the expression. 
     */
    private Button mButton;
		
    /**
     * Creates eventhandler responsible for giving commands. 
     */
    private static InputHandler h;

    /**
     * Creation hook method.
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
			
        // Sets the content view to the xml file, activity_main.
        setContentView(R.layout.succinct_activity_main);

        // Creates the TextView object and coordinates the xml layout
        // with the respective java code.
        mTextView = (TextView)findViewById(R.id.tv);
			
        // Creates the EditText object and coordinates the xml layout
        // with the respective java code.
        mEditText = (EditText)findViewById(R.id.et);
			
        // Creates generic button.
        mButton = new Button(this);

        // Initializes the Platform singleton with the appropriate
        // Platform strategy, which in this case will be the
        // AndroidPlatform.
        Platform.instance(new PlatformFactory(mEditText,
                                              mTextView,
                                              this).makePlatform());

        // Initializes the Options singleton.
        String args[] = new String[] { "CalculatorGUISuccinct" };
        Options.instance().parseArgs(args);
    }

    /**
     * Creates the options menu (Succinct and Verbose mode).
     * */
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it
        // is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
		
    /**
     * Adds two clickable cases to the options menu.
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent = null;

        switch(item.getItemId()) {
        case R.id.Succinct:
            Toast.makeText(getApplicationContext(),
                           "Switching to succinct mode",
                           Toast.LENGTH_SHORT).show();
            // Sets an intent for switching between the verbose
            // and succinct activities.
            intent = new Intent(getApplicationContext(),
                                CalculatorGUISuccinct.class);
            break;
        case R.id.Verbose:
            Toast.makeText(getApplicationContext(),
                           "Switching to verbose mode",
                           Toast.LENGTH_SHORT).show();
            // Sets an intent for switching between the verbose
            // and succinct activities.
            intent = new Intent(getApplicationContext(),
                                CalculatorGUIVerbose.class);
            break;
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
        return false;
    }
		
    /**
     * Sets the action of the button on click state.
     */
    public void enterButtonClicked(View view) {
        // Create an InputHandler to process the user input expression
        // where the mEditText contains the input and the mTextView will
        // be the output.
        InputDispatcher.instance().makeHandler(false,
                                               mEditText,
                                               mTextView,
                                               this);

        // Process the user input expression.
        InputDispatcher.instance().dispatchOneInput();
    }
		
    /**
     * Inputs the respective value in the calculator bar.
     */
    public void characterButtonClicked(View view) {
        mEditText.setText(mEditText.getText().toString()
                         + ((Button)view).getText());
    }
		
    /**
     * Clears the editText box.
     */
    public void clrButtonClicked(View view) {
        mEditText.setText("");
    }
		
    /** 
     * Retrieves the previous answer in the mTextView and adds it to
     * the mEditText box.
     */
    public void ansButtonClicked(View view) {
        mEditText.setText(mEditText.getText().toString()
                         + mTextView.getText().toString());
    }
		
    /**
     * Back button removes character from input field.
     */
    public void backButtonClicked(View view) {
    	String text = mEditText.getText().toString();
    	
    	if(!text.equals("")) {
            String textMinusLastChar = text.substring(0, text.length() - 1);
            mEditText.setText(textMinusLastChar);
        }
    }
}	
		
