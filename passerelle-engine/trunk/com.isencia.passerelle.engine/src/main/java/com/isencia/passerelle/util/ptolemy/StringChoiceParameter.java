package com.isencia.passerelle.util.ptolemy;

import java.util.List;

import ptolemy.data.expr.StringParameter;
import ptolemy.kernel.util.IllegalActionException;
import ptolemy.kernel.util.NameDuplicationException;
import ptolemy.kernel.util.NamedObj;

public class StringChoiceParameter extends StringParameter {

	public enum CHOICE_TYPE {SINGLE, MULTI}

	private IAvailableChoices availableChoices;
	private IAvailableChoices choiceType;;
	
	public StringChoiceParameter(final NamedObj    container, 
								final String       name,
								final List<String> choices,
								final CHOICE_TYPE  type) throws IllegalActionException, NameDuplicationException {
		this(container, name, new IAvailableChoices() {
			
			@Override
			public String[] getChoices() {
				return choices.toArray(new String[choices.size()]);
			}
		}, type);
	}
	public StringChoiceParameter(final NamedObj          container, 
			                     final String            name,
			                     final IAvailableChoices choices,
			                     final CHOICE_TYPE type) throws IllegalActionException, NameDuplicationException {
		super(container, name);
		this.availableChoices = choices;
		this.choiceType       = choices;
	}

	/**
	 * Interface used here so that can change choices as needed.
	 */
	public String[] getChoices() {
		return availableChoices.getChoices();
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 4876127320162014865L;

	public IAvailableChoices getAvailableChoices() {
		return availableChoices;
	}

	public void setAvailableChoices(IAvailableChoices availableChoices) {
		this.availableChoices = availableChoices;
	}

	public IAvailableChoices getChoiceType() {
		return choiceType;
	}

	public void setChoiceType(IAvailableChoices choiceType) {
		this.choiceType = choiceType;
	}

	public String[] getValue() {
		final String    expr = getExpression();
		if (expr == null || "".equals(expr)) return null;
		final String [] vals = expr.split(","); // , in string value not supported yet
		for (int i = 0; i < vals.length; i++) {
			vals[i] = vals[i].trim();
		}
		return vals;
	}

}
