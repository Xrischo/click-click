import java.io.*;
import java.util.*;

public class BareBonesExtended {
	ArrayList<String> codeBB = new ArrayList<String>(); // code stored line by line
	Map<String, Integer> variables = new HashMap<String, Integer>(); // variables stored with values
	
	// put all acceptable codelines in arraylist split from string
	String instrLine = "clear a; incr a; decr a; while a do; end; if a do; close;";
	String[] instrArr = instrLine.split("(?<=;)\\s"); // positive look behind, if ';' is followed by whitespace
	List<String> instrList = Arrays.asList(instrArr);
	
	private void interpret(int count) {
		String codeLine = "";
		String instruction = "";
		String var = "";
		int value;

		// putting the whole barebones program in a while loop and going line by line
		while (count+1 <= codeBB.size() && (codeLine = codeBB.get(count++)) != null) {
			instruction = getInstr(codeLine); // gets the instruction from the line
			var = getVar(codeLine); // gets the variable name
			
			if (instruction.equals("end")) { // base case for recursion
				return;
			} else if (instruction.equals("close")) { // end of if statement
				continue;
			} else if (instruction.equals("if")) {
				if (conditionCheck(codeLine)) { // if condition in if statement is true, move on with code
					continue;
				} else {
					count = blockJump(count, 0, 1); // else jump to the end of the if block code
				}
			} else if (instruction.equals("while")) { // making the while loop as recursion that ends at "end", then checks again
				while (conditionCheck(codeLine)) {
					interpret(count); // recursion
				}
					count = blockJump(count, 1, 0); // when value == 0 we need to jump to the end
			}
			
			value = getValue(var);
			
			//checking the instruction
			if (instruction.equals("clear")) {
				variables.put(var, 0);
			} else if (instruction.equals("incr")) {
				variables.put(var, value+1); // increment variable by 1 in the arraylist
			} else if (instruction.equals("decr")) {
				variables.put(var, value-1);
			} else {
				System.out.println("Unknown instruction."); // if somehow an unknown instruction slips in but regex game strong lol
			}
		}
	}
	
	private String humanInput() {
		System.out.println("Hello! Welcome to BareBones interpreter!");
		System.out.println("Variables are declared automatically if they don't exist yet");
		System.out.println("Please refer to the following syntax:");
		System.out.println("1. 'incr <var>; - increases value of <var> by 1");
		System.out.println("2. 'decr <var>; - decreases value of <var> by 1");
		System.out.println("3. 'clear <var>; - assigns value of <var> to 0");
		System.out.println("4. 'if (<var1> <==> <var2>) do; - makes an if statement");
		System.out.println("5. 'while (<var1> <==> <var2>) do; - opens a loop");
		System.out.println("6. 'end; - closes last opened loop / if statement; ends program when in none");
		System.out.println("You can add indentation too!");
		System.out.println("");
		System.out.println("Choose a name for your code: ");
		
		Scanner input = new Scanner(System.in);
		String fileName = "src/" + input.nextLine() + ".txt"; // concatenate file directiory
		
		System.out.println("-------------------------");
		System.out.println(fileName + " {");
		
		humanInput(fileName, input);
		return fileName;
	}
	
	private void humanInput(String fileName, Scanner input) {
		String codeLine = "";
		
		try {
			BufferedWriter typer = new BufferedWriter(new FileWriter(fileName));
			int blockLevel = 1; // are we in while loop or main code?
			
			while (blockLevel > 0) {
				codeLine = makeCodeValid(input.nextLine(), input);
				
				// check if we go from one block of code to another
				if (getInstr(codeLine).equals("while")) {
					blockLevel++;
				} else if (getInstr(codeLine).equals("end")) {
					blockLevel--;
				}
				
				// write in file
				typer.write(codeLine);
				typer.newLine();
			}
			typer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	

	// jumping to the end of if (false) or while (false)
	private int blockJump(int count, int loopBlock, int ifBlock) { 
		String check;

		while (loopBlock != 0) { // enter here if we came from while loop
			count++;
			check = codeBB.get(count);
			
			if (getInstr(check).equals("end")) {
				loopBlock--;
			} else if (getInstr(check).equals("while")) {
				loopBlock++;
			}
		}
		
		while (ifBlock != 0) { // enter here if we came from if statement
			count++;
			check = codeBB.get(count);
			
			if (getInstr(check).equals("close")) {
				ifBlock--;
			} else if (getInstr(check).equals("if")) {
				ifBlock++;
			}
		}		
		return count+1;
	}
	
	// check if the code entered is syntactically corect
	private String makeCodeValid(String codeLine, Scanner input) {
		int indentC = 0; //indentation level
		while (Character.isWhitespace(codeLine.charAt(indentC))) {
			indentC++;
		}
		
		// check if the whole line of code is syntax-correct by replacing variable name to a and remove indentation
		while (!instrList.contains(codeLine
				.substring(indentC) //remove indentation
				.replaceAll("(?<!\\)\\s)((?<=\\s)\\w+(?=;))|" // find var in '<instr> var;'
						+ "\\(\\w+\\s*[><]\\s*\\w+\\)|" //check form (var1 >< value)
						+ "\\(\\w+\\s*[=]{2}\\s*\\w+\\)" , "a"))) { //check form (var1 == value) 
			System.out.println("-------------------------");
			System.out.println("Error: Wrong syntax. Last line has been removed from your code file.");
			System.out.println("-------------------------");
			indentC = 0;
			
			codeLine = input.nextLine();
			while (Character.isWhitespace(codeLine.charAt(indentC))) {
				indentC++;
			}
		}
		return codeLine;
	}
	
	// check condition for if statement and while loop
	private boolean conditionCheck(String codeLine) {
		String condition = codeLine.replaceAll(".*\\(|\\).*", "");
		
		if (condition.equals("true")) {
			return true;
		}
		
		int firstVar = getValue(getFirstConVar(condition));
		int secondVar = getValue(getSecondConVar(condition));	
		
		if (condition.matches("\\(*\\w+\\s*[>]\\s*\\w+\\)*")) { // (var1 > var2)
			if (firstVar > secondVar) {
				return true;
			} else {
				return false;
			}
		} else if (condition.matches("\\(*\\w+\\s*[<]\\s*\\w+\\)*")) {
			if (firstVar < secondVar) {
				return true;
			} else {
				return false;
			}
		} else if (condition.matches("\\(*\\w+\\s*[=]{2}\\s*\\w+\\)*")) {
			if (firstVar == secondVar) {
				return true;
			} else {
				return false;
			}
		}
		return false;
	}
	
	// get value of a variable, check if it exists first etc.
	private int getValue(String var) {
		if (var.matches("\\d+")) {
			return Integer.parseInt(var);
		} else if (!variables.containsKey(var)) {
			variables.put(var, 0);
			return 0;
		}
		return variables.get(var);
	}
	
	// get first word (instruction)
	private String getInstr(String codeLine) {
		return codeLine.replaceAll("^\\s*|[^\\w*].*", "");
	}
	
	// get variable from '<instr> <var>;' form
	private String getVar(String codeLine) {
		return codeLine.replaceAll(("^\\s*\\w*\\s|;"), "");
	}
	
	// get first variable from '(x <==> y)' form
	private String getFirstConVar(String condition) {
		return condition.replaceAll("\\(|[^(\\w)].*", "");
	}
	
	// get second variable from '(x <==> y)' form
	private String getSecondConVar(String condition) {
		return condition.replaceAll(".*[<=>]{1,2}\\s*|\\)", "");
	}
	
	// move file to the arraylist on top
	private void fetchFile(String fileDir) {
		String thisLine;
		
		try (BufferedReader in = new BufferedReader(new FileReader(fileDir))) {
			while ((thisLine = in.readLine()) != null) {
				codeBB.add(thisLine);
			}			
		} catch (IOException e) {
			System.out.println(e);
		}
	}
	
	// at the end print every variable and its value
	private void printVar() {
		 variables.forEach((name, value) -> System.out.println(name + " = " + value)); 
	}
	
	public static void main(String[] args) {
		BareBones decode = new BareBones();
		String fileToFetch = decode.humanInput();
		
		decode.fetchFile(fileToFetch);
		decode.interpret(0);
		decode.printVar();
	}
}