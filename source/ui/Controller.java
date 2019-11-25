package ui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import utils.DBConnection;

public class Controller {

	public static void main(String[] args) {

		DBConnection dbconnection = new DBConnection();
		String userInput = new String();
		BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

		printMenu();
		while (!userInput.equals("x")) {

			print("what do you want to do? ");
			
			try {
				userInput = reader.readLine();
			} catch (IOException ex) {
				ex.printStackTrace();
				userInput = "x";
			}

			if (userInput.equals("x")) {
				println("good bye");
			} else if (userInput.equals("1")) {
				printSection();
				try {
					dataGenerationDialog(dbconnection, reader);
				}
				catch(ParseException ex) {
					ex.printStackTrace();
				} catch (IOException ex) {
					ex.printStackTrace();
				}
			} else if (userInput.equals("2")) {
				// TODO watermark embedding
			} else if (userInput.equals("3")) {
				// TODO watermark detection	
			} else if (userInput.equals("4")) {
				// TODO attack evaluation
			} else if (userInput.equals("5")) {
				printSection();
				try {
					databaseResetDialog(dbconnection, reader);
				}
				catch(IOException ex) {
					ex.printStackTrace();
				}
			} else {
				println("invalid input");
			}

			if (!userInput.equals("x")) {
				printMenu();
			}
		}
	}

	private static void println(String string) {
		System.out.println(string);
	}
	
	private static void print(String string) {
		System.out.print(string);
	}
	
	private static void printSection() {
		println("----------------------------------------------------------------------");
	}
	
	private static void printMenu() {
		printSection();
		println("Digital Watermarking of Medical Sensor Data for Data Leakage Detection");
		println("Proof of Concept Prototype");
		printSection();
		println("[1] Data Generation");
		println("[2] Watermark Embedding");
		println("[3] Watermark Detection");
		println("[4] Attack Evaluation");
		println("[5] Database Reset");
		println("[x] Exit");
	}

	private static void dataGenerationDialog(DBConnection dbconnection, BufferedReader reader) throws ParseException, IOException {
		String userInput = new String();
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		
		println("Data Generation Options");
		println("[x] Cancel");
		
		print("how many patients? ");
		userInput = reader.readLine();
		
		if(userInput.equals("x")) {
			println("data generation canceled");
		}
		else if(userInput.matches("[0-9]+")) {
			int noOfPatients = Integer.parseInt(userInput);
			
			print("timespan (from <yyyy-MM-dd>)? ");
			userInput = reader.readLine();
			
			if(userInput.equals("x")) {
				println("data generation canceled");
			}
			else if(userInput.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
				Date from = formatter.parse(userInput);
				
				print("timespan (to <yyyy-MM-dd>)? ");
				userInput = reader.readLine();

				if(userInput.equals("x")) {
					println("data generation canceled");
				}
				else if(userInput.matches("[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]")) {
					Date to = formatter.parse(userInput);
					
					if(from.before(to)) {
						println("generating data ...");
						//TODO generate data
						println("data generated");
						
						println("storing generated data in the database ...");
						//TODO store data in db
						println("generated data stored in the database");
					}
					else {
						println("\"from\" date cannot be after \"to\" date");
					}
				}
				else {
					println("invalid input");
				}
			}
			else {
				println("invalid input");
			}
		}
		else {
			println("invalid input");
		}
		
	}
	
	private static void databaseResetDialog(DBConnection dbconnection, BufferedReader reader) throws IOException {
		String userInput = new String();
		
		println("Database Reset Options");
		println("[1] Fragment Table");
		println("[2] Log Table");
		println("[3] Fragment and Log Table");
		println("[x] Cancel");
		
		print("what do you want to reset? ");
		userInput = reader.readLine();
		
		if(userInput.equals("x")) {
			println("database reset canceled");
		}
		else if(userInput.equals("1")) {
			println("reseting fragment table ...");
			dbconnection.resetFragmentTable();
			println("fragment table reset");
		}
		else if(userInput.equals("2")) {
			println("reseting log table ...");
			dbconnection.resetLogTable();
			println("log table reset");
		}
		else if(userInput.equals("3")) {
			println("reseting fragment table ...");
			dbconnection.resetFragmentTable();
			println("fragment table reset");
			println("reseting log table");
			dbconnection.resetLogTable();
			println("log table reset");
		}
		else {
			println("invalid input");
		}
	}

}
