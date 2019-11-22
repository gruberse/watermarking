package ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import utils.Database;

public class Controller {

	public static void main(String[] args) {
		try {
			printMenu();
			String input = "";
			BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
			while (!input.equals("x")) {
				print("what do you want to do?");
				input = reader.readLine();
				if(input.equals("x")) {
					print("good bye");
				}
				else if(input.equals("1")) {
					//TODO
				}
				else if(input.equals("2")) {
					//TODO
				}
				else if(input.equals("3")) {
					//TODO
				}
				else if(input.equals("4")) {
					//TODO
				}
				else {
					//print("invalid input");
					Database db = new Database();
				}

				if (!input.equals("x")) {
					printMenu();
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private static void printMenu() {
		print("Digital Watermarking of Medical Sensor Data for Data Leakage Detection");
		print("Proof of Concept Prototype");
		print("----------------------------------------------------------------------");
		print("[1] Watermark Embedding");
		print("[2] Watermark Detection");
		print("[3] Test Data Generation");
		print("[4] Attack Execution");
		print("[x] Exit");
	}

	private static void print(String string) {
		System.out.println(string);
	}

}
