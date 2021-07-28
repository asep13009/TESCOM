package com.asep.test.testno1;

import java.util.Scanner;

public class Test1 {

	public static void main(String[] args) {
		Scanner sc= new Scanner(System.in);     
		System.out.print("Enter number : ");  
		int a= sc.nextInt();  
		for (int i=1;i<=a;i++){
            for(int j=5;j>=i;j--){
                System.out.print(" ");
            }
            for(int k=1;k<=i;k++){
                System.out.print("#");
            }
            System.out.println();
        }

	}
		 

}
