package fr.eurecom.wifi3gproject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class Fragment1 extends DialogFragment {
	
	static Fragment1 newInstance(String title) {
		Fragment1 fragment = new Fragment1();
		Bundle args = new Bundle();
		args.putString("title", title);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		String title = getArguments().getString("title");
		return new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.wifi_offloading_1_editted)
				.setTitle(title)
				.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						((MainActivity) getActivity()).doPositiveClick();
					}
				}).create();
	}
}
