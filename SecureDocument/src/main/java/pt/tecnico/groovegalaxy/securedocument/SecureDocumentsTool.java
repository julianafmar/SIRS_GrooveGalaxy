package pt.tecnico.groovegalaxy.securedocument;

public class SecureDocumentsTool {

    public static void main(String[] args) throws Exception {
        System.out.println("Hi! I'm Secure Documents: a tool you can use to make your documents secure!");
        if (args.length < 1) {
            System.err.println("Argument(s) missing!");
            System.err.println("Usage: secure-document [operation] [operation_args]*");
            System.err.println("Type 'secure-document help' for more information about the possible operations");
            return;
        }
        String operation = args[0];
        if(operation.equals("help")) {
            System.out.println();
            System.out.println("Usage: secure-document [operation] [operation_args]*");
            System.out.println("Possible operations and respective arguments:");
            System.out.println("  - protect [input_file] [output_file] [key]");
            System.out.println("      |-> protects [input_file] with [key] and saves the result in [output_file]");
            System.out.println("  - unprotect [input_file] [output_file] [key]");
            System.out.println("      |-> unprotects [input_file] with [key] and saves the result in [output_file]");
            System.out.println("  - check [input_file] [key]");
            System.out.println("      |-> checks the integrity and authenticity of [input_file] with [key]");
            System.out.println("  - help");
            System.out.println("      |-> shows help about the possible operations");
            return;
        } else if(operation.equals("protect")){
            if(args.length != 4) {
                System.err.println("Argument(s) missing!");
                System.err.println("Usage: secure-document protect [input_file] [output_file] [key]");
                return;
            }
            Protect.protect_file(args[1], args[2], args[3], true);
        } else if(operation.equals("unprotect")){
            if(args.length != 4) {
                System.err.println("Argument(s) missing!");
                System.err.println("Usage: secure-document unprotect [input_file] [output_file] [key]");
                return;
            }
            Unprotect.unprotect_file(args[1], args[2], args[3], true);
        } else if(operation.equals("check")){
            if(args.length != 3) {
                System.err.println("Argument(s) missing!");
                System.err.println("Usage: secure-document check [input_file] [key]");
                return;
            }
            Check.check_file(args[1], args[2], true);
        } else {
            System.err.println("Unknown operation!");
            System.err.println("Usage: secure-document [operation] [operation_args]*");
            System.err.println("Type 'secure-document help' for more information about the possible operations");
            return;
        }
    }
}
