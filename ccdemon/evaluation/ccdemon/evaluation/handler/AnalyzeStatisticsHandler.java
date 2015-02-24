package ccdemon.evaluation.handler;

import java.util.ArrayList;

import mcidiff.model.CloneSet;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import ccdemon.evaluation.main.CloneRecoverer;
import ccdemon.evaluation.main.CloneRecoverer.CollectedData;
import ccdemon.evaluation.util.ExcelExporterWithPOI;
import ccdemon.util.CCDemonUtil;
import clonepedia.model.ontology.CloneSets;

public class AnalyzeStatisticsHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		Job job = new Job("analyzing project statistics"){

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				AnalyzeStatisticsHandler handler = new AnalyzeStatisticsHandler();
				handler.run();
				return Status.OK_STATUS;
			}
			
		};
		job.schedule();
		

		return null;
	}
	
	private void run(){
		CloneRecoverer recoverer = new CloneRecoverer();
		CloneSets sets = clonepedia.Activator.plainSets;
		int count = 0;
		//TODO how many times we have run this program
		int globalRunTimeCount = 0;
		//TODO what is the program name
		String projectName = "JHotDraw" + globalRunTimeCount;
		ExcelExporterWithPOI exporter = new ExcelExporterWithPOI();
		exporter.start();
		
		//TODO all ID that already been analyzed
		String analyzedIds = "";
		String[] analyzedIdsArray = analyzedIds.split(",");
		ArrayList<String> analyzedIdsList = new ArrayList<String>();
		for(String id : analyzedIdsArray){
			analyzedIdsList.add(id);
		}
		//TODO how many clone sets in one time
		int limitSetNum = 53;
		int limitCount = 0;
		String thisTimeIds = "";

		for(clonepedia.model.ontology.CloneSet clonepediaSet: sets.getCloneList()){

			System.out.println("--------------------current: " + sets.getCloneList().indexOf(clonepediaSet) + ", total: " + sets.getCloneList().size() + " -----------------------");
			System.out.println("Clone set ID: " + clonepediaSet.getId());
			if(analyzedIdsList.contains(clonepediaSet.getId())){
				continue;
			}else if(limitCount >= limitSetNum){
				break;
			}else{
				thisTimeIds += clonepediaSet.getId() + ",";
				limitCount++;
			}
			
			CloneSet set = CCDemonUtil.adaptMCIDiffModel(clonepediaSet);
			if(set.getInstances().size() < 3){
				continue;
			}
			ArrayList<CollectedData> datas = recoverer.getTrials(set);
			
			for(CollectedData data : datas){
				ArrayList<String> exportList = new ArrayList<String>();
				//cloneSetID
				exportList.add(clonepediaSet.getId());
				//instanceNum
				exportList.add(set.getInstances().size() + "");
				//avgLineNum
				exportList.add(data.getLineNum() + "");
				//typeIIorIII
				exportList.add(data.getTypeIIorIII());
				//type1to7;
				if(data.getRecall() == 1.0){
					if(data.getSavedEditingEffort() == 1.0){
						exportList.add("1");
					}else if(data.getSavedEditingEffort() > 0){
						exportList.add("2");
					}else{
						exportList.add("3");
					}
				}else if(data.getRecall() > 0){
					if(data.getSavedEditingEffort() == 1.0){
						exportList.add("4");
					}else if(data.getSavedEditingEffort() > 0){
						exportList.add("5");
					}else{
						exportList.add("6");
					}
				}else{
					exportList.add("7");
				}
				//recall;
				exportList.add(data.getRecall() + "");
				//precision;
				exportList.add(data.getPrecision() + "");
				//Fmeasure
				exportList.add(data.getfMeature() + "");
				//configurationEffort;
				exportList.add(data.getConfigurationEffort() + "");
				//savedEditingEffort;
				exportList.add(data.getSavedEditingEffort() + "");
				//trialTime;
				exportList.add(data.getTrialTime() + "");
				//diffTime;
				exportList.add(data.getDiffTime() + "");
				//APITime;
				exportList.add(data.getAPITime() + "");
				//cloneInstance;;
				exportList.add(data.getCloneInstance().toString());
				
				exporter.export(exportList, count);
				count++;
				//write excel whenever reach 1000 lines
				/*if(count % 1000 == 0){
					exporter.end(projectName);
					exporter.startAgain(projectName);
				}*/
				System.out.println("current line number: " + count);
			}
			
		}
		
		exporter.end(projectName);
        System.out.println("excel export done");
        System.out.println("thisTimeIds:");
        System.out.println(thisTimeIds);
	}

}
