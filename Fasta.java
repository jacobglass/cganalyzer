package sequences;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.CharBuffer;
import java.io.File;
import java.util.regex.*;
import java.util.*;

// package to handle fasta files

public class Fasta {
  
	private File file;
	private String seq;
	private String name;
	
	// constructor loads sequence
	public Fasta(String FileName) {
		file = new File(FileName);
		seq = readFasta();		
	}
			
	public String getSeq() {
		return(seq);
	}
	
	public String getName() {
		return(name);
	}
	
		
	// reads the fasta file and extracts the header
	private String readFasta() {
        BufferedReader objReader = null;
		CharBuffer cb = null;
		
		try {
			cb = CharBuffer.allocate((int)file.length());
		} catch(OutOfMemoryError E) {
			System.out.printf("\nCould not allocate %d bytes to load %s. Consider using the -XmxNG (N = # gigabytes) flag to specify a larger heap size\n\n", (int)file.length(), file.getName());
		}
		
        try {
			
            objReader = new BufferedReader(new FileReader(file));			
			name = objReader.readLine().substring(1); // extract header
			
            String strCurrentLine;
			
            while ((strCurrentLine = objReader.readLine()) != null) {
				
                if (strCurrentLine.matches("^>")) {
                    name = name + " " + strCurrentLine;
                    System.out.printf("Got a second sequence: %s\n", strCurrentLine);
                }
                else {
                    cb.put(strCurrentLine);
                }
            }
			
			cb.flip();
			
        } catch (IOException e) {
			
            e.printStackTrace();
			
        } finally {
			
            try {
                if (objReader != null)
                    objReader.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
		
		return(cb.toString());
	}
	
	// returns an Integer array of regex positions within the file
	public Integer[] getPositionList(String regex, boolean caseSensitive) {
		List<Integer> pList = new ArrayList<Integer>();
		Pattern p;
		
		if (caseSensitive == true) {
			p = Pattern.compile(regex);
		}
		else {
			p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		}
			
		Matcher m = p.matcher(seq);
		
		while (m.find()) {
			pList.add(m.end());
		}
		
		Integer[] pArray = new Integer[pList.size()];
		pList.toArray(pArray);
		
		return(pArray);
		
	}
	
}
