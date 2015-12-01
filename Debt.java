import java.io.*;
import java.util.StringTokenizer;
import java.util.Scanner;
import alex.struct.KeySet;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Debt {
	
	public static void main(String[] args) {
		if (args.length < 2) return;
		
		String input = args[0];		// path to input file
		String output = args[1];	// path to output file
		optimise(input, output);
	}
	
	private static void optimise(String input, String output) {
		try {
			
			// Set up file reading
			BufferedReader br = new BufferedReader(new FileReader(input));
			StringTokenizer words;
			String line = br.readLine();
			
			// Get input
			KeySet<String, Party> P = new KeySet<String, Party>(6);
			KeySet<String, Group> G = new KeySet<String, Group>(2);
			
			while (line != null) {
				if (!line.isEmpty() && (line.charAt(0) != '#')) {  // ignore empty lines and comments
					
					// Get line tokens
					
					
					if (line.charAt(0) == '@') {
						
						words = new StringTokenizer(line, "@ \t\n\r");		// get tokens
						String gName = words.nextToken();						// get group name
						G.add(gName, new Group());									// add new group
						
						while (words.hasMoreTokens()) {							// while more parties need to be added to the group
							String pName = words.nextToken();					// get party name
							P.add(pName, new Party(pName));						// add party to party set
							G.getElement(gName).add(P.getElement(pName));	// add that party to the group
						}
						
					} else {
						
						words = new StringTokenizer(line, " \t\n\r");
						String debtor = words.nextToken();
						double value = Double.parseDouble(words.nextToken());
						String lender = words.nextToken();
						
						Group g = G.getElement(debtor);
						if (g != null) {							// if debtor is a group
							double gValue = value/g.size();	// value split between group
							g.increaseDebt(gValue);				// increase debt of group members
						} else {
							P.add(debtor, new Party(debtor));
							P.getElement(debtor).increaseDebt(value);
						}
						
						P.add(lender, new Party(lender));
						P.getElement(lender).decreaseDebt(value);
						
					}
				}
				
				line = br.readLine();	// continue
			}
			br.close();
			
			// Set up file writing
			BufferedWriter bw = new BufferedWriter(new FileWriter(output));
			
			// Get output
			Transaction[] T = new Transaction[P.size() - 1];
		
			for (int i = 0; i < T.length; i++) {
				// Find min and max debt indicies
				int max = 0;
				int min = 0;
				for (int j = 1; j < P.size(); j++) {
					if (P.getElement(j).getDebt() > P.getElement(max).getDebt()) max = j;
					if (P.getElement(j).getDebt() < P.getElement(min).getDebt()) min = j;
				}
				
				double pay = P.getElement(max).getDebt();
				T[i] = new Transaction(P.getElement(max), pay, P.getElement(min));
				
				P.getElement(min).increaseDebt(pay);
				P.getElement(max).decreaseDebt(pay);
			}
			
			// Give output
			for (int i = 0; i < T.length; i++) {
				if (!T[i].isTrivial()) {
					String s = T[i].toString();
					bw.write(s, 0, s.length());
					bw.newLine();
				}
			}
			bw.close();
			
			System.out.printf("%nAn optimal set of transactions has been placed in " + output + "%n");
			
		} catch (FileNotFoundException e) {
			System.out.printf("File %s not found!", input);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static void usage() {
		System.out.printf("Simplifies a set of debts to a minimal set of transactions.%n%n");
		System.out.printf("Debt [-d dir] [file]%n");
		System.out.printf("  -d		Specify the directory containing input and output files.%n");
		System.out.printf("  file		Input file name.%n");
	}
	
}

interface Payable {
	
	public void increaseDebt(double value);
	public void decreaseDebt(double value);
	
}

class Group implements Payable {
	
	private KeySet<String, Party> P;
	
	public Group() {
		P = new KeySet<String, Party>(6);
	}
	
	public boolean add(Party p) {
		return P.add(p.getName(), p);
	}
	
	public void increaseDebt(double value) {
		for (int i = 0; i < P.size(); i++) {
			P.getElement(i).increaseDebt(value);
		}
	}
	
	public void decreaseDebt(double value) {
		increaseDebt(-value);
	}
	
	public int size() {
		return P.size();
	}
	
}

class Party implements Payable {
	
	private String name;
	private double debt;
	
	public Party(String id) {
		name = id;
	}
	
	public double getDebt() {
		return debt;
	}
	
	public void increaseDebt(double value) {
		debt += value;
	}
	
	public void decreaseDebt(double value) {
		increaseDebt(-value);
	}
	
	public String getName() {
		return name;
	}
	
	public String toString() {
		return name + " (" + String.format("%.2f", debt) + ")";
	}
	
}

class Transaction {
	
	private Party debtor;
	private Party lender;
	private double value;
	
	public Transaction(Party p0, double v, Party p1) {
		debtor = p0;
		value = v;
		lender = p1;
	}
	
	public boolean isTrivial() {
		return value == 0;
	}
	
	public String toString() {
		return debtor.getName() + ": " + String.format("%.2f", value) + " -> " + lender.getName();
	}
	
}
