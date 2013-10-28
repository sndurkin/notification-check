package com.sndurkin.notificationcheck;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.preference.ListPreference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

// This is a ListPreference with the following extra features:
//  - allows it to be disabled with added text explaining the reason it's restricted
//  - allows different dialog entry text from the summary text
public class EnhancedListPreference extends ListPreference {

    private int mClickedDialogEntryIndex;
    private CharSequence[] dialogEntries;

    private String reasonForRestriction;

    public EnhancedListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        if(attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.com_sndurkin_notificationcheck_EnhancedListPreference);
            dialogEntries = a.getTextArray(R.styleable.com_sndurkin_notificationcheck_EnhancedListPreference_dialogEntries);
            a.recycle();
        }
        if(dialogEntries == null) {
            dialogEntries = getEntries();
        }
    }

    public EnhancedListPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        mClickedDialogEntryIndex = findIndexOfValue(getValue());
        builder.setSingleChoiceItems(dialogEntries, mClickedDialogEntryIndex,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        mClickedDialogEntryIndex = which;

                        /*
                         * Clicking on an item simulates the positive button
                         * click, and dismisses the dialog.
                         */
                        EnhancedListPreference.this.onClick(dialog, DialogInterface.BUTTON_POSITIVE);
                        dialog.dismiss();
                    }
                });

        /*
         * The typical interaction for list-based dialogs is to have
         * click-on-an-item dismiss the dialog instead of the user having to
         * press 'Ok'.
         */
        builder.setPositiveButton(null, null);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult && mClickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String value = getEntryValues()[mClickedDialogEntryIndex].toString();
            if (callChangeListener(value)) {
                setValue(value);
            }
        }
    }

    @Override
    public CharSequence getSummary() {
        CharSequence summary = super.getSummary();
        if(reasonForRestriction != null && summary != null) {
            Spannable restriction = new SpannableString(reasonForRestriction);
            restriction.setSpan(new ForegroundColorSpan(getContext().getResources().getColor(R.color.restricted)), 0, restriction.length(), 0 );
            return TextUtils.concat(summary, "\n\n", restriction);
        }

        return summary;
    }

    public void setRestricted(String reasonForRestriction) {
        this.reasonForRestriction = reasonForRestriction;
        setEnabled(false);
        notifyChanged();
    }

}
