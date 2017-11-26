package de.tuebingen.sfs.tcl.ismla.exercises.ex03;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.TreeMap;

public class FrequencyDictionary {
	private Map<String, FrequencyDictionaryEntry> compoundFrequencyMap;
	private Map<String, FrequencyDictionaryEntry> charFrequencyMap;

	public FrequencyDictionary(InputStream tokenFrequencyFileAsStream, InputStream charFrequencyFileAsStream) {
		compoundFrequencyMap = new TreeMap<String, FrequencyDictionaryEntry>();
		charFrequencyMap = new TreeMap<String, FrequencyDictionaryEntry>();

		BufferedReader r = new BufferedReader(new InputStreamReader(tokenFrequencyFileAsStream));
		String line = "";
		int rank = 1;
		int compoundFrequencySum = 0;
		String[] tokens = null;
		try {
			while ((line = r.readLine()) != null) {
				tokens = line.split("\t");
				if (tokens.length < 2)
					continue;
				String token = tokens[0];
				if (token.length() >= 2) {
					int compoundFrequency = Integer.parseInt(tokens[1]);
					// initially build the token entries with raw counts in the logPercentage field
					FrequencyDictionaryEntry compoundEntry = new FrequencyDictionaryEntry(token, compoundFrequency,
							rank);
					compoundFrequencyMap.put(token, compoundEntry);
					compoundFrequencySum += compoundFrequency;
					rank++;
				}
			}
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// sum is known, convert the raw counts to actual log percentages
		for (FrequencyDictionaryEntry tokenEntry : compoundFrequencyMap.values()) {
			tokenEntry.logPercentage = Math.log(tokenEntry.logPercentage / compoundFrequencySum);
		}

		r = new BufferedReader(new InputStreamReader(charFrequencyFileAsStream));
		line = "";
		rank = 1;
		int charFrequencySum = 0;
		tokens = null;
		try {
			while ((line = r.readLine()) != null) {
				tokens = line.split("\t");
				if (tokens.length < 2)
					continue;
				String character = tokens[0];
				int charFrequency = Integer.parseInt(tokens[1]);
				// initially build the char entries with raw counts in the logPercentage field
				FrequencyDictionaryEntry charEntry = new FrequencyDictionaryEntry(character, charFrequency, rank);
				charFrequencyMap.put(character, charEntry);
				charFrequencySum += charFrequency;
				rank++;
			}
			r.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// sum is known, convert the raw counts to actual log percentages
		for (FrequencyDictionaryEntry charEntry : charFrequencyMap.values()) {
			charEntry.logPercentage = Math.log(charEntry.logPercentage / charFrequencySum);
		}
	}

	public FrequencyDictionaryEntry characterLookup(String character) {
		return charFrequencyMap.get(character);
	}

	public FrequencyDictionaryEntry compoundLookup(String token) {
		return compoundFrequencyMap.get(token);
	}
}
