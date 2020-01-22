package lse;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class LittleSearchEngine {
	
	HashMap<String,ArrayList<Occurrence>> keywordsIndex;
	
	HashSet<String> noiseWords;
	
	public LittleSearchEngine() {
		keywordsIndex = new HashMap<String,ArrayList<Occurrence>>(1000,2.0f);
		noiseWords = new HashSet<String>(100,2.0f);
	}
	
	public HashMap<String,Occurrence> loadKeywordsFromDocument(String docFile) 
	throws FileNotFoundException {
		
		HashMap<String, Occurrence> oneDocKeywords = new HashMap<>(1000, 2.0f);
		Scanner scn = new Scanner(new File(docFile));
		while (scn.hasNext()) 
		{
			String word = scn.next();
			word= getKeyword(word);
			
			if(word != null)
			{
				Occurrence occur= oneDocKeywords.get(word);
				if(occur != null)
					occur.frequency++;
				else
				{
					occur= new Occurrence(docFile, 1);
					oneDocKeywords.put(word,occur);
				}
			}	
		}
		
		return oneDocKeywords;
	}
	
	public void mergeKeywords(HashMap<String,Occurrence> kws) {
	
		for(Entry<String, Occurrence> temp : kws.entrySet())
		{
			String word= temp.getKey();
			Occurrence occur= temp.getValue();
			
			ArrayList<Occurrence> list= keywordsIndex.get(word);
			
			if(list != null)
			{
				list.add(occur);
				insertLastOccurrence(list);
			}
			else
			{
				list= new ArrayList<>();
				list.add(occur);
				keywordsIndex.put(word, list);
			}
		}
	}
	
	/*
	 * 	for(int i=0; i<length; i++)
		{
			char ch=word.charAt(i);
			
			if(i+1 < length)
			{
			if((ch == '.' || ch == ',' || ch == '?' || ch == ':' || ch == ';' || ch == '!' || ch == '-') && (Character.isLetter(word.charAt(i+1)) == true))
			}
			
			if(ch == '.' || ch == ',' || ch == '?' || ch == ':' || ch == ';' || ch == '!')
			{
				word= word.substring(0,i);	
				break;
			}
		}
	 */
	
	public String getKeyword(String word) {
		
		word= word.toLowerCase();
		int length= word.length();
		
		if(length == 0) {
			return null;
		}
		for(int i=0; i<length; i++)
		{
			char ch=word.charAt(i);
			
			if(i+1 < length)
			{
			if((ch == '.' || ch == ',' || ch == '?' || ch == ':' || ch == ';' || ch == '!' || ch == '-') && (Character.isLetter(word.charAt(i+1)) == true))
			{
				word= null;
				break;
			}
			}
			
			if(ch == '.' || ch == ',' || ch == '?' || ch == ':' || ch == ';' || ch == '!')
			{
				word= word.substring(0,i);	
				break;
			}
		}
		
		if (word != null && noiseWords.contains(word) ) {

            word= null;
		}
		
		return word;
	}

	public ArrayList<Integer> insertLastOccurrence(ArrayList<Occurrence> occs) {

		ArrayList<Integer> midIndex = new ArrayList<>();
		int freqToInsert= occs.get(occs.size()-1).frequency;
		int l=0, r=occs.size()-2, mid;
		int index=0;
		
		while(l <= r)
		{
			mid= (l+r)/2;
			midIndex.add(mid);
			
			int freq= occs.get(mid).frequency;
			
			if(freq == freqToInsert)
			{
				index=mid;
				break;
			}
			if(freq > freqToInsert)
			{
				l=mid+1;
				index=l;
			}
			else
			{
				r=mid-1;
				index=l;
			}
				
		}
		
		if(index != occs.size()-1)
		{
			Occurrence temp= occs.get(occs.size()-1);
			occs.remove(occs.size()-1);
			occs.add(index, temp);
		}
		
		
		return midIndex;
	}

	public void makeIndex(String docsFile, String noiseWordsFile) 
	throws FileNotFoundException {
		// load noise words to hash table
		Scanner sc = new Scanner(new File(noiseWordsFile));
		while (sc.hasNext()) {
			String word = sc.next();
			noiseWords.add(word);
		}
		
		sc = new Scanner(new File(docsFile));
		while (sc.hasNext()) {
			String docFile = sc.next();
			HashMap<String,Occurrence> kws = loadKeywordsFromDocument(docFile);
			mergeKeywords(kws);
		}
		sc.close();
	}
	
	public ArrayList<String> top5search(String kw1, String kw2) {

		ArrayList<String> result= new ArrayList<>();
		kw1=kw1.toLowerCase();
		kw2=kw2.toLowerCase();
		
		ArrayList<Occurrence> occ1= keywordsIndex.get(kw1);
		ArrayList<Occurrence> occ2= keywordsIndex.get(kw2);
	
		int index1=0, index2=0;
		int counter=0;
		Occurrence ptr1= null,ptr2= null;

		ArrayList<String> finalList= new ArrayList<>();
		
		
		while(finalList.size() < 5)
		{
			
			if(occ1 != null)
			{
				if(index1 < occ1.size())
					ptr1= occ1.get(index1);
				else
					ptr1=null;
			}
			
			if(occ2 != null)
			{
				if(index2 < occ2.size())
					ptr2= occ2.get(index2);
				else
					ptr2=null;
			}

			if(ptr1 != null && ptr2 != null)
			{
				if(ptr1.frequency == ptr2.frequency)
				{
					result.add(ptr1.document);
					index1++;
					counter++;
				}
			
				if(ptr1.frequency < ptr2.frequency)
				{
					result.add(ptr2.document);
					index2++;
					counter++;
				}
			
				if(ptr1.frequency > ptr2.frequency)
				{
					result.add(ptr1.document);
					index1++;
					counter++;
				}
			}
			else
			if(ptr1 != null && ptr2 == null)
			{
				result.add(ptr1.document);
				index1++;
				counter++;
			}
			else
			if(ptr1 == null && ptr2 != null)
			{
				result.add(ptr2.document);
				index2++;
				counter++;
			}
			else {
				break;
			}
			
			for( int i=0; i < counter; i++ )  
			{
				String doc=result.get(i);
				if(finalList.contains(doc))
					continue;
				else
					finalList.add(doc);
			}		
		}
		
		if(finalList.size() == 0)
			{return null;}
		else
			{return finalList;}
			
	
	}
}
