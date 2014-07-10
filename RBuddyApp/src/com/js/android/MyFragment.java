package com.js.android;

import static com.js.android.Tools.*;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class MyFragment extends Fragment {

	/**
	 * Constructor. Note, for technical reasons, each concrete subclass of this
	 * abstract class must provide a no-argument constructor (that does
	 * nothing).
	 */
	public MyFragment() {
	}

	protected void setLogging(boolean f) {
		mLogging = f;
	}

	protected void log(Object message) {
		if (mLogging) {
			StringBuilder sb = new StringBuilder("---> ");
			sb.append(nameOf(this));
			sb.append(" : ");
			tab(sb, 30);
			sb.append(message);
			pr(sb);
		}
	}

	@Override
	public void onAttach(Activity activity) {
		log("onAttach " + nameOf(activity));
		super.onAttach(activity);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		log("onCreate " + nameOf(savedInstanceState));
		super.onCreate(savedInstanceState);
		processSavedState(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		log("onActivityCreated " + nameOf(savedInstanceState));
		super.onActivityCreated(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle bundle) {
		log("onRestoreInstanceState; " + nameOf(bundle) + "\n\n");
	}

	@Override
	public void onStart() {
		log("onStart");
		super.onStart();
	}

	@Override
	public void onResume() {
		log("onResume");
		super.onResume();
		mWrappedFragment.onResumeAux();
	}

	@Override
	public void onPause() {
		log("onPause");
		super.onPause();
		mWrappedFragment.onPauseAux();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState; " + nameOf(outState) + "\n\n");
		super.onSaveInstanceState(outState);
		// Ask the pseudo fragment to save its view state, since scrollviews and
		// whatnot may be disappearing
		mWrappedFragment.onSaveViews();
	}

	@Override
	public void onDestroyView() {
		log("onDestroyView begins");
		mWrappedFragment.onDestroyView();
		super.onDestroyView();
		log("onDestroyView ends");
	}

	@Override
	public void onStop() {
		log("onStop");
		super.onStop();
	}

	@Override
	public void onDestroy() {
		log("onDestroy");
		super.onDestroy();
	}

	@Override
	public void onDetach() {
		log("onDetach");
		super.onDetach();
	}

	// From FragmentWrapper:
	private static final String BUNDLE_ORGANIZER_KEY = "organizer";

	/**
	 * Provide class being wrapped
	 * 
	 * @return subclass of MyFragment
	 */
	public abstract Class getFragmentClass();

	public void register(FragmentOrganizer f) {
		final boolean db = true;
		if (db)
			pr(hey(this) + "organizer=" + nameOf(f));

		Bundle args = new Bundle();
		args.putInt(BUNDLE_ORGANIZER_KEY, f.getUniqueId());
		setArguments(args);

		String name = getFragmentClass().getSimpleName();
		if (db)
			pr(" name=" + name);
		if (!f.isFactoryRegistered(name)) {
			if (db)
				pr("  registering factory");
			Factory factory = new Factory(this, name, f);
			f.register(factory);
			return;
		}
	}

	private void processSavedState(Bundle savedInstanceState) {
		final boolean db = true;
		if (db)
			pr(hey(this) + " savedState " + nameOf(savedInstanceState));
		if (savedInstanceState != null) {
			if (mWrappedFragment != null) {
				if (db)
					pr("  ...already have a wrapped fragment: "
							+ nameOf(mWrappedFragment));
				return;
			}
			int key = savedInstanceState.getInt(BUNDLE_ORGANIZER_KEY, -1);
			FragmentOrganizer f = FragmentOrganizer.getOrganizer(key);
			ASSERT(f != null);
			ASSERT(f.isAlive());
			// It was already registered, so get the singleton object
			mWrappedFragment = f.getWrappedSingleton(getFragmentClass());
			if (db)
				pr("  set mWrappedFragment to " + nameOf(mWrappedFragment));
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		log("onCreateView");
		processSavedState(savedInstanceState);
		if (mWrappedFragment == null)
			die("no wrapped fragment for " + nameOf(this) + ": "
					+ getFragmentClass().getSimpleName());

		// Perhaps don't send state along, since we are persisting it in other
		// methods
		return mWrappedFragment.onCreateView();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append(" (wrappedFragment " + nameOf(mWrappedFragment) + ")");
		return sb.toString();
	}

	private PseudoFragment mWrappedFragment;

	static class Factory {

		public Factory(MyFragment wrapper, String name,
				FragmentOrganizer organizer) {
			mWrapperClass = wrapper.getClass();
			mName = name;
			mOrganizer = organizer;
		}

		/**
		 * Get name of fragment. This is what is used as its tag when it is
		 * added to an activity
		 * 
		 * @return
		 */
		public String name() {
			return mName;
		}

		public MyFragment construct() {
			MyFragment f = null;
			try {
				f = (MyFragment) mWrapperClass.getConstructor().newInstance();
				Bundle b = new Bundle();
				b.putInt(BUNDLE_ORGANIZER_KEY, mOrganizer.getUniqueId());
				f.setArguments(b);
				f.processSavedState(b);
			} catch (Throwable t) {
				die("problem creating instance of " + mWrapperClass
						+ "; did you supply a default constructor?", t);
			}
			return f;
		}

		private String mName;
		private Class mWrapperClass;
		private FragmentOrganizer mOrganizer;
	}

	private boolean mLogging;
}
