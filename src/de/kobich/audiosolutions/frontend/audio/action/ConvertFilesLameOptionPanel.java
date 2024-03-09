package de.kobich.audiosolutions.frontend.audio.action;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;

import de.kobich.audiosolutions.core.service.convert.LameMP3ConversionOptions;
import de.kobich.audiosolutions.core.service.convert.LameMP3ConversionOptions.AudioEncodingMode;
import de.kobich.commons.ui.jface.JFaceUtils;
import de.kobich.commons.ui.memento.IMementoItem;

public class ConvertFilesLameOptionPanel implements IConvertFilesOptionPanel {
	private static final String NO_LIMIT = "<no-limit>";
	private static final String STATE_MODE = "mode";
	private static final String STATE_MONO = "mono";
	private static final String STATE_CBR = "cbr";
	private static final String STATE_ABR = "abr";
	private static final String STATE_ABR_MAX_BITRATE = "abrMaxBitrate";
	private static final String STATE_VBR = "vbr";
	private static final String STATE_VBR_MIN_BITRATE = "vbrMinBitrate";
	private static final String STATE_VBR_MAX_BITRATE = "vbrMaxBitrate";
	private boolean mono;
	private AudioEncodingMode encodingMode;
	private int cbrBitrate;
	private int abrBitrate;
	private int abrMaxBitrate;
	private int vbrQuality;
	private int vbrMinBitrate;
	private int vbrMaxBitrate;
	private Button monoButton;
	private Composite switchComposite;
	private StackLayout switchLayout;
	private Button cbrButton;
	private Button abrButton;
	private Button vbrButton;
	private Composite cbrComposite;
	private Composite abrComposite;
	private Composite vbrComposite;
	private Scale cbrBitrateScale;
	private Text cbrBitrateValueLabel;
	private Scale abrBitrateScale;
	private Text abrBitrateValueLabel;
	private Combo abrMaxBitrateCombo;
	private Scale vbrQualityScale;
	private Text vbrQualityValueLabel;
	private Combo vbrMinBitrateCombo;
	private Combo vbrMaxBitrateCombo;
	private Color infoColor;

	@Override
	public void createOptionComposite(Composite parent) {
		// mode composite
		Composite modeComposite = new Composite(parent, SWT.NONE);
		GridLayout modeLayout = JFaceUtils.createDialogGridLayout(3, false);
		modeComposite.setLayout(modeLayout);
		modeComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		modeComposite.setFont(JFaceResources.getDialogFont());

		// Mono
		monoButton = new Button(modeComposite, SWT.CHECK);
		monoButton.setText("Mono Encoding");
		GridData monoLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		monoLayoutData.horizontalSpan = 3;
		monoButton.setLayoutData(monoLayoutData);

		// encoding modes
		cbrButton = new Button(modeComposite, SWT.RADIO);
		cbrButton.setText("Constant Bitrate");
		cbrButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchLayout.topControl = cbrComposite;
				switchComposite.layout();
			}
		});
		abrButton = new Button(modeComposite, SWT.RADIO);
		abrButton.setText("Average Bitrate");
		abrButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchLayout.topControl = abrComposite;
				switchComposite.layout();
			}
		});
		vbrButton = new Button(modeComposite, SWT.RADIO);
		vbrButton.setText("Variable Bitrate (recommended)");
		vbrButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchLayout.topControl = vbrComposite;
				switchComposite.layout();
			}
		});

		switchComposite = new Composite(parent, SWT.NONE);
		switchLayout = new StackLayout();
		switchComposite.setLayout(switchLayout);
		switchComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		infoColor = JFaceUtils.getInfoTextForegroundColor();

		// === CBR
		cbrComposite = new Composite(switchComposite, SWT.NONE);
		GridLayout cbrLayout = JFaceUtils.createDialogGridLayout(2, false);
		cbrComposite.setLayout(cbrLayout);
		cbrComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label cbrLabel = new Label(cbrComposite, SWT.NONE);
		GridData cbrLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		cbrLayoutData.horizontalSpan = 2;
		cbrLabel.setLayoutData(cbrLayoutData);
		cbrLabel.setForeground(infoColor);
		cbrLabel.setText("The bitrate will be the same for the whole file. The musical passage beeing a difficult one \n"
				+ "to encode or an easy one, the encoder will use the same bitrate, so the quality of your mp3 is \n"
				+ "variable. Complex parts will be of a lower quality than the easiest ones.");
		// cbrBitrate
		Label cbrBitrateLabel = new Label(cbrComposite, SWT.NONE);
		cbrBitrateLabel.setText("Bitrate:");
		cbrBitrateLabel.setLayoutData(new GridData());
		cbrBitrateValueLabel = new Text(cbrComposite, SWT.NONE);
		cbrBitrateValueLabel.setEditable(false);
		cbrBitrateValueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		cbrBitrateScale = new Scale(cbrComposite, SWT.HORIZONTAL);
		GridData cbrBitrateScaleLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		cbrBitrateScaleLayoutData.horizontalSpan = 2;
		cbrBitrateScale.setLayoutData(cbrBitrateScaleLayoutData);
		cbrBitrateScale.setMinimum(8);
		cbrBitrateScale.setMaximum(320);
		cbrBitrateScale.setIncrement(8);
		cbrBitrateScale.setPageIncrement(8);
		cbrBitrateScale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int bitrate = cbrBitrateScale.getSelection();
				cbrBitrateValueLabel.setText("" + bitrate);
		    }
	    });
		// === ABR
		abrComposite = new Composite(switchComposite, SWT.NONE);
		GridLayout abrLayout = new GridLayout();
		abrLayout.numColumns = 2;
		abrComposite.setLayout(abrLayout);
		abrComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label abrLabel = new Label(abrComposite, SWT.NONE);
		GridData abrLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		abrLayoutData.horizontalSpan = 2;
		abrLabel.setLayoutData(abrLayoutData);
		abrLabel.setForeground(infoColor);
		abrLabel.setText("The encoder will try to constantly maintain an average bitrate while using higher bitrates \n"
				+ "for the parts of your music that need more bits. The result will be of higher quality than CBR encoding \n"
				+ "while the average file size will remain predictible, so this mode is highly recommended over CBR.");
		// abrBitrate
		Label abrBitrateLabel = new Label(abrComposite, SWT.NONE);
		abrBitrateLabel.setText("Bitrate:");
		GridData abrBitrateLayoutData = new GridData();
		abrBitrateLabel.setLayoutData(abrBitrateLayoutData);
		abrBitrateValueLabel = new Text(abrComposite, SWT.NONE);
		abrBitrateValueLabel.setEditable(false);
		abrBitrateValueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		abrBitrateScale = new Scale(abrComposite, SWT.HORIZONTAL);
		GridData abrBitrateScaleLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		abrBitrateScaleLayoutData.horizontalSpan = 2;
		abrBitrateScale.setLayoutData(abrBitrateScaleLayoutData);
		abrBitrateScale.setMinimum(8);
		abrBitrateScale.setMaximum(320);
		abrBitrateScale.setIncrement(8);
		abrBitrateScale.setPageIncrement(8);
		abrBitrateScale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int bitrate = abrBitrateScale.getSelection();
				abrBitrateValueLabel.setText("" + bitrate);
		    }
	    });
		// max bitrate
		Label abrMaxBitrateLabel = new Label(abrComposite, SWT.NONE);
		abrMaxBitrateLabel.setText("Max. Bitrate:");
		GridData abrMaxBitrateLayoutData = new GridData();
		abrMaxBitrateLabel.setLayoutData(abrMaxBitrateLayoutData);
		abrMaxBitrateCombo = new Combo(abrComposite, SWT.BORDER | SWT.READ_ONLY);
		GridData abrMaxBitrateComboLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		abrMaxBitrateCombo.setLayoutData(abrMaxBitrateComboLayoutData);
		String[] maxBitrates = { NO_LIMIT, "144", "160", "192", "224", "256", "320" };
		for (String b : maxBitrates) {
			abrMaxBitrateCombo.add(b);
		}
		abrMaxBitrateCombo.setText(NO_LIMIT);

		// == VBR
		vbrComposite = new Composite(switchComposite, SWT.NONE);
		GridLayout vbrLayout = new GridLayout();
		vbrLayout.numColumns = 2;
		vbrComposite.setLayout(vbrLayout);
		vbrComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Label vbrLabel = new Label(vbrComposite, SWT.NONE);
		GridData vbrLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		vbrLayoutData.horizontalSpan = 2;
		vbrLabel.setLayoutData(vbrLayoutData);
		vbrLabel.setForeground(JFaceUtils.getInfoTextForegroundColor());
		vbrLabel.setText(String.format("The encoder tries to maintain the given quality in the whole file by choosing the optimal number \n"
				+ "of bits to spend for each part of your music. The main advantage is that you are able to specify the \n"
				+ "quality level that you want to reach, but the inconvenient is that the final file size is totally unpredictible.\n"
				+ "Recommended value is %s.", 9));
		// vbrQuality
		Label vbrBitrateLabel = new Label(vbrComposite, SWT.NONE);
		vbrBitrateLabel.setText("Quality:");
		GridData vbrBitrateLayoutData = new GridData();
		vbrBitrateLabel.setLayoutData(vbrBitrateLayoutData);
		vbrQualityValueLabel = new Text(vbrComposite, SWT.NONE);
		vbrQualityValueLabel.setEditable(false);
		vbrQualityValueLabel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		vbrQualityScale = new Scale(vbrComposite, SWT.HORIZONTAL);
		GridData vbrQualityScaleLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		vbrQualityScaleLayoutData.horizontalSpan = 2;
		vbrQualityScale.setLayoutData(vbrQualityScaleLayoutData);
		vbrQualityScale.setMinimum(1);
		vbrQualityScale.setMaximum(10);
		vbrQualityScale.setIncrement(1);
		vbrQualityScale.setPageIncrement(5);
		vbrQualityScale.setDragDetect(true);
		vbrQualityScale.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				int bitrate = vbrQualityScale.getSelection();
				vbrQualityValueLabel.setText("" + bitrate);
		    }
	    });
		// min bitrate
		Label vbrMinBitrateLabel = new Label(vbrComposite, SWT.NONE);
		vbrMinBitrateLabel.setText("Min. Bitrate:");
		GridData vbrMinBitrateLayoutData = new GridData();
		vbrMinBitrateLabel.setLayoutData(vbrMinBitrateLayoutData);
		vbrMinBitrateCombo = new Combo(vbrComposite, SWT.BORDER | SWT.READ_ONLY);
		GridData vbrMinBitrateComboLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		vbrMinBitrateCombo.setLayoutData(vbrMinBitrateComboLayoutData);
		String[] minBitrates = { NO_LIMIT, "8", "16", "24", "32", "40", "48", "56", "64", "80", "96", "128" };
		for (String b : minBitrates) {
			vbrMinBitrateCombo.add(b);
		}
		vbrMinBitrateCombo.setText(NO_LIMIT);
		// max bitrate
		Label vbrMaxBitrateLabel = new Label(vbrComposite, SWT.NONE);
		vbrMaxBitrateLabel.setText("Max. Bitrate:");
		GridData vbrMaxBitrateLayoutData = new GridData();
		vbrMaxBitrateLabel.setLayoutData(vbrMaxBitrateLayoutData);
		vbrMaxBitrateCombo = new Combo(vbrComposite, SWT.BORDER | SWT.READ_ONLY);
		GridData vbrMaxBitrateComboLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		vbrMaxBitrateCombo.setLayoutData(vbrMaxBitrateComboLayoutData);
		for (String b : maxBitrates) {
			vbrMaxBitrateCombo.add(b);
		}
		vbrMaxBitrateCombo.setText(NO_LIMIT);

		switchComposite.layout();
	}

	@Override
	public void okPressed() {
		// mono
		mono = monoButton.getSelection();

		// encoding mode
		if (switchLayout.topControl.equals(cbrComposite)) {
			encodingMode = AudioEncodingMode.CONSTANT_BITRATE;
		}
		else if (switchLayout.topControl.equals(abrComposite)) {
			encodingMode = AudioEncodingMode.AVERAGE_BITRATE;
		}
		else if (switchLayout.topControl.equals(vbrComposite)) {
			encodingMode = AudioEncodingMode.VARIABLE_BITRATE;
		}

		// cbr
		cbrBitrate = cbrBitrateScale.getSelection();

		// abr
		abrBitrate = abrBitrateScale.getSelection();
		if (NO_LIMIT.equals(abrMaxBitrateCombo.getText())) {
			abrMaxBitrate = LameMP3ConversionOptions.NOT_SPECIFIED;
		}
		else {
			abrMaxBitrate = Integer.parseInt(abrMaxBitrateCombo.getText());
		}

		// vbr
		vbrQuality = vbrQualityScale.getSelection();
		if (NO_LIMIT.equals(vbrMinBitrateCombo.getText())) {
			vbrMinBitrate = LameMP3ConversionOptions.NOT_SPECIFIED;
		}
		else {
			vbrMinBitrate = Integer.parseInt(vbrMinBitrateCombo.getText());
		}
		if (NO_LIMIT.equals(vbrMaxBitrateCombo.getText())) {
			vbrMaxBitrate = LameMP3ConversionOptions.NOT_SPECIFIED;
		}
		else {
			vbrMaxBitrate = Integer.parseInt(vbrMaxBitrateCombo.getText());
		}
	}

	/**
	 * @return the mono
	 */
	public boolean isMono() {
		return mono;
	}
	/**
	 * @return the encodingMode
	 */
	public AudioEncodingMode getEncodingMode() {
		return encodingMode;
	}

	/**
	 * @return the cbrBitrate
	 */
	public int getCbrBitrate() {
		return cbrBitrate;
	}

	/**
	 * @return the abrBitrate
	 */
	public int getAbrBitrate() {
		return abrBitrate;
	}

	/**
	 * @return the abrMaxBitrate
	 */
	public int getAbrMaxBitrate() {
		return abrMaxBitrate;
	}

	/**
	 * @return the vbrQuality
	 */
	public int getVbrQuality() {
		return vbrQuality;
	}

	/**
	 * @return the vbrMinBitrate
	 */
	public int getVbrMinBitrate() {
		return vbrMinBitrate;
	}

	/**
	 * @return the vbrMaxBitrate
	 */
	public int getVbrMaxBitrate() {
		return vbrMaxBitrate;
	}
	
	@Override
	public void restoreState(IMementoItem mementoItem) {
		String mono = mementoItem.getString(STATE_MONO, Boolean.FALSE.toString());
		monoButton.setSelection(Boolean.parseBoolean(mono));
		
		String mode = mementoItem.getString(STATE_MODE, "vbr");
		if ("cbr".equals(mode)) {
			cbrButton.setSelection(true);
			switchLayout.topControl = cbrComposite;
		}
		else if ("abr".equals(mode)) {
			abrButton.setSelection(true);
			switchLayout.topControl = abrComposite;
		}
		else if ("vbr".equals(mode)) {
			vbrButton.setSelection(true);
			switchLayout.topControl = vbrComposite;
		}
		
		int cbrBitrate = mementoItem.getInteger(STATE_CBR, 192);
		cbrBitrateScale.setSelection(cbrBitrate);
		cbrBitrateValueLabel.setText("" + cbrBitrate);
		int abrBitrate = mementoItem.getInteger(STATE_ABR, 192);
		abrBitrateScale.setSelection(abrBitrate);
		abrBitrateValueLabel.setText("" + abrBitrate);
		String abrMaxBitrate = mementoItem.getString(STATE_ABR_MAX_BITRATE, NO_LIMIT);
		abrMaxBitrateCombo.setText(abrMaxBitrate);
		int vbrQuality = mementoItem.getInteger(STATE_VBR, LameMP3ConversionOptions.DEFAULT_VBR_QUALITY);
		vbrQualityScale.setSelection(vbrQuality);
		vbrQualityValueLabel.setText("" + vbrQuality);
		String vbrMinBitrate = mementoItem.getString(STATE_VBR_MIN_BITRATE, NO_LIMIT);
		vbrMinBitrateCombo.setText(vbrMinBitrate);
		String vbrMaxBitrate = mementoItem.getString(STATE_VBR_MAX_BITRATE, NO_LIMIT);
		vbrMaxBitrateCombo.setText(vbrMaxBitrate);
	}
	
	@Override
	public void saveState(IMementoItem mementoItem) {
		mementoItem.putString(STATE_MONO, "" + monoButton.getSelection());
		if (switchLayout.topControl.equals(cbrComposite)) {
			mementoItem.putInteger(STATE_CBR, cbrBitrateScale.getSelection());
			mementoItem.putString(STATE_MODE, "cbr");
		}
		else if (switchLayout.topControl.equals(abrComposite)) {
			mementoItem.putInteger(STATE_ABR, abrBitrateScale.getSelection());
			mementoItem.putString(STATE_ABR_MAX_BITRATE, abrMaxBitrateCombo.getText());
			mementoItem.putString(STATE_MODE, "abr");
		}
		else if (switchLayout.topControl.equals(vbrComposite)) {
			mementoItem.putInteger(STATE_VBR, vbrQualityScale.getSelection());
			mementoItem.putString(STATE_VBR_MIN_BITRATE, vbrMinBitrateCombo.getText());
			mementoItem.putString(STATE_VBR_MAX_BITRATE, vbrMaxBitrateCombo.getText());
			mementoItem.putString(STATE_MODE, "vbr");
		}
	}
}
