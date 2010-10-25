/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package r;


import problem.DataColumn;
import problem.Operation;
import org.rosuda.JRI.Rengine;
import org.rosuda.JRI.REXP;

/**
 *
 * @author Andrew Sterling
 */
public class OperationSummary extends problem.Operation {

	private Rengine re;
	private REXP exp;
	private String storedName;
	private double[] storedData;
	private double[] resultData;
	private DataColumn storedColumn;


	public Summary() {
		super("Summary");
		re=new Rengine(null, false, new RInterface());
	}

	public Summary(String name, double[] data) {
		super("Summary");
		re=new Rengine(null, false, new RInterface());
		storedName = name;
		storedData = data;
	}

	//@Override
	//Takes a string??
	@Override
	public DataColumn calcColumn(int index)
	{
		DataColumn c = new DataColumn("Summary");

		Double[] temp = (Double[]) c.toArray();

		//casts array to double
		for(int i = 0; i < c.size(); i++) {
			storedData[i] = temp[i].doubleValue();
		}

		//does operation
		storedName = c.getName();
		re.assign(storedName, storedData);
		exp = re.eval("summary(" + storedName + ")");

		//throw results from exp into the local column
		resultData = exp.asDoubleArray();
		
		if(!storedColumn.isEmpty()) {
			storedColumn.clear();
		}
		for(int i=0; i<resultData.length; i++) {
			storedColumn.add((Double)resultData[i]);
		}

		//operation via the Rengine.
		//this.ischanged?? will check all the way up if "go" is hit, and recalculate
		return storedColumn;
	}
}
