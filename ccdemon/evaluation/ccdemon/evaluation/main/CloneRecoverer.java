package ccdemon.evaluation.main;

import java.util.ArrayList;
import java.util.Iterator;

import mcidiff.main.SeqMCIDiff;
import mcidiff.model.CloneInstance;
import mcidiff.model.CloneSet;
import mcidiff.model.SeqMultiset;
import mcidiff.model.Token;
import mcidiff.model.TokenSeq;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.CompilationUnit;

import ccdemon.evaluation.model.CPWrapper;
import ccdemon.evaluation.model.CPWrapperList;
import ccdemon.model.Candidate;
import ccdemon.model.ConfigurationPoint;
import ccdemon.model.ConfigurationPointSet;
import ccdemon.model.ReferrableCloneSet;
import ccdemon.util.CCDemonUtil;

public class CloneRecoverer {
	
	public class CollectedData{
		private CloneInstance cloneInstance;
		private double configurationEffort;
		private double savedEditingEffort;
		private double correctness;
		
		private int historyNum = 0;
		private int environmentNum = 0;
		private int ruleNum = 0;
		private int totalNum = 0;
		
		private int configurationPointNum = 0;
		private ConfigurationPointSet cps;
		
		public String toString(){
			return "\ncloneInstance: " + cloneInstance.toString() + "\nconfigurationEffort: "
					+ configurationEffort + ", savedEditingEffort: " + savedEditingEffort
					+ ", correctness: " + correctness + ", historyNum: " + historyNum 
					+ ", environmentNum: " + environmentNum + ", ruleNum: " + ruleNum 
					+ ", configurationPointNum: " + configurationPointNum;
		}
		
		
		/**
		 * @return the configurationEffort
		 */
		public double getConfigurationEffort() {
			return configurationEffort;
		}
		/**
		 * @param configurationEffort the configurationEffort to set
		 */
		public void setConfigurationEffort(double configurationEffort) {
			this.configurationEffort = configurationEffort;
		}
		/**
		 * @return the savedEditingEffort
		 */
		public double getSavedEditingEffort() {
			return savedEditingEffort;
		}
		/**
		 * @param savedEditingEffort the savedEditingEffort to set
		 */
		public void setSavedEditingEffort(double savedEditingEffort) {
			this.savedEditingEffort = savedEditingEffort;
		}
		/**
		 * @return the correctness
		 */
		public double getCorrectness() {
			return correctness;
		}
		/**
		 * @param correctness the correctness to set
		 */
		public void setCorrectness(double correctness) {
			this.correctness = correctness;
		}
		public CloneInstance getCloneInstance() {
			return cloneInstance;
		}
		public void setCloneInstance(CloneInstance cloneInstance) {
			this.cloneInstance = cloneInstance;
		}
		/**
		 * @return the historyNum
		 */
		public int getHistoryNum() {
			return historyNum;
		}
		/**
		 * @param historyNum the historyNum to set
		 */
		public void setHistoryNum(int historyNum) {
			this.historyNum = historyNum;
		}
		/**
		 * @return the environmentNum
		 */
		public int getEnvironmentNum() {
			return environmentNum;
		}
		/**
		 * @param environmentNum the environmentNum to set
		 */
		public void setEnvironmentNum(int environmentNum) {
			this.environmentNum = environmentNum;
		}
		/**
		 * @return the ruleNum
		 */
		public int getRuleNum() {
			return ruleNum;
		}
		/**
		 * @param ruleNum the ruleNum to set
		 */
		public void setRuleNum(int ruleNum) {
			this.ruleNum = ruleNum;
		}
		/**
		 * @return the totalNum
		 */
		public int getTotalNum() {
			return totalNum;
		}
		/**
		 * @param totalNum the totalNum to set
		 */
		public void setTotalNum(int totalNum) {
			this.totalNum = totalNum;
		}

		/**
		 * @return the configurationPointNum
		 */
		public int getConfigurationPointNum() {
			return configurationPointNum;
		}
		/**
		 * @param configurationPointNum the configurationPointNum to set
		 */
		public void setConfigurationPointNum(int configurationPointNum) {
			this.configurationPointNum = configurationPointNum;
		}


		public ConfigurationPointSet getCps() {
			return cps;
		}


		public void setCps(ConfigurationPointSet cps) {
			this.cps = cps;
		}
		
		
	}
	
	private int historyNum = 0;
	private int environmentNum = 0;
	private int ruleNum = 0;
	private int totalNum = 0;
	
	public ArrayList<CollectedData> getTrials(CloneSet set){
		ArrayList<CollectedData> datas = new ArrayList<CollectedData>();
		
		SeqMCIDiff diff = new SeqMCIDiff();
		IJavaProject proj = CCDemonUtil.retrieveWorkingJavaProject();
		ArrayList<SeqMultiset> diffList = diff.diff(set, proj);
		
		/**
		 * choose the target clone instance to be recovered
		 */
		for(CloneInstance targetInstance: set.getInstances()){
			
			//ArrayList<SeqMultiset> typeIDiffs = findTypeIDiff(diffList, targetInstance);
			ArrayList<SeqMultiset> matchableDiffs = findMatchableDiff(diffList, targetInstance);
			
			double correctness = matchableDiffs.size()/((double)diffList.size());
			
			/**
			 * choose the source clone instance as the copied instance
			 */
			for(CloneInstance sourceInstance: set.getInstances()){
				if(sourceInstance.equals(targetInstance)){
					continue;
				}

				CPWrapperList wrapperList = 
						constructPartialConfigurationPoints(sourceInstance, targetInstance, matchableDiffs);
				ArrayList<ConfigurationPoint> pointList = wrapperList.getConfigurationPoints();
				
				ConfigurationPointSet cps = identifyPartialConfigurationPointSet(proj, 
						pointList, targetInstance, sourceInstance, set);
				
				CollectedData data = simulate(cps, wrapperList);
				data.setCorrectness(correctness);
				data.setCloneInstance(targetInstance);
				data.setHistoryNum(this.historyNum);
				data.setEnvironmentNum(this.environmentNum);
				data.setRuleNum(this.ruleNum);
				data.setTotalNum(this.totalNum);
				data.setConfigurationPointNum(cps.getConfigurationPoints().size());
				data.setCps(cps);
				datas.add(data);
				
				System.out.println("===================================");
				System.out.println("copied source:" + sourceInstance.toString());
				System.out.println("target source:" + targetInstance.toString());
				System.out.println("correctness: " + data.getCorrectness());
				System.out.println("configuration effort: " + data.getConfigurationEffort());
				System.out.println("saved editing effort: " + data.getSavedEditingEffort());
				System.out.println("===================================");
				
				System.currentTimeMillis();
				
				break;
			}
		}
		return datas;
	}
	
	private CollectedData simulate(ConfigurationPointSet cps,
			CPWrapperList wrapperList) {
		
		this.historyNum = 0;
		this.environmentNum = 0;
		this.ruleNum = 0;
		this.totalNum = 0;
		
		double totalConfigurationEffort = 0;
		double totalEditingEffort = 0;
		
		int configurableSize = 0;
		
		for(int i=0; i<cps.getConfigurationPoints().size(); i++){
			ConfigurationPoint cp = cps.getConfigurationPoints().get(i);
			TokenSeq correctSeq = wrapperList.findCorrectSeq(cp);
			int configurationEffort = findCandidate(cp.getCandidates(), correctSeq);
			
			if(configurationEffort != -1){
				totalConfigurationEffort += (double)configurationEffort/cp.getCandidates().size();
				configurableSize++;
				
				this.totalNum++;
			}
			else{
				totalEditingEffort++;
			}
			
			String text = correctSeq.getText();
			cps.getRule().applyRule(text, cp);
			cps.adjustCandidateRanking();
			
			System.currentTimeMillis();
		}
		
		totalConfigurationEffort /= configurableSize;
		double savedEditingEffort = 1 - ((double)totalEditingEffort)/cps.size();
		
		CollectedData data = new CollectedData();
		data.setConfigurationEffort(totalConfigurationEffort);
		data.setSavedEditingEffort(savedEditingEffort);
		
		return data;
	}

	private int findCandidate(ArrayList<Candidate> candidates, TokenSeq correctSeq) {
		for(int i=0; i<candidates.size(); i++){
			Candidate candidate = candidates.get(i);
			String text = candidate.getText();
			
			ArrayList<Token> tokenList = parseTokenFromText(text, 0);
			TokenSeq seq = new TokenSeq();
			seq.setTokens(tokenList);
			if(seq.isEpisolonTokenSeq() && correctSeq.isEpisolonTokenSeq()){
				countOriginContribution(candidate);
				return i;
			}
			else if(seq.toString().equals(correctSeq.toString())){
				countOriginContribution(candidate);
				return i;
			}
		}
		
		return -1;
	}
	
	private void countOriginContribution(Candidate candidate){
		if(candidate.isHistoryBased()){
			this.historyNum++;
		}
		else if(candidate.isEnvironmentBased()){
			this.environmentNum++;
		}
		else if(candidate.isRuleBased()){
			this.ruleNum++;
		}
	}
	
	private ArrayList<Token> parseTokenFromText(String pastedContent, int basePosition) {
		ArrayList<Token> tokenList = new ArrayList<>();
		
		IScanner scanner = ToolFactory.createScanner(false, false, false, false);
		scanner.setSource(pastedContent.toCharArray());
		
		Token previous = null;
		while(true){
			try {
				int t = scanner.getNextToken();
				if(t == ITerminalSymbols.TokenNameEOF){
					break;
				}
				String tokenName = new String(scanner.getCurrentTokenSource());
				
				int startPosition = basePosition + scanner.getCurrentTokenStartPosition();
				int endPosition = basePosition + scanner.getCurrentTokenEndPosition()+1;
				
				Token token = new Token(tokenName, null, null, startPosition, endPosition);
				tokenList.add(token);
				
				token.setPreviousToken(previous);
				if(previous != null){
					previous.setPostToken(token);
				}
				previous = token;
			} catch (InvalidInputException e) {
				e.printStackTrace();
			}
			
		}
		
		return tokenList;
	}

	private ConfigurationPointSet identifyPartialConfigurationPointSet(IJavaProject proj, ArrayList<ConfigurationPoint> pointList,
			CloneInstance targetInstance, CloneInstance sourceInstance, CloneSet set){
		CompilationUnit cu = findCompilationUnitOfTargetInstance(proj, targetInstance);
		int startPosition = cu.getPosition(targetInstance.getStartLine(), 0);
		
		ConfigurationPointSet cpSet = new ConfigurationPointSet(pointList, set, cu, startPosition);
		CloneSet newSet = cloneCloneSet(set, targetInstance);
		ReferrableCloneSet rcs = new ReferrableCloneSet(newSet, sourceInstance);
		ArrayList<ReferrableCloneSet> rcsList = new ArrayList<>();
		rcsList.add(rcs);
		
		cpSet.prepareForInstallation(rcsList);
		
		return cpSet;
	}
	
	private CloneSet cloneCloneSet(CloneSet set, CloneInstance instance){
		CloneSet newSet = new CloneSet(set.getId());
		for(CloneInstance ins: set.getInstances()){
			if(!ins.equals(instance)){
				newSet.addInstance(ins);
			}
		}
		
		return newSet;
	}
	
	private CompilationUnit findCompilationUnitOfTargetInstance(IJavaProject proj, CloneInstance targetInstance) {
		String filePath = targetInstance.getFileName();
		String path = filePath.substring(0, filePath.lastIndexOf("."));
		
		String projPath = proj.getProject().getLocation().toOSString();
		path = proj.getPath().toOSString() + path.substring(projPath.length(), path.length());
		
		CompilationUnit cu = CCDemonUtil.getCompilationUnitFromFileLocation(proj, path);
		return cu;
	}
	
	/**
	 * Given a target instance, the diffs can be divided into two categories: 
	 * Type I: The token sequence for target instance is different from the token sequences for other instances, 
	 * in this case, those token sequences in other instances are exactly the same.
	 * Type II (i.e., matchable diff): The token sequences for other instances are different with each other.
	 * 
	 * By differentiating these differences, I am able to identify the correct candidate for each configuration
	 * point.
	 * 
	 * @param diffList
	 * @return
	 */
	private ArrayList<SeqMultiset> findMatchableDiff(ArrayList<SeqMultiset> diffList, CloneInstance targetInstance){
		ArrayList<SeqMultiset> typeTwoDiffList = new ArrayList<>();
		
		for(SeqMultiset diff : diffList){
			//TokenSeq targetSeq = diff.findTokenSeqByCloneInstance(targetInstance);
			int difference = 0;
			for(int i = 0; i < diff.getSequences().size(); i++){
				if(!diff.getSequences().get(i).getCloneInstance().equals(targetInstance)){
					for(int j = i + 1; j < diff.getSequences().size(); j++){
						if(!diff.getSequences().get(j).getCloneInstance().equals(targetInstance)){
							if(!diff.getSequences().get(i).equals(diff.getSequences().get(j))){
								difference++;
							}
						}
					}
				}
			}
			if(difference != 0){
				SeqMultiset newDiff = new SeqMultiset();
				for(TokenSeq seq : diff.getSequences()){
					newDiff.addTokenSeq(seq);
				}
				typeTwoDiffList.add(newDiff);
			}
		}
		
		return typeTwoDiffList;
	}

	/**
	 * Create a configuration point for each diff, and take the value in source instance as the copied
	 * token sequence in configuration point. In addition, the value in target instance is removed so that 
	 * it will not be considered in rule generation and association mining.
	 * 
	 * @param sourceInstance
	 * @param targetInstance
	 * @param diffList
	 * @return
	 */
	private CPWrapperList constructPartialConfigurationPoints(
			CloneInstance sourceInstance, CloneInstance targetInstance, ArrayList<SeqMultiset> originalDiffList) {
		
		ArrayList<SeqMultiset> diffList = cloneList(originalDiffList);
		
		ArrayList<CPWrapper> cpList = new ArrayList<>();
		for(SeqMultiset multiset: diffList){
			TokenSeq copiedSeq = null;
			TokenSeq correctSeq = null;
			
			Iterator<TokenSeq> iterator = multiset.getSequences().iterator();
			while(iterator.hasNext()){
				TokenSeq tokenSeq = iterator.next();
				
				mcidiff.model.CloneInstance ins = tokenSeq.getCloneInstance();
				
				if(ins.equals(sourceInstance)){
					copiedSeq = tokenSeq;
				}
				else if(ins.equals(targetInstance)){
					correctSeq = tokenSeq;
					iterator.remove();
				}
			}
			
			ConfigurationPoint point = new ConfigurationPoint(copiedSeq, multiset);
			//TODO check whether it is correct
			point.setModifiedTokenSeq(copiedSeq);
			CPWrapper wrapper = new CPWrapper(point, correctSeq);
			cpList.add(wrapper);
		}
		
		return new CPWrapperList(cpList);
	}
	
	private ArrayList<SeqMultiset> cloneList(ArrayList<SeqMultiset> originalDiffList){
		ArrayList<SeqMultiset> diffList = new ArrayList<>();
		for(SeqMultiset set: originalDiffList){
			SeqMultiset newSet = new SeqMultiset();
			for(TokenSeq seq: set.getSequences()){
				newSet.addTokenSeq(seq);
			}
			diffList.add(newSet);
		}
		
		return diffList;
	}
}
