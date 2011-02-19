/*
 * The maRla Project - Graphical problem solver for statistics and probability problems.
 * Copyright (C) 2010 Cedarville University
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package operation;

import java.util.List;
import java.util.Random;
import java.util.Scanner;
import operation.OperationInformation.PromptType;
import problem.DataSet;
import problem.MarlaException;
import r.RProcessor;
import r.RProcessor.RecordMode;
import resource.Configuration;

/**
 * @author Ryan Morehart
 */
public class OperationTester
{
	/**
	 * Provides a command-line way to easily run a single operation and see results
	 */
	public static void main(String[] args) throws Exception
	{
		Configuration.load();
		RProcessor proc = RProcessor.getInstance();
		
		try
		{
			Scanner tty = new Scanner(System.in);

			// Create test dataset from a CSV
			DataSet testData = null;
			RecordMode prevMode = proc.setDebugMode(RecordMode.DISABLED);
			if(args.length < 2)
				testData = DataSet.importFile("test.csv");
			else
				testData = DataSet.importFile(args[1]);
			proc.setDebugMode(prevMode);

			// Choose operation
			Operation op = null;
			if(args.length < 3)
			{
				System.out.println("Available operations: ");
				for(String opName : Operation.getAvailableOperationsList())
				{
					System.out.println("  " + opName);
				}

				System.out.print("Type name of op: ");
				op = Operation.createOperation(tty.nextLine());
			}
			else
			{
				op = Operation.createOperation(args[2]);
			}

			// Add to data
			testData.addOperation(op);

			// Show data
			System.out.println("----- INCOMING DATA ------");
			System.out.println(testData.toString());
			System.out.println("--------------------------\n");

			// Fill data if needed
			if(op.isInfoUnanswered())
				fillRequiredInfo(op);

			// Fix cache (force so that we can show the full R log
			proc.setDebugMode(RecordMode.FULL);
			System.out.println("------- Debug Log --------");
			op.checkCache();
			System.out.println("--------------------------\n");
			proc.setDebugMode(RecordMode.DISABLED);
			
			// Run against it
			System.out.println("------- R commands -------");
			System.out.println(op.getRCommands(true));
			System.out.println("--------------------------\n");

			// Final result
			System.out.println("------- RESULT DATA -------");
			System.out.println(op.toString());
			System.out.println("---------------------------\n");
		}
		finally
		{
			proc.close();
		}
	}

	/**
	 * Automatically fills required information as best as possible
	 * @param op Operation to answer questions to
	 */
	public static void fillRequiredInfo(Operation op) throws MarlaException
	{
		List<OperationInformation> info = op.getRequiredInfoPrompt();

		// Fill with some BS. Not every operation is nicely handled with this approach
		// if it actually uses the data we may not have much fun (tests will fail)
		Random rand = new Random();
		for(OperationInformation question : info)
		{
			PromptType questionType = question.getType();
			switch(questionType)
			{
				case CHECKBOX:
					question.setAnswer(true);
					break;

				case NUMERIC:
					// Get a random number within the limits
					Double min = (Double)((OperationInfoNumeric)question).getMin();
					Double max = (Double)((OperationInfoNumeric)question).getMax();

					// Limit it some to what people will reasonably use
					min = min > -1000 ? min : -1000;
					max = max < 1000 ? max : 1000;

					question.setAnswer(rand.nextDouble() * (max - min) - min);
					break;

				case STRING:
					question.setAnswer("test string");
					break;

				case COMBO:
				case COLUMN:
					// Choose one of the values they offered us
					question.setAnswer(((OperationInfoCombo)question).getOptions().get(0));
					break;

				default:
					throw new MarlaException("Question type '" + questionType + "' not supported for filling yet");
			}
		}
	}

	/**
	 * Prevent class from being instantiated
	 */
	private OperationTester()
	{
	}
}