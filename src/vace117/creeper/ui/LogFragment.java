package vace117.creeper.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Scrolling Creeper log screen
 *
 * @author Val Blant
 */
public class LogFragment extends Fragment {
	public TextView logString;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		
		View logView = inflater.inflate(R.layout.fragment_log, container, false);
	    
		logString = (TextView) logView.findViewById(R.id.logString);
	    logString.setMovementMethod(new ScrollingMovementMethod());
	    logString.setFreezesText(true);
	    
		return logView;
	}

	@Override
	public void onResume() {
		super.onResume();
		
	    ((BootstrapActivity)getActivity()).onLogFragmentReady();
	}


}
