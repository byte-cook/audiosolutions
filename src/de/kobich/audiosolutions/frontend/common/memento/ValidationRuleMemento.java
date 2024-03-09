package de.kobich.audiosolutions.frontend.common.memento;

import java.util.ArrayList;
import java.util.List;

import de.kobich.audiosolutions.frontend.common.ui.ValidationDialog;
import de.kobich.audiosolutions.frontend.common.util.ConverterUtils;
import de.kobich.commons.misc.validate.rule.AutoNumberingRule;
import de.kobich.commons.misc.validate.rule.DigitRule;
import de.kobich.commons.misc.validate.rule.ForbiddenTextsRule;
import de.kobich.commons.misc.validate.rule.IValidationRule;
import de.kobich.commons.misc.validate.rule.LetterRule;
import de.kobich.commons.misc.validate.rule.LowerCaseRule;
import de.kobich.commons.misc.validate.rule.SentenceCaseRule;
import de.kobich.commons.misc.validate.rule.StartCaseRule;
import de.kobich.commons.misc.validate.rule.UpperCaseRule;
import de.kobich.commons.ui.memento.IMementoItem;
import de.kobich.commons.ui.memento.IMementoItemSerializer;
import de.kobich.commons.ui.memento.IMementoItemSerializer2;

/**
 * Saves/Restores validation rules.
 */
public class ValidationRuleMemento implements IMementoItemSerializer<List<IValidationRule>>, IMementoItemSerializer2<List<IValidationRule>> {
	private static final String RULE_POSTFIX = "-Rule";
	private static final String FORBIDDEN_RULE_POSTFIX = "-ForbiddenRule";
	private static final IValidationRule[] allValidationRules = new IValidationRule[] {new LowerCaseRule(), new UpperCaseRule(), new StartCaseRule(), new SentenceCaseRule(),
			new DigitRule(), new LetterRule(), new ForbiddenTextsRule(null), new AutoNumberingRule(1, 1)};
	private final String state;
	
	/**
	 * @param state
	 */
	public ValidationRuleMemento(String state) {
		this.state = state;
	}

	/**
	 * @return the state
	 */
	public String getState() {
		return state;
	}

	@Override
	public void restore(List<IValidationRule> rules, IMementoItem mementoItem) {
		rules.addAll(restore(mementoItem));
	}

	@Override
	public void save(List<IValidationRule> rules, IMementoItem mementoItem) {
		String ruleValue = "";
		for (IValidationRule rule : rules) {
			ruleValue += rule.getName() + ",";
			if (rule instanceof ForbiddenTextsRule) {
				ForbiddenTextsRule r = (ForbiddenTextsRule) rule;
				String forbidden = ConverterUtils.convert2String(r.getForbiddenTexts(), ValidationDialog.SEPARATOR);
				mementoItem.putString(state + FORBIDDEN_RULE_POSTFIX, forbidden);
			}
		}
		mementoItem.putString(state + RULE_POSTFIX, ruleValue);
	}

	@Override
	public List<IValidationRule> restore(IMementoItem mementoItem) {
		String ruleValue = mementoItem.getString(state + RULE_POSTFIX, "");
		List<IValidationRule> rules = new ArrayList<IValidationRule>();
		for (IValidationRule rule : allValidationRules) {
			if (ruleValue.contains(rule.getName())) {
				if (rule instanceof ForbiddenTextsRule) {
					String forbidden = mementoItem.getString(state + FORBIDDEN_RULE_POSTFIX, "");
					rules.add(new ForbiddenTextsRule(ConverterUtils.convert2StringArray(forbidden, ValidationDialog.SEPARATOR)));
				}
				else {
					rules.add(rule);
				}
			}
		}
		return rules;
	}
}
