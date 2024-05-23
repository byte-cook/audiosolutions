package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;

import com.google.common.base.Objects;

import de.kobich.audiosolutions.core.AudioSolutions;
import de.kobich.audiosolutions.core.service.info.AudioInfoService;
import de.kobich.audiosolutions.core.service.info.FileInfo;
import de.kobich.audiosolutions.frontend.Activator;
import de.kobich.audiosolutions.frontend.common.preferences.GeneralPreferencePage;
import de.kobich.audiosolutions.frontend.common.proxy.RCPProxyProvider;
import de.kobich.audiosolutions.frontend.common.util.FileDescriptorSelection;
import de.kobich.commons.type.Wrapper;
import de.kobich.commons.ui.jface.JFaceExec;
import de.kobich.component.file.DefaultFileDescriptorComparator;
import de.kobich.component.file.FileDescriptor;

public class LogoImagePostSelectionListener implements ISelectionChangedListener {
	private final File coverArtRootDir; 
	private final ICollectionEditor editor;
	private final CollectionEditorViewerFilter filter;
	private FileDescriptor currentFile;
	
	public LogoImagePostSelectionListener(ICollectionEditor editor, CollectionEditorViewerFilter filter) {
		this.coverArtRootDir = AudioSolutions.getCoverArtRootDir();
		this.editor = editor;
		this.filter = filter;
	}

	@Override
	public void selectionChanged(SelectionChangedEvent event) {
		FileDescriptorSelection selection = new FileDescriptorSelection(event.getSelection(), filter);
		List<FileDescriptor> files = new ArrayList<>(selection.getFileDescriptors());
		if (files.isEmpty()) {
			editor.showDefaultLogo();
			return;
		}
		Collections.sort(files, new DefaultFileDescriptorComparator());
		if (Objects.equal(currentFile, files.get(0))) {
			return;
		}
		currentFile = files.get(0);
		
		JFaceExec.cancelJobs(this);
		
		final Wrapper<FileDescriptor> FILE = Wrapper.of(this.currentFile);
		final Wrapper<FileInfo> FILE_INFO = Wrapper.empty();
		JFaceExec.builder(editor.getSite().getShell(), "Loading File Logo")
//			.ui(ctx -> editor.showDefaultLogo())
			.worker(ctx -> {
				boolean loadCoversFromInternet = Activator.getDefault().getPreferenceStore().getBoolean(GeneralPreferencePage.LOAD_COVERS_FROM_INTERNET);
				final AudioInfoService audioInfoService = AudioSolutions.getService(AudioInfoService.class);
				FILE_INFO.set(audioInfoService.getFileInfo(List.of(FILE.get()), loadCoversFromInternet, coverArtRootDir, new RCPProxyProvider(), null).orElse(null));
			})
			.ui(ctx -> {
				if (FILE_INFO.isPresent()) {
					editor.showLogo(FILE_INFO.get());
				}
				else {
					editor.showDefaultLogo();
				}
			})
			.exceptionally((ctx, e) -> {
				editor.showDefaultLogo();
				ctx.setCanceled(true);
			})
			.runBackgroundJob(0, false, true, null, this);
	}

}
