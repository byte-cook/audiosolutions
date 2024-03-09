package de.kobich.audiosolutions.frontend.audio.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.service.AudioDataChange;
import de.kobich.audiosolutions.core.service.cddb.ICDDBRelease;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.jface.table.ViewerColumn;
import de.kobich.commons.ui.jface.table.ViewerColumnManager;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

public class CDDBAudioDataWizardPage extends WizardPage implements Listener {
	public static ViewerColumn COLUMN_FILE = new ViewerColumn("File", 30);
	public static ViewerColumn COLUMN_TRACK = new ViewerColumn("Track", 20);
	public static ViewerColumn COLUMN_TRACK_NO = new ViewerColumn("No", 10);
	public static ViewerColumn COLUMN_ARTIST = new ViewerColumn("Artist", 20);
	public static ViewerColumn COLUMN_DISK = new ViewerColumn("Disk", 20);
	public static ViewerColumnManager COLUMNS = new ViewerColumnManager(COLUMN_FILE, COLUMN_TRACK, COLUMN_TRACK_NO, COLUMN_ARTIST, COLUMN_DISK);
	private TableViewer trackListViewer;
	private Button moveUpButton;
	private Button moveDownButton;
	private Button removeButton;
	private Button resetButton;
	private Text releaseText;
	private Text publicationText;
	private ArrayList<FileDescriptorTrackItem> model;

	public CDDBAudioDataWizardPage() {
		super("CDDBAudioData");
		super.setTitle("Preview Audio Data");
		super.setMessage("View mapping between files and audio data and allows modifications.");
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// info
		Composite previewComposite = new Composite(composite, SWT.NONE);
		previewComposite.setLayout(new GridLayout(2, false));
		previewComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label releaseLabel = new Label(previewComposite, SWT.NONE);
		releaseLabel.setText("Release:");
		releaseText = new Text(previewComposite, SWT.NONE);
		releaseText.setEditable(false);
		releaseText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label publicationLabel = new Label(previewComposite, SWT.NONE);
		publicationLabel.setText("Publication:");
		publicationText = new Text(previewComposite, SWT.NONE);
		publicationText.setEditable(false);
		publicationText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// edit
		Composite editComposite = new Composite(composite, SWT.NONE);
		editComposite.setLayout(new GridLayout(2, false));
		editComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		JFaceUtils.createHorizontalSeparator(editComposite, 2);
		// album list
		Composite tableComposite = new Composite(editComposite, SWT.NONE);
		tableComposite.setLayout(new GridLayout());
		tableComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		TableColumnLayout tableColumnLayout = new TableColumnLayout();
		tableComposite.setLayout(tableColumnLayout);
		trackListViewer = new TableViewer(tableComposite, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		for (final ViewerColumn column : COLUMNS) {
			final TableViewerColumn viewerColumn = new TableViewerColumn(trackListViewer, SWT.NONE, COLUMNS.indexOf(column));
			tableColumnLayout.setColumnData(viewerColumn.getColumn(), new ColumnWeightData(column.getWidthPercent(), column.getMinimumWidth()));
			viewerColumn.getColumn().setText(column.getName());
		}
		Table table = trackListViewer.getTable();
		table.setLayoutData(new GridData(GridData.FILL_BOTH));
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		trackListViewer.setContentProvider(ArrayContentProvider.getInstance());
		trackListViewer.setLabelProvider(new AlbumListLabelProvider());
		table.addListener(SWT.Selection, this);
		
		// actions
		Composite actionComposite = new Composite(editComposite, SWT.NONE);
		FillLayout al = new FillLayout(SWT.VERTICAL);
		actionComposite.setLayout(al);
		GridData ld = new GridData();
		ld.verticalAlignment = SWT.TOP;
		actionComposite.setLayoutData(ld);
		moveUpButton = new Button(actionComposite, SWT.NONE);
		moveUpButton.setText("Up");
		moveUpButton.addListener(SWT.Selection, this);
		moveUpButton.setEnabled(false);
		moveDownButton = new Button(actionComposite, SWT.NONE);
		moveDownButton.setText("Down");
		moveDownButton.addListener(SWT.Selection, this);
		moveDownButton.setEnabled(false);
		removeButton = new Button(actionComposite, SWT.NONE);
		removeButton.setText("Remove");
		removeButton.addListener(SWT.Selection, this);
		removeButton.setEnabled(false);
		resetButton = new Button(actionComposite, SWT.NONE);
		resetButton.setText("Reset");
		resetButton.addListener(SWT.Selection, this);
		
		setPageComplete(true);
		super.setControl(composite);
	}
	
	public void initPage(ICDDBRelease release, List<AudioDataChange> changes) {
		this.releaseText.setText(release.getAlbum());
		if (release.getPublication() != null) {
			this.publicationText.setText(release.getPublication());
		}
		
		this.model = new ArrayList<FileDescriptorTrackItem>();
		for (AudioDataChange change : changes) {
			FileDescriptorTrackItem item = new FileDescriptorTrackItem();
			item.fileDescriptor = change.getFileDescriptor();
//			item.audioValues = change.getAudioDataValues();
//			item.originalAudioValues = change.getAudioDataValues();
			item.audioDataChange = change;
			item.originalAudioDataChange = change;
			model.add(item);
		}
		Collections.sort(model);
		trackListViewer.setInput(model);
	}
	
	public void resetPage() {
		this.model = null;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.widget == trackListViewer.getTable()) {
			boolean itemSelected = !getSelectedItems().isEmpty();
			moveUpButton.setEnabled(itemSelected);
			moveDownButton.setEnabled(itemSelected);
			removeButton.setEnabled(itemSelected);
		}
		else if (event.widget == moveUpButton) {
			List<FileDescriptorTrackItem> items = getSelectedItems();
			if (items.contains(this.model.get(0))) {
				return;
			}
			moveItems(items, SWT.UP);
			trackListViewer.refresh();
		}
		else if (event.widget == moveDownButton) {
			List<FileDescriptorTrackItem> items = getSelectedItems();
			if (items.contains(this.model.get(this.model.size() - 1))) {
				return;
			}
			moveItems(items, SWT.DOWN);
			trackListViewer.refresh();
		}
		else if (event.widget == removeButton) {
			List<FileDescriptorTrackItem> items = getSelectedItems();
			for (FileDescriptorTrackItem item : items) {
				item.audioDataChange = null;
			}
			trackListViewer.refresh();
		}
		else if (event.widget == resetButton) {
			for (FileDescriptorTrackItem item : this.model) {
				item.audioDataChange = item.originalAudioDataChange;
			}
			trackListViewer.refresh();
		}
		setPageComplete(true);
		getWizard().getContainer().updateButtons();
	}
	
	private void moveItems(List<FileDescriptorTrackItem> items, int state) {
		List<FileDescriptorTrackItem> selection = new ArrayList<FileDescriptorTrackItem>();
		Collections.sort(items);
		if (state == SWT.DOWN) {
			Collections.reverse(items);
		}
		for (FileDescriptorTrackItem item : items) {
			int index = this.model.indexOf(item);
			if (state == SWT.DOWN) {
				index++;
				if (index >= this.model.size()) {
					break;
				}
			}
			else if (state == SWT.UP) {
				index--;
				if (0 > index) {
					selection.add(item);
					break;
				}
			}
			FileDescriptorTrackItem otherItem = this.model.get(index);
			AudioDataChange tmpAudioDataChange = item.audioDataChange;
			item.audioDataChange = otherItem.audioDataChange;
			otherItem.audioDataChange = tmpAudioDataChange;
			selection.add(otherItem);
		}
		trackListViewer.setSelection(new StructuredSelection(selection));
	}

	private List<FileDescriptorTrackItem> getSelectedItems() {
		List<FileDescriptorTrackItem> items = new ArrayList<FileDescriptorTrackItem>();
		ISelection selection = trackListViewer.getSelection();
		IStructuredSelection structuredSelection = (IStructuredSelection) selection;
		for (Object obj : structuredSelection.toList()) {
			if (obj instanceof FileDescriptorTrackItem) {
				items.add((FileDescriptorTrackItem) obj);
			}
		}
		return items;
	}
	
	public Set<AudioDataChange> getCDDBTracks() {
		if (this.model == null) {
			// this page was skipped
			return null;
		}
		Set<AudioDataChange> changes = new HashSet<>();
		for (FileDescriptorTrackItem item : this.model) {
			if (item.fileDescriptor != null && item.audioDataChange != null) {
//				AudioFileDescriptor container = new AudioFileDescriptor(item.fileDescriptor, item.audioValues);
				changes.add(item.audioDataChange);
			}
		}
		return changes;
	}
	
	private static class AlbumListLabelProvider extends BaseLabelProvider implements ITableLabelProvider, ITableColorProvider {

		@Override
		public String getColumnText(Object element, int columnIndex) {
			if (element instanceof FileDescriptorTrackItem) {
				FileDescriptorTrackItem item = (FileDescriptorTrackItem) element;
				ViewerColumn column = COLUMNS.getByIndex(columnIndex);
				if (COLUMN_FILE.equals(column)) {
					if (item.fileDescriptor == null) {
						return null;
					}
//					return item.fileDescriptor.getAbsolutePath();
					return item.fileDescriptor.getFileName();
				}
				else if (item.audioDataChange == null) {
					return null;
				}
				else if (COLUMN_TRACK.equals(column)) {
//					return item.audioValues.get(AudioAttribute.TRACK);
					return item.audioDataChange.getTrack();
				}
				else if (COLUMN_TRACK_NO.equals(column)) {
//					return item.audioValues.get(AudioAttribute.TRACK_NO);
					return String.valueOf(item.audioDataChange.getTrackNo());
				}
				else if (COLUMN_ARTIST.equals(column)) {
//					return item.audioValues.get(AudioAttribute.ARTIST);
					return item.audioDataChange.getArtist();
				}
				else if (COLUMN_DISK.equals(column)) {
//					return item.audioValues.get(AudioAttribute.DISK);
					return item.audioDataChange.getDisk();
				}
			}
			return null;
		}

		@Override
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		@Override
		public Color getForeground(Object element, int columnIndex) {
			return null;
		}

		@Override
		public Color getBackground(Object element, int columnIndex) {
			if (columnIndex == 0) {
				return Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
			}
			return null;
		}
	}
	
	private class FileDescriptorTrackItem implements Comparable<FileDescriptorTrackItem> {
		private FileDescriptor fileDescriptor;
//		private Map<AudioAttribute, String> audioValues;
//		private Map<AudioAttribute, String> originalAudioValues;
		private AudioDataChange audioDataChange;
		private AudioDataChange originalAudioDataChange;
		
		@Override
		public int compareTo(FileDescriptorTrackItem o) {
			int result = 0;
			if (this.fileDescriptor != null && o.fileDescriptor != null) {
				result = new DefaultFileDescriptorComparator().compare(fileDescriptor, o.fileDescriptor);
			}
			else if (this.originalAudioDataChange != null && o.originalAudioDataChange != null) {
				String disk1 = this.originalAudioDataChange.getDisk();
				String disk2 = o.originalAudioDataChange.getDisk();
				if (disk1 != null && disk2 != null) {
					result = disk1.compareTo(disk2);
				}
				
				if (result == 0) {
					Integer no1 = this.originalAudioDataChange.getTrackNo();
					Integer no2 = o.originalAudioDataChange.getTrackNo();
					result = no1.compareTo(no2);
				}
			}
			return result;
		}
	}

}
