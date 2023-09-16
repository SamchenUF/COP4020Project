package edu.ufl.cise.cop4020fa23;

public class tester {
    public static void main (String args[]) {
        String input = """
		     , [ ]
			##{ }.
			% + /
			? !;
			""";
        char[] arr = input.toCharArray();
        System.out.println(input.charAt(19));
    }
}
