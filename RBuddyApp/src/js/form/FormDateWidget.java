package js.form;

import java.text.ParseException;

import js.rbuddy.AndroidDate;
import js.rbuddy.JSDate;
import android.app.DatePickerDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

public class FormDateWidget extends FormTextWidget {
	public FormDateWidget(FormField owner) {
		super(owner);

		input.setHint(getOwner().strArg("hint", "date"));
		input.setFocusable(false);
		input.setClickable(true);
		input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processClick();
			}
		});
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

	private void processClick() {
		setEnabled(true);

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

		new DatePickerDialog(getOwner().getOwner().context(), dateListener,
				ymd[0], ymd[1], ymd[2]).show();
	}

}
