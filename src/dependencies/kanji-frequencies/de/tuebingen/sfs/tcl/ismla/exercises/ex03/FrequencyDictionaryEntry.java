package de.tuebingen.sfs.tcl.ismla.exercises.ex03;

public class FrequencyDictionaryEntry {
	public String lemma;
	public double logPercentage;
	public int rank;

	public FrequencyDictionaryEntry(String lemma, double logPercentage, int rank) {
		this.lemma = lemma;
		this.logPercentage = logPercentage;
		this.rank = rank;
	}
}
