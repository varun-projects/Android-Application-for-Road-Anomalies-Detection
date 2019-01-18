package com.example.varun_garg.orientation_corection;

import java.util.Scanner;

/** Class CircularBufferTest  **/
public class CircularBufferTest
{
    public static void main(String[] args)
    {
        Scanner scan = new Scanner(System.in);

        System.out.println("Circular Buffer Test\n");
        System.out.println("Enter Size of Buffer ");
        int n = scan.nextInt();
        /* creating object of class CircularBuffer */
        CircularBuffer cb = new CircularBuffer(n);

        /* Perform Circular Buffer Operations */
        char ch;

        do
        {
            System.out.println("\nCircular Buffer Operations");
            System.out.println("1. insert");
            System.out.println("2. remove");
            System.out.println("3. size");
            System.out.println("4. check empty");
            System.out.println("5. check full");
            System.out.println("6. clear");

            int choice = scan.nextInt();
            switch (choice)
            {
                case 1 :
                    System.out.println("Enter character to insert");
                    cb.insert( scan.next().charAt(0) );
                    break;
                case 2 :
                    System.out.println("Removed Element = "+ cb.delete());
                    break;
                case 3 :
                    System.out.println("Size = "+ cb.getSize());
                    break;
                case 4 :
                    System.out.println("Empty status = "+ cb.isEmpty());
                    break;
                case 5 :
                    System.out.println("Full status = "+ cb.isFull());
                    break;
                case 6 :
                    System.out.println("\nBuffer Cleared\n");
                    cb.clear();
                    break;
                default : System.out.println("Wrong Entry \n ");
                    break;
            }
            /* display Buffer */
            cb.display();

            System.out.println("\nDo you want to continue (Type y or n) \n");
            ch = scan.next().charAt(0);

        } while (ch == 'Y'|| ch == 'y');
    }

}
