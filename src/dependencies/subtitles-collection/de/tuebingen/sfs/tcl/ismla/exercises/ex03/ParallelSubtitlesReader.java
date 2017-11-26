package de.tuebingen.sfs.tcl.ismla.exercises.ex03;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.cas.CASException;
import org.apache.uima.collection.CollectionException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.jcas.JCas;
import org.apache.uima.util.Progress;
import org.apache.uima.util.ProgressImpl;

public class ParallelSubtitlesReader extends org.apache.uima.fit.component.JCasCollectionReader_ImplBase {
	public static final String DATA_DIRECTORY_PARAM = "dataDirName";
	@ConfigurationParameter(name = DATA_DIRECTORY_PARAM)
	private String dataDirName;

	int currentPosition;
	List<String> parallelSubtitleFileNames;

	public void initialize(UimaContext context) {
		File dataDir = new File(dataDirName);
		parallelSubtitleFileNames = new ArrayList<String>();
		addFiles(dataDir);
	}

	private void addFiles(File subtitleFileDir) {
		for (File file : subtitleFileDir.listFiles()) {
			if (file.isDirectory()) {
				addFiles(file);
			} else {
				if (file.getName().endsWith("tsv")) {
					parallelSubtitleFileNames.add(file.getAbsolutePath());
				}
			}
		}
	}

	public void getNext(JCas cas) throws IOException, CollectionException {
		File subtitleFile = new File(parallelSubtitleFileNames.get(currentPosition));
		
		BufferedReader r = new BufferedReader(new FileReader(subtitleFile));
		// process header
		String header = r.readLine();
		String[] columnCaptions = header.split("\t");
		if (columnCaptions.length < 3) {
			r.close();
			throw new IOException("ERROR: header of subtitle file " + subtitleFile.getAbsolutePath() + "too short!");
		}
		List<String[]> tokensPerLine = new LinkedList<String[]>();
		String line = null;
		while ((line = r.readLine()) != null) {
			String[] tokens = line.split("\t");
			if (tokens.length < columnCaptions.length)
				continue;
			tokensPerLine.add(tokens);
		}
		r.close();

		for (int langColID = 2; langColID < columnCaptions.length; langColID++)
		{
			String langName = columnCaptions[langColID];
			JCas langView = null;
			try {
				langView = cas.createView(langName);
			} catch (CASException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			StringBuilder langText = new StringBuilder();
			for (String[] tokens : tokensPerLine)
			{
				if (tokens[langColID].length() > 0)
				{
					langText.append(tokens[langColID] + "\n");
				}
			}
			langView.setDocumentText(langText.toString());
		}
		
		currentPosition++;
	}

	public Progress[] getProgress() {
		Progress prog = new ProgressImpl(currentPosition, parallelSubtitleFileNames.size(), Progress.ENTITIES);
		return new Progress[] { prog };
	}

	public boolean hasNext() throws IOException, CollectionException {
		return (currentPosition < parallelSubtitleFileNames.size());
	}

}
