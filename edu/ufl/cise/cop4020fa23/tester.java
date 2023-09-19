package edu.ufl.cise.cop4020fa23;

public class tester {
    public static void main (String args[]) {
        String input =  """
            \s"strange"
           
                 "positioning"
                  
                     blue
                     """;
        char[] arr = input.toCharArray();
        for(int i = 0; i < input.length(); i++) {
        if(input.charAt(i) == ' ') {
            System.out.println("Space");
        }
        else if(input.charAt(i) == '\n') {
            System.out.println("newline");
        }
        else
            System.out.println(input.charAt(i));}
    }
}
