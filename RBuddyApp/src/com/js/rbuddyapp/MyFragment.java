package com.js.rbuddyapp;

//import static com.js.android.Tools.*;

//import java.util.ArrayList;

//import android.app.Activity;
import android.app.Fragment;
//import android.os.Bundle;

//import android.os.Message;

public abstract class MyFragment extends Fragment {

	// @Override
	// public void onResume() {
	// final boolean db = true;
	// // final boolean db = true;
	// if (db)
	// pr(hey());
	// super.onResume();
	// pauseHandler.resume();
	// }
	//
	// @Override
	// public void onPause() {
	// final boolean db = true;
	// if (db)
	// pr(hey());
	// super.onPause();
	// pauseHandler.pause();
	// }
	//
	// public void postMessage(Message m) {
	// pauseHandler.storeMessage(m);
	// }
	//
	// protected abstract void processMessage(Message m);
	//
	// private PauseHandler pauseHandler = new PauseHandler();
	//
	// private class PauseHandler {
	//
	// /**
	// * Message Queue Buffer
	// */
	// private ArrayList<Message> messageQueueBuffer = new ArrayList<Message>();
	//
	// /**
	// * Flag indicating the pause state
	// */
	// private boolean paused = true;
	//
	// /**
	// * Resume the handler
	// */
	// public void resume() {
	// paused = false;
	// processMessages();
	// }
	//
	// private void processMessages() {
	// final boolean db = true;
	// if (db)
	// pr(hey() + " paused=" + paused);
	//
	// while (!paused) {
	// Message msg = getNextMessage();
	// if (db)
	// pr(hey() + "message=" + msg);
	// if (msg == null)
	// break;
	// processMessage(msg);
	// }
	// }
	//
	// /**
	// * Pause the handler
	// */
	// public void pause() {
	// paused = true;
	// }
	//
	// /**
	// * Notification that the message is about to be stored as the activity
	// * is paused. If not handled the message will be saved and replayed when
	// * the activity resumes.
	// *
	// * @param message
	// * the message which optional can be handled
	// * @return true if the message is to be stored
	// */
	// public void storeMessage(Message message) {
	// final boolean db = true;
	// if (db)
	// pr(hey() + "message=" + message);
	// messageQueueBuffer.add(message);
	// processMessages();
	// }
	//
	// public Message getNextMessage() {
	// if (paused)
	// return null;
	// if (messageQueueBuffer.isEmpty())
	// return null;
	// return messageQueueBuffer.remove(0);
	// }
	// }

	public static interface Factory {
		public String tag();
		public MyFragment construct();
	}

	// protected void setParent(Activity parent) {
	// this.parent = parent;
	// }
	//
	// protected Activity getParent() {
	// this.
	// ASSERT(parent != null);
	// return parent;
	// }
	//
	// private Activity parent;
}
