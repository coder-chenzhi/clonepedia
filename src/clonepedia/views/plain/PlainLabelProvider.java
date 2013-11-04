package clonepedia.views.plain;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import clonepedia.model.ontology.CloneInstance;
import clonepedia.model.ontology.CloneSet;
@Deprecated
public class PlainLabelProvider implements ILabelProvider {

	@Override
	public void addListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isLabelProperty(Object element, String property) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void removeListener(ILabelProviderListener listener) {
		// TODO Auto-generated method stub

	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof CloneSet)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
		else if (element instanceof CloneInstance)
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);

		return null;
	}

	@Override
	public String getText(Object element) {
		if (element instanceof CloneSet){
			CloneSet set = (CloneSet) element;
			return set.getId() + "<" + set.size() + ">";
		}
		else if (element instanceof CloneInstance){
			CloneInstance instance = (CloneInstance)element;
			return instance.toString();
		}
		return null;
	}

}
