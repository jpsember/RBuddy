package com.js.android;

import static com.js.android.Tools.*;
import com.js.android.FragmentOrganizer;
import com.js.android.MyFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class FragmentWrapper extends MyFragment {

	/**
	 * Get the class that this wrapper contains an instance of
	 * 
	 * @return
	 */
	public abstract Class getFragmentClass();

	public FragmentWrapper() {
		assertUIThread();

		// If we haven't registered a factory for this class of wrapper, do so;
		// and in that case, don't attempt to construct the wrapped singleton,
		// since the factory wasn't prepared.
		FragmentOrganizer f = App.sharedInstance().fragments();
		String name = getFragmentClass().getSimpleName();

		if (!f.isFactoryRegistered(name)) {
			Factory factory = new OurFactory(this, name);
			f.register(factory);
			return;
		}

		// It was already registered, so get the singleton object
		mWrappedFragment = f.getWrappedSingleton(getFragmentClass());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return mWrappedFragment.onCreateView(inflater, container,
				savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
		mWrappedFragment.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mWrappedFragment.onPause();
	}

	@Override
	public void onDestroyView() {
		mWrappedFragment.onDestroyView();
		super.onDestroyView();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mWrappedFragment.onSaveInstanceState(outState);
	}

	private MyFragment mWrappedFragment;

	private static class OurFactory implements Factory {

		public OurFactory(FragmentWrapper wrapper, String name) {
			mWrapperClass = wrapper.getClass();
			mName = name;
		}

		@Override
		public String name() {
			return mName;
		}

		@Override
		public MyFragment construct() {
			MyFragment f = null;
			try {
				f = (MyFragment) mWrapperClass.getConstructor().newInstance();
			} catch (Throwable t) {
				die("problem creating instance of " + mWrapperClass
						+ "; did you supply a default constructor?", t);
			}
			return f;
		}

		private String mName;
		private Class mWrapperClass;
	}
}
