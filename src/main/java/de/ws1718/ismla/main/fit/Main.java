package de.ws1718.ismla.main.fit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.uima.UIMAException;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.cas.text.Language;
import org.apache.uima.collection.CollectionReaderDescription;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.CollectionReaderFactory;
import org.apache.uima.fit.factory.TypeSystemDescriptionFactory;
import org.apache.uima.fit.pipeline.SimplePipeline;
import org.apache.uima.fit.util.JCasUtil;
import org.apache.uima.jcas.JCas;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.TypeSystemDescription;

import de.tuebingen.sfs.tcl.ismla.exercises.ex03.FrequencyDictionary;
import de.tuebingen.sfs.tcl.ismla.exercises.ex03.ParallelSubtitlesReader;
import de.ws1718.ismla.ae.fit.KanjiAnnotatorFit;
import de.ws1718.ismla.types.Compound;

public class Main {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		final String CHINESE = "cmn";
		final String JAPANESE = "jpn";
		
		String[] languages = {CHINESE, JAPANESE};
		
		runPipeline(languages);
	}
	
	
	/**
	 * configurable pipeline
	 * 
	 * @param languages
	 */
	public static void runPipeline(String[] languages){
		
		HashMap<String, ArrayList<RanksComparator>> movies = new HashMap<String, ArrayList<RanksComparator>>();
		
		try {
			
			TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription(new File((String)"src/resources/typeSystemDescriptor").getAbsolutePath());
			
			CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
					ParallelSubtitlesReader.class, tsd, ParallelSubtitlesReader.DATA_DIRECTORY_PARAM, "src/dependencies/subtitles-collection/data");
			
			//different instances (cmn, jpn)
			for(String l : languages){
				
				System.out.println(l);
				
				//chinese annotator instance 
				AnalysisEngineDescription kanji = AnalysisEngineFactory.createEngineDescription(KanjiAnnotatorFit.class, tsd, KanjiAnnotatorFit.PARAM_RANK_THRESHOLD, 5,
						KanjiAnnotatorFit.PARAM_CHAR_FREQ_LIST, "opensubtitles-freq-chars-" + l + ".tsv",
						KanjiAnnotatorFit.PARAM_TOKEN_FREQ_LIST, "opensubtitles-freq-tokens-" + l + ".tsv",
						KanjiAnnotatorFit.PARAM_LANGUAGE, l,
						KanjiAnnotatorFit.PARAM_GREEDY_COMPOUNT_SIZE, 2);
				
				Iterator<JCas> iterCMN = SimplePipeline.iteratePipeline(reader, kanji).iterator();
				
				int i = 0;
				Language language = new Language(l);
				
				//iterate over jcas'
				while(iterCMN.hasNext()){
					//movie seperation
					String movie = "Movie: " + i;
					
					//check wheter there is a data for this movie
					ArrayList<RanksComparator> ranks;
					if(movies.keySet().contains(movie)){
						ranks = movies.get(movie);
					}else{
						ranks = new ArrayList<Main.RanksComparator>();
					}
					
					
					//get the jcas view
					JCas jcas = iterCMN.next().getView(l);
					
					//compute ranks
					ranks = getRanks(ranks, jcas, language);
					
					movies.put(movie, ranks);
					
					//increment movie number
					i++;
				}
			}
			
			for(String movie : movies.keySet()){
				System.out.println(movie);
				ArrayList<RanksComparator> r = movies.get(movie);
				for(RanksComparator rc : r){
					System.out.println(rc);
					break;
				}
//				break;
			}
			

			
			
			
			
			
			
			
			
					
//			//japanese annotator instance
//			AnalysisEngineDescription kanjiJPN = AnalysisEngineFactory.createEngineDescription(KanjiAnnotatorFit.class, tsd, KanjiAnnotatorFit.PARAM_RANK_THRESHOLD, 5,
//					KanjiAnnotatorFit.PARAM_CHAR_FREQ_LIST, "opensubtitles-freq-chars-jpn.tsv",
//					KanjiAnnotatorFit.PARAM_TOKEN_FREQ_LIST, "opensubtitles-freq-tokens-jpn.tsv",
//					KanjiAnnotatorFit.PARAM_LANGUAGE, "jpn",
//					KanjiAnnotatorFit.PARAM_GREEDY_COMPOUNT_SIZE, 2);
//			
//			Iterator<JCas> iterJPN = SimplePipeline.iteratePipeline(reader, kanjiJPN).iterator();
//			
//			int j = 0;
//			Language jpn = new Language("jpn");
//			
//			while(iterJPN.hasNext()){
//				
//				//movie seperation
//				String movie = "Movie: " + j;
//				
//				//check wheter there is a data for this movie
//				ArrayList<RanksComparator> ranks;
//				if(movies.keySet().contains(movie)){
//					ranks = movies.get(movie);
//				}else{
//					ranks = new ArrayList<Main.RanksComparator>();
//				}
//				
//				
//				JCas jcas = iterJPN.next().getView("jpn");
//				
//				//compute ranks
//				ranks = getRanks(ranks, jcas, jpn);
//				
//				movies.put(movie, ranks);
//				
//				//increment movie number
//				j++;
//			}
//			
//			for(String movie : movies.keySet()){
//				System.out.println(movie);
//				ArrayList<RanksComparator> r = movies.get(movie);
//				for(RanksComparator rc : r){
//					System.out.println(rc);
//					break;
//				}
////				break;
//			}
			
			
		} catch (ResourceInitializationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UIMAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
		}
				
	}
	
	
	/**
	 * helper for computing ranks
	 * @param ranks
	 * @param jcas
	 * @return
	 */
	public static ArrayList<RanksComparator> getRanks(ArrayList<RanksComparator> ranks, JCas jcas, Language l){
		
		//iterate over annotations
		for(Compound compound : JCasUtil.select(jcas, Compound.class)){
			
//			System.out.print(compound.getCoveredText());
//			System.out.println(compound);
			
			int rank = compound.getRank();
			String token = compound.getCoveredText();
			
			boolean containsCompound = false;
			for(RanksComparator rc : ranks){
				if(rc.getCompound().equals(token)){
					//add the ranks
					if(!rc.getLanguageRanks().containsKey(l)){
						rc.getLanguageRanks().put(l, rank);
					}
					//set flag
					containsCompound = true;
					break;
				}
			}
			
			if(!containsCompound){
				HashMap<Language, Integer> map = new HashMap<Language, Integer>();
				map.put(l, rank);
				
				RanksComparator rc = new Main().new RanksComparator(token, map);
				ranks.add(rc);
			}
			
			
		}
		
		return ranks;
	}
	
	
	
	private class RanksComparator{

		private String compound;
		private HashMap<Language, Integer> languageRanks;
		
		public RanksComparator(String compound, HashMap<Language, Integer> languageRanks) {
			super();
			this.compound = compound;
			this.languageRanks = languageRanks;
		}
		
		public String getCompound() {
			return compound;
		}

		public void setCompound(String compound) {
			this.compound = compound;
		}

		public HashMap<Language, Integer> getLanguageRanks() {
			return languageRanks;
		}

		public void setLanguageRanks(HashMap<Language, Integer> languageRanks) {
			this.languageRanks = languageRanks;
		}
		
		@Override
		public String toString() {
			return "RanksComparator [compound=" + compound + ", languageRanks=" + languageRanks + "]";
		}
	}
	
	
	

}
