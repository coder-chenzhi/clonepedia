package clonepedia.actions;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

import clonepedia.featuretemplate.TFGBuilder;
import clonepedia.featuretemplate.TMGBuilder;
import clonepedia.model.ontology.CloneSets;
import clonepedia.model.template.TFGList;
import clonepedia.model.template.TMGList;
import clonepedia.model.template.TemplateFeatureGroup;
import clonepedia.model.template.TemplateMethodGroup;
import clonepedia.model.template.TotalTFGs;
import clonepedia.syntactic.util.comparator.LevenshteinPathComparator;
import clonepedia.util.MinerUtil;
import clonepedia.util.Settings;

public class TemplateGenerationAction implements IWorkbenchWindowActionDelegate {

	@Override
	public void run(IAction action) {
		
		Job job = new Job("building template"){
			protected IStatus run(IProgressMonitor monitor) {
				
				CloneSets sets = (CloneSets)MinerUtil.deserialize(Settings.ontologyFile, true);
				sets.setPathComparator(new LevenshteinPathComparator());
				
				TMGBuilder builder = new TMGBuilder(sets);
				builder.build();
				HashSet<TemplateMethodGroup> templateMethodGroupList = builder.getMethodGroupList();
				
				ArrayList<TemplateMethodGroup> list = new ArrayList<TemplateMethodGroup>();
				
				ArrayList<TemplateMethodGroup> sigificantTMGList = new ArrayList<TemplateMethodGroup>();
				for(TemplateMethodGroup tmg: templateMethodGroupList){
					list.add(tmg);
					if(tmg.getMethods().size() > 2){
						sigificantTMGList.add(tmg);
					}
				}
				
				TFGBuilder featureBuilder = new TFGBuilder(list);
				featureBuilder.generateTemplateFeatures();
				TotalTFGs featureGroups = featureBuilder.getFeatureGroups();
				
				ArrayList<ArrayList<TemplateFeatureGroup>> significantGroups = new ArrayList<ArrayList<TemplateFeatureGroup>>();
				for(ArrayList<TemplateFeatureGroup> group: featureGroups){
					if(group.size() > 2){
						significantGroups.add(group);
					}
				}
				
				TMGList tmgList = new TMGList(sets, list);
				//TFGList tfgList = new TFGList(sets, featureGroups);
				
				try {
					MinerUtil.serialize(tmgList, "tmgList");
					MinerUtil.serialize(featureGroups, "totalTFGs");
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				//sets.setTemplateMethodGroup(list);
				//sets.setFeatureGroupList(featureGroups);
				return Status.OK_STATUS;
			}
		};
		
		job.schedule();
		
	}

	@Override
	public void selectionChanged(IAction action, ISelection selection) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public void init(IWorkbenchWindow window) {
		// TODO Auto-generated method stub

	}

}