package com.sndurkin.notificationcheck;

import android.content.Context;
import android.preference.ListPreference;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;

public class RestrictableListPreference extends ListPreference {

    private String reasonForRestriction;

    public RestrictableListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RestrictableListPreference(Context context) {
        this(context, null);
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
