.PHONY: clean run-lexer run-parser run-lexer-parser

# Compilation command for lexer and parser
make:
	@javac lexer/*.java parser/*.java

run-parser:
	@java -cp parser parser.Main  # Run the parser with its package

run-lexer:
	@java -cp lexer Main  # Specify classpath to the lexer directory

run-lexer-parser:
	@make run-lexer  # Run the lexer first
	@make run-parser  # Then run the parser

clean:
	@rm -f lexer/*.class
	@rm -f parser/*.class
