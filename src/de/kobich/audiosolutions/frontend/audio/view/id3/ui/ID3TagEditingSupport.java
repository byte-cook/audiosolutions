package de.kobich.audiosolutions.frontend.audio.view.id3.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.AudioFileResult;
import de.kobich.audiosolutions.core.service.mp3.id3.ID3TagVersion;
import de.kobich.audiosolutions.core.service.mp3.id3.IFileID3TagService;
import de.kobich.audiosolutions.core.service.mp3.id3.MP3ID3TagType;
import de.kobich.audiosolutions.core.service.mp3.id3.WriteSingleID3TagRequest;
import de.kobich.audiosolutions.frontend.audio.view.id3.ID3TagView;
import de.kobich.audiosolutions.frontend.audio.view.id3.model.ID3TagItem;
import de.kobich.audiosolutions.frontend.common.FileDescriptorConverter;
import de.kobich.audiosolutions.frontend.common.listener.ActionType;
import de.kobich.audiosolutions.frontend.common.listener.EventSupport;
import de.kobich.audiosolutions.frontend.common.listener.UIEvent;
import de.kobich.audiosolutions.frontend.common.ui.FileResultDialog;
import de.kobich.commons.converter.ConverterUtils;
import de.kobich.commons.ui.jface.JFaceThreadRunner;
import de.kobich.commons.ui.jface.JFaceThreadRunner.RunningState;

public class ID3TagEditingSupport extends EditingSupport {
	private static final Logger logger = Logger.getLogger(ID3TagEditingSupport.class);
	private static final String KEEP_VALUE = "<keep-value>";
	private static final String DELETE_VALUE = "<delete-value>";
	private ComboBoxCellEditor editor;
	private TextCellEditor textCellEditor;
	private ID3TagView view;
	private TableViewer viewer;
	private ID3TagColumnType column;

	public ID3TagEditingSupport(ID3TagView view, TableViewer viewer, ID3TagColumnType column) {
		super(viewer);
		this.view = view;
		this.viewer = viewer;
		this.column = column;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (ID3TagColumnType.VALUE.equals(column)) {
			if (element instanceof ID3TagItem) {
				ID3TagItem item = (ID3TagItem) element;
				return item.getKey().isEditable();
			}
		}
		return false;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof ID3TagItem) {
			ID3TagItem item = (ID3TagItem) element;
			List<String> values = new ArrayList<String>();
			values.addAll(item.getValues());
			if (item.getValues().size() > 1) {
				values.add(0, DELETE_VALUE);
				values.add(0, KEEP_VALUE);
				Collections.sort(values);
				textCellEditor = null;
				editor = new ComboBoxCellEditor(viewer.getTable(), values.toArray(new String[0]));
				return editor;
			}
			else {
				editor = null;
				textCellEditor = new TextCellEditor(viewer.getTable());
//				if (!values.isEmpty()) {
//					textCellEditor.setValue(values.iterator().next());
//				}
				return textCellEditor;
			}
		}
		return null;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof ID3TagItem) {
			ID3TagItem item = (ID3TagItem) element;
			if (editor != null) {
				return Integer.valueOf(0);
			}
			else if (textCellEditor != null) {
				Set<String> values = item.getValues();
				if (!values.isEmpty()) {
					return values.iterator().next();
				}
				return "";
			}
		}
		return null;
	}

	@Override
	protected void setValue(final Object element, final Object valueObj) {
		if (element instanceof ID3TagItem) {
			final ID3TagItem item = (ID3TagItem) element;
			final MP3ID3TagType tag = item.getKey();
			
			List<RunningState> states = Arrays.asList(RunningState.UI_1, RunningState.WORKER_1, RunningState.UI_2);
			JFaceThreadRunner runner = new JFaceThreadRunner("MP3 ID3 Tags", view.getViewSite().getShell(), states) {
				private String value;
				private AudioFileResult result;

				@Override
				protected void run(RunningState state) throws Exception {
					switch (state) {
					case UI_1:
						// get value to set
						if (editor != null) {
							int index = Integer.parseInt("" + valueObj);
							if (index == -1) {
								value = ((CCombo) editor.getControl()).getText();
							}
							else {
								value = editor.getItems()[index];
							}
							
							if (DELETE_VALUE.equals(value)) {
								value = "";
							}
							if (KEEP_VALUE.equals(value)) {
								value = null;
								super.setNextState(RunningState.UI_2);
							}
						}
						else if (textCellEditor != null) {
							Set<String> values = item.getValues();
							value = ((Text) textCellEditor.getControl()).getText();
							if (!values.isEmpty() && values.iterator().next().equals(value)) {
								value = null;
								super.setNextState(RunningState.UI_2);
							}
						}
						break;
					case WORKER_1:
						logger.info("Set tag: " + tag + " to value: " + value);
						IFileID3TagService id3TagService = AudioSolutions.getService(IFileID3TagService.JAUDIO_TAGGER, IFileID3TagService.class);
						WriteSingleID3TagRequest request = new WriteSingleID3TagRequest(item.getFileDescriptors(), tag, value, ID3TagVersion.ALL);
						request.setProgressMonitor(super.getProgressMonitor());
						this.result = id3TagService.writeSingleID3Tag(request);
						break;
					case UI_2:
						if (value == null) {
							return;
						}
						
						// show failed dialog
						if (!result.getFailedFiles().isEmpty()) {
							// run asynchronously
							super.getParent().getDisplay().asyncExec(new Runnable() {
								@Override
								public void run() {
									FileResultDialog dialog = FileResultDialog.createDialog(getParent(), getName(), "ID3 tag could not be written.", result.getFailedFiles());
									dialog.open();
								}
							});
						}
						
						UIEvent event = new UIEvent(ActionType.FILE);
						Collection<File> updatedFiles = ConverterUtils.convert(result.getSucceededFiles(), FileDescriptorConverter.INSTANCE);
						event.getFileDelta().getModifyItems().addAll(updatedFiles);
						EventSupport.INSTANCE.fireEvent(event);

						Set<String> values = new HashSet<String>();
						values.add(value);
						item.setValues(values);
						view.fireSelection(item.getFileDescriptors());
						
						getViewer().update(element, null);
						break;
					case UI_ERROR:
						if (super.getProgressMonitor().isCanceled()) {
							return;
						}
						Exception e = super.getException();
						logger.error(e.getMessage(), e);
						MessageDialog.openError(super.getParent(), super.getName(), "Operation failed: \n" + e.getMessage());
						break;
					default: 
						break;
					}
				}
			};
			runner.runProgressMonitorDialog(true, false);
		}
	}

}
