package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.Serializable;

import de.kobich.audiosolutions.frontend.common.ui.editor.ICollectionEditor.CollectionEditorType;

public interface IOpeningInfo extends Serializable {
	CollectionEditorType getEditorType();
	
	String getName();
}
