package org.streampipes.pe.processors.esper.filter.numerical;

import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.soda.Expression;
import com.espertech.esper.client.soda.Expressions;
import com.espertech.esper.client.soda.FilterStream;
import com.espertech.esper.client.soda.FromClause;
import com.espertech.esper.client.soda.SelectClause;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.streampipes.pe.processors.esper.util.NumericalOperator;
import org.streampipes.wrapper.esper.EsperEventEngine;

import java.util.ArrayList;
import java.util.List;


public class NumericalFilter extends EsperEventEngine<NumericalFilterParameter>{
	
	private static final Logger logger = LoggerFactory.getLogger(NumericalFilter.class.getSimpleName());

	protected List<String> statements(final NumericalFilterParameter params) {
		
		List<String> statements = new ArrayList<String>();
		
		EPStatementObjectModel model = new EPStatementObjectModel();
		//model.insertInto(new InsertIntoClause(fixEventName(params.getOutName()))); // out name
		model.selectClause(SelectClause.createWildcard());
		model.fromClause(new FromClause().add(FilterStream.create(fixEventName(params.getInputStreamParams().get(0).getInName())))); // in name
		
		Expression numericalFilter = null;
		if (params.getNumericalOperator() == NumericalOperator.GE)
			numericalFilter = Expressions.ge(convert(params.getFilterProperty()), Expressions.constant(params.getThreshold()));
		if (params.getNumericalOperator() == NumericalOperator.LE)
			numericalFilter = Expressions.le(convert(params.getFilterProperty()), Expressions.constant(params.getThreshold()));
		if (params.getNumericalOperator() == NumericalOperator.LT)
			numericalFilter = Expressions.lt(convert(params.getFilterProperty()), Expressions.constant(params.getThreshold()));
		if (params.getNumericalOperator() == NumericalOperator.GT)
			numericalFilter = Expressions.gt(convert(params.getFilterProperty()), Expressions.constant(params.getThreshold()));
		if (params.getNumericalOperator() == NumericalOperator.EQ)
			numericalFilter = Expressions.eq(convert(params.getFilterProperty()), Expressions.constant(params.getThreshold()));

		model.whereClause(numericalFilter);
		logger.info("Generated EPL: " +model.toEPL());
		
		statements.add(model.toEPL());
		return statements;
		
	}
	
	private Expression convert(String property)
	{
		//return Expressions.property(property);
		return Expressions.cast(Expressions.property(property), "double");
		//return Expressions.cast(property, "double");
	}
}
