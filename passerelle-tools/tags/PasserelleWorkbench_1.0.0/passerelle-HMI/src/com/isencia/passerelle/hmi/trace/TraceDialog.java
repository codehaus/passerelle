/*
 * (c) Copyright 2004, iSencia Belgium NV
 * All Rights Reserved.
 * 
 * This software is the proprietary information of iSencia Belgium NV.  
 * Use is subject to license terms.
 */
package com.isencia.passerelle.hmi.trace;

import java.awt.BorderLayout;
import java.awt.HeadlessException;

import javax.swing.JDialog;
import javax.swing.JFrame;

import com.isencia.passerelle.actor.Actor;
import com.isencia.passerelle.domain.cap.Director;

/**
 * @todo Class Comment
 *
 * @author erwin dl
 */
public class TraceDialog extends JDialog implements TraceVisualizer {
    
    private TracePanel tracePanel;
    /**
     * @throws HeadlessException
     */
    public TraceDialog(JFrame owner) throws HeadlessException {
        super(owner,"Trace Messages",false);
        
        tracePanel = new TracePanel();
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(tracePanel, BorderLayout.CENTER);
        setSize(400,600);
        setLocationRelativeTo(owner);
        /*
        // buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout());
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        buttonPanel.add(closeButton);
        
        getContentPane().add(buttonPanel,BorderLayout.SOUTH);
        */
    }
    
    public void trace(Actor source, String message) {
        tracePanel.addTraceMessage(source.getFullNameButWithoutModelName(), message);
    }

    public void trace(Director source, String message) {
        tracePanel.addTraceMessage(source.getName(), message);
    }

	public TracePanel getTracePanel() {
		return tracePanel;
	}

	public void setTracePanel(TracePanel tracePanel) {
		this.tracePanel = tracePanel;
	}
	
	
	
}
