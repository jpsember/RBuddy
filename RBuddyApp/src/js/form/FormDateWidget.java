package js.form;

import static js.basic.Tools.*;

import java.text.ParseException;
import java.util.Map;

import js.rbuddy.AndroidDate;
import js.rbuddy.JSDate;
import android.app.DatePickerDialog;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;

public class FormDateWidget extends FormTextWidget {
	public FormDateWidget(FormItem item, Map<String, Object> arguments) {
		super(item, arguments);

		input.setFocusable(false);

		input.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				processClick();
			}
		});
	}
	
	@Override
	public JSDate getDate() {
		String content = input.getText().toString();
		JSDate ret = null;
		try {
			ret = AndroidDate.parseJSDateFromUserString(content);
		} catch (ParseException e) {
			warning("problem parsing " + e);
		}
		return ret;
	}

	private void processClick() {
		DatePickerDialog.OnDateSetListener dateListener = new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				JSDate date = JSDate.buildFromValues(year, monthOfYear,
						dayOfMonth);
				input.setText(AndroidDate.formatUserDateFromJSDate(date));
			}
		};
		JSDate date = getDate();
		if (date == null) {
			date = JSDate.currentDate();
		}
		int[] ymd = AndroidDate.getJavaYearMonthDay(date);

		new DatePickerDialog(this.formItem.owner().context(), dateListener,
				ymd[0], ymd[1], ymd[2]).show();
	}

}
