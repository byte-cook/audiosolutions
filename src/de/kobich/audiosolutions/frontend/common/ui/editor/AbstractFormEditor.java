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
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.part.EditorPart;

import de.kobich.audiosolutions.core.service.info.FileInfo;
import de.kobich.commons.ui.jface.JFaceUtils;

public abstract class AbstractFormEditor extends EditorPart {
	private FormToolkit toolkit;
	private Form form;

	public Form createForm(Composite parent) {
		this.toolkit = new FormToolkit(parent.getDisplay());
		this.form = toolkit.createForm(parent);
		Composite body = form.getBody();
		body.setLayout(JFaceUtils.createViewGridLayout(1, true));
		body.setLayoutData(new GridData(GridData.FILL_BOTH));
		return form;
	}
	
	public Composite createInformationComposite(Composite parent) {
		Composite composite = toolkit.createComposite(parent, SWT.NONE);
		composite.setLayout(JFaceUtils.createViewGridLayout(2, false));
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		return composite;
	}

	public Composite createContentComposite(Composite parent) {
		Composite composite = toolkit.createComposite(parent, SWT.WRAP);
		composite.setLayout(JFaceUtils.createViewGridLayout(1, true));
		composite.setLayoutData(new GridData(GridData.FILL_BOTH));
		return composite;
	}
	
	protected Composite createSection(Composite parent, String sectionText, int numColumns, int style) {
		Section generalSection = toolkit.createSection(parent, style);
		generalSection.setText(sectionText);
		generalSection.setLayoutData(new GridData(GridData.FILL_BOTH));
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
	
	protected Label createLogo(Composite parent, Point size) {
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
	
	public FormToolkit getFormToolkit() {
		return toolkit;
	}
	
	@Override
	public void dispose() {
		this.form.dispose();
		this.toolkit.dispose();
		super.dispose();
	}
}
