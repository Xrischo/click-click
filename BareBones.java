import java.io.*;
import java.util.*;

public class BareBones {
	ArrayList<String> codeBB = new ArrayList<String>(); // code stored line by line
	Map<String, Integer> variables = new HashMap<String, Integer>(); // variables stored with values
	
	private String humanInput() {
		System.out.println("Hello! Welcome to BareBones interpreter!");
		System.out.println("Please refer to the following syntax:");
		System.out.println("1. 'incr <var>; - increases value of <var> by 1 (declares <var> = 0 if none exists");
		System.out.println("2. 'decr <var>; - decreases value of <var> by 1 (declares <var> = 0 if none exists");
		System.out.println("3. 'clear <var>; - assigns value of <var> to 0 (declares <var> = 0 if none exists");
		System.out.println("4. 'while <var> not 0 do; - opens a loop (declares <var> = 0 if none exists");
		System.out.println("5. 'end; - closes last opened loop, ends program if no loop");
		System.out.println("You can add indentation too!");
		System.out.println("");
		System.out.println("Choose a name for your code: ");
		
		Scanner input = new Scanner(System.in);
		String fileName = "src/" + input.nextLine() + ".txt";
		
		System.out.println("-------------------------");
		System.out.println(fileName + " {");
		
		humanInput(fileName, input);
		return fileName;
	}
	
	private void humanInput(String fileName, Scanner input) {
		// put all acceptable codelines in arraylist
		String instrLine = "clear a; incr a; decr a; while a not 0 do; end;";
		String[] instrArr = instrLine.split("(?<=;)\\s");
		List<String> instrList = new ArrayList<String>();
		instrList = Arrays.asList(instrArr);
		
		String codeLine = "";
		
		try {
			BufferedWriter typer = new BufferedWriter(new FileWriter(fileName));
			int blockLevel = 1; // are we in while loop or main code?
			
			while (blockLevel > 0) {
				codeLine = input.nextLine();
				int indentC = 0;
				while (Character.isWhitespace(codeLine.charAt(indentC))) {
					indentC++;
				}
				
				// check if the whole line of code is syntax-correct by replacing variable name to a and remove indentation
				while (!instrList.contains(codeLine.substring(indentC).replaceAll("(?<=[a-z])\\s\\w+(?=;)|\\s\\w+(?=\\snot)", " a"))) {
					System.out.println("-------------------------");
					System.out.println("Error: Wrong syntax. Last line has been removed from your code file.");
					System.out.println("-------------------------");
					indentC = 0;
					
					codeLine = input.nextLine();
					while (Character.isWhitespace(codeLine.charAt(indentC))) {
						indentC++;
					}
				}
				
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
	
	private void interpret(int count) {
		String codeLine = "";
		String instruction = "";
		String var = "";
		int value;
		
		ArrayList<Integer> loopVar = new ArrayList<Integer>(1); //while loop argument variable, check infinite loop

		//putting the whole barebones program in a while loop and going line by line
		while (count+1 <= codeBB.size() && (codeLine = codeBB.get(count++)) != null) {
			instruction = getInstr(codeLine);
			var = getVar(codeLine);
			
			if (instruction.equals("end")) { // base case for recursion
				return;
			} else if (!variables.containsKey(var)) {
				variables.put(var, 0);
			}
			value = variables.get(var);
			
			//checking the instruction
			if (instruction.equals("clear")) {
				variables.put(var, 0);
			} else if (instruction.equals("incr")) {
				variables.put(var, value+1); // increment variable by 1 in the arraylist
			} else if (instruction.equals("decr")) {
				variables.put(var, value-1);
			} else if (instruction.equals("while")) { // making the while loop as recursion that ends at "end", then checks again
				while (value != 0) {
					if (loopVar.size() == 0) {
						loopVar.add(value);
					} else if (loopVar.get(0) - value <= 0) {
						System.out.println("Infinite loop, faulty code.");
						System.exit(0);
					}
					interpret(count);
				}
					count = loopJump(count); // when value == 0 we need to jump to the end
			} else {
				System.out.println("Unknown instruction.");
			}
		}
	}
	
	// jumping to the end of the loop when var == 0
	private int loopJump(int count) { 
		int equalise = 1; // Number of "while" minus number of "end"
		String check;

		while (equalise != 0) {
			count++;
			check = codeBB.get(count);
			
			if (getInstr(check).equals("end")) {
				equalise--;
			} else if (getInstr(check).equals("while")) {
				equalise++;
			}
		}
		
		return count+1;
	}
	
	//move file to an arraylist
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
	
	private String getInstr(String codeLine) {
		return codeLine.replaceAll("^\\s*|[^\\w*].*", "");
	}
	
	private String getVar(String codeLine) {
		return codeLine.replaceAll(("^\\s*\\w*\\s|;|\\snot.*"), "");
	}
	
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