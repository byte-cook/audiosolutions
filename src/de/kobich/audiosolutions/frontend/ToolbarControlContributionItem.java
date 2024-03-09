package de.kobich.audiosolutions.frontend;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.menus.WorkbenchWindowControlContribution;

// not yet used
public class ToolbarControlContributionItem extends WorkbenchWindowControlContribution {

	@Override
	protected Control createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout(3, false);
        composite.setLayout(layout);
        
        Action scanAction = new Action("Scan Host") {
            @Override
            public void run() {
            	
            }
        };
        
        ToolBarManager manager = new ToolBarManager();
        manager.add(scanAction);
        
        ToolBar toolBar = manager.createControl(parent);
        return toolBar;
        
//		Text text = new Text(composite, SWT.SINGLE | SWT.BORDER);
//		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
//		data.widthHint = 350;
//		text.setLayoutData(data);
//		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
//		Point p = text.getSize();
//		composite.setSize(320, p.y); // try 1 - doesn't help
//		p = text.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//		System.out.println("text width=" + p.x + " height=" + p.y);
//		p = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
//                composite.setSize(350, p.y); // try 2 - doesn't help
//		System.out.println("Composite width=" + p.x + " height=" + p.y);
//		return composite;
	}

}
