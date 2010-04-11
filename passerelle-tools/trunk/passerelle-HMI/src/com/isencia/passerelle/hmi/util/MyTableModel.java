package com.isencia.passerelle.hmi.util;

import javax.swing.table.DefaultTableModel;

public class MyTableModel extends DefaultTableModel 
{
	public MyTableModel(String[] string, int rowCount)
	{
		super(string,rowCount);
	}
	
	public Class getColumnClass(int c) {
		return getValueAt(0, c).getClass();
	}
	
	public boolean isCellEditable(int row, int col) 
	{
		//Note that the data/cell address is constant,
		//no matter where the cell appears onscreen.
		
		if (col < 1) 
		{
			return false;
		} 
		else 
		{
			return true;
		}
		
	} 
}