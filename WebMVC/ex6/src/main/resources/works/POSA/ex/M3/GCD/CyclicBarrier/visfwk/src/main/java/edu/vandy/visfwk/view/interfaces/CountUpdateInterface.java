package edu.vandy.visfwk.view.interfaces;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import edu.vandy.visfwk.utils.UiUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Interface for the Presenter layer to interact with the Count EditText.
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public interface CountUpdateInterface<TestFunc> {
    /**
     * This field stores the EditText reference that's initialized
     * after the fact by the initializePresenterInterface()
     * method. This is done to use this interface with defaults as a
     * means to achieve multiple inheritance.
     */
    ArrayList<WeakReference<EditText>> editTextRef =
            new ArrayList<>(1);

    /**
     * Initialize the Counter instance from the UI to this interface
     * so that this interface's default methods can operate properly.
     */
    default void initializeCounter(EditText editText,
                                   ViewInterface<TestFunc> viewInterface) {
        if (editTextRef.size() != 0)
            editTextRef.remove(0);

        editTextRef.add(0,
                new WeakReference<>(editText));
    }

    /**
     * Helper method to localize access of the EditText from its
     * storage and to give runtime exception if EditText was not
     * properly initialized.
     */
    default EditText getCountEditText() {
        EditText count = editTextRef.get(0).get();
        if (count == null)
            throw new RuntimeException("Count EditText Uninitialized");

        return count;
    }

    /**
     * Set the text on the EditText for Count
     */
    default void setEditText(String text) {
        getCountEditText().setText(text);
    }

    /**
     * Request focus onto the EditText.
     */
    default void countEditTextRequestFocus() {
        getCountEditText().requestFocus();
    }

    /**
     * Get the Editable from the EditText.
     */
    default Editable countEditTextGetText() {
        return getCountEditText().getEditableText();
    }

    /**
     * Set an {@link android.widget.TextView.OnEditorActionListener}
     * for the EditText. This notifies the listener whenever the text
     * of the EditText changes.
     */
    default void countEditTextSetOnEditorActionListener(TextView.OnEditorActionListener listener) {
        getCountEditText().setOnEditorActionListener(listener);
    }
}
