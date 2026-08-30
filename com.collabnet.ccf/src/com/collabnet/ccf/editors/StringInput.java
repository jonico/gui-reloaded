package com.collabnet.ccf.editors;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IPersistableElement;
import org.eclipse.ui.IStorageEditorInput;

public class StringInput extends PlatformObject implements IStorageEditorInput {
	private IStorage storage;
	
	public StringInput(IStorage storage) {
		this.storage = storage;
	}

	public IStorage getStorage() throws CoreException {
		return storage;
	}

	public boolean exists() {
		return true;
	}

	public ImageDescriptor getImageDescriptor() {
		return null;
	}

	public String getName() {
		return storage.getName();
	}

	public IPersistableElement getPersistable() {
		return null;
	}

	public String getToolTipText() {
		return getName();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof StringInput input) {
			return getName().equals(input.getName());
		}
		return super.equals(obj);
	}

}
