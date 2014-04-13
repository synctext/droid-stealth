package content;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.support.v7.view.ActionMode;
import com.stealth.android.R;
import com.stealth.dialog.DialogConstructor;
import com.stealth.dialog.DialogOptions;
import com.stealth.dialog.IDialogResponse;
import com.stealth.files.IndexedFile;
import com.stealth.files.IndexedItem;
import com.stealth.utils.IOnResult;
import com.stealth.utils.Utils;
import encryption.EncryptionManager;
import encryption.IContentManager;

/**
 * Created by Alex on 13-4-2014.
 */
public class ActionManager implements IActionManager {

	private ActionMode mActionMode;
	private IContentManager mContentManager;
	private EncryptionManager mEncryptionManager;

	public ActionManager(IContentManager contentManager){
		mContentManager = contentManager;
	}

	@Override
	public void setEncryptionManager(EncryptionManager encryptionManager) {
		mEncryptionManager = encryptionManager;
	}

	@Override
	public void setActionMode(ActionMode actionMode) {
		mActionMode = actionMode;
	}

	@Override
	public void actionShred(ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {
		mContentManager.removeItems(with, listener);

		//TODO always everything removed?
		finishActionMode();
	}

	@Override
	public void actionOpen(IndexedItem with, IOnResult<Boolean> listener) {

	}

	@Override
	public void actionLock(ArrayList<IndexedItem> with, final IOnResult<Boolean> listener) {
		if(mEncryptionManager == null)
			Utils.d("Called lock when encryption service not bound!");

		if (with == null) {
			Utils.d("We got an empty list to process. Can't deal with this.");
			return;
		}

		for (IndexedItem item : with) {
			if (item instanceof IndexedFile) {
				// clear the thumbnails because if we lock the file
				// it might have changed
				((IndexedFile) item).clearThumbnail();
			}
		}

		mEncryptionManager.encryptItems(with, new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if (result) {
					finishActionMode();
				}

				if(listener != null)
					listener.onResult(result);
			}
		});
	}

	@Override
	public void actionUnlock(ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {
		if(mEncryptionManager == null)
			Utils.d("Called unlock when encryption service not bound!");

		mEncryptionManager.decryptItems(with, new IOnResult<Boolean>() {
			@Override
			public void onResult(Boolean result) {
				if (result) {
					finishActionMode();
				}
			}
		});
	}

	@Override
	public void actionShare(ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {

	}

	@Override
	public void actionRestore(ArrayList<IndexedItem> with, IOnResult<Boolean> listener) {

	}

	private void finishActionMode() {
		if (mActionMode == null) {
			return;
		}

		Utils.runOnMain(new Runnable() {
			@Override
			public void run() {
				mActionMode.finish();
			}
		});
	}
}
