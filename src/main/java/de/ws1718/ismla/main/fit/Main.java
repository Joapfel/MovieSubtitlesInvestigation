package de.ws1718.ismla.main.fit;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.WordUtils;
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

		String[] languages = { CHINESE, JAPANESE };

		runPipeline(languages, 1);

	}

	/**
	 * configurable pipeline
	 * 
	 * @param languages
	 */
	public static void runPipeline(String[] languages, int compoundSize) {
		int totalResult = 0;
		int thresholdK = 0;
		int thresholdM = 0;

		for (int k = 0; k <= 2000; k += 100) {
			for (int m = 0; m <= 2000; m += 100) {

				System.out.println("threshold k : " + k);
				System.out.println("threshold m : " + m);

				int[][] overlap = new int[4][4];

				try {

					TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription(
							new File((String) "src/resources/typeSystemDescriptor").getAbsolutePath());

					CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
							ParallelSubtitlesReader.class, tsd, ParallelSubtitlesReader.DATA_DIRECTORY_PARAM,
							"src/dependencies/subtitles-collection/data");

					// chinese annotator instance
					AnalysisEngineDescription kanji = AnalysisEngineFactory.createEngineDescription(
							KanjiAnnotatorFit.class, tsd, KanjiAnnotatorFit.PARAM_RANK_THRESHOLD, k,
							KanjiAnnotatorFit.PARAM_CHAR_FREQ_LIST, "opensubtitles-freq-chars-cmn.tsv",
							KanjiAnnotatorFit.PARAM_TOKEN_FREQ_LIST, "opensubtitles-freq-tokens-cmn.tsv",
							KanjiAnnotatorFit.PARAM_LANGUAGE, "cmn", KanjiAnnotatorFit.PARAM_GREEDY_COMPOUNT_SIZE,
							compoundSize);

					// chinese annotator instance
					AnalysisEngineDescription hanze = AnalysisEngineFactory.createEngineDescription(
							KanjiAnnotatorFit.class, tsd, KanjiAnnotatorFit.PARAM_RANK_THRESHOLD, m,
							KanjiAnnotatorFit.PARAM_CHAR_FREQ_LIST, "opensubtitles-freq-chars-jpn.tsv",
							KanjiAnnotatorFit.PARAM_TOKEN_FREQ_LIST, "opensubtitles-freq-tokens-jpn.tsv",
							KanjiAnnotatorFit.PARAM_LANGUAGE, "jpn", KanjiAnnotatorFit.PARAM_GREEDY_COMPOUNT_SIZE,
							compoundSize);

					Iterator<JCas> iterJCAS = SimplePipeline.iteratePipeline(reader, kanji, hanze).iterator();
					
					
					//try with a list
					ArrayList<List<String>> cmnCompounds = new ArrayList<List<String>>();
					ArrayList<List<String>> jpnCompounds = new ArrayList<List<String>>(); 

					int movieNumber = 0;
					
					// iterate over jcas'
					while (iterJCAS.hasNext()) {

						JCas jcas = iterJCAS.next();
						
						for (String l : languages) {

							// movie seperation
							String movieName = "Movie: " + movieNumber;

							// get the jcas view
							JCas view = jcas.getView(l);

							// get all the compounds for a jcas
							List<Compound> compounds = getCompounds(view);
							
							if(l.equals(languages[0])){
								//extract the covered compounds
								cmnCompounds.add(getCoveredTextsFromCompounds(compounds));
								
							}else if(l.equals(languages[1])){
								//extract the covered compounds
								jpnCompounds.add(getCoveredTextsFromCompounds(compounds));
								
							}
						}
						
						// increment movie number
						movieNumber++;
					}
					
					
					//fill the matrix
					for(int i = 0; i < cmnCompounds.size(); i++){
						
						List<String> cmnList = cmnCompounds.get(i);
						List<String> jpnList = null;
						
						for(int j = 0; j < jpnCompounds.size(); j++){
							
							jpnList = jpnCompounds.get(j);
							
							int shared = getSharedKanjiCount(cmnList, jpnList);

							overlap[i][j] = shared;
						}
					}
					
					
					//print the table
					for(int i = 0; i < overlap.length; i++){
						int[] row = overlap[i];
						for(int j = 0; j < row.length; j++){
							int cell = row[j];
							System.out.print(cell + " | ");
						}
						System.out.println();
						System.out.println("-----------------------------------");
						System.out.println();
					}
					// all artifacts were processed
					
					
					 // hardcoded test
					if(k == 0 && m == 0){
						List<String> mv1 = cmnCompounds.get(0);
						List<String> mv2 = jpnCompounds.get(0);
						List<Word> test = getSortedSharedKanjis(mv1, mv2, 10);
						for(Word w : test){
							System.out.println(w.getCompound() + " overlap -> " + w.getFrequency());
						}
					}
					

					int[][] expectedMatrix = getExpectedValuesForMatrix(overlap);

					// print the expected matrix
					for (int[] row : expectedMatrix) {
						for (int col : row) {
							System.out.print(col + " | ");
						}
						System.out.println();
						System.out.println("----------------------------");
						System.out.println();
					}


					// calculate result
					int magicalShi = getMagicalShi(overlap, expectedMatrix);
					
					//safe if result is better then previous one
					if (magicalShi > totalResult) {
						totalResult = magicalShi;
						thresholdK = k;
						thresholdM = m;
					}
					System.out.println("magical shi value: " + magicalShi);

				} catch (ResourceInitializationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UIMAException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}

		System.out.println();
		System.out.println("total Result: " + totalResult);
		System.out.println("best jpn threshold: " + thresholdK);
		System.out.println("best cmn threshold: " + thresholdM);
	}
	

	/**
	 * get all compounds for a specific jcas
	 * 
	 * @param jcas
	 * @return
	 */
	public static List<Compound> getCompounds(JCas jcas) {

		List<Compound> rval = new ArrayList<Compound>();

		for (Compound compound : JCasUtil.select(jcas, Compound.class)) {
			rval.add(compound);
		}
		return rval;
	}
	
	
	public static List<String> getCoveredTextsFromCompounds(List<Compound> compounds){
		List<String> rval = new ArrayList<String>();
		
		for(Compound c : compounds){
			rval.add(c.getCoveredText());
		}
		return rval;
	}
	
	
	public static int getSharedKanjiCount(List<String> comps, List<String> compsCompare) {
		int rval = 0;

		HashSet<String> singletons = new HashSet<String>();

		for (String s : comps) {
			singletons.add(s);
		}

		for (String s : singletons) {

			// get number of occurances in first movie
			int compsAmount = 0;
			for (String w : comps) {
				if (w.equals(s)) {
					compsAmount++;
				}
			}

			// get number of occurances in the second movie
			int compsCompareAmount = 0;
			for (String w : compsCompare) {
				if (w.equals(s)) {
					compsCompareAmount++;
				}
			}
			rval += Math.min(compsAmount, compsCompareAmount);
		}
		return rval;
	}
	
	
	public static List<Word> getSortedSharedKanjis(List<String> comps, List<String> compsCompare, int n) {
		
		List<Word> rval = new ArrayList<Word>();
		
		HashSet<String> singletons = new HashSet<String>();

		for (String s : comps) {
			singletons.add(s);
		}
		
		for (String s : singletons) {

			// get number of occurances in first movie
			int compsAmount = 0;
			for (String w : comps) {
				if (w.equals(s)) {
					compsAmount++;
				}
			}

			// get number of occurances in the second movie
			int compsCompareAmount = 0;
			for (String w : compsCompare) {
				if (w.equals(s)) {
					compsCompareAmount++;
				}
			}
			int overlap = Math.min(compsAmount, compsCompareAmount);
			rval.add(new Word(s, overlap));
		}
		
		Collections.sort(rval);
		Collections.reverse(rval);
		
		return rval;
	}


	public static ArrayList<Integer> getSummedRows(int[][] matrix) {
		ArrayList<Integer> rval = new ArrayList<Integer>();

		for (int[] row : matrix) {
			int rowSum = 0;
			for (int col : row) {
				rowSum += col;
			}
			rval.add(rowSum);
		}
		return rval;
	}

	public static ArrayList<Integer> getSummedCols(int[][] matrix) {
		ArrayList<Integer> rval = new ArrayList<Integer>();

		int[] colSums = new int[matrix.length];
		for (int[] row : matrix) {

			for (int i = 0; i < row.length; i++) {
				int col = row[i];
				colSums[i] += col;
			}
		}
		for (int i : colSums) {
			rval.add(i);
		}
		return rval;
	}

	public static int getMatrixSum(int[][] matrix) {
		int sum = 0;

		for (int[] row : matrix) {
			for (int col : row) {
				sum += col;
			}
		}
		return sum;
	}

	public static int[][] getExpectedValuesForMatrix(int[][] matrix) {
		int[][] rval = new int[matrix.length][matrix[0].length];

		List<Integer> summedRows = getSummedRows(matrix);
		List<Integer> summedCols = getSummedCols(matrix);
		int matrixSum = getMatrixSum(matrix);

		for (int i = 0; i < matrix.length; i++) {
			int summedRow = summedRows.get(i);
			for (int j = 0; j < matrix[i].length; j++) {
				int summedCol = summedCols.get(j);

				rval[i][j] = (summedRow * summedCol) / matrixSum;
			}
		}

		return rval;
	}


	public static int getMagicalShi(int[][] matrix, int[][] expectedMatrix) {
		
		//from expected matrix
		int diagonalSum = 0;
		int nonDiagonalSum = 0;
		
		for(int i = 0; i < expectedMatrix.length; i++){
			int[] row = expectedMatrix[i];
			for(int j = 0; j < row.length; j++){
				if(i==j){
					diagonalSum += expectedMatrix[i][j];
				}else{
					nonDiagonalSum += expectedMatrix[i][j];
				}
			}
		}

		if(diagonalSum == 0 || nonDiagonalSum == 0) return 0;
		
		ArrayList<Integer> diagonal = new ArrayList<Integer>();
		ArrayList<Integer> notDiagonal = new ArrayList<Integer>();

		// square the differences
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {

				// diagonal
				if (i == j) {
					int diff = matrix[i][j] - expectedMatrix[i][j];
					diagonal.add(diff);

					// not diagonal
				} else {
					int diff = matrix[i][j] - expectedMatrix[i][j];
					notDiagonal.add(diff);
				}
			}
		}
		
		
		

		// sum diagonals
		int d = 0;
		for (int i : diagonal) {
			d += i;
		}
		d = d * d;
		d = d / diagonalSum;

		// sum not diagonals
		int notD = 0;
		for (int i : notDiagonal) {
			notD += i;
		}
		notD = notD * notD;
		notD = notD / nonDiagonalSum;

		System.out.println("diagonal sum: " + d);
		System.out.println("non-diagonal sum: " + notD);

		return d + notD;
	}
}
