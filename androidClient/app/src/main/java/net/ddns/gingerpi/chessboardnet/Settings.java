package net.ddns.gingerpi.chessboardnet;

import android.app.Activity;
import android.os.Bundle;
import android.widget.GridView;

public class Settings extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		GridView set1_selector =findViewById(R.id.set1_selector);
		SetSelectorAdapter s=new SetSelectorAdapter(this);
		set1_selector.setAdapter(s);

		GridView set2_selector =findViewById(R.id.set2_selector);
		set2_selector.setAdapter(s);
	}
}
