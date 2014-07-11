package com.js.android;

import static com.js.android.Tools.*;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public abstract class MyFragment extends Fragment {

	private static final String BUNDLE_ORGANIZER_KEY = "organizer";

	/**
	 * Constructor. Note, for technical reasons, each concrete subclass of this
	 * abstract class must provide a no-argument constructor (that does
	 * nothing).
	 */
	public MyFragment() {
		if (db)
			pr(hey(this) + "constructing; " + stackTrace(0, 10));
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
		log("onCreate " + nameOf(savedInstanceState)
				+ dumpState(savedInstanceState, this.getArguments()));
		super.onCreate(savedInstanceState);
		processSavedState(savedInstanceState);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		log("onActivityCreated " + nameOf(savedInstanceState)
				+ dumpState(savedInstanceState, this.getArguments()));
		super.onActivityCreated(savedInstanceState);
	}

	public void onRestoreInstanceState(Bundle bundle) {
		log("onRestoreInstanceState; " + nameOf(bundle)
				+ dumpState(bundle, this.getArguments()) + "\n\n");
	}

	@Override
	public void onStart() {
		log("onStart");
		super.onStart();
		updatePseudoFragment();
	}

	@Override
	public void onResume() {
		log("onResume");
		super.onResume();
		updatePseudoFragment();
		mWrappedFragment.onResumeAux();
	}

	@Override
	public void onPause() {
		log("onPause");
		super.onPause();
		updatePseudoFragment();
		mWrappedFragment.onPauseAux();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		log("onSaveInstanceState; " + nameOf(outState)
				+ dumpState(outState, this.getArguments()) + "\n\n");
		super.onSaveInstanceState(outState);

		// The fragment ought to have a bundle id stored by this point
		if (db)
			pr(hey(this) + " >>>>> currently stored key: "
					+ outState.get(BUNDLE_ORGANIZER_KEY));

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
		updatePseudoFragment();
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

	/**
	 * Provide class being wrapped
	 * 
	 * @return subclass of MyFragment
	 */
	public abstract Class getFragmentClass();

	public void register(FragmentOrganizer f) {
		if (db)
			pr(hey(this) + "organizer=" + nameOf(f));

		Bundle args = this.getArguments();
		if (args == null) {
			args = new Bundle();
			args.putString(BUNDLE_ORGANIZER_KEY, f.getLabel());
			setArguments(args);

			if (db)
				pr(hey(this) + " >>>>> stored organizer key " + f.getLabel()
						+ " in bundle key;"
						+ dumpState(null, this.getArguments()));
		}

		String name = getFragmentClass().getSimpleName();
		if (!f.isFactoryRegistered(name)) {
			Factory factory = new Factory(this, name, f);
			if (db)
				pr(" registering factory " + nameOf(factory) + " name " + name
						+ " for fragment " + nameOf(this));
			f.register(factory);
		}
	}

	private void setFragmentOrganizer(FragmentOrganizer f) {
		if (f != mFragmentOrganizer) {
			mFragmentOrganizer = f;
		}
	}

	private void processSavedState(Bundle savedInstanceState) {
		// TODO: investigate whether we need separate code for our factory
		// constructor AND the onCreate(Bundle) method to determine the
		// pseudoFragment
		if (db)
			pr(hey(this) + " savedState " + nameOf(savedInstanceState)
					+ dumpState(savedInstanceState, this.getArguments()));

		// TODO maybe we need to update the wrapped fragment always here?
		if (mWrappedFragment != null) {
			if (db)
				pr("  ...already have a wrapped fragment: "
						+ nameOf(mWrappedFragment));
			return;
		}

		String key = null;

		if (savedInstanceState != null) {
			key = savedInstanceState.getString(BUNDLE_ORGANIZER_KEY);
			if (db)
				pr(" recalled key from savedInstanceState: " + key);
		}
		if (key == null) {
			Bundle b2 = this.getArguments();
			key = b2.getString(BUNDLE_ORGANIZER_KEY);
			if (db)
				pr(" recalled key from current arguments: " + key);
		}

		ASSERT(key != null, "no organizer key in saved state");

		// int key = savedInstanceState.getInt(BUNDLE_ORGANIZER_KEY, -1);

		FragmentOrganizer f = FragmentOrganizer.getOrganizer(key);
		ASSERT(f != null);
		setFragmentOrganizer(f);

		updatePseudoFragment();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		log("onCreateView");
		processSavedState(savedInstanceState);
		if (mWrappedFragment == null)
			die("no wrapped fragment for " + nameOf(this) + ": "
					+ getFragmentClass().getSimpleName());
		log("wrappedFragment=" + describe(mWrappedFragment));
		return mWrappedFragment.onCreateView(this);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(nameOf(this));
		sb.append(" (wrappedFragment " + nameOf(mWrappedFragment) + ")");
		return sb.toString();
	}

	private void updatePseudoFragment() {
		// It's holding onto old fragment organizer! Not just the old fragment.

		final boolean db = true;
		if (db)
			pr(hey() + "organizer=" + nameOf(mFragmentOrganizer) + "\n"
					+ stackTrace(1, 3));
		ASSERT(mFragmentOrganizer != null);

		mFragmentOrganizer = mFragmentOrganizer.mostRecent();

		PseudoFragment f = mFragmentOrganizer
				.getWrappedSingleton(getFragmentClass());
		if (f != mWrappedFragment) {
			if (db)
				pr("change fragment from " + nameOf(mWrappedFragment) + " to "
						+ nameOf(f));
			mWrappedFragment = f;
		}
	}

	private String dumpState(Bundle b1, Bundle b2) {
		StringBuilder sb = new StringBuilder(" [");

		sb.append("current:");
		if (b1 == null)
			sb.append("---");
		else
			sb.append(b1.get(BUNDLE_ORGANIZER_KEY));
		sb.append(" aux:");
		if (b2 == null)
			sb.append("---");
		else
			sb.append(b2.get(BUNDLE_ORGANIZER_KEY));
		sb.append("]");
		return sb.toString();

	}

	private boolean mLogging;
	private FragmentOrganizer mFragmentOrganizer;
	private PseudoFragment mWrappedFragment;

	static class Factory {

		public Factory(MyFragment fragment, String name,
				FragmentOrganizer organizer) {
			mWrapperClass = fragment.getClass();
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
				b.putString(BUNDLE_ORGANIZER_KEY, mOrganizer.getLabel());
				f.setArguments(b);
				if (db)
					pr(hey(this) + " >>>>> stored organizer key "
							+ mOrganizer.getLabel() + " in bundle key");
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

}
