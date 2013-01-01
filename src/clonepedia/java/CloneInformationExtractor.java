package clonepedia.java;

import java.util.ArrayList;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;

import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.TypeLiteral;


import clonepedia.filepraser.CloneDetectionFileParser;
import clonepedia.filepraser.FileParser;
import clonepedia.businessdata.OntologicalDataFetcher;
import clonepedia.java.ASTComparator;
import clonepedia.java.model.*;
import clonepedia.java.util.MinerUtilforJava;
import clonepedia.java.visitor.CloneASTNodeVisitor;
import clonepedia.java.visitor.CloneASTStatementVisitor;
import clonepedia.model.ontology.CloneInstance;
import clonepedia.model.ontology.CloneSet;
import clonepedia.model.ontology.CloneSets;
import clonepedia.model.ontology.Constant;
import clonepedia.model.ontology.Field;
import clonepedia.model.ontology.Method;
import clonepedia.model.ontology.PrimiType;
import clonepedia.model.ontology.ProgrammingElement;
import clonepedia.model.ontology.Project;
import clonepedia.model.ontology.RegionalOwner;
import clonepedia.model.ontology.Variable;
import clonepedia.util.MinerUtil;
import clonepedia.util.Settings;

public class CloneInformationExtractor {

	private OntologicalDataFetcher fetcher = new OntologicalDataFetcher();
	private Project project;
	private CloneDetectionFileParser cloneFileParser;
	private ArrayList<CloneSetWrapper> setWrapperList = new ArrayList<CloneSetWrapper>();

	public CloneInformationExtractor(CloneDetectionFileParser cloneFileParser, Project project) {
		this.cloneFileParser = cloneFileParser;
		this.project = project;
	}
	
	public CloneInformationExtractor(){}
	
	/**
	 * Extract the structural and diff information of clone sets
	 */
	public void extract() {
		
		CompilationUnitPool pool = new CompilationUnitPool();
		
		CloneSets cloneSets = cloneFileParser.getCloneSets(false, "");
		for (CloneSet cloneSet : cloneSets.getCloneList()) {
			try{
				/**
				 * The following code is for debugging
				 */
				/*if(cloneSet.getId().equals("471532")){
					System.out.print("");
				}
				else
					continue;*/
				
				cloneSet.setProject(project);
				/**
				 * extract structural information
				 */
				CloneSetWrapper setWrapper = new CloneSetWrapper(cloneSet, pool);
				/**
				 * extract diff information
				 */
				if(Settings.diffComparisonMode.equals("ASTNode_Based")){
					setWrapper = extractCounterRelationalDifferencesOfCloneSet(setWrapper);					
				}
				else if(Settings.diffComparisonMode.equals("Statement_Based")){
					setWrapper = extractCounterRelationalDifferencesWithinSyntacticBoundary(setWrapper);
				}
				setWrapperList.add(setWrapper);
				System.out.print("");
			}
			catch(Exception e){
				System.out.println(cloneSet.getId());
				e.printStackTrace();
			}
		}
		
		storeInformation();
	}
	
	/**
	 * A clone set will maintain a common node list represent all the common AST nodes shared
	 * by its instances in order, which is used as a reference list during the algorithm. 
	 * A clone instance will maintain its own instance node list in terms of ordered AST nodes 
	 * representing the cloned code fragment. In diff algorithm, two cursors will be maintained 
	 * for each node list to point to the AST node in comparison. The cursor in common node list 
	 * is called common cursor, while the cursor in instance node list is called instance cursor. 
	 * In order to specify a range in a list, we need two cursors: starting index cursor and ending 
	 * index cursor. They will change in the execution of the diff algorithm.<p>
	 * 
	 * We need to find the differences with counter-relation. Therefore, we need to identify the 
	 * range where the counter-relational differences happen. The reference is the common node list
	 * in clone set and all the instance starting/ending index cursors are identified according to
	 * the common starting/ending index cursor. <p>
	 * 
	 * This method is used to find all the counter-relational differences in a clone set.
	 * @param setWrapper
	 */
	public CloneSetWrapper extractCounterRelationalDifferencesOfCloneSet(CloneSetWrapper setWrapper){
		
		setWrapper.generateCommonListforCloneSetWrapper();
		if (setWrapper.getCommonASTNodeList().length != 0) 
			setWrapper.generateDiffPart();
		
		return setWrapper;
	}

	/*private void filterComplicatedASTNodeforList(ArrayList<ASTNode> list) {
		Iterator<ASTNode> iter = list.iterator();
		while (iter.hasNext()) {
			ASTNode node = (ASTNode) iter.next();
			
			if (!MinerUtilforJava.isConcernedType(node) && !MinerUtilforJava.isBenchMarkType(node))
				iter.remove();
		}
	}*/
	
	public CloneSetWrapper extractCounterRelationalDifferencesWithinSyntacticBoundary(CloneSetWrapper setWrapper){
		setWrapper.generateCommonStatementListforCloneSetWrapper();
		if (setWrapper.getCommonStatementList().length != 0) {
			setWrapper.generateDiffPartWithinSyntacticBoundary(true);			
		}
		else{
			extractCounterRelationalDifferencesOfCloneSet(setWrapper);
		}
		
		return setWrapper;
	}

	private void storeInformation(){
		
		for(CloneSetWrapper setWrapper: setWrapperList){
			CloneSet set = setWrapper.getCloneSet();
			try{
				set.setProject(project);
				
				for(CloneInstanceWrapper instanceWrapper: setWrapper){
					CloneInstance instance = instanceWrapper.getCloneInstance();
					Method residingMethod = MinerUtilforJava.getMethodfromASTNode(instanceWrapper.getMethodDeclaration(), project);
					instance.setResidingMethod(residingMethod);
					fetcher.storeCloneInstanceWithDependency(instance);
				}
				
				for(ASTNode node: setWrapper.getCommonASTNodeList()){
					if(MinerUtilforJava.isConcernedType(node)){
						ProgrammingElement element = transferASTNodesToProgrammingElement(node, set);
						if(null != element) 
							fetcher.storeCommonRelation(set, element);
						System.out.print("");
					}
				}
				
				for(DiffCounterRelationGroupEmulator group: setWrapper.getRelationGroups()){
					String groupId = group.getId();
					for(DiffInstanceElementRelationEmulator relation: group.getRelations()){
						ASTNode node = relation.getNode();
						if(MinerUtilforJava.isConcernedType(node) /*|| node.getNodeType() == ASTNode.PRIMITIVE_TYPE*/){
							ProgrammingElement element = transferASTNodesToProgrammingElement(node, set);
							CloneInstanceWrapper instanceWrapper = relation.getInstanceWrapper();
							CloneInstance instance = instanceWrapper.getCloneInstance();
							Method residingMethod = MinerUtilforJava.getMethodfromASTNode(instanceWrapper.getMethodDeclaration(), project);
							instance.setResidingMethod(residingMethod);
							if(null != element)
								fetcher.storeDiffRelation(groupId, instance, element);
							//System.out.print("");
						}
					}
				}
			}
			catch(Exception e){
				System.out.println(setWrapper.getId());
				e.printStackTrace();
			}
			
		}
	}

	private ProgrammingElement transferASTNodesToProgrammingElement(ASTNode node, RegionalOwner owner) throws Exception{
		
		if(MinerUtilforJava.isConcernedType(node)){
			if(node.getNodeType() == ASTNode.PRIMITIVE_TYPE){
				PrimitiveType primitiveType = (PrimitiveType)node;
				PrimiType primiType = new PrimiType(primitiveType.toString());
				return primiType;
			}
			else if(node.getNodeType() == ASTNode.STRING_LITERAL ||
					node.getNodeType() == ASTNode.NUMBER_LITERAL ||
					node.getNodeType() == ASTNode.CHARACTER_LITERAL ||
					node.getNodeType() == ASTNode.BOOLEAN_LITERAL){
				
				String typeName = ASTNode.nodeClassForType(node.getNodeType()).getSimpleName();
				typeName = typeName.substring(0, typeName.length()-7);
				Constant constant = new Constant(MinerUtilforJava.getConcernedASTNodeName(node), new PrimiType(typeName), false);
				return constant;
			}
			else if(node.getNodeType() == ASTNode.TYPE_LITERAL){
				TypeLiteral type = (TypeLiteral)node;
				if(type.getType().resolveBinding().isClass() || type.getType().resolveBinding().isInterface()){
					return MinerUtilforJava.transferTypeToComplexType(type.getType(), project, (CompilationUnit)node.getRoot());	
				}
				else return null;
			}
			else {
				SimpleName name = (SimpleName)node;
				
				if(name.resolveBinding() == null)
					return null;
				
				if(name.resolveBinding().getKind() == IBinding.TYPE){
					ITypeBinding typeBinding = (ITypeBinding)name.resolveBinding();
					if(typeBinding.isClass() || typeBinding.isInterface()){
						return MinerUtilforJava.transferTypeToComplexType(typeBinding, project, (CompilationUnit)node.getRoot());	
					}
					else return null;
				}
				else if(name.resolveBinding().getKind() == IBinding.METHOD){
					IMethodBinding methodBinding = (IMethodBinding) name.resolveBinding();
					Method m = MinerUtilforJava.getMethodfromBinding(methodBinding, project, (CompilationUnit)node.getRoot());
					Method method = fetcher.getTheExistingMethodorCreateOne(m);
					return method;
				}
				else if(name.resolveBinding().getKind() == IBinding.VARIABLE){
					IVariableBinding variableBinding = (IVariableBinding) name.resolveBinding();
					if(variableBinding.isField()){
						Field f = MinerUtilforJava.getFieldfromBinding(variableBinding, project, (CompilationUnit)node.getRoot());
						if(f.getOwnerType() != null){
							Field field = fetcher.getTheExistingFieldorCreateOne(f);
							return field;
						}
						else{
							Variable v = MinerUtilforJava.getVariablefromBinding(name, variableBinding, project, (CompilationUnit)node.getRoot());
							v.setOwner(owner);
							
							return v;
						}
					}
					else {
						Variable v = MinerUtilforJava.getVariablefromBinding(name, variableBinding, project, (CompilationUnit)node.getRoot());
						v.setOwner(owner);
						
						return v;
					}
				}
				return null;
			}
		}
		else return null;
	}
	
	

	

	public Project getProject() {
		return project;
	}

	public void setProject(Project project) {
		this.project = project;
	}

}
