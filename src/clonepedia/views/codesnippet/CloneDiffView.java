package clonepedia.views.codesnippet;

import java.util.ArrayList;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import clonepedia.Activator;
import clonepedia.java.ASTComparator;
import clonepedia.java.CloneInformationExtractor;
import clonepedia.java.CompilationUnitPool;
import clonepedia.java.model.CloneInstanceWrapper;
import clonepedia.java.model.CloneSetWrapper;
import clonepedia.java.model.DiffCounterRelationGroupEmulator;
import clonepedia.java.model.DiffInstanceElementRelationEmulator;
import clonepedia.java.util.MinerUtilforJava;
import clonepedia.model.ontology.CloneInstance;
import clonepedia.model.ontology.CloneSet;
import clonepedia.model.ontology.ComplexType;
import clonepedia.model.ontology.CounterRelationGroup;
import clonepedia.model.ontology.Method;
import clonepedia.perspective.CloneDiffPerspective;
import clonepedia.util.ImageUI;
import clonepedia.views.DiffPropertyView;
import clonepedia.views.util.ViewUtil;

public class CloneDiffView extends ViewPart {

	private clonepedia.java.model.CloneSetWrapper set;
	private ScrolledComposite scrolledComposite;
	private SashForm sashForm;
	private DiffCounterRelationGroupEmulator relationGroup;
	
	/**
	 * Show which diff to highlight
	 */
	private int diffIndex = -1;
	
	public void setDiffIndex(int diffIndex) {
		this.diffIndex = diffIndex;
	}

	public CloneDiffView() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		this.scrolledComposite = new ScrolledComposite(parent, SWT.H_SCROLL /*| SWT.V_SCROLL*/);
		
		hookActionsOnToolBar();
	}
	
	private void hookActionsOnToolBar(){
		IActionBars actionBars = getViewSite().getActionBars();
		IToolBarManager toolBar = actionBars.getToolBarManager();
		
		Action nextDiffAction = new Action("Check Next Diff"){
			public void run(){
				
				if(set == null) return;
				
				ArrayList<DiffCounterRelationGroupEmulator> list = set.getRelationGroups();
				if(diffIndex < list.size()-1){
					diffIndex++;
				}
				
				if(diffIndex >= 0){	
					if(diffIndex > list.size()-1){
						diffIndex = 0;
					}
					relationGroup = list.get(diffIndex);
				}
				
				showCodeSnippet(relationGroup);
				
				DiffPropertyView propertyViewPart = (DiffPropertyView)getSite().getWorkbenchWindow().getActivePage().findView(CloneDiffPerspective.DIFF_PROPERTY_VIEW);
				if(propertyViewPart != null){
					propertyViewPart.showDiffInformation(relationGroup);
					propertyViewPart.setFocus();
				}
			}
		};
		nextDiffAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageUI.DOWN_ARROW));
		
		Action prevDiffAction = new Action("Check Previous Diff"){
			public void run(){
				
				if(set == null) return;
				
				ArrayList<DiffCounterRelationGroupEmulator> list = set.getRelationGroups();
				if(diffIndex > 0){
					diffIndex--;
				}
				
				if(diffIndex >= 0){					
					relationGroup = list.get(diffIndex);
				}
				
				showCodeSnippet(relationGroup);
				
				DiffPropertyView propertyViewPart = (DiffPropertyView)getSite().getWorkbenchWindow().getActivePage().findView(CloneDiffPerspective.DIFF_PROPERTY_VIEW);
				if(propertyViewPart != null){
					propertyViewPart.showDiffInformation(relationGroup);
				}
			}
		};
		prevDiffAction.setImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(ImageUI.UP_ARROW));
		
		
		toolBar.add(nextDiffAction);
		toolBar.add(prevDiffAction);
	}
	
	public void showCodeSnippet(DiffCounterRelationGroupEmulator relationGroup){
		if(this.set != null){
			showCodeSnippet(this.set, relationGroup);
		}
	}
	
	public void showCodeSnippet(clonepedia.java.model.CloneSetWrapper syntacticCloneSetWrapper, DiffCounterRelationGroupEmulator relationGroup){
		//CompilationUnitPool pool = new CompilationUnitPool();
		//CloneSetWrapper setWrapper = new clonepedia.java.model.CloneSetWrapper(set, pool);
		//this.set = new CloneInformationExtractor().extractCounterRelationalDifferencesWithinSyntacticBoundary(setWrapper);
		this.set = syntacticCloneSetWrapper;
		this.relationGroup = relationGroup;
		/**
		 * If there is no these two statements, the following sash form will not present in UI.
		 */
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);
		
		this.sashForm = new SashForm(scrolledComposite, SWT.HORIZONTAL);
		for(CloneInstanceWrapper instanceWrapper: this.set){
			generateCodeComposite(sashForm, instanceWrapper);
		}
		
		scrolledComposite.setContent(sashForm);
		//scrolledComposite.setMinSize(sashForm.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		scrolledComposite.layout();
	}

	private void generateCodeComposite(Composite parent, CloneInstanceWrapper instanceWrapper){
		
		int overallHeight = parent.getParent().getClientArea().height;
		int overallWidth = parent.getParent().getClientArea().width;
		
		int widgetHeight = overallHeight - 20;
		int widgetWidth = (set.size() <= 4)? (overallWidth/set.size() - 20) : overallWidth/4;
		
		Composite codeComposite = new Composite(parent, SWT.BORDER);
		GridLayout overGridLayout = new GridLayout();
		overGridLayout.numColumns = 1;
		overGridLayout.verticalSpacing = 1;
		overGridLayout.marginLeft = 0;
		overGridLayout.marginRight = 0;
		codeComposite.setLayout(overGridLayout);
		
		Label label = new Label(codeComposite, SWT.NONE);
		GridData labelLayoutData = new GridData(GridData.FILL_HORIZONTAL);
		labelLayoutData.heightHint = 20;
		labelLayoutData.widthHint = widgetWidth;
		/*labelLayoutData.grabExcessHorizontalSpace = true;
		labelLayoutData.grabExcessVerticalSpace = true;*/
		label.setLayoutData(labelLayoutData);
		String ownerName = instanceWrapper.getCloneInstance().getResidingMethod().getOwner().getFullName();
		String methodName = instanceWrapper.getCloneInstance().getResidingMethod().getMethodName();
		label.setText(ownerName + ":" + methodName);
		
		GridData scrollCodeLayoutData = new GridData(GridData.FILL_BOTH);
		scrollCodeLayoutData.heightHint = widgetHeight; 
		scrollCodeLayoutData.widthHint = widgetWidth;
		//scrollCodeLayoutDdata.grabExcessHorizontalSpace = true;
		//scrollCodeLayoutDdata.grabExcessVerticalSpace = true;
		
		/*ScrolledComposite sc = new ScrolledComposite(codeComposite, SWT.H_SCROLL | SWT.V_SCROLL);
		sc.setExpandHorizontal(true);
		sc.setExpandVertical(true);
		sc.setLayoutData(scrollCodeLayoutDdata);*/
		
		/*Composite com = new Composite(sc, SWT.BORDER);
		GridLayout gridLayout = new GridLayout(1, true);
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		com.setLayout(gridLayout);*/
		
		generateCodeText(instanceWrapper, codeComposite, scrollCodeLayoutData);
		
		//sc.setContent(com);
		//sc.setMinSize(com.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		//sc.layout();
		codeComposite.layout();
	}
	
	private void generateCodeText(CloneInstanceWrapper instanceWrapper, Composite parent, GridData scrollCodeLayoutData){
		StyledText text = new StyledText(parent, SWT.V_SCROLL | SWT.H_SCROLL);
		text.setLayoutData(scrollCodeLayoutData);
		
		Menu menu = new Menu(parent);
		MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
		menuItem.setText("Show in java editor");
		text.setMenu(menu);
		
		String content = null;
		
		CloneInstance instance = instanceWrapper.getCloneInstance();
		
		final ICompilationUnit iunit = ViewUtil.getCorrespondingCompliationUnit(instance);
		if(iunit != null){
			
			ASTParser parser = ASTParser.newParser(AST.JLS3);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			parser.setSource(iunit);
			parser.setResolveBindings(true);
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			
			int startClonePosition = cu.getPosition(instance.getStartLine()-1, 0);
			int endClonePosition = cu.getPosition(instance.getEndLine(), 0);
			
			MethodDeclaration methodDeclaration = instanceWrapper.getMethodDeclaration();
			int methodStartPosition = methodDeclaration.getStartPosition();
			int methodEndPosition = methodStartPosition + methodDeclaration.getLength();
			
			try {
				content = iunit.getSource();
			} catch (JavaModelException e) {
				e.printStackTrace();
			}
			
			////content = content.substring(methodStartPosition, methodEndPosition);
			text.setText(content);
			//cloneStyleRange.fontStyle = SWT.BOLD;
			
			/*StyleRange cloneStyleRange = new StyleRange();
			cloneStyleRange.start = startClonePosition - methodStartPosition;
			cloneStyleRange.length = endClonePosition - startClonePosition;
			cloneStyleRange.foreground = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_YELLOW);
			text.setStyleRange(cloneStyleRange);*/
			
			//int startCloneLineNumber = cu.getLineNumber(startClonePosition) - cu.getLineNumber(methodStartPosition);
			//int lineCount =  cu.getLineNumber(endClonePosition) - cu.getLineNumber(startClonePosition);
			
			////int startCloneLineNumber = cu.getLineNumber(startClonePosition) - cu.getLineNumber(methodStartPosition);
			////int lineCount =  cu.getLineNumber(endClonePosition) - cu.getLineNumber(startClonePosition);
			
			final int startCloneLineNumber = cu.getLineNumber(startClonePosition);
			final int lineCount =  cu.getLineNumber(endClonePosition) - cu.getLineNumber(startClonePosition);

			menuItem.addSelectionListener(new SelectionListener() {
				
				@Override
				public void widgetSelected(SelectionEvent e) {
					ViewUtil.openJavaEditorForCloneInstace(iunit, startCloneLineNumber, startCloneLineNumber+lineCount);
				}
				
				@Override
				public void widgetDefaultSelected(SelectionEvent e) {
				}
			});
			
			text.setTopIndex(startCloneLineNumber - 3);
			
			Color disposableColoar = new Color(Display.getCurrent(), 150, 250, 250);
			text.setLineBackground(startCloneLineNumber, lineCount, disposableColoar);
			
			ASTNode doc = methodDeclaration.getJavadoc();
			
			generateKeywordsStyle(text);
			
			if(doc != null){
				generateStyleRangeFromASTNode(text, doc, methodStartPosition, 
						CloneDiffView.DOC_STYLE, 0, false);
			}
			/**
			 * for counter relationally difference
			 */
			for(DiffCounterRelationGroupEmulator relationGroup: this.set.getRelationGroups()){
				for(DiffInstanceElementRelationEmulator relation: relationGroup.getElements()){
					CloneInstanceWrapper referInstance = relation.getInstanceWrapper();
					if(referInstance.equals(instanceWrapper)){
						
						
						
						generateStyleRangeFromASTNode(text, relation.getNode(), 
								methodStartPosition, getDiffStyle(relationGroup), relationGroup.getElements().size(), false);
						
						
					}
				}
			}
			
			/**
			 * for uncounter relationally difference
			 */
			for(ASTNode node: instanceWrapper.getUncounterRelationalDifferenceNodes()){
				generateStyleRangeFromASTNode(text, node, methodStartPosition, 
						CloneDiffView.GAP_DIFF_STYLE, 1, false);
				
			}
			
			/**
			 * for selected counter relational difference
			 */
			if(relationGroup != null){
				for(DiffInstanceElementRelationEmulator relation: relationGroup.getElements()){
					if(relation.getInstanceWrapper().equals(instanceWrapper)){
						ASTNode node = relation.getNode();
						
						generateStyleRangeFromASTNode(text, node,
								methodStartPosition, getDiffStyle(relationGroup), relationGroup.getElements().size(), true);
						
						////text.setTopIndex(cu.getLineNumber(node.getStartPosition()) - cu.getLineNumber(methodStartPosition) - 3);
						////text.setHorizontalIndex(cu.getColumnNumber(node.getStartPosition()) - 20);
						text.setTopIndex(cu.getLineNumber(node.getStartPosition()) - 3);
						text.setHorizontalIndex(cu.getColumnNumber(node.getStartPosition()) - 20);
					}
				}
			}
		}
		
		if(content == null){
			content = instanceWrapper.getMethodDeclaration().toString();
			text.setText(content);
		}
		
		/*GridData gData = new GridData();
		gData.heightHint = 600;
		gData.widthHint = 600;
		text.setLayoutData(gData);*/
	}
	
	private int getDiffStyle(DiffCounterRelationGroupEmulator relationGroup){
		
		ASTNode[] nodeList = new ASTNode[relationGroup.getElements().size()];
		int count = 0;
		for(DiffInstanceElementRelationEmulator relation: relationGroup.getElements()){
			ASTNode node = relation.getNode();
			nodeList[count++] = node;
		}
		
		if(relationGroup.getElements().size() == set.size()){
			return isAllTheElementDifferent(nodeList)? COUNTER_EVEN_DIFF_STYLE : COUNTER_PARTIAL_DIFF_STYLE;
		}
		else{
			return isAllTheElementDifferent(nodeList)? COUNTER_GAP_DIFF_STYPE : COUNTER_GAP_STYLE;
		}
	}
	
	private boolean isAllTheElementDifferent(ASTNode[] nodeList){
		for(int i=0; i<nodeList.length; i++){
			for(int j=i+1; j<nodeList.length; j++){
				if(new ASTComparator().isMatch(nodeList[i], nodeList[j])){
					return false;
				}
			}
		}
		return true;
	}
	
	private void generateKeywordsStyle(StyledText text){
		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
		int style = SWT.BOLD;
		
		String codeText = text.getText();
		
		int startPosition = 0;
		int length = 0;
		
		for(String keyword: this.keywords){
			
			startPosition = codeText.indexOf(keyword);
			
			while(startPosition != -1){
				length = keyword.length();
				
				StyleRange styleRange = new StyleRange();
				////styleRange.start = startNodePosition - methodStartPosition;
				styleRange.start = startPosition;
				styleRange.length = length;
				styleRange.foreground = color;
				styleRange.fontStyle = style;	
				
				text.setStyleRange(styleRange);

				startPosition = codeText.indexOf(keyword, startPosition+length);
			}
			
		}
	}
	
	private String[] keywords = {"package ", "import ", "private ", "public ", "protected ", "class ", "interface ", "new ", 
			"final ", "static ", "int ", "double ", "short ", "long ", "char ", "boolean ", "void ", "instanceof ", 
			"switch", "case", "for(", "for ", "if(", "if ", "else", "while(", "try{", "catch(", "finally",
			"return", "throw", "throws", "null"};
	
	private final static int DOC_STYLE = 1;
	private final static int GAP_DIFF_STYLE = 2;
	//private final static int COUNTER_DIFF_STYLE = 3;
	private static final int COUNTER_EVEN_DIFF_STYLE = 3;
	private static final int COUNTER_PARTIAL_DIFF_STYLE = 4;
	private static final int COUNTER_GAP_STYLE = 5;
	private static final int COUNTER_GAP_DIFF_STYPE = 6;
	
	private void generateStyleRangeFromASTNode(StyledText text, ASTNode node, 
			int methodStartPosition, int codeTextStyle, int relationGroupSize, boolean highlight){
		
		Color color = Display.getCurrent().getSystemColor(SWT.COLOR_GRAY);
		int style = SWT.NORMAL;
		
		switch(codeTextStyle){
		case DOC_STYLE:
			color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
			break;
		case GAP_DIFF_STYLE:
			color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
			break;
		case COUNTER_EVEN_DIFF_STYLE:
			//int s = (relationGroupSize < set.size())? SWT.COLOR_MAGENTA : SWT.COLOR_RED;
			color = Display.getCurrent().getSystemColor(SWT.COLOR_RED);
			break;
		case COUNTER_PARTIAL_DIFF_STYLE:
			color = Display.getCurrent().getSystemColor(SWT.COLOR_MAGENTA);
			break;
		case COUNTER_GAP_STYLE:
			color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
			break;
		case COUNTER_GAP_DIFF_STYPE:
			color = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
			break;
		}
		
		if(highlight){
			style = SWT.BOLD;
		}
		
		
		int startNodePosition = node.getStartPosition();
		int length = node.getLength();
		
		if(MinerUtilforJava.isComplexStatement(node)){
			StyleRange styleRange1 = new StyleRange();
			StyleRange styleRange2 = new StyleRange();
			////styleRange1.start = startNodePosition - methodStartPosition;
			////styleRange2.start = startNodePosition - methodStartPosition + length -1;
			styleRange1.start = startNodePosition;
			styleRange2.start = startNodePosition + length -1;
			
			switch(node.getNodeType()){
			case ASTNode.BLOCK: 
				styleRange1.length = 1;
				break;
			case ASTNode.IF_STATEMENT: 
				styleRange1.length = 2;
				break;
			case ASTNode.FOR_STATEMENT:
				styleRange1.length = 3;
				break;
			case ASTNode.ENHANCED_FOR_STATEMENT:
				styleRange1.length = 3;
				break;
			case ASTNode.WHILE_STATEMENT:
				styleRange1.length = 5;
				break;
			case ASTNode.DO_STATEMENT:
				styleRange1.length = 2;
				break;
			case ASTNode.TRY_STATEMENT:
				styleRange1.length = 3;
				break;
			case ASTNode.SYNCHRONIZED_STATEMENT:
				styleRange1.length = 12;
				break;
			case ASTNode.SWITCH_STATEMENT:
				styleRange1.length = 5;
				break;
			default:
				styleRange1.length = 0;	
			}
			
			styleRange2.length = 1;
			styleRange1.foreground = styleRange2.foreground = color;
			styleRange1.fontStyle = styleRange2.fontStyle = style;
			
			text.setStyleRange(styleRange1);
			text.setStyleRange(styleRange2);
		}
		else{
			StyleRange styleRange = new StyleRange();
			////styleRange.start = startNodePosition - methodStartPosition;
			styleRange.start = startNodePosition;
			styleRange.length = length;
			styleRange.foreground = color;
			styleRange.fontStyle = style;	
			
			text.setStyleRange(styleRange);
		}
	}
	
	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	public clonepedia.java.model.CloneSetWrapper getSet() {
		return set;
	}

	public void setSet(clonepedia.java.model.CloneSetWrapper set) {
		this.set = set;
	}

}
