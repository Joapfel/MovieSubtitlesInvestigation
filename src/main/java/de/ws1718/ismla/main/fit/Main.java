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

		for (int k = 0; k < 2000; k += 100) {
			for (int m = 0; m < 2000; m += 100) {
				
				System.out.println("threshold k : " + k);
				System.out.println("threshold m : " + m);

				HashMap<String, ArrayList<RanksComparator>> movies = new HashMap<String, ArrayList<RanksComparator>>();

				List<ArtifactComparator> mvs = new ArrayList<Main.ArtifactComparator>();

				HashMap<String, List<ArtifactComparator>> mv = new HashMap<String, List<ArtifactComparator>>();

				int[][] overlap = new int[4][4];

				try {

					TypeSystemDescription tsd = TypeSystemDescriptionFactory.createTypeSystemDescription(
							new File((String) "src/resources/typeSystemDescriptor").getAbsolutePath());

					CollectionReaderDescription reader = CollectionReaderFactory.createReaderDescription(
							ParallelSubtitlesReader.class, tsd, ParallelSubtitlesReader.DATA_DIRECTORY_PARAM,
							"src/dependencies/subtitles-collection/data");

					// different instances (cmn, jpn)

					for (String l : languages) {
						
						int threshold = 0;
						if(l.equals("cmn")){
							threshold = m;
						}else{
							threshold = k;
						}

						System.out.println(l + " " + threshold);

						// chinese annotator instance
						AnalysisEngineDescription kanji = AnalysisEngineFactory.createEngineDescription(
								KanjiAnnotatorFit.class, tsd, KanjiAnnotatorFit.PARAM_RANK_THRESHOLD, threshold,
								KanjiAnnotatorFit.PARAM_CHAR_FREQ_LIST, "opensubtitles-freq-chars-" + l + ".tsv",
								KanjiAnnotatorFit.PARAM_TOKEN_FREQ_LIST, "opensubtitles-freq-tokens-" + l + ".tsv",
								KanjiAnnotatorFit.PARAM_LANGUAGE, l, KanjiAnnotatorFit.PARAM_GREEDY_COMPOUNT_SIZE,
								compoundSize);

						Iterator<JCas> iterCMN = SimplePipeline.iteratePipeline(reader, kanji).iterator();

						int i = 0;
						Language language = new Language(l);

						// iterate over jcas'
						while (iterCMN.hasNext()) {
							// movie seperation
							String movie = "Movie: " + i;

							// check wheter there is a data for this movie
							ArrayList<RanksComparator> ranks;
							if (movies.keySet().contains(movie)) {
								ranks = movies.get(movie);
							} else {
								ranks = new ArrayList<Main.RanksComparator>();
							}

							// get the jcas view
							JCas jcas = iterCMN.next().getView(l);

							// get all the compounds for a jcas
							List<Compound> compounds = getCompounds(jcas);

							// create comparator
							ArtifactComparator mc = new Main().new ArtifactComparator(movie, language, compounds);
							mvs.add(mc);
							if (mv.keySet().contains(l)) {
								mv.get(l).add(mc);
							} else {
								ArrayList<ArtifactComparator> tmp = new ArrayList<Main.ArtifactComparator>();
								tmp.add(mc);
								mv.put(l, tmp);
							}

							// compute ranks
							ranks = getRanks(ranks, jcas, language);

							movies.put(movie, ranks);

							// increment movie number
							i++;
						}
					}

					// all artifacts were processed

					// // hardcoded test
					// List<ArtifactComparator> cmnList = mv.get("cmn");
					// List<ArtifactComparator> jpnList = mv.get("jpn");
					// ArtifactComparator a1 = null;
					// ArtifactComparator a2 = null;
					// for (ArtifactComparator ac : cmnList) {
					// System.out.println(ac.getArtifactName());
					// if (ac.getArtifactName().trim().equals("Movie: 0")) {
					// a1 = ac;
					// }
					// }
					// for (ArtifactComparator ac : jpnList) {
					// if (ac.getArtifactName().trim().equals("Movie: 0")) {
					// a2 = ac;
					// }
					// }
					// System.out.println("===============================================");
					// if (a1 != null && a2 != null) {
					// System.out.println("Getting N most frequent shared
					// Kanjies...");
					// List<Compound> result =
					// a1.getNMostFrequentSharedKanjis(10,
					// a2);
					// for (Compound c : result) {
					// System.out.println(c.getCoveredText() + " -> " +
					// c.getRank());
					// }
					// }
					// System.out.println("End of N most frequent shared
					// Kanjies...");
					// System.out.println("===============================================");

					
					//filling the matrix
					List<ArtifactComparator> l1 = mv.get(languages[0]);
					List<ArtifactComparator> l2 = mv.get(languages[1]);

					for (int i = 0; i < l1.size(); i++) {
						ArtifactComparator ac = l1.get(i);

						for (int j = 0; j < l2.size(); j++) {
							ArtifactComparator acComp = l2.get(j);

							int sharedKanji = ac.getSharedKanjiCount(acComp);

							System.out.println(i + " " + j);
							if (i < overlap.length && j < overlap[i].length) {

								int overlapValue = overlap[i][j];
								if (overlapValue == 0) {
									overlap[i][j] = sharedKanji;
								}
							}

							System.out.println(ac.getArtifactName() + "/" + acComp.getArtifactName() + "("
									+ ac.getLanguage().getFullLanguage() + "/" + acComp.getLanguage().getFullLanguage()
									+ ")" + " has " + sharedKanji + " shared Kanji's");
						}

					}
					// }

					for (int[] row : overlap) {
						for (int col : row) {
							System.out.print(col + " | ");
						}
						System.out.println();
						System.out.println("----------------------------");
						System.out.println();
					}
					System.out.println();
					System.out.println();

					ArrayList<Integer> summedRows = getSummedRows(overlap);
					ArrayList<Integer> summedCols = getSummedCols(overlap);
					int sum = getMatrixSum(overlap);

					int[][] expectedMatrix = getExpectedValuesForMatrix(summedRows, summedCols, sum);

					for (int[] row : expectedMatrix) {
						for (int col : row) {
							System.out.print(col + " | ");
						}
						System.out.println();
						System.out.println("----------------------------");
						System.out.println();
					}

					int magicalShi = getMagicalShi(overlap, expectedMatrix);
					if(magicalShi > totalResult){
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
		System.out.println("best k: " + thresholdK);
		System.out.println("best m: " + thresholdM);
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

	public static int[][] getExpectedValuesForMatrix(List<Integer> summedRows, List<Integer> summedCols,
			int matrixSum) {
		int[][] rval = new int[summedRows.size()][summedCols.size()];

		for (int i = 0; i < rval.length; i++) {
			int summedRow = summedRows.get(i);
			for (int j = 0; j < rval[i].length; j++) {
				int summedCol = summedCols.get(j);

				rval[i][j] = (summedRow * summedCol) / matrixSum;
			}
		}

		return rval;
	}

	public static int getMagicalShi(int[][] matrix, int[][] expectedMatrix) {

		ArrayList<Integer> diagonal = new ArrayList<Integer>();
		ArrayList<Integer> notDiagonal = new ArrayList<Integer>();

		// square the differences
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[i].length; j++) {

				// diagonal
				if (i == j) {
					int diff = matrix[i][j] - expectedMatrix[i][j];
					diagonal.add(diff * diff);

					// not diagonal
				} else {
					int diff = matrix[i][j] - expectedMatrix[i][j];
					notDiagonal.add(diff * diff);
				}
			}
		}

		// sum diagonals
		int d = 0;
		for (int i : diagonal) {
			d += i;
		}
		d = d / diagonal.size();

		// sum not diagonals
		int notD = 0;
		for (int i : notDiagonal) {
			notD += i;
		}
		notD = notD / notDiagonal.size();

		System.out.println("diagonal sum: " + d);
		System.out.println("non-diagonal sum: " + notD);

		return d + notD;
	}

	/**
	 * helper for computing ranks
	 * 
	 * @param ranks
	 * @param jcas
	 * @return
	 */
	public static ArrayList<RanksComparator> getRanks(ArrayList<RanksComparator> ranks, JCas jcas, Language l) {

		// iterate over annotations
		for (Compound compound : JCasUtil.select(jcas, Compound.class)) {

			int rank = compound.getRank();
			String token = compound.getCoveredText();

			boolean containsCompound = false;
			for (RanksComparator rc : ranks) {
				if (rc.getCompound().equals(token)) {
					// add the ranks
					if (!rc.getLanguageRanks().containsKey(l)) {
						rc.getLanguageRanks().put(l, rank);
					}
					// set flag
					containsCompound = true;
					break;
				}
			}

			if (!containsCompound) {
				HashMap<Language, Integer> map = new HashMap<Language, Integer>();
				map.put(l, rank);

				RanksComparator rc = new Main().new RanksComparator(token, map);
				ranks.add(rc);
			}

		}

		return ranks;
	}

	private class RanksComparator {

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

	private class ArtifactComparator {

		private String artifactName;
		private Language language;
		private List<Compound> compounds;

		public ArtifactComparator(String artifactName, Language language, List<Compound> compounds) {
			super();
			this.artifactName = artifactName;
			this.language = language;
			this.compounds = compounds;
		}

		public int getSharedKanjiCountOLD(ArtifactComparator mc) {
			int rval = 0;

			List<Compound> comps = this.compounds;
			List<Compound> compsCompare = mc.getCompounds();

			// System.out.println(comps.size() + " : " + compsCompare.size());

			for (int i = 0; i < comps.size(); i++) {
				String s1 = comps.get(i).getCoveredText();
				String s2 = "";
				if (i < compsCompare.size()) {
					s2 = compsCompare.get(i).getCoveredText();
				}
				if (s1.equals(s2))
					rval++;
			}

			return rval;
		}

		public int getSharedKanjiCount(ArtifactComparator mc) {
			int rval = 0;

			List<Compound> comps = this.compounds;
			List<Compound> compsCompare = mc.getCompounds();

			// System.out.println("number of compounds " + comps.size());

			HashSet<String> hs = new HashSet<String>();

			// this collection
			for (Compound c1 : comps) {

				String s1 = c1.getCoveredText();
				// if (!hs.contains(s1)) {

				boolean shared = false;

				// collection to compare to
				for (Compound c2 : compsCompare) {

					String s2 = c2.getCoveredText();
					if (s1.equals(s2))
						shared = true;

				}

				// if a compound from one collection appears in the other
				// collection
				if (shared) {
					rval++;
				}

				hs.add(s1);
			}
			// }
			return rval;
		}

		public List<Compound> getNMostFrequentSharedKanjis(int n, ArtifactComparator mc) {
			ArrayList<Compound> rval = new ArrayList<Compound>();

			List<Compound> comps = this.compounds;
			List<Compound> compsCompare = mc.getCompounds();

			if (n > comps.size() || n > compsCompare.size())
				return new ArrayList<Compound>();

			HashSet<Compound> tmp = new HashSet<Compound>();

			// this collection
			for (Compound c1 : comps) {

				String s1 = c1.getCoveredText();
				// if (!hs.contains(s1)) {

				boolean shared = false;

				// collection to compare to
				for (Compound c2 : compsCompare) {

					String s2 = c2.getCoveredText();
					if (s1.equals(s2)) {
						shared = true;
					}

				}

				// if a compound from one collection appears in the other
				// collection
				if (shared) {
					boolean add = true;
					for (Compound c : tmp) {
						if (c.getCoveredText().trim().equals(s1)) {
							add = false;
						}
					}
					if (add) {
						tmp.add(c1);
					}
				}
			}

			// naive sorting/picking
			Compound highestRank = null;
			for (int i = 0; i < n; i++) {
				Compound del = null;
				for (Compound c : tmp) {
					if (highestRank == null) {
						highestRank = c;
					}
					if (c.getRank() > highestRank.getRank()) {
						highestRank = c;
						del = c;
					}
				}
				rval.add(highestRank);
				tmp.remove(del);
				highestRank = null;
			}
			return rval;
		}

		public String getArtifactName() {
			return artifactName;
		}

		public void setArtifactName(String movie) {
			this.artifactName = movie;
		}

		public Language getLanguage() {
			return language;
		}

		public void setLanguage(Language language) {
			this.language = language;
		}

		public List<Compound> getCompounds() {
			return compounds;
		}

		public void setCompounds(List<Compound> compounds) {
			this.compounds = compounds;
		}

		@Override
		public String toString() {
			return "MovieComparator [movie=" + artifactName + ", language=" + language + ", compounds=" + compounds
					+ "]";
		}

		/**
		 * not regarding the outer enclosing type
		 * 
		 * comparing only on artifact instance
		 */
		public boolean equalsMovie(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArtifactComparator other = (ArtifactComparator) obj;

			if (artifactName == null) {
				if (other.artifactName != null)
					return false;
			} else if (!artifactName.equals(other.artifactName))
				return false;
			return true;
		}

		/**
		 * not regarding the outer enclosing type
		 * 
		 * comparing only on language instance
		 */
		public boolean equalsLanguage(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArtifactComparator other = (ArtifactComparator) obj;

			if (language == null) {
				if (other.language != null)
					return false;
			} else if (!language.getFullLanguage().equals(other.language.getFullLanguage()))
				return false;
			return true;
		}

		/**
		 * not regarding the outer enclosing type
		 * 
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			ArtifactComparator other = (ArtifactComparator) obj;

			if (artifactName == null) {
				if (other.artifactName != null)
					return false;
			} else if (!artifactName.equals(other.artifactName))
				return false;
			if (compounds == null) {
				if (other.compounds != null)
					return false;
			} else if (!compounds.equals(other.compounds))
				return false;
			if (language == null) {
				if (other.language != null)
					return false;
			} else if (!language.getFullLanguage().equals(other.language.getFullLanguage()))
				return false;
			return true;
		}

	}

}
