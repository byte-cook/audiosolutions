package de.kobich.audiosolutions.frontend.audio.action;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.service.convert.FfmpegConversionOptions;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.memento.IMementoItem;

public class ConvertFilesFfmpegOptionPanel implements IConvertFilesOptionPanel {
	private static final String STATE_QUALITY = "quality";
	private int quality;
	private Scale qualityScale;
	private Text qualityLabel;

	@Override
	public void createOptionComposite(Composite parent) {
		// mode composite
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout modeLayout = JFaceUtils.createDialogGridLayout(2, false);
		composite.setLayout(modeLayout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		composite.setFont(JFaceResources.getDialogFont());
		
		// quality
		Label vbrBitrateLabel = new Label(composite, SWT.NONE);
		vbrBitrateLabel.setText("Quality:");
		GridData vbrBitrateLayoutData = new GridData();
		vbrBitrateLabel.setLayoutData(vbrBitrateLayoutData);
		qualityLabel = new Text(composite, SWT.NONE);
		qualityLabel.setEditable(false);
		qualityLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		qualityScale = new Scale(composite, SWT.HORIZONTAL);
		GridData vbrQualityScaleLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		vbrQualityScaleLayoutData.horizontalSpan = 2;
		qualityScale.setLayoutData(vbrQualityScaleLayoutData);
		qualityScale.setMinimum(1);
		qualityScale.setMaximum(10);
		qualityScale.setIncrement(1);
		qualityScale.setPageIncrement(5);
		qualityScale.setDragDetect(true);
		qualityScale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int bitrate = qualityScale.getSelection();
				qualityLabel.setText("" + bitrate);
		    }
	    });
		
		Label vbrLabel = new Label(composite, SWT.NONE);
		GridData vbrLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		vbrLayoutData.horizontalSpan = 2;
		vbrLabel.setLayoutData(vbrLayoutData);
		vbrLabel.setForeground(JFaceUtils.getInfoTextForegroundColor());
		vbrLabel.setText(String.format("Choose the quality of the output file. Higher value means higher quality. \nRecommended value is %s. \nNot all encoders support this setting.", FfmpegConversionOptions.DEFAULT_QUALITY));
		
	}

	@Override
	public void okPressed() {
		quality = qualityScale.getSelection();
	}

	/**
	 * @return the quality
	 */
	public int getQuality() {
		return quality;
	}
	
	@Override
	public void restoreState(IMementoItem mementoItem) {
		int quality = mementoItem.getInteger(STATE_QUALITY, FfmpegConversionOptions.DEFAULT_QUALITY);
		qualityScale.setSelection(quality);
		qualityLabel.setText("" + quality);
	}
	
	@Override
	public void saveState(IMementoItem mementoItem) {
		mementoItem.putInteger(STATE_QUALITY, qualityScale.getSelection());
	}
}
