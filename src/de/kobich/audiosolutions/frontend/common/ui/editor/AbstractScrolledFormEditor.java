package de.kobich.audiosolutions.frontend.common.ui.editor;

import java.io.File;
import java.io.InputStream;
import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.part.EditorPart;

import de.kobich.audiosolutions.core.service.info.FileInfo;
import de.kobich.commons.ui.jface.JFaceUtils;

public abstract class AbstractScrolledFormEditor extends EditorPart {
	
	protected Composite createTableWrapSection(FormToolkit toolkit, Composite parent, String sectionText, int numColumns, int style) {
		Section generalSection = toolkit.createSection(parent, style);
		generalSection.setText(sectionText);
		generalSection.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		Composite generalComposite = toolkit.createComposite(generalSection);
		generalComposite.setLayout(JFaceUtils.createViewGridLayout(numColumns, false, JFaceUtils.MARGIN_WIDTH));
		generalComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		generalSection.setClient(generalComposite);
		return generalComposite;
	}
	
	protected StyledText createStyledText(Composite parent, boolean editable) {
		StyledText st = new StyledText(parent, SWT.NONE);
		st.setEditable(editable);
		if (!editable) {
			st.setCaret(null);
		}
		return st;
	}
	
	protected Label createLogo(FormToolkit toolkit, Composite parent, Point size) {
		Label logoLabel = toolkit.createLabel(parent, "", SWT.CENTER);
		GridData gd = new GridData();
		gd.widthHint = size.x;
		gd.heightHint = size.y;
		logoLabel.setLayoutData(gd);
		return logoLabel;
	}
	
	protected Optional<ImageData> getImageData(FileInfo fileInfo) {
		InputStream is = fileInfo.getArtwork().orElse(null);
		File file = fileInfo.getArtworkFile().orElse(null);
		ImageData imageData = null;
		if (is != null) {
			imageData = new ImageData(is);
		}
		else if (file != null) {
			imageData = new ImageData(file.getAbsolutePath());
		}
		return Optional.ofNullable(imageData);
	}
	
}
