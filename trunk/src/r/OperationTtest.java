/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package r;


import problem.DataColumn;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;
import problem.CalcException;

/**
 *
 * @author Andrew
 */
public class OperationTtest extends problem.Operation {

	private Rengine re;
	private REXP exp;
	private String storedName;
	private DataColumn storedColumn;

	public OperationTtest() {
		super("Ttest");
		re = new Rengine(new String[]{"--no-save"}, false, new RInterface());

	}

	@Override
	public DataColumn calcColumn(int index) throws CalcException
	{
		storedColumn = parent.getColumn(index);

		DataColumn out = new DataColumn("Ttest");

		Double[] temp = new Double[storedColumn.size()];
		storedColumn.toArray(temp);

		double[] storedData = new double[storedColumn.size()];

		//casts array to double
		for(int i = 0; i < storedColumn.size(); i++)
		{
			storedData[i] = temp[i].doubleValue();
		}


		//does operation
		storedName = "ttest";
		re.assign(storedName, storedData);
		exp = re.eval("t.test(" + storedName + ")");

		String[] resultData = exp.asStringArray();
		for(int i=0; i<resultData.length; i++) {
			out.add(Double.parseDouble(resultData[i]));
		}
		out.setName("ttest");

		re.end();
		return out;
	}
}
