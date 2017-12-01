package de.ws1718.ismla.main.fit;

public class Word implements Comparable<Word>{
	
	private String compound;
	private int frequency;
	
	public Word(String compound, int frequency) {
		super();
		this.compound = compound;
		this.frequency = frequency;
	}

	public String getCompound() {
		return compound;
	}

	public void setCompound(String compound) {
		this.compound = compound;
	}

	public int getFrequency() {
		return frequency;
	}

	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}

	public int compareTo(Word o) {
		// TODO Auto-generated method stub
		if(this.frequency == o.getFrequency()) return 0;
		return this.getFrequency() > o.getFrequency() ? 1 : -1;
	}
	
	

}
