package test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TestHashSet {
	public static void main(String args[]) {
		HashSet <String> hs= new HashSet();
		
		hs.add("11");
		hs.add("22");
		hs.add("11");
		
		Iterator<String> i = hs.iterator();
		
		while(i.hasNext()) {
			System.out.println(i.next());
		}
		
		HashMap<String, String> hm = new HashMap();
		hm.put("1", "A");
		hm.put("2","B");
		hm.put("1", "z");
		
		for(String s:hm.keySet()) {
			System.out.println(hm.get(s));
		}
		
		
	}
}
