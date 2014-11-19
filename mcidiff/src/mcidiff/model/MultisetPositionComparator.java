package mcidiff.model;

import java.util.ArrayList;
import java.util.Comparator;

public class MultisetPositionComparator implements Comparator<Multiset>{
	private ArrayList<Multiset> list;
	
	public MultisetPositionComparator(ArrayList<Multiset> list){
		this.list = list;
	}
	
	@Override
	public int compare(Multiset set1, Multiset set2) {
		
		if(set1 != null && set2 != null){
			
			boolean isComparable = false;
			
			int sum = 0;
			for(Token token1: set1.getTokens()){
				CloneInstance instance = token1.getCloneInstance();
				Token token2 = set2.findToken(instance);
				
				if(!token2.isEpisolon() && !token1.isEpisolon()){
					isComparable = true;
					sum += token1.getStartPosition() - token2.getStartPosition();;							
				}
			}
			
			if(!isComparable){
				for(Multiset set: list){
					if(isComparable(set, set1) && isComparable(set, set2)){
						int value1 = compareValidatedSet(set, set1);
						int value2 = compareValidatedSet(set, set2);
						
						if(value1*value2 < 0){
							if(value1 > 0){
								return -1;
							}
							else{
								return 1;
							}
						}
					}
				}
				
				//System.out.println(set1.toString() + "is not comparable with " + set2.toString());
			}
			
			return sum;
		}
		
		return 0;
	}
	
	private int compareValidatedSet(Multiset set1, Multiset set2){
		int sum = 0;
		for(Token token1: set1.getTokens()){
			CloneInstance instance = token1.getCloneInstance();
			Token token2 = set2.findToken(instance);
			
			if(!token2.isEpisolon() && !token1.isEpisolon()){
				sum += token1.getStartPosition() - token2.getStartPosition();;							
			}
		}
		
		return sum;
	}
	
	private boolean isComparable(Multiset set1, Multiset set2){
		for(Token token1: set1.getTokens()){
			CloneInstance instance = token1.getCloneInstance();
			Token token2 = set2.findToken(instance);
			
			if(!token2.isEpisolon() && !token1.isEpisolon()){
				return true;
			}
		}
		
		return false;
	}
}