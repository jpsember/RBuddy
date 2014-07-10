package com.js.android;

import static com.js.android.Tools.*;

import com.js.android.FragmentOrganizer;
import com.js.android.MyFragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment subclass that acts as a wrapper for singleton fragments.
 * 
 * Generally, the Android OS will destroy and reconstruct fragments, e.g., if
 * backstack or orientation events occur. This can be problematic, since it is
 * convenient to treat some fragments as if they are singletons. Suppose you
 * have an instance X of a MyFragment subclass, and X is intended to be a
 * singleton. An instance Y of the FragmentWrapper class can act as a wrapper
 * for X. Even if multiple instances Y',Y''... end of being constructed, they
 * will all contain the same instance X.
 * 
 * To have X's class behave in this way, there are two steps:
 * 
 * 1) have X define Y's class, a (public) concrete subclass of FragmentWrapper,
 * one that includes two methods:
 * 
 * ... i) a zero-argument constructor; and
 * 
 * ... ii) getFragmentClass(), a method that returns X's class
 * 
 * 2) In X's constructor, make sure Y's class is registered by constructing an
 * instance of Y.
 * 
 * See ReceiptEditor for an example.
 * 
 */
public abstract class FragmentWrapper extends MyFragment {

	/**
	 * Provide class being wrapped
	 * 
	 * @return subclass of MyFragment
	 */
	public abstract Class getFragmentClass();

	/**
	 * Constructor. Note, for technical reasons, the concrete subclass of this
	 * abstract class must provide a no-argument constructor (that does
	 * nothing).
	 */
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
		// Perhaps don't send state along, since we are persisting it in other
		// methods
		return mWrappedFragment.onCreateView();
	}

	@Override
	public void onResume() {
		super.onResume();
		mWrappedFragment.onResumeAux();
	}

	@Override
	public void onPause() {
		super.onPause();
		mWrappedFragment.onPauseAux();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		// Ask the pseudo fragment to save its view state, since scrollviews and
		// whatnot may be disappearing
		mWrappedFragment.onSaveViews();
	}

	@Override
	public void onDestroyView() {
		mWrappedFragment.onDestroyView();
		super.onDestroyView();
	}

	private PseudoFragment mWrappedFragment;

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
