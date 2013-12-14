package vace117.creeper.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Instantiates the screens (tabs) for the ViewPager
 *
 * @author Val Blant
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
	
    enum Tabs {
    	LOG(0), 
    	SCARY_CREEPER(1);
    	
    	public int index;

		private Tabs(int index) {
			this.index = index;
		}
    }

	public ViewPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {
		if ( Tabs.LOG.index == index ) {
			return new LogFragment();
		}
		else if ( Tabs.SCARY_CREEPER.index == index ) {
			return new ScaryCreeperFragment();
		}
		else {
			return null;
		}
	}

	@Override
	public int getCount() {
		return Tabs.values().length;
	}

}
