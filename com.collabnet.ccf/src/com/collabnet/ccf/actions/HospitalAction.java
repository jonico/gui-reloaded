package com.collabnet.ccf.actions;

import java.util.Iterator;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionDelegate;

import com.collabnet.ccf.Activator;
import com.collabnet.ccf.db.CcfDataProvider;
import com.collabnet.ccf.db.Filter;
import com.collabnet.ccf.model.Landscape;
import com.collabnet.ccf.model.MappingGroup;
import com.collabnet.ccf.model.SynchronizationStatus;
import com.collabnet.ccf.views.HospitalView;

public class HospitalAction extends ActionDelegate {
	private IStructuredSelection fSelection;
	
	@SuppressWarnings("unchecked")
	public void run(IAction action) {
		Iterator iter = fSelection.iterator();
		while (iter.hasNext()) {
			Object object = iter.next();
			if (object instanceof Landscape || object instanceof SynchronizationStatus || object instanceof MappingGroup) {
				Landscape landscape = null;
				SynchronizationStatus status = null;
				if (object instanceof SynchronizationStatus) {
					status = (SynchronizationStatus)object;
					landscape = status.getLandscape();
				}
				else if (object instanceof Landscape) {
					landscape = (Landscape)object;
				}
				else if (object instanceof MappingGroup) {
					MappingGroup mappingGroup = (MappingGroup)object;
					landscape = mappingGroup.getProjectMappingsParent().getLandscape();
				}
				try {
					HospitalView.setLandscape(landscape);
					if (null == status) {
						
// TODO: Support for OR filtering.
						
//						Filter sourceSystemFilter1 = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, landscape.getType1(), true, Filter.FILTER_TYPE_LIKE);
//						Filter targetSystemFilter1 = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, landscape.getType2(), true, Filter.FILTER_TYPE_LIKE);
//						
//						Filter[] orGroup1 = { sourceSystemFilter1, targetSystemFilter1 };
//						
//						Filter sourceSystemFilter2 = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, landscape.getType2(), true, Filter.FILTER_TYPE_LIKE);
//						Filter targetSystemFilter2 = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, landscape.getType1(), true, Filter.FILTER_TYPE_LIKE);
//						
//						Filter[] orGroup2 = { sourceSystemFilter2, targetSystemFilter2 };
						
//						Filter[][] filterGroups = { orGroup1, orGroup2 };
						
//						String adaptorName = null;
//						if (landscape.getType1().equals(Landscape.TYPE_PT) || landscape.getType2().equals(Landscape.TYPE_PT)) {
//							adaptorName = Landscape.TYPE_PT;
//						} else {
//							adaptorName = Landscape.TYPE_TF;
//						}
						
//						adaptorName = landscape.getType2();
//						
//						Filter adaptorNameFilter = new Filter(CcfDataProvider.HOSPITAL_ADAPTOR_NAME, adaptorName, true, Filter.FILTER_TYPE_LIKE);
//						Filter[] filters = { adaptorNameFilter };
//						Filter[][] filterGroups = { filters };
						
//						HospitalView.setFilters(filterGroups, true, landscape.getDescription());
						HospitalView.setFilters(null, true, landscape.getDescription());
					} else {
						String sourceSystemKind;
						if (status.isPaused()) sourceSystemKind = status.getSourceSystemKind().substring(0, 2);
						else sourceSystemKind = status.getSourceSystemKind();
						Filter sourceSystemFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_SYSTEM_KIND, sourceSystemKind, true, Filter.FILTER_TYPE_LIKE);
						Filter sourceRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_SOURCE_REPOSITORY_ID, status.getSourceRepositoryId(), true);
						Filter targetSystemFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_SYSTEM_KIND, status.getTargetSystemKind(), true, Filter.FILTER_TYPE_LIKE);
						Filter targetRepositoryFilter = new Filter(CcfDataProvider.HOSPITAL_TARGET_REPOSITORY_ID, status.getTargetRepositoryId(), true);
						Filter[] filters = { sourceSystemFilter, sourceRepositoryFilter, targetSystemFilter, targetRepositoryFilter };
						
						Filter[][] filterGroups = { filters };
						
						HospitalView.setFilters(filterGroups, true, status.toString());
					}
					HospitalView hospitalView = (HospitalView)Activator.getDefault().getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HospitalView.ID);
					hospitalView.refresh();
				} catch (PartInitException e) {
					Activator.handleError(e);
				}
				break;
			}
		}
	}

	public void selectionChanged(IAction action, ISelection sel) {
		if (sel instanceof IStructuredSelection) {
			fSelection= (IStructuredSelection) sel;
		}
	}	
}
