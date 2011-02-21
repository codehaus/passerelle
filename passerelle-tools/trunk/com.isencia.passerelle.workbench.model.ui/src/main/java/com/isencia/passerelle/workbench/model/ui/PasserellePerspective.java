package com.isencia.passerelle.workbench.model.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;
import org.eclipse.ui.console.IConsoleConstants;

public class PasserellePerspective implements IPerspectiveFactory {
	
	public static final String ID = "com.isencia.passerelle.workbench.model.ui.perspective";


	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(true);
				
		IFolderLayout navigatorFolder = layout.createFolder("navigator-folder", IPageLayout.LEFT, 0.3f, editorArea);
		navigatorFolder.addView("org.eclipse.ui.navigator.ProjectExplorer");
        
        IFolderLayout outlineFolder = layout.createFolder("outline-folder",IPageLayout.BOTTOM,0.6f,"navigator-folder");
        outlineFolder.addView(IPageLayout.ID_OUTLINE);
        
        IFolderLayout viewFolder = layout.createFolder("view-folder",IPageLayout.BOTTOM,0.6f,editorArea);
        // This view is specific to passerelle and is simpler and nicer to use than standard properties view.
        viewFolder.addView("com.isencia.passerelle.workbench.model.editor.ui.views.ActorAttributesView");
        viewFolder.addView(IConsoleConstants.ID_CONSOLE_VIEW);
//        viewFolder.addView(IPageLayout.ID_PROBLEM_VIEW);
        
        // Ensure that the run menu is visible
        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        
	}
}
