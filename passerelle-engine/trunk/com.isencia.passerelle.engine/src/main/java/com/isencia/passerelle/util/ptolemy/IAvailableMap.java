package com.isencia.passerelle.util.ptolemy;

import java.util.Map;

public interface IAvailableMap {
	
	public Map<String,String> getMap();
	
	/**
	 * If the keys are mapped, this will return something not null.
	 * @return
	 */
	public Map<String,String> getVisibleKeyChoices();

}
