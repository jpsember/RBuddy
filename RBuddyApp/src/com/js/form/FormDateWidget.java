package com.js.form;

import java.text.ParseException;
import java.util.Map;

import com.js.rbuddyapp.AndroidDate;

import com.js.rbuddy.JSDate;
import android.app.DatePickerDialog;
import android.widget.DatePicker;

public class FormDateWidget extends FormTextWidget {
	public FormDateWidget(Form owner, Map attributes) {
		super(owner,attributes);

		input.setHint(strAttr("hint", "Date"));
	}

	@Override
	public void updateUserValue(String internalValue) {
		String dispVal = "";
		JSDate date = null;
		try {
			date = JSDate.parse(internalValue);
			dispVal = AndroidDate.formatUserDateFromJSDate(date);
		} catch (IllegalArgumentException e) {
		}
		setInputText(dispVal);
	}

	@Override
	public String parseUserValue() {
		String ret = "";
		try {
			String content = input.getText().toString();
			JSDate date = AndroidDate.parseJSDateFromUserString(content);
			ret = date.toString();
		} catch (ParseException e) {
		}
		return ret;
	}

	@Override
	protected int getFocusType() {
		return FOCUS_NEVER;
	}

	protected void processClick() {
		super.processClick();
		
		DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				JSDate date = JSDate.buildFromValues(year, monthOfYear,
						dayOfMonth);
				setInputText(AndroidDate.formatUserDateFromJSDate(date));
			}
		};
		JSDate date = JSDate.parse(getValue(), true);
		int[] ymd = AndroidDate.getJavaYearMonthDay(date);

		new DatePickerDialog(context(), dateListener,
				ymd[0], ymd[1], ymd[2]).show();
	}

}
