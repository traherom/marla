/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package r;

import problem.DataColumn;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;

/**
 *
 * @author Andrew Sterling
 */
public class OperationSummary extends problem.Operation
{

	private Rengine re;
	private REXP exp;
	private String storedName;
	private double[] storedData;
	private double[] resultData;
	private DataColumn storedColumn;

	public OperationSummary()
	{
		super("Summary");
		re = new Rengine(null, false, new RInterface());
	}

	//@Override
	@Override
	public DataColumn calcColumn(int index)
	{

		storedColumn = parent.getColumn(index);

		DataColumn out = new DataColumn("Summary");

		Double[] temp = (Double[]) storedColumn.toArray();

		//casts array to double
		for(int i = 0; i < storedColumn.size(); i++)
		{
			storedData[i] = temp[i].doubleValue();
		}

		//does operation
		storedName = storedColumn.getName();
		re.assign(storedName, storedData);
		exp = re.eval("summary(" + storedName + ")");

		//throw results from exp into the local column
		resultData = exp.asDoubleArray();

		if(!storedColumn.isEmpty())
		{
			storedColumn.clear();
		}
		for(int i = 0; i < resultData.length; i++)
		{
			out.add((Double) resultData[i]);
		}
		out.setName("min, 1stQ, median, mean, 3rdQ, max");

		//operation via the Rengine.
		//this.ischanged?? will check all the way up if "go" is hit, and recalculate
		return out;
	}
}
