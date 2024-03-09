package de.kobich.audiosolutions.frontend.audio.view.play;

public enum AudioPlayItemState {
	WAITING("Waiting"), PLAYING("Playing"), PLAYED("Played"), STOPPED("Stopped"), PAUSED("Paused"), SKIPPING("Skipping");
	
	private final String label;
	
	private AudioPlayItemState(String label) {
		this.label = label;
	}

	/**
	 * @return the label
	 */
	public String getLabel() {
		return label;
	}
}
