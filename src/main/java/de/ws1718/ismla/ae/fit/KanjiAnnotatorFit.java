package de.ws1718.ismla.ae.fit;

import java.io.InputStream;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.cas.CASException;
import org.apache.uima.fit.component.JCasAnnotator_ImplBase;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;

import de.tuebingen.sfs.tcl.ismla.exercises.ex03.FrequencyDictionary;
import de.tuebingen.sfs.tcl.ismla.exercises.ex03.FrequencyDictionaryEntry;
import de.ws1718.ismla.types.Compound;

public class KanjiAnnotatorFit extends JCasAnnotator_ImplBase {

	public static final String PARAM_LANGUAGE = "language";
	@ConfigurationParameter(name = "PARAM_LANGUAGE", defaultValue = "cmn")
	private String language;

	public static final String PARAM_CHAR_FREQ_LIST = "path2charFreqList";
	@ConfigurationParameter(name = "PARAM_CHAR_FREQ_LIST", defaultValue = "opensubtitles-freq-chars-cmn.tsv")
	private String path2charFreqList;

	public static final String PARAM_TOKEN_FREQ_LIST = "path2tokenFreqList";
	@ConfigurationParameter(name = "PARAM_TOKEN_FREQ_LIST", defaultValue = "opensubtitles-freq-tokens-cmn.tsv")
	private String path2tokenFreqList;

	public static final String PARAM_RANK_THRESHOLD = "threshold";
	@ConfigurationParameter(name = "PARAM_RANK_THRESHOLD", defaultValue = "500")
	private int threshold;

	public static final String PARAM_GREEDY_COMPOUNT_SIZE = "compoundSize";
	@ConfigurationParameter(name = "PARAM_GREEDY_COMPOUNT_SIZE", defaultValue = "2")
	private int compoundSize;

	private FrequencyDictionary freqDict;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		// TODO Auto-generated method stub
		super.initialize(context);

		InputStream inChar = FrequencyDictionary.class.getResourceAsStream(path2charFreqList);
		InputStream inToken = FrequencyDictionary.class.getResourceAsStream(path2tokenFreqList);

		freqDict = new FrequencyDictionary(inToken, inChar);
	}

	@Override
	public void process(JCas arg0) throws AnalysisEngineProcessException {
		// TODO Auto-generated method stub
		try {

			JCas jcas = arg0.getView(language);
			String text = jcas.getDocumentText();

			for (int i = 0; i < text.length(); i++) {
	
				int end = i + compoundSize;
				if (end >= text.length())
					break;
				
				String compound = text.substring(i, end);

				if (!compound.trim().equals("")) {
					FrequencyDictionaryEntry entry = null;
					//depending on whether it is a singleton or a compound
					if(compoundSize == 1){
						entry = freqDict.characterLookup(compound);
					}else if(compoundSize >= 2){
						entry = freqDict.compoundLookup(compound);
					}
					
					if (!(entry == null) && entry.rank >= threshold) {
						Compound c = new Compound(jcas);
						c.setBegin(i);
						c.setEnd(end);
						c.setRank(entry.rank);
						c.addToIndexes(jcas);
						
						//without overlap ???
						i += compoundSize-1;
					}
				}
			}

		} catch (CASException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
